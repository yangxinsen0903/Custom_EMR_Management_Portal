package com.sunbox.domain.azure;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  表名修改为了:azure_cleaned_vms_record
 * @author 
 */
@Data
public class AzureVmtraceInfo   {
    /**
     * ID
     */
    private Long id;

    /**
     * 实例名称
     */
    private String vmName;

    /**
     * 机器名称
     */
    private String hostName;

    /**
     * 唯一id
     */
    private String uniqueId;

    /**
     * private ip
     */
    private String privateIp;

    /**
     * 可用区
     */
    private String zone;

    /**
     * 优先级
     */
    private String priority;

    /**
     * vmsize
     */
    private String vmSize;

    /**
     * 集群id,有可能为空
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 伸缩实例组角色
     */
    private String vmRole;

    /**
     * 实例组ID
     */
    private String groupId;

    /**
     * 实例组名称
     */
    private String groupName;

    /**
     * vm创建时间
     */
    private Date vmCreatedTime;

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

    /**
     * 调用接口次数
     */
    private Integer invokeCount;

    /**
     * 删除虚拟机响应值
     */
    private String vmsDelResponse;

}