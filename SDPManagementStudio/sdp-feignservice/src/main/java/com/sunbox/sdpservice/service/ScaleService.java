package com.sunbox.sdpservice.service;

import com.sunbox.domain.ResultMsg;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@FeignClient("SDP-SCALE")
@RestController
@RequestMapping("/scale")
public interface ScaleService {
    /**
     * 弹性伸缩规则变更
     *
     * @param
     * @return resultMsg
     */
    @PostMapping(value = "/metricChange")
    ResultMsg metricChange();
}
