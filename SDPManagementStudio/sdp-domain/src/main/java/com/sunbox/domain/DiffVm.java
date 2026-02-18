/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

/**
 * SDP与Yarn对比出来的差异VM
 * @author wangda
 * @date 2023/7/25
 */
public class DiffVm {

    /** ID */
    private String id;
    /** 统计时间 */
    private String statTime;

    /** 差异类型，见：SdpYarnDiffVm 枚举 */
    private String diffType;

    /** 集群ID */
    private String clusterId;
    /** 集群名 */
    private String clusterName;
    /** 实例组名称 */
    private String groupName;

    /** 主机角色  */
    private String vmRole;

    /** 虚机名 */
    private String vmName;
    /** 主机名 */
    private String hostName;
    /** IP地址 */
    private String ip;
    /** 购买类型 */
    private String purchaseType;
    /** sku */
    private String sku;
    /** cpu个数 */
    private String cpu;
    /** 内存大小 */
    private String memory;
    /** 当前状态 */
    private String currentState;

    public String toCSVLine() {
        return id + "," + statTime + "," + diffType + "," + clusterId + ","
                + clusterName + "," + groupName + "," + vmRole + "," + vmName + ","
                + hostName + "," + ip + "," + purchaseType + "," + sku + "," + cpu + ","
                + memory + "," + currentState;
    }

    public String toCSVHeader() {
        return "id,统计时间(statTime),差异类型(diffType),集群ID(clusterId),集群名称(clusterName),实例组名称(groupName),主机角色(vmRole),主机名(vmName),"
                + "HostName,IP地址(ip),购买类型(1:ondemaon; 2:spot),sku,cpu,memory,当前状态(currentState)";
    }
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatTime() {
        return statTime;
    }

    public void setStatTime(String statTime) {
        this.statTime = statTime;
    }

    public String getDiffType() {
        return diffType;
    }

    public void setDiffType(String diffType) {
        this.diffType = diffType;
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

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
