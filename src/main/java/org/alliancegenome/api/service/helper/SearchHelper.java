package org.alliancegenome.api.service.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.UriInfo;

import org.alliancegenome.api.model.AggDocCount;
import org.alliancegenome.api.model.AggResult;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.RandomScoreFunctionBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.jboss.logging.Logger;

@RequestScoped
@SuppressWarnings("serial")
public class SearchHelper {

	private Logger log = Logger.getLogger(getClass());

	private HashMap<String, List<String>> category_filters = new HashMap<String, List<String>>() {
		{
			put("gene", new ArrayList<String>() {
				{
					add("soTermName");
					add("gene_biological_process");
					add("gene_molecular_function");
					add("gene_cellular_component");
					add("species");
				}
			});
			put("go", new ArrayList<String>() {
				{
					add("go_type");
					add("go_species");
					add("go_genes");
				}
			});
			put("disease", new ArrayList<String>() {
				{
					add("disease_species");
					add("disease_genes");
				}
			});
		}
	};

	private HashMap<String, Integer> custom_boosts = new HashMap<String, Integer>() {
		{
			put("primaryId", 400);
			put("secondaryIds", 100);
			put("symbol", 500);
			put("symbol.raw", 1000);
			put("synonyms", 120);
			put("synonyms.raw", 200);
			put("name", 100);
			put("name.symbol", 200);
			put("gene_biological_process.symbol", 50);
			put("gene_molecular_function.symbol", 50);
			put("gene_cellular_component.symbol", 50);
			put("diseases.do_name", 50);
		}
	};

	private List<String> search_fields = new ArrayList<String>() {
		{
			add("primaryId"); add("secondaryIds"); add("name"); add("symbol"); add("symbol.raw"); add("synonyms"); add("synonyms.raw");
			add("description"); add("external_ids"); add("species"); add("gene_biological_process"); add("gene_molecular_function");
			add("gene_cellular_component"); add("go_type"); add("go_genes"); add("go_synonyms"); add("disease_genes"); add("disease_synonyms");
			add("diseases.do_name");
		}
	};

	private List<String> special_search_fields = new ArrayList<String>() {
		{
			add("name.symbol");
			add("gene_biological_process.symbol");
			add("gene_molecular_function.symbol");
			add("gene_cellular_component.symbol");
		}
	};

	private List<String> highlight_blacklist_fields = new ArrayList<String>() {
		{
			add("go_genes");
		}
	};



	public List<AggregationBuilder> createAggBuilder(String category) {
		List<AggregationBuilder> ret = new ArrayList<AggregationBuilder>();

		if(category == null || !category_filters.containsKey(category)) {
			TermsAggregationBuilder term = AggregationBuilders.terms("categories");
			term.field("category");
			term.size(50);
			ret.add(term);
		} else {
			for(String item: category_filters.get(category)) {
				TermsAggregationBuilder term = AggregationBuilders.terms(item);
				term.field(item + ".raw");
				term.size(999);
				ret.add(term);
			}
		}

		return ret;
	}


	public ArrayList<AggResult> formatAggResults(String category, SearchResponse res) {
		ArrayList<AggResult> ret = new ArrayList<AggResult>();

		if(category == null) {

			Terms aggs = res.getAggregations().get("categories");

			AggResult ares = new AggResult("category");
			for (Terms.Bucket entry : aggs.getBuckets()) {
				ares.values.add(new AggDocCount(entry.getKeyAsString(), entry.getDocCount()));
			}
			ret.add(ares);

		} else {
			if(category_filters.containsKey(category)) {
				for(String item: category_filters.get(category)) {
					Terms aggs = res.getAggregations().get(item);

					AggResult ares = new AggResult(item);
					for (Terms.Bucket entry : aggs.getBuckets()) {
						ares.values.add(new AggDocCount(entry.getKeyAsString(), entry.getDocCount()));
					}
					ret.add(ares);
				}
			}
		}

		return ret;
	}



