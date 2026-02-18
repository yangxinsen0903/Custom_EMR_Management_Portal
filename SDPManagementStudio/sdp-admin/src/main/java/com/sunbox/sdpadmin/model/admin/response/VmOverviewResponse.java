package com.sunbox.sdpadmin.model.admin.response;

import com.sunbox.domain.ConfGroupElasticScalingRule;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Data
public class VmOverviewResponse {

    /**
     * 允许设置
     */
    public static final Integer ALLOWED_SET = 1;
    /**
     * 不允许设置
     */
    public static final Integer NOT_ALLOWD_SET = 0;
    // 实例配置id
    private String vmConfId;

    // 实例组名称
    private String groupName;

    // 实例类型
    private String vmRole;

    // 实例状态
    private Integer state;

    // 节点数量
    private Integer vmCountByRole;

    // 规格
    private String sku;

    private List<String> skuNames;

    // 网络
    private String vnet;

    // 子网
    private String subnet;

    // 弹性扩缩容状态
    private Integer scaleState;
    private Integer dataVolumeSize;
    private Integer dataVolumeCount;
    private String dataVolumeType;
    private List<ConfGroupElasticScalingRule> scalingRules;
    private Integer purchaseType;
    private Integer expectCount;
    private Integer priceStrategy;
    private BigDecimal maxPrice;

    /** 竞价实例组状态。0 关闭竞价买入和缩容流程 1 关闭买入，开放缩容 2 开发买入，关闭缩容 3 开放买入和缩容 */
    private Integer spotState;

    /** 竞价买入开关状态。true: 开启。false: 关闭 */
    private Boolean spotBuyState = true;

    /** 竞价逐出开关状态。true: 开启。false: 关闭 */
    private Boolean spotDestoryState = true;

    /**
     * 全托管
     */
    private Integer isFullCustody;

    /**
     * 竞价分配策略,LowestPrice:按最低价,CapacityOptimized:容量,PriceCapacityOptimized:容量和价格
     */
    private String spotAllocationStrategy;
    /**
     * 按需分配策略,LowestPrice:按最低价,Prioritized:按照指定的优先级
     */
    private String regularAllocationStrategy;

    private String vcpus;

    /**
     * Pv2磁盘的IOPS
     */
    private Integer iops;
    /**
     * 吞吐量,单位(M)
     */
    private Integer throughput;



    public Boolean getSpotBuyState() {
        return spotBuyState;
    }

    public void setSpotBuyState(Boolean spotBuyState) {
        this.spotBuyState = spotBuyState;
    }

    public Boolean getSpotDestoryState() {
        return spotDestoryState;
    }

    public void setSpotDestoryState(Boolean spotDestoryState) {
        this.spotDestoryState = spotDestoryState;
    }

    public void setDataVolumeSize(Integer dataVolumeSize) {
        this.dataVolumeSize = dataVolumeSize;
    }

    public Integer getDataVolumeSize() {
        return dataVolumeSize;
    }

    public void setDataVolumeCount(Integer dataVolumeCount) {
        this.dataVolumeCount = dataVolumeCount;
    }

    public Integer getDataVolumeCount() {
        return dataVolumeCount;
    }

    public void setDataVolumeType(String dataVolumeType) {
        this.dataVolumeType = dataVolumeType;
    }

    public String getDataVolumeType() {
        return dataVolumeType;
    }

    public void setScalingRules(List<ConfGroupElasticScalingRule> scalingRules) {
        this.scalingRules = scalingRules;
    }

    public List<ConfGroupElasticScalingRule> getScalingRules() {
        return scalingRules;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Integer getPurchaseType() {
        return purchaseType;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public Integer getPriceStrategy() {
        return priceStrategy;
    }

    public void setPriceStrategy(Integer priceStrategy) {
        this.priceStrategy = priceStrategy;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setSpotState(Integer spotState) {
        this.spotState = spotState;
        if (Objects.isNull(spotState)) {
            return;
        }
        if (spotState == 0) {
            this.spotBuyState = false;
            this.spotDestoryState = false;
        } else if (spotState == 1) {
            this.spotBuyState = false;
            this.spotDestoryState = true;
        } else if (spotState == 2) {
            this.spotBuyState = true;
            this.spotDestoryState = false;
        } else if (spotState == 3) {
            this.spotBuyState = true;
            this.spotDestoryState = true;
        }
    }

    public Integer getSpotState() {
        return spotState;
    }
}
