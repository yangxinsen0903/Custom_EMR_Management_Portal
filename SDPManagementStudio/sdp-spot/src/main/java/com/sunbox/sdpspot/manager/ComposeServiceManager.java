package com.sunbox.sdpspot.manager;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdpspot.data.VmRealtimePrice;
import org.slf4j.Logger;

import java.util.Map;

public class ComposeServiceManager {
    private ComposeServiceManager() {
    }

   /* public static VmRealtimePrice tryGetSpotVmRealtimePrice(Logger logger, ComposeService composeService, String skuName) {
        ResultMsg instancePrice = composeService.getInstancePrice(skuName);
        if (!instancePrice.getResult()) {
            logger.error("composeService.getInstancePrice error skuName:{}, message:{}",
                    skuName,
                    instancePrice.getMsg());
            return null;
        }

        logger.info("composeService.getInstancePrice skuName:{}, result:{}", skuName, instancePrice.getData());

        try {
            Map<String, Object> dataMap = (Map<String, Object>) instancePrice.getData();
            return new VmRealtimePrice(dataMap.get("vmSkuName").toString(),
                    Double.parseDouble(dataMap.get("unitPrice").toString()));
        } catch (Exception e) {
            logger.error("construct VmRealtimePrice error", e);
            return null;
        }
    }*/
}
