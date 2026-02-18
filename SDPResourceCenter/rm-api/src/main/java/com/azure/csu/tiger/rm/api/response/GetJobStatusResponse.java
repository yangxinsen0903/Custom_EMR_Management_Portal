package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class GetJobStatusResponse extends JobResponse{

    private String code;

    private Object message;

    private Object Data;

    public static GetJobStatusResponse from(JobResponse jobResponse) {
        GetJobStatusResponse response = new GetJobStatusResponse();
        response.setId(jobResponse.getId());
        response.setName(jobResponse.getName());
        response.setType(jobResponse.getType());
        response.setStatus(jobResponse.getStatus());
        return response;
    }

    public static GetJobStatusResponse from(JobResponse jobResponse, Object info) {
        GetJobStatusResponse response = new GetJobStatusResponse();
        response.setData(info);
        response.setId(jobResponse.getId());
        response.setName(jobResponse.getName());
        response.setType(jobResponse.getType());
        response.setStatus(jobResponse.getStatus());
        return response;
    }

    public static GetJobStatusResponse from(JobResponse jobResponse, String message) {
        GetJobStatusResponse response = new GetJobStatusResponse();
        response.setMessage(message);
        response.setId(jobResponse.getId());
        response.setName(jobResponse.getName());
        response.setType(jobResponse.getType());
        response.setStatus(jobResponse.getStatus());
        return response;
    }

    public static GetJobStatusResponse from(JobResponse jobResponse, Object info, Object message) {
        GetJobStatusResponse response = new GetJobStatusResponse();
        response.setData(info);
        response.setMessage(message);
        response.setId(jobResponse.getId());
        response.setName(jobResponse.getName());
        response.setType(jobResponse.getType());
        response.setStatus(jobResponse.getStatus());
        return response;
    }
}
