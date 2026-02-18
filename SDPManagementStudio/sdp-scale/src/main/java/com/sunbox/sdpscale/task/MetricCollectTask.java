package com.sunbox.sdpscale.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.sunbox.domain.ConfGroupElasticScalingRule;
import com.sunbox.domain.Metric;
import com.sunbox.domain.enums.AggregateType;
import com.sunbox.sdpscale.constant.ScaleConstant;
import com.sunbox.sdpscale.mapper.ConfGroupElasticScalingRuleMapper;
import com.sunbox.sdpscale.model.ClusterMetrics;
import com.sunbox.sdpscale.service.MetricService;
import com.sunbox.sdpscale.service.RuleComputeService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 弹性伸缩性能指标采集定时任务
 */
@Component
public class MetricCollectTask implements BaseCommonInterFace, ScaleConstant {
    @Autowired
    private ConfGroupElasticScalingRuleMapper scalingRuleMapper;

    @Autowired
    private RuleComputeService ruleComputeService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Value("${hadoop.jmx.api.port:8088}")
    private int hadoopJmxApiPort;

    @Value("${metric.collect.wait.time:60}")
    private int metricCollectWaitTime;

    private static List<ConfGroupElasticScalingRule> scalingRuleList;
    private static int serverCount;

    private static MetricRegistry registry = new MetricRegistry();

    private static Date lastLoadScalingRuleTime;

