package org.alliancegenome.api.rest.interfaces;

import org.alliancegenome.api.service.StatisticsService;
import org.alliancegenome.core.TransgenicAlleleStats;

import javax.inject.Inject;

public class StatisticsController implements StatisticsRESTInterface {

    @Inject
    private StatisticsService service;

    @Override
    public TransgenicAlleleStats getTrans() {
        service.getAllTransgenicAlleles();
        return null;
    }
}
