package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;

public class AzureAddPartRequest {
    /**
     * 地区
     */
    private String region;
    private String subscriptionId;
    private String groupName;

    private String apiVersion;

    private String transactionId;

    private String clusterName;

    private List<String> vmNames;

    private Integer newDataDiskSizeGB;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getVmNames() {
        return vmNames;
    }

    public void setVmNames(List<String> vmNames) {
        this.vmNames = vmNames;
    }

    public Integer getNewDataDiskSizeGB() {
        return newDataDiskSizeGB;
    }

    public void setNewDataDiskSizeGB(Integer newDataDiskSizeGB) {
        this.newDataDiskSizeGB = newDataDiskSizeGB;
    }

    @Override
    public String toString() {
        return "AzureAddPartRequest{" +
                "region='" + region + '\'' +
                ", groupName='" + groupName + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", vmNames=" + vmNames +
                ", newDataDiskSizeGB=" + newDataDiskSizeGB +
                '}';
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
