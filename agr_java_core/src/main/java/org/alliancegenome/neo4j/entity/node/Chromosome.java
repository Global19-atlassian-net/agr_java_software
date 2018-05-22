package org.alliancegenome.neo4j.entity.node;

import org.alliancegenome.neo4j.entity.Neo4jEntity;
import org.neo4j.ogm.annotation.NodeEntity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@NodeEntity
public class Chromosome extends Neo4jEntity {

    private String primaryKey;
}