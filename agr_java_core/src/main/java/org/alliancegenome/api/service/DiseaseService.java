package org.alliancegenome.api.service;

import org.alliancegenome.api.entity.DiseaseEntitySubgroupSlim;
import org.alliancegenome.api.entity.DiseaseRibbonEntity;
import org.alliancegenome.api.entity.DiseaseRibbonSummary;
import org.alliancegenome.cache.repository.DiseaseCacheRepository;
import org.alliancegenome.cache.repository.PhenotypeCacheRepository;
import org.alliancegenome.core.service.*;
import org.alliancegenome.es.model.query.FieldFilter;
import org.alliancegenome.es.model.query.Pagination;
import org.alliancegenome.neo4j.entity.DiseaseAnnotation;
import org.alliancegenome.neo4j.entity.DiseaseSummary;
import org.alliancegenome.neo4j.entity.PhenotypeAnnotation;
import org.alliancegenome.neo4j.entity.PrimaryAnnotatedEntity;
import org.alliancegenome.neo4j.entity.node.DOTerm;
import org.alliancegenome.neo4j.entity.node.Gene;
import org.alliancegenome.neo4j.entity.node.SimpleTerm;
import org.alliancegenome.neo4j.repository.DiseaseRepository;
import org.alliancegenome.neo4j.repository.GeneRepository;
import org.apache.commons.collections.CollectionUtils;

import javax.enterprise.context.RequestScoped;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RequestScoped
public class DiseaseService {

    private static DiseaseRepository diseaseRepository = new DiseaseRepository();
    private static GeneRepository geneRepository = new GeneRepository();
    private static DiseaseCacheRepository diseaseCacheRepository = new DiseaseCacheRepository();
    private static PhenotypeCacheRepository phenotypeCacheRepository = new PhenotypeCacheRepository();

    public DiseaseService() {

    }

