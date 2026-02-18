package com.sunbox.sdpscale.constant;

public interface ScaleConstant {
    /**
     * 规则变更通知:metric_change_ip:true/false
     */
    String metric_change = "metric_change";
    /**
     * 注册IP：item:ip_timestamp
     */
    String metric_machine_ips = "metric_machine_ips";
    /**
     * 指标锁：item:ip_clusterId_loadmetric_windowSize_timestamp
     */
    String lock_metrics = "lock_metrics";
    /**
     * 采集服务状态检查
     */
    String metric_machine_check = "metric_machine_check";
    /**
     * 指标状态检查
     */
    String metrics_check = "metrics_check";

    /**
     * 指标轮询锁
     */
    String metric_collect_lock = "metric_collect_lock";

    /**
     * 主机IP列表缓存
     */
    String cluster_host_list_prefix = "cluster_host_list_prefix";
}
