package com.sunbox.domain;

import java.util.Date;

/**
 * 安装或扩容时被剔除集群的VM
 */
public class InfoClusterVmReject {
    /** 剔除ID */
    private String rejectId;

    private String clusterId;

    private String vmName;

    private String hostName;

    private String internalip;

    /** 执行步骤ID */
    private String activityLogId;

    /** 步骤名称 */
    private String activityCnName;

    private Date createdTime;

    private Date destroyTime;

    /** 剔除原因 */
    private String rejectReason;

    /** 执行计划ID */
    private String planId;

    public String getRejectId() {
        return rejectId;
    }

    public void setRejectId(String rejectId) {
        this.rejectId = rejectId == null ? null : rejectId.trim();
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName == null ? null : hostName.trim();
    }

    public String getInternalip() {
        return internalip;
    }

    public void setInternalip(String internalip) {
        this.internalip = internalip == null ? null : internalip.trim();
    }

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId == null ? null : activityLogId.trim();
    }

    public String getActivityCnName() {
        return activityCnName;
    }

    public void setActivityCnName(String activityCnName) {
        this.activityCnName = activityCnName == null ? null : activityCnName.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getDestroyTime() {
        return destroyTime;
    }

    public void setDestroyTime(Date destroyTime) {
        this.destroyTime = destroyTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason == null ? null : rejectReason.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    @Override
    public String toString() {
        return "InfoClusterVmReject{" +
                "rejectId='" + rejectId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", vmName='" + vmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", internalip='" + internalip + '\'' +
                ", activityLogId='" + activityLogId + '\'' +
                ", activityCnName='" + activityCnName + '\'' +
                ", createdTime=" + createdTime +
                ", destroyTime=" + destroyTime +
                ", rejectReason='" + rejectReason + '\'' +
                ", planId='" + planId + '\'' +
                '}';
    }
}