	public QueryBuilder buildQuery(String q, String category, UriInfo uriInfo) {

		QueryBuilder query = QueryBuilders.matchAllQuery();

		if((q == null || q.equals("") || q.length() == 0) && (category == null || category.equals("") || category.length() == 0)) {
			RandomScoreFunctionBuilder rsfb = new RandomScoreFunctionBuilder();
			rsfb.seed(12345);
			return new FunctionScoreQueryBuilder(query, rsfb);
		}

		if(q == null || q.equals("") || q.length() == 0) {
			query = QueryBuilders.matchAllQuery();
		} else {
			query = buildSearchParams(q);
		}

		if(category == null || category.equals("") || category.length() == 0) {
			return query;
		} else {
			return buildCategoryQuery(query, category, uriInfo);
		}
	}


	public QueryBuilder buildSearchParams(String q) {

		q = q.replaceAll("\"", "");

		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		DisMaxQueryBuilder dis_max = new DisMaxQueryBuilder();
		bool.must(dis_max);
		ExistsQueryBuilder categoryExists = new ExistsQueryBuilder("category");
		bool.must(categoryExists);

		ArrayList<String> final_search_fields = new ArrayList<String>();
		final_search_fields.addAll(search_fields);
		final_search_fields.addAll(special_search_fields);

		for(String field: final_search_fields) {
			MatchQueryBuilder boostedMatchQuery = new MatchQueryBuilder(field, q);
			if(custom_boosts.containsKey(field)) {
				boostedMatchQuery.boost(custom_boosts.get(field));
			} else {
				boostedMatchQuery.boost(50);
			}
			dis_max.add(boostedMatchQuery);

			if(field.contains(".")) {
				dis_max.add(new MatchPhrasePrefixQueryBuilder(field.split("\\.")[0], q));
			} else {
				dis_max.add(new MatchPhrasePrefixQueryBuilder(field, q));
			}

		}

		return bool;
	}

	public QueryBuilder buildCategoryQuery(QueryBuilder query, String category, UriInfo uriInfo) {

		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		bool.must(query);
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category", category);
		bool.must(termQueryBuilder);

		//		for(Entry<String, List<String>> e: uriInfo.getQueryParameters().entrySet()) {
		//			System.out.println(e.getKey());
		//			System.out.println(e.getValue());
		//			System.out.println();
		//		}

		if(category_filters.containsKey(category)) {
			for(String item: category_filters.get(category)) {
				if(uriInfo.getQueryParameters().containsKey(item)) {
					for(String param: uriInfo.getQueryParameters().get(item)) {
						TermQueryBuilder termQuery = new TermQueryBuilder(item + ".raw", param);
						bool.must(termQuery);
					}
				}
			}
		}

		return bool;
	}


	public ArrayList<Map<String, Object>> formatResults(SearchResponse res) {
		log.info("Formatting Results: ");
		ArrayList<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		for(SearchHit hit: res.getHits()) {

			Map<String, Object> map = new HashMap<String, Object>();
			for(String key: hit.getHighlightFields().keySet()) {
				if(key.endsWith(".symbol")) {
					log.info("Source as String: " + hit.getSourceAsString());
					log.info("Highlights: " + hit.getHighlightFields());
				}
				ArrayList<String> list = new ArrayList<String>();
				for(Text t: hit.getHighlightFields().get(key).getFragments()) {
					list.add(t.string());
				}
				map.put(hit.getHighlightFields().get(key).getName(), list);
			}
			hit.getSource().put("highlights", map);
			hit.getSource().put("id", hit.getId());
			ret.add(hit.getSource());
		}
		log.info("Finished Formatting Results: ");
		return ret;
	}

	public HighlightBuilder buildHighlights() {

		HighlightBuilder hlb = new HighlightBuilder();

		for(String field: search_fields) {
			if(!highlight_blacklist_fields.contains(field)) {
				hlb.field(field);
			}
		}

		return hlb;
	}

}
