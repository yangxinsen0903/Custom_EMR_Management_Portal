package com.sunbox.sdpservice.service;

import com.sunbox.domain.ResultMsg;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@FeignClient("SDP-SPOT")
@RestController
@RequestMapping("/spot")
public interface SpotService {
    /**
     * 获取竞价实例对应sku的价格
     *
     * @param
     * @return resultMsg
     */
    @PostMapping(value = "/getSkuRealtimePrice")
    ResultMsg getSkuRealtimePrice(@RequestParam("skuName") String skuName);
}
