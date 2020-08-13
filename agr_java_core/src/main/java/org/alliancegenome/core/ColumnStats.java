package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.apache.commons.lang3.Range;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.Map;

@Setter
@Getter
@Schema(name = "TransgenicAlleleStats", description = "POJO that represents Transgenic Allele Statistics")
public class ColumnStats implements Serializable {

    @JsonView(View.API.class)
    private String name;
    @JsonView(View.API.class)
    private boolean superEntity;
    @JsonView(View.API.class)
    private boolean rowEntity;
    @JsonView(View.API.class)
    private boolean multiValued;
    @JsonView(View.API.class)
    private boolean limitedValues;

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

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues) {
        this.name = name;
        this.superEntity = superEntity;
        this.rowEntity = rowEntity;
        this.multiValued = multiValued;
        this.limitedValues = limitedValues;
    }

    @Override
    public String toString() {
        String display = name + "\r\n";
        display += "T:\t" + totalNumber + "\r\n";
        display += "TD:\t" + totalDistinctNumber + "\r\n";
        if (limitedValues)
            display += "Enum:\t" + histogram + "\r\n";
        display += "C:\t" + cardinality + "\r\n";
        display += "PC:\t" + cardinalityParent + "\r\n";
        display += "\r\n";
        return display;
    }
}
