package org.alliancegenome.data_extractor.extractors.fms;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DataFile {
    private String s3Path;
    private DataType dataType;
    private DataSubType dataSubType;
    
    @JsonIgnore
    public String getFileName() {
        return dataType.getName() + "_" + dataSubType.getName() + "." + dataType.getFileExtension();
    }
}
