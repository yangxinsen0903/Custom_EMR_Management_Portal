package com.sunbox.domain;

import java.util.Date;

public class InfoSpotGroupScaleTaskItem {
    private String itemId;

    private String taskId;

    private String clusterId;

    private String groupId;

    private String vmRole;

    private String vmName;

    private String hostname;

    private Integer scaleMethod;

    private Integer state;

    private Date expectedTime;

    private String createdby;

    private Date createdTime;

    private String modifiedby;

    private Date modifiedTime;

    private Date begTime;

    private Date endTime;

    private String reason;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId == null ? null : itemId.trim();
    }

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

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName == null ? null : vmName.trim();
    }

    public Integer getScaleMethod() {
        return scaleMethod;
    }

    public void setScaleMethod(Integer scaleMethod) {
        this.scaleMethod = scaleMethod;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(Date expectedTime) {
        this.expectedTime = expectedTime;
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InfoSpotGroupScaleTaskItem{");
        sb.append("itemId='").append(itemId).append('\'');
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", groupId='").append(groupId).append('\'');
        sb.append(", vmRole='").append(vmRole).append('\'');
        sb.append(", vmName='").append(vmName).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", scaleMethod=").append(scaleMethod);
        sb.append(", state=").append(state);
        sb.append(", expectedTime=").append(expectedTime);
        sb.append(", createdby='").append(createdby).append('\'');
        sb.append(", createdTime=").append(createdTime);
        sb.append(", modifiedby='").append(modifiedby).append('\'');
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", begTime=").append(begTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", reason='").append(reason).append('\'');
        sb.append('}');
        return sb.toString();
    }
}