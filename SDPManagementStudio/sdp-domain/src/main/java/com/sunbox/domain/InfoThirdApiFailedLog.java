package com.sunbox.domain;

import java.util.Date;

public class InfoThirdApiFailedLog {
    private Long id;
    /** 调用接口资源所属Region */
    private String region;
    private String regionName;

    private String apiName;

    private String apiUrl;

    private Integer failedType;

    private Integer timeOut;

    private Date createdTime;


    public static Integer FAILED_TYPE_TIMEOUT=1;

    public static Integer FAILED_TYPE_EXCEPTION=2;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName == null ? null : apiName.trim();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl == null ? null : apiUrl.trim();
    }

    public Integer getFailedType() {
        return failedType;
    }

    public void setFailedType(Integer failedType) {
        this.failedType = failedType;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "InfoThirdApiFailedLog{" +
                "id=" + id +
                ", apiName='" + apiName + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", failedType=" + failedType +
                ", timeOut=" + timeOut +
                ", createdTime=" + createdTime +
                '}';
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}