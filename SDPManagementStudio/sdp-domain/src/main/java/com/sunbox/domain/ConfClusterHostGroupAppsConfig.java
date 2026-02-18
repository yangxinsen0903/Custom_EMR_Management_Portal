package com.sunbox.domain;

import java.util.Date;

public class ConfClusterHostGroupAppsConfig {
    public static final Integer DELETE_NO = 0;
    public static final Integer DELETE_YES = 1;
    private String appConfigItemId;

    private String clusterId;

    private String groupId;

    private String appConfigClassification;

    private String appName;

    private String configItem;

    private Integer isDelete;

    private Date createdTime;

    private String createdby;

    private String configVal;

    public String getAppConfigItemId() {
        return appConfigItemId;
    }

    public void setAppConfigItemId(String appConfigItemId) {
        this.appConfigItemId = appConfigItemId == null ? null : appConfigItemId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getAppConfigClassification() {
        return appConfigClassification;
    }

    public void setAppConfigClassification(String appConfigClassification) {
        this.appConfigClassification = appConfigClassification == null ? null : appConfigClassification.trim();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }

    public String getConfigItem() {
        return configItem;
    }

    public void setConfigItem(String configItem) {
        this.configItem = configItem == null ? null : configItem.trim();
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getConfigVal() {
        return configVal;
    }

    public void setConfigVal(String configVal) {
        this.configVal = configVal == null ? null : configVal.trim();
    }

    @Override
    public String toString() {
        return "ConfClusterHostGroupAppsConfig{" +
                "appConfigItemId='" + appConfigItemId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", appConfigClassification='" + appConfigClassification + '\'' +
                ", appName='" + appName + '\'' +
                ", configItem='" + configItem + '\'' +
                ", isDelete=" + isDelete +
                ", createdTime='" + createdTime + '\'' +
                ", createdby=" + createdby +
                ", configVal='" + configVal + '\'' +
                '}';
    }
}