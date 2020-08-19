package org.alliancegenome.api.rest.interfaces;

import org.alliancegenome.api.service.StatisticsService;
import org.alliancegenome.cache.repository.helper.JsonResultResponse;
import org.alliancegenome.core.ColumnStats;
import org.alliancegenome.core.StatisticRow;
import org.alliancegenome.es.model.query.FieldFilter;
import org.alliancegenome.es.model.query.Pagination;
import org.alliancegenome.neo4j.entity.node.Allele;
import org.alliancegenome.neo4j.entity.node.Construct;
import org.alliancegenome.neo4j.entity.node.GeneticEntity;
import org.alliancegenome.neo4j.entity.node.Synonym;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class StatisticsController implements StatisticsRESTInterface {

    private StatisticsService<Allele> service = new StatisticsService<>();

    @Override
    public JsonResultResponse<StatisticRow> getTrans(String geneSpecies,
                                                     String filterSubEntity,
                                                     String sortBy) {
        long start = System.currentTimeMillis();
        Pagination pagination = new Pagination();
        pagination.addFieldFilter(FieldFilter.SPECIES, geneSpecies);
        pagination.addFieldFilter(FieldFilter.SUB_ENTITY, filterSubEntity);
        pagination.setSortBy(sortBy);
        List<ColumnStats<Allele, ?>> stats = List.of(
                new ColumnStats<>("Gene", true, false, false, false),
                new ColumnStats<>("Gene Species", true, false, false, true),
                new ColumnStats<>("Species, (carrying the transgene)", false, false, false, true, allele -> {
                    if (allele.getSpecies() != null && allele.getSpecies().getType() != null)
                        return allele.getSpecies().getType().getAbbreviation();
                    return "";
                }
                ),
                new ColumnStats<>("Allele Symbol", false, true, false, false),
                new ColumnStats<>("Synonym", false, false, true, false, Allele::getSynonyms,
                        allele -> allele.getSynonyms().stream().map(Synonym::getName).collect(Collectors.joining(","))),
                new ColumnStats<>("Construct", false, false, true, false, Allele::getConstructs,
                        allele -> allele.getConstructs().stream().map(Construct::getName).collect(Collectors.joining(","))),
                new ColumnStats<>("Expressed Components", false, false, true, false, allele ->
                        allele.getConstructs().stream()
                                .map(Construct::getExpressedGenes)
                                .flatMap(Collection::stream)
                                .collect(toList()),
                        allele -> allele.getConstructs().stream().map(Construct::getExpressedGenes).flatMap(Collection::stream).map(GeneticEntity::getSymbol).collect(Collectors.joining(","))),
                new ColumnStats<>("Knock-down Target", false, false, true, false, allele ->
                        allele.getConstructs().stream()
                                .map(Construct::getTargetGenes)
                                .flatMap(Collection::stream)
                                .collect(toList()),
                        allele -> allele.getConstructs().stream().map(Construct::getTargetGenes).flatMap(Collection::stream).map(GeneticEntity::getSymbol).collect(Collectors.joining(","))),
                new ColumnStats<>("Regulatory Region", false, false, true, false, allele ->
                        allele.getConstructs().stream()
                                .map(Construct::getRegulatedByGenes)
                                .flatMap(Collection::stream)
                                .collect(toList()),
                        allele -> allele.getConstructs().stream().map(Construct::getRegulatedByGenes).flatMap(Collection::stream).map(GeneticEntity::getSymbol).collect(Collectors.joining(","))),
                new ColumnStats<>("Associated Human Disease", false, false, false, true, allele -> allele.hasDisease().toString()),
                new ColumnStats<>("Associated Phenotype", false, false, false, true, allele -> allele.hasPhenotype().toString())
        );
        service.add(stats);
        JsonResultResponse<StatisticRow> response = service.getAllTransgenicAlleles(pagination);
        response.calculateRequestDuration(start);
        return response;
    }
}
