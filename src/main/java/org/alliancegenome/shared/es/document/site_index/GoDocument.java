package org.alliancegenome.shared.es.document.site_index;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoDocument extends ESDocument {

	private String primaryId;
	private String category = "go";
	private String href;
	private String name;
	private String name_key;
	private String description;

	private String id;
	private String go_type;
	private List<String> synonyms;
	private List<String> go_genes;
	private List<String> go_species;

	@Override
	@JsonIgnore
	public String getDocumentId() {
		return id;
	}
	
	@Override
	@JsonIgnore
	public String getType() {
		return category;
	}
	
}