package com.sunbox.sdpscale.task;


import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.sunbox.dao.mapper.BizConfigMapper;
import com.sunbox.dao.mapper.ConfGroupElasticScalingMapper;
import com.sunbox.dao.mapper.InfoClusterVmNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpscale.mapper.*;
import com.sunbox.sdpscale.model.ClusterMetrics;
import com.sunbox.sdpscale.model.ClusterMetricsSnapshot;
import com.sunbox.sdpscale.service.MetricService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.IPUtils;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 全托管弹性扩缩容任务 <p/>
 * 1. 采集Yarn监控数据 <br/>
 * 2. 计算监控指标 <br/>
 * 3. 计算扩缩容 <br/>
 * 4. 启动扩缩容 <br/>
 */
@Component
public class FullCustodyTask implements BaseCommonInterFace {
    public static final String SCALING_TYPE_NONE = "无";
    public static final String SCALING_TYPE_SCALEOUT = "扩容";
    public static final String SCALING_TYPE_SCALEIN = "缩容";
    public static final String METRIC_APPS_PENDING = "AppsPending";
    public static final String METRIC_APPS_RUNNING = "AppsRunning";
    public static final String METRIC_ALLOCATED_CONTAINERS = "AllocatedContainers";
    public static final String METRIC_PENDING_CONTAINERS = "PendingContainers";
    public static final String METRIC_AVAILABLE_MB = "AvailableMB";
    public static final String METRIC_ALLOCATED_MB = "AllocatedMB";
    public static final String METRIC_PENDING_MB = "PendingMB";
//    public static final String SCALE_HOSTING_WINDOW_SIZE = "scale.hosting.window.size";
//    public static final String SCALE_HOSTING_WINDOW_COUNT = "scale.hosting.window.count";
//    public static final String SCALE_HOSTING_FREEZE_TIME = "scale.hosting.freeze.time";
//    public static final String SCALE_HOSTING_SCALEOUT_METRIC = "scale.hosting.scaleout.metric";
//    public static final String SCALE_HOSTING_SCALEIN_MEMORY_THRESHOLD = "scale.hosting.scalein.memory.threshold";
    public static final String BIZ_CATEGORY = "托管扩缩容配置";
    public static List<String> metricNames = Arrays.asList(METRIC_APPS_PENDING,
            METRIC_APPS_RUNNING,
            METRIC_ALLOCATED_CONTAINERS,
            METRIC_PENDING_CONTAINERS,
            METRIC_AVAILABLE_MB,
            METRIC_ALLOCATED_MB,
            METRIC_PENDING_MB);
//    private final Map<String,String> configCache = new ConcurrentHashMap<>();
    private final Cache<String, String> configCache = CacheUtil.newTimedCache(5 * 60 * 1000);
    private final ScheduledThreadPoolExecutor taskPool = new ScheduledThreadPoolExecutor(5);
    public boolean isLocked = false;
    @Value("${automatic.metric.collect.interval:15}")
    private int automaticMetricCollectInterval;
//    @Value("${automatic.metric.compute.interval:300}")
//    private int automaticMetricComputeInterval;
    @Value("${hadoop.jmx.api.port:8088}")
    private int hadoopJmxApiPort;
    @Autowired
    private DistributedRedisLock redisLock;
    @Value("${automatic.memory.weight:0}")
    private Double memoryWeight;
    @Value("${automatic.allow.pending.containers.size:0}")
    private Integer allowPendingContainersSize;
    /**
     * 保护期，单位分钟
     */
//    @Value("${automatic.protection.period:20}")
//    private long protectionPeriod;


    @Autowired
    private InfoGroupFullCustodyElasticScalingLogMapper fullCustodyElasticScalingLogMapper;
    @Autowired
    private BizConfigMapper bizConfigMapper;
    @Autowired
    private ConfGroupElasticScalingMapper confGroupElasticScalingMapper;
    @Autowired
    private MetricService metricService;
    @Autowired
    private ComposeService composeService;
    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmNeoMapper;
    private List<AutomaticMetricCollectTask> automaticMetricCollectTasks = new ArrayList<>();

