package com.azure.csu.tiger.rm.api.service.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.csu.tiger.rm.api.enums.UploadFileEnum;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.response.UploadFileResponse;
import com.azure.csu.tiger.rm.api.service.BlobService;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class BlobServiceImpl implements BlobService {

    private static final Logger logger = LoggerFactory.getLogger(BlobServiceImpl.class);

    @Autowired
    private TokenCredential tokenCredential;
    @Autowired
    private AzureResourceHelper azureResourceHelper;

    @Override
    public UploadFileResponse uploadFile(String storageAccount, String container, String fileName, MultipartFile file) {
//        if (!azureResourceHelper.existStorageAccount(storageAccount)) {
//            throw new RmException(HttpStatus.BAD_REQUEST, "Storage account not found");
//        }

        // 使用默认凭据获取 BlobServiceClient
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net",storageAccount))
                .credential(tokenCredential)
                .buildClient();

        // 获取 BlobClient
        try {
            BlobClient blobClient = blobServiceClient.getBlobContainerClient(container)
                    .getBlobClient(fileName);

            // 上传 Blob
            InputStream dataStream = file.getInputStream();
            blobClient.upload(dataStream, file.getSize(), true);
            logger.info("Blob uploaded successfully! blob url is: {}", blobClient.getBlobUrl());
            return UploadFileResponse.from(UploadFileEnum.Success.name(), blobClient.getBlobUrl(), null);
        } catch (IOException e) {
            logger.error("Failed to upload Blob: " + e.getMessage());
            return UploadFileResponse.from(UploadFileEnum.Failed.name(), null,"Failed to upload Blob: " + e.getMessage());
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                logger.warn("Container not found");
                return UploadFileResponse.from(UploadFileEnum.Failed.name(), null, "Container not found");
            }
            logger.error("Failed to upload Blob: " + e.getMessage());
            return UploadFileResponse.from(UploadFileEnum.Failed.name(), null, "Failed to upload Blob: " + e.getMessage());
        }
    }
}
