package com.sunbox.sdpadmin.task;

import com.sunbox.sdpadmin.service.PrometheusMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @date 2023/6/23
 */
@Component
@EnableAsync
public class AutoStatPrometheusMetricsTask {

    @Autowired
    private PrometheusMetricsService prometheusMetricsService;

    /**
     * 自动统计Prometheus指标数据的定时任务执行时间间隔, 单位:毫秒，默认140秒
     */
    @Scheduled(fixedDelayString = "${auto.stat.prometheus.metrics.interval:140000}")
    @Async
    public void start() {
        prometheusMetricsService.statPrometheusMetrics();
    }
}
