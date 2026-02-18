package com.sunbox.sdpcompose.model.azure.fleet.request;

import java.util.List;

public class BaseProfile {

    /**
     * 操作系统镜像ID
     */
    private String customOSImageId;

    /**
     * 数据盘数量
     */
    private int dataDiskCount;

    /**
     * 数据盘大小
     */
    private int dataDiskSizeGB;

    /**
     * 数据盘sku
     */
    private String dataDiskSku;

    /**
     * hostname前缀 = resourceGroupName
     */
    private String hostNameSuffix;

    /**
     * 市场镜像名称
     */
    private String marketplaceOSImageName;

    /**
     * 安全组资源Id
     */
    private String nsgResourceId;

    /**
     * 系统盘大小
     */
    private int osDiskSizeGB;

    /**
     * 系统盘Sku
     */
    private String osDiskSku;

    /**
     * 操作系统类型
     */
    private String osImageType;

    /**
     * 备用可用区
     */
    private String secondaryZone;

    /**
     *  存储需要灌注密钥的keyVault资源Id
     */
    private String sshKeyVaultId;

    /**
     *
     */
    private String sshPublicKey;

    /**
     * 公钥名称
     */
    private String sshPublicKeySecretName;

    /**
     * 公钥类型
     */
    private String sshPublicKeyType;

    /**
     * 启动脚本地址- 未启用
     */
    private String startupScriptBlobUrl;

    /**
     * 子网ID
     */
    private String subnetResourceId;

    /**
     * MI resourceId
     */
    private List<String> userAssignedIdentityResourceIds;

    /**
     * 操作系统 登陆用户名
     */
    private String userName;

    /**
     * 可用区
     */
    private String zone;

    public String getCustomOSImageId() {
        return customOSImageId;
    }

    public void setCustomOSImageId(String customOSImageId) {
        this.customOSImageId = customOSImageId;
    }

    public int getDataDiskCount() {
        return dataDiskCount;
    }

    public void setDataDiskCount(int dataDiskCount) {
        this.dataDiskCount = dataDiskCount;
    }

    public int getDataDiskSizeGB() {
        return dataDiskSizeGB;
    }

    public void setDataDiskSizeGB(int dataDiskSizeGB) {
        this.dataDiskSizeGB = dataDiskSizeGB;
    }

    public String getDataDiskSku() {
        return dataDiskSku;
    }

    public void setDataDiskSku(String dataDiskSku) {
        this.dataDiskSku = dataDiskSku;
    }

    public String getHostNameSuffix() {
        return hostNameSuffix;
    }

    public void setHostNameSuffix(String hostNameSuffix) {
        this.hostNameSuffix = hostNameSuffix;
    }

    public String getMarketplaceOSImageName() {
        return marketplaceOSImageName;
    }

    public void setMarketplaceOSImageName(String marketplaceOSImageName) {
        this.marketplaceOSImageName = marketplaceOSImageName;
    }

    public String getNsgResourceId() {
        return nsgResourceId;
    }

    public void setNsgResourceId(String nsgResourceId) {
        this.nsgResourceId = nsgResourceId;
    }

    public int getOsDiskSizeGB() {
        return osDiskSizeGB;
    }

    public void setOsDiskSizeGB(int osDiskSizeGB) {
        this.osDiskSizeGB = osDiskSizeGB;
    }

    public String getOsDiskSku() {
        return osDiskSku;
    }

    public void setOsDiskSku(String osDiskSku) {
        this.osDiskSku = osDiskSku;
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType;
    }

    public String getSecondaryZone() {
        return secondaryZone;
    }

    public void setSecondaryZone(String secondaryZone) {
        this.secondaryZone = secondaryZone;
    }

    public String getSshKeyVaultId() {
        return sshKeyVaultId;
    }

    public void setSshKeyVaultId(String sshKeyVaultId) {
        this.sshKeyVaultId = sshKeyVaultId;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getSshPublicKeySecretName() {
        return sshPublicKeySecretName;
    }

    public void setSshPublicKeySecretName(String sshPublicKeySecretName) {
        this.sshPublicKeySecretName = sshPublicKeySecretName;
    }

    public String getSshPublicKeyType() {
        return sshPublicKeyType;
    }

    public void setSshPublicKeyType(String sshPublicKeyType) {
        this.sshPublicKeyType = sshPublicKeyType;
    }

    public String getStartupScriptBlobUrl() {
        return startupScriptBlobUrl;
    }

    public void setStartupScriptBlobUrl(String startupScriptBlobUrl) {
        this.startupScriptBlobUrl = startupScriptBlobUrl;
    }

    public String getSubnetResourceId() {
        return subnetResourceId;
    }

    public void setSubnetResourceId(String subnetResourceId) {
        this.subnetResourceId = subnetResourceId;
    }

    public List<String> getUserAssignedIdentityResourceIds() {
        return userAssignedIdentityResourceIds;
    }

    public void setUserAssignedIdentityResourceIds(List<String> userAssignedIdentityResourceIds) {
        this.userAssignedIdentityResourceIds = userAssignedIdentityResourceIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "BaseProfile{" +
                "customOSImageId='" + customOSImageId + '\'' +
                ", dataDiskCount=" + dataDiskCount +
                ", dataDiskSizeGB=" + dataDiskSizeGB +
                ", dataDiskSku='" + dataDiskSku + '\'' +
                ", hostNameSuffix='" + hostNameSuffix + '\'' +
                ", marketplaceOSImageName='" + marketplaceOSImageName + '\'' +
                ", nsgResourceId='" + nsgResourceId + '\'' +
                ", osDiskSizeGB=" + osDiskSizeGB +
                ", osDiskSku='" + osDiskSku + '\'' +
                ", osImageType='" + osImageType + '\'' +
                ", secondaryZone='" + secondaryZone + '\'' +
                ", sshKeyVaultId='" + sshKeyVaultId + '\'' +
                ", sshPublicKey='" + sshPublicKey + '\'' +
                ", sshPublicKeySecretName='" + sshPublicKeySecretName + '\'' +
                ", sshPublicKeyType='" + sshPublicKeyType + '\'' +
                ", startupScriptBlobUrl='" + startupScriptBlobUrl + '\'' +
                ", subnetResourceId='" + subnetResourceId + '\'' +
                ", userAssignedIdentityResourceIds=" + userAssignedIdentityResourceIds +
                ", userName='" + userName + '\'' +
                ", zone='" + zone + '\'' +
                '}';
    }
}
