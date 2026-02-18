package com.sunbox.domain;

import java.util.Date;

public class InfoClusterVmIndex {
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 实例角色
     */
    private String vmRole;

    /**
     * 结束索引
     */
    private Integer endIndex;

    private Date createTime;

    private Date modifiedTime;

    private String taskId;

    private Integer beforeIndex;

    private Integer afterIndex;

    private Integer deltaIndex;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getBeforeIndex() {
        return beforeIndex;
    }

    public void setBeforeIndex(Integer beforeIndex) {
        this.beforeIndex = beforeIndex;
    }

    public Integer getAfterIndex() {
        return afterIndex;
    }

    public void setAfterIndex(Integer afterIndex) {
        this.afterIndex = afterIndex;
    }

    public Integer getDeltaIndex() {
        return deltaIndex;
    }

    public void setDeltaIndex(Integer deltaIndex) {
        this.deltaIndex = deltaIndex;
    }

    @Override
    public String toString() {
        return "InfoClusterVmIndex{" +
                "clusterId='" + clusterId + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", endIndex=" + endIndex +
                ", createTime=" + createTime +
                ", modifiedTime=" + modifiedTime +
                ", taskId='" + taskId + '\'' +
                ", beforeIndex=" + beforeIndex +
                ", afterIndex=" + afterIndex +
                ", deltaIndex=" + deltaIndex +
                '}';
    }
}
