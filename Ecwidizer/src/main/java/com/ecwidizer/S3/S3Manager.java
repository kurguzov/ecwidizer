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
    private static final String PROPERTIES_FILE = "s3_access.properties";
    private static Properties properties = new Properties();
    private static AmazonS3Client s3Client;
    private static String accessKey;
    private static String secretKey;
    private static String bucketName;
    AtomicInteger internalCounter = new AtomicInteger(0);

    public interface ImageUploadedConsumer {
        public void imageUploaded(String imageUri);
    }

    public static S3Manager getInstance(Context context) throws S3ManagerInitializeException {
        S3Manager.context = context;
        if(s3Manager == null) {
            s3Manager = new S3Manager();
        }
        init();
        return s3Manager;
    }

    private static void init() throws S3ManagerInitializeException {
        if (s3Client != null) {
            return;
        }
        try {
            AssetManager assetManager = context.getResources().getAssets();
            properties.load(assetManager.open(PROPERTIES_FILE));
            accessKey = properties.getProperty("accessKey");
            secretKey = properties.getProperty("secretKey");
            bucketName = properties.getProperty("bucketname");
            s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
        } catch (Exception e) {
            throw new S3ManagerInitializeException("Can't initialize S3Manager");
        }
        Logger.log("S3 Manager is initialized");
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
                } catch (Exception e) {
                    consumer.imageUploaded(null);
                    Logger.error("Image upload failed", e);
                    return;
                }
                consumer.imageUploaded("http://s3.amazonaws.com/" + bucketName + "/" + imageName);

            }
        }.start();
    }

    private String createImageName() {
        return internalCounter.incrementAndGet() + "_" + System.currentTimeMillis() + ".jpg";
    }
}
