package com.azure.csu.tiger.rm.api.response;

import com.azure.csu.tiger.rm.api.enums.UploadFileEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class UploadFileResponse {

    private String result;

    private String blobUrl;

    private String message;

    public static UploadFileResponse from(String result, String blobUrl, String message) {
        UploadFileResponse response = new UploadFileResponse();
        response.setResult(result);
        response.setBlobUrl(blobUrl);
        response.setMessage(message);
        return response;
    }
}
