package com.sunbox.domain;

import java.util.Date;

public class BaseClusterOperationTemplate {
    private String templateId;

    private String releaseVersion;

    private String operationName;

    private String operationDescription;

    private Integer isDelete;

    private String createdby;

    private Date createdTime;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion == null ? null : releaseVersion.trim();
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName == null ? null : operationName.trim();
    }

    public String getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(String operationDescription) {
        this.operationDescription = operationDescription == null ? null : operationDescription.trim();
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
        return "BaseClusterOperationTemplate{" +
                "templateId='" + templateId + '\'' +
                ", releaseVersion='" + releaseVersion + '\'' +
                ", operationName='" + operationName + '\'' +
                ", operationDescription='" + operationDescription + '\'' +
                ", isDelete=" + isDelete +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}