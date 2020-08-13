package org.alliancegenome.core;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Schema(name = "StatisticRow", description = "POJO that represents statistic row")
// <column Name, value>
public class StatisticRow extends LinkedHashMap<String, String> implements Serializable {

}
