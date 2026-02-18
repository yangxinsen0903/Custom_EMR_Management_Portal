package com.sunbox.service;

import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.ConfClusterVm;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格相关的
 */
public interface IAzurePriceService {
    /**
     *
     * @param region 数据中心
     * @param skuList sku列表
     * @param priceStrategy 竞价策略 1 按市场价百分比, 2 固定价
     * @param maxPrice 出价
     * @return
     */
    BigDecimal computeSpotPrice(String region, List<String> skuList, Integer priceStrategy, BigDecimal maxPrice);

    /**
     *
     * @param region 数据中心
     * @param confClusterVm
     * @return
     */
    BigDecimal computeSpotPrice(String region, ConfClusterVm confClusterVm);

    /**
     *
     * @param priceStrategy 竞价策略 1 按市场价百分比, 2 固定价
     * @param ondemandUnitPrice 按需价格
     * @param maxPrice 出价
     * @return
     */
    BigDecimal computeSpotPrice(Integer priceStrategy,BigDecimal ondemandUnitPrice,BigDecimal maxPrice);

    /**
     * 在skuList列表中,获取按需价格最高的sku
     * @param region
     * @param skuList
     * @return
     */
    AzurePriceHistory getMaxOndemondPrice(String region, List<String> skuList);

    /**
     * 从缓存中获取价格,缓存中没有从数据库获取
     * @param region
     * @param sku
     * @return
     */
    AzurePriceHistory getPriceCache(String region, String sku);
}
