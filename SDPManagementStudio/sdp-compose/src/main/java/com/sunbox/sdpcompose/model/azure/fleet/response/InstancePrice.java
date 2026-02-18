package com.sunbox.sdpcompose.model.azure.fleet.response;

import java.math.BigDecimal;

public class InstancePrice {

    /**
     * VM 规格名称
     */
    private String vmSkuName;

    /**
     * spot 小时单价 美元
     */
    private BigDecimal spotUnitPricePerHourUSD;

    /**
     * ondemand 小时单价 美元
     */
    private BigDecimal onDemandUnitPricePerHourUSD;

    public String getVmSkuName() {
        return vmSkuName;
    }

    public void setVmSkuName(String vmSkuName) {
        this.vmSkuName = vmSkuName;
    }

    public BigDecimal getSpotUnitPricePerHourUSD() {
        return spotUnitPricePerHourUSD;
    }

    public void setSpotUnitPricePerHourUSD(BigDecimal spotUnitPricePerHourUSD) {
        this.spotUnitPricePerHourUSD = spotUnitPricePerHourUSD;
    }

    public BigDecimal getOnDemandUnitPricePerHourUSD() {
        return onDemandUnitPricePerHourUSD;
    }

    public void setOnDemandUnitPricePerHourUSD(BigDecimal onDemandUnitPricePerHourUSD) {
        this.onDemandUnitPricePerHourUSD = onDemandUnitPricePerHourUSD;
    }

    @Override
    public String toString() {
        return "InstancePrice{" +
                "vmSkuName='" + vmSkuName + '\'' +
                ", spotUnitPricePerHourUSD=" + spotUnitPricePerHourUSD +
                ", onDemandUnitPricePerHourUSD=" + onDemandUnitPricePerHourUSD +
                '}';
    }
}
