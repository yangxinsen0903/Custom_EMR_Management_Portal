package com.azure.csu.tiger.rm.api.service;

import com.azure.csu.tiger.rm.api.response.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BlobService {

    UploadFileResponse uploadFile(String storageAccount, String container, String fileName, MultipartFile file);
}
