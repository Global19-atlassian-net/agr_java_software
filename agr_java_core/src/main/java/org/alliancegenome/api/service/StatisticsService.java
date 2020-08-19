package org.alliancegenome.api.service;

import org.alliancegenome.cache.repository.AlleleCacheRepository;
import org.alliancegenome.cache.repository.helper.JsonResultResponse;
import org.alliancegenome.core.ColumnStats;
import org.alliancegenome.core.ColumnValues;
import org.alliancegenome.core.StatisticRow;
import org.alliancegenome.core.TransgenicAlleleStats;
import org.alliancegenome.es.model.query.FieldFilter;
import org.alliancegenome.es.model.query.Pagination;
import org.alliancegenome.neo4j.entity.SpeciesType;
import org.alliancegenome.neo4j.entity.node.Allele;
import org.alliancegenome.neo4j.entity.node.Construct;
import org.alliancegenome.neo4j.entity.node.GeneticEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

public class StatisticsService<Entity> {

    @Inject
    private AlleleCacheRepository cacheRepository = new AlleleCacheRepository();


    public JsonResultResponse<StatisticRow> getAllTransgenicAlleles(Pagination pagination) {
        final Map<String, List<Allele>> alleleMap = cacheRepository.getAllTransgenicAlleles();
        String species = pagination.getFieldFilterValueMap().get(FieldFilter.SPECIES);
        String geneSpeciesTaxon = SpeciesType.getTaxonId(species);

        // filter by gene species
        Map<String, List<Allele>> filteredAlleleMap = alleleMap.entrySet().stream()
                .filter(entry -> {
                    if (geneSpeciesTaxon != null) {
                        return geneSpeciesTaxon.equals(SpeciesType.getTypeByID(entry.getKey().split("\\|\\|")[2]).getTaxonID());
                    } else
                        return true;
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        // filter by sub entity
        String subEntityFilter = pagination.getFieldFilterValueMap().get(FieldFilter.SUB_ENTITY);
        if (subEntityFilter != null && !subEntityFilter.isEmpty()) {
            Map<String, List<Allele>> filteredAlleleMap1;
            String[] subEntitySplit = subEntityFilter.split(";");
            String subEntityName = subEntitySplit[0];
            String subEntityFilterValue = subEntitySplit[1];
            ColumnStats<Allele, ?> columnStats = getSubEntityRowColumn().stream()
                    .filter(col -> col.getName().equals(subEntityName)).findFirst().get();

            filteredAlleleMap1 = filteredAlleleMap.entrySet().stream()
                    .filter(entry ->
                            entry.getValue().stream()
                                    .anyMatch(allele -> columnStats.getSingleValuefunction().apply(allele).equalsIgnoreCase(subEntityFilterValue))
                    )
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            filteredAlleleMap = filteredAlleleMap1;
        }

        List<StatisticRow> rows = new ArrayList<>();
        filteredAlleleMap.forEach((gene, alleles) -> {
            StatisticRow row = new StatisticRow();
            ColumnStats geneStat = new ColumnStats("Gene", true, false, false, false);
            ColumnValues columnValues = new ColumnValues();
            columnValues.setValue(gene);
            row.put(geneStat, columnValues);

            ColumnStats geneSpecies = new ColumnStats("Gene Species", true, false, false, true);
            ColumnValues genSpecValue = new ColumnValues();
            genSpecValue.setValue(SpeciesType.getTypeByID(gene.split("\\|\\|")[2]).getAbbreviation());
            row.put(geneSpecies, genSpecValue);

            ColumnStats<Allele, ?> alleleColStat = getRowEntityColumn();
            ColumnValues alleleValue = new ColumnValues();
            alleleValue.setTotalNumber(alleles.size());
            row.put(alleleColStat, alleleValue);

            getSubEntityRowColumn().forEach(columnStats -> {
                ColumnValues columnValues1 = new ColumnValues();
                if (columnStats.isMultiValued()) {
                    columnValues1.setTotalNumber(getTotalNumberPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                    columnValues1.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                    columnValues1.setCardinality(getCardinalityPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                } else {
                    // calculate the histogram of possible values
                    if (columnStats.getSingleValuefunction() != null) {
                        Map<String, List<String>> unSortedHistogram = alleles.stream()
                                .map(columnStats.getSingleValuefunction())
                                .collect(toList())
                                .stream()
                                .collect(groupingBy(o -> o));
                        Map<String, Integer> sortedHistogram = getValueSortedMap(unSortedHistogram);
                        columnValues1.setHistogram(sortedHistogram);
                        columnValues1.setTotalDistinctNumber(sortedHistogram.size());
                        columnValues1.setTotalNumber(sortedHistogram.size());
                    }
                }
                row.put(columnStats, columnValues1);

            });

            rows.add(row);

        });

        // sorting

        // default sorting: by number of Alleles
        if (StringUtils.isNotEmpty(pagination.getSortBy())) {
            String sortByField = pagination.getSortBy();
            ColumnStats<Allele, ?> columnStats = getSubEntityRowColumn().stream()
                    .filter(col -> col.getName().equals(sortByField)).findFirst().get();
            rows.sort(comparing(statisticRow -> (Integer)statisticRow.getColumns().entrySet().stream()
                    .filter(entry -> entry.getValue().getColumnDefinition().equals(columnStats))
                    .findAny().get().getValue().getColumnStat().getCardinality().getMaximum()));
            Collections.reverse(rows);
        } else {
            rows.sort(comparing(statisticRow -> statisticRow.getColumns().entrySet().stream()
                    .filter(entry -> entry.getValue().getColumnDefinition().isRowEntity())
                    .findAny().get().getValue().getColumnStat().getTotalNumber()));
            Collections.reverse(rows);
        }
        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setTotal(rows.size());
        response.setResults(rows.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        response.addSupplementalData("statistic", getAllTransgenicAlleleStat(filteredAlleleMap));
        return response;
    }

    public TransgenicAlleleStats getAllTransgenicAlleleStat() {
        Map<String, List<Allele>> alleleMap = cacheRepository.getAllTransgenicAlleles();
        return getAllTransgenicAlleleStat(alleleMap);
    }

    public TransgenicAlleleStats getAllTransgenicAlleleStat(final Map<String, List<Allele>> alleleMap) {

        TransgenicAlleleStats stat = new TransgenicAlleleStats();

        ColumnStats gene = new ColumnStats("Gene", true, false, false, false);
        ColumnValues geneValues = new ColumnValues();
        geneValues.setTotalNumber(alleleMap.size());
        geneValues.setTotalDistinctNumber(alleleMap.size());
        stat.addColumn(gene, geneValues);

        ColumnStats geneSpecies = new ColumnStats("Gene Species", true, false, false, true);

        List<String> species = alleleMap.keySet().stream()
                .map(pk -> SpeciesType.getTypeByID(pk.split("\\|\\|")[2]).getAbbreviation())
                .collect(toList());
        Map<String, List<String>> sortSpecies = species.stream()
                .collect(groupingBy(o -> o));

        ColumnValues geneSpeciesValues = new ColumnValues();
        geneSpeciesValues.setHistogram(getValueSortedMap(sortSpecies));
        geneSpeciesValues.setTotalDistinctNumber(getValueSortedMap(sortSpecies).size());
        stat.addColumn(geneSpecies, geneSpeciesValues);

        ColumnStats speciesTG = new ColumnStats("Species, (carrying the transgene", false, false, false, true);

        Map<String, List<String>> sortSpeciesTG = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .filter(allele -> allele.getSpecies() != null)
                .filter(allele -> allele.getSpecies().getType() != null)
                .map(allele -> allele.getSpecies().getType().getAbbreviation())
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> o));

        Map<String, Integer> speciesTGHisto = getValueSortedMap(sortSpeciesTG);

        ColumnValues speciesTGValues = new ColumnValues();
        speciesTGValues.setHistogram(speciesTGHisto);
        speciesTGValues.setTotalDistinctNumber(speciesTGHisto.size());
        stat.addColumn(speciesTG, speciesTGValues);

        ColumnStats allele = new ColumnStats("Allele Symbol", false, true, false, false);
        ColumnValues alleleValues = new ColumnValues();
        alleleValues.setTotalNumber(alleleMap.values().stream().mapToInt(List::size).sum());
        List<String> distinctAlleles = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(GeneticEntity::getPrimaryKey)
                .distinct()
                .collect(toList());
        alleleValues.setTotalDistinctNumber(distinctAlleles.size());

        // Allele ID, List<GeneID>
        Map<String, List<String>> alleleOCa = new HashMap<>();
        alleleMap.forEach((geneAl, alleles) -> {
            alleles.forEach(allele2 -> {
                String alleleID = allele2.getPrimaryKey();
                List<String> geneList = alleleOCa.computeIfAbsent(alleleID, k -> new ArrayList<>());
                geneList.add(geneAl);
            });
        });
        Map<String, Integer> alleleOCardinality = getValueSortedMap(alleleOCa);
        if (MapUtils.isNotEmpty(alleleOCardinality)) {
            Range<Integer> alleleParentRange = Range.between(Collections.max(alleleOCardinality.values()), Collections.min(alleleOCardinality.values()));
            alleleValues.setCardinalityParent(alleleParentRange);
        }

        Map<String, Integer> alleleCardinality = alleleMap.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, o -> o.getValue().size(),
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new));

        if (MapUtils.isNotEmpty(alleleCardinality)) {
            Range<Integer> alleleRange = Range.between(Collections.max(alleleCardinality.values()), Collections.min(alleleCardinality.values()));
            alleleValues.setCardinality(alleleRange);
        }
        stat.addColumn(allele, alleleValues);

        ColumnStats synonym = new ColumnStats("Synonym", false, false, true, false);
        ColumnValues synonymValues = new ColumnValues();
        synonymValues.setTotalNumber(getTotalNumber(alleleMap, Allele::getSynonyms));
        synonymValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getSynonymList));
        synonymValues.setCardinality(getCardinality(alleleMap, Allele::getSynonymList));
        stat.addColumn(synonym, synonymValues);

