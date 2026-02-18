package com.sunbox.domain.azure;

import java.util.List;

public class AzureDeleteVMsRequest {

    private String apiVersion;

    private String transactionId;

    private String clusterName;

    private List<String> vmNames;

    private List<String> dnsNames;

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

    public List<String> getVmNames() {
        return vmNames;
    }

    public void setVmNames(List<String> vmNames) {
        this.vmNames = vmNames;
    }

    public List<String> getDnsNames() {
        return dnsNames;
    }

    public void setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
    }

    @Override
    public String toString() {
        return "AzureDeleteVMsRequest{" +
                "apiVersion='" + apiVersion + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", vmNames=" + vmNames +
                ", region='" + region + '\'' +
                '}';
    }
}
