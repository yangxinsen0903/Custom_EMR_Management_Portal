/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdptask.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.dao.mapper.MetaDataItemMapper;
import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.MetaDataItem;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.MetaDataType;
import com.sunbox.sdptask.mapper.InfoClusterOperationPlanMapper;
import com.sunbox.service.IAzureService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 从Azure获取SKU价格和驱逐率的任务, 每小时执行一次.
 * @author wangda
 * @date 2024/6/13
 */
@Component
public class GetAzurePriceAndEvictionRateTask implements BaseCommonInterFace {
    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private MetaDataItemMapper metaDataItemMapper;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    @Autowired
    private IAzureService azureService;

    @Scheduled(cron = "${cron.azure.priceAndEviction.task:0 0 * * * ?}")
//    @Scheduled(cron = "${cron.azure.priceAndEviction.task:0 * * * * ?}")
//    @Scheduled(fixedDelay = 10000L)
    public void startJob() {
        // 获取锁
        getLogger().info("从Azure获取竞价实例价格及驱逐率任务开始....");
        String lockKey = "azure.priceAndEviction.lockKey";
        boolean lockResult = this.redisLock.tryLock(lockKey);
        if (!lockResult) {
            getLogger().error("从Azure获取竞价实例价格及驱逐率任务在获取锁时失败, lock key:{}", lockKey);
            return;
        }
        try {
            String currentMinuteStr = DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm");
            Date currentMinute = DateUtil.parse(currentMinuteStr);
            checkIsFinishedInCurrentHour(currentMinute);

            // 获取所有的Region
            List<JSONObject> regions = getAllRegions();
            for (JSONObject region : regions) {
                try {
                    String regionId = region.getString("region");

                    // 处理一个Region的价格和驱逐率
                    handleARegion(currentMinute, regionId);
                } catch (Exception ex) {
                    getLogger().error("获取竞价实例价格和驱逐率失败：region={}", region.getString("region"), ex);
                }
            }
        } catch (Exception ex) {
            getLogger().error("从Azure获取竞价实例价格及驱逐率任务-执行失败", ex);
        }finally {
            this.redisLock.tryUnlock(lockKey);
        }
    }

