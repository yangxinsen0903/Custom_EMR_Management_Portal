package com.sunbox.domain;


import java.util.Date;

public class InfoClusterVmDelete {
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

    //region 优先级定义
    // 按需实例
    public static Integer PRIORITY_OD = 1;
    // 弹性缩容或手工缩容
    public static Integer PRIORITY_SCALEIN =1;
    // 竞价逐出
    public static Integer PRIORITY_EVICTION=0;
    // 清理VM
    public static Integer PRIORITY_CLEAR=1;
    // 删除失败的VM
    public static Integer PRIORITY_FAILED=-1;
    // 默认
    public static Integer PRIORITY_DEFAULT=0;
    //endregion 优先级定义


    //region 状态枚举
    // 冻结
    public static Integer STATUS_FREEZE=-1;

    //init
    public static Integer STATUS_INIT=0;

    // 发送删除请求中
    public static Integer STATUS_DELETE_REQUEST_SENDING=1;

    // 删除中
    public static Integer STATUS_DELETING=2;

    //删除完成
    public static Integer STATUS_DELETE_SUCCESS=3;

    //删除失败
    public static Integer STATUS_DELETE_FAILED= -9;


    //endregion

    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

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

    @Override
    public String toString() {
        return "InfoClusterVmDelete{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", vmName='" + vmName + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", priority=" + priority +
                ", purchaseType='" + purchaseType + '\'' +
                ", planId='" + planId + '\'' +
                ", status=" + status +
                ", jobId='" + jobId + '\'' +
                ", begSendRequestTime=" + begSendRequestTime +
                ", getDeleteJobidTime=" + getDeleteJobidTime +
                ", retryCount=" + retryCount +
                ", releaseFreezeTime=" + releaseFreezeTime +
                ", freezeCount=" + freezeCount +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}