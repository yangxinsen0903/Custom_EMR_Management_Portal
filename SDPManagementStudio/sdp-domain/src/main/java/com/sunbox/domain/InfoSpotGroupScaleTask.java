package com.sunbox.domain;

import java.util.Date;

public class InfoSpotGroupScaleTask {
    private String taskId;

    private String clusterId;

    private String groupId;

    private String vmRole;

    private Integer scaleMethod;

    private Integer scaleCount;

    private Integer actualCount;

    private Integer state;

    private String createdby;

    private Date createdTime;

    private String modifiedby;

    private Date modifiedTime;

    private Date begTime;

    private Date endTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public Integer getScaleMethod() {
        return scaleMethod;
    }

    public void setScaleMethod(Integer scaleMethod) {
        this.scaleMethod = scaleMethod;
    }

    public Integer getScaleCount() {
        return scaleCount;
    }

    public void setScaleCount(Integer scaleCount) {
        this.scaleCount = scaleCount;
    }

    public Integer getActualCount() {
        return actualCount;
    }

    public void setActualCount(Integer actualCount) {
        this.actualCount = actualCount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby == null ? null : modifiedby.trim();
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Date getBegTime() {
        return begTime;
    }

    public void setBegTime(Date begTime) {
        this.begTime = begTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "InfoSpotGroupScaleTask{" +
                "taskId='" + taskId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", scaleMethod=" + scaleMethod +
                ", scaleCount=" + scaleCount +
                ", actualCount=" + actualCount +
                ", state=" + state +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedby='" + modifiedby + '\'' +
                ", modifiedTime=" + modifiedTime +
                ", begTime=" + begTime +
                ", endTime=" + endTime +
                '}';
    }
}