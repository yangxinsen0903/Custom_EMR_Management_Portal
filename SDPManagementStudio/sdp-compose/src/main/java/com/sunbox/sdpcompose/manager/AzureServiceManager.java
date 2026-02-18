package com.sunbox.sdpcompose.manager;

import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ConfScalingTaskVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.SpotPriceStrategy;
import com.sunbox.sdpcompose.mapper.ConfClusterVmMapper;
import com.sunbox.sdpcompose.model.azure.request.AzureSpotProfile;
import com.sunbox.sdpcompose.service.IAzureService;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public class AzureServiceManager {
    private AzureServiceManager() {
    }

    public static VmRealtimePrice tryGetSpotVmRealtimePrice(Logger logger, IAzureService azureService, String skuName,String region) {
        logger.info("tryGetSpotVmRealtimePrice skuName:{}", skuName);
        ResultMsg instancePrice = azureService.getInstancePrice(skuName,region);
        if (!instancePrice.getResult()) {
            logger.error("azureService.getInstancePrice error skuName:{}, message:{}",
                    skuName,
                    instancePrice.getMsg());
            return null;
        }

        logger.info("azureService.getInstancePrice skuName:{}, result:{}", skuName, instancePrice.getData());

        try {
            /**
             * {
             *   "vmSkuName": "Standard_D4s_v5",
             *   "spotUnitPricePerHourUSD": 0.019429,
             *   "onDemandUnitPricePerHourUSD": 0.192
             * }
             */
            Map<String, Object> dataMap = (Map<String, Object>) instancePrice.getData();
            return new VmRealtimePrice(dataMap.get("vmSkuName").toString(),
                    Double.parseDouble(dataMap.get("spotUnitPricePerHourUSD").toString()),
                    Double.parseDouble(dataMap.get("onDemandUnitPricePerHourUSD").toString()));
        } catch (Exception e) {
            logger.error("construct VmRealtimePrice error", e);
            return null;
        }
    }

    public static class VmRealtimePrice {
        private String vmName;
        private double rtPrice;
        private double stdPrice;

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


    public static ResultMsg buildAzureSpotProfile(Logger logger,
                                                  IAzureService azureService,
                                                  ConfScalingTaskVm taskVm,
                                                  ConfClusterVmMapper confClusterVmMapper,
                                                  String clusterId,String region) {
        if (Objects.equals(taskVm.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
            logger.info("build spot AzureSpotProfile clusterId:{}, taskId:{}",
                    clusterId,
                    taskVm.getTaskId());
            ConfClusterVm confClusterVm = confClusterVmMapper.selectByClusterIdAndVmConfId(clusterId, taskVm.getVmConfId());
            if (confClusterVm == null) {
                logger.error("not found confClusterVm,clusterId:{}, taskId:{}, vmConfId:{}",
                        clusterId,
                        taskVm.getTaskId(),
                        taskVm.getVmConfId());
                return ResultMsg.FAILURE("not found confClusterVm");
            } else {
                return buildAzureSpotProfile(logger, azureService, confClusterVm, clusterId,region);
            }
        } else {
            return ResultMsg.SUCCESS();
        }
    }

    public static ResultMsg buildAzureSpotProfile(Logger logger,
                                                  IAzureService azureService,
                                                  ConfClusterVm item,
                                                  String clusterId,String region) {
        if (Objects.equals(item.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
            logger.info("build spot AzureSpotProfile clusterId:{}, groupId:{}",
                    clusterId,
                    item.getGroupId());
            if (item.getPriceStrategy() == null) {
                logger.error("skip build azure vm parameter because price strategy is null, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        item.getPriceStrategy());
                return ResultMsg.FAILURE("price strategy is null");
            }

            if (item.getMaxPrice() == null) {
                logger.error("skip build azure vm parameter because max price is null, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        item.getPriceStrategy());
                return ResultMsg.FAILURE("max price is null");
            }

            if (!SpotPriceStrategy.validate(item.getPriceStrategy())) {
                logger.error("skip build azure vm parameter because price strategy is not invalid, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        item.getPriceStrategy());
                return ResultMsg.FAILURE("price strategy is not invalid");
            }

            AzureServiceManager.VmRealtimePrice vmRealtimePrice = AzureServiceManager.tryGetSpotVmRealtimePrice(logger, azureService, item.getSku(),region);
            if (vmRealtimePrice == null) {
                logger.error("skip build azure vm parameter because get vmRealtimePrice return null, clusterId:{}, groupId:{}, skuName:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku());
                return ResultMsg.FAILURE("get spot price error");
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
                logger.error("skip build azure vm parameter because price strategy is not invalid, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        item.getPriceStrategy());
                return ResultMsg.FAILURE("price strategy is not invalid");
            }

            if (bidPrice == null || bidPrice.compareTo(BigDecimal.ZERO) < 0) {
                logger.error("skip build azure vm parameter because get bid price is null or less than zero, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        vmRealtimePrice.getStdPrice(),
                        vmRealtimePrice.getRtPrice());
                return ResultMsg.FAILURE(" bid price is null or less than zero");
            }

            if (vmRealtimePrice.getRtPrice() > bidPrice.doubleValue()) {
                logger.error("skip build azure vm parameter because realtime price greater than bid price, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}, bid price:{}",
                        item.getClusterId(),
                        item.getGroupId(),
                        item.getSku(),
                        item.getMaxPrice(),
                        vmRealtimePrice.getStdPrice(),
                        vmRealtimePrice.getRtPrice(),
                        bidPrice.doubleValue());
                return ResultMsg.FAILURE("realtime price greater than bid price");
            }
//            else if (vmRealtimePrice.getRtPrice() < bidPrice.doubleValue()) {
//                logger.warn("update bid price:{} = realtime price:{}, clusterId:{}, groupId:{}, skuName:{}, max price:{}, std price:{}, realtime price:{}",
//                        bidPrice,
//                        vmRealtimePrice.getRtPrice(),
//                        item.getClusterId(),
//                        item.getGroupId(),
//                        item.getSku(),
//                        item.getMaxPrice(),
//                        vmRealtimePrice.getStdPrice(),
//                        vmRealtimePrice.getRtPrice());
//                bidPrice = new BigDecimal(Double.toString(vmRealtimePrice.getRtPrice()));
//            }

            //组建参数
            AzureSpotProfile azureSpotProfile = new AzureSpotProfile();
            azureSpotProfile.setEvictionPolicy(AzureSpotProfile.EvictionPolicy.Delete);
            azureSpotProfile.setMaxPricePerHour(bidPrice.doubleValue());
            azureSpotProfile.setDemandPricePerHour(vmRealtimePrice.getStdPrice());
            azureSpotProfile.setEvictionType(AzureSpotProfile.EvictionType.PriceOrCapacity);
            logger.info("return AzureSpotProfile clusterId:{}, groupId:{}, skuName:{}, max price:{}, bid price:{}, std price:{}, realtime price:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    bidPrice,
                    vmRealtimePrice.getStdPrice(),
                    vmRealtimePrice.getRtPrice());
            return ResultMsg.SUCCESS(azureSpotProfile);
        }
        return ResultMsg.SUCCESS();
    }
}
