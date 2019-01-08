package org.alliancegenome.indexer.config;

import org.alliancegenome.indexer.indexers.*;

public enum IndexerConfig {

    // These numbers target a 10KB document batch size
    GeneIndexer("gene", GeneIndexer.class, 1, 240),
    DiseaseIndexer("disease", DiseaseIndexer.class, 2, 150),
    DiseaseAnnotationIndexer("diseaseAnnotation", DiseaseAnnotationIndexer.class, 2, 635),
    AlleleIndexer("allele", AlleleIndexer.class, 2, 743),
    GoIndexer("go", GoIndexer.class, 2, 2300),;

    private String typeName;
    private Class<?> indexClazz;
    private int threadCount;
    private int bufferSize;

    IndexerConfig(String typeName, Class<?> indexClazz, int threadCount, int bufferSize) {
        this.typeName = typeName;
        this.indexClazz = indexClazz;
        this.threadCount = threadCount;
        this.bufferSize = bufferSize;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getIndexClazz() {
        return indexClazz;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
