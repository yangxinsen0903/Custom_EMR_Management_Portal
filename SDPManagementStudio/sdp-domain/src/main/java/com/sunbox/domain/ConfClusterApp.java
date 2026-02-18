package com.sunbox.domain;

import java.util.Date;

/**
    * 集群部署应用; 1*集群要部署应用数量
    */
public class ConfClusterApp {
    /**
    * 集群ID
    */
    private String clusterId;

    /**
    * 组件名称
    */
    private String appName;

    /**
    * 组件版本
    */
    private String appVersion;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

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

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
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
}