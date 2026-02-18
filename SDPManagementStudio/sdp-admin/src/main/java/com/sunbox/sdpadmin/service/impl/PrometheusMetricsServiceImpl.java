package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.sunbox.dao.mapper.TaskEventMapper;
import com.sunbox.sdpadmin.mapper.PrometheusMetricsMapper;
import com.sunbox.sdpadmin.model.metrics.PrometheusMetrics;
import com.sunbox.sdpadmin.service.PrometheusMetricsService;
import com.sunbox.sdpadmin.util.RedisConst;
import com.sunbox.util.DistributedRedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 为Prometheus提供指标数据
 * @date 2023/6/14
 */
@Service
public class PrometheusMetricsServiceImpl implements PrometheusMetricsService {

    private Logger logger = LoggerFactory.getLogger(PrometheusMetricsServiceImpl.class);
    @Autowired
    private PrometheusMetricsMapper prometheusMetricsMapper;

    @Autowired
    private TaskEventMapper taskEventMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Override
    public String getPrometheusMetrics() {
        StringBuilder content = new StringBuilder();
        // 统计竞价实例组扩容·购买失败率 & 原因：整个流程
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpSpotBuyFailureRate()));

        // 统计竞价实例组正常运行逐出率 & 数量 | 原因 | 占比（失败次数聚合 -> 占比和数量）
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpEvictRate()));

        // 统计平均购买时间（买机器时间/单次任务的累加时间  ->  结果 目标值 & 当前值，容量充足情况下的预估时间&实际时间 ）
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpSpotBuyAvgtime()));

        // 统计集群资源数量：主机数量，CPU核数。
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpClusterResource()));

        // 统计集群资源数量：主机数量，CPU核数。
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpClusterResource()));

        // 统计集群任务执行失败次数
        content.append(getPrometheusMetricsFromCache(RedisConst.keySdpTaskFailCount()));

        return content.toString();
    }

    // 从缓存在获取Prometheus数据
    private String getPrometheusMetricsFromCache(String key) {
        String content = redisLock.getValue(key);
        if (StrUtil.isBlank(content)) {
            logger.info("Prometheus指标数据在缓存中不存在，重新统计并缓存：key={}", key);
            statPrometheusMetrics();
            content = redisLock.getValue(key);
        }
        return content;
    }

    void statisticsSpotBuyFailureRate(StringBuilder content) {
        content.append(statisticsSpotBuyFailureRate());
    }

    StringBuilder statisticsSpotBuyFailureRate() {
        long start = System.currentTimeMillis();

        PrometheusMetrics metrics = PrometheusMetrics.newCounter("sdp_spot_buy_failure_rate",
                "竞价买入流程失败率，失败买入次数/总买入次数",
                Arrays.asList("clusterid", "clustername", "groupname", "sku", "errcode", "state"));
        // 统计竞价实例组扩容·购买失败率 & 原因：整个流程
        List<Map> failureRates = prometheusMetricsMapper.statisticsSpotBuyFailureRate();
        for (Map failureRate : failureRates) {
            String clusterId = (String) failureRate.get("cluster_id");
            String clusterName = (String) failureRate.get("cluster_name");
            String groupName = (String) failureRate.get("group_name");
            String sku = (String) failureRate.get("sku");
            Integer errcode = Convert.toInt(failureRate.get("errcode"));
            String state = (String) failureRate.get("state");
            Long failureCount = Convert.toLong(failureRate.get("fail_count"), 0L);
            if (Objects.nonNull(errcode)) {
                metrics.addMetrics("failure", Arrays.asList(clusterId, clusterName, groupName, sku, String.valueOf(errcode), state),
                        failureCount.doubleValue());
            }
        }
        // 计算总次数
        failureRates.stream().collect(Collectors.groupingBy(failureRate -> {
            String clusterId = (String) failureRate.get("cluster_id");
            String clusterName = (String) failureRate.get("cluster_name");
            String groupName = (String) failureRate.get("group_name");
            String sku = (String) failureRate.get("sku");
            String state = (String) failureRate.get("state");
            return Arrays.asList(clusterId, clusterName, groupName, sku, null, state);
        })).forEach((key, value) -> {
            Long total = value.stream().mapToLong(failureRate -> Convert.toLong(failureRate.get("total_count"))).sum();
            metrics.addMetrics("total", key, total.doubleValue());
        });
        logger.info("从数据库统计statisticsSpotBuyFailureRate耗时: {}ms", System.currentTimeMillis() - start);
        return metrics.toStringBuilder();
    }

    void statisticsSpotEvictRate(StringBuilder content) {
        content.append(statisticsSpotEvictRate());
    }

    StringBuilder statisticsSpotEvictRate() {
        long start = System.currentTimeMillis();
        PrometheusMetrics metrics = PrometheusMetrics.newCounter("sdp_evict_rate",
                "竞价实例的逐出率，数量，原因，占比",
                Arrays.asList("clusterid", "clustername", "groupname", "sku", "reason", "state"));

        // 统计竞价实例组正常运行逐出率 & 数量 | 原因 | 占比（失败次数聚合 -> 占比和数量）
        List<Map> evictRates = prometheusMetricsMapper.statisticsSpotEvictRate();

        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String sku = (String) evictRate.get("sku_name");
            String statTypeStr = (String) evictRate.get("stat_type");
            Long deleteCount = Convert.toLong(evictRate.get("delete_count"));
            String reasonCode = (String) evictRate.get("reason_code");
            String state = (String) evictRate.get("state");

            if (StrUtil.equalsIgnoreCase("delete", statTypeStr)) {
                metrics.addMetrics("count", Arrays.asList(clusterId, clusterName, groupName, sku, reasonCode, state),
                        deleteCount.doubleValue());
            }
        }

        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String statTypeStr = (String) evictRate.get("stat_type");
            String sku = (String) evictRate.get("sku_name");
            String state = (String) evictRate.get("state");
            Long totalCount = Convert.toLong(evictRate.get("buy_count"), 0L);

            if (StrUtil.equalsIgnoreCase("buy", statTypeStr)) {
                metrics.addMetrics("total", Arrays.asList(clusterId, clusterName, groupName, sku, null, state), totalCount.doubleValue());
            }
        }
        logger.info("从数据库统计statisticsSpotEvictRate耗时: {}ms", System.currentTimeMillis() - start);
        return metrics.toStringBuilder();
    }


    /**
     * 统计平均购买时间，返回字段
     * @param content
     */
    void statisticsAverageBuyTime(StringBuilder content) {
        content.append(statisticsAverageBuyTime());
    }

    StringBuilder statisticsAverageBuyTime() {
        long start = System.currentTimeMillis();
        PrometheusMetrics metrics = PrometheusMetrics.newCounter("sdp_spot_buy_avgtime",
                "平均购买时间，仅根据从Azure购买主机步骤统计",
                Arrays.asList("clusterid", "clustername", "groupname", "sku", "state"));
        // 统计平均购买时间（买机器时间/单次任务的累加时间  ->  结果 目标值 & 当前值，容量充足情况下的预估时间&实际时间 ）
        List<Map> evictRates = prometheusMetricsMapper.statisticsAverageBuyTime();

        // 计平均购买时间，返回Map的字段如下：
        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String sku = (String) evictRate.get("sku_name");
            String state = (String) evictRate.get("state");
            Long deleteCount = Convert.toLong(evictRate.get("total"), 0L);

            metrics.addMetrics("count", Arrays.asList(clusterId, clusterName, groupName, sku, state), deleteCount.doubleValue());
        }

        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String sku = (String) evictRate.get("sku_name");
            String state = (String) evictRate.get("state");
            Long timeSecond = Convert.toLong(evictRate.get("time_second"), 0L);

            metrics.addMetrics("time_seconds", Arrays.asList(clusterId, clusterName, groupName, sku, state), timeSecond.doubleValue());
        }
        logger.info("从数据库统计statisticsAverageBuyTime耗时: {}ms", System.currentTimeMillis() - start);
        return metrics.toStringBuilder();
    }

    /**
     * 统计集群资源计数
     * @param content
     */
    void statisticsClusterResource(StringBuilder content) {
        content.append(statisticsClusterResource());
    }

    StringBuilder statisticsClusterResource() {
        long start = System.currentTimeMillis();
        PrometheusMetrics metrics = PrometheusMetrics.newCounter("sdp_cluster_resource",
                "集群资源信息统计，只统计运行中的资源数据",
                Arrays.asList("clusterid", "clustername", "groupname", "sku", "purchasetype"));
        // 统计平均购买时间（买机器时间/单次任务的累加时间  ->  结果 目标值 & 当前值，容量充足情况下的预估时间&实际时间 ）
        List<Map> evictRates = prometheusMetricsMapper.statisticsClusterResource();

        // 计平均购买时间，返回Map的字段如下：
        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String sku = (String) evictRate.get("sku_name");
            String purchaseType = (String) evictRate.get("purchase_type");
            Long vmCount = Convert.toLong(evictRate.get("vm_count"), 0L);

            metrics.addMetrics("vmcount", Arrays.asList(clusterId, clusterName, groupName, sku, purchaseType),
                    vmCount.doubleValue());
        }

        for (Map evictRate : evictRates) {
            String clusterId = (String) evictRate.get("cluster_id");
            String clusterName = (String) evictRate.get("cluster_name");
            String groupName = (String) evictRate.get("group_name");
            String sku = (String) evictRate.get("sku_name");
            String purchaseType = (String) evictRate.get("purchase_type");
            Long cpuCount = Convert.toLong(evictRate.get("cpu_count"), 0L);

            metrics.addMetrics("cpucount", Arrays.asList(clusterId, clusterName, groupName, sku, purchaseType),
                    cpuCount.doubleValue());
        }
        logger.info("从数据库统计statisticsClusterResource耗时: {}ms", System.currentTimeMillis() - start);
        return metrics.toStringBuilder();
    }


    /**
     * 统计Prometheus指标数据
     */
    @Override
    public void statPrometheusMetrics() {
        logger.info("开始统计Prometheus指标数据");
        String lockKey = RedisConst.keyLockStatPrometheusMetrics();
        boolean locked = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, RedisConst.EXPIRES_ONE_MINITE);
        if (!locked) {
            logger.warn("统计Prometheus指标数据时未获取到锁，跳过本次统计");
            return;
        }

        try {
            // 统计竞价实例组扩容·购买失败率 & 原因：整个流程
            StringBuilder buyFailureRate = statisticsSpotBuyFailureRate();

            // 统计竞价实例组正常运行逐出率 & 数量 | 原因 | 占比（失败次数聚合 -> 占比和数量）
            StringBuilder evictRate = statisticsSpotEvictRate();

            // 统计平均购买时间（买机器时间/单次任务的累加时间  ->  结果 目标值 & 当前值，容量充足情况下的预估时间&实际时间 ）
            StringBuilder avgBuyTime = statisticsAverageBuyTime();

            // 统计集群资源数量：主机数量，CPU核数。
            StringBuilder clusterResource = statisticsClusterResource();

            // 执行任务失败数量
            StringBuilder failCount = getTaskFailPrometheusMetrics();

            // 保存进Redis
            putPrometheusMetricsToCache(buyFailureRate, evictRate, avgBuyTime, clusterResource, failCount);
        } catch(Exception ex) {
            logger.error("统计Prometheus指标数据时发生异常: " + ex.getMessage(), ex);
        } finally {
            redisLock.unlock(lockKey);
        }
        logger.info("统计Prometheus指标数据完成");
    }

    /**
     * 将4个指标保存到Redis中
     */
    void putPrometheusMetricsToCache(StringBuilder spotBuyFailureRate,
                                     StringBuilder evictRate,
                                     StringBuilder spotBuyAvgtime,
                                     StringBuilder clusterResource,
                                     StringBuilder failCount  ) {
        long start = System.currentTimeMillis();
        // 竞价实例组扩容·购买失败率
        redisLock.save(RedisConst.keySdpSpotBuyFailureRate(), spotBuyFailureRate.toString(), 5 * 60);

        // 竞价实例组正常运行逐出率
        redisLock.save(RedisConst.keySdpEvictRate(), evictRate.toString(), 5 * 60);

        // 竞价实例组平均购买时间
        redisLock.save(RedisConst.keySdpSpotBuyAvgtime(), spotBuyAvgtime.toString(), 5 * 60);

        // 集群资源数量
        redisLock.save(RedisConst.keySdpClusterResource(), clusterResource.toString(), 5 * 60);

        // 集群执行任务失败数量
        redisLock.save(RedisConst.keySdpTaskFailCount(), failCount.toString(), 5 * 60);

        logger.info("保存Prometheus指标数据到Redis中耗时：{}ms", System.currentTimeMillis() - start);
    }


    @Override
    public StringBuilder getTaskFailPrometheusMetrics() {
        long start = System.currentTimeMillis();
        List<Map> metricsList = taskEventMapper.statDestroyTaskFailEventCount();
        PrometheusMetrics metrics = PrometheusMetrics.newCounter("sdp_task_fail_count",
                "集群执行任务失败次数",
                Arrays.asList("clusterid", "clustername", "groupname", "eventtype", "purchasetype"));

        // 计平均购买时间，返回Map的字段如下：
        for (Map aMetrics : metricsList) {
            String clusterId = (String) aMetrics.get("cluster_id");
            String clusterName = (String) aMetrics.get("cluster_name");
            String groupName = (String) aMetrics.get("group_name");
            String eventType = (String) aMetrics.get("event_type");
            String purchaseType = (String) aMetrics.get("purchase_type");
            Long failCount = Convert.toLong(aMetrics.get("fail_count"));

            metrics.addMetrics("", Arrays.asList(clusterId, clusterName, groupName, eventType, purchaseType), failCount.doubleValue());
        }
        logger.info("统计任务失败事件耗时：{}ms", System.currentTimeMillis() - start);
        return metrics.toStringBuilder();
    }
}