    public static List<String> getDiseaseParents(String diseaseSlimID) {
        if (!diseaseSlimID.equals(DiseaseRibbonSummary.DOID_OTHER)) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add(diseaseSlimID);
            return strings;
        }
        return new ArrayList<>(Arrays.asList("DOID:0080015", "DOID:0014667", "DOID:150", "DOID:225"));
    }

    public static List<String> getAllOtherDiseaseTerms() {
        return new ArrayList<>(Arrays.asList("DOID:0080015", "DOID:0014667", "DOID:150", "DOID:225"));
    }

    public DOTerm getById(String id) {
        return diseaseRepository.getDiseaseTerm(id);
    }

    public JsonResultResponse<DiseaseAnnotation> getDiseaseAnnotationsByDisease(String diseaseID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        PaginationResult<DiseaseAnnotation> paginationResult = diseaseCacheRepository.getDiseaseAnnotationList(diseaseID, pagination);
        JsonResultResponse<DiseaseAnnotation> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startDate);
        if (paginationResult != null) {
            response.setResults(paginationResult.getResult());
            response.setTotal(paginationResult.getTotalNumber());
        }
        return response;
    }

    public JsonResultResponse<DiseaseAnnotation> getDiseaseAnnotationsWithAlleles(String diseaseID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        List<DiseaseAnnotation> fullDiseaseAnnotationList = diseaseCacheRepository.getDiseaseAlleleAnnotationList(diseaseID);
        JsonResultResponse<DiseaseAnnotation> result = new JsonResultResponse<>();
        if (fullDiseaseAnnotationList == null) {
            result.calculateRequestDuration(startDate);
            return result;
        }

        List<DiseaseAnnotation> alleleDiseaseAnnotations = fullDiseaseAnnotationList.stream()
                .filter(annotation -> annotation.getFeature() != null)
                .collect(toList());
        //filtering
        FilterService<DiseaseAnnotation> filterService = new FilterService<>(new DiseaseAnnotationFiltering());
        List<DiseaseAnnotation> filteredDiseaseAnnotationList = filterService.filterAnnotations(alleleDiseaseAnnotations, pagination.getFieldFilterValueMap());
        result.setTotal(filteredDiseaseAnnotationList.size());
        result.setResults(filterService.getSortedAndPaginatedAnnotations(pagination, filteredDiseaseAnnotationList, new DiseaseAnnotationSorting()));
        return result;
    }

    public JsonResultResponse<DiseaseAnnotation> getDiseaseAnnotationsWithGenes(String geneID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        List<DiseaseAnnotation> fullDiseaseAnnotationList = diseaseCacheRepository.getDiseaseAnnotationList(geneID);
        JsonResultResponse<DiseaseAnnotation> result = new JsonResultResponse<>();
        if (fullDiseaseAnnotationList == null) {
            result.calculateRequestDuration(startDate);
            return result;
        }

        // need to group annotations by gene / association type
        Map<Gene, Map<String, List<DiseaseAnnotation>>> groupedByGeneList = fullDiseaseAnnotationList.stream()
                .filter(diseaseAnnotation -> diseaseAnnotation.getGene() != null)
                .collect(Collectors.groupingBy(DiseaseAnnotation::getGene,
                        Collectors.groupingBy(DiseaseAnnotation::getAssociationType)));

        List<DiseaseAnnotation> geneDiseaseAnnotations = new ArrayList<>();
        groupedByGeneList.forEach((gene, typeMap) -> {
            typeMap.forEach((type, diseaseAnnotations) -> {
                DiseaseAnnotation firstAnnotation = diseaseAnnotations.get(0);
                diseaseAnnotations.forEach(annotation -> {
                    firstAnnotation.addAllPrimaryAnnotatedEntities(annotation.getPrimaryAnnotatedEntities());
                });
                geneDiseaseAnnotations.add(firstAnnotation);
            });
        });
        //filtering
        FilterService<DiseaseAnnotation> filterService = new FilterService<>(new DiseaseAnnotationFiltering());
        List<DiseaseAnnotation> filteredDiseaseAnnotationList = filterService.filterAnnotations(geneDiseaseAnnotations, pagination.getFieldFilterValueMap());
        result.setTotal(filteredDiseaseAnnotationList.size());
        result.setResults(filterService.getSortedAndPaginatedAnnotations(pagination, filteredDiseaseAnnotationList, new DiseaseAnnotationSorting()));
        return result;
    }

    public JsonResultResponse<DiseaseAnnotation> getDiseaseAnnotationsWithAGM(String diseaseID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        List<DiseaseAnnotation> fullDiseaseAnnotationList = diseaseCacheRepository.getDiseaseAnnotationList(diseaseID);
        JsonResultResponse<DiseaseAnnotation> result = new JsonResultResponse<>();
        if (fullDiseaseAnnotationList == null) {
            result.calculateRequestDuration(startDate);
            return result;
        }

        // select list of annotations to model entities
        List<DiseaseAnnotation> modelDiseaseAnnotations = fullDiseaseAnnotationList.stream()
                .filter(diseaseAnnotation -> diseaseAnnotation.getModel() != null)
                .collect(Collectors.toList());

        //filtering
        FilterService<DiseaseAnnotation> filterService = new FilterService<>(new ModelAnnotationFiltering());
        List<DiseaseAnnotation> filteredDiseaseAnnotationList = filterService.filterAnnotations(modelDiseaseAnnotations, pagination.getFieldFilterValueMap());
        result.setTotal(filteredDiseaseAnnotationList.size());
        result.setResults(filterService.getSortedAndPaginatedAnnotations(pagination, filteredDiseaseAnnotationList, new ModelAnnotationsSorting()));
        result.calculateRequestDuration(startDate);
        return result;
    }

    public JsonResultResponse<PrimaryAnnotatedEntity> getDiseaseAnnotationsWithGeneAndAGM(String geneID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        List<DiseaseAnnotation> fullDiseaseAnnotationList = diseaseCacheRepository.getDiseaseAnnotationList(geneID);
        JsonResultResponse<PrimaryAnnotatedEntity> result = new JsonResultResponse<>();

        // select list of annotations that have primaryAnnotatedEntity

        Map<String, PrimaryAnnotatedEntity> diseaseEntities = null;
        Map<String, PrimaryAnnotatedEntity> phenotypeEntities = null;
        Map<String, PrimaryAnnotatedEntity> modelEntities = null;

        List<PrimaryAnnotatedEntity> primaryAnnotatedEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fullDiseaseAnnotationList)) {
            primaryAnnotatedEntities = fullDiseaseAnnotationList.stream()
                    // filter out annotations with primary annotated diseaseEntities
                    .filter(diseaseAnnotation -> CollectionUtils.isNotEmpty(diseaseAnnotation.getPrimaryAnnotatedEntities()))
                    .map(DiseaseAnnotation::getPrimaryAnnotatedEntities)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
            diseaseEntities = primaryAnnotatedEntities.stream()
                    .collect(Collectors.toMap(PrimaryAnnotatedEntity::getId, entity -> entity));
        }

        List<PhenotypeAnnotation> allPhenotypeAnnotations = phenotypeCacheRepository.getPhenotypeAnnotationList(geneID);
        // select annotations that have primaryAnnotated Entities
        List<PrimaryAnnotatedEntity> primaryAnnotatedEntitiesPhenotype = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allPhenotypeAnnotations)) {
            primaryAnnotatedEntitiesPhenotype = allPhenotypeAnnotations.stream()
                    // filter out annotations with primary annotated diseaseEntities
                    .filter(annotation -> CollectionUtils.isNotEmpty(annotation.getPrimaryAnnotatedEntities()))
                    .map(PhenotypeAnnotation::getPrimaryAnnotatedEntities)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

            phenotypeEntities = primaryAnnotatedEntitiesPhenotype.stream()
                    .collect(Collectors.toMap(PrimaryAnnotatedEntity::getId, entity -> entity));
        }

        List<PrimaryAnnotatedEntity> fullModelList = diseaseCacheRepository.getPrimaryAnnotatedEntitList(geneID);

        if (CollectionUtils.isNotEmpty(fullModelList)) {
            modelEntities = fullModelList.stream()
                    .collect(Collectors.toMap(PrimaryAnnotatedEntity::getId, entity -> entity));
        }

        if (diseaseEntities == null && phenotypeEntities == null && modelEntities == null) {
            return result;
        }


        List<String> mergedEntities = new ArrayList<>();
        // add AGMs to the ones created in the disease cycle
        if (diseaseEntities != null && phenotypeEntities != null) {
            for (String id : diseaseEntities.keySet()) {
                if (phenotypeEntities.get(id) != null) {
                    PrimaryAnnotatedEntity diseaseEntity = diseaseEntities.get(id);
                    PrimaryAnnotatedEntity phenotypeEntity = phenotypeEntities.get(id);
                    diseaseEntity.addPhenotypes(phenotypeEntity.getPhenotypes());
                    mergedEntities.add(id);
                }
            }
        }
        // remove the merged ones
        primaryAnnotatedEntitiesPhenotype = primaryAnnotatedEntitiesPhenotype.stream()
                .filter(entity -> !mergedEntities.contains(entity.getId()))
                .collect(toList());
        primaryAnnotatedEntities.addAll(primaryAnnotatedEntitiesPhenotype);


        if (CollectionUtils.isEmpty(primaryAnnotatedEntities)) {
            return result;
        }

        List<String> geneIDs = primaryAnnotatedEntities.stream()
                .map(PrimaryAnnotatedEntity::getId)
                .collect(toList());

        // remove the AGMs that are already accounted for by disease or phenotype relationship
        // leaving only those AGMs that have no one of that kind
        if (CollectionUtils.isNotEmpty(fullModelList)) {
            geneIDs.forEach(geneId -> {
                fullModelList.removeIf(entity -> entity.getId().equals(geneId));
            });
            primaryAnnotatedEntities.addAll(fullModelList);
        }

        //filtering
        FilterService<PrimaryAnnotatedEntity> filterService = new FilterService<>(new PrimaryAnnotatedEntityFiltering());
        List<PrimaryAnnotatedEntity> filteredDiseaseAnnotationList = filterService.filterAnnotations(primaryAnnotatedEntities, pagination.getFieldFilterValueMap());
        result.setTotal(filteredDiseaseAnnotationList.size());
        result.setResults(filterService.getSortedAndPaginatedAnnotations(pagination, filteredDiseaseAnnotationList, new PrimaryAnnotatedEntitySorting()));
        result.calculateRequestDuration(startDate);
        return result;
    }

    public JsonResultResponse<DiseaseAnnotation> getDiseaseAnnotations(String geneID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        JsonResultResponse<DiseaseAnnotation> response = getDiseaseAnnotationsWithGenes(geneID, pagination);
        String note = "";
        if (!SortingField.isValidSortingFieldValue(pagination.getSortBy())) {
            note += "Invalid sorting name provided: " + pagination.getSortBy();
            note += ". Sorting is ignored! ";
            note += "Allowed values are (case insensitive): " + SortingField.getAllValues();
        }
        if (pagination.hasInvalidElements()) {
            note += "Invalid filtering name(s) provided: " + pagination.getInvalidFilterList();
            note += ". Filtering for these elements is ignored! ";
            note += "Allowed values are (case insensitive): " + FieldFilter.getAllValues();
        }
        if (!note.isEmpty())
            response.setNote(note);
        response.calculateRequestDuration(startDate);
        return response;
    }

    public DiseaseSummary getDiseaseSummary(String id, DiseaseSummary.Type type) {
        return diseaseRepository.getDiseaseSummary(id, type);
    }

    public DiseaseRibbonSummary getDiseaseRibbonSummary(List<String> geneIDs) {
        DiseaseRibbonService diseaseRibbonService = new DiseaseRibbonService();
        DiseaseRibbonSummary summary = diseaseRibbonService.getDiseaseRibbonSectionInfo();
        Pagination pagination = new Pagination();
        pagination.setLimitToAll();
        // loop over all genes provided
        geneIDs.forEach(geneID -> {
            PaginationResult<DiseaseAnnotation> paginationResult = diseaseCacheRepository.getDiseaseAnnotationList(geneID, pagination);
            if (paginationResult == null) {
                paginationResult = new PaginationResult<>();
                paginationResult.setTotalNumber(0);
            }
            // calculate histogram
            Map<String, List<DiseaseAnnotation>> histogram = getDiseaseAnnotationHistogram(paginationResult);

            Gene gene = geneRepository.getShallowGene(geneID);
            if (gene == null)
                return;
            // populate diseaseEntity records
            populateDiseaseRibbonSummary(geneID, summary, histogram, gene);
            summary.addAllAnnotationsCount(geneID, paginationResult.getTotalNumber());
        });
        return summary;
    }

    private void populateDiseaseRibbonSummary(String geneID, DiseaseRibbonSummary summary, Map<String, List<DiseaseAnnotation>> histogram, Gene gene) {
        DiseaseRibbonEntity entity = new DiseaseRibbonEntity();
        entity.setId(geneID);
        entity.setLabel(gene.getSymbol());
        entity.setTaxonID(gene.getTaxonId());
        entity.setTaxonName(gene.getSpecies().getName());
        summary.addDiseaseRibbonEntity(entity);

        Set<String> allTerms = new HashSet<>();
        Set<DiseaseAnnotation> allAnnotations = new HashSet<>();
        List<String> agrDoSlimIDs = diseaseRepository.getAgrDoSlim().stream()
                .map(SimpleTerm::getPrimaryKey)
                .collect(toList());
        // add category term IDs to get the full histogram mapped into the response
        agrDoSlimIDs.addAll(DiseaseRibbonService.slimParentTermIdMap.keySet());
        agrDoSlimIDs.forEach(slimId -> {
            DiseaseEntitySubgroupSlim group = new DiseaseEntitySubgroupSlim();
            int size = 0;
            List<DiseaseAnnotation> diseaseAnnotations = histogram.get(slimId);
            if (diseaseAnnotations != null) {
                allAnnotations.addAll(diseaseAnnotations);
                size = diseaseAnnotations.size();
                Set<String> terms = diseaseAnnotations.stream().map(diseaseAnnotation -> diseaseAnnotation.getDisease().getPrimaryKey())
                        .collect(Collectors.toSet());
                allTerms.addAll(terms);
                group.setNumberOfClasses(terms.size());
            }
            group.setNumberOfAnnotations(size);
            group.setId(slimId);
            if (size > 0)
                entity.addDiseaseSlim(group);
        });
        entity.setNumberOfClasses(allTerms.size());
        entity.setNumberOfAnnotations(allAnnotations.size());
    }

    private DiseaseRibbonService diseaseRibbonService = new DiseaseRibbonService();

    private Map<String, List<DiseaseAnnotation>> getDiseaseAnnotationHistogram(PaginationResult<DiseaseAnnotation> paginationResult) {
        Map<String, List<DiseaseAnnotation>> histogram = new HashMap<>();
        if (paginationResult.getResult() == null)
            return histogram;
        paginationResult.getResult().forEach(annotation -> {
            Set<String> parentIDs = diseaseRibbonService.getAllParentIDs(annotation.getDisease().getPrimaryKey());
            parentIDs.forEach(parentID -> {
                List<DiseaseAnnotation> list = histogram.get(parentID);
                if (list == null)
                    list = new ArrayList<>();
                list.add(annotation);
                histogram.put(parentID, list);
            });
        });
        return histogram;
    }

    public JsonResultResponse<DiseaseAnnotation> getRibbonDiseaseAnnotations(List<String> geneIDs, String termID, Pagination pagination) {
        LocalDateTime startDate = LocalDateTime.now();
        PaginationResult<DiseaseAnnotation> paginationResult = diseaseCacheRepository.getRibbonDiseaseAnnotations(geneIDs, termID, pagination);
        JsonResultResponse<DiseaseAnnotation> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startDate);
        if (paginationResult != null) {
            response.setResults(paginationResult.getResult());
            response.setTotal(paginationResult.getTotalNumber());
        }
        return response;
    }

}
