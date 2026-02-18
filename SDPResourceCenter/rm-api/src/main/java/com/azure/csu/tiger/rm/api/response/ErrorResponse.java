package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class ErrorResponse {

    private String type;

    private String title;

    private Integer status;

    private String detail;

    private String instance;

    public ErrorResponse(Integer status, String title, String detail, String instance) {
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.instance = instance;
    }
}
