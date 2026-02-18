package com.sunbox.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class InfoClusterVmDeleteDetail {
    private Long id;

    private String clusterId;

    private String vmName;

    private String vmRole;

    private Integer priority;

    private String purchaseType;

    private String planId;

    private Integer status;

    private String jobId;

    private Date begSendRequestTime;

    private Date getDeleteJobidTime;

    private Integer retryCount;

    private Date releaseFreezeTime;

    private Integer freezeCount;

    private Date createdTime;

    private Date modifiedTime;

    private String clusterName;
    private String planName;
    private String hostName;


    private String region;
    private String regionName;


    //endregion

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName == null ? null : vmName.trim();
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole == null ? null : vmRole.trim();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType == null ? null : purchaseType.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public Date getBegSendRequestTime() {
        return begSendRequestTime;
    }

    public void setBegSendRequestTime(Date begSendRequestTime) {
        this.begSendRequestTime = begSendRequestTime;
    }

    public Date getGetDeleteJobidTime() {
        return getDeleteJobidTime;
    }

    public void setGetDeleteJobidTime(Date getDeleteJobidTime) {
        this.getDeleteJobidTime = getDeleteJobidTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Date getReleaseFreezeTime() {
        return releaseFreezeTime;
    }

    public void setReleaseFreezeTime(Date releaseFreezeTime) {
        this.releaseFreezeTime = releaseFreezeTime;
    }

    public Integer getFreezeCount() {
        return freezeCount;
    }

    public void setFreezeCount(Integer freezeCount) {
        this.freezeCount = freezeCount;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InfoClusterVmDeleteDetail{");
        sb.append("id=").append(id);
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", vmName='").append(vmName).append('\'');
        sb.append(", vmRole='").append(vmRole).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(", purchaseType='").append(purchaseType).append('\'');
        sb.append(", planId='").append(planId).append('\'');
        sb.append(", status=").append(status);
        sb.append(", jobId='").append(jobId).append('\'');
        sb.append(", begSendRequestTime=").append(begSendRequestTime);
        sb.append(", getDeleteJobidTime=").append(getDeleteJobidTime);
        sb.append(", retryCount=").append(retryCount);
        sb.append(", releaseFreezeTime=").append(releaseFreezeTime);
        sb.append(", freezeCount=").append(freezeCount);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", clusterName='").append(clusterName).append('\'');
        sb.append(", planName='").append(planName).append('\'');
        sb.append(", hostName='").append(hostName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}