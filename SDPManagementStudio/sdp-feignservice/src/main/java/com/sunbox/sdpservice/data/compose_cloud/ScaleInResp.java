package com.sunbox.sdpservice.data.compose_cloud;

public class ScaleInResp {
    private String taskId;

    private ScaleInResp() {
    }

    public ScaleInResp(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
}