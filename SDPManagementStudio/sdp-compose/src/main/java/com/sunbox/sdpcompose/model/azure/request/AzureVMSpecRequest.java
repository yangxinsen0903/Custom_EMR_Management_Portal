package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;
import java.util.Map;

public class AzureVMSpecRequest {

    private String skuName;

    private String osImageType;

    private String customOSImageId;

    private String marketplaceOSImageName;

    private String hostNameSuffix;

    private String userName;

    private String sshPublicKeyType;

    private String sshPublicKeySecretName;

    private String sshPublicKey;

    private String subnetResourceId;

    private String nsgResourceId;

    private String osDiskSku;

    private Integer osDiskSizeGB;

    private String dataDiskSku;

    private Integer dataDiskSizeGB;

    private Integer dataDiskCount;

    private String startupScriptBlobUrl;

    private String zone;

    /**
     * 跨区申请时的第二可用区
     */
    private String secondaryZone;

    private List<String> userAssignedIdentityResourceIds;

    private Map<String, String> virtualMachineTags;

    /**
     * 竞价实例配置
     */
    private AzureSpotProfile spotProfile;

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

    public Integer getOsDiskSizeGB() {
        return osDiskSizeGB;
    }

    public void setOsDiskSizeGB(Integer osDiskSizeGB) {
        this.osDiskSizeGB = osDiskSizeGB;
    }

    public String getDataDiskSku() {
        return dataDiskSku;
    }

    public void setDataDiskSku(String dataDiskSku) {
        this.dataDiskSku = dataDiskSku;
    }

    public Integer getDataDiskSizeGB() {
        return dataDiskSizeGB;
    }

    public void setDataDiskSizeGB(Integer dataDiskSizeGB) {
        this.dataDiskSizeGB = dataDiskSizeGB;
    }

    public Integer getDataDiskCount() {
        return dataDiskCount;
    }

    public void setDataDiskCount(Integer dataDiskCount) {
        this.dataDiskCount = dataDiskCount;
    }

    public String getStartupScriptBlobUrl() {
        return startupScriptBlobUrl;
    }

    public void setStartupScriptBlobUrl(String startupScriptBlobUrl) {
        this.startupScriptBlobUrl = startupScriptBlobUrl;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSecondaryZone() {
        return secondaryZone;
    }

    public void setSecondaryZone(String secondaryZone) {
        this.secondaryZone = secondaryZone;
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

    public AzureSpotProfile getSpotProfile() {
        return spotProfile;
    }

    public void setSpotProfile(AzureSpotProfile spotProfile) {
        this.spotProfile = spotProfile;
    }
}
