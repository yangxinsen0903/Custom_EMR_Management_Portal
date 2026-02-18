package com.sunbox.domain;

import java.math.BigDecimal;
import java.util.Date;

public class InfoClusterPlaybookJob {
    private String transactionId;

    private String clusterId;

    private String confScriptId;

    private String jobType;

    private String clusterName;

    private String activityLogId;

    private String jobId;

    private String playbookUri;

    private String extraVars;

    private Integer sortNo;

    private Integer jobStatus;

    private Date begTime;

    private Date endTime;

    public static final Integer JOB_INIT=0;
    public static final Integer JOB_RUNNING=1;
    public static final Integer JOB_OK=2;
    public static final Integer JOB_FAILED=3;


    public String getConfScriptId() {
        return confScriptId;
    }

    public void setConfScriptId(String confScriptId) {
        this.confScriptId = confScriptId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
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

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId == null ? null : activityLogId.trim();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public String getPlaybookUri() {
        return playbookUri;
    }

    public void setPlaybookUri(String playbookUri) {
        this.playbookUri = playbookUri == null ? null : playbookUri.trim();
    }

    public String getExtraVars() {
        return extraVars;
    }

    public void setExtraVars(String extraVars) {
        this.extraVars = extraVars == null ? null : extraVars.trim();
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
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
        return "InfoClusterPlaybookJob{" +
                "transactionId='" + transactionId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", activityLogId='" + activityLogId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", playbookUri='" + playbookUri + '\'' +
                ", extraVars='" + extraVars + '\'' +
                ", sortNo=" + sortNo +
                ", jobStatus=" + jobStatus +
                ", begTime=" + begTime +
                ", endTime=" + endTime +
                '}';
    }
}