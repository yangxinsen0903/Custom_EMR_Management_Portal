package com.sunbox.sdpspot.controller;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpservice.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spot")
public class SpotController {
    @Autowired
    SpotService spotService;

    /**
     * 获取竞价实例对应sku的价格
     *
     * @param
     * @return resultMsg
     */
    @PostMapping(value = "/getSkuRealtimePrice")
    public ResultMsg getSkuRealtimePrice(@RequestParam("skuName") String skuName) {
        return spotService.getSkuRealtimePrice(skuName);
    }
}
