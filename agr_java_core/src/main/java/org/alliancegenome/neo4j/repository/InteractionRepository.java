package org.alliancegenome.neo4j.repository;

import org.alliancegenome.neo4j.entity.node.InteractionGeneJoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InteractionRepository extends Neo4jRepository<InteractionGeneJoin> {

    public InteractionRepository() {
        super(InteractionGeneJoin.class);
    }

    private String interactionsQuery = "MATCH p1=(g1:Gene)--(igj:InteractionGeneJoin)--(g2:Gene), p2=(igj:InteractionGeneJoin)--(s) where g1.primaryKey = {primaryKey}"
            + " OPTIONAL MATCH p3=(g1:Gene)-->(s1:Species) "
            + " OPTIONAL MATCH p4=(g2:Gene)-->(s2:Species) ";

    public List<InteractionGeneJoin> getInteractions(String primaryKey) {
        HashMap<String, String> map = new HashMap<>();
        List<InteractionGeneJoin> results = new ArrayList<>();

        map.put("primaryKey", primaryKey);
        String query = interactionsQuery + " RETURN p1, p2, p3, p4";
        //String query = "MATCH p1=(g:Gene)-[iw:INTERACTS_WITH]->(g2:Gene), p2=(g:Gene)-->(igj:InteractionGeneJoin)--(s) where g.primaryKey = {primaryKey} and iw.uuid = igj.primaryKey RETURN p1, p2";

        Iterable<InteractionGeneJoin> joins = query(query, map);
        for (InteractionGeneJoin join : joins) {
            results.add(join);
        }
        return results;
    }

    public long getInteractionCount(String geneID) {
        HashMap<String, String> bindingValueMap = new HashMap<>();
        bindingValueMap.put("primaryKey", geneID);

        String cypher = interactionsQuery + " RETURN count(distinct igj) as total";
        return (Long) queryForResult(cypher, bindingValueMap).iterator().next().get("total");
    }

    public long getInteractorCount(String geneID) {
        HashMap<String, String> bindingValueMap = new HashMap<>();
        bindingValueMap.put("primaryKey", geneID);

        String cypher = interactionsQuery + " RETURN count(distinct g2) as total";
        return (Long) queryForResult(cypher, bindingValueMap).iterator().next().get("total");
    }
}