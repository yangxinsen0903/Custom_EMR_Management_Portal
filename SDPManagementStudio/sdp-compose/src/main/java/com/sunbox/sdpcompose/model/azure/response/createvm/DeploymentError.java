/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.model.azure.response.createvm;

import java.util.List;

/**
 * @author wangda
 * @date 2024/7/17
 */
public class DeploymentError {
    private String code;
    private String message;
    private String target;
    private List<DeploymentErrorDetail> details;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<DeploymentErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(List<DeploymentErrorDetail> details) {
        this.details = details;
    }
}
