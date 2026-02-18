package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.enums.UploadFileEnum;
import com.azure.csu.tiger.rm.api.response.UploadFileResponse;
import com.azure.csu.tiger.rm.api.service.BlobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(tags="Blob rest api")
@RequestMapping("/api/v1/blobs")
@RestController
public class BlobController {

    private static final Logger logger = LoggerFactory.getLogger(BlobController.class);

    @Autowired
    private BlobService blobService;

    @ApiOperation(value = "上传脚本文件")
    @PostMapping(path = "/{storageAccount}/{container}/{fileName}")
    public ResponseEntity<UploadFileResponse> uploadScript(@PathVariable String storageAccount, @PathVariable String container,
                                               @PathVariable String fileName, @RequestParam("file") MultipartFile file) {
        if (file.getSize() > 50 * 1024 * 1024) {
            return ResponseEntity.ok(UploadFileResponse.from(UploadFileEnum.Failed.name(), null, "file exceed 50M"));
        }
        UploadFileResponse response = blobService.uploadFile(storageAccount, container, fileName, file);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "上传日志文件")
    @PostMapping(path = "/logs/{storageAccount}/{container}/{serviceName}/{logDate}/{fileName}")
    public ResponseEntity<UploadFileResponse> uploadLog(@PathVariable String storageAccount, @PathVariable String container,
                                            @PathVariable String serviceName, @PathVariable String logDate,
                                               @PathVariable String fileName, @RequestParam("file") MultipartFile file) {
        if (file.getSize() > 200 * 1024 * 1024) {
            return ResponseEntity.ok(UploadFileResponse.from(UploadFileEnum.Failed.name(),null, "file exceed 200M"));
        }
        UploadFileResponse response = blobService.uploadFile(storageAccount, container, String.format("%s/%s/%s", serviceName, logDate, fileName), file);
        return ResponseEntity.ok(response);
    }

}