    /** 配置缓存失效的时间点 */
    private Date configCacheExpiredTime = new Date();
    /**
     * 指标存储窗口时长单位 秒
     */
//    @Value("${automatic.window.duration:300}")
//    private Integer windowsDuration;
//    @Value("${automatic.allow.memory.available.percentage:35}")
//    private Double allowMemoryAvailablePercentage;

    {
        taskPool.setRemoveOnCancelPolicy(true);
    }

    private String getConfig(String key){
        String val = configCache.get(key);
        if (StrUtil.isNotBlank(val)) {
            return val;
        }

        List<BizConfig> bizConfigs = bizConfigMapper.selectByCategoryAndKey(BIZ_CATEGORY, key);
        if (CollUtil.isEmpty(bizConfigs) ){
            getLogger().error("业务配置不存在， category: 托管扩缩容配置 key:{}",key);
            throw new RuntimeException("业务配置不存在， category: 托管扩缩容配置 key: "+key);
        }
        if (bizConfigs.size() > 1){
            getLogger().error("业务配置重复， category: 托管扩缩容配置 key:{}",key);
            throw new RuntimeException("业务配置不重复， category: 托管扩缩容配置 key: "+key);
        }
        String cfgValue = bizConfigs.get(0).getCfgValue();
        configCache.put(key,cfgValue);
        return cfgValue;
    }

    private Integer getIntegerConfig(String key){
        return Convert.toInt(getConfig(key));
    }

    private Double getDoubleConfig(String key){
        return Convert.toDouble(getConfig(key));
    }

    /**
     * 获取窗口时间 单位分钟
     * @return
     */
    public Integer getScaleHostingWindowSize(){
        return getIntegerConfig(FullCustodyParam.SCALE_HOSTING_WINDOW_SIZE);
    }

    /**
     * 获取窗口数量
     * @return
     */
    public Integer getScaleHostingWindowsCount(){
        return getIntegerConfig(FullCustodyParam.SCALE_HOSTING_WINDOW_COUNT);
    }

    /**
     * 获取保护期 单位分钟
     * @return
     */
    private Integer getScaleHostingFreezeTime(){
        return getIntegerConfig(FullCustodyParam.SCALE_HOSTING_FREEZE_TIME);
    }

    /**
     * 获取缩容内存阀值，已用内存/（可用+已用内存）小于此值开始缩容 单位百分比
     * @return
     */
    private Integer getScaleHostingScaleinMemoryThreshold(){
        return getIntegerConfig(FullCustodyParam.SCALE_HOSTING_SCALEIN_MEMORY_THRESHOLD);
    }

    /**
     * 获取apps权重
     * @return
     */
    private Double getAppsWeight(){
        String config = getConfig(FullCustodyParam.SCALE_HOSTING_SCALEOUT_METRIC);
        if (Objects.equals("App",config)){
            return 1.0;
        }else{
            return 0.0;
        }
    }

    /**
     * 获取容器权重
     * @return
     */
    private Double getContainersWeight(){
        String config = getConfig(FullCustodyParam.SCALE_HOSTING_SCALEOUT_METRIC);
        if (Objects.equals("Container",config)){
            return 1.0;
        }else{
            return 0.0;
        }
    }

