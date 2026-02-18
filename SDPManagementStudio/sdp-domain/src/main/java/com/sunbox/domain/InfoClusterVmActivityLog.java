package com.sunbox.domain;

import java.util.Date;

public class InfoClusterVmActivityLog {
    private Long id;

    private String clusterId;

    private String vmName;

    private String planId;

    private String activityLogId;

    private Integer activityType;

    private Date createdTime;

    private String remark;



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

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId == null ? null : activityLogId.trim();
    }

    public Integer getActivityType() {
        return activityType;
    }

    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    @Override
    public String toString() {
        return "InfoClusterVmActivityLog{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", vmName='" + vmName + '\'' +
                ", planId='" + planId + '\'' +
                ", activityLogId='" + activityLogId + '\'' +
                ", activityType=" + activityType +
                ", createdTime=" + createdTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}