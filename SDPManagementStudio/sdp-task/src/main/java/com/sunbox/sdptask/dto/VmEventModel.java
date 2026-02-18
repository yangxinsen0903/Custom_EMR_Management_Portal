/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdptask.dto;

import java.util.Date;

/**
 * @author wangda
 * @date 2024/7/17
 */
public class VmEventModel {

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 实例组名
     */
    private String groupName;

    /**
     * vm名称
     */
    private String vmName;

    /**
     * vm的HostName
     */
    private String hostName;

    /**
     * vmid
     */
    private String vmId;

    /**
     * 购买类型: Spot 或者 OnDemand
     */
    private String purchaseType;

    /**
     * 事件类型:ONLINE（上线）， OFFLINE（下线）
     */
    private String eventType;

    /**
     * 事件触发时间
     */
    private Date triggerTime;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }
}
