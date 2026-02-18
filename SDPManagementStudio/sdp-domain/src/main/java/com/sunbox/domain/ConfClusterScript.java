package com.sunbox.domain;

import java.util.Date;

/**
    * 集群可执行脚本列表
    */
public class ConfClusterScript {
    /**
    * 脚本ID
    */
    private String confScriptId;

    /**
    * 集群ID
    */
    private String clusterId;

    /**
    * 脚本名称
    */
    private String scriptName;

    /**
    * 执行时机;aftervminit,beforestart,afterstart
    */
    private String runTiming;

    /**
    * 脚本路径
    */
    private String scriptPath;

    private Integer sortNo;

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    /**
    * 脚本参数
    */
    private String scriptParam;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
     * 文件名称
     */
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getConfScriptId() {
        return confScriptId;
    }

    public void setConfScriptId(String confScriptId) {
        this.confScriptId = confScriptId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getRunTiming() {
        return runTiming;
    }

    public void setRunTiming(String runTiming) {
        this.runTiming = runTiming;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getScriptParam() {
        return scriptParam;
    }

    public void setScriptParam(String scriptParam) {
        this.scriptParam = scriptParam;
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