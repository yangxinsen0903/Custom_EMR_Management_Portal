package com.sunbox.sdpcompose.model.azure.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请开通主机规格
 */
public class SpecClass {

    /**
     * 数据盘信息
     */
    private List<DataDiskElement> dataDisks;

    /**
     * 主机名，将主机的hostName设置为此值
     */
    private String hostNameSuffix;

    /**
     * 系盘信息
     */
    private OSDiskClass osDisk;

    /**
     * 自定义镜像ID，自定义镜像时：Azure DiskImage资源Id
     * osImageType CustomImage 类型
     */
    private String customOSImageId;

    /**
     * 自定义镜像ID，自定义镜像时：Azure DiskImage资源Id
     * osImageType MarketplaceImage 类型
     */
    private String marketplaceOSImageName;

    /**
     * 镜像类型，标准 或自定义
     */
    private String osImageType;

    /**
     * 标准镜像版本名，标准镜像时：Ubuntu 1804
     */
    private String osImageVersion;

    /**
     * 安全组
     */
    private String nsgResourceId;

    /**
     * sshPublicKeyType: PlainText/KeyVaultSecret 二选一<br/>
     * PlainText:按照明文传输 <br/>
     * KeyVaultSecret:设置SecretName
     */
    private String sshPublicKeyType;

    private String sshPublicKeySecretName;
    // Ssh Key 所有的Keyvault的资源ID. SDP2.0升级后, 此字段需要SDP提供
    private String sshKeyVaultId;
    private String sshPublicKeySecretResourceId;

    /**
     * 公钥，客户公钥，用于客户跳板机免密登录
     */
    private String sshPublicKey;

    /**
     * 子网名，全局唯
     */
    private String subnetResourceId;

    /**
     * 可用区
     */
    private String zone;

    /**
     * 跨区申请时的第二可用区
     */
    private String secondaryZone;

    /**
     * 标签
     */
    private Map virtualMachineTags = new HashMap();

    /**
     * 竞价实例配置
     */
    private AzureSpotProfile spotProfile;

    /**
     * 默认用户名
     */
    private String userName;

    /**
     * 虚拟机SkuName
     */
    private String skuName;

    /**
     * 系统盘SKU
     */
    private String osDiskSku;

    /**
     * 系统盘大小
     */
    private Integer osDiskSizeGB;

    /**
     * 数据盘sku
     */
    private String dataDiskSku;

    /**
     * 数据盘大小
     */
    private Integer dataDiskSizeGB;

    /**
     * 数据盘数量
     */
    private Integer dataDiskCount;

    /**
     * 初始化脚本
     */
    // private String startupScriptBlobUrl = "https://sasdpscriptstmp.blob.core.windows.net/scripts/bootstrap_test.sh";
    // 不需要初始化脚本
    private String startupScriptBlobUrl;

    public String getSshKeyVaultId() {
        return sshKeyVaultId;
    }

    public void setSshKeyVaultId(String sshKeyVaultId) {
        this.sshKeyVaultId = sshKeyVaultId;
    }

    public String getSshPublicKeySecretResourceId() {
        return sshPublicKeySecretResourceId;
    }

    public void setSshPublicKeySecretResourceId(String sshPublicKeySecretResourceId) {
        this.sshPublicKeySecretResourceId = sshPublicKeySecretResourceId;
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

    /**
     * 向VM挂的MI（托管标识） ResourceId
     */
    private List<String> userAssignedIdentityResourceIds = new ArrayList<>();

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

    public List<DataDiskElement> getDataDisks() {
        return dataDisks;
    }

    public void setDataDisks(List<DataDiskElement> dataDisks) {
        this.dataDisks = dataDisks;
    }

    public String getHostNameSuffix() {
        return hostNameSuffix;
    }

    public void setHostNameSuffix(String hostNameSuffix) {
        this.hostNameSuffix = hostNameSuffix;
    }

    public OSDiskClass getOsDisk() {
        return osDisk;
    }

    public void setOsDisk(OSDiskClass osDisk) {
        this.osDisk = osDisk;
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

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType;
    }

    public String getOsImageVersion() {
        return osImageVersion;
    }

    public void setOsImageVersion(String osImageVersion) {
        this.osImageVersion = osImageVersion;
    }

    public String getNsgResourceId() {
        return nsgResourceId;
    }

    public void setNsgResourceId(String nsgResourceId) {
        this.nsgResourceId = nsgResourceId;
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

    public Map getVirtualMachineTags() {
        return virtualMachineTags;
    }

    public void setVirtualMachineTags(Map virtualMachineTags) {
        this.virtualMachineTags = virtualMachineTags;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public List<String> getUserAssignedIdentityResourceIds() {
        return userAssignedIdentityResourceIds;
    }

    public void setUserAssignedIdentityResourceIds(List<String> userAssignedIdentityResourceIds) {
        this.userAssignedIdentityResourceIds = userAssignedIdentityResourceIds;
    }

    public AzureSpotProfile getSpotProfile() {
        return spotProfile;
    }

    public void setSpotProfile(AzureSpotProfile spotProfile) {
        this.spotProfile = spotProfile;
    }

    @Override
    public String toString() {
        return "SpecClass{" +
                "dataDisks=" + dataDisks +
                ", hostNameSuffix='" + hostNameSuffix + '\'' +
                ", sshPublicKeyType='" + sshPublicKeyType + '\'' +
                ", sshPublicKeySecretName='" + sshPublicKeySecretName + '\'' +
                ", osDisk=" + osDisk +
                ", osImageId='" + customOSImageId + '\'' +
                ", osImageType='" + osImageType + '\'' +
                ", osImageVersion='" + osImageVersion + '\'' +
                ", securityGroup='" + nsgResourceId + '\'' +
                ", sshPublicKey='" + sshPublicKey + '\'' +
                ", subnetName='" + subnetResourceId + '\'' +
                ", tags=" + virtualMachineTags +
                ", userName='" + userName + '\'' +
                ", vmSkuName='" + skuName + '\'' +
                '}';
    }
}