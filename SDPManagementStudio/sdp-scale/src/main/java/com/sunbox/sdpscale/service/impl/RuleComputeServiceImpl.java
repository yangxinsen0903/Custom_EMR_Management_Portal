package com.sunbox.sdpscale.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sunbox.dao.mapper.ConfGroupElasticScalingMapper;
import com.sunbox.dao.mapper.ConfScalingTaskNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpscale.task.ScaleContext;
import com.sunbox.sdpscale.constant.ScaleConstant;
import com.sunbox.sdpscale.enums.EnumOperator;
import com.sunbox.sdpscale.mapper.*;
import com.sunbox.sdpscale.service.RuleComputeConstant;
import com.sunbox.sdpscale.service.RuleComputeService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : RuleComputeServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/15 12:01 PM]
 */
@Service
public class RuleComputeServiceImpl implements RuleComputeService, BaseCommonInterFace, RuleComputeConstant, ScaleConstant {
    @Autowired
    private ConfGroupElasticScalingRuleMapper elasticScalingRuleMapper;

    @Autowired
    private InfoGroupElasticScalingRuleLogMapper elasticScalingRuleLogMapper;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    private ConfGroupElasticScalingMapper confGroupElasticScalingMapper;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    private static List<ConfGroupElasticScalingRule> allScalingRuleList;

    private static ReentrantLock lock = new ReentrantLock(false);

    @Override
    public ResultMsg reloadScalingRule() {
        getLogger().info("reloadScalingRule begin");
        ResultMsg resultMsg = new ResultMsg();
        try {
            lock.lock();
            allScalingRuleList = elasticScalingRuleMapper.selectValidRuleList();
            getLogger().info("reloadScalingRule end");
            lock.unlock();
        } catch (Exception ex) {
            getLogger().error("指标计算异常", ex);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ex.getMessage());
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        return resultMsg;
    }

