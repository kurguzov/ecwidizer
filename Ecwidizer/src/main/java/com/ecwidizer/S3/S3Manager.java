package com.ecwidizer.S3;

import android.content.Context;
import android.content.res.AssetManager;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ecwidizer.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sergeyvyachkilev on 9/13/13.
 */
public class S3Manager {

    private static S3Manager s3Manager;
    private static Context context;
    private static AmazonS3Client s3Client;
    private static String bucketName = "ecwid-dev-kaktus";
    private static final AtomicInteger internalCounter = new AtomicInteger(0);

    public interface ImageUploadedConsumer {
        public void imageUploaded(String imageUri);
		public void onFailure(Throwable e);
    }

    public static S3Manager getInstance(Context context) throws S3ManagerInitializeException {
        S3Manager.context = context;

        if(s3Manager == null) {
            s3Manager = new S3Manager();
        }

        if (s3Client != null) {
            return s3Manager;
        }

        try {
            s3Client = new AmazonS3Client(new BasicAWSCredentials("AKIAJJDL7RRZN3BOIP4A", "THFDsUll6PPsAEvlFERsZNSCTpiY93cNP6yJZm3Q"));
        } catch (Throwable e) {
            throw new S3ManagerInitializeException("Can't initialize S3Manager", e);
        }
        Logger.log("S3 Manager is initialized");


        return s3Manager;
    }

    /**
     * Consumer returns null if there are any problems with upload
     * @param imageFile
     * @param consumer
     */
    public void uploadToS3(final File imageFile, final ImageUploadedConsumer consumer) {

        new Thread() {
            @Override
            public void run() {
                String imageName = createImageName();
                try {
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    objectMetadata.setContentLength(imageFile.length());
                    objectMetadata.setContentType("image/jpeg");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                    String expiresHeader = dateFormat.format(new Date(365 * 60*60*24 * 1000L)) + " GMT";
                    objectMetadata.setHeader("Expires", expiresHeader);

                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, imageName, imageFile);
                    putObjectRequest.setMetadata(objectMetadata);
                    s3Client.putObject(putObjectRequest);
                    Logger.log("Image successfully uploaded " + "http://s3.amazonaws.com/" + bucketName + "/" + imageName);
					consumer.imageUploaded("http://s3.amazonaws.com/" + bucketName + "/" + imageName);
                } catch (Throwable e) {
					consumer.onFailure(e);
                }
            }
        }.start();
    }

    private String createImageName() {
        return internalCounter.incrementAndGet() + "_" + System.currentTimeMillis() + ".jpg";
    }
}