    @Scheduled(cron = "${metric.collect.task.time: 0 * * * * ? }")
    public void start(){

        if (isLocked || (isLocked = redisLock.tryLock("full_custody_task_lock"))) {
            getLogger().info("全托管获取锁成功ip:{}", IPUtils.getIpAddress());
            List<ConfGroupElasticScaling> confGroupElasticScalings = confGroupElasticScalingMapper.listByValid();
            List<ConfGroupElasticScaling> elasticScalings = confGroupElasticScalings.stream().filter(x -> Objects.equals(1, x.getIsFullCustody())).collect(Collectors.toList());
            automatic(elasticScalings);
        }else {
            automatic(new ArrayList<>());
        }
    }
    public void automatic(List<ConfGroupElasticScaling> elasticScalings){
        List<AutomaticMetricCollectTask> newTasks = elasticScalings.stream().map(scaling->new AutomaticMetricCollectTask(scaling,this)).collect(Collectors.toList());
        List<AutomaticMetricCollectTask> disabledTasks = automaticMetricCollectTasks.stream().filter(task -> !newTasks.contains(task)).collect(Collectors.toList());
        //删除关闭的任务
        for (AutomaticMetricCollectTask disabledTask : disabledTasks) {
            getLogger().info("删除全托管任务: clusterId:{} groupName:{}",disabledTask.getScaling().getClusterId(),disabledTask.getScaling().getGroupName());
            automaticMetricCollectTasks.remove(disabledTask);

            if (!disabledTask.cancel()) {
                getLogger().error("删除全托管收集任务失败 clusterId:{} groupName:{}",disabledTask.getScaling().getClusterId(),disabledTask.getScaling().getGroupName());
            }
            if (!disabledTask.getComputeTask().cancel()) {
                getLogger().error("删除全托管计算任务失败 clusterId:{} groupName:{}",disabledTask.getScaling().getClusterId(),disabledTask.getScaling().getGroupName());

            }
        }
        //添加新任务
        List<AutomaticMetricCollectTask> addTasks = newTasks.stream().filter(task -> !automaticMetricCollectTasks.contains(task)).collect(Collectors.toList());
        for (AutomaticMetricCollectTask addTask : addTasks) {
            getLogger().info("添加全托管任务: clusterId:{} groupName:{}",addTask.getScaling().getClusterId(),addTask.getScaling().getGroupName());
            ScheduledFuture<?> collectFuture = taskPool.scheduleAtFixedRate(addTask, 5, automaticMetricCollectInterval, TimeUnit.SECONDS);
            addTask.setCollectFuture(collectFuture);
            ThreadUtil.safeSleep(20);
            ScheduledFuture<?> computeFuture = taskPool.scheduleAtFixedRate(addTask.getComputeTask(), getScaleHostingWindowSize() * 60, getScaleHostingWindowSize() * 60, TimeUnit.SECONDS);
            addTask.getComputeTask().setComputeFuture(computeFuture);
            ThreadUtil.safeSleep(30);
            automaticMetricCollectTasks.add(addTask);
        }
    }

