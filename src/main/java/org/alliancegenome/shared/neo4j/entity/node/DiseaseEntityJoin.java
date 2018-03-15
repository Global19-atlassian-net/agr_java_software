package org.alliancegenome.shared.neo4j.entity.node;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import lombok.Getter;
import lombok.Setter;

@NodeEntity
@Getter
@Setter
public class DiseaseEntityJoin extends Association {

	private String primaryKey;
	private String joinType;

	@Relationship(type = "EVIDENCE")
	private Publication publication;

	@Relationship(type = "EVIDENCE")
	private List<EvidenceCode> evidenceCodes;

	@Relationship(type = "ASSOCIATION", direction = Relationship.INCOMING)
	private Gene gene;

	@Relationship(type = "ASSOCIATION", direction = Relationship.INCOMING)
	private Feature feature;

	@Relationship(type = "ASSOCIATION")
	private DOTerm disease;

}
