package com.sunbox.constant;

/**
 * 元数据常量信息
 */
public class MetaDataConstants {
    /**
     * 类型 元数据类型
     * SupportedRegionList, SupportedVMSkuList,
     * SupportedDiskSkuList, SupportedSubnetList, SupportedNSGSkuList,
     * SupportedSSHKeyPairList, SupportedManagedIdentityList,
     * SupportedLogsBlobContainerList, SupportedAvailabilityZoneList,SupportedKeyVaultList
     */
    public static final String TYPE = "type";

    public static final String REMARK = "remark";

    public static final String VERSION = "version";

    //region 数据中心

    /**
     * 地域
     */
    public static final String REGION = "region";
    /**
     * 地域名称
     */
    public static final String REGION_NAME = "regionName";
    /**
     * key
     */
    public static final String REGION_KEY = "key";
    /**
     * 显示名称
     */
    public static final String REGION_DISPLAY_NAME = "displayName";
    /**
     * 物理位置
     */
    public static final String REGION_PHYSICAL_LOCATION = "physicalLocation";
    /**
     * 订阅id
     */
    public static final String SUBSCRIPTION_ID="subscriptionId";
    /**
     * 订阅name
     */
    public static final String SUBSCRIPTION_NAME="subscriptionName";


    //endregion

    //region 可用区

    /**
     * 可用区名称
     */
    public static final String AVAILABILITY_ZONE = "availabilityZone";

    /**
     * 逻辑可用区编号
     */
    public static final String LOGICAL_ZONE = "logicalZone";

    /**
     * 物理可用区编号
     */
    public static final String PHYSICAL_ZONE = "physicalZone";

    //endregion

    //region 子网
    /**
     * 子网资源id
     */
    public static final String SUBNET_ID = "subnetId";
    /**
     * 子网资源名称
     */
    public static final String SUBNET_NAME = "subnetName";
    /**
     * 子网业务名称
     */
    public static final String SUBNET_DISPLAY_NAME = "subnetDisplayName";

    /**
     * 虚拟网络资源id
     */
    public static final String SUBNET_VIRTUAL_NETWORK_RESOURCE_ID = "virtualNetworkResourceId";
    /**
     * 虚拟网络资源Name
     */
    public static final String SUBNET_VIRTUAL_NETWORK_NAME = "virtualNetworkName";

    //endregion

    //region 安全组
    /**
     * 安全组资源id
     */
    public static final String SECURITY_GROUP_ID = "resourceId";
    /**
     * 安全组资源名称
     */
    public static final String SECURITY_GROUP_RESOURCE_NAME = "name";
    /**
     * 安全组业务名称
     */
    public static final String SECURITY_GROUP_NAME = "securityGroupName";

    //endregion

    //region 机型Sku
    /**
     * 机型名称
     */
    public static final String VM_NAME = "name";
    /**
     * 此特定 SKU 的系列
     */
    public static final String VM_FAMILY = "family";
    /**
     * CPU核数
     */
    public static final String VM_CORE_COUNT = "vCoreCount";

    /**
     * 内存数(GB)
     */
    public static final String VM_MEMORY_GB = "memoryGB";
    /**
     * 最多支持磁盘数量
     */
    public static final String VM_MAX_DATA_DISKS_COUNT = "maxDataDisksCount";
    /**
     * CPU类型
     */
    public static final String VM_CPU_TYPE = "cpuType";

    public static final String TEMP_SSD_STORAGE_GB = "tempSSDStorageGB";
    public static final String TEMP_NVME_STORAGE_GB = "tempNVMeStorageGB";
    public static final String TEMP_NVME_DISKS_COUNT = "tempNVMeDisksCount";
    public static final String TEMP_NVME_DISK_SIZE_GB = "tempNVMeDiskSizeGB";
    //endregion


    //region 磁盘SKU
    /**
     * 磁盘SKU名称
     */
    public static final String DISK_NAME = "name";
    /**
     * 最大大小（单位G）
     */
    public static final String DISK_MAX_SIZE = "maxSize";
    /**
     * 最大吞吐量（MB/s）
     */
    public static final String DISK_MAX_THROUGHPUT = "maxThroughput";
    /**
     * 最大IOPS
     */
    public static final String DISK_MAX_IOPS = "maxIOPS";
    //endregion


    //region sh 密钥对
    /**
     * ssh 密钥对业务名称
     */
    public static final String SSH_KEY_PAIR_NAME = "name";
    /**
     * KeyVault的资源id
     */
    public static final String SSH_KEY_VAULT_RESOURCE_ID = "keyVaultResourceId";
    /**
     * KeyVault的资源名称
     */
    public static final String SSH_KEY_VAULT_RESOURCE_NAME = "keyVaultResourceName";
    /**
     * 密钥在KeyVault的的名称
     */
    public static final String PRIVATE_KEY_SECRET_NAME = "nameInKeyVault";
    /**
     * 密钥的资源ID
     */
    public static final String PUBLIC_KEY_SECRET_NAME = "secretResourceId";
    /**
     * 密钥类型
     */
    public static final String KEY_TYPE = "keyType";

    //endregion


    //region 托管标识
    /**
     * 托管标识业务名称
     */
    public static final String MI_NAME = "miName";

    /**
     * Tenant ID
     */
    public static final String MI_TENANT_ID = "tenantId";
    /**
     * Client Id
     */
    public static final String MI_CLIENT_ID = "clientId";
    /**
     * 资源ID
     */
    public static final String MI_RESOURCE_ID = "resourceId";
    /**
     * 资源
     */
    public static final String MI_RESOURCE_NAME = "name";

    public static final String MI_PRINCIPAL_ID = "principalId";

    //endregion


    //region 日志桶
    /**
     * 日志桶业务名称
     */
    public static final String LOG_NAME = "logName";

    /**
     * 存储账户名称
     */
    public static final String LOG_STORAGE_ACCOUNT = "storageAccountName";
    /**
     * 存储账户id
     */
    public static final String LOG_STORAGE_ACCOUNT_RESOURCE_ID = "resourceId";
    /**
     * 日志桶
     */
    public static final String LOG_BLOB_CONTAINER_NAME = "name";
    /**
     * 日志桶的URL地址
     */
    public static final String LOG_BLOB_CONTAINER_URL = "blobContainerUrl";
    //endregion

    //region keyVault
    /**
     * key
     */
    public static final String KV_ENDPOINT= "endpoint";
    /**
     * 业务名称
     */
    public static final String KV_NAME="name";

    /**
     * keyVault名称
     */
    public static final String KEY_VAULT_NAME="keyVaultName";

    public static final String KV_RESOURCE_ID="resourceId";
    //endregion
}
