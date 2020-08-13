package org.alliancegenome.neo4j.entity.node;

import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@NodeEntity
@Getter
@Setter
@Schema(name="Synonym", description="POJO that represents Synonyms")
public class Synonym extends Identifier {

    private String primaryKey;
    @JsonView({View.Default.class})
    private String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Synonym synonym = (Synonym) o;
        return Objects.equals(primaryKey, synonym.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey);
    }
}
