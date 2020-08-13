package org.alliancegenome.api.tests.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alliancegenome.api.service.StatisticsService;
import org.alliancegenome.core.TransgenicAlleleStats;
import org.alliancegenome.core.config.ConfigHelper;
import org.alliancegenome.neo4j.view.OrthologyModule;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class StatisticsIT {

    private ObjectMapper mapper = new ObjectMapper();

    private StatisticsService service = new StatisticsService();

    public static void main(String[] args) {

    }

    @Before
    public void before() {
        Configurator.setRootLevel(Level.WARN);
        ConfigHelper.init();

        //geneService = new GeneService();

        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new OrthologyModule());
    }


    @Test
    public void checkPhenotypeByGeneWithoutPagination() {
        TransgenicAlleleStats stat = service.getAllTransgenicAlleles();
        System.out.println(stat);
        assertNotNull(stat);
    }
}