    /**
     * 扩缩容计算
     * @param collectTask
     * @param clusterMetricsSnapshot
     * @param customParam 自定义全托管参数
     * @return 扩缩容比率, 正值扩容，负值缩容
     */
    public double automaticMetricCompute(AutomaticMetricCollectTask collectTask,
                                         ClusterMetricsSnapshot clusterMetricsSnapshot,
                                         FullCustodyParam customParam) {
        String scaleoutMetric = getConfig(FullCustodyParam.SCALE_HOSTING_SCALEOUT_METRIC);
        if (StrUtil.isNotBlank(customParam.getScaleoutMetric())) {
            scaleoutMetric = customParam.getScaleoutMetric();
        }
        double value = 0D;
        if (StrUtil.equalsIgnoreCase(scaleoutMetric, "App")) {
            value = clusterMetricsSnapshot.getAppPendingRatio();
        } else if (StrUtil.equalsIgnoreCase(scaleoutMetric, "Container")) {
            value = clusterMetricsSnapshot.getContainerPendingRatio();
        }
//        double apps = clusterMetricsSnapshot.getAppPendingRatio()  * getAppsWeight();
//        double containers = clusterMetricsSnapshot.getContainerPendingRatio() * getContainersWeight();
//        double memory = clusterMetricsSnapshot.getPendingMb().getMean() / clusterMetricsSnapshot.getAllcatedMb().getMean() * memoryWeight;
        // 检查PendingContainer的数量, 对于没有Pending Container的情况, 不进行扩容
        if (clusterMetricsSnapshot.getPendingContainers().getMean() <= allowPendingContainersSize){
            value = 0;
        }
        ConfGroupElasticScaling scaling = collectTask.getScaling();
        getLogger().info("全托管【扩容】判断,clusterId:{} groupName:{} value:{}",scaling.getClusterId(),scaling.getGroupName(),value);
        if (value > 0){
            return value;
        }

        // 计算内存比率
        // 内存使用比率, 与下面计算得到的可用率要区分一下.
        Integer memoryUsedRatioThreshold = getScaleHostingScaleinMemoryThreshold();
        if (customParam.isSetScaleinMemoryThreshold()) {
            memoryUsedRatioThreshold = customParam.getScaleinMemoryThreshold();
        }
        
        double memoryAvailable = clusterMetricsSnapshot.getAvaliableMemoryRatio();
        getLogger().info("全托管【缩容】判断,clusterId:{} groupName:{} 可用内存比例value:{}",
                scaling.getClusterId(),scaling.getGroupName(),memoryAvailable);
        if (memoryAvailable * 100 <= 100 - memoryUsedRatioThreshold){
            memoryAvailable = 0;
        }
//        getLogger().info("全托管【缩容】判断,clusterId:{} groupName:{} 可用内存比例value:{}",scaling.getClusterId(),scaling.getGroupName(),-memoryAvailable);
        return -memoryAvailable;
    }

