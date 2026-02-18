package com.sunbox.domain;

import java.util.Date;

public class InfoAmbariConfigGroup {
    public static final Integer STATE_DELETE = -1;
    public static final Integer STATE_RUNNING = 1;
    public static final Integer STATE_SCALEOUT = 2;
    public static final Integer STATE_SCALEIN = 3;
    public static final Integer STATE_CREATING = 0;
    /**
     * 配置ID
     */
    private String confId;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 实例组ID
     */
    private String groupId;

    /**
     * ambari配置ID
     */
    private Long ambariId;

    /**
     * ambari hadoop 服务名称
     */
    private String ambariServiceName;

    /**
     * ambari config groupname
     */
    private String ambariGroupName;

    /**
     *
     */
    private String ambariTag;

    /**
     * ambari 集群名称
     */
    private String ambariClusterName;

    private String ambariDescription;

    private Integer state;

    private Date createdTime;
    private String sdpGroupName;
    /**
     * 实例组SKU ID
     */
    private String vmSkuId;

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId == null ? null : confId.trim();
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

    public Long getAmbariId() {
        return ambariId;
    }

    public void setAmbariId(Long ambariId) {
        this.ambariId = ambariId;
    }

    public String getAmbariServiceName() {
        return ambariServiceName;
    }

    public void setAmbariServiceName(String ambariServiceName) {
        this.ambariServiceName = ambariServiceName == null ? null : ambariServiceName.trim();
    }

    public String getAmbariGroupName() {
        return ambariGroupName;
    }

    public void setAmbariGroupName(String ambariGroupName) {
        this.ambariGroupName = ambariGroupName == null ? null : ambariGroupName.trim();
    }

    public String getAmbariTag() {
        return ambariTag;
    }

    public void setAmbariTag(String ambariTag) {
        this.ambariTag = ambariTag == null ? null : ambariTag.trim();
    }

    public String getAmbariClusterName() {
        return ambariClusterName;
    }

    public void setAmbariClusterName(String ambariClusterName) {
        this.ambariClusterName = ambariClusterName == null ? null : ambariClusterName.trim();
    }

    public String getAmbariDescription() {
        return ambariDescription;
    }

    public void setAmbariDescription(String ambariDescription) {
        this.ambariDescription = ambariDescription == null ? null : ambariDescription.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setSdpGroupName(String sdpGroupName) {
        this.sdpGroupName = sdpGroupName;
    }

    public String getSdpGroupName() {
        return sdpGroupName;
    }

    public String getVmSkuId() {
        return vmSkuId;
    }

    public void setVmSkuId(String vmSkuId) {
        this.vmSkuId = vmSkuId;
    }

    @Override
    public String toString() {
        return "InfoAmbariConfigGroup{" +
                "confId='" + confId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", ambariId=" + ambariId +
                ", ambariServiceName='" + ambariServiceName + '\'' +
                ", ambariGroupName='" + ambariGroupName + '\'' +
                ", ambariTag='" + ambariTag + '\'' +
                ", ambariClusterName='" + ambariClusterName + '\'' +
                ", ambariDescription='" + ambariDescription + '\'' +
                ", state=" + state +
                ", createdTime=" + createdTime +
                ", sdpGroupName='" + sdpGroupName + '\'' +
                ", vmSkuId='" + vmSkuId + '\'' +
                '}';
    }
}