    /**
     * 弹性伸缩性能指标采集
     */
    @Scheduled(cron = "${metric.collect.task.time: 0 * * * * ? }")
    public void start() {
        getLogger().info("弹性伸缩性能指标采集轮询-开始");
        // 指标规则是否发生变化,如果变化了, 需要重新从数据库加载规则
        Boolean metricChange = false;
        Boolean serverCountChange = false;
        String metricChangeKey = StrUtil.EMPTY;
        Boolean tryLock = false;
        try {
            tryLock = redisLock.tryLock(metric_machine_check, TimeUnit.SECONDS, 200, 300);
            if (!tryLock)
                return;

            // 注册本机, 并加载
            ScaleContext scaleContext = ScaleContext.getInstance();
            String localIp = ScaleContext.ip;
            metricChangeKey = metric_change + "_" + localIp;

            // 检查当前主机是否注册成功, 如果没有注册成功, 退出任务: Scale启动时会自己注册
            int nowServerCount = scaleContext.scaleServerCount();
            Integer serverIndex = scaleContext.scaleServerIndex();
            if (serverIndex == null || nowServerCount == 0) {
                getLogger().error("{},服务注册失败,无法进行采集", localIp);
                return;
            }
            getLogger().info("弹性伸缩服务个数:{}", nowServerCount);
            getLogger().info("弹性伸缩服务历史个数,serverIndex:{},serverCount:{}", serverIndex, serverCount);
            if (nowServerCount != serverCount) {
                serverCountChange = true;
            }
            getLogger().info("Metrics个数:{}", CollUtil.size(registry.getMetrics()));
            serverCount = nowServerCount;
            metricChange = Convert.toBool(redisLock.getValue(metricChangeKey), false);
            if (CollectionUtils.isEmpty(scalingRuleList)
                    || metricChange
                    || serverCountChange
                    || needReloadScalingRule()) {
                if (CollectionUtils.isEmpty(scalingRuleList)
                        || metricChange
                        || needReloadScalingRule()) {
                    lastLoadScalingRuleTime = new Date();
                    scalingRuleList = scalingRuleMapper.selectDistinctValidRuleList(serverIndex, serverCount);
                    ruleComputeService.reloadScalingRule();
                }

                // 从数据库加载弹性扩缩容规则为空的话，说明弹性扩缩容规则都被删掉了，不需要对该集群采集指标了。
                if (CollUtil.isEmpty(scalingRuleList)) {
                    List<String> containValueList = redisLock.getContainValueList(lock_metrics, localIp);
                    redisLock.removeValueFromListByKeys(lock_metrics, localIp);
                    for (String s : containValueList) {
                        getLogger().info("停止弹性规则采集计算:{}", s);
                        List<String> split = StrUtil.split(s, "_");
                        String metricName = StrUtil.format("{}_{}_{}", split.get(1), split.get(2), split.get(3));
                        registry.remove(MetricRegistry.name(metricName));
                    }
                    getLogger().info("无可用的弹性伸缩规则,serverIndex:{},serverCount:{}", serverIndex, serverCount);
                    return;
                }
            }
            List<String> newMetricWindowList = scalingRuleList.stream().map(p -> p.getMetriWindowKey())
                    .collect(Collectors.toList());
            List<String> list = redisLock.getContainValueList(lock_metrics, localIp);
            list = list.stream().map(p -> {
                List<String> split = StrUtil.split(p, "_");
                return StrUtil.format("{}_{}_{}", split.get(1), split.get(2), split.get(3));
            }).collect(Collectors.toList());
            for (String oldMetric : list) {
                if (!newMetricWindowList.contains(oldMetric)) {
                    registry.remove(MetricRegistry.name(oldMetric));
                    getLogger().info("移除指标,{}", oldMetric);
                    redisLock.removeValueFromListByKeys(lock_metrics, localIp, oldMetric);
                }
            }
            for (ConfGroupElasticScalingRule scalingRule : scalingRuleList) {
                Long currentTime = DateUtil.current();
                String metricWindowKey = scalingRule.getMetriWindowKey();
                String newValue = localIp + "_" + metricWindowKey + "_" + currentTime;
                String metricWindowKeyLock = metricWindowKey + ":lock";
                Integer index = redisLock.listContainValue(lock_metrics, metricWindowKey);
                if (index != null) {
                    String oldValue = redisLock.listGetValue(lock_metrics, index);
                    List<String> split = StrUtil.split(oldValue, "_");
                    String last = CollUtil.getLast(split);
                    Long metricLiveTime = Convert.toLong(last);
                    getLogger().info("指标过期判断,currentTime={},metricLiveTime={},value={},clusterId:{},groupName:{}",
                            currentTime,
                            metricLiveTime,
                            oldValue,
                            scalingRule.getClusterId(),
                            scalingRule.getGroupName());
                    if (currentTime - metricLiveTime < TimeUnit.SECONDS.toMillis(metricCollectWaitTime)) {
                        if (oldValue.contains(localIp)) {
                            getLogger().info("更新指标时间,metricWindowKey={},timestamp={},clusterId:{},groupName:{}",
                                    metricWindowKey,
                                    currentTime,
                                    scalingRule.getClusterId(),
                                    scalingRule.getGroupName());
                            redisLock.listSetValue(lock_metrics, oldValue, newValue);
                        }
                        continue;
                    } else {
                        getLogger().info("指标超时，更新指标时间,metricWindowKey={},timestamp={},clusterId:{},groupName:{}",
                                metricWindowKey,
                                currentTime,
                                scalingRule.getClusterId(),
                                scalingRule.getGroupName());
                        redisLock.listSetValue(lock_metrics, oldValue, newValue);
                        getLogger().info("获取list信息:{},clusterId:{},groupName:{}",
                                redisLock.getList(lock_metrics),
                                scalingRule.getClusterId(),
                                scalingRule.getGroupName());
                    }
                } else {
                    boolean tryMetricWindowKeyLock = redisLock.tryLock(metricWindowKeyLock, TimeUnit.SECONDS, 200, 300);
                    if (!tryMetricWindowKeyLock) continue;
                    try {
                        index = redisLock.listContainValue(lock_metrics, metricWindowKey);
                        if (index != null) {
                            getLogger().info("指标已存在,metricWindowKey={},timestamp={},clusterId:{},groupName:{}",
                                    metricWindowKey,
                                    currentTime,
                                    scalingRule.getClusterId(),
                                    scalingRule.getGroupName());
                            continue;
                        }
                        getLogger().info("添加指标时间,metricWindowKey={},timestamp={},clusterId:{},groupName:{}",
                                metricWindowKey,
                                currentTime,
                                scalingRule.getClusterId(),
                                scalingRule.getGroupName());
                        redisLock.addList(lock_metrics, newValue);
                    } finally {
                        if (tryMetricWindowKeyLock) {
                            redisLock.unlock(metricWindowKeyLock);
                        }
                    }
                }
                getLogger().info("开始指标采集,clusterId={},meric={},clusterId:{},groupName:{}",
                        scalingRule.getLoadMetric(),
                        metricWindowKey,
                        scalingRule.getClusterId(),
                        scalingRule.getGroupName());
                if (registry.getNames().contains(MetricRegistry.name(metricWindowKey))) {
                    continue;
                }
                Histogram histogram = new Histogram(
                        new SlidingTimeWindowArrayReservoir(scalingRule.getWindowSize(), TimeUnit.MINUTES));
                registry.register(MetricRegistry.name(metricWindowKey), histogram);

                Thread collectThread = new Thread(() -> {
                    while (true) {
                        // 访问集群监控指标
                        if (isStop(metricWindowKey)) {
                            getLogger().info("停止规则指标采集,metricWindowKey:{},clusterId:{},groupName:{}",
                                    metricWindowKey,
                                    scalingRule.getClusterId(),
                                    scalingRule.getGroupName());
                            break;
                        }
                        ThreadUtil.sleep(15, TimeUnit.SECONDS);
                        // 访问集群监控指标
                        int collect = collect(scalingRule);
                        if (collect > 0) {
                            histogram.update(collect);
                        }
                    }
                });

                Thread computeThread = new Thread(() -> {
                    while (true) {
                        try {
                            if (isStop(metricWindowKey)) {
                                getLogger().info("停止规则指标计算,metricWindowKey:{},clusterId:{},groupName:{}",
                                        metricWindowKey,
                                        scalingRule.getClusterId(),
                                        scalingRule.getGroupName());
                                break;
                            }
                            ThreadUtil.sleep(scalingRule.getWindowSize(), TimeUnit.MINUTES);
                            List<Metric> metrics = new ArrayList<>();
                            Metric metricMin = new Metric();
                            metricMin.setMetricName(scalingRule.getLoadMetric());
                            metricMin.setClusterId(scalingRule.getClusterId());
                            metricMin.setAggregateType(AggregateType.MIN.typeText);
                            metricMin.setMetricValue(Convert.toBigDecimal(histogram.getSnapshot().getMin()));
                            metricMin.setWindowSize(scalingRule.getWindowSize());
                            metrics.add(metricMin);

                            Metric metricMedian = new Metric();
                            metricMedian.setMetricName(scalingRule.getLoadMetric());
                            metricMedian.setClusterId(scalingRule.getClusterId());
                            metricMedian.setAggregateType(AggregateType.AVG.typeText);
                            metricMedian.setMetricValue(Convert.toBigDecimal(histogram.getSnapshot().getMedian()));
                            metricMedian.setWindowSize(scalingRule.getWindowSize());
                            metrics.add(metricMedian);

                            Metric metricMax = new Metric();
                            metricMax.setMetricName(scalingRule.getLoadMetric());
                            metricMax.setClusterId(scalingRule.getClusterId());
                            metricMax.setAggregateType(AggregateType.MAX.typeText);
                            metricMax.setMetricValue(Convert.toBigDecimal(histogram.getSnapshot().getMax()));
                            metricMax.setWindowSize(scalingRule.getWindowSize());
                            metrics.add(metricMax);
                            getLogger().info("开始规则计算,metrics={},clusterId:{},groupName:{}",
                                    JSONUtil.toJsonStr(metrics),
                                    scalingRule.getClusterId(),
                                    scalingRule.getGroupName());
                            ruleComputeService.compute(metrics);
                            getLogger().info("指标计算完成,clusterId={},metricName={},groupName:{}",
                                    scalingRule.getClusterId(),
                                    scalingRule.getLoadMetric(),
                                    scalingRule.getGroupName());
                        } catch (Exception ex) {
                            getLogger().error("指标计算发生错误,clusterId={},metricName={},groupName:{}",
                                    scalingRule.getClusterId(),
                                    scalingRule.getLoadMetric(),
                                    scalingRule.getGroupName(), ex);
                        }
                    }
                });
                collectThread.start();
                computeThread.start();
            }
            getLogger().info("弹性伸缩性能指标采集轮询-完成");
        } catch (Exception ex) {
            getLogger().error("弹性伸缩性能指标采集轮询-异常", ex);
        } finally {
            if (tryLock) {
                redisLock.unlock(metric_machine_check);
            }
            if (metricChange && StrUtil.isNotEmpty(metricChangeKey)) {
                redisLock.delete(metricChangeKey);
            }
        }
    }

