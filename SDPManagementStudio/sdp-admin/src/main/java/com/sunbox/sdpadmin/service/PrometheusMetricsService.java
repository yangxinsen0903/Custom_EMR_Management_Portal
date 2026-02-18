package com.sunbox.sdpadmin.service;

/**
 * @date 2023/6/14
 */
public interface PrometheusMetricsService {

    /**
     * 获取Prometheus指标数据
     * @return
     */
    String getPrometheusMetrics();

    /**
     * 自动统计Prometheus指标数据，主要用于定时任务调用
     */
    void statPrometheusMetrics();

    StringBuilder getTaskFailPrometheusMetrics();
}
