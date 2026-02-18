package com.sunbox.domain;

import java.util.Date;

/**
    * 集群应用组件配置; sum(各个应用配置项数量）< 200
    */
public class ConfClusterAppsConfig {
    /**
    * 组件配置项
    */
    private String appConfigItemId;

    /**
    * 集群ID
    */
    private String clusterId;

    /**
    * 组件名称
    */
    private String appName;

    /**
    * 配置分类
    */
    private String appConfigClassification;

    /**
    * 配置项
    */
    private String configItem;

    /**
    * 配置项value
    */
    private String configVal;

    /**
    * 是否删除;0 无效 1 有效
    */
    private Integer isDelete;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    public String getAppConfigItemId() {
        return appConfigItemId;
    }

    public void setAppConfigItemId(String appConfigItemId) {
        this.appConfigItemId = appConfigItemId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppConfigClassification() {
        return appConfigClassification;
    }

    public void setAppConfigClassification(String appConfigClassification) {
        this.appConfigClassification = appConfigClassification;
    }

    public String getConfigItem() {
        return configItem;
    }

    public void setConfigItem(String configItem) {
        this.configItem = configItem;
    }

    public String getConfigVal() {
        return configVal;
    }

    public void setConfigVal(String configVal) {
        this.configVal = configVal;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "ConfClusterAppsConfig{" +
                "appConfigItemId='" + appConfigItemId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", appName='" + appName + '\'' +
                ", appConfigClassification='" + appConfigClassification + '\'' +
                ", configItem='" + configItem + '\'' +
                ", configVal='" + configVal + '\'' +
                ", isDelete=" + isDelete +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}