    @Override
    public ResultMsg compute(List<Metric> metricList) {
        getLogger().info("指标计算开始,metrics={}", metricList);
        ResultMsg resultMsg = new ResultMsg();
        try {
            if (CollectionUtils.isEmpty(metricList)) {
                throw new Exception("指标数据为空");
            }
            if (allScalingRuleList == null) {
                throw new Exception("规则数据为空");
            }

            Map<String, List<ConfGroupElasticScalingRule>> scalingRuleGroupMap = allScalingRuleList.stream().collect(Collectors.groupingBy(ConfGroupElasticScalingRule::getGroupEsId));
            Iterator<Map.Entry<String, List<ConfGroupElasticScalingRule>>> iteScalingRuleGroup = scalingRuleGroupMap.entrySet().iterator();
            while (iteScalingRuleGroup.hasNext()) {
                Map.Entry<String, List<ConfGroupElasticScalingRule>> scalingGroup = iteScalingRuleGroup.next();
                List<ConfGroupElasticScalingRule> scalingRuleList = scalingGroup.getValue();
                for (ConfGroupElasticScalingRule scalingRule : scalingRuleList) {
                    Optional<Metric> optionalMetric = metricList.stream().filter(metric ->
                            Objects.equals(metric.getMetricName(), scalingRule.getLoadMetric()) &&
                                    Objects.equals(metric.getAggregateType(), scalingRule.getAggregateType()) &&
                                    Objects.equals(metric.getWindowSize(), scalingRule.getWindowSize()) &&
                                    Objects.equals(metric.getClusterId(), scalingRule.getClusterId())).findFirst();
                    if (!optionalMetric.isPresent()) continue;

                    Metric metric = optionalMetric.get();
                    InfoGroupElasticScalingRuleLog scalingRuleLog = computeMetric(metric, scalingRule);
                    if (Objects.equals(scalingRuleLog.getComputeResult(), rule_validate_pass)) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().error("指标计算异常", ex);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ex.getMessage());
        }
        getLogger().info("指标计算完成");
        return resultMsg;
    }

    @Override
    public ResultMsg metricChangeNotify() {
        getLogger().info("弹性伸缩规则变更通知-开始");
        ResultMsg resultMsg = new ResultMsg();
        List<String> list = redisLock.getList(metric_machine_ips);
        for (String s : list) {
            List<String> split = StrUtil.split(s, "_");
            String scaleServerIp = split.get(0);
            redisLock.save(metric_change + "_" + scaleServerIp, "true");
        }
        resultMsg.setResult(true);
        getLogger().info("弹性伸缩规则变更通知-完成");
        return resultMsg;
    }

    private InfoGroupElasticScalingRuleLog computeMetric(Metric metric, ConfGroupElasticScalingRule scalingRule) {
        InfoGroupElasticScalingRuleLog scalingRuleLog = new InfoGroupElasticScalingRuleLog();
        scalingRuleLog.setEsRuleId(scalingRule.getEsRuleId());
        scalingRuleLog.setAggregateType(scalingRule.getAggregateType());
        scalingRuleLog.setClusterId(scalingRule.getGroupEsId());
        scalingRuleLog.setLoadMetric(metric.getMetricName());
        scalingRuleLog.setMetricVal(metric.getMetricValue().doubleValue());
        scalingRuleLog.setThreshold(scalingRule.getThreshold());
        scalingRuleLog.setOperator(scalingRule.getOperator());
        scalingRuleLog.setCreatedTime(new Date());
        scalingRuleLog.setClusterId(scalingRule.getClusterId());
        scalingRuleLog.setMetricStartTime(metric.getStartTime());
        scalingRuleLog.setMetricEndTime(metric.getEndTime());
        scalingRuleLog.setScaleServerIp(ScaleContext.getInstance().ip);
        elasticScalingRuleLogMapper.insertSelective(scalingRuleLog);

        // 弹性伸缩规则计算
        getLogger().info("计算表达式开始,{},Threshold={},logId={}", JSONUtil.toJsonStr(metric), scalingRule.getThreshold(), scalingRuleLog.getEsRuleLogId());
        Boolean computeRes = computeRule(metric, scalingRule);

        InfoGroupElasticScalingRuleLog updateComputeResult = new InfoGroupElasticScalingRuleLog();
        updateComputeResult.setEsRuleLogId(scalingRuleLog.getEsRuleLogId());
        updateComputeResult.setComputeResult(computeRes ? rule_validate_pass : rule_validate_fail);
        elasticScalingRuleLogMapper.update(updateComputeResult);

        if (computeRes) {
            // 触发弹性伸缩,更新任务执行结果,更新taskId
            InfoGroupElasticScalingRuleLog updateCreateTaskResult = new InfoGroupElasticScalingRuleLog();
            updateCreateTaskResult.setEsRuleLogId(scalingRuleLog.getEsRuleLogId());
            ResultMsg resultMsg = startAutoScaling(scalingRule, scalingRuleLog);
            updateCreateTaskResult.setTaskResult(Boolean.toString(resultMsg.getResult()));
            updateCreateTaskResult.setTaskResultMessage(resultMsg.getRetcode() + ":" + resultMsg.getErrorMsg());
            updateCreateTaskResult.setTaskId(Convert.toStr(resultMsg.getData()));
            if (resultMsg.getResult()) {
                updateCreateTaskResult.setIsStartScaling(task_runing);
            } else {
                updateCreateTaskResult.setIsStartScaling(task_create_failure);
                if (isGroupOrClusterDeleted(scalingRule.getClusterId(), scalingRule.getGroupName())) {
                    getLogger().info("更新实例组规则为无效,{}", scalingRule.getGroupEsId());
                    ConfGroupElasticScalingRule updateIsValid = new ConfGroupElasticScalingRule();
                    updateIsValid.setGroupEsId(scalingRule.getGroupEsId());
                    updateIsValid.setIsValid(rule_isValid_disabled);
                    elasticScalingRuleMapper.updateByGroupEsId(updateIsValid);
                    getLogger().info("通知弹性伸缩规则变更,{}", scalingRule.getGroupEsId());
                    metricChangeNotify();
                }
            }
            elasticScalingRuleLogMapper.update(updateCreateTaskResult);
        }
        return scalingRuleLog;
    }

    /**
     * 弹性伸缩规则计算
     *
     * @param metric
     * @param scalingRule
     */
    private Boolean computeRule(Metric metric, ConfGroupElasticScalingRule scalingRule) {
        String express = String.format("key %s value", scalingRule.getOperator());
        try {
            Boolean res = EnumOperator.valueOfOperator(scalingRule.getOperator()).compare(metric.getMetricValue(), new BigDecimal(scalingRule.getThreshold() + ""));
            getLogger().info("计算表达式结果,{},res={}", scalingRule.getMetriWindowKey(), res);
            if (!res) {
                getLogger().info("规则指标不满足,{},key={},value={},metric_value={}",
                        express, metric.getMetricName(), scalingRule.getThreshold(), metric.getMetricValue());
                return false;
            }
            return true;
        } catch (Exception ex) {
            getLogger().error("弹性伸缩规则验证异常,{},key={},value={},metric_value={}", express, metric.getMetricName(), scalingRule.getThreshold(), metric.getMetricValue(), ex);
        }
        return false;
    }

    /**
     * 弹性伸缩开始
     *
     * @param scalingRule
     * @param scalingRuleLog
     */
    private ResultMsg startAutoScaling(ConfGroupElasticScalingRule scalingRule, InfoGroupElasticScalingRuleLog scalingRuleLog) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            //进行弹性伸缩
            Integer repeatCount = scalingRule.getRepeatCount();
            if (repeatCount != null) {
                Boolean isContinuous = false;
                if (Objects.equals(scalingRule.getRepeatCount(), 1)) {
                    isContinuous = true;
                } else {
                    List<InfoGroupElasticScalingRuleLog> scalingRuleLogList = elasticScalingRuleLogMapper.selectComputePassInWindowSize(scalingRule.getEsRuleId(), scalingRule.getLoadMetric(), scalingRule.getAggregateType(), repeatCount, scalingRuleLog.getCreatedTime());
                    scalingRuleLogList.sort(Comparator.comparing(InfoGroupElasticScalingRuleLog::getCreatedTime));
                    int ruleLogSize = scalingRuleLogList.size();
                    if (ruleLogSize < repeatCount) {
                        return resultMsg;
                    }
                    for (int i = 0; i < scalingRuleLogList.size(); i++) {
                        InfoGroupElasticScalingRuleLog first = scalingRuleLogList.get(i);
                        if (Objects.equals(first.getComputeResult(), rule_validate_fail)) {
                            break;
                        }
                        int next = i + 1;
                        if (next > scalingRuleLogList.size() - 1) {
                            break;
                        }
                        InfoGroupElasticScalingRuleLog second = scalingRuleLogList.get(next);
                        Long firstTime = first.getCreatedTime().getTime();
                        Long secondTime = second.getCreatedTime().getTime();
                        Long metricSub = secondTime - firstTime;
                        getLogger().info("连续窗口时间判断,clusterId={},secondLog={}-{},firstLog={}-{},metricSub={},windowSize={}",
                                scalingRule.getClusterId(),
                                second.getEsRuleLogId(), secondTime, first.getEsRuleLogId(), firstTime, metricSub,
                                scalingRule.getWindowSize());
                        isContinuous = TimeUnit.MILLISECONDS.toMinutes(metricSub) <= scalingRule.getWindowSize();
                        if (!isContinuous) {
                            break;
                        }
                    }
                }
                if (isContinuous) {
                    String lockKey = "request-scale:" + scalingRule.getClusterId() + ":" + scalingRule.getGroupName();
                    boolean lockResult = this.redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
                    if (!lockResult) {
                        getLogger().info("try lock error,lockKey:{}", lockKey);
                        resultMsg.setResult(false);
                        resultMsg.setErrorMsg("执行冲突，请重新提交");
                        return resultMsg;
                    }

                    try {
                        int createOrRunningCount = this.confScalingTaskNeoMapper.countByScalingTypeAndState(scalingRule.getClusterId(),
                                scalingRule.getGroupName(),
                                ConfScalingTask.ScaleType_OUT,
                                ConfScalingTask.ScaleType_IN,
                                ConfScalingTask.SCALINGTASK_Create,
                                ConfScalingTask.SCALINGTASK_Running);
                        if(createOrRunningCount > 0) {
                            getLogger().info("当前实例组存在正在执行的扩缩容任务，请稍后提交, clusterId:{}, groupName:{}",
                                    scalingRule.getClusterId(),
                                    scalingRule.getGroupName());
                            resultMsg.setResult(false);
                            resultMsg.setErrorMsg("当前实例组存在正在执行的扩缩容任务，请稍后提交");
                            return resultMsg;
                        }

                        getLogger().info("满足弹性伸缩条件,创建任务，ruleId={},clusterId={},groupName={},scaleType={}",
                                scalingRule.getEsRuleId(), scalingRule.getClusterId(), scalingRule.getGroupName(), scalingRule.getScalingType());
                        //更新日志表taskid
                        ConfGroupElasticScaling elasticScaling = confGroupElasticScalingMapper.selectByPrimaryKey(scalingRule.getGroupEsId());
                        ConfScalingTask confScalingTask = new ConfScalingTask();
                        confScalingTask.setEsRuleId(scalingRule.getEsRuleId());
                        confScalingTask.setClusterId(scalingRule.getClusterId());
                        confScalingTask.setEsRuleName(scalingRule.getEsRuleName());
                        confScalingTask.setGroupName(scalingRule.getGroupName());
                        confScalingTask.setVmRole(StringUtils.lowerCase(elasticScaling.getVmRole()));
                        confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_Scaling);
                        confScalingTask.setIsGracefulScalein(scalingRule.getIsGracefulScalein());
                        confScalingTask.setScaleinWaitingtime(scalingRule.getScaleinWaitingtime());
                        confScalingTask.setEnableBeforestartScript(scalingRule.getEnableBeforestartScript());
                        confScalingTask.setEnableAfterstartScript(scalingRule.getEnableAfterstartScript());
                        confScalingTask.setMaxCount(scalingRule.getMaxCount());
                        confScalingTask.setMinCount(scalingRule.getMinCount());
                        ConfScalingTask scalingTaskCreated = confScalingTaskNeoMapper.queryLastOne(confScalingTask);
                        getLogger().info("查询上一次任务信息,{}", JSONUtil.toJsonStr(scalingTaskCreated));
                        if (scalingTaskCreated != null) {
                            if (Objects.equals(scalingTaskCreated.getState(), ConfScalingTask.SCALINGTASK_Running)
                                    || Objects.equals(scalingTaskCreated.getState(), ConfScalingTask.SCALINGTASK_Create)) {
                                getLogger().info("任务运行或创建中,{}", JSONUtil.toJsonStr(confScalingTask));
                                resultMsg.setErrorMsg("任务运行或创建中");
                                return resultMsg;
                            }
                            if (Objects.equals(scalingTaskCreated.getState(), ConfScalingTask.SCALINGTASK_Complete)) {
                                long lastFinishTime = scalingTaskCreated.getEndTime().getTime();
                                long waitSec = scalingRule.getFreezingTime();
                                if (DateUtil.current() - lastFinishTime < TimeUnit.SECONDS.toMillis(waitSec)) {
                                    getLogger().info("扩缩容在冷却期中,{}", JSONUtil.toJsonStr(confScalingTask));
                                    resultMsg.setErrorMsg("扩缩容在冷却期中");
                                    return resultMsg;
                                }
                            }
                        }
                        confScalingTask.setTaskId(UUID.randomUUID().toString());
                        confScalingTask.setScalingCount(scalingRule.getPerSalingCout());
                        if (Objects.equals(scalingRule.getScalingType(), scale_type_in)) {
                            confScalingTask.setScalingType(ConfScalingTask.ScaleType_IN);
                            confScalingTask.setCreateTime(new Date());
                            resultMsg = composeService.createScaleInTask(confScalingTask);
                            if(!resultMsg.isSuccess()){
                                getLogger().error("createScaleInTask error,task:{},msg:{}", confScalingTask, resultMsg.getErrorMsg());
                            }
                        } else if (Objects.equals(scalingRule.getScalingType(), scale_type_out)) {
                            confScalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
                            confScalingTask.setCreateTime(new Date());
                            resultMsg = composeService.createScaleOutTask(confScalingTask);
                            if(!resultMsg.isSuccess()){
                                getLogger().error("createScaleOutTask error,task:{},msg:{}", confScalingTask, resultMsg.getErrorMsg());
                            }
                        }
                        resultMsg.setData(confScalingTask.getTaskId());
                        getLogger().info("满足弹性伸缩条件,创建任务完成，ruleId={},clusterId={},groupName={},scaleType={},res={}",
                                scalingRule.getEsRuleId(), scalingRule.getClusterId(), scalingRule.getGroupName(), scalingRule.getScalingType(), JSONUtil.toJsonStr(resultMsg));
                    } finally {
                        this.redisLock.unlock(lockKey);
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().error("满足弹性伸缩条件,创建任务，ruleId={},clusterId={},groupName={},scaleType={}",
                    scalingRule.getEsRuleId(), scalingRule.getClusterId(), scalingRule.getGroupName(), scalingRule.getScalingType(), ex);
        }
        return resultMsg;
    }

    private Boolean isGroupOrClusterDeleted(String clusterId, String groupName) {
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(clusterId, groupName);
        if (confClusterHostGroup == null) {
            return true;
        }
        if (Objects.equals(confClusterHostGroup.getState(), ConfClusterHostGroup.STATE_DELETED)
                || Objects.equals(confClusterHostGroup.getState(), ConfClusterHostGroup.STATE_RELEASED)) {
            return true;
        }

        ConfCluster confCluster = this.confClusterMapper.selectByPrimaryKey(clusterId);
        if(confCluster == null){
            return true;
        }

        if (Objects.equals(confCluster.getState(), ConfCluster.DELETED)
                || Objects.equals(confCluster.getState(), ConfCluster.FAILED)) {
            return true;
        }

        return false;
    }
}
