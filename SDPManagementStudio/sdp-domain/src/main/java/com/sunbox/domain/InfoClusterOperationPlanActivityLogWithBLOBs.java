package com.sunbox.domain;

public class InfoClusterOperationPlanActivityLogWithBLOBs extends InfoClusterOperationPlanActivityLog {
    private String paraminfo;

    private String logs;

    public String getParaminfo() {
        return paraminfo;
    }

    public void setParaminfo(String paraminfo) {
        this.paraminfo = paraminfo == null ? null : paraminfo.trim();
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        logs = logs == null ? null : logs.trim();
        if (logs != null && logs.length() > 64*1024) {
            logs = logs.substring(0, 64*0124);
        }
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "InfoClusterOperationPlanActivityLogWithBLOBs{" +
                "paraminfo='" + paraminfo + '\'' +
                ", logs='" + logs + '\'' +
                '}';
    }
}