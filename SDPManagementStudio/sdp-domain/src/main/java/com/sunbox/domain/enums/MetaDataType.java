package com.sunbox.domain.enums;

/**
 * 元数据类型
 */
public enum MetaDataType
{
    REGION("SupportedRegionList", "地区"),
    VM_SKU("SupportedVMSkuList", "虚拟机"),
    DISK_SKU("SupportedDiskSkuList", "磁盘Sku"),
    SUBNET("SupportedSubnetList", "子网"),
    NSG_SKU("SupportedNSGSkuList", "NSG Sku 安全组"),
    SSH_KEY("SupportedSSHKeyPairList", "SSH密钥对"),
    MANAGED_IDENTITY("SupportedManagedIdentityList", "身份管理"),
    LOGS_BLOB_CONTAINER("SupportedLogsBlobContainerList", "日志Blob容器"),
    AVAILABILITY_ZONE("SupportedAvailabilityZoneList", "可用区域"),
    KEY_VAULT("SupportedKeyVaultList", "KeyVault");

    private final String code;
    private final String info;

    MetaDataType(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
