package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;

public class AzureAppendVMsRequest {

    private String apiVersion;

    private String transactionId;

    private String clusterName;

    private List<AzureVMGroupsRequest> virtualMachineGroups;

    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public List<AzureVMGroupsRequest> getVirtualMachineGroups() {
        return virtualMachineGroups;
    }

    public void setVirtualMachineGroups(List<AzureVMGroupsRequest> virtualMachineGroups) {
        this.virtualMachineGroups = virtualMachineGroups;
    }
}
