package org.alliancegenome.variant_indexer.vcf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alliancegenome.variant_indexer.download.model.DownloadFileSet;
import org.alliancegenome.variant_indexer.download.model.DownloadSource;
import org.alliancegenome.variant_indexer.download.model.DownloadableFile;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class VCFDocumentCreationManager extends Thread {

    private DownloadFileSet downloadSet;

    public VCFDocumentCreationManager(DownloadFileSet downloadSet) {
        this.downloadSet = downloadSet;
    }
    
    public void run() {
        
        try {

            ExecutorService executor = Executors.newFixedThreadPool(downloadSet.getDocumentCreatorThreads());
            
            for(DownloadSource source: downloadSet.getDownloadFileSet()) {
                for(DownloadableFile df: source.getFileList()) {
                    VCFDocumentCreator creator = new VCFDocumentCreator(df);
                    executor.execute(creator);
                }
            }
            
            executor.shutdown();  
            while (!executor.isTerminated()) {   } 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
