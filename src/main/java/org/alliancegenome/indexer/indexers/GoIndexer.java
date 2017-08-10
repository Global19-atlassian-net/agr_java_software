package org.alliancegenome.indexer.indexers;

import org.alliancegenome.indexer.config.IndexerConfig;
import org.alliancegenome.indexer.document.GoDocument;
import org.alliancegenome.indexer.entity.Go;
import org.alliancegenome.indexer.service.Neo4jESService;

public class GoIndexer extends Indexer<GoDocument> {

	private Neo4jESService<Go> neo4jService = new Neo4jESService<Go>();

	public GoIndexer(IndexerConfig config) {
		super(config);
	}
	
	public void index() {
		
	}

}
