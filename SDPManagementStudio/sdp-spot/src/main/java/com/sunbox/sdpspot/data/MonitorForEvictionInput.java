package com.sunbox.sdpspot.data;

import java.util.Date;

public class MonitorForEvictionInput {
    /**
     * 主机名
     */
    private String vmName;

    /**
     * 是否要被逐出，true-要被逐出，false-不会被逐出
     */
    private Boolean evictFlag;

    /**
     * 逐出时间
     */
    private Date evictTime;

    private String accessToken;

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public Boolean getEvictFlag() {
        return evictFlag;
    }

    public void setEvictFlag(Boolean evictFlag) {
        this.evictFlag = evictFlag;
    }

    public Date getEvictTime() {
        return evictTime;
    }

    public void setEvictTime(Date evictTime) {
        this.evictTime = evictTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "MonitorForEvictionInput{" +
                "vmName='" + vmName + '\'' +
                ", evictFlag=" + evictFlag +
                ", evictTime=" + evictTime +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
