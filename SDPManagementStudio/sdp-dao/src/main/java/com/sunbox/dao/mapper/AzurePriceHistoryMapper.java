/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.dao.mapper;

import com.sunbox.domain.AzurePriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Azure历史价格表 Mapper
 * @author wangda
 * @date 2024/6/13
 */
@Mapper
@Repository
public interface AzurePriceHistoryMapper {
    /**
     * 插入一条数据Azure历史价格表
     * @param azurePriceHistory
     */
    void insert(AzurePriceHistory azurePriceHistory);

    /**
     * 根据region和时间范围查询Azure历史价格记录, 不作查询条数限制
     * @param region
     * @param startDate
     * @param endDate
     * @return
     */
    List<AzurePriceHistory> selectByRegionAndDateRange(@Param("region") String region,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate);

    List<AzurePriceHistory> selectByRegionAndSkuNameAndDateRange(@Param("region") String region,
                                                       @Param("skuName") String skuName,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate);

    /**
     * 查询最新的价格
     * @param region Region
     * @param skuName Sku名
     * @return 价格, 如果没有,返回null
     */
    AzurePriceHistory selectLatestPrice(@Param("region") String region, @Param("skuName") String skuName);

    /**
     * 根据时间范围查询每天最后一条Azure历史价格记录, 不作查询条数限制
     * @param startDate
     * @param endDate
     * @return
     */
    List<AzurePriceHistory> selectDayLastByRegionAndDateRange(@Param("region") String region,@Param("skuName") String skuName,@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询最新的价格
     * @param region Region
     * @param skuNames Sku名
     * @return 价格
     */
    List<AzurePriceHistory> selectSkuLatest(@Param("region") String region, @Param("skuNames") List<String> skuNames);
}
