package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class MetadataResponse {

    private List<Object> data;

    private String region;

    public static MetadataResponse from(List<Object> data, String region) {
        MetadataResponse response = new MetadataResponse();
        response.setData(data);
        response.setRegion(region);
        return response;
    }
}
