package com.sunbox.domain;

import java.util.Date;

public class InfoThirdApiFailedLogWithBLOBs extends InfoThirdApiFailedLog {
    private String apiKeyParam;

    private String exceptionInfo;

    public String getApiKeyParam() {
        return apiKeyParam;
    }

    public void setApiKeyParam(String apiKeyParam) {
        this.apiKeyParam = apiKeyParam == null ? null : apiKeyParam.trim();
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo == null ? null : exceptionInfo.trim();
    }

    public InfoThirdApiFailedLogWithBLOBs withApiKeyParam(String apiKeyParam){
        this.apiKeyParam=apiKeyParam;
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withExceptionInfo(String exceptionInfo){
        this.exceptionInfo = exceptionInfo;
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withApiName(String apiName){
        this.setApiName(apiName);
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withApiUrl(String apiurl){
        this.setApiUrl(apiurl);
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withFailedType(Integer failedType){
        this.setFailedType(failedType);
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withTimeOut(Integer timeout){
        this.setTimeOut(timeout);
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withCreatedTime(Date createdTime){
        this.setCreatedTime(createdTime);
        return this;
    }

    public InfoThirdApiFailedLogWithBLOBs withRegion(String region) {
        this.setRegion(region);
        return this;
    }
    @Override
    public String toString() {
        return "InfoThirdApiFailedLogWithBLOBs{" +
                "apiKeyParam='" + apiKeyParam + '\'' +
                ", exceptionInfo='" + exceptionInfo + '\'' +
                '}';
    }
}