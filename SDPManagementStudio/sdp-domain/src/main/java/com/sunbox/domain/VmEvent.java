/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import java.util.Date;

/**
 * VM上下线事件表
 * @author wangda
 * @date 2024/7/14
 */
public class VmEvent {
    /**
     * ID， 不自增
     */
    private Long id;

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

    /**
     * 备注说明
     */
    private String remark;

    /**
     * INIT（初始化）， PROCESSING（处理中）， SUCCESS（成功）， FAIL（失败）
     */
    private String state;

    /**
     * 处理完成时间
     */
    private Date finishTime;

    /**
     * 不参与数据操作
     */
    private String region;
    private String regionName;

    // 购买类型
    public static final String PURCHASE_TYPE_ONDEMAND= "OnDemand";
    public static final String PURCHASE_TYPE_SPOT = "Spot";

    // 状态
    public static final String STATE_INIT = "INIT";
    public static final String STATE_PROCESSING = "PROCESSING";
    public static final String STATE_SUCCESS = "SUCCESS";
    public static final String STATE_FAIL = "FAIL";

    // EventType
    public static final String EVENT_TYPE_ONLINE = "ONLINE";
    public static final String EVENT_TYPE_OFFLINE = "OFFLINE";

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
