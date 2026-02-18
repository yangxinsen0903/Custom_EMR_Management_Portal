package com.sunbox.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 实例组SKU表(ConfHostGroupVmSku)实体类
 *
 * @author makejava
 * @since 2024-08-05 10:44:41
 */
@Data
public class ConfHostGroupVmSku implements Serializable {
    private static final long serialVersionUID = -23984569908651206L;
    /**
     * 实例组SKU ID
     */
    private String vmSkuId;
    /**
     * 集群ID
     */
    private String clusterId;
    /**
     * 实例组ID,conf_cluster_host_group表主键
     */
    private String groupId;
    /**
     * 实例组配置ID,conf_cluster_vm表主键
     */
    private String vmConfId;
    /**
     * 实例组名称
     */
    private String groupName;
    /**
     * 实例角色;Master Core Task
     */
    private String vmRole;
    /**
     * 实例规格
     */
    private String sku;
    /**
     * CPU类型:AMD64或Intel
     */
    private String cpuType;
    /**
     * CPU核数
     */
    private String vcpus;
    /**
     * 内存大小（GB）
     */
    private String memory;
    /**
     * 购买类型;1 按需  2 竞价
     */
    private Integer purchaseType;
    /**
     * 创建人
     */
    private String createdby;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 修改人
     */
    private String modifiedby;
    /**
     * 修改时间
     */
    private Date modifiedTime;


    //region 不参与数据库操作
    /**
     * 镜像ID
     */
    private String osImageid;
    //endregion
}

