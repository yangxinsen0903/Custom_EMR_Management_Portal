package com.sunbox.domain;

import java.util.Date;

/**
 * 操作虚拟机任务信息,
 * 从Azure Api创建VM或删除VM时都是异步操作,每次调用的异步任务都放在此表中.
 */
public class InfoClusterVmJob {
    private String transactionId;

    private String clusterId;

    private String clusterName;

    /** 操作类型;create/delete
     * 更多值见: {@link InfoClusterOperationPlan}.Plan_OP_* 的常量
     * */
    private String operationType;

    /** 任务ID, 由Azure Api返回 */
    private String jobId;

    /** 任务状态;0 job创建完成 1 执行中 2 执行完成 3 job行失败 */
    private Integer jobStatus;

    private Date begTime;

    private Date endTime;

    /** 调度任务活动ID */
    private String activityLogId;

    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId == null ? null : transactionId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType == null ? null : operationType.trim();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
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
        return "InfoClusterVmJob{" +
                "transactionId='" + transactionId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", operationType='" + operationType + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobStatus=" + jobStatus +
                ", begTime=" + begTime +
                ", endTime=" + endTime +
                '}';
    }
}