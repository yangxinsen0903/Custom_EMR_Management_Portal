package com.azure.csu.tiger.ansible.agent.helper;


import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AzureBlobHelper {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobHelper.class);

    @Autowired
    private TokenCredential defaultAzureCredential;

    @Value("${ansible.scriptpath}")
    private String scriptPath;

    private HttpClient httpClient;

    @PostConstruct
    private void init() {
        httpClient = HttpClients.createDefault();
    }

    public String downloadFileByUrl(String blobUrl) {

        BlobClient blobClient = null;
        ByteArrayOutputStream outputStream = null;
        try {
            // Build a BlobClient to interact with the Blob
            blobClient = new BlobClientBuilder().credential(defaultAzureCredential).endpoint(blobUrl).buildClient();
            String blobFullName = blobClient.getBlobName();
            String blobName = blobFullName.substring(blobFullName.lastIndexOf("/")+1);
            outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            String playbookFullPathName = scriptPath+blobName;
            FileHelper.createFile(playbookFullPathName,outputStream,true);

            logger.info("File downloaded successfully to " + scriptPath);
            return scriptPath+blobName;
        } catch (Exception e) {

            logger.error("Error occurred while trying to download the blob: " + e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            logger.info("Close the blob client" + blobUrl);
            if(blobClient!=null) blobClient=null;
            if(outputStream!=null) outputStream = null;
        }
    }

    public String downloadFileByHttpClient(String url) {
        HttpGet request = new HttpGet(url);
        String fileName = url.substring(url.lastIndexOf("/")+1);
        String playbookFullPathName = scriptPath+fileName;
        try {
            return httpClient.execute(request, response -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outputStream);
                FileHelper.createFile(playbookFullPathName, outputStream, true);
                return playbookFullPathName;
            });
        } catch (IOException e) {
            logger.error("Error occurred while trying to download the file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}


