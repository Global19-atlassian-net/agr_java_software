package org.alliancegenome.agr_submission.controllers;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jws.WebService;

import org.alliancegenome.agr_submission.BaseController;
import org.alliancegenome.agr_submission.entities.DataFile;
import org.alliancegenome.agr_submission.interfaces.server.DataFileControllerInterface;
import org.alliancegenome.agr_submission.services.DataFileService;

@RequestScoped
@WebService
public class DataFileController extends BaseController implements DataFileControllerInterface {

    @Inject DataFileService dataFileService;
    
    @Override
    public DataFile create(String schemaVersion, String dataType, String dataSubtype, DataFile entity) {
        return dataFileService.create(schemaVersion, dataType, dataSubtype, entity);
    }

    @Override
    public DataFile get(Long id) {
        return dataFileService.get(id);
    }

    @Override
    public DataFile update(DataFile entity) {
        return dataFileService.update(entity);
    }

    @Override
    public DataFile delete(Long id) {
        return dataFileService.delete(id);
    }
    
    public List<DataFile> getDataFiles() {
        return dataFileService.getDataFiles();
    }

    @Override
    public List<DataFile> getDataTypeFiles(String dataType) {
        return dataFileService.getDataTypeFiles(dataType);
    }

    @Override
    public List<DataFile> getDataTypeSubTypeFiles(String dataType, String dataSubtype) {
        return dataFileService.getDataTypeSubTypeFiles(dataType, dataSubtype);
    }

}
