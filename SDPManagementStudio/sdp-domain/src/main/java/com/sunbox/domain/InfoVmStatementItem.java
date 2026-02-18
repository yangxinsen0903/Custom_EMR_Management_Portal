package com.sunbox.domain;

import java.util.Date;

public class InfoVmStatementItem {
    private Long id;
    private String statementId;
    private String vmName;
    private String vmState;
    private String nicId;
    private String nicState;
    private String privateIp;
    private String clusterId;
    private String clusterName;
    private String vmRole;
    private String groupName;
    private String hostName;
    private String purchaseType;
    private String sku;
    private Integer cpu;
    private Integer memory;
    private String zoneName;
    private String subnet;
    private String physicalZone;
    private String vmTags;
    private String vmSource;
    private Date vmCreatedTime;
    private Date createdTime;
    private Date modifiedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatementId() {
        return statementId;
    }

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmState() {
        return vmState;
    }

    public void setVmState(String vmState) {
        this.vmState = vmState;
    }

    public String getNicId() {
        return nicId;
    }

    public void setNicId(String nicId) {
        this.nicId = nicId;
    }

    public String getNicState() {
        return nicState;
    }

    public void setNicState(String nicState) {
        this.nicState = nicState;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getPhysicalZone() {
        return physicalZone;
    }

    public void setPhysicalZone(String physicalZone) {
        this.physicalZone = physicalZone;
    }

    public String getVmTags() {
        return vmTags;
    }

    public void setVmTags(String vmTags) {
        this.vmTags = vmTags;
    }

    public String getVmSource() {
        return vmSource;
    }

    public void setVmSource(String vmSource) {
        this.vmSource = vmSource;
    }

    public Date getVmCreatedTime() {
        return vmCreatedTime;
    }

    public void setVmCreatedTime(Date vmCreatedTime) {
        this.vmCreatedTime = vmCreatedTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
