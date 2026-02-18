package com.sunbox.domain;

import java.util.Date;

public class BaseClusterScript {
    private String confScriptId;

    private String releaseVersion;

    private String scriptName;

    private String runTiming;

    private String playbookUri;

    private String scriptFileUri;

    private String extraVars;

    private Integer sortNo;

    private String createdby;

    private Date createdTime;

    public String getConfScriptId() {
        return confScriptId;
    }

    public void setConfScriptId(String confScriptId) {
        this.confScriptId = confScriptId == null ? null : confScriptId.trim();
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion == null ? null : releaseVersion.trim();
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName == null ? null : scriptName.trim();
    }

    public String getRunTiming() {
        return runTiming;
    }

    public void setRunTiming(String runTiming) {
        this.runTiming = runTiming == null ? null : runTiming.trim();
    }

    public String getPlaybookUri() {
        return playbookUri;
    }

    public void setPlaybookUri(String playbookUri) {
        this.playbookUri = playbookUri == null ? null : playbookUri.trim();
    }

    public String getScriptFileUri() {
        return scriptFileUri;
    }

    public void setScriptFileUri(String scriptFileUri) {
        this.scriptFileUri = scriptFileUri == null ? null : scriptFileUri.trim();
    }

    public String getExtraVars() {
        return extraVars;
    }

    public void setExtraVars(String extraVars) {
        this.extraVars = extraVars == null ? null : extraVars.trim();
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
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
        return "BaseClusterScript{" +
                "confScriptId='" + confScriptId + '\'' +
                ", releaseVersion='" + releaseVersion + '\'' +
                ", scriptName='" + scriptName + '\'' +
                ", runTiming='" + runTiming + '\'' +
                ", playbookUri='" + playbookUri + '\'' +
                ", scriptFileUri='" + scriptFileUri + '\'' +
                ", extraVars='" + extraVars + '\'' +
                ", sortNo=" + sortNo +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}