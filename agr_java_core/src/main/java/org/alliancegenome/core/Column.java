package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@Schema(name = "Column", description = "POJO that represents statistic row")
// <column Name, value>
@Setter
@Getter
public class Column implements Serializable {

    @JsonView(View.API.class)
    private ColumnStats columnDefinition;
    @JsonView(View.API.class)
    private ColumnValues columnStat;

}
