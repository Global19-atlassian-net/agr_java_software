package org.alliancegenome.api.rest.interfaces;

import org.alliancegenome.api.DiseaseCacheRepository;
import org.alliancegenome.api.entity.CacheSummary;
import org.alliancegenome.neo4j.repository.*;

public class DevtoolController implements DevtoolRESTInterface {

    @Override
    public CacheSummary getCacheStatus() {
        CacheSummary summary = new CacheSummary();

        DiseaseCacheRepository diseaseCacheRepository = new DiseaseCacheRepository();
        InteractionCacheRepository interactionCacheRepository = new InteractionCacheRepository();
        ExpressionCacheRepository expressionCacheRepository = new ExpressionCacheRepository();
        PhenotypeCacheRepository phenotypeCacheRepository = new PhenotypeCacheRepository();
        GeneCacheRepository geneCacheRepository = new GeneCacheRepository();
        AlleleCacheRepository alleleCacheRepository = new AlleleCacheRepository();

        return summary;
    }
}
