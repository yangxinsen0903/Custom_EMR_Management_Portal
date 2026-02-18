package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.ClusterDestroyTaskMapper;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.dao.mapper.ConfGroupElasticScalingMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.*;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.consts.LockPrefixConstant;
import com.sunbox.sdpcompose.manager.ClusterHostGroupManager;
import com.sunbox.sdpcompose.manager.ComposeConfClusterManager;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IClusterService;
import com.sunbox.sdpcompose.service.IMQProducerService;
import com.sunbox.sdpcompose.service.IPlanExecService;
import com.sunbox.sdpcompose.service.IVMService;
import com.sunbox.sdpcompose.util.BeanMethod;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.sdpservice.service.AdminService;
import com.sunbox.sdpservice.service.ScaleService;
import com.sunbox.service.*;
import com.sunbox.service.consts.DestroyStatusConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sunbox.constant.RedisLockKeys.CLUSTERDAEMONTASK_CLUSTER_STATUS;
import static com.sunbox.constant.RedisLockKeys.CLUSTERDAEMONTASK_CLUSTER_TIME;

/**
 * @author : [niyang]
 * @className : PlanExecServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/1 10:14 PM]
 */
@Service("PlanExecService")
public class PlanExecServiceImpl implements IPlanExecService, BaseCommonInterFace {
    @Autowired
    private SpringContextUtil app;

    @Autowired
    private BaseClusterOperationTemplateMapper baseClusterOperationTemplateMapper;

    @Autowired
    private BaseClusterOperationTemplateActivityMapper templateActivityMapper;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private ConfClusterMapper clusterMapper;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private InfoClusterVmJobMapper vmJobMapper;

    @Autowired
    private ConfClusterVmMapper confClusterVmMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private IMQProducerService producerService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private ConfScalingTaskMapper scalingTaskMapper;

    @Autowired
    private ConfClusterOpTaskMapper opTaskMapper;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    private ConfClusterHostGroupAppsConfigMapper confClusterHostGroupAppsConfigMapper;

    @Autowired
    private ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Autowired
    private ConfGroupElasticScalingMapper confGroupElasticScalingMapper;

    @Autowired
    private ConfGroupElasticScalingRuleMapper confGroupElasticScalingRuleMapper;

    @Autowired
    private InfoSpotGroupScaleTaskMapper infoSpotGroupScaleTaskMapper;

    @Autowired
    private ComposeConfClusterManager confClusterManager;

    @Autowired
    private InfoClusterVmRejectMapper vmRejectMapper;

    @Autowired
    private ITaskEventService taskEventService;

    @Autowired
    ConfScalingVmMapper confScalingVmMapper;

    @Autowired
    IVmEventService vmEventService;

    @Value("${compose.message.clientname}")
    private String clientname;

    @Value("${activity.timeout}")
    private String activityTimeout;

    @Value("${activity.retry.black:5c4dbe92-78d0-11ed-85b7-6045bdc7fdca}")
    private String blackactlog;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private IVMDeleteService vmdeleteService;
    @Autowired
    private AzureFleetServiceImpl azureFleetServiceImpl;

    @Resource
    private ClusterDestroyTaskMapper clusterDestroyTaskMapper;

    @Autowired
    private IConfScalingTaskService confScalingTaskService;

    /**
     * 任务计划执行消息驱动
     *
     * @param message 消息为jsonstring
     *                {
     *                "activity":"implclass@method",
     *                "activityId":"",
     *                "planid":""
     *                }
     * @return
     */
    @Override
    public ResultMsg composeExecute(String message) {

        JSONObject paramjson = JSON.parseObject(message);
        String activity = paramjson.getString("activity");
        String classname = activity.split("@")[0];
        String methodname = activity.split("@")[1];

        try {
            if (StringUtils.isNotEmpty(paramjson.getString(ComposeConstant.Cluster_ID))) {
                MDC.put(ComposeConstant.Cluster_ID,
                        paramjson.getString(ComposeConstant.Cluster_ID));
            } else {
                getLogger().error("clusterId缺失:", paramjson.toJSONString());
            }

            if (StringUtils.isNotEmpty(paramjson.getString(ComposeConstant.Plan_Id))) {
                MDC.put(ComposeConstant.Plan_Id,
                        paramjson.getString(ComposeConstant.Plan_Id));
            } else {
                getLogger().error("Plan_Id缺失:", paramjson.toJSONString());
            }


        } catch (Exception e) {
            getLogger().error("message MDC Ex,", e);
        }

        BeanMethod beanmethod = null;
        Method method = null;
        if (ProducerCache.methods != null
                && ProducerCache.methods.containsKey(classname)
                && ProducerCache.methods.get(classname).getMethods() != null
                && ProducerCache.methods.get(classname).getMethods().containsKey(methodname)) {
            beanmethod = ProducerCache.methods.get(classname);
            method = beanmethod.getMethods().get(methodname);
        } else {
            if (ProducerCache.methods == null) {
                ProducerCache.methods = new HashMap<>();
            }
            Object obj = SpringContextUtil.getBean(classname);
            Class cls = obj.getClass();
            Class[] classes = new Class[1];
            classes[0] = String.class;
            beanmethod = new BeanMethod();
            beanmethod.setObj(obj);
            try {
                method = cls.getMethod(methodname, classes);
                HashMap<String, Method> methodHashMap = beanmethod.getMethods();
                if (methodHashMap == null) {
                    methodHashMap = new HashMap<>();
                }
                methodHashMap.put(methodname, method);
                beanmethod.setMethods(methodHashMap);
                ProducerCache.methods.put(activity, beanmethod);
            } catch (Exception e) {
                getLogger().error("reflact exception：", e);
            }
        }
        Object[] params = new Object[1];
        params[0] = message;
        try {
            return (ResultMsg) method.invoke(beanmethod.getObj(), params);
        } catch (Exception e) {
            // 采集执行上下文信息
            String methodName = Objects.nonNull(method)? method.getName(): "方法名为空";

            String beanName = "BeanName为空";
            if (Objects.nonNull(beanmethod)) {
                if (Objects.nonNull(beanmethod.getObj())) {
                    beanName = beanmethod.getObj().toString();
                }
            }
            String param = message;
            getLogger().error("call back exception-" + activity + "：beanName=" + beanName +
                            " methodName=" + methodName + " param=" + param,
                    e);

            getLogger().error("call back exception-" + activity + "：", e);
        }
        return new ResultMsg();
    }

    /**
     * 启动执行计划
     *
     * @param planId 执行计划ID
     * @return
     */
    @Override
    public ResultMsg startPlan(String planId) {
        // 获取第一个activityLog

        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);
        plan.setBegTime(new Date());
        planMapper.updateByPrimaryKeySelective(plan);
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog = getFirstActivity(planId);

        activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_RUNNING);
        activityLog.setBegtime(new Date());

        //region 发送消息
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("activity", activityLog.getActivityType() + "@" + activityLog.getActivityName());
        reqMap.put("activityLogId", activityLog.getActivityLogId());
        reqMap.put(ComposeConstant.Cluster_ID, plan.getClusterId());
        reqMap.put(ComposeConstant.Plan_Id,planId);

        //region扩缩容任务需要追加taskId
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut) ||
                plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleIn) ||
                plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Part_ScaleOut)||
                plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOutEvictVm) ||
                plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_pv2DiskThroughput)
        ) {
            reqMap.put(ComposeConstant.Task_ID, plan.getScalingTaskId());

            // 更新任务状态为扩缩容运行中
            ConfScalingTask task = new ConfScalingTask();
            task.setTaskId(plan.getScalingTaskId());
            task.setState(ConfScalingTask.SCALINGTASK_Running);
            task.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            task.setBegTime(new Date());
            scalingTaskMapper.updateByPrimaryKeySelective(task);
            getLogger().info("PlanExecServiceImpl.startPlan update scalingTask. plan: {}, task: {}", JSON.toJSONString(plan), JSON.toJSONString(task));
        }

        // 集群销毁任务
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Delete)) {
          /*  ConfCluster confCluster=clusterMapper.selectByPrimaryKey(plan.getClusterId());
            confCluster.setState(ConfCluster.DELETING);
            clusterMapper.updateByPrimaryKeySelective(confCluster);*/

            // 集群销毁，停止弹性伸缩功能
            clusterService.resetScalingRule(plan.getClusterId(), 0);
        }

        // 集群服务操作 restart start stop
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Start)
                || plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Restart)
                || plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Stop)) {
            ConfClusterOpTask task = opTaskMapper.selectByPrimaryKey(plan.getOpTaskId());
            task.setState(ConfClusterOpTask.OP_TASK_STATE_Running);
            task.setBeginTime(new Date());
            opTaskMapper.updateByPrimaryKeySelective(task);
        }

        activityLog.setParaminfo(JSON.toJSONString(reqMap));
        //endregion

        //任务开始之后,修正期望值,竞价实例的实例组缩容需要在执行前就修改期望值
        ClusterHostGroupManager clusterHostGroupManager = new ClusterHostGroupManager(getLogger(), this.scalingTaskMapper, this.confClusterHostGroupMapper);
        clusterHostGroupManager.updateGroupExpectCount(plan.getScalingTaskId());

        planActivityLogMapper.updateByPrimaryKeySelective(activityLog);
        ResultMsg sendMessageResult = producerService.sendMessage(clientname, JSON.toJSONString(reqMap));
        getLogger().info("startPlan message:" + reqMap.toString());
        //endregion

        return sendMessageResult;
    }

    /**
     * 获取下一个活动
     *
     * @param planId 计划ID
     * @param currentActivityId
     * @return
     */
    @Override
    public InfoClusterOperationPlanActivityLogWithBLOBs getNextActivity(String planId, String currentActivityId) {
        Map<String, Object> paramap = new HashMap<>();
        paramap.put("planId", planId);
        paramap.put("activityLogId", currentActivityId);
        return planActivityLogMapper.getNextActivity(paramap);
    }

    /**
     * 获取上一个活动
     *
     * @param planid
     * @param currentActivityLogId
     * @return
     */
    @Override
    public InfoClusterOperationPlanActivityLogWithBLOBs getPreviousActivity(String planid, String currentActivityLogId) {
        Map<String, Object> paramap = new HashMap<>();
        paramap.put("planId", planid);
        paramap.put("activityLogId", currentActivityLogId);
        return planActivityLogMapper.getPrevActivity(paramap);
    }

    /**
     * 发送下一个活动的消息
     *
     * @param currentActivityLogId
     * @param jsonMessage
     * @return
     */
    @Override
    public ResultMsg sendNextActivityMsg(String currentActivityLogId, JSONObject jsonMessage) {
        ResultMsg msg = new ResultMsg();
        try {

            //集群不可用，结束
            if (!planClusterAvailable(currentActivityLogId)) {
                return msg;
            }

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                    = planActivityLogMapper.selectByPrimaryKey(currentActivityLogId);

            InfoClusterOperationPlanActivityLogWithBLOBs planActivityLog =
                    getNextActivity(currentLog.getPlanId(), currentActivityLogId);
            if (planActivityLog == null) {
                // 没有下一个步骤后, 完成当前流程
                msg.setResult(true);
                msg.setMsg("Job has completed.");
                getLogger().info("Job has completed.");
                this.completePlan(currentLog.getPlanId());
                getLogger().info(" after job completed, execute complete plan ok ");
                return msg;
            }
            String activity = planActivityLog.getActivityType() + "@" + planActivityLog.getActivityName();
            jsonMessage.put("activity", activity);
            jsonMessage.put("activityLogId", planActivityLog.getActivityLogId());
            //更新下个活动 状态为执行中，活动开始时间
            planActivityLog.setParaminfo(jsonMessage.toJSONString());
            planActivityLog.setBegtime(new Date());
            planActivityLog.setState(1);
            planActivityLogMapper.updateByPrimaryKeySelective(planActivityLog);

            producerService.sendMessage(clientname, jsonMessage.toJSONString());
            msg.setResult(true);
        } catch (Exception e) {
            getLogger().error("send next activity message exception,", e);
            msg.setResult(false);
            msg.setErrorMsg("send next activity message exception:" + e.getMessage());
        }
        return msg;
    }

    /**
     * 发送上一步活动的消息
     *
     * @param previousActivity
     * @param delay
     * @return
     */
    @Override
    public ResultMsg sendPrevActivityMsg(InfoClusterOperationPlanActivityLogWithBLOBs previousActivity, Long delay) {
        ResultMsg msg = new ResultMsg();
        try {
            if (previousActivity == null) {
                msg.setResult(false);
                msg.setMsg(" not exist prev activity.");
                getLogger().info(" not exist prev activity.");
                return msg;
            }
            //集群不可用，结束
            if (!planClusterAvailable(previousActivity.getActivityLogId())) {
                return msg;
            }
            //更新上一个活动 状态为执行中，活动开始时间
            previousActivity.setBegtime(new Date());
            previousActivity.setState(1);
            planActivityLogMapper.updateByPrimaryKeySelective(previousActivity);
            producerService.sendScheduleMessage(clientname, previousActivity.getParaminfo(),delay);
            msg.setResult(true);
        } catch (Exception e) {
            getLogger().error("send prev activity message exception,", e);
            msg.setResult(false);
            msg.setErrorMsg("send prev activity message exception:" + ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * 当前计划集群可用性查询
     * deleteing计划返回true
     *
     * @param activityLogId
     * @return
     */
    private boolean planClusterAvailable(String activityLogId) {
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog
                = planActivityLogMapper.selectByPrimaryKey(activityLogId);
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLog.getPlanId());

        // delete 操作直接返回
        // 因为Delete是销毁集群, 销毁集群时不检查集群的可用性.因为集群可能被销毁了.
        if (plan.getOperationType().equalsIgnoreCase("delete")) {
            return true;
        }

        ResultMsg msg = clusterService.checkClusterAvailable(plan.getClusterId());
        if (!msg.getResult()) {
            //集群不可用
            getLogger().info("集群不可用，集群名={}, 集群Id={}", plan.getClusterName(), plan.getClusterId());
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(-2);
            complateActivity(activityLog);
        }
        return msg.getResult();
    }


    /**
     * 发送下一个带有延时的消息
     *
     * @param currentActivityLogId
     * @param jsonMessage
     * @param second
     * @return
     */
    @Override
    public ResultMsg sendNextActivityDelayMsg(String currentActivityLogId, JSONObject jsonMessage, Long second) {
        ResultMsg msg = new ResultMsg();
        try {
            // 集群不可用
            if (!planClusterAvailable(currentActivityLogId)) {
                return msg;
            }

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                    = planActivityLogMapper.selectByPrimaryKey(currentActivityLogId);

            InfoClusterOperationPlanActivityLogWithBLOBs planActivityLog =
                    getNextActivity(currentLog.getPlanId(), currentActivityLogId);
            if (planActivityLog == null) {
                msg.setResult(true);
                msg.setMsg("Job has completed.");
                getLogger().info("Job has completed.");
                this.completePlan(currentLog.getPlanId());
                getLogger().info(" after job completed, execute complete plan ok ");
                return msg;
            }
            String activity = planActivityLog.getActivityType() + "@" + planActivityLog.getActivityName();
            jsonMessage.put("activity", activity);
            jsonMessage.put("activityLogId", planActivityLog.getActivityLogId());
            //更新下个活动 状态为执行中，活动开始时间
            planActivityLog.setParaminfo(jsonMessage.toJSONString());
            planActivityLog.setBegtime(new Date());
            planActivityLog.setState(1);
            planActivityLogMapper.updateByPrimaryKeySelective(planActivityLog);

            producerService.sendScheduleMessage(clientname, jsonMessage.toJSONString(), second);
            msg.setResult(true);
        } catch (Exception e) {
            getLogger().error("send next activity message exception,", e);
            msg.setResult(false);
            msg.setErrorMsg("send next activity message exception:" + e.getMessage());
        }
        return msg;
    }

    /**
     * 执行计划完成，做相关操作<br/>
     * 整个流程执行完成后会调用此方法.
     */
    private ResultMsg completePlan(String planId) {

        ResultMsg msg = new ResultMsg();

        //region 更新计划数据
        InfoClusterOperationPlan fullPlan = planMapper.selectByPrimaryKey(planId);
        String operationType = fullPlan.getOperationType();
        fullPlan.setEndTime(new Date());
        planMapper.updateByPrimaryKeySelective(fullPlan);
        getLogger().info("plan主表info_cluster_operation_plan设置完成时间， plan complete:" + planId);
        // 更新计划状态和进度
        updatePlanStateAndPercent(planId);
        //endregion

        if (null == operationType) {
            getLogger().warn("执行计划完成，做相关操作，非模版创建plan");
            msg.setResult(false);
            return msg;
        }

        // 获取该Plan的所有执行步骤信息
        List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogs = planActivityLogMapper.getAllActivity(planId);

        // 过滤出失败的步骤: 失败和超时
        List<InfoClusterOperationPlanActivityLogWithBLOBs> faileds = activityLogs.stream().filter(x -> {
            return x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED) ||
                    x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT);
        }).collect(Collectors.toList());

        //region 失败的任务日志收集
        if (faileds != null && !faileds.isEmpty()) {
            createCollectLogPlan(fullPlan.getClusterId(), fullPlan.getOperationType(),fullPlan.getPlanId());
            //更新任务为失败
            updateDestroyTaskAndCache(Collections.singletonList(fullPlan.getClusterId()), DestroyStatusConstant.DESTROY_STATUS_FAIL,null,new Date());
        }
        //endregion

        //region 创建
        // 创建集群完成后的处理工作
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create)) {
            // 1. 更新InfoCluster的集群创建结束时间
            String clusterId = fullPlan.getClusterId();
            InfoCluster infoCluster = new InfoCluster();
            infoCluster.setClusterId(clusterId);
            infoCluster.setClusterCreateEndtime(new Date());
            infoClusterMapper.updateByPrimaryKeySelective(infoCluster);
            getLogger().info("info_cluster表设置完成时间 ok");

            // 2. 更新ConfCluster状态. 如果集群创建成功, 还要启动清理VM的执行计划. 避免因为降级等原因有VM成为僵尸机
            ConfCluster cluster = clusterMapper.selectByPrimaryKey(clusterId);
            if (Objects.equals(cluster.getState(), ConfCluster.DELETING) || Objects.equals(cluster.getState(), ConfCluster.DELETED)) {
                getLogger().info("集群正在删除中或已经删除，不更新集群状态（成功或失败）");
                msg.setResult(true);
                msg.setMsg("complete plan ok: cluster deleting or deleted");
                return msg;
            } else {
                if (null != faileds && faileds.size() > 0) {
                    confClusterManager.updateConfClusterState(clusterId, ConfCluster.FAILED);
                } else {
                    // 所有步骤都成功, 清理降级处理的VM.
                    ivmService.startClearVMPlan(clusterId,planId);
                    confClusterManager.updateConfClusterState(clusterId, ConfCluster.CREATED);
                }
                getLogger().info("集群状态conf_cluster-->state处理 ok");
            }

            // 3. 更新Task实例组的扩缩容规则为生效状态
            List<ConfClusterHostGroup> hostGroups = confClusterHostGroupMapper.selectByVmRoleAndClusterId(cluster.getClusterId(), VmRoleType.TASK.getVmRole());
            for (ConfClusterHostGroup hostGroup : hostGroups) {
                updateElasticScaleValid(hostGroup, ConfGroupElasticScalingRule.ISVALID_YES);
            }

            // 4. 更新info_cluster_vm中所有VM的加入集群时间(因为是创建集群, 所以可以批量更新全部VM)
            infoClusterVmMapper.updateJoinClusterTimeOnCompleteCreate(clusterId);
            //

            // 5. 调用Ambari, 设置集群自动启动
            clusterService.enableClusterAutoStart(cluster);
            //endregion

            // 6. 记录创建集群时生成的VM的上下线事件
            vmEventService.saveVmEventsForCreateCluster(clusterId);
        }
        //endregion

        //region 删除集群(销毁集群)
        // 删除
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Delete)) {
            // 1. 更新实例组状态为删除
            confClusterHostGroupMapper.updateByClusterId(fullPlan.getClusterId(), ConfClusterHostGroup.STATE_DELETED);
            // 2. 设置集群的弹性扩缩容规则为失效
            clusterService.resetScalingRule(fullPlan.getClusterId(), ConfGroupElasticScalingRule.ISVALID_NO);
            // 记录上下线事件, 由于销毁集群的主机不容易在最后统计,所以记录销毁集群的事件放在第一步处理.
            // 代码位置为: AzureVMServiceImpl.queryDeleteVms() 方法为:saveVmOfflineEvent(clusterId)
//            vmEventService.saveVmEventsForScaleInTask(fullPlan.getClusterId(), fullPlan.getScalingTaskId());
            // 更新任务表为已经销毁
            updateDestroyTask(Collections.singletonList(fullPlan.getClusterId()), DestroyStatusConstant.DESTROY_STATUS_END,null,new Date());
        }
        //endregion

        //region 扩容, 包括普通扩容和Azure补全驱逐的SpotVM
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut) ||
                operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOutEvictVm)) {
            // 1. 更新集群状态为已创建 , 目前不清楚为啥有此步骤
            confClusterManager.updateConfClusterState(fullPlan.getClusterId(), ConfCluster.CREATED);

            //region 2. 完成扩容删除扩容标识
            delScalingFlag(fullPlan.getClusterId());
            //endreigon

            ConfCluster cluster = clusterMapper.selectByPrimaryKey(fullPlan.getClusterId());

            //region 3. 更新扩容任务状态
            ConfScalingTask scalingTask = updateScalingTask(fullPlan, faileds);
            //endregion

            //region 更新pv2磁盘iops和吞吐量
            pv2DiskScalingTask(scalingTask.getClusterId(), scalingTask.getGroupName(),scalingTask.getVmRole(),fullPlan.getScalingTaskId());
            //endregion

            // 4. 如果有失败的步骤, 清理扩容成功清理vm
            if (faileds == null || faileds.size() == 0){
                ivmService.startClearVMPlan(fullPlan.getClusterId(),planId);
            }

            //region 5. 竞价实例扩容后完成后处理逻辑, 更新 InfoSpotGroupScale表的状态
            if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                spotScalingTaskProcess(scalingTask);
            }
            //endreigon
            // 6. 扩容完成，关闭VM的工程模式
            infoClusterVmMapper.updateMaintenanceModeByScaleoutTaskId(scalingTask.getTaskId(),
                    fullPlan.getClusterId(),InfoClusterVm.MaintenanceModeOFF);

            // 7.
            updateInfoClusterStatics(fullPlan.getClusterId(), scalingTask, CollUtil.isNotEmpty(faileds));

            //region 8. 扩容后更新clustervm加入集群时间
            infoClusterVmMapper.updateJoinClusterTimeOnCompleteScaleOut(fullPlan.getClusterId(),scalingTask.getTaskId());
            //endregion

            // 记录上下线事件
            vmEventService.saveVmEventsForScaleOutTask(fullPlan.getClusterId(), fullPlan.getScalingTaskId());
        }
        //endregion

        //region 缩容
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleIn)) {
            //region 完成缩容删除扩容标识
            delScalingFlag(fullPlan.getClusterId());
            //endregion

            ConfScalingTask scalingTask = updateScalingTask(fullPlan, faileds);

            //region 竞价实例扩容后完成后处理逻辑
            if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                spotScalingTaskProcess(scalingTask);
            }
            //endregion

            updateInfoClusterStatics(fullPlan.getClusterId(), scalingTask, CollUtil.isNotEmpty(faileds));

            // 记录上下线事件
            vmEventService.saveVmEventsForScaleInTask(fullPlan.getClusterId(), fullPlan.getScalingTaskId());
        }
        //endregion

        //region 磁盘扩容
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Part_ScaleOut)) {
            //region 完成缩容删除扩容标识
            delScalingFlag(fullPlan.getClusterId());
            //endreigon
            ConfScalingTask scalingTask = updateScalingTask(fullPlan, faileds);

            //更新磁盘扩容数据
            List<ConfClusterVm> vmConfsByGroupName = confClusterVmMapper.getVmConfsByGroupName(scalingTask.getGroupName(), scalingTask.getClusterId());
            ConfClusterVm confClusterVm = vmConfsByGroupName.get(0);
            ConfClusterVmDataVolume dataVolume = new ConfClusterVmDataVolume();
            dataVolume.setVmConfId(confClusterVm.getVmConfId());
            dataVolume.setDataVolumeSize(scalingTask.getAfterScalingCount());
            confClusterVmDataVolumeMapper.updateByVmConfId(dataVolume);

            confScalingVmMapper.updateStateByTaskId(scalingTask.getTaskId(), scalingTask.getState());
            getLogger().info("confScalingVmMapper updateStateByTaskId taskId:{},state:{}", scalingTask.getTaskId(), scalingTask.getState());
        }
        //endregion

        //region 磁盘性能调整
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_pv2DiskThroughput)) {
            ConfScalingTask scalingTask = updateScalingTask(fullPlan, faileds);
            //计划完成更新磁盘性能指标
            if (ConfScalingTask.SCALINGTASK_Complete.compareTo(scalingTask.getState())==0) {
                List<ConfClusterVm> vmConfsByGroupName = confClusterVmMapper.getVmConfsByGroupName(scalingTask.getGroupName(), scalingTask.getClusterId());
                for (ConfClusterVm confClusterVm : vmConfsByGroupName) {
                    ConfClusterVmDataVolume dataVolume = new ConfClusterVmDataVolume();
                    dataVolume.setVmConfId(confClusterVm.getVmConfId());
                    dataVolume.setThroughput(scalingTask.getAfterScalingCount());
                    dataVolume.setIops(scalingTask.getBeforeScalingCount());
                    confClusterVmDataVolumeMapper.updateByVmConfId(dataVolume);
                }
            }
        }
        //endregion

        //region 清理vm

        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClearVMs)){
            updateInfoClusterStaticsForClearVM(fullPlan.getClusterId());

            // 记录上下线事件
            vmEventService.saveVmEventsForScaleInTask(fullPlan.getClusterId(), fullPlan.getScalingTaskId());
        }

        //endregion

        //region 集群服务操作完成后数据逻辑处理
        if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Stop)
                || operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Restart)
                || operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ClusterService_Start)) {
            updateServiceOpTask(fullPlan, faileds);
        }
        //endregion

        msg.setResult(true);
        msg.setMsg("complete plan ok");
        return msg;
    }

    /**
     * 计算一个执行计划的执行状态及执行百分比<br/>
     * 返回结果说明: 返回一个通用的没有类型的对象ResultMsg, <br/>
     * ResultMsg.result = false时, 失败. errorMsg中是错误堆栈信息; <br/>
     * ResultMsg.result = true时, 成功, ResultMsg.data里装一个 <code>Map&lt;String,Integer&gt;</code>类型的Map,此Map的值如下:
     * <ul>
     *     <li>state: 执行状态, 执行状态的代码见: InfoClusterOperationPlanActivityLog.ACTION_* 的变量</li>
     *     <li>percent: 执行百分比</li>
     * </ul>
     * @param planId
     * @return
     */
    private ResultMsg computePlanStateAndPercent(String planId){
        ResultMsg msg = new ResultMsg();
        try {
            List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogs = planActivityLogMapper.getAllActivity(planId);

            int allSize = activityLogs.size();

            Map<String,Integer> processData = new HashMap<>();

            //region 统计执行步骤各状态的数量
            AtomicInteger completedSize = new AtomicInteger();
            AtomicInteger planningSize = new AtomicInteger();
            AtomicInteger timeOutSize = new AtomicInteger();
            AtomicInteger failedSize = new AtomicInteger();

            Map<Integer, List<InfoClusterOperationPlanActivityLogWithBLOBs>> groupLogs =
                    activityLogs.stream().collect(Collectors.groupingBy(x -> {
                        return x.getState();
                    }));

            groupLogs.entrySet().stream().forEach(item -> {
                // 执行完成的步骤数量
                if (item.getKey().equals(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED)) {
                    if (item.getValue() != null) {
                        completedSize.set(item.getValue().size());
                    } else {
                        completedSize.set(0);
                    }
                }
                // 待执行的步骤数量
                if (item.getKey().equals(InfoClusterOperationPlanActivityLog.ACTION_PLANING)) {
                    if (item.getValue() != null) {
                        planningSize.set(item.getValue().size());
                    } else {
                        planningSize.set(0);
                    }
                }
                // 执行超时的步骤数量
                if (item.getKey().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT)) {
                    if (item.getValue() != null) {
                        timeOutSize.set(item.getValue().size());
                    }
                }

                // 执行失败的步骤数量
                if (item.getKey().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED)) {
                    if (item.getValue() != null) {
                        failedSize.set(item.getValue().size());
                    } else {
                        failedSize.set(0);
                    }
                }
            });

            //endregion
            getLogger().info("failedSize:{},timeoutSize:{},", failedSize.get(), timeOutSize.get());

            //region 计算步骤执行百分比
            Double percent = ((Double.valueOf(allSize)-Double.valueOf(planningSize.get()))/Double.valueOf(allSize) * 100);

            if (NumberUtil.equals(percent, (Double)100.0)) {
                percent = 99.0;
            }
            processData.put("percent", percent.intValue());
            //endregion
            msg.setResult(true);
            //region state 状态
            // 失败
            if (failedSize.get() > 0) {
               processData.put("state",InfoClusterOperationPlan.Plan_State_Failed);
                msg.setData(processData);
                return msg;
            }
            // 超时
            if (timeOutSize.get() > 0){
                processData.put("state",InfoClusterOperationPlan.Plan_state_TimeOut);
                msg.setData(processData);
                return msg;
            }
            // 完成
            if (completedSize.get() == allSize){
                processData.put("state",InfoClusterOperationPlan.Plan_State_Completed);
                // 只有完成的任务，才是100
                processData.put("percent", 100);
                msg.setData(processData);
                return msg;
            }
            // 待执行
            if (planningSize.get() == allSize){
                processData.put("state",InfoClusterOperationPlan.Plan_State_Create);
                msg.setData(processData);
                return msg;
            }
            // 执行中
            processData.put("state",InfoClusterOperationPlan.Plan_State_Running);
            msg.setData(processData);
            return msg;

            //endregion

        }catch (Exception e){
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return msg;
        }

    }

    /**
     * 竞价实例扩缩容完成后处理逻辑
     *
     * @param task
     * @return
     */
    private void spotScalingTaskProcess(ConfScalingTask task) {
        if (!Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_spot)) {
            getLogger().info("skip spotScalingTaskProcess clusterId:{}, groupId:{}, taskId:{}, taskState:{}, operationType:{}",
                    task.getClusterId(),
                    task.getGroupName(),
                    task.getTaskId(),
                    task.getState(),
                    task.getOperatiionType());
            return;
        }

        getLogger().info("spotScalingTaskProcess clusterId:{}, groupId:{}, taskId:{}, taskState:{}",
                task.getClusterId(),
                task.getGroupName(),
                task.getTaskId(),
                task.getState());

        if (Objects.equals(task.getState(), ConfScalingTask.SCALINGTASK_Complete)) {
            try {
                // 执行成功
                InfoSpotGroupScaleTask infoSpotGroupScaleTask = infoSpotGroupScaleTaskMapper.selectByPrimaryKey(task.getTaskId());
                if (infoSpotGroupScaleTask == null) {
                    getLogger().warn("not found infoSpotGroupScaleTask taskId:{}", task.getTaskId());
                } else {
                    getLogger().info("begin update infoSpotGroupScaleTask state to EXECUTE_SUCCESS clusterId:{}, groupId:{}, taskId:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            task.getTaskId());
                    infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTE_SUCCESS.getValue());
                    infoSpotGroupScaleTask.setModifiedby(task.getDefaultUsername());
                    infoSpotGroupScaleTask.setModifiedTime(new Date());
                    infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
                    getLogger().info("end update infoSpotGroupScaleTask state to EXECUTE_SUCCESS clusterId:{}, groupId:{}, taskId:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            task.getTaskId());
                }
            } catch (Exception e) {
                getLogger().error("update infoSpotGroupScaleTask state to EXECUTE_SUCCESS error clusterId:{}, groupId:{}, taskId:{}",
                        task.getClusterId(),
                        task.getGroupName(),
                        task.getTaskId(),
                        e);
            }
        } else {
            try {
                // 执行失败
                InfoSpotGroupScaleTask infoSpotGroupScaleTask = infoSpotGroupScaleTaskMapper.selectByPrimaryKey(task.getTaskId());
                if (infoSpotGroupScaleTask == null) {
                    getLogger().warn("not found infoSpotGroupScaleTask taskId:{}", task.getTaskId());
                } else {
                    getLogger().info("begin update infoSpotGroupScaleTask state to EXECUTE_FAILURE clusterId:{}, groupId:{}, taskId:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            task.getTaskId());
                    infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
                    infoSpotGroupScaleTask.setModifiedby(task.getDefaultUsername());
                    infoSpotGroupScaleTask.setModifiedTime(new Date());
                    infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
                    getLogger().info("end update infoSpotGroupScaleTask state to EXECUTE_FAILURE clusterId:{}, groupId:{}, taskId:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            task.getTaskId());
                }
            } catch (Exception e) {
                getLogger().error("update infoSpotGroupScaleTask state to EXECUTE_FAILURE error clusterId:{}, groupId:{}, taskId:{}",
                        task.getClusterId(),
                        task.getGroupName(),
                        task.getTaskId(),
                        e);
            }
        }
    }


    /**
     * 创建收集日志的执行计划并立即执行
     *
     * @param clusterId
     * @return
     */
    private ResultMsg createCollectLogPlan(String clusterId, String operationtype, String planId) {
        ResultMsg msg = new ResultMsg();
        try {
            //只有创建集群 扩容 磁盘扩容 运行用户自定义脚本任务失败时开启日志收集
            if (operationtype.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut) ||
                operationtype.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create) ||
                operationtype.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Part_ScaleOut)) {

                ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);

                //region 检查扩容是否申请资源成功
                if (operationtype.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut) ){
                    InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);
                    List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(confCluster.getClusterId(),plan.getScalingTaskId());
                    if (vms == null || vms.size() == 0){
                        getLogger().warn("没有申请到VM，不发起收集日志到任务。");
                    }
                    msg.setResult(false);
                    msg.setErrorMsg("没有申请到VM，不发起收集日志到任务。");
                    return msg;
                }
                //endregion

                //region 检查创建集群是否申请资源成功
                if (operationtype.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create)){
                    List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterId(confCluster.getClusterId());
                    if (vms == null || vms.size() == 0){
                        getLogger().warn("没有申请到VM，不发起收集日志到任务。");
                    }
                    msg.setResult(false);
                    msg.setErrorMsg("没有申请到VM，不发起收集日志到任务。");
                    return msg;
                }

                createPlanAndRun(clusterId,
                        confCluster.getClusterReleaseVer(),
                        InfoClusterOperationPlan.Plan_OP_CollectLogs,
                        null, planId);
                msg.setResult(true);
            }
        } catch (Exception e) {
            getLogger().error("createCollectLogPlan,异常", e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 更新集群服务操作任务
     *
     * @param fullPlan
     * @param faileds
     */
    private void updateServiceOpTask(InfoClusterOperationPlan fullPlan,
                                     List<InfoClusterOperationPlanActivityLogWithBLOBs> faileds) {
        //region 更新扩容任务状态
        String taskId = fullPlan.getOpTaskId();
        ConfClusterOpTask opTask = opTaskMapper.selectByPrimaryKey(taskId);
        opTask.setEndTime(new Date());
        if (null != faileds && faileds.size() > 0) {
            opTask.setState(ConfClusterOpTask.OP_TASK_STATE_Failed);
        } else {
            opTask.setState(ConfClusterOpTask.OP_TASK_STATE_Complate);
        }
        getLogger().info("update:" + opTask.toString());
        opTaskMapper.updateByPrimaryKeySelective(opTask);
        // endregion
    }

    /**
     * 更新扩缩容任务, 主要是更新扩缩容任务的执行状态和是否在等待队列中.<br/>
     * 也会更新备注信息.如果失败了,会把失败的信息保存起来.
     *
     * @param fullPlan
     * @param faileds
     */
    private ConfScalingTask updateScalingTask(InfoClusterOperationPlan fullPlan,
                                              List<InfoClusterOperationPlanActivityLogWithBLOBs> faileds) {
        //region 更新扩容任务状态
        String taskId = fullPlan.getScalingTaskId();
        ConfScalingTask scalingTask = scalingTaskMapper.selectByPrimaryKey(taskId);
        scalingTask.setEndTime(new Date());
        if (null != faileds && faileds.size() > 0) {
            scalingTask.setState(ConfScalingTask.SCALINGTASK_Failed);
            scalingTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            scalingTask.appendRemark(getRemarkFromLogs(faileds));
        } else {
            scalingTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            scalingTask.setState(ConfScalingTask.SCALINGTASK_Complete);
            if (StringUtils.isNotEmpty(scalingTask.getRemark())) {
                scalingTask.appendRemark("成功,来自\"" + scalingTask.getRemark() + "\"");
            }
        }

        getLogger().info("updateScalingTask task:{}", scalingTask);
        scalingTaskMapper.updateByPrimaryKeySelective(scalingTask);
        // endregion
        return scalingTask;
    }

    @NotNull
    private static String getRemarkFromLogs(List<InfoClusterOperationPlanActivityLogWithBLOBs> faileds) {
        String remark = null;
        for (InfoClusterOperationPlanActivityLogWithBLOBs failed : faileds) {
            if (StringUtils.isEmpty(remark)) {
                remark = failed.getLogs();
            }
        }
        if (StringUtils.isNotEmpty(remark)) {
            if (remark.length() > 100) {
                remark = remark.substring(0, 100);
            }
        } else {
            remark = "出现了错误记录";
        }
        return remark;
    }

    /**
     * 更新集群统计信息
     *
     * @param clusterId
     * @return
     */
    private boolean updateInfoClusterStaticsForClearVM(String clusterId){
        try {

            InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(clusterId);

            List<HashMap> vmRoleNameCount = infoClusterVmMapper.getVMRoleNameCount(clusterId);
            for (HashMap hashMap : vmRoleNameCount) {
                String vmRole = (String) hashMap.get("vm_role");
                if (StrUtil.equalsIgnoreCase(vmRole, "task")) {
                    infoCluster.setTaskVmsCount(Convert.toInt(hashMap.get("cnt"), 0));
                } else if (StrUtil.equalsIgnoreCase(vmRole, "core")) {
                    infoCluster.setCoreVmsCount(Convert.toInt(hashMap.get("cnt"), 0));
                }
            }
            infoClusterMapper.updateByPrimaryKeySelective(infoCluster);

            List<HashMap> group_cnt = infoClusterVmMapper.getGroupNameCount(clusterId);

            for (HashMap hashMap : group_cnt) {
                String groupName = (String) hashMap.get("group_name");

                ConfClusterHostGroup hostGroup =
                        confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(clusterId, groupName);
                hostGroup.setInsCount(Convert.toInt(hashMap.get("cnt"), 0));
                confClusterHostGroupMapper.updateByPrimaryKeySelective(hostGroup);
            }
            return true;
        }catch (Exception e){
            getLogger().error("更新集群统计信息异常，",e);
            return false;
        }

    }

    private boolean updateInfoClusterStatics(String clusterId, ConfScalingTask scalingTask, Boolean isError) {
        if (isError) {
            getLogger().info("计划执行失败，不更新统计信息,clusterId={},taskId={}", clusterId, scalingTask.getTaskId());
            return false;
        }
        try {
            // region 更新infocluster 统计数据
            String groupName = scalingTask.getGroupName();
            InfoClusterVm vmCountByGroupName = infoClusterVmMapper.getVmCountByGroupName(clusterId, groupName);
            List<ConfClusterVm> confClusterVmList = confClusterVmMapper.getVmConfsByGroupName(clusterId, groupName);

            getLogger().info("扩缩容更新数量-updateInfoClusterStatics:" + vmCountByGroupName.toString());
            InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(clusterId);

            Optional<ConfClusterVm> first = confClusterVmList.stream().filter(p -> Objects.equals(groupName, p.getGroupName())).findFirst();
            List<ConfClusterVm> updateClusterVmCnts = new ArrayList<>();
            if (first.isPresent()) {
                ConfClusterVm confClusterVm = first.get();
                confClusterVm.setCount(vmCountByGroupName.getCnt());
                if (Objects.equals(confClusterVm.getCount(), 0)) {
                    if (Objects.equals(scalingTask.getDeleteGroup(), ConfScalingTask.SCALINGTASK_DELETE_GROUP)) {
                        confClusterVm.setState(ConfClusterVm.STATE_DELETED);
                    } else {
                        confClusterVm.setState(ConfClusterVm.STATE_RELEASED);
                    }
                }
                updateClusterVmCnts.add(confClusterVm);
            }

            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(clusterId, groupName);
            if (hostGroup != null) {
                //获取最新的 info cluster vm 的数量
                int infoClusterVmCountFromDb = 0;
                if(vmCountByGroupName.getCnt() != null) {
                    infoClusterVmCountFromDb = vmCountByGroupName.getCnt();
                    getLogger().warn("vmCountByGroupName cnt is null, clusterId:{}, groupId:{}",
                            hostGroup.getClusterId(),
                            hostGroup.getGroupId());
                }

                ConfClusterHostGroup updateHostGroup = new ConfClusterHostGroup();
                if(scalingTask.getAfterScalingCount() != null && scalingTask.getAfterScalingCount() != infoClusterVmCountFromDb) {
                    getLogger().warn("updateHostGroup afterScalingCount:{} not eq infoClusterVmCountFromDb:{}, clusterId:{}, groupId:{}",
                            scalingTask.getAfterScalingCount(),
                            infoClusterVmCountFromDb,
                            hostGroup.getClusterId(),
                            hostGroup.getGroupId());
                }
                updateHostGroup.setInsCount(infoClusterVmCountFromDb);
                updateHostGroup.setGroupId(hostGroup.getGroupId());
                if (Objects.equals(scalingTask.getDeleteGroup(), ConfScalingTask.SCALINGTASK_DELETE_GROUP)) {
                    updateHostGroup.setState(ConfClusterHostGroup.STATE_DELETED);
                    confClusterHostGroupAppsConfigMapper.deleteByGroupId(hostGroup.getGroupId());
                    //更新实例规则状态未无效
                    updateElasticScaleValid(hostGroup, ConfGroupElasticScaling.ISVALID_NO);
                    //TODO: 调用Azure接口删除AzureFleet
                    ConfCluster confCluster = clusterMapper.selectByPrimaryKey(scalingTask.getClusterId());
                    azureFleetServiceImpl.deleteAzureFleet(confCluster, groupName);
                }
                if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_create_group)) {
                    updateHostGroup.setState(ConfClusterHostGroup.STATE_RUNNING);
                    updateElasticScaleValid(hostGroup, ConfGroupElasticScaling.ISVALID_YES);
                }
                //解决因创建实例组任务失败，后面扩容任务不能更新实例组状态的问题
                if (Objects.equals(scalingTask.getScalingType(),ConfScalingTask.ScaleType_OUT) &&
                        Objects.equals(hostGroup.getState(),ConfClusterHostGroup.STATE_CREATING)){
                    updateHostGroup.setState(ConfClusterHostGroup.STATE_RUNNING);
                    updateElasticScaleValid(hostGroup, ConfGroupElasticScaling.ISVALID_YES);
                }
                //竞价实例的处理, 竞价实例扩缩容是根据实际值与期望值之间的差来判断的. 所以要做特殊处理
                if (PurchaseType.Spot.equalValue(hostGroup.getPurchaseType())) {
                    if (!Objects.equals(ConfScalingTask.Operation_type_spot, scalingTask.getOperatiionType())) {
                        // 不是竞价扩缩容
                        if (!Objects.equals(ConfScalingTask.Operation_type_delete_Task_Vm, scalingTask.getOperatiionType())) {
                            //手动缩容时修改期望数量
                            if (Objects.equals(ConfScalingTask.ScaleType_IN, scalingTask.getScalingType())) {
                                // 修改HostGroup期望值
                                updateHostGroup.setExpectCount(scalingTask.getAfterScalingCount());
                                getLogger().info("updateInfoClusterStatics setExpectCount:{}", updateHostGroup);
                            }
                        }
                    } else if (Objects.equals(ConfScalingTask.Operation_type_Complete_Evict_Vm, scalingTask.getOperatiionType())) {
                        // 如果是补全驱逐VM流程, 将ExpectCount与insCount设置为一致, 这样就不再走后续的扩容了
                        updateHostGroup.setExpectCount(updateHostGroup.getInsCount());
                        getLogger().info("补全驱逐VM,设置实例组的expectCount与insCount一致. hostGroup.insCount={}, " +
                                "hostGroup.expectCount={}, task.scaleCount={}, task.afterScalingCount={}",
                                updateHostGroup.getInsCount(), updateHostGroup.getExpectCount(),
                                scalingTask.getScalingCount(), scalingTask.getAfterScalingCount());
                    } else {
                        //spot竞价服务处理
                        spotScalingTaskProcess(scalingTask);
                        //如果是第一次竞价扩容, 则将弹性规则设置为有效
                        if (Objects.equals(ConfScalingTask.ScaleType_OUT, scalingTask.getScalingType()) &&
                                scalingTask.getBeforeScalingCount() <= 0) {
                            updateElasticScaleValid(hostGroup, ConfGroupElasticScaling.ISVALID_YES);
                        }
                        // 如果是缩容, 缩容完成后,更新hostGroup的ExpectCount,避免再次扩容
                        if (Objects.equals(ConfScalingTask.ScaleType_IN, scalingTask.getScalingType()) &&
                            Objects.equals(ConfScalingTask.Operation_type_spot, scalingTask.getOperatiionType())) {
                            getLogger().info("竞价缩容完成，变更实例组的ExpectCount。insCount={}, 原expectCount={}, 新expectCount={}",
                                    updateHostGroup.getInsCount(),
                                    updateHostGroup.getExpectCount(),
                                    updateHostGroup.getInsCount());
                            updateHostGroup.setExpectCount(updateHostGroup.getInsCount());
                        }
                    }
                }
                confClusterHostGroupMapper.updateByPrimaryKeySelective(updateHostGroup);
            }
            List<HashMap> vmRoleNameCount = infoClusterVmMapper.getVMRoleNameCount(infoCluster.getClusterId());
            for (HashMap hashMap : vmRoleNameCount) {
                String vmRole = (String) hashMap.get("vm_role");
                if (StrUtil.equalsIgnoreCase(vmRole, "task")) {
                    infoCluster.setTaskVmsCount(Convert.toInt(hashMap.get("cnt"), 0));
                } else if (StrUtil.equalsIgnoreCase(vmRole, "core")) {
                    infoCluster.setCoreVmsCount(Convert.toInt(hashMap.get("cnt"), 0));
                }
            }
            getLogger().info("扩缩容更新数量-infocluster:" + infoCluster);
            infoClusterMapper.updateByPrimaryKeySelective(infoCluster);
            if (!CollectionUtils.isEmpty(confClusterVmList)) {
                for (ConfClusterVm confClusterVm : confClusterVmList) {
                    confClusterVmMapper.updateByPrimaryKeySelective(confClusterVm);
                }
            }
            // endregion
            return true;
        } catch (Exception e) {
            getLogger().error("updateInfoClusterStatics,", e);
            return false;
        }
    }

    private void updateElasticScaleValid(ConfClusterHostGroup hostGroup, Integer isValid) {
        try {
            getLogger().info("实例组扩缩容完成时更新规则为无效,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid);
            ConfGroupElasticScaling scaling = new ConfGroupElasticScaling();
            scaling.setClusterId(hostGroup.getClusterId());
            scaling.setGroupName(hostGroup.getGroupName());
            scaling.setIsValid(isValid);
            confGroupElasticScalingMapper.updateValid(scaling);

            ConfGroupElasticScalingRule scalingRule = new ConfGroupElasticScalingRule();
            scalingRule.setClusterId(hostGroup.getClusterId());
            scalingRule.setGroupName(hostGroup.getGroupName());
            scalingRule.setIsValid(isValid);
            confGroupElasticScalingRuleMapper.updateValid(scalingRule);

            // 通知扩缩容服务规则有变更, 需要重新加载
            scaleService.metricChange();
            getLogger().error("实例组扩缩容完成时更新规则为完成,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid);
        } catch (Exception ex) {
            getLogger().error("实例组扩缩容完成时更新规则为异常,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid, ex);
        }
    }

    private boolean delScalingFlag(String clusterId) {
        try {
            redisLock.delete(ConfScalingTask.SCALING_FLAG + clusterId);
            getLogger().info("clusterId：" + clusterId + "，扩缩容任务完成，删除SCALING_FLAG。");
            return true;
        } catch (Exception e) {
            getLogger().error("delSclingFlag", e);
            return false;
        }
    }


    /**
     * 完成当前活动
     *
     * @param activityLog
     * @return
     */
    @Override
    public ResultMsg complateActivity(InfoClusterOperationPlanActivityLogWithBLOBs activityLog) {
        ResultMsg msg = new ResultMsg();
        try {
            activityLog.setEndtime(new Date());
            if (activityLog.getBegtime() != null) {
                Long duration =
                        activityLog.getEndtime().getTime() / 1000 - activityLog.getBegtime().getTime() / 1000;
                activityLog.setDuration(duration.intValue());
            }
            planActivityLogMapper.updateByPrimaryKeySelective(activityLog);

            if (activityLog.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT)
                    || activityLog.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED)) {
                completePlan(activityLog.getPlanId());
                saveTaskFailedEvent(activityLog);
                saveFailedVmJob(activityLog);
            }else{
                updatePlanStateAndPercent(activityLog.getPlanId());
            }
        } catch (Exception e) {
            getLogger().error("comlate activity exception:", e);
            msg.setResult(false);
        }
        return msg;
    }

    private void saveFailedVmJob(InfoClusterOperationPlanActivityLogWithBLOBs activityLog){
        try {
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLog.getPlanId());
            //扩容类任务
            if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut)){

                String scaleOutTaskId = plan.getScalingTaskId();

                //region 查询失败任务的，申请到的VM
                List<InfoClusterVm> vms = null;
                if (StringUtils.isNotEmpty(scaleOutTaskId)) {
                    vms = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(plan.getClusterId(), scaleOutTaskId);
                }
                //endregion

                if (vms!=null && vms.size()>0){
                    //失败的任务，已经成功拿回VM
                    return;
                }else {
                    //region 失败的扩容类任务，没有得到VM，需要加入到失败监控表
                    InfoClusterVmJob vmJob = vmJobMapper.getVmJobByPlanId(plan.getPlanId());
                    if (vmJob != null) {
                        InfoClusterVmReqJobFailed vmReqJobFailed = new InfoClusterVmReqJobFailed();
                        vmReqJobFailed.setClusterId(vmJob.getClusterId());
                        vmReqJobFailed.setPlanId(activityLog.getPlanId());
                        vmReqJobFailed.setJobId(vmJob.getJobId());
                        vmReqJobFailed.setStatus(InfoClusterVmReqJobFailed.STATUS_INIT);
                        vmReqJobFailed.setCreatedTime(new Date());
                        vmdeleteService.saveClusterVMJobFailed(vmReqJobFailed);
                    }
                    //endregion
                }
            }
        }catch (Exception e){
            getLogger().error("保存失败的vmJob异常，",e);
        }
    }

    //保存失败的事件
    public void saveTaskFailedEvent(InfoClusterOperationPlanActivityLogWithBLOBs activityLog){
        try {
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLog.getPlanId());
            int resultType =0;
            if (activityLog.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT)){
                resultType = TaskEventHelper.TIMEOUT;
            }
            if (activityLog.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED)){
                resultType =TaskEventHelper.FAIL;
            }
            TaskEventType taskEventType = TaskEventHelper.parseDestroyTaskType(plan.getPlanName(),resultType);
            if (Objects.isNull(taskEventType)) {
                getLogger().info("任务名暂时不支持触发任务事件. planId={}, planName={}", plan.getPlanId(), plan.getPlanName());
                return;
            }
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(plan.getClusterId());
            String clusterName = Objects.isNull(confCluster)? "": confCluster.getClusterName();
            String groupName = "";
            String vmRole = "";
            if (TaskEventType.CLEAN_TASK_SET.contains(taskEventType)) {
                // 清理异常VM,特殊处理,需要通过 op_task_id 获取planid
                InfoClusterOperationPlan parentPlan = planMapper.selectByPrimaryKey(plan.getOpTaskId());
                if (Objects.nonNull(parentPlan)) {
                    String scalingOutId = parentPlan.getScalingTaskId();
                    List<InfoClusterVm> scaleOutVms = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(plan.getClusterId(), scalingOutId);
                    if (CollectionUtil.isNotEmpty(scaleOutVms)) {
                        groupName = scaleOutVms.get(0).getGroupName();
                        vmRole = scaleOutVms.get(0).getVmRole();
                    }
                }
            } else if (TaskEventType.SCALEIN_TASK_SET.contains(taskEventType)) {
                List<InfoClusterVm> scaleInVms = infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(plan.getClusterId(), plan.getScalingTaskId());
                if (CollectionUtil.isNotEmpty(scaleInVms)) {
                    groupName = scaleInVms.get(0).getGroupName();
                    vmRole = scaleInVms.get(0).getVmRole();
                }
            }
            // 找到clusterName, groupName, vm_role 信息
            if (taskEventType !=null) {
                TaskEvent taskEvent = TaskEvent.build();
                taskEvent.setEventType(taskEventType.name());
                taskEvent.setClusterId(plan.getClusterId());
                taskEvent.setClusterName(clusterName);
                taskEvent.setGroupName(groupName);
                taskEvent.setVmRole(vmRole);
                taskEvent.setPlanId(plan.getPlanId());
                taskEvent.setEventTriggerTime(new Date());
                taskEvent.setPlanActivityLogId(activityLog.getActivityLogId());
                taskEvent.setPlanActivityLogName(activityLog.getActivityCnname());
                taskEvent.setPlanName(plan.getPlanName());
                taskEvent.setEventDesc(taskEventType.getDesc());
                taskEventService.saveTaskEvent(taskEvent);
            }
        }catch (Exception e){
            getLogger().error("保存task事件异常。",e);
        }
    }

    /**
     * 更新执行计划的状态和执行进度进度
     *
     * @param planId
     * @return
     */
    @Override
    public ResultMsg updatePlanStateAndPercent(String planId){
        ResultMsg msg = new ResultMsg();
        InfoClusterOperationPlan plan = new InfoClusterOperationPlan();
        plan.setPlanId(planId);
        try {
            ResultMsg stateMsg = computePlanStateAndPercent(planId);
            if (stateMsg.getResult()) {
                Map<String, Integer> stateMap = (Map<String, Integer>) stateMsg.getData();
                plan.setState(stateMap.get("state"));
                plan.setPercent(Double.valueOf(stateMap.get("percent")));
                planMapper.updatePlanStateAndPercent(plan);
                msg.setResult(true);
                return msg;
            }else{
                return stateMsg;
            }
        }catch (Exception e){
            getLogger().error("更新执行计划状态异常：",e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }


    @Override
    public ResultMsg updatePlanName(String planId) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);
            String planName = getPlanName(plan.getClusterId(), plan.getOperationType(), plan.getScalingTaskId(), plan.getPlanId());
            plan.setPlanName(planName);
            planMapper.updatePlanName(plan);
            msg.setResult(true);
            return msg;
        }catch (Exception e){
            msg.setResult(false);
            getLogger().error("更新执行计划名称异常",e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }

    /**
     * 通用的创建一个执行计划<br/>
     * 根据operationName,找到对应的执行计划模板,然后生成一个执行计划并立即执行计划
     *
     * @param clusterId     集群ID
     * @param realseVersion 发行版本号
     * @param operationName 操作名称
     * @param taskId        扩容任务ID
     * @param opTaskid      操作任务ID
     * @return
     */
    @Override
    public ResultMsg createPlanAndRun(String clusterId,
                                      String realseVersion,
                                      String operationName,
                                      String taskId,
                                      String opTaskid) {
        ResultMsg msg = new ResultMsg();

        // 1.获取模版信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("releaseVersion", realseVersion);
        paramMap.put("operationName", operationName);

        getLogger().info("查询baseTemplate，paramMap："+paramMap.toString());

        List<BaseClusterOperationTemplate> templateList =
                baseClusterOperationTemplateMapper.selectByCondition(paramMap);

        if (templateList == null) {
            msg.setResult(false);
            msg.setErrorMsg("Not found templete for this release version and operation:"
                    + realseVersion + "& " + operationName);
            return msg;
        }

        BaseClusterOperationTemplate basetemplate = templateList.get(0);

        //2.获取该模版下的活动

        List<BaseClusterOperationTemplateActivity> activityList =
                templateActivityMapper.selectByTemplateId(basetemplate.getTemplateId());

        if (null == activityList || activityList.size() == 0) {
            msg.setResult(false);
            msg.setErrorMsg("Not found activitys for this template:"
                    + basetemplate.getTemplateId());
            return msg;
        }
        getLogger().info("构建执行计划开始。taskId:{}", taskId);
        //3.构建执行计划对象并保存
        msg = buildAndSavePlan(clusterId, basetemplate, activityList, taskId, opTaskid);

        getLogger().info("构建执行计划结果：" + msg.toString());

        //4.启动执行计划
        if (msg.getResult() &&
                StringUtils.isNotEmpty(msg.getBizid()) &&
                !operationName.equalsIgnoreCase("runuserscript")) {
            startPlan(msg.getBizid());
        } else {
            //创建计划失败
            createPlanFailedProcess(clusterId, realseVersion, operationName, taskId, msg);
        }
        return msg;
    }

    /**
     * 获取计划任务名称
     *
     * @param clusterId
     * @param operationName
     * @param taskId
     * @param planId
     * @return
     */
    private String getPlanName(String clusterId,
                               String operationName,
                               String taskId,
                               String planId){
        //region 普通任务创建

        if (operationName.equalsIgnoreCase("create")){
            return "创建集群";
        }

        if (operationName.equalsIgnoreCase("delete")){
            return "销毁集群";
        }

        if (operationName.equalsIgnoreCase("restartservice")){
            return "重启服务";
        }

        if (operationName.equalsIgnoreCase("clearvms")){
            return "清理异常VM";
        }

        if (operationName.equalsIgnoreCase("collectLogs")){
            return "日志收集";
        }

        if (operationName.equalsIgnoreCase("scaleoutpart")){
            return "磁盘扩容";
        }

        if (operationName.equalsIgnoreCase("runuserscript")){
            return "用户自定义脚本";
        }

        if (operationName.equalsIgnoreCase("scaleoutEvictVm")){
            return "补全驱逐VM";
        }

        if (operationName.equalsIgnoreCase("pv2DiskAdjust")){
            return "pv2磁盘性能调整";
        }

        //endregion
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);
        //region 扩容
        if (operationName.equalsIgnoreCase("scaleout")){
            if (StringUtils.isNotEmpty(taskId)) {
                ConfScalingTask scalingTask = scalingTaskMapper.selectByPrimaryKey(taskId);

                if (scalingTask.getOperatiionType() == null){
                    return "手动扩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_Scaling) &&
                        confCluster.getCreationSubState() != null &&
                        confCluster.getCreationSubState().equalsIgnoreCase("RUNNING")){
                    return "增量创建扩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_Scaling) &&
                        (StringUtils.isEmpty(confCluster.getCreationSubState()) ||
                        !confCluster.getCreationSubState().equalsIgnoreCase("RUNNING"))){
                    return "弹性扩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_UserManual)){
                    if (scalingTask.getBeforeScalingCount().equals(0)){
                        return "新增实例组";
                    }
                    return "手动扩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_create_group)){
                    return "新增实例组";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)){
                    return "竞价买入";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_Complete_Evict_Vm)){
                    return "补全驱逐VM";
                }
            }
        }
        //endregion
        //region 缩容
        if (operationName.equalsIgnoreCase("scalein")){
            if (StringUtils.isNotEmpty(taskId)) {
                ConfScalingTask scalingTask = scalingTaskMapper.selectByPrimaryKey(taskId);

                if (scalingTask.getOperatiionType() == null){
                    return "手动缩容";
                }

                if (scalingTask.getDeleteGroup() != null && scalingTask.getDeleteGroup().equals(1)){
                    return "删除实例组";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_Scaling)){
                    return "弹性缩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_UserManual)){
                    return "手动缩容";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)){
                    return "竞价逐出";
                }

                if (scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_delete_Task_Vm)){
                    return "删除实例";
                }

            }
        }
        //endregion
        getLogger().error("plan_id not fount plan_name:"+planId);
        return null;
    }

    /**
     * 创建计划失败的处理逻辑
     *
     * @param clusterId
     * @param realseVersion
     * @param opreationName
     * @param taskId
     * @return
     */
    private ResultMsg createPlanFailedProcess(String clusterId,
                                              String realseVersion,
                                              String opreationName,
                                              String taskId,
                                              ResultMsg errorMsg) {

        getLogger().info("创建计划失败：clusterId：{},realseVersion:{},OpreationName:{},taskId:{}"
                , clusterId, realseVersion, opreationName, taskId);
        ResultMsg msg = new ResultMsg();
        try {
            // 扩缩容创建计划任务失败，confscalingtask 状态变更为失败，不影响下一个任务提交
            if (Objects.equals(opreationName, InfoClusterOperationPlan.Plan_OP_ScaleIn)
                    || Objects.equals(opreationName, InfoClusterOperationPlan.Plan_OP_ScaleOut)) {
                ConfScalingTask task = scalingTaskMapper.selectByPrimaryKey(taskId);
                task.setState(ConfScalingTask.SCALINGTASK_Failed);
                task.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
                task.appendRemark("创建计划失败" + opreationName + "," + errorMsg.getErrorMsg());
                task.setEndTime(new Date());
                scalingTaskMapper.updateByPrimaryKeySelective(task);
                spotScalingTaskProcess(task);
                msg.setResult(true);
            }
        } catch (Exception e) {
            getLogger().error("创建计划失败,变更任务状态异常", e);
        }
        return msg;
    }


    /**
     * 创建并保存执行计划
     *
     * @param clusterId    集群ID
     * @param template     模版对象
     * @param activityList 活动对象列表
     * @param taskId       扩缩容任务ID
     * @return
     */
    private ResultMsg buildAndSavePlan(String clusterId,
                                       BaseClusterOperationTemplate template,
                                       List<BaseClusterOperationTemplateActivity> activityList,
                                       String taskId,
                                       String opTaskId) {
        getLogger().info("buildAndSavePlan clusterId:{},template:{},activityList:{},taskId:{},opTaskId:{}",
                clusterId,
                template,
                activityList,
                taskId,
                opTaskId);

        ResultMsg msg = new ResultMsg();
        String planName = getPlanName(clusterId,template.getOperationName(),taskId,null);
        String lock_task_id = "";
        String lock_opTaskId = "";

        if (StringUtils.isNotEmpty(taskId)) {
            lock_task_id = taskId;
        }
        if (StringUtils.isNotEmpty(opTaskId)) {
            lock_opTaskId = opTaskId;
        }

        String lockKey = LockPrefixConstant.Create_Plan + clusterId + lock_task_id + lock_opTaskId;
        boolean lock = false;
        try {
            // 0. 幂等处理
            lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 10, 600);
            if (lock) {
                // 1.计划数据构建
                InfoClusterOperationPlan plan = planMapper.getPlanByScalingTaskId(clusterId, taskId);
                if (plan != null) {
                    getLogger().error("buildAndSavePlan build error because operation plan has created,clusterId:{},taskId:{}", clusterId, taskId);
                    msg.setErrorMsg("任务不能重复启动");
                    msg.setResult(false);
                    return msg;
                }

                plan = new InfoClusterOperationPlan();
                plan.setTemplateId(template.getTemplateId());
                plan.setClusterId(clusterId);
                //v2.0新增扩容任务ID
                plan.setScalingTaskId(taskId);
                //v2.0.0210 新增 操作任务ID
                plan.setOpTaskId(opTaskId);
                plan.setOperationType(template.getOperationName());
                plan.setPlanId(UUID.randomUUID().toString().replaceAll("-", ""));
                plan.setCreatedby("system");
                plan.setCreatedTime(new Date());
                plan.setPlanName(planName);
                plan.setState(InfoClusterOperationPlan.Plan_State_Running);
                plan.setPercent(0d);

                // 2.活动明细构建

                List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogs = new ArrayList<>();

                InfoClusterOperationPlan finalPlan = plan;
                activityList.stream().forEach(x -> {
                    InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                            new InfoClusterOperationPlanActivityLogWithBLOBs();
                    activityLog.setActivityLogId(UUID.randomUUID().toString().replaceAll("-", ""));
                    activityLog.setPlanId(finalPlan.getPlanId());
                    activityLog.setActivityId(x.getActivityId());
                    activityLog.setActivityName(x.getActivityName());
                    activityLog.setActivityType(x.getActivityType());
                    activityLog.setActivityCnname(x.getActivityCnname());
                    activityLog.setTemplateId(x.getTemplateId());
                    activityLog.setSortNo(x.getSortNo());
                    activityLog.setTimeout(x.getTimeout());
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_PLANING);
                    activityLog.setCreatedby("system");
                    activityLog.setCreatedTime(new Date());
                    activityLogs.add(activityLog);
                });

                // 3.保存数据
                planMapper.insert(plan);
                planActivityLogMapper.insertBatch(activityLogs);

                msg.setBizid(plan.getPlanId());
                msg.setResult(true);
            } else {
                msg.setErrorMsg("系统繁忙，获取createPlan锁失败。");
            }
        } catch (Exception e) {
            msg.setResult(false);
            getLogger().error("build and save plan exception:", e);
            msg.setErrorMsg("build and save plan exception");
        } finally {
            try {
                if (lock) {
                    redisLock.unlock(lockKey);
                }
            } catch (Exception e) {
                getLogger().error("锁释放异常，", e);
            }

        }
        return msg;
    }

    /**
     * 订阅创建和销毁虚拟机job异步通知消息
     * 包含 创建完成消息，销毁操作完成消息
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg receiveVmJobMessage(String message) {
        getLogger().info("VM OP's job异步通知消息:" + message);
        JSONObject ms = JSON.parseObject(message);
        JSONObject jsonmessage = ms.getJSONObject("Data");

        if (jsonmessage != null && jsonmessage.containsKey("type")) {

            if (jsonmessage.getString("type")
                    .equalsIgnoreCase("CreateVirtualMachines")) {
                //创建虚拟机完成消息
                String cvmjobId = jsonmessage.getString("id");
                InfoClusterVmJob vmJob = vmJobMapper.getVmJobByJobId(cvmjobId);
                try {
                    //MDC.put(ComposeConstant.Cluster_ID, vmJob.getClusterId()+"_callback");
                } catch (Exception e) {
                    getLogger().error("message MDC Ex,", e);
                }

                if (vmJob.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create)) {
                    // return ivmService.commonSaveVMInfo(vmJob.getActivityLogId(), cvmjobId, jsonmessage);
                }
                if (vmJob.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut)) {
                    //return ivmService.saveClusterAppendVminfo(jsonmessage.toJSONString());
                }
            }

            if (jsonmessage.getString("type")
                    .equalsIgnoreCase("DeleteResourceGroup")) {
                // 销毁集群虚拟机的消息
                // todo

            }
        }
        return null;
    }

    /**
     * 根据activityLogId获取
     *
     * @param activityLogId
     * @return
     */
    @Override
    public InfoClusterOperationPlanActivityLogWithBLOBs getInfoActivityLogByLogId(String activityLogId) {
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                planActivityLogMapper.selectByPrimaryKey(activityLogId);
        return activityLog;
    }

    /**
     * 根据activityId获取confCluster
     *
     * @param activityLogId
     * @return
     */
    @Override
    public ConfCluster getConfClusterByActivityLogId(String activityLogId) {
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                planActivityLogMapper.selectByPrimaryKey(activityLogId);
        if (activityLog == null) {
            return null;
        }
        InfoClusterOperationPlan plan =
                planMapper.selectByPrimaryKey(activityLog.getPlanId());

        if (plan == null) {
            getLogger().error("Plan数据为空，" + activityLog.getPlanId());
            return null;
        }

        return clusterMapper.selectByPrimaryKey(plan.getClusterId());
    }

    /**
     * 根据planid获取执行计划第一个activity
     *
     * @param planId
     * @return
     */
    @Override
    public InfoClusterOperationPlanActivityLogWithBLOBs getFirstActivity(String planId) {
        return planActivityLogMapper.getFirstActivity(planId);
    }

    /**
     * 判断Activity是否 是否超时
     * 超时的任务不再loop，并截断计划
     *
     * @param activityLogId
     * @return
     */
    @Override
    public ResultMsg checkActivityLogTimeout(String activityLogId) {

        ResultMsg msg = new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                planActivityLogMapper.selectByPrimaryKey(activityLogId);

        // 运行中的活动
        if (activityLog.getBegtime() != null && activityLog.getState() == 1) {
            Long duri = new Date().getTime() / 1000 - activityLog.getBegtime().getTime() / 1000;
            Long timeout = Long.valueOf(activityTimeout);

            if (null != activityLog.getTimeout() && activityLog.getTimeout() > 0) {
                timeout = Long.parseLong(activityLog.getTimeout() + "");
            }
            if (duri > timeout) {
                msg.setResult(true);
                activityLog.setState(-1);
                activityLog.setLogs("timeout,value:" + timeout);
                activityLog.setEndtime(new Date());
                planActivityLogMapper.updateByPrimaryKeySelective(activityLog);
                getLogger().info("timetout:" + activityLogId);
            }
        }
        return msg;
    }

    /**
     * 活动重复
     *
     * @param clientName  发送延时消息的客户端name，
     *                    需要在配置中心的配置
     * @param message     需要传递的消息
     * @param delaySecond 需要延时的second
     * @return
     */
    @Override
    public ResultMsg loopActivity(String clientName, String message,
                                  Long delaySecond, String activityLogId) {
        ResultMsg msg = new ResultMsg();

        // 1. 判断集群是否可用
        // 集群不可用
        if (!planClusterAvailable(activityLogId)) {
            return msg;
        }

        // 2. 判断是否超时

        ResultMsg msg1 = checkActivityLogTimeout(activityLogId);

        if (msg1.getResult()) {
            // 流程超时，重置集群状态
            InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                    planActivityLogMapper.selectByPrimaryKey(activityLogId);
            if (activityLog != null) {
                InfoClusterOperationPlan infoClusterOperationPlan = planMapper.selectByPrimaryKey(activityLog.getPlanId());
                if (Objects.equals(infoClusterOperationPlan.getOperationType(), InfoClusterOperationPlan.Plan_OP_Create)) {
                    String clusterId = infoClusterOperationPlan.getClusterId();
                    updatePlanStateAndPercent(activityLog.getPlanId());
                    clusterMapper.updateClusterState(clusterId, ConfCluster.FAILED);
                }else{
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT);
                    complateActivity(activityLog);
                }

            }
            // 超时，直接结束退出loop
            return msg1;
        }

        try {
            getLogger().info("发送延时消息开始");
            producerService.sendScheduleMessage(clientname, message, delaySecond);
            getLogger().info("发送延时消息结束。");
            msg.setResult(true);
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg("发送延时消息异常:" + ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * 重试活动
     *
     * @param activityLogId 活动ID
     * @return
     */
    @Override
    public ResultMsg retryActivity(String activityLogId) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterOperationPlanActivityLogWithBLOBs activity =
                    planActivityLogMapper.selectByPrimaryKey(activityLogId);

            if (activity.getActivityId().equalsIgnoreCase(blackactlog)) {
                msg.setResult(false);
                msg.setErrorMsg("当前步骤重试暂不可用。");
                return msg;
            }
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activity.getPlanId());

            // 运行中和待执行
            if (activity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_PLANING) ||
                    activity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_RUNNING)) {
                msg.setResult(false);
                msg.setErrorMsg("待执行和执行中的活动状态不支持重试。");
                return msg;
            }

            String param = activity.getParaminfo();
            JSONObject paramjson = JSON.parseObject(param);
            paramjson.put("retry_time", System.currentTimeMillis());


            // 失败步骤执行
            if (activity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED) ||
                    activity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT)) {
                producerService.sendMessage(clientname, paramjson.toJSONString());
                getLogger().info("执行步骤状态为:{}, 重试本步骤, 并将状态修改为待执行. 步骤ID={}, 名称={}",
                        activity.getState(), activity.getActivityId(), activity.getActivityCnname() );
                // 更新当前步骤状态为待执行.
                activity.setBegtime(new Date());
                activity.setState(InfoClusterOperationPlanActivityLog.ACTION_PLANING);
                activity.setEndtime(null);
                planActivityLogMapper.updateByPrimaryKey(activity);
                // 更新集群状态为运行中
                setClusterStateToRunning(plan.getClusterId(), plan.getOperationType());
            }

            // 成功步骤执行
            if (activity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED)) {
                InfoClusterOperationPlanActivityLogWithBLOBs nextActivity
                        = getNextActivity(plan.getPlanId(), activityLogId);

                if (nextActivity != null && (nextActivity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED) ||
                        nextActivity.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT))) {

                    // 重置当前以及后续活动的开始结束时间
                    List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogWithBLOBsList =
                            planActivityLogMapper.getAllActivity(activity.getPlanId());
                    activityLogWithBLOBsList.stream().forEach(x -> {
                        if (Objects.equals(x.getSortNo(), activity.getSortNo())) {
                            x.setBegtime(new Date());
                            x.setState(InfoClusterOperationPlanActivityLog.ACTION_PLANING);
                            x.setEndtime(null);
                            planActivityLogMapper.updateByPrimaryKey(x);
                        }
                        if (x.getSortNo().compareTo(activity.getSortNo()) > 1) {
                            x.setBegtime(null);
                            x.setState(InfoClusterOperationPlanActivityLog.ACTION_PLANING);
                            x.setEndtime(null);
                            planActivityLogMapper.updateByPrimaryKey(x);
                        }
                    });
                    producerService.sendMessage(clientname, paramjson.toJSONString());
                    setClusterStateToRunning(plan.getClusterId(), plan.getOperationType());
                } else {
                    msg.setResult(false);
                    msg.setErrorMsg("下一活动状态为执行失败或执行超时，当前活动才可以重试。");
                    return msg;
                }
            }
            msg.setResult(true);
        } catch (Exception e) {
            getLogger().error("retryActivity,", e);
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 超时巡检任务，自动重试活动专用
     *
     * @param activityLogId
     * @return
     */
    @Override
    public ResultMsg autoRetryActivityForTimeOut(String activityLogId) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterOperationPlanActivityLogWithBLOBs activity =
                    planActivityLogMapper.selectByPrimaryKey(activityLogId);
            String param = activity.getParaminfo();
            JSONObject paramjson = JSON.parseObject(param);
            paramjson.put("retry_time", System.currentTimeMillis());
            producerService.sendMessage(clientname, paramjson.toJSONString());
            getLogger().info("超时巡检任务，自动重试活动,发送重试消息完成。");
            msg.setResult(true);
        }catch (Exception e){
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }

    private void setClusterStateToRunning(String clusterId, String operationType) {
        try {
            //重试更新集群状态
            if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create)) {
                clusterMapper.updateClusterState(clusterId, ConfCluster.CREATING);
            }
            if (operationType.equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Delete)) {
                clusterMapper.updateClusterState(clusterId, ConfCluster.DELETING);
            }

        } catch (Exception ex) {
            // 出异常后，不抛出。
            getLogger().error(ex.getMessage(), ex);
        }
    }

    public void updateDestroyTaskAndCache(List<String> clusterIds, String destroyStatus, Date startDestroyTime, Date endDestroyTime){
        updateDestroyTask(clusterIds, destroyStatus, startDestroyTime, endDestroyTime);
        updateRedisLimit(clusterIds);
    }
    public void updateDestroyTask(List<String> clusterIds, String destroyStatus, Date startDestroyTime, Date endDestroyTime){
        if (CollectionUtils.isEmpty(clusterIds)) {
            return;
        }
        clusterDestroyTaskMapper.updateBatchTaskById(clusterIds, destroyStatus, startDestroyTime, endDestroyTime);
    }
    /**
     * 删除集群id的缓存集合
     * @param clusterIdList
     */
    public void updateRedisLimit(List<String> clusterIdList) {
        if (CollectionUtils.isEmpty(clusterIdList)) {
            return;
        }
        for (String id : clusterIdList) {
            redisLock.mapRemove(CLUSTERDAEMONTASK_CLUSTER_STATUS, id);
            redisLock.mapRemove(CLUSTERDAEMONTASK_CLUSTER_TIME, id);
        }
    }

    /**
     * 实例扩容之后判断是否需要更新磁盘IOPS和吞吐量
     * 1, 判断是否是PV2类型的磁盘,且磁盘IOPS大于3000或吞吐量大于125
     * @param clusterId
     * @param groupName
     * @param vmRole
     * @param scalingTaskId
     */
    private void pv2DiskScalingTask(String clusterId, String groupName, String vmRole, String scalingTaskId) {
        // 获取实例组信息
        List<ConfClusterVm> confClusterVms = confClusterVmMapper.getVmConfsByGroupName(groupName, clusterId);
        if (CollUtil.isEmpty(confClusterVms)) {
            getLogger().error("更新pv2磁盘iops和吞吐量获取实例配置失败!");
            return;
        }

        // 获取实例组的磁盘信息
        ConfClusterVm confClusterVm = confClusterVms.get(0);
        List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(confClusterVm.getVmConfId());
        if (CollUtil.isEmpty(confClusterVmDataVolumes)) {
            getLogger().error("更新pv2磁盘iops和吞吐量获取数据盘信息失败!");
            return;
        }

        // 生成设置Pv2磁盘信息的任务
        ConfClusterVmDataVolume dataVolume = confClusterVmDataVolumes.get(0);
        if (DataVolumeType.PremiumV2_LRS.name().equals(dataVolume.getDataVolumeType()) &&
                (dataVolume.getIops() > 3000 || dataVolume.getThroughput() > 125)) {
            confScalingTaskService.savePv2DiskScalingTask(clusterId,
                    groupName,
                    vmRole,
                    dataVolume.getIops(),
                    dataVolume.getThroughput(),
                    scalingTaskId);
        } else {
            getLogger().info("不是pv2磁盘不需要更新ops和吞吐量获取数据盘信息失败!");
        }
    }
}
