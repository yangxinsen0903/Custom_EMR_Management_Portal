package com.sunbox.domain;

public class InfoClusterFullLogWithBLOBs extends InfoClusterFullLog {
    private String requestParam;

    private String responseBody;

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam == null ? null : requestParam.trim();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody == null ? null : responseBody.trim();
    }

    @Override
    public String toString() {
        return "InfoClusterFullLogWithBLOBs{" +
                "requestParam='" + requestParam + '\'' +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }
}