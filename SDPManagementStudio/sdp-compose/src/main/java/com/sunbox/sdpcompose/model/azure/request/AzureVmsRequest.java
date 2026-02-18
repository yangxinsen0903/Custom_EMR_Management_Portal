package com.sunbox.sdpcompose.model.azure.request;

import java.util.HashMap;
import java.util.List;

public class AzureVmsRequest {
    /**
     * 地区
     */
    private String region;
    /**
     * 版本，v1
     */
    private String apiVersion;
    /**
     * 虚拟机名称索引，Azure从此索引开始创建
     */
    private String beginIndex;
    /**
     * 集群名
     */
    private String clusterName;
    /**
     * 虚拟机名称前缀，虚拟机名称不能重复
     */
    private String namePrefix;
    /**
     * 流水号，调用方生成
     */
    private String transactionId;
    /**
     * 申请开通虚机清单，undefined[ MASTER，CORE，TASK各有一个对象 ][ MASTER，CORE，TASK各有一个对象 ]
     */
    private List<VmGroups> virtualMachineGroups;

    private HashMap<String, String> clusterTags = new HashMap<>();

    public HashMap<String, String> getClusterTags() {
        return clusterTags;
    }

    public void setClusterTags(HashMap<String, String> clusterTags) {
        this.clusterTags = clusterTags;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(String beginIndex) {
        this.beginIndex = beginIndex;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<VmGroups> getVirtualMachineGroups() {
        return virtualMachineGroups;
    }

    public void setVirtualMachineGroups(List<VmGroups> virtualMachineGroups) {
        this.virtualMachineGroups = virtualMachineGroups;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "AzureVmsRequest{" +
                "region='" + region + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", beginIndex='" + beginIndex + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", namePrefix='" + namePrefix + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", virtualMachineGroups=" + virtualMachineGroups +
                ", clusterTags=" + clusterTags +
                '}';
    }
}