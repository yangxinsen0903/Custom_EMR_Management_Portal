package com.sunbox.sdpadmin.model.shein.request;

import java.io.Serializable;

/**
 * @Description: 启动脚本
 * @Title: ClusterScript
 * @Package: com.sunbox.sdpadmin.model.shein.request
 * @Author: wangshihao
 * @Copyright:
 * @CreateTime: 2022/12/20 17:18
 */
public class ClusterScript implements Serializable {

    private static final long serialVersionUID = 1L;

    private String runTiming;
    private String scriptName;
    private String scriptParam;
    private String scriptPath;
    private Integer sortNo;

    public String getRunTiming() {
        return runTiming;
    }

    public void setRunTiming(String runTiming) {
        this.runTiming = runTiming;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptParam() {
        return scriptParam;
    }

    public void setScriptParam(String scriptParam) {
        this.scriptParam = scriptParam;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
