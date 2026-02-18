package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;

public class AzureUpdateVirtualMachinesDiskSizeRequest {

    private String apiVersion;

    private String transactionId;

    private String clusterName;

    private List<String> virtualMachineNames;

    private Integer newOSDiskSizeGB;

    private Integer newDataDiskSizeGB;
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

    public List<String> getVirtualMachineNames() {
        return virtualMachineNames;
    }

    public void setVirtualMachineNames(List<String> virtualMachineNames) {
        this.virtualMachineNames = virtualMachineNames;
    }

    public Integer getNewOSDiskSizeGB() {
        return newOSDiskSizeGB;
    }

    public void setNewOSDiskSizeGB(Integer newOSDiskSizeGB) {
        this.newOSDiskSizeGB = newOSDiskSizeGB;
    }

    public Integer getNewDataDiskSizeGB() {
        return newDataDiskSizeGB;
    }

    public void setNewDataDiskSizeGB(Integer newDataDiskSizeGB) {
        this.newDataDiskSizeGB = newDataDiskSizeGB;
    }
}
