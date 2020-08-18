package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.apache.commons.lang3.Range;

import java.util.Map;

@Setter
@Getter
public class ColumnValues {

    @JsonView(View.API.class)
    private String value;
    @JsonView(View.API.class)
    private int totalNumber;
    @JsonView(View.API.class)
    private int totalDistinctNumber;
    @JsonView(View.API.class)
    private Map<String, Integer> histogram;
    @JsonView(View.API.class)
    private Range cardinality;
    @JsonView(View.API.class)
    private Range cardinalityParent;

}
