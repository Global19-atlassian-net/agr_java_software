package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.apache.commons.collections4.MapUtils;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@Schema(name = "TransgenicAlleleStats", description = "POJO that represents Transgenic Allele Statistics")
public class TransgenicAlleleStats implements Serializable {

    @JsonView(View.API.class)
    private Map<String, Column> columns;

    public void addColumn(ColumnStats col, ColumnValues val) {
        if (MapUtils.isEmpty(columns))
            columns = new LinkedHashMap<>();
        Column column = new Column();
        column.setColumnStat(val);
        column.setColumnDefinition(col);
        columns.put(column.getColumnDefinition().getName(), column);
    }

}
