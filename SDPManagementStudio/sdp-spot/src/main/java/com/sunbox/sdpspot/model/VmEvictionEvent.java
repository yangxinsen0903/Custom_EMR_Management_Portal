package com.sunbox.sdpspot.model;

import java.util.Date;

public class VmEvictionEvent {
    public static final int EXPIRES_60S = 60;
    public static final int EXPIRES_5MIN = 5 * 60;

    /**
     * 实例名称
     */
    private String vmName;

    private String hostname;

    /**
     * 计划逐出时间
     */
    private Date evictTime;

    private Date time;

    /**
     * 剩余多少秒
     */
    private Integer remaining;

    private Boolean deleted = false;

    public VmEvictionEvent() {
    }

    public static VmEvictionEvent withShutdown(String vmName, String hostname) {
        VmEvictionEvent event = new VmEvictionEvent();
        event.vmName = vmName;
        event.hostname = hostname;
        event.time = null;
        event.evictTime = null;
        event.remaining = 0;
        return event;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Date getEvictTime() {
        return evictTime;
    }

    public void setEvictTime(Date evictTime) {
        this.evictTime = evictTime;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "VmEvictionEvent{" +
                "vmName='" + vmName + '\'' +
                ", hostname='" + hostname + '\'' +
                ", evictTime=" + evictTime +
                ", time=" + time +
                ", remaining=" + remaining +
                ", deleted=" + deleted +
                '}';
    }
}