        ColumnStats tg = new ColumnStats("Construct", false, false, true, false);
        ColumnValues tgValues = new ColumnValues();
        tgValues.setTotalNumber(getTotalNumber(alleleMap, Allele::getConstructs));
        tgValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getConstructs));
        tgValues.setCardinality(getCardinality(alleleMap, Allele::getConstructs));
        stat.addColumn(tg, tgValues);

        ColumnStats expressedComponents = new ColumnStats("Expressed Components", false, false, true, false);
        ColumnValues expressedValues = new ColumnValues();
        Function<Allele, List<GeneticEntity>> expressedComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getExpressedGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        expressedValues.setTotalNumber(getTotalNumber(alleleMap, expressedComponentFunction));
        expressedValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, expressedComponentFunction));
        expressedValues.setCardinality(getCardinality(alleleMap, expressedComponentFunction));
        stat.addColumn(expressedComponents, expressedValues);

        ColumnStats targetedComponent = new ColumnStats("Knock-down Target", false, false, true, false);
        ColumnValues targetedValues = new ColumnValues();
        Function<Allele, List<GeneticEntity>> targetComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getTargetGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        targetedValues.setTotalNumber(getTotalNumber(alleleMap, targetComponentFunction));
        targetedValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, targetComponentFunction));
        targetedValues.setCardinality(getCardinality(alleleMap, targetComponentFunction));
        stat.addColumn(targetedComponent, targetedValues);

        ColumnStats regulatoryComponent = new ColumnStats("Regulatory Region", false, false, true, false);
        ColumnValues regulatedValues = new ColumnValues();
        Function<Allele, List<GeneticEntity>> regulatoryComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getRegulatedByGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        regulatedValues.setTotalNumber(getTotalNumber(alleleMap, regulatoryComponentFunction));
        regulatedValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, regulatoryComponentFunction));
        regulatedValues.setCardinality(getCardinality(alleleMap, regulatoryComponentFunction));
        stat.addColumn(regulatoryComponent, regulatedValues);

        ColumnStats diseaseStat = new ColumnStats("Associated Human Disease", false, false, false, true);
        ColumnValues diseaseValues = new ColumnValues();
        Function<Allele, String> diseaseFunction = feature -> feature.hasDisease().toString();
