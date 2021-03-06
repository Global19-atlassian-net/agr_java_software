package org.alliancegenome.api.entity;

import org.alliancegenome.neo4j.view.View;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.*;

@Setter
@Getter
public class ExpressionSummaryGroupTerm {
    @JsonView({ View.Expression.class})
    private String id;
    @JsonView({ View.Expression.class})
    private String name;
    @JsonView({ View.Expression.class})
    private int numberOfAnnotations;
    private int numberOfClasses;

    @Override
    public String toString() {
        return name + " [" + numberOfAnnotations + ']';
    }
}

