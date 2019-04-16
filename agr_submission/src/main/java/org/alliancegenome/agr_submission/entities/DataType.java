package org.alliancegenome.agr_submission.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.alliancegenome.agr_submission.BaseEntity;
import org.alliancegenome.agr_submission.views.View;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity @ApiModel
@Getter @Setter @ToString
public class DataType extends BaseEntity {

    @Id @GeneratedValue
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class, View.DataFileView.class})
    private Long id;
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class, View.DataFileView.class})
    private String name;
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class})
    private String description;
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class})
    private String fileExtension;
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class})
    private boolean dataSubTypeRequired;
    @JsonView({View.DataTypeView.class, View.DataSubTypeView.class})
    private boolean validationRequired;

    @ManyToMany(fetch=FetchType.EAGER)
    @JsonView({View.DataTypeView.class})
    private Set<DataSubType> dataSubTypes;

    @OneToMany(mappedBy = "dataType", fetch=FetchType.EAGER)
    @JsonView({View.DataTypeView.class})
    private Set<SchemaFile> schemaFiles;

    @Transient
    public Map<String, String> getSchemaFilesMap() {
        HashMap<String, String> map = new HashMap<>();
        if(schemaFiles != null) {
            for(SchemaFile s: schemaFiles) {
                map.put(s.getSchemaVersion().getSchema(), s.getFilePath());
            }
        }
        return map;
    }

}
