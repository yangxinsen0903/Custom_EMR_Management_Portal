package com.sunbox.domain;

import java.util.Date;

/**
    * 发行版本使用操作系统; sum(各个应用配置项数量）< 200
    */
public class BaseReleaseVmImg {
    /**
    * 发行版本号
    */
    private String releaseVersion;

    /**
    * 实例角色;ambari master core task
    */
    private String vmRole;

    /**
    * 镜像ID
    */
    private String osImageid;

    /**
    * 镜像类型;标准/自定义
    */
    private String osImageType;

    /**
    * 镜像内系统版本号
    */
    private String osVersion;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
     * 镜像ID 数据源来自 base_images 表
     */
    private String imgId;

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getOsImageid() {
        return osImageid;
    }

    public void setOsImageid(String osImageid) {
        this.osImageid = osImageid;
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
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