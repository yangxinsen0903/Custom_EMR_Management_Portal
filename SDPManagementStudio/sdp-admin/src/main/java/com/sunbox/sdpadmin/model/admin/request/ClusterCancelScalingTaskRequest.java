package com.sunbox.sdpadmin.model.admin.request;

import javax.validation.constraints.NotEmpty;


public class ClusterCancelScalingTaskRequest {
    // 集群id
    @NotEmpty(message = "集群id不能为空")
    private String clusterId;

    // 实例类型
    @NotEmpty(message = "任务id")
    private String taskId;

    // 实例组名称
    @NotEmpty(message = "实例组名称不能为空")
    private String groupName;

    private String createdBy;

    private String user;

    private String userRealName;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClusterCancelScalingTaskRequest{");
        sb.append("clusterId='").append(clusterId).append('\'');
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
