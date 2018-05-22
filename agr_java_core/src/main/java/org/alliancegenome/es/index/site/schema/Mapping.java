package org.alliancegenome.es.index.site.schema;

import java.io.IOException;

import org.alliancegenome.es.index.site.schema.mapping.*;
import org.elasticsearch.common.xcontent.XContentBuilder;

public abstract class Mapping extends Builder {
    
    public Mapping(Boolean pretty) {
        super(pretty);
    }

    public abstract void buildMappings();

    //Mappings that must be shared / equivalent across searchable documents
    protected void buildSharedSearchableDocumentMappings() throws IOException {

        new FieldBuilder(builder,"primaryId","keyword").build();
        new FieldBuilder(builder,"primaryKey","keyword").build();
        new FieldBuilder(builder,"category","keyword").symbol().autocomplete().keyword().build();
        new FieldBuilder(builder,"associationType","text").symbol().autocomplete().keyword().standardText().build();
        new FieldBuilder(builder,"name", "text")
                .symbol()
                .autocomplete()
                .keyword()
                .htmlSmoosh()
                .standardBigrams()
                .build();
        new FieldBuilder(builder,"name_key","text").analyzer("symbols")
                .autocomplete()
                .keyword()
                .htmlSmoosh()
                .standardBigrams()
                .build();
        new FieldBuilder(builder, "synonyms","text").analyzer("symbols")
                .autocomplete()
                .keyword()
                .htmlSmoosh()
                .standardBigrams()
                .build();
        new FieldBuilder(builder, "external_ids","text").analyzer("symbols");
        new FieldBuilder(builder, "href","keyword");
        new FieldBuilder(builder, "id","keyword");
        new FieldBuilder(builder, "description","text");
        new FieldBuilder(builder, "symbol","text").analyzer("symbols").autocomplete().keyword().sort().build();
        new FieldBuilder(builder, "species","text").keyword().synonym().sort().build();
    }

    protected void buildNestedDocument(String name) throws IOException {
        builder.startObject(name);
        builder.startObject("properties");
        //likely more fields than most will need, but the schema will be there for as many as are necessary
        buildSharedSearchableDocumentMappings();
        builder.endObject();
        builder.endObject();
    }

    public enum MappingClass {
        Disease("disease", DiseaseMapping.class),
        DiseaseAnnotation("diseaseAnnotation", DiseaseAnnotationMapping.class),
        Feature("feature", FeatureMapping.class),
        Gene("gene", GeneMapping.class),
        Go("go", GoMapping.class),
        ;

        private String type;
        private Class<?> mappingClass;

        private MappingClass(String type, Class<?> mappingClass) {
            this.type = type;
            this.mappingClass = mappingClass;
        }

        public String getType() {
            return type;
        }
        public Class<?> getMappingClass() {
            return mappingClass;
        }

    }

    public static class FieldBuilder {
        XContentBuilder builder;
        String name;
        String type;
        String analyzer;
        boolean autocomplete;
        boolean htmlSmoosh;
        boolean keyword;
        boolean sort;
        boolean standardBigrams;
        boolean standardText;
        boolean symbol;
        boolean synonym;

        public FieldBuilder(XContentBuilder builder, String name, String type) {
            this.builder = builder;
            this.name = name;
            this.type = type;
        }

        public FieldBuilder analyzer(String analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public FieldBuilder autocomplete() {
            this.autocomplete = true;
            return this;
        }

        public FieldBuilder htmlSmoosh() {
            this.htmlSmoosh = true;
            return this;
        }

        public FieldBuilder keyword() {
            this.keyword = true;
            return this;
        }

        public FieldBuilder sort() {
            this.sort = true;
            return this;
        }

        public FieldBuilder standardBigrams() {
            this.standardBigrams = true;
            return this;
        }

        public FieldBuilder standardText() {
            this.standardText = true;
            return this;
        }

        public FieldBuilder symbol() {
            this.symbol = true;
            return this;
        }

        public FieldBuilder synonym() {
            this.synonym = true;
            return this;
        }

        protected void buildProperty(String name, String type) throws IOException {
            buildProperty(name, type, null, null, null);
        }

        protected void buildProperty(String name, String type, String analyzer) throws IOException {
            buildProperty(name, type, analyzer, null, null);
        }

        protected void buildProperty(String name, String type, String analyzer, String search_analyzer, String normalizer) throws IOException {
            builder.startObject(name);
            if(type != null) builder.field("type", type);
            if(analyzer != null) builder.field("analyzer", analyzer);
            if(search_analyzer != null) builder.field("search_analyzer", search_analyzer);
            if(normalizer!= null) builder.field("normalizer", normalizer);
            builder.endObject();
        }


        public void build() throws IOException {
            builder.startObject(name);
            if(type != null) builder.field("type", type);
            if(analyzer != null) builder.field("analyzer", analyzer);
            if(symbol || autocomplete || keyword || synonym || sort || standardText) {
                builder.startObject("fields");
                if(keyword) { buildProperty("keyword", "keyword"); }
                if(symbol) { buildProperty("symbol", "text", "symbols"); }
                if(autocomplete) buildProperty("autocomplete", "text", "autocomplete", "autocomplete_search", null);
                if(synonym) buildProperty("synonyms", "text", "generic_synonym", "autocomplete_search", null);
                if(sort) buildProperty("sort", "keyword", null, null, "lowercase");
                if(htmlSmoosh) buildProperty("htmlSmoosh", "text", "html_smoosh");
                if(standardBigrams) buildProperty("standardBigrams", "text", "standard_bigrams");
                if(standardText) buildProperty("standardText", "text", "standard_text");
                builder.endObject();
            }
            builder.endObject();
        }
    }

}