package com.sunbox.domain;

import java.util.Date;

public class BaseImageScripts {
    private String imgScriptId;

    private String imgId;

    private String scriptName;

    private String runTiming;

    private String playbookUri;

    private String scriptFileUri;

    private String extraVars;

    private Integer sortNo;

    private String createdby;

    private Date createdTime;

    /**
     * 安装ambariserver
     */
    public static final String RUNTIMING_AMBARI_SERVER="install_ambari_server";

    /**
     * 安装ambariagent
     */
    public static final String RUNTIMING_AMBARI_AGENT="install_ambari_agent";

    /**
     * 安装TEZUI
     */
    public static final String RUNTIMING_TEZUI="install_tez_ui";

    /**
     * 收集日志
     */
    public static final String RUNTIMING_COLLECTLOG="collectLogs";

    /**
     * 磁盘扩容
     */
    public static final String RUNTIMING_DISKSCALEOUT="diskscaleout";

    /**
     * Decommionsion调整带宽
     */
    public static final String RUNTIMING_BANDWITDTH_ADJUEST="bandwidth";

    /**
     * 清理ganglia数据
     */
    public static final String RUNTIMING_CLEAER_GANGLIA_DATA="clear_ganglia_data";

    /**
     * 清理ambari历史
     */
    public static final String RUNTIMING_CLEAN_AMBARI_HISTORY="clean_ambari_history";
    public static final String RUNTIMING_SHUTDOWN_AMBAERI_AGENT = "shutdown_ambari_agent";
    public static final String RUNTIMING_COLLECT_CLUSTER_INFO = "collect_cluster_info";


    public String getImgScriptId() {
        return imgScriptId;
    }

    public void setImgScriptId(String imgScriptId) {
        this.imgScriptId = imgScriptId == null ? null : imgScriptId.trim();
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId == null ? null : imgId.trim();
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
}