    /**
     * 全托管扩缩容任务主方法.
     * @param task 任务需要的参数都在这个对象中
     */
    private void  fullCustodyCompute(AutomaticMetricCollectTask task){
        ConfGroupElasticScaling scalingRule = task.getScaling();
        if (task.initTime > System.currentTimeMillis() - getScaleHostingWindowsCount() * getScaleHostingWindowSize() * 60_000L){
            getLogger().info("全托管窗口周期和窗口数量未满足 clusterId:{}  groupName:{}",scalingRule.getClusterId(),scalingRule.getGroupName());
            return;
        }

        ClusterMetricsSnapshot clusterMetricsSnapshot = new ClusterMetricsSnapshot(task.getRegistry());
        getLogger().info("扩缩容全托管指标计算 clusterId:{}  groupName:{} appsPendingMean:{} appsRunningMean:{} pendingContainersMean:{}" +
                        " allocatedContainersMean:{} allocatedMbMean:{} availableMbMean:{} pendingMbMean:{}",
                scalingRule.getClusterId(),scalingRule.getGroupName(),clusterMetricsSnapshot.getAppsPending().getMean(),
                clusterMetricsSnapshot.getAppsRunning().getMean(),clusterMetricsSnapshot.getPendingContainers().getMean(),
                clusterMetricsSnapshot.getAllocatedContainers().getMean(),clusterMetricsSnapshot.getAllcatedMb().getMean(),
                clusterMetricsSnapshot.getAvailableMb().getMean(),clusterMetricsSnapshot.getPendingMb().getMean());

        InfoGroupFullCustodyElasticScalingLog infoGroupFullCustodyElasticScalingLog = new InfoGroupFullCustodyElasticScalingLog();
        infoGroupFullCustodyElasticScalingLog.setClusterId(scalingRule.getClusterId());
        infoGroupFullCustodyElasticScalingLog.setGroupName(scalingRule.getGroupName());
        infoGroupFullCustodyElasticScalingLog.setCreatedTime(new Date());
        infoGroupFullCustodyElasticScalingLog.setScalingType(SCALING_TYPE_NONE);
        infoGroupFullCustodyElasticScalingLog.setMetricValues(clusterMetricsSnapshot.toMeanJsonValues());
        infoGroupFullCustodyElasticScalingLog.setIsStartScaling(0);

        //判断是否扩缩容，计算比例
        ConfGroupElasticScaling latestScalingObj = confGroupElasticScalingMapper.selectByPrimaryKey(scalingRule.getGroupEsId());
        FullCustodyParam customParam = latestScalingObj.getFullCustodyParamObject();
        double value = automaticMetricCompute(task, clusterMetricsSnapshot, customParam);
        boolean isScaleout = value > 0;
        boolean isScalein = value < 0;

        if (isScaleout){
            infoGroupFullCustodyElasticScalingLog.setScalingType(SCALING_TYPE_SCALEOUT);
        }
        if (isScalein){
            infoGroupFullCustodyElasticScalingLog.setScalingType(SCALING_TYPE_SCALEIN);
        }
        infoGroupFullCustodyElasticScalingLog.setComputeValue(value);
        boolean inProtectionPeriod = System.currentTimeMillis() - task.lastTime < getScaleHostingFreezeTime() * 60_000L;
        if (inProtectionPeriod){
            infoGroupFullCustodyElasticScalingLog.setTaskResult("0");
            infoGroupFullCustodyElasticScalingLog.setTaskResultMessage("保护期内不触发扩缩容");
        }
        //保存记录
        fullCustodyElasticScalingLogMapper.insertSelective(infoGroupFullCustodyElasticScalingLog);
        if (Math.abs(value) < 0.0001){
            //未触发扩缩容
            return;
        }
        if (inProtectionPeriod){
            getLogger().info("保护期内不触发扩缩容 clusterId:{} groupName:{} lastTime:{}",scalingRule.getClusterId(),scalingRule.getGroupName(),task.lastTime);
            return;
        }

        ConfScalingTask confScalingTask = new ConfScalingTask();
//            confScalingTask.setEsRuleId(scalingRule.getEsRuleId());
        confScalingTask.setClusterId(scalingRule.getClusterId());
//            confScalingTask.setEsRuleName(scalingRule.getEsRuleName());
        confScalingTask.setGroupName(scalingRule.getGroupName());
        confScalingTask.setVmRole(StringUtils.lowerCase(latestScalingObj.getVmRole()));
        confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_Scaling);
        confScalingTask.setIsGracefulScalein(scalingRule.getIsGracefulScalein());
        confScalingTask.setScaleinWaitingtime(scalingRule.getScaleinWaitingTime());
        confScalingTask.setEnableBeforestartScript(scalingRule.getEnableBeforestartScript());
        confScalingTask.setEnableAfterstartScript(scalingRule.getEnableAfterstartScript());
        confScalingTask.setMaxCount(scalingRule.getMaxCount());
        confScalingTask.setMinCount(scalingRule.getMinCount());
        confScalingTask.setTaskId(UUID.randomUUID().toString());


        List<InfoClusterVm> vmCountByState = infoClusterVmNeoMapper.getVMCountByStateGroupByRole(scalingRule.getClusterId(), 1);
        int coreCount = 0;
        int taskCount = 0;
        int currentTaskCount = 0;
        for (InfoClusterVm infoClusterVm : vmCountByState) {
            if ("task".equalsIgnoreCase(infoClusterVm.getVmRole())){
                taskCount += infoClusterVm.getCnt();
            }
            if ("core".equalsIgnoreCase(infoClusterVm.getVmRole())){
                coreCount += infoClusterVm.getCnt();
            }
            if (Objects.equals(scalingRule.getGroupName(),infoClusterVm.getGroupName())){
                currentTaskCount = infoClusterVm.getCnt();
            }
        }
        getLogger().info("获取集群vm数量 clusterId:{} coreCount:{} taskCount:{} cureentGroupCount:{}",
                scalingRule.getClusterId(),
                coreCount,
                taskCount,
                currentTaskCount);
        double ratioValue = Math.abs(value);
        int scalingCount = (int)(coreCount * ratioValue * 0.6 + taskCount * ratioValue) ;

