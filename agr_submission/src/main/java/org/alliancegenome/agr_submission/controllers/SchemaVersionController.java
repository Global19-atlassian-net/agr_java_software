package org.alliancegenome.agr_submission.controllers;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.alliancegenome.agr_submission.BaseController;
import org.alliancegenome.agr_submission.entities.SchemaVersion;
import org.alliancegenome.agr_submission.interfaces.SchemaVersionControllerInterface;
import org.alliancegenome.agr_submission.services.SchemaVersionService;

@RequestScoped
public class SchemaVersionController extends BaseController implements SchemaVersionControllerInterface {

    @Inject SchemaVersionService schemaVersionService;
    
    @Override
    public SchemaVersion create(SchemaVersion entity) {
        return schemaVersionService.create(entity);
    }

    @Override
    public SchemaVersion get(Long id) {
        return schemaVersionService.get(id);
    }

    @Override
    public SchemaVersion update(SchemaVersion entity) {
        return schemaVersionService.update(entity);
    }

    @Override
    public SchemaVersion delete(Long id) {
        return schemaVersionService.delete(id);
    }
    
    public List<SchemaVersion> getDataTypes() {
        return schemaVersionService.getSchemaVersions();
    }

}
