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

@Schema(name = "StatisticRow", description = "POJO that represents statistic row")
// <column Name, value>
@Setter
@Getter
public class StatisticRow implements Serializable {

    @JsonView(View.API.class)
    private HashMap<String, Column> columns;

    public void put(ColumnStats geneStat, ColumnValues columnValues) {
        Column col = new Column();
        col.setColumnDefinition(geneStat);
        col.setColumnStat(columnValues);
        if (MapUtils.isEmpty(columns))
            columns = new LinkedHashMap<>();
        columns.put(col.getColumnDefinition().getName(), col);
    }
}
