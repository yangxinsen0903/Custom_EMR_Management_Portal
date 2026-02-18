/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Azure价格的驱逐率历史表
 *
 * @author wangda
 * @date 2024/6/13
 */
public class AzurePriceHistory {
    //    '自增ID',
    private Long id;
    //    '从Azure获取数据的执行时间',
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;
    //    'VM SKU Name',
    private String vmSkuName;
    //    'Region',
    private String region;
    //     '竞价实例价格',
    private BigDecimal spotUnitPrice;
    //     '按需实例价格',
    private BigDecimal ondemandUnitPrice;
    //    '最低驱逐率',
    private BigDecimal evictionRateLower;
    //   '最高驱逐率',
    private BigDecimal evictionRateUpper;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public String getVmSkuName() {
        return vmSkuName;
    }

    public void setVmSkuName(String vmSkuName) {
        this.vmSkuName = vmSkuName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getSpotUnitPrice() {
        return spotUnitPrice;
    }

    public void setSpotUnitPrice(BigDecimal spotUnitPrice) {
        this.spotUnitPrice = spotUnitPrice;
    }

    public BigDecimal getOndemandUnitPrice() {
        return ondemandUnitPrice;
    }

    public void setOndemandUnitPrice(BigDecimal ondemandUnitPrice) {
        this.ondemandUnitPrice = ondemandUnitPrice;
    }

    public BigDecimal getEvictionRateLower() {
        return evictionRateLower;
    }

    public void setEvictionRateLower(BigDecimal evictionRateLower) {
        this.evictionRateLower = evictionRateLower;
    }

    public BigDecimal getEvictionRateUpper() {
        return evictionRateUpper;
    }

    public void setEvictionRateUpper(BigDecimal evictionRateUpper) {
        this.evictionRateUpper = evictionRateUpper;
    }
}
