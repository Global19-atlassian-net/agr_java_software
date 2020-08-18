package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.apache.commons.lang3.Range;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Setter
@Getter
@Schema(name = "TransgenicAlleleStats", description = "POJO that represents Transgenic Allele Statistics")
public class ColumnStats<Entity, SubEntity> implements Serializable {

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

    private Function<Entity, List<SubEntity>> multiValueFunction;
    private Function<Entity, String> singleValuefunction;

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues) {
        this.name = name;
        this.superEntity = superEntity;
        this.rowEntity = rowEntity;
        this.multiValued = multiValued;
        this.limitedValues = limitedValues;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<Entity, List<SubEntity>> function, Function<Entity, String> sfunction) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValuefunction = sfunction;
        this.multiValueFunction = function;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<Entity, String> function) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValuefunction = function;
    }

    public String getSingleValue(Entity a) {
        if (singleValuefunction == null)
            return null;
        return singleValuefunction.apply(a);
    }

}
