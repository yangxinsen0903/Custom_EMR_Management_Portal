package com.sunbox.domain;

import java.util.Date;

/**
 * VM清理日志
 */
public class InfoClusterVmClearLog {
    private Long id;

    private String clusterId;

    /** 任务ID */
    private String planId;

    private String vmName;

    private String vmRole;

    /**
     * 删除VM的任务ID, 调用Azure Api接口返回
     */
    private String jobId;

    /**
     * 状态, 见本类中的VM_Clear_Status_*开头的常量
     */
    private Integer status;

    private Date createdTime;

    private Date modifiedTime;

    //region Clear Log 状态枚举
    /**
     * 数据初始化
     */
    public static final int VM_Clear_Status_INIT=0;

    /**
     *  删除中
     */
    public static final int VM_Clear_Status_Deleting=1;
    /**
     * 已删除
     */
    public static final int VM_Clear_Status_Deleted=2;

    /**
     *  删除失败
     */
    public static final int VM_Clear_Status_Failed = -1;

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

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName == null ? null : vmName.trim();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    @Override
    public String toString() {
        return "InfoClusterVmClearLog{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", planId='" + planId + '\'' +
                ", vmName='" + vmName + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", jobId='" + jobId + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}