package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;
import java.util.Map;

public class AzureVMInstanceRequest {

    private String clusterName;

    private String groupName;

    private String groupIndex;


    private String skuName;

    private String osImageType = "MarketplaceImage";

    private String customOSImageId;

    private String marketplaceOSImageName;

    private String hostNameSuffix;

    private String userName;

    private String sshPublicKeyType = "PlainText";

    private String sshPublicKeySecretName;

    private String sshPublicKey;

    private String subnetResourceId;

    private String nsgResourceId;

    private String osDiskSku;

    private int osDiskSizeGB;

    private String dataDiskSku;

    private int dataDiskSizeGB;

    private int dataDiskCount;

    private String startupScriptBlobUrl;

    private List<String> userAssignedIdentityResourceIds;

    private Map<String, String> virtualMachineTags;

    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(String groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType;
    }

    public String getCustomOSImageId() {
        return customOSImageId;
    }

    public void setCustomOSImageId(String customOSImageId) {
        this.customOSImageId = customOSImageId;
    }

    public String getMarketplaceOSImageName() {
        return marketplaceOSImageName;
    }

    public void setMarketplaceOSImageName(String marketplaceOSImageName) {
        this.marketplaceOSImageName = marketplaceOSImageName;
    }

    public String getHostNameSuffix() {
        return hostNameSuffix;
    }

    public void setHostNameSuffix(String hostNameSuffix) {
        this.hostNameSuffix = hostNameSuffix;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSshPublicKeyType() {
        return sshPublicKeyType;
    }

    public void setSshPublicKeyType(String sshPublicKeyType) {
        this.sshPublicKeyType = sshPublicKeyType;
    }

    public String getSshPublicKeySecretName() {
        return sshPublicKeySecretName;
    }

    public void setSshPublicKeySecretName(String sshPublicKeySecretName) {
        this.sshPublicKeySecretName = sshPublicKeySecretName;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getSubnetResourceId() {
        return subnetResourceId;
    }

    public void setSubnetResourceId(String subnetResourceId) {
        this.subnetResourceId = subnetResourceId;
    }

    public String getNsgResourceId() {
        return nsgResourceId;
    }

    public void setNsgResourceId(String nsgResourceId) {
        this.nsgResourceId = nsgResourceId;
    }

    public String getOsDiskSku() {
        return osDiskSku;
    }

    public void setOsDiskSku(String osDiskSku) {
        this.osDiskSku = osDiskSku;
    }

    public int getOsDiskSizeGB() {
        return osDiskSizeGB;
    }

    public void setOsDiskSizeGB(int osDiskSizeGB) {
        this.osDiskSizeGB = osDiskSizeGB;
    }

    public String getDataDiskSku() {
        return dataDiskSku;
    }

    public void setDataDiskSku(String dataDiskSku) {
        this.dataDiskSku = dataDiskSku;
    }

    public int getDataDiskSizeGB() {
        return dataDiskSizeGB;
    }

    public void setDataDiskSizeGB(int dataDiskSizeGB) {
        this.dataDiskSizeGB = dataDiskSizeGB;
    }

    public int getDataDiskCount() {
        return dataDiskCount;
    }

    public void setDataDiskCount(int dataDiskCount) {
        this.dataDiskCount = dataDiskCount;
    }

    public String getStartupScriptBlobUrl() {
        return startupScriptBlobUrl;
    }

    public void setStartupScriptBlobUrl(String startupScriptBlobUrl) {
        this.startupScriptBlobUrl = startupScriptBlobUrl;
    }

    public List<String> getUserAssignedIdentityResourceIds() {
        return userAssignedIdentityResourceIds;
    }

    public void setUserAssignedIdentityResourceIds(List<String> userAssignedIdentityResourceIds) {
        this.userAssignedIdentityResourceIds = userAssignedIdentityResourceIds;
    }

    public Map<String, String> getVirtualMachineTags() {
        return virtualMachineTags;
    }

    public void setVirtualMachineTags(Map<String, String> virtualMachineTags) {
        this.virtualMachineTags = virtualMachineTags;
    }
}