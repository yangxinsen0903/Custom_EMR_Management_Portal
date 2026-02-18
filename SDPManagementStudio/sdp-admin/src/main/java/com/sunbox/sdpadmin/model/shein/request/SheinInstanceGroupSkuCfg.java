package com.sunbox.sdpadmin.model.shein.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SheinInstanceGroupSkuCfg {

    @JsonProperty("insGpCnt")
    private Integer cnt;

    @JsonProperty("insType")
    private String skuName;

    @JsonProperty("volumeType")
    private String dataVolumeType;

    @JsonProperty("volumeSizeInGB")
    private Integer dataVolumeSize;

    @JsonProperty("dataDiskCnt")
    private Integer dataVolumeCount;

    // 购买类型（purchaseType） 1 按需（ondemond）, 2 竞价（spot）
    @JsonProperty("insMktType")
    private Integer purchaseType;

    // 竞价策略 1 按市场价百分比, 2 固定价
    private Integer priceStrategy;

    // 最高价格（maxPrice）
    @JsonProperty("priceStrategyValue")
    private BigDecimal maxPrice;

    // 优先级，默认1
    private Integer purchasePriority = 1;

    // 竞价类型
    // private Integer spotType;

    // 竞价价格
    // private BigDecimal spotPrice;

    private Integer enableBeforestartScript;

    private Integer enableAfterstartScript;

    // ======= 补充参数 =======

    private String vmRole;

    private String groupName;

    private Integer memoryGB;

    @JsonProperty("vCPUs")
    private Integer vCPUs;

    /**
     * 竞价分配策略,LowestPrice:按最低价,CapacityOptimized:容量,PriceCapacityOptimized:容量和价格
     */
    private String spotAllocationStrategy;
    /**
     * 按需分配策略,LowestPrice:按最低价,Prioritized:按照指定的优先级
     */
    private String regularAllocationStrategy;

}
