package org.alliancegenome.api.service;

import org.alliancegenome.cache.repository.AlleleCacheRepository;
import org.alliancegenome.core.ColumnStats;
import org.alliancegenome.core.TransgenicAlleleStats;
import org.alliancegenome.neo4j.entity.SpeciesType;
import org.alliancegenome.neo4j.entity.node.Allele;
import org.alliancegenome.neo4j.entity.node.Construct;
import org.alliancegenome.neo4j.entity.node.GeneticEntity;
import org.apache.commons.lang3.Range;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

@RequestScoped
public class StatisticsService {

    @Inject
    private AlleleCacheRepository cacheRepository = new AlleleCacheRepository();


    public TransgenicAlleleStats getAllTransgenicAlleles() {
        Map<String, List<Allele>> alleleMap = cacheRepository.getAllTransgenicAlleles();

        TransgenicAlleleStats stat = new TransgenicAlleleStats();

        ColumnStats gene = new ColumnStats("Gene", true, false, false, false);
        gene.setTotalNumber(alleleMap.size());
        gene.setTotalDistinctNumber(alleleMap.size());
        stat.addColumn(gene);

        ColumnStats geneSpecies = new ColumnStats("Gene Species", true, false, false, true);

        List<String> species = alleleMap.keySet().stream()
                .map(pk -> SpeciesType.getTypeByID(pk.split("\\|\\|")[2]).getAbbreviation())
                .collect(toList());
        Map<String, List<String>> sortSpecies = species.stream()
                .collect(groupingBy(o -> o));

        Map<String, Integer> sorted = getValueSortedMap(sortSpecies);
        geneSpecies.setHistogram(sorted);
        geneSpecies.setTotalDistinctNumber(sorted.size());
        stat.addColumn(geneSpecies);

        ColumnStats speciesTG = new ColumnStats("Species, (carrying the transgene", false, false, false, true);

        alleleMap.values().forEach(alleles -> {
            alleles.stream()
                    .filter(allele -> allele.getSpecies() != null)
                    .filter(allele -> allele.getSpecies().getType() == null)
                    .collect(toList())
                    .forEach(allele -> {
//                                System.out.println("Missing Species Type info for Allele: ");
                                //                              System.out.println(allele.getPrimaryKey() + " " + allele.getSymbolText());
                            }
                    );
        });

        Map<String, List<String>> sortSpeciesTG = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .filter(allele -> allele.getSpecies() != null)
                .filter(allele -> allele.getSpecies().getType() != null)
                .map(allele -> allele.getSpecies().getType().getAbbreviation())
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> o));

        Map<String, Integer> speciesTGHisto = getValueSortedMap(sortSpeciesTG);
        speciesTG.setHistogram(speciesTGHisto);
        speciesTG.setTotalDistinctNumber(speciesTGHisto.size());
        stat.addColumn(speciesTG);

        ColumnStats allele = new ColumnStats("Allele Symbol", false, true, false, false);
        allele.setTotalNumber(alleleMap.values().stream().mapToInt(List::size).sum());
        List<String> distinctAlleles = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(GeneticEntity::getPrimaryKey)
                .distinct()
                .collect(toList());
        allele.setTotalDistinctNumber(distinctAlleles.size());

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
        Range<Integer> alleleParentRange = Range.between(Collections.max(alleleOCardinality.values()), Collections.min(alleleOCardinality.values()));
        allele.setCardinalityParent(alleleParentRange);

        Map<String, Integer> alleleCardinality = alleleMap.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, o -> o.getValue().size(),
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new));

        Range<Integer> alleleRange = Range.between(Collections.max(alleleCardinality.values()), Collections.min(alleleCardinality.values()));
        allele.setCardinality(alleleRange);
        stat.addColumn(allele);

        ColumnStats synonym = new ColumnStats("Synonym", false, false, true, false);
        synonym.setTotalNumber(getTotalNumber(alleleMap, Allele::getSynonyms));
        synonym.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getSynonymList));
        synonym.setCardinality(getCardinality(alleleMap, Allele::getSynonymList));
        stat.addColumn(synonym);

        ColumnStats tg = new ColumnStats("Construct", false, false, true, false);
        tg.setTotalNumber(getTotalNumber(alleleMap, Allele::getConstructs));
        tg.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getConstructs));
        tg.setCardinality(getCardinality(alleleMap, Allele::getConstructs));
        stat.addColumn(tg);

        ColumnStats expressedComponents = new ColumnStats("Expressed Components", false, false, true, false);
        Function<Allele, List<GeneticEntity>> expressedComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getExpressedGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        expressedComponents.setTotalNumber(getTotalNumber(alleleMap, expressedComponentFunction));
        expressedComponents.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, expressedComponentFunction));
        expressedComponents.setCardinality(getCardinality(alleleMap, expressedComponentFunction));
        stat.addColumn(expressedComponents);

        ColumnStats targetedComponent = new ColumnStats("Knock-down Target", false, false, true, false);
        Function<Allele, List<GeneticEntity>> targetComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getTargetGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        targetedComponent.setTotalNumber(getTotalNumber(alleleMap, targetComponentFunction));
        targetedComponent.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, targetComponentFunction));
        targetedComponent.setCardinality(getCardinality(alleleMap, targetComponentFunction));
        stat.addColumn(targetedComponent);

        ColumnStats regulatoryComponent = new ColumnStats("Regulatory Region", false, false, true, false);
        Function<Allele, List<GeneticEntity>> regulatoryComponentFunction = feature ->
                feature.getConstructs().stream()
                        .map(Construct::getRegulatedByGenes)
                        .flatMap(Collection::stream)
                        .collect(toList());
        ;
        regulatoryComponent.setTotalNumber(getTotalNumber(alleleMap, regulatoryComponentFunction));
        regulatoryComponent.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, regulatoryComponentFunction));
        regulatoryComponent.setCardinality(getCardinality(alleleMap, regulatoryComponentFunction));
        stat.addColumn(regulatoryComponent);

        ColumnStats diseaseStat = new ColumnStats("Associated Human Disease", false, false, false, true);
        Function<Allele, String> diseaseFunction = feature -> feature.hasDisease().toString();
/*
        diseaseStat.setTotalNumber(getTotalNumber(alleleMap, disease));
        diseaseStat.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, disease));
        diseaseStat.setCardinality(getCardinality(alleleMap, disease));
*/
        diseaseStat.setHistogram(getHistogram(alleleMap, diseaseFunction));
        stat.addColumn(diseaseStat);

        return stat;
    }

    private static <ID> Range getCardinality(Map<String, List<Allele>> alleleMap, Function<Allele, List<ID>> function) {
        Map<String, Integer> cardinality = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .collect(toMap(Allele::getPrimaryKey,
                        o -> function.apply(o).size(),
                        // if duplicates occur just take one.
                        (a1, a2) -> a1));
        return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
    }

    private static <ID> int getTotalNumber(Map<String, List<Allele>> alleleMap, Function<Allele, List<ID>> function) {
        return alleleMap.values().stream()
                .flatMap(Collection::stream)
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

}
