package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Schema(name = "TransgenicAlleleStats", description = "POJO that represents Transgenic Allele Statistics")
public class TransgenicAlleleStats implements Serializable {

    @JsonView(View.API.class)
    private List<ColumnStats> columns = new ArrayList<>();

    public void addColumn(ColumnStats col) {
        columns.add(col);
    }

    @Override
    public String toString() {
        return columns.stream()
                .map(ColumnStats::toString)
                .collect(Collectors.joining(","));
    }
}