        ResultMsg result = ResultMsg.FAILURE("未触发");
        if (isScaleout){
            if (scalingCount  > scalingRule.getMaxCount() - currentTaskCount){
                getLogger().info("全托管[弹性扩容]数量大于实例组最大VM数量限制: Max:{}, Current:{}, ScaleCount:{}, Adjust: {}",
                        scalingRule.getMaxCount(), currentTaskCount, scalingCount, (scalingRule.getMaxCount() - currentTaskCount));
                scalingCount = scalingRule.getMaxCount() - currentTaskCount;
            }
            if (scalingCount > 0 ) {
                confScalingTask.setScalingCount(scalingCount);
                confScalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
                getLogger().info("全托管创建[扩容]任务clusterId:{} taskId:{}", scalingRule.getClusterId(), confScalingTask.getTaskId());

                result = composeService.createScaleOutTask(confScalingTask);
            }else {
                result = ResultMsg.FAILURE("计算后扩容数量小于0:" + scalingCount);
            }
        }
        if (isScalein){
            if (currentTaskCount - scalingCount < scalingRule.getMinCount() ){
                scalingCount = currentTaskCount - scalingRule.getMinCount();
            }
            if (scalingCount > 0) {
                confScalingTask.setScalingCount(scalingCount);
                confScalingTask.setScalingType(ConfScalingTask.ScaleType_IN);
                getLogger().info("全托管创建[缩容]任务clusterId:{} taskId:{}", scalingRule.getClusterId(), confScalingTask.getTaskId());
                result = composeService.createScaleInTask(confScalingTask);
            }else {
                result = ResultMsg.FAILURE("计算后缩容数量小于0:" + scalingCount);
            }
        }

