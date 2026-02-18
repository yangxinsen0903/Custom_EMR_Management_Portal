package com.sunbox.sdpcompose.model.azure.fleet.response;

/**
 *  AzureFleet Job 结果
 */
public class AzureFleetJobResult {
    private String code;

    private Object data;

    private String id;

    private String message;

    private String name;

    private String status;

    private String type;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AzureFleetJobResult{" +
                "code='" + code + '\'' +
                ", data=" + data +
                ", id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
