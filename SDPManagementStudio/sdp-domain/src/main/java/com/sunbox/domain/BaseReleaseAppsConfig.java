package com.sunbox.domain;

import java.util.Date;

/**
    * 发行版本可用应用组件配置列表; sum(各个应用配置项数量）< 200
    */
public class BaseReleaseAppsConfig {
    /**
    * 发行版本号
    */
    private String releaseVersion;

    /**
    * 组件名称
    */
    private String appName;

    /**
    * 配置分类
    */
    private String appConfigClassification;

    /**
    * 配置文件
    */
    private String appConfigFile;

    /**
     * 显示排序
     */
    private Integer sortNo;

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

    public String getAppConfigClassification() {
        return appConfigClassification;
    }

    public void setAppConfigClassification(String appConfigClassification) {
        this.appConfigClassification = appConfigClassification;
    }

    public String getAppConfigFile() {
        return appConfigFile;
    }

    public void setAppConfigFile(String appConfigFile) {
        this.appConfigFile = appConfigFile;
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

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}