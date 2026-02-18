package com.sunbox.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 集群实例组模式节点配置; 1000
 */
@Data
public class ConfClusterVm {
    /**
     * 实例配置ID
     */
    private String vmConfId;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 实例角色;Master Core Task
     */
    private String vmRole;

    /**
     * 实例组名称
     */
    private String groupName;

    /**
     * 实例组ID
     */
    private String groupId;

    /**
     * 弹性伸缩规则Id
     */
    private String elasticRuleId;

    /**
     * 实例规格
     */
    private String sku;

    /**
     * 镜像ID
     */
    private String osImageid;

    /**
     * 镜像类型;标准/自定义
     */
    private String osImageType;

    /**
     * 镜像内系统版本号
     */
    private String osVersion;

    /**
     * OS磁盘大小
     */
    private Integer osVolumeSize;

    /**
     * OS磁盘类型
     */
    private String osVolumeType;

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
     * 实例购买数量
     */
    private Integer count;

    /**
     * 购买类型;1 按需  2 竞价
     */
    private Integer purchaseType;

    public static final Integer PURCHASETYPE_ONDEMOND = 1;
    public static final Integer PURCHASETYPE_SPOT = 2;

    /**
     * 是否跨物理机申请主机
     * 1: VM_Standalone(不强制跨物理机申请虚拟机)
     * 2: VMSS_Flexible(跨物理机申请虚拟机)
     */
    private Integer provisionType;

    public static final Integer PROVISION_TYPE_VM_Standalone = 1;
    public static final Integer PROVISION_TYPE_VMSS_Flexible = 2;

    /**
     * 初始化脚本地址
     */
    private String initScriptPath;

    /**
     * 状态;0 待创建 1 创建中 2 运行中  -1释放中 -2 已释放
     */
    private Integer state;

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
     * 镜像ID，非resourceid
     */
    private String imgId;

    private Integer priceStrategy;

    private BigDecimal maxPrice;

    private Integer purchasePriority;
    /**
     * VM申请类型:(单VM:VM,多机型资源池:VM_POOL,实例队列:VM_FLEET)
     * 弃用原因:竞价实例不在区分单vm 和 多机型资源池,已弃用
     */
    @Deprecated
    private String groupVmType;
    /**
     * CPU与内存的比率
     * 1,用于多机型资源池(groupVmType=VM_POOL)时,记录CPU与内存的比率,已弃用
     * 2,弃用原因:多机型资源池需求改变,多机型资源池由前端传多个sku,不在由此字段计算符合规格的sku
     */
    @Deprecated
    private BigDecimal cpuMemoryRadio;
    /**
     * 竞价分配策略,LowestPrice:按最低价,CapacityOptimized:容量,PriceCapacityOptimized:容量和价格
     */
    private String spotAllocationStrategy;
    /**
     * 按需分配策略,LowestPrice:按最低价,Prioritized:按照指定的优先级
     */
    private String regularAllocationStrategy;



    private List<ConfClusterVmDataVolume>  vmDataVolumes;

    /**
     * 实例组状态常量 //状态;0 待创建 1 创建中 2 运行中  -1释放中 -2 已释放,-3已删除
     *
     * @return
     */
    public static final Integer STATE_WAIT_CREATE = 0;
    public static final Integer STATE_CREATING = 1;
    public static final Integer STATE_RUNNING = 2;
    public static final Integer STATE_RELEASING = -1;
    public static final Integer STATE_RELEASED = -2;
    public static final Integer STATE_DELETED = -3;
}