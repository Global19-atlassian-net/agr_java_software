package org.alliancegenome.variant_indexer.es;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.alliancegenome.es.util.EsClientFactory;
import org.alliancegenome.variant_indexer.config.VariantConfigHelper;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ESDocumentInjector extends Thread {

    private BulkProcessor.Builder builder;
    private BulkProcessor bulkProcessor;
    private String indexName = "site_variant_index";
    //private boolean createIndex = false;
    private LinkedBlockingQueue<IndexRequest> queue = new LinkedBlockingQueue<>(VariantConfigHelper.getIndexRequestQueueSize());

    private RestHighLevelClient client = EsClientFactory.createNewClient();

    public ESDocumentInjector() {

        BulkProcessor.Listener listener = new BulkProcessor.Listener() { 
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                //log.info("Size: " + request.requests().size() + " MB: " + request.estimatedSizeInBytes() + " Time: " + response.getTook() + " Bulk Requet Finished");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("Bulk Request Failure: " + failure.getMessage());
                for(DocWriteRequest<?> req: request.requests()) {
                    IndexRequest idxreq = (IndexRequest)req;
                    try {
                        queue.offer(idxreq, 10, TimeUnit.DAYS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.error("Finished Adding requests to Queue:");
            }
        };

        log.info("Creating Bulk Processor");
        builder = BulkProcessor.builder((request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener);
        builder.setBulkActions(VariantConfigHelper.getEsBulkActionSize());
        builder.setConcurrentRequests(VariantConfigHelper.getEsBulkConcurrentRequestsAmount());
        builder.setBulkSize(new ByteSizeValue(VariantConfigHelper.getEsBulkSizeMB(), ByteSizeUnit.MB));
        builder.setFlushInterval(TimeValue.timeValueSeconds(180L));
        builder.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1L), 60));

        bulkProcessor = builder.build();

        log.info("Finished Creating Bulk Processor");

        start();
    }

    public void createIndex() {
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
        indexRequest.settings(Settings.builder() 
                .put("index.number_of_shards", VariantConfigHelper.getEsNumberOfShards())
                .put("index.refresh_interval", -1)
                .put("index.number_of_replicas", 0)
                );

        try {
            CreateIndexResponse createIndexResponse = client.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
                bulkProcessor.add(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        log.info("Closing Down Injector: ");
        try {
            while(queue.size() > 0) {
                sleep(1000);
            }
            log.info("Closing Down bulkProcessor: ");
            bulkProcessor.awaitClose(20, TimeUnit.MINUTES);
            log.info("Closing Down bulkProcessor Finished: ");
            queue = null;
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Injector Closed: ");
    }

    public void addDocument(String json) {
        try {
            queue.offer(new IndexRequest(indexName).source(json, XContentType.JSON), 10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
