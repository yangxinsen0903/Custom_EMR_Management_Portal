package com.sunbox.sdpcompose.consts;

/**
 * @Description: componse constant 常量
 * @Title: ComposeConstant
 * @Package: com.sunbox.sdpcompose.consts
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/6 19:15
 */
public class ComposeConstant {

    /**元数据 枚举 metadataType 开始*/
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
     * 元数据 枚举 metadataType 结束
     */


    public static final String Task_ID = "taskId";

    public static final String Activity_Log_ID = "activityLogId";

    public static final String Cluster_ID = "clusterId";

    public static final String Plan_Id = "planId";

    public static final String DELETE_CLUSTER = "delete";

    public static final String compose_plan_wait_queue = "compose:task";
    public static final String compose_cluster_vmrole_list = "compose:cluster:vmrole:list";
}
