package com.sunbox.sdpadmin.model.admin.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.domain.enums.DataVolumeType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class InstanceGroupSkuCfg {
    private Integer cnt;
    private String vmRole;
    private String groupName;
    private Integer dataVolumeSize;
    private String dataVolumeType;

    // 购买类型 1 按需, 2 竞价
    private Integer purchaseType;
    private Integer spotType;
    private BigDecimal spotPrice;
    private BigDecimal memoryGB;
    private String cpuType;
    private Integer osVolumeSize;
    private String osVolumeType;
    private List<String> skuNames;
    //按需模式下,单个sku名称,为了兼容前端,返回数据的时候使用
    private String skuName;
    @JsonProperty("vCPUs")
    private Integer vCPUs;
    private String dataVolumeCount;
    // 实例组自定义参数
    private List<ClusterCfg> groupCfgs;
    private String clusterId;
    private String groupId;
    private ConfGroupElasticScalingData confGroupElasticScalingData;

    private Integer enableBeforestartScript;
    private Integer enableAfterstartScript;

    // 竞价策略 1 按市场价百分比, 2 固定价
    private Integer priceStrategy;

    // 最高价格
    private BigDecimal maxPrice;

    // 优先级，默认1
    private Integer purchasePriority;

    /**
     * 是否跨物理机申请主机
     * VM_Standalone: 不强制跨物理机申请虚拟机)
     * VMSS_Flexible: 跨物理机申请虚拟机)
     */
    private String provisionType;

    /** 镜像在SDP平台的ID */
    private String imgId;

    /** 镜像在Azure上的资源ID */
    private String osImageId;
    /**
     * 竞价分配策略,LowestPrice:按最低价,CapacityOptimized:容量,PriceCapacityOptimized:容量和价格
     */
    private String spotAllocationStrategy;
    /**
     * 按需分配策略,LowestPrice:按最低价,Prioritized:按照指定的优先级
     */
    private String regularAllocationStrategy;

    /**
     * OS盘是否使用了PV2磁盘
     * @return
     */
    public boolean isOSUsePv2DataVolume() {
        // PV2磁盘的Sku是：PremiumV2_LRS
        return StrUtil.equalsIgnoreCase(DataVolumeType.PremiumV2_LRS.name(), osVolumeType);
    }

    /**
     * 是否混用L系列机型与非L系列机型
     */
    public boolean isMixedLVmSku() {
        if (CollUtil.isEmpty(skuNames)) {
            return false;
        }

        boolean haveL = false;
        boolean haveOther = false;
        for (String name : skuNames) {
            if (StrUtil.contains(name, "_L")) {
                haveL = true;
            } else {
                haveOther = true;
            }
        }
        // 混用: 有L并且也有其它
        return haveL && haveOther;
    }
}
