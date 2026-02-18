package com.sunbox.sdpspot.task;

import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.ScaleMethods;
import com.sunbox.domain.enums.SpotGroupScaleTaskStates;
import com.sunbox.domain.enums.SpotPriceStrategy;
import com.sunbox.runtime.RuntimeManager;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdpspot.constant.RedisConst;
import com.sunbox.sdpspot.manager.AzureServiceManager;
import com.sunbox.sdpspot.manager.ScheduleEventManager;
import com.sunbox.sdpspot.mapper.*;
import com.sunbox.sdpspot.model.ClusterHostGroupNode;
import com.sunbox.sdpspot.model.NeedScaleOutGroupResult;
import com.sunbox.sdpspot.util.DistributedRedisSpot;
import com.sunbox.service.IAzureService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * 竞价实例调度管理任务
 */
@Component
public class SpotScaleOutManageTask implements BaseCommonInterFace {
    private static final long VM_CREATION_CD_5MIN = 5 * 60 * 1000;
    private static final long VM_HEALTH_UPDATE_CD_60SEC = 60 * 1000;
    private static final long VM_HEALTH_TIMEOUT_20MIN = 20 * 60 * 1000;

    private static final long TASK_TIMEOUT_TICKS_5M = 5 * 60 * 1000;

    private static final long TASK_TIMEOUT_TICKS_30M = 30 * 60 * 1000;
    private static final int SCHEDULE_EVENT_HTTP_PORT = 7374;

    @Value("${spot.pre.scale.out.count:50}")
    private Integer PRE_TASK_SCALE_OUT_COUNT;

    @Autowired
    private DistributedRedisLock distributedRedisLock;

    @Autowired
    private DistributedRedisSpot spotRedissonClient;

    @Autowired
    InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    ConfClusterVmMapper confClusterVmMapper;

    @Autowired
    ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    InfoSpotGroupScaleTaskMapper infoSpotGroupScaleTaskMapper;

    @Autowired
    InfoSpotGroupScaleTaskItemMapper infoSpotGroupScaleTaskItemMapper;

    @Autowired
    ComposeService composeService;

    @Autowired
    RuntimeManager runtimeManager;

    @Autowired
    ConfScalingTaskMapper confScalingTaskMapper;

    @Autowired
    ScheduleEventManager scheduleEventManager;

    @Autowired
    IAzureService azureService;

    @Autowired
    ConfClusterMapper confClusterMapper;
    @Autowired
    ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;


