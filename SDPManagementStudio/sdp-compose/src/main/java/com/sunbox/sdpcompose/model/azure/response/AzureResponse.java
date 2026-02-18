package com.sunbox.sdpcompose.model.azure.response;

import java.util.List;

public class AzureResponse {
    /**
     * 代码
     */
    private String code;
    private Object data;
    /**
     * 消息
     */
    private String message;
    /**
     * 状态
     */
    private String status;

    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }

    public Object getData() { return data; }
    public void setData(Object value) { this.data = value; }

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }

    @Override
    public String toString() {
        return "AzureResponse{" +
                "code='" + code + '\'' +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}