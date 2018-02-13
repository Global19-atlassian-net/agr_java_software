package org.alliancegenome.shared.aws;

import java.io.File;
import java.util.Date;

import org.alliancegenome.shared.config.ConfigHelper;
import org.alliancegenome.shared.exceptions.FileSavingException;
import org.alliancegenome.shared.exceptions.GenericException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Helper {

	private Log log = LogFactory.getLog(getClass());

	public int listFiles(String prefix) {
		int count = 0;
		try {
			log.info("Getting S3 file listing");
			AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ConfigHelper.getAWSAccessKey(), ConfigHelper.getAWSSecretKey()))).withRegion(Regions.US_EAST_1).build();
			ObjectListing ol = s3.listObjects(ConfigHelper.getAWSBucketName(), prefix);
			log.debug(ol.getObjectSummaries().size());
			count = ol.getObjectSummaries().size();
			for (S3ObjectSummary summary : ol.getObjectSummaries()) {
				log.debug(" - " + summary.getKey() + "\t(size = " + summary.getSize() + ")\t(lastModified = " + summary.getLastModified() + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public void saveFile(String path, String fileData) throws GenericException {
		try {
			Date d = new Date();
			String outFileName = "tmp.data_" + d.getTime();
			File outfile = new File(outFileName);
			log.info("Saving file to local filesystem: " + outfile.getAbsolutePath());
			FileUtils.writeStringToFile(outfile, fileData);
			log.info("Save file to local filesystem complete");

			log.info("Uploading file to S3: " + outfile.getAbsolutePath() + " -> s3://" + ConfigHelper.getAWSBucketName() + "/" + path);
			AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ConfigHelper.getAWSAccessKey(), ConfigHelper.getAWSSecretKey()))).withRegion(Regions.US_EAST_1).build();
			TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3).build();
			final Upload uploadFile = tm.upload(ConfigHelper.getAWSBucketName(), path, outfile);
			uploadFile.waitForCompletion();
			tm.shutdownNow();
			outfile.delete();
			log.info("S3 Upload complete");
		} catch (Exception e) {
			e.printStackTrace();
			throw new FileSavingException(e.getMessage());
		}
	}

}