    private static boolean needReloadScalingRule() {
        if (lastLoadScalingRuleTime == null) {
            return true;
        }

        if (System.currentTimeMillis() - lastLoadScalingRuleTime.getTime() > 10 * 60 * 1000) {
            return true;
        }
        return false;
    }

    private boolean isStop(String metricWindowKey) {
        return !registry.getNames().contains(metricWindowKey);
    }

    /**
     * 从ResourceManager采集指标
     *
     * @param scalingRule 弹性扩缩容规则, 只是用来记录日志使用用
     * @return
     */
    private int collect(ConfGroupElasticScalingRule scalingRule) {
        getLogger().info("collect scalingRule:{}", scalingRule);
        boolean tryLock = false;
        String clusterId = scalingRule.getClusterId();
        String clusterIdKey = cluster_host_list_prefix + ":" + clusterId;
        String lockKey = clusterIdKey + ":lock";
        try {
            List<String> hostList;
            hostList = metricService.getResourceManagerHostNames(clusterId);
            if (hostList == null){
                getLogger().error("未查询到ResourceManager，集群可能已被销毁，ClusterId：{}",clusterId);
            }
            getLogger().info("集群地址信息,clusterId={},hostList={},groupName:{}", clusterId, hostList, scalingRule.getGroupName());
            // 循环遍历两个ResourceManager, 因为不一定哪个是Activate, 哪个是StandBy
            for (String host : hostList) {
                // 通过 /jmx 接口查询ResourceManager的监控指标
                // 查询条件使用 Hadoop:service=ResourceManager,name=QueueMetrics,q0=root, 查询queue的指标
                String jmx = StrUtil.format(
                        "http://{}:{}/jmx?qry=Hadoop:service=ResourceManager,name=QueueMetrics,q0=root",
                        host, hadoopJmxApiPort);
                getLogger().info("集群请求地址,clusterId={},jmx={},groupName:{}", clusterId, jmx, scalingRule.getGroupName());
                String strBeans = HttpUtil.get(jmx);
                JSONObject beans = JSON.parseObject(strBeans);
                JSONArray jsonArray = beans.getJSONArray("beans");
                getLogger().info("集群请求地址,clusterId={},返回json={},groupName:{}", clusterId, jsonArray.toString(), scalingRule.getGroupName());
                int metricValue = 0;
                if (jsonArray.size() > 0) {
                    ClusterMetrics clusterMetrics = jsonArray.getObject(0, ClusterMetrics.class);
                    clusterMetrics.setClusterId(clusterId);
                    Object invoke = ReflectUtil.invoke(metricService,
                            StrUtil.format("metric{}", scalingRule.getLoadMetric()), clusterMetrics);
                    metricValue = (int) invoke;
                }

                // 监控指标为0, 说明这台ResourceManager是Standby状态,需要继续从下一台ResourceManager上取监控指标
                if (metricValue == 0) {
                    getLogger().info("采集数据为0，有可能发生主备切换，换一下ResourceManager主机重新获取监控指标。[clusterId={},metric={},windowSize={},groupName:{}]",
                            scalingRule.getClusterId(),
                            scalingRule.getLoadMetric(),
                            scalingRule.getWindowSize(),
                            scalingRule.getGroupName());
                    continue;
                }
                return metricValue;
            }
        } catch (Exception ex) {
            getLogger().error("集群监控数据采集异常,clusterId={},metric={},windowSize={},groupName:{}",
                    scalingRule.getClusterId(),
                    scalingRule.getLoadMetric(),
                    scalingRule.getWindowSize(),
                    scalingRule.getGroupName(),
                    ex);
        } finally {
            if (tryLock) {
                redisLock.unlock(lockKey);
            }
        }
        return 0;
    }
}
