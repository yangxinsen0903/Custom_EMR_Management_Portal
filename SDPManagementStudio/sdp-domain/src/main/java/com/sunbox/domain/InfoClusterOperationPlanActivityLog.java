package com.sunbox.domain;

import java.util.Date;

public class InfoClusterOperationPlanActivityLog {
    private String activityLogId;

    private String planId;

    private String activityId;

    private String templateId;

    private String activityType;

    private String activityName;

    private Integer sortNo;

    private Integer timeout;

    private Date begtime;

    private Date endtime;

    private Integer duration;

    private Integer state;

    private Integer retryCount;

    private String createdby;

    private Date createdTime;

    private String activityCnname;

    private Date lastRetryTime;


    public static final int ACTION_PLANING=0;
    public static final int ACTION_RUNNING=1;
    public static final int ACTION_COMPLETED=2;
    public static final int ACTION_TIMEOUT=-1;
    public static final int ACTION_FAILED=-2;

    public Date getLastRetryTime() {
        return lastRetryTime;
    }

    public void setLastRetryTime(Date lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getActivityCnname() {
        return activityCnname;
    }

    public void setActivityCnname(String activityCnname) {
        this.activityCnname = activityCnname;
    }

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId == null ? null : activityLogId.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId == null ? null : activityId.trim();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType == null ? null : activityType.trim();
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName == null ? null : activityName.trim();
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Date getBegtime() {
        return begtime;
    }

    public void setBegtime(Date begtime) {
        this.begtime = begtime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public String toString() {
        return "InfoClusterOperationPlanActivityLog{" +
                "activityLogId='" + activityLogId + '\'' +
                ", planId='" + planId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", activityType='" + activityType + '\'' +
                ", activityName='" + activityName + '\'' +
                ", sortNo=" + sortNo +
                ", timeout=" + timeout +
                ", begtime=" + begtime +
                ", endtime=" + endtime +
                ", duration=" + duration +
                ", state=" + state +
                ", retryCount=" + retryCount +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", activityCnname='" + activityCnname + '\'' +
                ", lastRetryTime=" + lastRetryTime +
                '}';
    }
}