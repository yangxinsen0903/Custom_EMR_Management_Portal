package com.sunbox.domain;

import java.util.Date;

public class BaseImages {
    private String imgId;

    private String osImageId;

    private String osImageType;

    private String osVersion;

    private Date createdTime;

    private String createdby;

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId == null ? null : imgId.trim();
    }

    public String getOsImageId() {
        return osImageId;
    }

    public void setOsImageId(String osImageId) {
        this.osImageId = osImageId == null ? null : osImageId.trim();
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType == null ? null : osImageType.trim();
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion == null ? null : osVersion.trim();
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
        this.createdby = createdby == null ? null : createdby.trim();
    }
}