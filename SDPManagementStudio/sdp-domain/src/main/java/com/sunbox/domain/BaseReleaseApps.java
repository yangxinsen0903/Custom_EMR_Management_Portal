package com.sunbox.domain;

import java.util.Date;

/**
    * 发行版包含应用组件
    */
public class BaseReleaseApps {
    /**
    * 发行版本号
    */
    private String releaseVersion;

    /**
    * 应用组件名称
    */
    private String appName;

    /**
    * 应用组件版本
    */
    private String appVerison;

    /**
     * 显示排序
     */
    private Integer sortNo;

    /**
    * 是否必选
    */
    private Integer required;

    /**
    * 创建人
    */
    private String createdBy;

    /**
    * 创建时间
    */
    private Date createdTime;

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVerison() {
        return appVerison;
    }

    public void setAppVerison(String appVerison) {
        this.appVerison = appVerison;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}