package org.alliancegenome.indexer.document;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlleleDocument extends ESDocument {

    private String category = "allele";
    private String primaryKey;
    private String symbol;
    private Date dateProduced;
    private Date dataProvider;
    private String release;
    private String localId;
    private String globalId;
    private String species;

    private List<String> secondaryIds;
    private List<String> synonyms;

    @Override
    @JsonIgnore
    public String getDocumentId() {
        return primaryKey;
    }
}