/*
        diseaseStat.setTotalNumber(getTotalNumber(alleleMap, disease));
        diseaseStat.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, disease));
        diseaseStat.setCardinality(getCardinality(alleleMap, disease));
*/
        diseaseValues.setHistogram(getHistogram(alleleMap, diseaseFunction));
        stat.addColumn(diseaseStat, diseaseValues);

        ColumnStats phenotypeStat = new ColumnStats("Associated Phenotype", false, false, false, true);
        ColumnValues phenotypeValues = new ColumnValues();
        Function<Allele, String> phenotypeFunction = feature -> feature.hasPhenotype().toString();
/*
        diseaseStat.setTotalNumber(getTotalNumber(alleleMap, disease));
        diseaseStat.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, disease));
        diseaseStat.setCardinality(getCardinality(alleleMap, disease));
*/
        phenotypeValues.setHistogram(getHistogram(alleleMap, phenotypeFunction));
        stat.addColumn(phenotypeStat, phenotypeValues);

        return stat;
    }

    private static <ID> Range getCardinality(Map<String, List<Allele>> alleleMap, Function<Allele, List<ID>> function) {
        Map<String, Integer> cardinality = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .collect(toMap(Allele::getPrimaryKey,
                        o -> function.apply(o).size(),
                        // if duplicates occur just take one.
                        (a1, a2) -> a1));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <ID> Range getCardinalityPerUberEntity(List<Allele> alleles, Function<Allele, List<ID>> function) {
        Map<String, Integer> cardinality = alleles.stream()
                .collect(toMap(Allele::getPrimaryKey,
                        o -> function.apply(o).size(),
                        // if duplicates occur just take one.
                        (a1, a2) -> a1));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <ID> int getTotalNumber(Map<String, List<Allele>> alleleMap, Function<Allele, List<ID>> function) {
        return alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .mapToInt(List::size).sum();
    }

    private static <ID> int getTotalNumberPerUberEntity(List<Allele> alleles, Function<Allele, List<ID>> function) {
        return alleles.stream()
                .map(function)
                .mapToInt(List::size).sum();
    }

    private static <ID> int getTotalDistinctNumber(Map<String, List<Allele>> alleleMap, Function<Allele, List<ID>> function) {
        return alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .flatMap(Collection::stream)
                .collect(toSet())
                .size();
    }

    private static <ID> int getTotalDistinctNumberPerUberEntity(List<Allele> alleles, Function<Allele, List<ID>> function) {
        return alleles.stream()
                .map(function)
                .flatMap(Collection::stream)
                .collect(toSet())
                .size();
    }

    private static Map<String, Integer> getHistogram(Map<String, List<Allele>> alleleMap, Function<Allele, String> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> o));
        return getValueSortedMap(histogramRaw);
    }

    private static Map<String, Integer> getValueSortedMap(Map<String, List<String>> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 0)
                .sorted(comparingInt(entry -> -entry.getValue().size()))
                .collect(toMap(
                        Map.Entry::getKey,
                        o -> o.getValue().size(),
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }

    private ColumnStats getRowEntityColumn() {
        final Optional<ColumnStats<Allele, ?>> any = stats.stream().filter(ColumnStats::isRowEntity).findAny();
        if (any.isEmpty())
            throw new RuntimeException("Missing row entity column");
        return any.get();
    }

    private List<ColumnStats<Allele, ?>> getSubEntityRowColumn() {
        return stats.stream()
                .filter(columnStats -> !columnStats.isRowEntity())
                .filter(columnStats -> !columnStats.isSuperEntity())
                .collect(toList());
    }

    private List<ColumnStats<Allele, ?>> stats;

    public void add(List<ColumnStats<Allele, ?>> stats) {
        this.stats = stats;
    }
}
