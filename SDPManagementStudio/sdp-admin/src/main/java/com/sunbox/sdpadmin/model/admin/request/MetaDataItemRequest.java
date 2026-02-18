// package com.sunbox.sdpadmin.model.admin.request;
//
// import lombok.Data;
//
// import javax.validation.constraints.NotEmpty;
// import java.io.Serializable;
// import java.util.Date;
//
// /**
//  * 元数据
//  */
//
// @Data
// public class MetaDataItemRequest implements Serializable {
//     private static final long serialVersionUID = 1L;
//
//     private Long id;
//     /**
//      * 数据中心
//      */
//     private String region;
//     /**
//      * 数据中心名称
//      */
//     private String regionName;
//     /**
//      * 类型 元数据类型 - SupportedRegionList, SupportedVMSkuList, SupportedDiskSkuList, SupportedSubnetList, SupportedNSGSkuList, SupportedSSHKeyPairList, SupportedManagedIdentityList, SupportedLogsBlobContainerList, SupportedAvailabilityZoneList
//      */
//     private String type;
//     /**
//      * 版本
//      */
//     private String version;
//     /**
//      * 备注
//      */
//     private String remark;
//
//     //可用区
//     /**
//      * 可用区名称
//      */
//     private String availabilityZone;
//
//     /**
//      * 逻辑可用区编号
//      */
//     private Integer logicalZone;
//     /**
//      * 物理可用区编号
//      */
//     private Integer physicalZone;
//
//     //子网
//     /**
//      * 子网资源id
//      */
//     private String subnetId;
//     /**
//      * 子网资源名称
//      */
//     private String subnetName;
//
//     //安全组
//     /**
//      * 安全组id
//      */
//     private String securityGroupId;
//     /**
//      * 安全组名称
//      */
//     private String securityGroupName;
//
//     //机型sku
//     /**
//      * 机型名称
//      */
//     private String vmName;
//     /**
//      * 此特定 SKU 的系列
//      */
//     private String vmFamily;
//     /**
//      * CPU核数
//      */
//     private String vmCoreCount;
//
//     /**
//      * 内存数(GB)
//      */
//     private Integer vmMemoryGB;
//     /**
//      * 最多支持磁盘数量
//      */
//     private Integer vmMaxDataDisksCount;
//     /**
//      * CPU类型
//      */
//     private String vmCpuType;
//
//     private String tempSSDStorageGB;
//     private String tempNVMeStorageGB;
//     private String tempNVMeDisksCount;
//     private String tempNVMeDiskSizeGB;
//
//     //磁盘sku
//     /**
//      * 磁盘SKU名称
//      */
//     private String diskName;
//     /**
//      * 最大大小（单位G）
//      */
//     private Integer diskMaxSize;
//     /**
//      * 最大吞吐量（MB/s）
//      */
//     private Integer diskMaxThroughput;
//     /**
//      * 最大IOPS
//      */
//     private Integer diskMaxIOPS;
//
//
//     //ssh 密钥对
//     /**
//      * ssh 密钥对名称
//      */
//     private String sshKeyPairName;
//     /**
//      * KeyVault的资源
//      */
//     private String sshKeyVaultResourceId;
//     /**
//      * KeyVault的资源名称
//      */
//     private String sshKeyVaultResourceName;
//     /**
//      * 私钥的名称
//      */
//     private String privateKeySecretName;
//     /**
//      * 公钥的名称
//      */
//     private String publicKeySecretName;
//
//     //托管标识管理
//     /**
//      * 托管标识名称
//      */
//     private String miName;
//     /**
//      * Tenant ID
//      */
//     private String miTenantId;
//     /**
//      * Client Id
//      */
//     private String miClientId;
//     /**
//      * 资源ID
//      */
//     private String miResourceId;
//
//     //日志桶容器
//     /**
//      * 日志桶名称
//      */
//     private String logName;
//
//     /**
//      * 日志桶的URL地址
//      */
//     private String blobContainerUrl;
//
//
//
// }