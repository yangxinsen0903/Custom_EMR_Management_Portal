package com.sunbox.sdpadmin.util;

/**
 * @date 2023/6/23
 */
public class RedisConst {

    public static final Long EXPIRES_ONE_HOUR = 3600L;

    public static final Long EXPIRES_ONE_MINITE = 60L;

    /**
     * redis中存储的锁的key，用于检查集群中已停止的组件
     * @return
     */
    public static String keyLockCheckStoppedComponent() {
        return "sdp_lock_check_stopped_component";
    }

    /**
     * redis中存储的锁的key，用于自动统计Prometheus指标数据
     * @return
     */
    public static String keyLockStatPrometheusMetrics() {
        return "sdp_lock_stat_prometheus_metrics";
    }

    public static String keySdpSpotBuyFailureRate() {
        return "sdp:spot:buy:failure:rate";
    }

    public static String keySdpEvictRate() {
        return "sdp:evict:rate";
    }

    public static String keySdpSpotBuyAvgtime() {
        return "sdp:spot:buy:avgtime";
    }

    public static String keySdpClusterResource() {
        return "sdp:cluster:resource";
    }

    public static String keySdpTaskFailCount() {
        return "sdp:task:fail:count";
    }

}
