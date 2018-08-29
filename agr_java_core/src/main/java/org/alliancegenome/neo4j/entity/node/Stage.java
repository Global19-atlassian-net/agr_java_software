package org.alliancegenome.neo4j.entity.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.es.util.DateConverter;
import org.alliancegenome.neo4j.entity.Neo4jEntity;
import org.alliancegenome.neo4j.entity.relationship.GenomeLocation;
import org.alliancegenome.neo4j.entity.relationship.Orthologous;
import org.alliancegenome.neo4j.view.View;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import java.util.*;

@NodeEntity
@Getter
@Setter
public class Stage extends Neo4jEntity implements Comparable<Stage> {

    @JsonView({View.OrthologyView.class, View.InteractionView.class, View.ExpressionView.class})
    @JsonProperty("stageID")
    private String primaryKey;

    @Override
    public String toString() {
        return primaryKey;
    }

    @Override
    public int compareTo(Stage o) {
        return 0;
    }
}
