/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import java.util.Date;

/**
 * Azure Fleet自动补齐被驱逐的VM
 * @author wangda
 * @date 2024/7/4
 */
public class AutoCreatedEvictVm {
    /** 自增ID' */
    private Long  id;
    /** 集群ID' */
    private String   clusterId;
    /** '集群名称' */
    private String  clusterName;
    /** 主机角色' */
    private String  vmRole;
    /** 实例组名称' */
    private String  groupName;
    /** 主机名' */
    private String   vmName;
    /** vmId' */
    private String   vmid;
    /** '购买类型: '*/
    private String   purchaseType;
    /** 状态' */
    private String   state;
    /** 事件详情' */
    private String   eventContent;
    /** 创建时间' */
    private Date   createTime;

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

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
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

    public String getVmid() {
        return vmid;
    }

    public void setVmid(String vmid) {
        this.vmid = vmid;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEventContent() {
        return eventContent;
    }

    public void setEventContent(String eventContent) {
        this.eventContent = eventContent;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
