package com.sunbox.service.consts;

/**
 * @Description: componse constant 常量
 * @Title: ComposeConstant
 * @Package: com.sunbox.sdpcompose.consts
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/6 19:15
 */
public class AzureConstant {

    //region 元数据 枚举 metadataType 开始
    /**
     * VM_SKU
     */
    public static final String METADATA_VM_SKU = "/supportedVMSkuList";

    /**
     * DISK_SKU
     */
    public static final String METADATA_DISK_SKU = "/supportedDiskSkuList";

    /**
     * METADATA_SUBNET_SKU
     */
    public static final String METADATA_SUBNET_SKU = "/supportedSubnetList";

    /**
     * supportedNSGSkuList
     */
    public static final String METADATA_NSG_SKU = "/supportedNSGSkuList";

    /**
     * supportedSSHKeyPairList
     */
    public static final String METADATA_SSH_KEY_PAIR_SKU = "/supportedSSHKeyPairList";

    /**
     * 查询MI列表接口地址
     */
    public static final String METADATA_MANAGED_IDENTITY = "/supportedManagedIdentityList";

    /**
     *查询支持的Key Vault地址
     */
    public static final String METADATA_KEY_VAULT = "/supportedKVList";

    /**
     *查询支持的Key Vault地址
     */
    public static final String METADATA_SSH_KEY_PAIR_SKU_KVID = "/supportedSSHKeyPairList";

    /**
     *查询存储帐户列表地址
     */
    public static final String METADATA_STORAGE_ACCOUNT = "/supportedStorageAccountList";

    /**
     * 查询日志桶元数据根据id
     */
    public static final String METADATA_LOGS_BLOB_CONTAINER = "/supportedLogsBlobContainerList";
    /**
     * 查询虚拟网络地址
     */
    public static final String METADATA_NET_WORK = "/supportedNetworkList";


    /**
     * 根据id获取子网列表信息
     */
    public static final String METADATA_SUBNET_SKU_BY_ID = "/supportedSubnetList";
    /**
     * 查询数据中心地址
     */
    public static final String METADATA_REGION = "/supportedRegionList";
    /**
     * 查询订阅列表地址
     */
    public static final String SUBSCRIPTION = "/supportedSubscriptionList";

    //endregion 元数据 枚举 metadataType 结束


}
