package com.sunbox.domain;

import java.util.Date;

public class BaseClusterOperationTemplateActivity {
    private String activityId;

    private String templateId;

    private String activityType;

    private String activityName;

    private Integer sortNo;

    private Integer timeout;

    private String createdby;

    private Date createdTime;

    private String activityCnname;

    public String getActivityCnname() {
        return activityCnname;
    }

    public void setActivityCnname(String activityCnname) {
        this.activityCnname = activityCnname;
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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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

    @Override
    public String toString() {
        return "BaseClusterOperationTemplateActivity{" +
                "activityId='" + activityId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", activityType='" + activityType + '\'' +
                ", activityName='" + activityName + '\'' +
                ", sortNo=" + sortNo +
                ", timeout=" + timeout +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", activityCnname='" + activityCnname + '\'' +
                '}';
    }
}