    private static ThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(200, 1000,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(800), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Scheduled(cron = "${spot.scale.manage.task.time}")
    public void start() {
        runtimeManager.withCounter(() -> {
            if (SpotScaleInManageTask.clusterGroupHostNodes == null) {
                getLogger().debug("clusterGroupHostNodes == null");
                return;
            }

            if (SpotScaleInManageTask.clusterGroupHostNodes.isEmpty()) {
                getLogger().debug("clusterGroupHostNodes.isEmpty()");
                return;
            }

            for (ClusterHostGroupNode clusterHostGroupNode : SpotScaleInManageTask.clusterGroupHostNodes) {
                if (runtimeManager.isShuttingDown()) {
                    this.getLogger().debug("return isShuttingDown=true, clusterId:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                    return;
                }

                if (clusterHostGroupNode.getExpectCount() == null) {
                    this.getLogger().warn("not found expect count, clusterId:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                    continue;
                }

                int activeCount = executor.getActiveCount();
                if (executor.getActiveCount() > 1000) {
                    getLogger().warn("spot lock executor activeCount:{} too large", activeCount);
                    continue;
                }

                // 0 关闭竞价买入和缩容流程; 1 关闭买入，开放缩容; 2 开发买入，关闭缩容; 3 开放买入和缩容
                // 此处判断是否需要进行扩容.
                if(clusterHostGroupNode.getSpotState() != null
                        && (clusterHostGroupNode.getSpotState() == 0
                        || clusterHostGroupNode.getSpotState() == 1)) {
                    this.getLogger().warn("pause spot scale out, clusterId:{}, groupId:{}, spotState:{}",
                            clusterHostGroupNode.getClusterId(),
                            clusterHostGroupNode.getGroupId(),
                            clusterHostGroupNode.getSpotState());
                    continue;
                }

                Future<?> submit = executor.submit(() -> {
                    String lockKey = RedisConst.keyLockScaleOutGroup(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                    boolean lockSuccess = distributedRedisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
                    if (!lockSuccess) {
                        getLogger().debug("try spot lock distribute lock fail, key:{}", lockKey);
                        return;
                    }
                    try {
                        getLogger().debug("spot lock distribute lock, key:{}", lockKey);
                        scaleOutGroup(clusterHostGroupNode);
                    } catch (Exception e) {
                        getLogger().error("scale group error, clusterId:" + clusterHostGroupNode.getClusterId() + ", groupId:" + clusterHostGroupNode.getGroupId(), e);
                    } finally {
                        distributedRedisLock.tryUnlock(lockKey);
                        getLogger().debug("unlock distribute lock, key:{}", lockKey);
                    }
                });
            }
        });
    }

    /**
     * 扩容一个实例组
     * @param clusterGroup
     */
    private void scaleOutGroup(ClusterHostGroupNode clusterGroup) {
        NeedScaleOutGroupResult needScaleOutGroupResult = needScaleOutGroup(clusterGroup);
        if (!needScaleOutGroupResult.isNeedScale()) {
            getLogger().debug("skip scaleOutGroup, needScaleOutGroup = false, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (hasOtherScaleLockKey(clusterGroup)) {
            getLogger().debug("skip scaleOutGroup, hasOtherScaleLockKey = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (isGroupScaling(clusterGroup)) {
            getLogger().debug("skip scaleOutGroup, isGroupScaling = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (hasOtherScaleTask(clusterGroup)) {
            getLogger().debug("skip scaleOutGroup, hasOtherScaleTask(from db) = true, clusterId:{}, vmRole:{}, groupId:{}, groupName:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId(),
                    clusterGroup.getGroupName());
            return;
        }

        if (waitLastFailureScaleOutTask(clusterGroup)) {
            getLogger().debug("skip scaleOutGroup, waitLastFailureScaleTask(from db) = true, clusterId:{}, vmRole:{}, groupId:{}, groupName:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId(),
                    clusterGroup.getGroupName());
            return;
        }

        if (hasSpotScaleTask(clusterGroup)) {
            getLogger().info("skip scaleOutGroup, hasSpotScaleTask(from db) = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (!checkSpotBidPrice(clusterGroup)) {
            getLogger().error("skip scaleOutGroup, checkSpotBidPrice = false, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        InfoSpotGroupScaleTask spotGroupScaleOutTask = createSpotGroupScaleOutTask(clusterGroup, needScaleOutGroupResult.getRequireCount());

        invokeSpotGroupScaleOutService(clusterGroup, spotGroupScaleOutTask, needScaleOutGroupResult.getRequireCount());
    }

    private boolean waitLastFailureScaleOutTask(ClusterHostGroupNode clusterGroup) {
        List<ConfScalingTask> lastTasks = confScalingTaskMapper.findLast3TaskOrderByDesc(clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                clusterGroup.getGroupName(),
                ConfScalingTask.ScaleType_OUT);
        if (lastTasks.isEmpty()) {
            return false;
        }

        ConfScalingTask lastTask = lastTasks.get(0);
        if (lastTask.getEndTime() == null) {
            return false;
        }

        if (!Objects.equals(lastTask.getState(), ConfScalingTask.SCALINGTASK_Failed)) {
            return false;
        }

        int waitTimeCounter = 0;
        for (int index = 0; index < lastTasks.size(); index++) {
            ConfScalingTask confScalingTask = lastTasks.get(index);
            if (!Objects.equals(confScalingTask.getState(), ConfScalingTask.SCALINGTASK_Failed)) {
                break;
            }
            waitTimeCounter++;
        }

        long cdTime = 0;
        if (waitTimeCounter >= 3) {
            cdTime = 10 * 60000;
        } else {
            cdTime = 5 * 60000;
        }

        long nowTicks = System.currentTimeMillis();
        String cdLockKey = "SPOT_SCALE_OUT_CD:" + clusterGroup.getClusterId() + ":" + clusterGroup.getGroupId();
        if (nowTicks - lastTask.getEndTime().getTime() < cdTime) {
            long cdTimeSeconds = ((nowTicks - lastTask.getEndTime().getTime()) / 1000);
            if (!this.distributedRedisLock.haveKey(cdLockKey)) {
                getLogger().info("hasSpotScaleTask set SPOT_SCALE_OUT_CD,clusterId:{},groupId:{},cdTimeSeconds:{}",
                        clusterGroup.getClusterId(),
                        clusterGroup.getGroupId(),
                        cdTimeSeconds);
            }
            this.distributedRedisLock.trySaveValueAsString(getLogger(),
                    cdLockKey,
                    "1",
                    cdTimeSeconds);
            return true;
        }

        this.distributedRedisLock.delete(cdLockKey);
        getLogger().info("hasSpotScaleTask delete SPOT_SCALE_OUT_CD,clusterId:{},groupId:{}", clusterGroup.getClusterId(), clusterGroup.getGroupId());
        return false;
    }

    /**
     * 是否存在其它在进行中的任务
     *
     * @param clusterGroup 竞价实例组
     * @return true存在，false不存在
     */
    private boolean hasOtherScaleTask(ClusterHostGroupNode clusterGroup) {
        int otherRunningTaskCount = confScalingTaskMapper.countByClusterIdAndClusterNameAndState(clusterGroup.getClusterId(),
                clusterGroup.getGroupName(),
                ConfScalingTask.SCALINGTASK_Create,
                ConfScalingTask.SCALINGTASK_Running);
        getLogger().debug("hasSpotScaleTask hasOtherScaleTask(from db):{}", otherRunningTaskCount);
        if (otherRunningTaskCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 检查竞价实例出价
     * true:能出价，false不能出价
     *
     * @param clusterHostGroupNode
     * @return
     */
    private boolean checkSpotBidPrice(ClusterHostGroupNode clusterHostGroupNode) {
        getLogger().debug("check spot bit price, clusterId:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());

        List<ConfClusterVm> confClusterVms = confClusterVmMapper.listByClusterId(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId(), ConfClusterVm.PURCHASETYPE_SPOT);
        if (confClusterVms.isEmpty()) {
            getLogger().error("not found any confClusterVm, clusterId:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
            return false;
        }

        ConfClusterVm item = confClusterVms.get(0);
        Logger logger = getLogger();
        if (!SpotPriceStrategy.validate(item.getPriceStrategy())) {
            logger.error("skip build azure vm parameter because price strategy is not invalid, clusterId:{}, groupId:{}, skuName:{}, max price:{}, price strategy:{}",
                    item.getClusterId(),
                    item.getGroupId(),
                    item.getSku(),
                    item.getMaxPrice(),
                    item.getPriceStrategy());
            return false;
        }
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(item.getClusterId());
        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByClusterIdAndGroupId(item.getClusterId(), item.getGroupId());

        return AzureServiceManager.canBuildAzureSpotProfile(azureService, logger, confCluster,item,confHostGroupVmSkus);
    }

    /**
     * 是否存在进行中的任务
     *
     * @param clusterGroup 竞价实例组
     * @return true存在，false不存在
     */
    private boolean hasSpotScaleTask(ClusterHostGroupNode clusterGroup) {
        InfoSpotGroupScaleTask runningTask = infoSpotGroupScaleTaskMapper.findLatestByClusterIdAndNotState(clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                SpotGroupScaleTaskStates.EXECUTE_SUCCESS.getValue(),
                SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
        if (runningTask != null) {
            if (runningTask.getCreatedTime() == null) {
                terminateTask(runningTask);
                getLogger().debug("hasSpotScaleTask = false, not found createdTime from task, clusterId:{}, groupId:{}, taskId:{}",
                        runningTask.getClusterId(),
                        runningTask.getGroupId(),
                        runningTask.getTaskId());
                return false;
            } else {
                long durationTime = System.currentTimeMillis() - runningTask.getCreatedTime().getTime();
                if (durationTime > TASK_TIMEOUT_TICKS_5M && durationTime < TASK_TIMEOUT_TICKS_30M) {
                    ConfScalingTask confScalingTask = this.confScalingTaskMapper.selectByPrimaryKey(runningTask.getClusterId(), runningTask.getTaskId());
                    if (confScalingTask != null) {
                        if (confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Complete)) {
                            successTask(runningTask);
                            getLogger().debug("hasSpotScaleTask = false, task is success(found scaling task success), clusterId:{}, groupId:{}, taskId:{}",
                                    runningTask.getClusterId(),
                                    runningTask.getGroupId(),
                                    runningTask.getTaskId());
                            return false;
                        } else if (confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Failed)) {
                            failureTask(runningTask);
                            getLogger().debug("hasSpotScaleTask = false, task is failure(found scaling task failure), clusterId:{}, groupId:{}, taskId:{}",
                                    runningTask.getClusterId(),
                                    runningTask.getGroupId(),
                                    runningTask.getTaskId());
                            return false;
                        } else {
                            getLogger().debug("hasSpotScaleTask = true, task is running(found scaling task), clusterId:{}, groupId:{}, taskId:{}",
                                    runningTask.getClusterId(),
                                    runningTask.getGroupId(),
                                    runningTask.getTaskId());
                            return true;
                        }
                    } else {
                        terminateTask(runningTask);
                        getLogger().debug("hasSpotScaleTask = false, task is timeout(not found scaling task), clusterId:{}, groupId:{}, taskId:{}",
                                runningTask.getClusterId(),
                                runningTask.getGroupId(),
                                runningTask.getTaskId());
                        return false;
                    }
                } else if (durationTime > TASK_TIMEOUT_TICKS_30M) {
                    terminateTask(runningTask);
                    getLogger().debug("hasSpotScaleTask = false, task is timeout, clusterId:{}, groupId:{}, taskId:{}",
                            runningTask.getClusterId(),
                            runningTask.getGroupId(),
                            runningTask.getTaskId());
                    return false;
                } else {
                    getLogger().debug("hasSpotScaleTask = true, task is running, clusterId:{}, groupId:{}, taskId:{}",
                            runningTask.getClusterId(),
                            runningTask.getGroupId(),
                            runningTask.getTaskId());
                    return true;
                }
            }
        }
        return false;
    }

    private void terminateTask(InfoSpotGroupScaleTask task) {
        try {
            task.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
            task.setModifiedby("task");
            task.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateState(task);
            getLogger().debug("terminateTask, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId());
        } catch (Exception e) {
            getLogger().error("terminateTask error, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId(),
                    e);
        }
    }

    private void successTask(InfoSpotGroupScaleTask task) {
        try {
            task.setState(SpotGroupScaleTaskStates.EXECUTE_SUCCESS.getValue());
            task.setModifiedby("task");
            task.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateState(task);
            getLogger().debug("successTask, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId());
        } catch (Exception e) {
            getLogger().error("successTask error, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId(),
                    e);
        }
    }

    private void failureTask(InfoSpotGroupScaleTask task) {
        try {
            task.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
            task.setModifiedby("task");
            task.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateState(task);
            getLogger().debug("failureTask, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId());
        } catch (Exception e) {
            getLogger().error("failureTask error, clusterId:{}, groupId:{}, taskId:{}",
                    task.getClusterId(),
                    task.getGroupId(),
                    task.getTaskId(),
                    e);
        }
    }

    /**
     * 调用竞价实例扩容接口
     *
     * @param clusterGroup           竞价实例组
     * @param infoSpotGroupScaleTask 竞价实例组缩容任务
     * @param requireCount           需求数量
     */
    private void invokeSpotGroupScaleOutService(ClusterHostGroupNode clusterGroup,
                                                InfoSpotGroupScaleTask infoSpotGroupScaleTask,
                                                int requireCount) {
        getLogger().info("invokeSpotGroupScaleOutService, clusterId:{}, vmRole:{}, groupId:{},requireCount:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                clusterGroup.getGroupId(),
                requireCount);

        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.findByClusterIdAndGroupId(clusterGroup.getClusterId(),
                clusterGroup.getGroupId());
        if (confClusterHostGroup == null) {
            getLogger().error("invokeSpotGroupScaleOutService error,not found confClusterHostGroup, clusterId:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId());
            return;
        }

        //0 关闭竞价买入和缩容流程 1 关闭买入，开放缩容 2 开发买入，关闭缩容 3 开放买入和缩容
        if(confClusterHostGroup.getSpotState() != null
                && (confClusterHostGroup.getSpotState() == 0
                || confClusterHostGroup.getSpotState() == 1)) {
            this.getLogger().warn("terminate spot scale out, clusterId:{}, groupId:{}, spotState:{}",
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    confClusterHostGroup.getSpotState());
            return;
        }

        if (!Objects.equals(confClusterHostGroup.getState(), ConfClusterHostGroup.STATE_RUNNING)) {
            getLogger().error("invokeSpotGroupScaleOutService error,not found confClusterHostGroup state{} ne RUNNING, clusterId:{}, groupId:{}",
                    confClusterHostGroup.getState(),
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId());
            return;
        }
        if (confClusterHostGroup.getExpectCount() == null) {
            getLogger().error("invokeSpotGroupScaleOutService error,not found confClusterHostGroup expectCount, clusterId:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId());
            return;
        }

        long infoClusterVmCount = infoClusterVmMapper.countByClusterIdAndGroupIdAndPurchaseType(clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                InfoClusterVm.VM_RUNNING);
        int reRequireCount = Math.toIntExact((confClusterHostGroup.getExpectCount() - infoClusterVmCount));
        if (reRequireCount > PRE_TASK_SCALE_OUT_COUNT) {
            reRequireCount = PRE_TASK_SCALE_OUT_COUNT;
        }

        if (reRequireCount != requireCount) {
            getLogger().warn("invokeSpotGroupScaleOutService error,clusterId:{}, groupId:{},reRequireCount:{},requireCount:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId(),
                    reRequireCount,
                    requireCount);
            return;
        }

        long reExpectCount = infoClusterVmCount + reRequireCount;
        if (reExpectCount > confClusterHostGroup.getExpectCount()) {
            getLogger().error("invokeSpotGroupScaleOutService error gt group expectCount,clusterId:{}, groupId:{},reRequireCount:{},requireCount:{},reExpectCount:{},expectCount:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId(),
                    reRequireCount,
                    requireCount,
                    reExpectCount,
                    confClusterHostGroup.getExpectCount());
            return;
        }

        /**
         * String taskId
         * String clusterId 集群ID
         * String groupId 实例组ID
         * Integer scaleCount 扩容数量
         */
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", infoSpotGroupScaleTask.getTaskId());
        param.put("clusterId", clusterGroup.getClusterId());
        param.put("vmRole", clusterGroup.getVmRole());
        param.put("groupId", clusterGroup.getGroupId());
        param.put("expectCount", reExpectCount);
        param.put("scaleCount", reRequireCount);

        getLogger().info("begin invoke composeService.spotScaleOut success, param:{}", param);
        ResultMsg resultMsg = composeService.spotScaleOut(param);
        if (resultMsg != null && resultMsg.getResult()) {
            getLogger().info("end invoke composeService.spotScaleOut success, resultMsg:{}", resultMsg);
            infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTING.getValue());
            infoSpotGroupScaleTask.setModifiedby("task");
            infoSpotGroupScaleTask.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
            getLogger().info("update infoSpotGroupScaleTask taskId:{}, EXECUTING state:{}", infoSpotGroupScaleTask.getTaskId(), infoSpotGroupScaleTask.getState());
        } else {
            getLogger().warn("end invoke composeService.spotScaleOut failure, resultMsg:{}", resultMsg);
            infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
            infoSpotGroupScaleTask.setModifiedby("task");
            infoSpotGroupScaleTask.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
            getLogger().info("update infoSpotGroupScaleTask taskId:{}, EXECUTE_FAILURE state:{}", infoSpotGroupScaleTask.getTaskId(), infoSpotGroupScaleTask.getState());
        }
    }

    /**
     * 创建竞价实例组的扩容任务数据
     *
     * @param clusterGroup 竞价实例组
     * @param scaleCount   预期扩容数量
     * @return 竞价实例组缩容任务
     */
    private InfoSpotGroupScaleTask createSpotGroupScaleOutTask(ClusterHostGroupNode clusterGroup, int scaleCount) {
        this.getLogger().debug("begin createSpotGroupScaleOutTask clusterId:{}, groupId:{}, vmRole:{}, insCount;{}, scaleCount:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                clusterGroup.getExpectCount(),
                scaleCount);
        InfoSpotGroupScaleTask infoSpotGroupScaleTask = new InfoSpotGroupScaleTask();
        infoSpotGroupScaleTask.setTaskId(UUID.randomUUID().toString());
        infoSpotGroupScaleTask.setGroupId(clusterGroup.getGroupId());
        infoSpotGroupScaleTask.setClusterId(clusterGroup.getClusterId());
        infoSpotGroupScaleTask.setVmRole(clusterGroup.getVmRole());
        infoSpotGroupScaleTask.setScaleMethod(ScaleMethods.SCALE_OUT.getValue());
        infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.WAITING.getValue());
        infoSpotGroupScaleTask.setCreatedby("task");
        infoSpotGroupScaleTask.setCreatedTime(new Date());
        infoSpotGroupScaleTask.setScaleCount(scaleCount);
        infoSpotGroupScaleTask.setActualCount(0);
        infoSpotGroupScaleTaskMapper.insert(infoSpotGroupScaleTask);
        this.getLogger().debug("end createSpotGroupScaleOutTask clusterId:{}, groupId:{}, vmRole:{}, insCount;{}, scaleCount:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                clusterGroup.getExpectCount(),
                scaleCount);
        return infoSpotGroupScaleTask;
    }

    /**
     * 检查是否需要扩容
     * 当已有实例数量低于预期数量时就需要扩容
     *
     * @param clusterGroup 竞价实例组
     * @return true需要扩容，false不需要扩容
     */
    private NeedScaleOutGroupResult needScaleOutGroup(ClusterHostGroupNode clusterGroup) {
        long infoClusterVmCount = infoClusterVmMapper.countByClusterIdAndGroupIdAndPurchaseType(clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                InfoClusterVm.VM_RUNNING);
        if (clusterGroup.getExpectCount() > infoClusterVmCount) {
            int requireCount = Math.toIntExact((clusterGroup.getExpectCount() - infoClusterVmCount));
            if (requireCount > PRE_TASK_SCALE_OUT_COUNT) {
                requireCount = PRE_TASK_SCALE_OUT_COUNT;
            }
            getLogger().debug("needScaleOutGroup = true, clusterId:{}, groupId:{}, clusterGroup.getInsCount():{}, infoClusterVmCount:{}, requireCount:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId(),
                    clusterGroup.getExpectCount(),
                    infoClusterVmCount,
                    requireCount);
            return new NeedScaleOutGroupResult(true, requireCount);
        }

        getLogger().debug("needScaleOutGroup = false, clusterId:{}, groupId:{}, clusterGroup.getInsCount():{}, infoClusterVmCount:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getExpectCount(),
                infoClusterVmCount);
        return new NeedScaleOutGroupResult(false, 0);
    }

    private boolean hasOtherScaleLockKey(ClusterHostGroupNode clusterGroup) {
        String lockKey = "scaling_" + clusterGroup.getClusterId() + "_" + clusterGroup.getVmRole() + "_" + clusterGroup.getGroupName();
        if (distributedRedisLock.haveKey(lockKey)) {
            getLogger().debug("hasOtherScaleLockKey = true, has key:{}", lockKey);
            return true;
        }
        getLogger().debug("hasOtherScaleLockKey = false, has key:{}", lockKey);
        return false;
    }

    /**
     * 从数据库中检查实例组的状态，判断是否正在做扩缩容
     *
     * @param clusterGroup 实例组
     * @return true在扩缩容，false没有在扩缩容
     */
    private boolean isGroupScaling(ClusterHostGroupNode clusterGroup) {
        getLogger().debug("isGroupScaling clusterId:{}, vmRole:{}, groupId:{}, purchaseType:{}, state<>({},{},{})",
                clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                clusterGroup.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                ConfClusterHostGroup.STATE_RUNNING,
                ConfClusterHostGroup.STATE_RELEASED,
                ConfClusterHostGroup.STATE_DELETED);

        int scalingGroupCount = confClusterHostGroupMapper.countByClusterIdAndVmRoleAndPurchaseTypeNeState(clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                ConfClusterHostGroup.STATE_RUNNING,
                ConfClusterHostGroup.STATE_RELEASED,
                ConfClusterHostGroup.STATE_DELETED);

        if (scalingGroupCount > 0) {
            getLogger().debug("isGroupScaling = true, scalingGroupCount:{}, clusterId:{}, vmRole:{}, groupId:{}",
                    scalingGroupCount,
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return true;
        }

        getLogger().debug("isGroupScaling = false, scalingGroupCount:0, clusterId:{}, vmRole:{}, groupId:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                clusterGroup.getGroupId());
        return false;
    }
}