        String paramStr = getActualFullCustodyParam(customParam);
        getLogger().info("全托管创建扩缩容任务结果 esFullLogId:{} clusterId;{} groupName:{} result:{} msg:{}",infoGroupFullCustodyElasticScalingLog.getEsFullLogId(),scalingRule.getClusterId(),scalingRule.getGroupName(),result.getResult(),result.getMsg());
        InfoGroupFullCustodyElasticScalingLog fullCustodyElasticScalingLog = new InfoGroupFullCustodyElasticScalingLog();
        fullCustodyElasticScalingLog.setEsFullLogId(infoGroupFullCustodyElasticScalingLog.getEsFullLogId());
        fullCustodyElasticScalingLog.setTaskId(confScalingTask.getTaskId());
        fullCustodyElasticScalingLog.setTaskResult(Boolean.toString(result.getResult()));
        fullCustodyElasticScalingLog.setTaskResultMessage(ObjectUtil.defaultIfNull(result.getMsg(), "") + paramStr);
        fullCustodyElasticScalingLog.setScalingCount(scalingCount);
        fullCustodyElasticScalingLog.setIsStartScaling(scalingCount > 0 ? 1:0);
        fullCustodyElasticScalingLogMapper.update(fullCustodyElasticScalingLog);
        if (result.getResult()){
            task.lastTime = System.currentTimeMillis();
        }
        getLogger().info("全托管计算任务完成 clusterId;{} groupName:{} ",scalingRule.getClusterId(),scalingRule.getGroupName());
    }

    private String getActualFullCustodyParam(FullCustodyParam customParam) {
        String result = "";
        if (Objects.isNull(customParam)) {
            return result;
        }

        // 查询扩容指标名称
        String scaleoutMetric = getConfig(FullCustodyParam.SCALE_HOSTING_SCALEOUT_METRIC);
        if (StrUtil.isNotBlank(customParam.getScaleoutMetric())) {
            scaleoutMetric = customParam.getScaleoutMetric();
        }
        result = " metric:" + scaleoutMetric;

        // 查询缩容内存阈值
        if (customParam.isSetScaleinMemoryThreshold()){
            result += " memRatio:" + customParam.getScaleinMemoryThreshold();
        } else {
            result += " memRatio:" + getConfig(FullCustodyParam.SCALE_HOSTING_SCALEIN_MEMORY_THRESHOLD);
        }
        return result;
    }


    private ClusterMetrics collectMetrics(ConfGroupElasticScaling scaling) {
        String clusterId = scaling.getClusterId();
        try{
            List<String> hostList = metricService.getResourceManagerHostNames(clusterId);
            if (hostList == null) {
                getLogger().error("未查询到ResourceManager，集群可能已被销毁，ClusterId：{}", clusterId);
                return null;
            }
            getLogger().info("集群地址信息,clusterId={},hostList={},groupName:{}", clusterId, hostList, scaling.getGroupName());
            // 循环遍历两个ResourceManager, 因为不一定哪个是Activate, 哪个是StandBy
            for (String host : hostList) {
                // 通过 /jmx 接口查询ResourceManager的监控指标
                // 查询条件使用 Hadoop:service=ResourceManager,name=QueueMetrics,q0=root, 查询queue的指标

                String data = HttpUtil.get(StrUtil.format("http://{}:{}/cluster", host, hadoopJmxApiPort));
                if (data != null && data.contains("This is standby RM")){
                    continue;
                }
                String jmx = StrUtil.format(
                        "http://{}:{}/jmx?qry=Hadoop:service=ResourceManager,name=QueueMetrics,q0=root",
                        host, hadoopJmxApiPort);
                getLogger().info("集群请求地址,clusterId={},jmx={}, groupName:{}", clusterId, jmx, scaling.getGroupName());
                String strBeans = HttpUtil.get(jmx);
                JSONObject beans = JSON.parseObject(strBeans);
                JSONArray jsonArray = beans.getJSONArray("beans");
                getLogger().info("集群请求地址,clusterId={},返回json={}, groupName:{}", clusterId, jsonArray.toString(), scaling.getGroupName());
                if (!jsonArray.isEmpty()) {
                    ClusterMetrics clusterMetrics = jsonArray.getObject(0, ClusterMetrics.class);
                    clusterMetrics.setClusterId(clusterId);
                    return clusterMetrics;
                }
            }
        }
        catch (Exception ex){
            getLogger().error("集群监控数据采集异常,clusterId={},groupName:{}",
                    scaling.getClusterId(),
                    scaling.getGroupName(),
                    ex);
        }
        return null;
    }

    /**
     * 从Yarn采集监控指标的线程. <br/>
     * 此任务只进行监控指标采集,并保存指标值. <br/>
     * 计算,判断等其他操作都在 <code>AutomaticMetricComputeTask</code> 类完成
     */
    public static class AutomaticMetricCollectTask implements Runnable,BaseCommonInterFace{
        private final AutomaticMetricComputeTask computeTask;
        MetricRegistry registry = new MetricRegistry();
        private ScheduledFuture collectFuture;
        private ConfGroupElasticScaling scaling;
        private FullCustodyTask fullCustodyTask;
        private long lastTime;
        private long initTime;
        public AutomaticMetricCollectTask(ConfGroupElasticScaling scaling, FullCustodyTask fullCustodyTask) {
            this.scaling = scaling;
            this.fullCustodyTask = fullCustodyTask;
            Histogram histogram = null;
            initTime = System.currentTimeMillis();
            // 初始化完成后, 不能修改时间窗口信息
            for (String metricName : metricNames) {
                histogram = new Histogram(
                        new SlidingTimeWindowArrayReservoir(fullCustodyTask.getScaleHostingWindowSize() * fullCustodyTask.getScaleHostingWindowsCount() * 60L , TimeUnit.SECONDS));
                registry.register(metricName,histogram);
            }
            computeTask = new AutomaticMetricComputeTask(this,fullCustodyTask);
        }

        @Override
        public void run() {
            try {
                if (getCollectFuture().isCancelled()){
                    getLogger().error("出错了，收集任务已被取消还在执行 clusterId:{} groupName:{}",getScaling().getClusterId(),getScaling().getGroupName());
                    return;
                }
                ClusterMetrics clusterMetrics = fullCustodyTask.collectMetrics(scaling);
                for (String metricName : metricNames) {
                    Histogram histogram = registry.histogram(metricName);
                    Object fieldValue = ReflectUtil.getFieldValue(clusterMetrics, metricName);
                    histogram.update(Convert.toLong(fieldValue, 0L));
                }
            }catch (Throwable t){
                getLogger().error("获取集群监控指标失败clusterId:{}",scaling.getClusterId(),t);
            }
        }
        public boolean cancel(){
            return getCollectFuture().cancel(true);
        }

        public ConfGroupElasticScaling getScaling() {
            return scaling;
        }

        public  AutomaticMetricComputeTask getComputeTask() {

            return computeTask;
        }

        public MetricRegistry getRegistry() {
            return registry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AutomaticMetricCollectTask that = (AutomaticMetricCollectTask) o;
            return Objects.equals(scaling.getClusterId(), that.scaling.getClusterId()) &&
                    Objects.equals(scaling.getGroupName(), that.scaling.getGroupName()) &&
                    Objects.equals(scaling.getMinCount(), that.scaling.getMinCount()) &&
                    Objects.equals(scaling.getMaxCount(),that.scaling.getMaxCount());
        }

        @Override
        public int hashCode() {
            return Objects.hash(scaling.getClusterId(),scaling.getGroupName(),scaling.getMinCount(),scaling.getMaxCount());
        }

        public ScheduledFuture getCollectFuture() {
            return collectFuture;
        }

        public void setCollectFuture(ScheduledFuture collectFuture) {
            this.collectFuture = collectFuture;
        }
    }

    /**
     * 此线程调用<code>FullCustodyTask</code>完成指标计算, 并根据结果生成扩缩容<br/>
     * 此线程只是调度线程,具体执行都在<code>FullCustodyTask</code>中完成.
     */
    public static class AutomaticMetricComputeTask implements Runnable,BaseCommonInterFace{
        private ScheduledFuture computeFuture;
        private AutomaticMetricCollectTask task;
        private FullCustodyTask fullCustodyTask;

        public AutomaticMetricComputeTask(AutomaticMetricCollectTask task, FullCustodyTask fullCustodyTask) {
            this.task = task;
            this.fullCustodyTask = fullCustodyTask;
        }

        @Override
        public void run() {

            try {
                if (getComputeFuture().isCancelled()){
                    getLogger().error("出错了，计算任务已被取消还在执行 clusterId:{} groupName:{}",task.getScaling().getClusterId(),task.getScaling().getGroupName());
                    return;
                }
                fullCustodyTask.fullCustodyCompute(task);
            }catch (Throwable t){
                getLogger().error("全托管计算扩缩容失败clusterId:{}",task.getScaling().getClusterId(),t);
            }
        }

        public boolean cancel(){
            return getComputeFuture().cancel(true);
        }
        public ScheduledFuture getComputeFuture() {
            return computeFuture;
        }

        public void setComputeFuture(ScheduledFuture computeFuture) {
            this.computeFuture = computeFuture;
        }
    }


    public static class Main implements Runnable{
        public static void main(String[] args) throws InterruptedException {
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
            System.out.println(scheduledThreadPoolExecutor.getRemoveOnCancelPolicy());
            scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
            Main main = new Main();
            ScheduledFuture<?> scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(main, 1, 1, TimeUnit.SECONDS);

            Thread.sleep(5000);
            boolean cancel = scheduledFuture.cancel(false);
            System.out.println(cancel);
            Thread.sleep(3000);
            System.out.println(scheduledThreadPoolExecutor.getQueue().size());
            scheduledThreadPoolExecutor.shutdown();
        }

        @Override
        public void run() {
            System.out.println("1");
        }
    }

}


