package org.alliancegenome.agr_submission.services;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.alliancegenome.agr_submission.BaseService;
import org.alliancegenome.agr_submission.dao.ReleaseVersionDAO;
import org.alliancegenome.agr_submission.dao.SchemaVersionDAO;
import org.alliancegenome.agr_submission.entities.ReleaseVersion;
import org.alliancegenome.agr_submission.entities.SchemaVersion;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class ReleaseVersionService extends BaseService<ReleaseVersion> {

    @Inject private ReleaseVersionDAO dao;
    @Inject private SchemaVersionDAO schemaDAO;

    @Override
    @Transactional
    public ReleaseVersion create(ReleaseVersion entity) {
        log.info("ReleaseVersionService: create: ");
        return dao.persist(entity);
    }

    @Override
    @Transactional
    public ReleaseVersion get(Long id) {
        log.info("ReleaseVersionService: get: " + id);
        return dao.find(id);
    }

    @Override
    @Transactional
    public ReleaseVersion update(ReleaseVersion entity) {
        log.info("ReleaseVersionService: update: ");
        return dao.merge(entity);
    }

    @Override
    @Transactional
    public ReleaseVersion delete(Long id) {
        log.info("ReleaseVersionService: delete: " + id);
        return dao.remove(id);
    }

    public List<ReleaseVersion> getReleaseVersions() {
        return dao.findAll();
    }
    
    @Transactional
    public ReleaseVersion addSchema(String release, String schema) {
        ReleaseVersion rv = dao.findByField("releaseVersion", release);
        SchemaVersion sv = schemaDAO.findByField("schema", schema);
        if(sv != null && !rv.getSchemaVersions().contains(sv)) {
            rv.getSchemaVersions().add(sv);
            dao.persist(rv);
        }
        return rv;
    }


}
