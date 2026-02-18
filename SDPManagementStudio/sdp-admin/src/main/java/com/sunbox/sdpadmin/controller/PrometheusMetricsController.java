package com.sunbox.sdpadmin.controller;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.http.HttpUtil;
import com.sunbox.constant.BizConfigConstants;
import com.sunbox.sdpadmin.service.PrometheusMetricsService;
import com.sunbox.service.BizConfigService;
import com.sunbox.web.BaseCommon;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
public class PrometheusMetricsController extends BaseCommon implements BaseCommonInterFace {

    @Autowired
    private PrometheusMetricsService prometheusMetricsService;

    @Autowired
    private BizConfigService bizConfigService;

    private Boolean isEnableRouter = false;

    private String sdp1url;

    private final ExecutorService executor = ExecutorBuilder.create()
            .setCorePoolSize(2)
            .setMaxPoolSize(10)
            .setWorkQueue(new LinkedBlockingQueue<>(10))
            .build();

    @RequestMapping(value = "/prom/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
    public String metrics() {
        this.getParameterFromDb();
        if (isEnableRouter){
            Future<String> future = executor.submit(() -> HttpUtil.get(sdp1url+"/prom/metrics"));
            String prometheusMetrics = prometheusMetricsService.getPrometheusMetrics();
            try {
                String prometheusMetricsSDP1 = future.get();
                prometheusMetrics=prometheusMetrics+prometheusMetricsSDP1;
            } catch (Exception e) {
                logger.error("PrometheusMetricsController.metrics() future.get error", e);
            }
            return prometheusMetrics;
        }else {
            return prometheusMetricsService.getPrometheusMetrics();
        }
    }


    /**
     * 从数据库biz_config获取数据
     */
    private void getParameterFromDb() {
        isEnableRouter = bizConfigService.getConfigValue(
                BizConfigConstants.SDP_ROUTER,
                BizConfigConstants.SDP_ROUTER_IS_ENABLE_ROUTER,
                Boolean.class);
        sdp1url = bizConfigService.getConfigValue(
                BizConfigConstants.SDP_ROUTER,
                BizConfigConstants.SDP_ROUTER_SDP1URL,
                String.class);
    }

}
