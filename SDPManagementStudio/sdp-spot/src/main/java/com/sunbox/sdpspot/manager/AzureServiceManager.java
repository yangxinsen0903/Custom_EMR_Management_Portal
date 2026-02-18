package com.sunbox.sdpspot.manager;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ConfHostGroupVmSku;
import com.sunbox.domain.enums.SpotPriceStrategy;
import com.sunbox.service.IAzureService;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


public class AzureServiceManager {

    private AzureServiceManager() {
    }

    private static VmRealtimePrice tryGetSpotVmRealtimePrice(IAzureService iAzureService, Logger logger, List<String> skuNames,String region) {
        logger.info("tryGetSpotVmRealtimePrice skuName:{}", skuNames);
        List<AzurePriceHistory> instancePriceList = iAzureService.getSkuInstancePrice(skuNames, region);
        if (CollectionUtils.isEmpty(instancePriceList)) {
            logger.error("未获取到实时价格 skuName:{}, result:{}", skuNames, instancePriceList);
            return null;
        }
        logger.info("azureService.getInstancePrice skuName:{}, result:{}", skuNames, instancePriceList);
        try {
            //一个资源池中有多个VM Sku， 那么选择价格最高的Sku
            Optional<AzurePriceHistory> max = instancePriceList.stream().max(Comparator.comparing(AzurePriceHistory::getOndemandUnitPrice));
            if (!max.isPresent()){
                logger.error("未获取到最大实时价格 skuName:{}, result:{}", skuNames, instancePriceList);
                return null;
            }
            AzurePriceHistory instancePrice = max.get();
            /**
             * {
             *   "vmSkuName": "Standard_D4s_v5",
             *   "spotUnitPricePerHourUSD": 0.019429,
             *   "onDemandUnitPricePerHourUSD": 0.192
             * }
             */
            return new VmRealtimePrice(instancePrice.getVmSkuName(),
                    instancePrice.getSpotUnitPrice().doubleValue(),
                    instancePrice.getOndemandUnitPrice().doubleValue());
        } catch (Exception e) {
            logger.error("construct VmRealtimePrice error", e);
            return null;
        }
    }

    public static class VmRealtimePrice {
        private String vmName;
        private double rtPrice;//市场价格
        private double stdPrice; //实际价格

        public VmRealtimePrice(String vmName, double rtPrice, double stdPrice) {
            this.vmName = vmName;
            this.rtPrice = rtPrice;
            this.stdPrice = stdPrice;
        }

        public String getVmName() {
            return vmName;
        }

        public double getRtPrice() {
            return rtPrice;
        }

        public double getStdPrice() {
            return stdPrice;
        }
    }

    /**
     * true: 能构建，false：不能构建
     *
     * @param logger
     * @param azureService
     * @return
     */
    public static boolean canBuildAzureSpotProfile(IAzureService azureService,
                                                   Logger logger,
                                                   ConfCluster confCluster,
                                                   ConfClusterVm item,
                                                   List<ConfHostGroupVmSku> vmSkus) {
        if (!Objects.equals(item.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
            logger.error("invalid purchase type:{}, clusterId:{}, groupId:{}, skuName:{}",
                    item.getPurchaseType(),
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku());
            return false;
        }

        logger.info("check build spot AzureSpotProfile clusterId:{}, groupId:{}",
                confCluster.getClusterId(),
                item.getGroupId());
        if (item.getPriceStrategy() == null) {
            logger.error("can not build azure vm parameter because price strategy is null, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy());
            return false;
        }

        if (item.getMaxPrice() == null) {
            logger.error("can not build azure vm parameter because max price is null, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy());
            return false;
        }

        if (!SpotPriceStrategy.validate(item.getPriceStrategy())) {
            logger.error("can not build azure vm parameter because price strategy is not invalid, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy());
            return false;
        }
        List<String> skuNames = vmSkus.stream().map(ConfHostGroupVmSku::getSku).collect(Collectors.toList());
        //兼容之前单机型
        if (CollectionUtils.isEmpty(skuNames)){
            skuNames=new ArrayList<>();
            skuNames.add(item.getSku());
        }
        VmRealtimePrice vmRealtimePrice = AzureServiceManager.tryGetSpotVmRealtimePrice(azureService, logger, skuNames, confCluster.getRegion());
        if (vmRealtimePrice == null) {
            logger.error("can not build azure vm parameter because get vmRealtimePrice return null, clusterId:{}, groupId:{}, skuName:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku());
            return false;
        }

        BigDecimal bidPrice = item.getMaxPrice();
        if (Objects.equals(item.getPriceStrategy(), SpotPriceStrategy.MARKET.getId())) {
            bidPrice = new BigDecimal(vmRealtimePrice.getStdPrice()).multiply(item.getMaxPrice()).divide(new BigDecimal("100.00"));
            bidPrice = bidPrice.setScale(6, RoundingMode.HALF_UP);
            logger.info("calculate azure vm parameter bid price, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}, bid price:{}, std price:{}, realtime price:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy(),
                    bidPrice,
                    vmRealtimePrice.getStdPrice(),
                    vmRealtimePrice.getRtPrice());
        } else if (Objects.equals(item.getPriceStrategy(), SpotPriceStrategy.QUOTE.getId())) {
            bidPrice = item.getMaxPrice();
        } else {
            logger.error("can not build azure vm parameter because price strategy is not invalid, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy());
            return false;
        }

        if (bidPrice == null || bidPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("can not build azure vm parameter because get bid price is null or less than zero, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}, bid price:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    vmRealtimePrice.getStdPrice(),
                    vmRealtimePrice.getRtPrice(),
                    bidPrice.doubleValue());
            return false;
        }

        if (vmRealtimePrice.getRtPrice() > bidPrice.doubleValue()) {
            logger.error("can not build azure vm parameter because realtime price greater than bid price, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}, bid price:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    vmRealtimePrice.getStdPrice(),
                    vmRealtimePrice.getRtPrice(),
                    bidPrice.doubleValue());
            return false;
        }
//        else if (vmRealtimePrice.getRtPrice() < bidPrice.doubleValue()) {
//            logger.warn("update bid price:{} = realtime price, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}, bid price:{}",
//                    bidPrice,
//                    vmRealtimePrice.getRtPrice(),
//                    item.getClusterId(),
//                    item.getGroupId(),
//                    item.getSku(),
//                    item.getMaxPrice(),
//                    vmRealtimePrice.getStdPrice(),
//                    vmRealtimePrice.getRtPrice(),
//                    bidPrice.doubleValue());
//            bidPrice = new BigDecimal(vmRealtimePrice.getRtPrice());
//        }

        //组建参数
        logger.info("can build AzureSpotProfile clusterId:{}, groupId:{}, skuName:{}, max price:{}, bid price:{}, std price:{}, realtime price:{}",
                item.getClusterId(),
                item.getGroupId(),
                item.getSku(),
                item.getMaxPrice(),
                bidPrice,
                vmRealtimePrice.getStdPrice(),
                vmRealtimePrice.getRtPrice());
        return true;
    }
}
