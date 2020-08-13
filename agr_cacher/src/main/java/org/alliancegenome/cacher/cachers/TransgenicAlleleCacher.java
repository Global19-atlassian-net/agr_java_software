package org.alliancegenome.cacher.cachers;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.api.service.AlleleService;
import org.alliancegenome.cache.CacheAlliance;
import org.alliancegenome.cache.repository.helper.JsonResultResponse;
import org.alliancegenome.es.model.query.Pagination;
import org.alliancegenome.neo4j.entity.SpeciesType;
import org.alliancegenome.neo4j.entity.node.Allele;
import org.alliancegenome.neo4j.entity.node.Gene;
import org.alliancegenome.neo4j.repository.GeneRepository;
import org.alliancegenome.neo4j.view.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class TransgenicAlleleCacher extends Cacher {

    private static GeneRepository repository = new GeneRepository();

    private static AlleleService alleleService = new AlleleService();

    @Override
    protected void cache() {

        startProcess("get All Transgenic Alleles");
        Map<String, List<Allele>> alleleMap = new HashMap<>();
        Arrays.stream(SpeciesType.values())
                //.filter(speciesType -> speciesType.equals(SpeciesType.ZEBRAFISH))
                .forEach(speciesType -> {
                    List<Gene> genes = repository.getAllGenes(List.of(speciesType.getTaxonID()));
                    //genes.removeIf(gene -> !gene.getPrimaryKey().equals("FB:FBgn0003996"));
                    log.info("Organism: " + speciesType.getAbbreviation());
                    log.info("Total number of genes: " + genes.size());

                    for (int index = 0; index < genes.size(); index++) {
                        Gene gene = genes.get(index);
                        String geneID = gene.getPrimaryKey();
                        Pagination pagination = new Pagination();
                        pagination.setUnlimited();
                        JsonResultResponse<Allele> response = alleleService.getTransgenicAlleles(geneID, pagination);
                        // do not include genes without transgenic allele records
                        if (response.getTotal() == 0)
                            continue;
                        alleleMap.put(geneID + "||" + gene.getSymbol()+"||"+gene.getTaxonId(), response.getResults());
                        if (index % 1000 == 0 && index % 5000 != 0 && index % 10000 != 0)
                            System.out.print(".");
                        if (index % 5000 == 0 && index % 10000 != 0)
                            System.out.print(":");
                        if (index % 10000 == 0)
                            System.out.print(index / 1000);
                    }
                    System.out.print("");
                });
        createCacheMap(alleleMap, View.TransgenicAlleleAPI.class, CacheAlliance.TRANSGENIC_ALLELES);
        finishProcess();
    }


}