    private void checkIsFinishedInCurrentHour(Date currentMinute) {
        // 从数据库中获取当前小时的数据
        String currentHourBeginStr = DateUtil.format(currentMinute, "yyyy-MM-dd HH:00:00");
        Date currentHourBegin = DateUtil.parse(currentHourBeginStr);
        String currentHourEndStr = DateUtil.format(currentMinute, "yyyy-MM-dd HH:59:59");
        Date currentHourEnd = DateUtil.parse(currentHourEndStr);
        List<AzurePriceHistory> azurePriceHistories = azurePriceHistoryMapper
                .selectByRegionAndDateRange(null, currentHourBegin, currentHourEnd);
        if (CollUtil.isNotEmpty(azurePriceHistories)) {
            throw new RuntimeException("当前小时的数据已经存在，无需再次执行：currentHourBegin=" + DateUtil.format(currentMinute, "yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * 处理一个Region的价格和驱逐率
     * @param currentMinute 当前时间
     * @param regionId
     */
    private void handleARegion(Date currentMinute, String regionId) {
        List<AzurePriceHistory> azurePriceHistories = new ArrayList<>();
        // 获取所有的SKU,每页10个,原因: 调用Azure接口获取竞价实例价格每次最多支持12个sku
        Map<Integer, List<String>> vmSkusPage = getAllVmSkusPage(regionId);
        for (Integer i : vmSkusPage.keySet()) {
            List<String> skus = vmSkusPage.get(i);

            // 调用Azure接口获取数据
            getLogger().info("开始获取竞价实例价格，region={}, vmSkuNames={}", regionId, StrUtil.join(",", skus));
            List<JSONObject> prices = azureService.getInstancePrice(skus, regionId);
            Map<String, JSONObject> priceMap = prices.stream().collect(Collectors.toMap(s -> s.getString("vmSkuName"), Function.identity()));

            getLogger().info("开始获取竞价实例驱逐率，region={}, vmSkuNames={}", regionId, StrUtil.join(",", skus));
            List<JSONObject> evictionRates = azureService.spotEvictionRate(skus, regionId);
            Map<String, JSONObject> evictionRateMap = evictionRates.stream().collect(Collectors.toMap(s -> s.getString("vmSkuName"), Function.identity()));

            for (String sku : skus) {
                AzurePriceHistory azurePriceHistory = new AzurePriceHistory();
                azurePriceHistory.setRegion(regionId);
                azurePriceHistory.setVmSkuName(sku);
                azurePriceHistory.setExecuteTime(currentMinute);
                JSONObject priceJson = priceMap.get(sku);
                if (Objects.nonNull(priceJson)) {
                    azurePriceHistory.setSpotUnitPrice(new BigDecimal(priceJson.getString("spotUnitPricePerHourUSD")).setScale(6));
                    azurePriceHistory.setOndemandUnitPrice(new BigDecimal(priceJson.getString("onDemandUnitPricePerHourUSD")).setScale(6));
                } else {
                    azurePriceHistory.setSpotUnitPrice(BigDecimal.ZERO);
                    azurePriceHistory.setOndemandUnitPrice(BigDecimal.ZERO);
                }

                JSONObject evictionRateJson = evictionRateMap.get(sku);
                if (Objects.nonNull(evictionRateJson)) {
                    String lowerValue = evictionRateJson.getString("evictionRateLowerPercentage");
                    lowerValue = Objects.isNull(lowerValue)? "0" : lowerValue;
                    String upperValue = evictionRateJson.getString("evictionRateUpperPercentage");
                    upperValue = Objects.isNull(upperValue)? "0" : upperValue;
                    azurePriceHistory.setEvictionRateLower(new BigDecimal(lowerValue).setScale(6));
                    azurePriceHistory.setEvictionRateUpper(new BigDecimal(upperValue).setScale(6));
                } else {
                    azurePriceHistory.setEvictionRateLower(BigDecimal.ZERO);
                    azurePriceHistory.setEvictionRateUpper(BigDecimal.ZERO);
                }
                azurePriceHistories.add(azurePriceHistory);
            }
        }
        if (CollUtil.isNotEmpty(azurePriceHistories)) {
            for (AzurePriceHistory azurePriceHistory : azurePriceHistories) {
                azurePriceHistoryMapper.insert(azurePriceHistory);
            }
        }
    }

    /**
     * 获取所有的Region
     * @return
     */
    private List<JSONObject> getAllRegions() {
        MetaDataItem item = new MetaDataItem();
        item.setType(MetaDataType.REGION.getCode());
        List<String> items = metaDataItemMapper.selectMetaData(item);
        return items.stream().map(s -> {
            return JSON.parseObject(s);
        }).collect(Collectors.toList());
    }

    /**
     * 获取某个Region下的所有的VM Sku并去除重复
     * @param region
     * @return
     */
    private List<String> getAllVmSkus(String region) {
        MetaDataItem item = new MetaDataItem();
        item.setRegion(region);
        item.setType(MetaDataType.VM_SKU.getCode());
        List<String> items = metaDataItemMapper.selectMetaData(item);
        return items.stream().map(s -> {
            Map map = JSON.parseObject(s, Map.class);
            return (String) map.get("name");
        }).distinct().collect(Collectors.toList());
    }
    /**
     * 获取某个Region下的所有的VM Sku 分页
     * @param region
     * @return
     */
    private Map<Integer, List<String>> getAllVmSkusPage(String region){
        Map<Integer, List<String>> vmSkusPage = new HashMap<>();
        List<String> allVmSkus = this.getAllVmSkus(region);
        if (CollUtil.isEmpty(allVmSkus)) {
            getLogger().info("获取数据中心所有VM SKU时，返回空，不进行后续处理：region={}", allVmSkus);
            return vmSkusPage;
        }
        //分页, 分页10个
        for (int i = 0; i < allVmSkus.size(); i++) {
            int nowPage = i / 10;
            if (!vmSkusPage.containsKey(nowPage)) {
                vmSkusPage.put(nowPage, new ArrayList<>());
            }
            vmSkusPage.get(nowPage).add(allVmSkus.get(i));
        }
        return vmSkusPage;
    }
}
