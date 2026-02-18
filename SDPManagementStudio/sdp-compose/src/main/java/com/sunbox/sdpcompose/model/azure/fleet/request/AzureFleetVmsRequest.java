package com.sunbox.sdpcompose.model.azure.fleet.request;

import java.util.List;
import java.util.Map;

/**
 * 请求Aure创建VM的对象
 */
public class AzureFleetVmsRequest {

    private String apiVersion;

    private String clusterName;

    private Map<String,String> clusterTags;

    private String region;

    private String requestTimestamp;

    private String transactionId;

    private List<AzureVMGroupRequest> virtualMachineGroups;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getClusterTags() {
        return clusterTags;
    }

    public void setClusterTags(Map<String, String> clusterTags) {
        this.clusterTags = clusterTags;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<AzureVMGroupRequest> getVirtualMachineGroups() {
        return virtualMachineGroups;
    }

    public void setVirtualMachineGroups(List<AzureVMGroupRequest> virtualMachineGroups) {
        this.virtualMachineGroups = virtualMachineGroups;
    }

    @Override
    public String toString() {
        return "AzureVmsRequest{" +
                "apiVersion='" + apiVersion + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clusterTags=" + clusterTags +
                ", region='" + region + '\'' +
                ", requestTimestamp='" + requestTimestamp + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", virtualMachineGroups=" + virtualMachineGroups +
                '}';
    }
}
