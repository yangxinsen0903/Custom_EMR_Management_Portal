package com.sunbox.sdpadmin.model.shein.request;

import com.sunbox.sdpadmin.model.shein.response.ScalingConstraint;

import java.math.BigDecimal;
import java.util.List;

public class InstanceGroupAddConfig {
    private Integer insGpCnt;

    private String insGpRole;

    private String insGpName;

    private String insGroupId;

    /**
     * 购买类型（purchaseType） 1 按需（ondemond）, 2 竞价（spot）
     * EMR ondemond/spot
     */
    private String insMktType;

    private ScalingConstraint scalingConstraint;

    private List<SheinElasticScalingRuleData> scalingRules;

    private String vCpus;

    private String memory;

    private Integer rootVolSize;

    private String rootVolType;

    /**
     * EMR
     * skuName
     */
    private String insType;

    private long localSsdCnt;

    /**
     * scsi|nvme
     */
    private String localSsdInterface;

    /**
     * only available when io1
     */
    private long volumeIOps;

    private Integer volumeSizeInGB;

    /**
     * 数据盘类型
     */
    private String volumeType;

    private Integer dataDiskCnt;

    // 竞价策略 1 按市场价百分比, 2 固定价
    private Integer priceStrategy;

    // 最高价格（maxPrice）
    private BigDecimal priceStrategyValue;

    // 优先级，默认1
    private Integer purchasePriority = 1;

    /**
     * 是否跨物理机申请主机
     * 1: VM_Standalone(不强制跨物理机申请虚拟机)
     * 2: VMSS_Flexible(跨物理机申请虚拟机)
     */
    private Integer provisionType;

    /**
     * 竞价分配策略,LowestPrice:按最低价,CapacityOptimized:容量,PriceCapacityOptimized:容量和价格
     */
    private String spotAllocationStrategy;
    /**
     * 按需分配策略,LowestPrice:按最低价,Prioritized:按照指定的优先级
     */
    private String regularAllocationStrategy;

    public String getInsGroupId() {
        return insGroupId;
    }

    public void setInsGroupId(String insGroupId) {
        this.insGroupId = insGroupId;
    }

    public String getInsGpName() {
        return insGpName;
    }

    public void setInsGpName(String insGpName) {
        this.insGpName = insGpName;
    }

    public String getvCpus() {
        return vCpus;
    }

    public void setvCpus(String vCpus) {
        this.vCpus = vCpus;
    }

    public Integer getRootVolSize() {
        return rootVolSize;
    }

    public void setRootVolSize(Integer rootVolSize) {
        this.rootVolSize = rootVolSize;
    }

    public String getRootVolType() {
        return rootVolType;
    }

    public void setRootVolType(String rootVolType) {
        this.rootVolType = rootVolType;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Integer getDataDiskCnt() {
        return dataDiskCnt;
    }

    public void setDataDiskCnt(Integer dataDiskCnt) {
        this.dataDiskCnt = dataDiskCnt;
    }

    public Integer getInsGpCnt() {
        return insGpCnt;
    }

    public void setInsGpCnt(Integer value) {
        this.insGpCnt = value;
    }

    public String getInsGpRole() {
        return insGpRole;
    }

    public void setInsGpRole(String value) {
        this.insGpRole = value;
    }

    public String getInsMktType() {
        return insMktType;
    }

    public void setInsMktType(String value) {
        this.insMktType = value;
    }

    public ScalingConstraint getScalingConstraint() {
        return scalingConstraint;
    }

    public void setScalingConstraint(ScalingConstraint scalingConstraint) {
        this.scalingConstraint = scalingConstraint;
    }

    public List<SheinElasticScalingRuleData> getScalingRules() {
        return scalingRules;
    }

    public void setScalingRules(List<SheinElasticScalingRuleData> scalingRules) {
        this.scalingRules = scalingRules;
    }

    public String getInsType() {
        return insType;
    }

    public void setInsType(String value) {
        this.insType = value;
    }

    public long getLocalSsdCnt() {
        return localSsdCnt;
    }

    public void setLocalSsdCnt(long value) {
        this.localSsdCnt = value;
    }

    public String getLocalSsdInterface() {
        return localSsdInterface;
    }

    public void setLocalSsdInterface(String value) {
        this.localSsdInterface = value;
    }

    public long getVolumeIOps() {
        return volumeIOps;
    }

    public void setVolumeIOps(long value) {
        this.volumeIOps = value;
    }

    public Integer getVolumeSizeInGB() {
        return volumeSizeInGB;
    }

    public void setVolumeSizeInGB(Integer volumeSizeInGB) {
        this.volumeSizeInGB = volumeSizeInGB;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String value) {
        this.volumeType = value;
    }

    public Integer getPriceStrategy() {
        return priceStrategy;
    }

    public void setPriceStrategy(Integer priceStrategy) {
        this.priceStrategy = priceStrategy;
    }

    public BigDecimal getPriceStrategyValue() {
        return priceStrategyValue;
    }

    public void setPriceStrategyValue(BigDecimal priceStrategyValue) {
        this.priceStrategyValue = priceStrategyValue;
    }

    public Integer getPurchasePriority() {
        return purchasePriority;
    }

    public void setPurchasePriority(Integer purchasePriority) {
        this.purchasePriority = purchasePriority;
    }

    public Integer getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(Integer provisionType) {
        this.provisionType = provisionType;
    }

    public String getSpotAllocationStrategy() {
        return spotAllocationStrategy;
    }

    public void setSpotAllocationStrategy(String spotAllocationStrategy) {
        this.spotAllocationStrategy = spotAllocationStrategy;
    }

    public String getRegularAllocationStrategy() {
        return regularAllocationStrategy;
    }

    public void setRegularAllocationStrategy(String regularAllocationStrategy) {
        this.regularAllocationStrategy = regularAllocationStrategy;
    }
}