package com.sunbox.domain;

import java.util.Date;

public class InfoClusterFullLog {
    private Long logId;

    private String clusterId;

    private String clusterName;

    private String activityLogId;

    private String actionName;

    private String planId;

    private Date requestTime;

    private Date responseTime;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
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

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName == null ? null : actionName.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {
        return "InfoClusterFullLog{" +
                "logId=" + logId +
                ", clusterId='" + clusterId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", activityLogId='" + activityLogId + '\'' +
                ", actionName='" + actionName + '\'' +
                ", planId='" + planId + '\'' +
                ", requestTime=" + requestTime +
                ", responseTime=" + responseTime +
                '}';
    }
}