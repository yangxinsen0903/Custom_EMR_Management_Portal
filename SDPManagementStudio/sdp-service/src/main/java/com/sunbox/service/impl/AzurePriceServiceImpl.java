package com.sunbox.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ConfHostGroupVmSku;
import com.sunbox.domain.enums.SpotPriceStrategy;
import com.sunbox.service.IAzurePriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AzurePriceServiceImpl implements IAzurePriceService {
    private final TimedCache<String, AzurePriceHistory> priceCache = CacheUtil.newTimedCache(60000);

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;
    @Qualifier("confHostGroupVmSkuMapper")
    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    /**
     * 计算实际出价
     *
     * @param region
     * @param skuList
     * @param priceStrategy 竞价策略 1 按市场价百分比, 2 固定价
     * @param maxPrice      出价
     * @return
     */
    @Override
    public BigDecimal computeSpotPrice(String region, List<String> skuList, Integer priceStrategy, BigDecimal maxPrice) {
        AzurePriceHistory instancePrice = this.getMaxOndemondPrice(region, skuList);
        if (instancePrice == null) {
            log.error("获取价格失败, region:{}, skuList:{}", region, skuList);
            return null;
        }
        BigDecimal spotPrice = this.computeSpotPrice(priceStrategy,instancePrice.getOndemandUnitPrice(), maxPrice);
        log.info("PriceServiceImpl.getSpotPrice,计算实际出价, region:{}, priceStrategy:{}, skuList:{}, maxPrice:{},realtime price:{}",
                region,
                priceStrategy,
                skuList,
                maxPrice,
                spotPrice);
        return spotPrice;
    }

    /**
     * 计算实际出价
     *
     * @param priceStrategy 竞价策略 1 按市场价百分比, 2 固定价
     * @param ondemandUnitPrice 按需价格
     * @param maxPrice      出价
     * @return
     */
    @Override
    public BigDecimal computeSpotPrice(Integer priceStrategy,BigDecimal ondemandUnitPrice,BigDecimal maxPrice) {
        BigDecimal spotPrice = null;
        if (maxPrice == null) {
            log.error("出价信息不存在");
            return null;
        }
        if (Objects.equals(priceStrategy, SpotPriceStrategy.MARKET.getId())) {
            if (ondemandUnitPrice == null) {
                log.error("获取按需价格失败");
                return null;
            }
            spotPrice = ondemandUnitPrice
                    .multiply(maxPrice).divide(new BigDecimal("100.00"));
            spotPrice = spotPrice.setScale(6, RoundingMode.HALF_UP);
        } else if (Objects.equals(priceStrategy, SpotPriceStrategy.QUOTE.getId())) {
            spotPrice = maxPrice;
        }
        return spotPrice;
    }

    /**
     * 在skuList列表中,获取按需价格最高的sku
     *
     * @param region
     * @param skuList
     * @return
     */
    @Override
    public AzurePriceHistory getMaxOndemondPrice(String region, List<String> skuList) {
        List<AzurePriceHistory> azurePriceHistories = azurePriceHistoryMapper.selectSkuLatest(region, skuList);
        Optional<AzurePriceHistory> azurePriceHistoryMax = azurePriceHistories
                .stream()
                .max(Comparator.comparing(AzurePriceHistory::getOndemandUnitPrice));
        return azurePriceHistoryMax.orElse(null);
    }

    /**
     * 计算实际出价
     *
     * @param confClusterVm
     * @return
     */
    @Override
    public BigDecimal computeSpotPrice(String region, ConfClusterVm confClusterVm) {
        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByVmConfId(confClusterVm.getVmConfId());
        List<String> skuNameList = confHostGroupVmSkus.stream()
                .map(ConfHostGroupVmSku::getSku)
                .collect(Collectors.toList());
        return this.computeSpotPrice(region, skuNameList,confClusterVm.getPriceStrategy(),confClusterVm.getMaxPrice());
    }

    /**
     * 获取价格
     *
     * @param region
     * @param sku
     * @return
     */
    @Override
    public AzurePriceHistory getPriceCache(String region, String sku) {
        String key="price:"+region+":"+sku;
        AzurePriceHistory azurePriceHistory = priceCache.get(key);
        if (azurePriceHistory == null){
            azurePriceHistory = azurePriceHistoryMapper.selectLatestPrice(region, sku);
            if (azurePriceHistory != null){
                priceCache.put(key,azurePriceHistory);
            }
        }
        return azurePriceHistory;
    }

}
