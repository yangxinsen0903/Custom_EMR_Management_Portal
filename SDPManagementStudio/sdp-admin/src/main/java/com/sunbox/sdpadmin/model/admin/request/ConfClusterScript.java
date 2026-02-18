package com.sunbox.sdpadmin.model.admin.request;

public class ConfClusterScript {
    private String runTiming;
    private String scriptName;
    private String scriptParam;
    private String scriptPath;
    private Integer sortNo;

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getRunTiming() { return runTiming; }
    public void setRunTiming(String value) { this.runTiming = value; }

    public String getScriptName() { return scriptName; }
    public void setScriptName(String value) { this.scriptName = value; }

    public String getScriptParam() { return scriptParam; }
    public void setScriptParam(String value) { this.scriptParam = value; }

    public String getScriptPath() { return scriptPath; }
    public void setScriptPath(String value) { this.scriptPath = value; }
}