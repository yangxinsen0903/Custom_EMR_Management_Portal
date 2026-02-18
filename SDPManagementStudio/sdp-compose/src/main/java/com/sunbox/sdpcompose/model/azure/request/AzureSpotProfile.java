package com.sunbox.sdpcompose.model.azure.request;

public class AzureSpotProfile {

    private EvictionType evictionType;

    private EvictionPolicy evictionPolicy;

    /**
     * 客户接受的最高价格
     */
    private Double maxPricePerHour;

    /**
     * 按需价格
     */
    private Double demandPricePerHour;

    public enum EvictionType {

        None,

        /**
         * 客户指定价格
         */
        PriceOrCapacity,

        /**
         * 不指定价格，接收配额不足时被驱逐
         */
        CapacityOnly
    }

    public enum EvictionPolicy {

        None,

        /**
         * 虚机被驱逐后只关机
         */
        StopAndDeallocate,

        /**
         * 虚机被驱逐后删除
         */
        Delete
    }

    public EvictionType getEvictionType() {
        return evictionType;
    }

    public void setEvictionType(EvictionType evictionType) {
        this.evictionType = evictionType;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public void setEvictionPolicy(EvictionPolicy evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public Double getMaxPricePerHour() {
        return maxPricePerHour;
    }

    public void setMaxPricePerHour(Double maxPricePerHour) {
        this.maxPricePerHour = maxPricePerHour;
    }

    public Double getDemandPricePerHour() {
        return demandPricePerHour;
    }

    public void setDemandPricePerHour(Double demandPricePerHour) {
        this.demandPricePerHour = demandPricePerHour;
    }

    @Override
    public String toString() {
        return "AzureSpotProfile{" +
                "evictionType=" + evictionType +
                ", evictionPolicy=" + evictionPolicy +
                ", maxPricePerHour=" + maxPricePerHour +
                ", demandPricePerHour=" + demandPricePerHour +
                '}';
    }
}
