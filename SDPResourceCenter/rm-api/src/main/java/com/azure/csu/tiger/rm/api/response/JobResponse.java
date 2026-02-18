package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class JobResponse {

    private String id;

    private String name;

    private String type;

    private String status;

    public static JobResponse from(String id, String name, String type, String status) {
        JobResponse response = new JobResponse();
        response.setId(id);
        response.setName(name);
        response.setType(type);
        response.setStatus(status);
        return response;
    }
}
