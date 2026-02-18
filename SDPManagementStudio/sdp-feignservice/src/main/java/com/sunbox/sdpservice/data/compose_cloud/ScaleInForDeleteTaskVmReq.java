package com.sunbox.sdpservice.data.compose_cloud;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class ScaleInForDeleteTaskVmReq {
    @NotBlank
    private String clusterId;

    @NotBlank
    private String groupId;

    @NotBlank
    private String scaleOutTaskId;

    @NotNull
    private List<String> vmNames;

    @NotNull
    private Date createdTime;

    private ScaleInForDeleteTaskVmReq(){}

    public ScaleInForDeleteTaskVmReq(String clusterId, String groupId, String scaleOutTaskId, List<String> vmNames, Date createdTime) {
        this.clusterId = clusterId;
        this.groupId = groupId;
        this.scaleOutTaskId = scaleOutTaskId;
        this.vmNames = vmNames;
        this.createdTime = createdTime;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getScaleOutTaskId() {
        return scaleOutTaskId;
    }

    public void setScaleOutTaskId(String scaleOutTaskId) {
        this.scaleOutTaskId = scaleOutTaskId;
    }

    public List<String> getVmNames() {
        return vmNames;
    }

    public void setVmNames(List<String> vmNames) {
        this.vmNames = vmNames;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "ScaleInForDeleteTaskVmReq{" +
                "clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", scaleOutTaskId='" + scaleOutTaskId + '\'' +
                ", vmNames=" + vmNames +
                ", createdTime=" + createdTime +
                '}';
    }
}
