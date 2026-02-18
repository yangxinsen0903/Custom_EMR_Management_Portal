package com.azure.csu.tiger.ansible.api.vo;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExecuteResponse {

    private Integer status;

    private String code;

    private String message;

    private List<String> data;

    public static ExecuteResponse success(List<String> data) {
        ExecuteResponse response = new ExecuteResponse();
        response.setStatus(200);
        response.setCode("Success");
        response.setMessage("");
        response.setData(data);
        return response;
    }

    public static ExecuteResponse badRequest(String message) {
        ExecuteResponse response = new ExecuteResponse();
        response.setStatus(400);
        response.setCode("Bad Request");
        response.setMessage(message);
        response.setData(Lists.newArrayList());
        return response;
    }
}
