package com.sunbox.sdpspot.task;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.ScaleMethods;
import com.sunbox.domain.enums.SpotGroupScaleTaskStates;
import com.sunbox.runtime.RuntimeManager;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdpspot.constant.RedisConst;
import com.sunbox.sdpspot.data.LiveFailData;
import com.sunbox.sdpspot.manager.ConfClusterHostGroupManager;
import com.sunbox.sdpspot.manager.ScheduleEventManager;
import com.sunbox.sdpspot.mapper.*;
import com.sunbox.sdpspot.model.ClusterHostGroupNode;
import com.sunbox.sdpspot.model.GroupEvictionEventCache;
import com.sunbox.sdpspot.model.VmEvictionEvent;
import com.sunbox.sdpspot.util.DistributedRedisSpot;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * 竞价实例调度管理任务
 */
@Component
public class SpotScaleInManageTask implements BaseCommonInterFace {
    private static final long VM_CREATION_CD_5MIN = 5 * 60 * 1000;
    private static final long VM_HEALTH_UPDATE_CD_60SEC = 60 * 1000;
    private static final long VM_HEALTH_TIMEOUT_20MIN = 20 * 60 * 1000;

    private static final long TASK_TIMEOUT_TICKS_3M = 3 * 60 * 1000;
    private static final long TASK_TIMEOUT_TICKS_5M = 5 * 60 * 1000;

    private static final long TASK_TIMEOUT_TICKS_30M = 30 * 60 * 1000;
    private static final int SCHEDULE_EVENT_HTTP_PORT = 7374;

    private static final int PRE_TASK_SCALE_OUT_COUNT = 50;

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

    /**
     * 等待逐出时间的最大时间（秒），超过这个时间立即逐出
     */
    @Value("${spot.eviction.wait.time:600000}")
    private Integer spotEvictionWaitTime;

    /**
     * 等待逐出时间的最大数量（台），超过这个数量立即逐出
     */
    @Value("${spot.eviction.wait.count:10}")
    private Integer spotEvictionWaitCount;

    static List<ClusterHostGroupNode> clusterGroupHostNodes;
    static Date lastRefreshClusterGroupTime;

    private static ThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(500, 1000,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(800), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Scheduled(cron = "${spot.scale.manage.task.time}")
    public void start() {
        runtimeManager.withCounter(() -> {
            if (needReloadClusterHostGroupNodes(clusterGroupHostNodes, lastRefreshClusterGroupTime)) {
                clusterGroupHostNodes = ConfClusterHostGroupManager.listRunningClusterHostGroupNodes(this.getLogger(), this.confClusterHostGroupMapper);
                lastRefreshClusterGroupTime = new Date();
            }

            if (clusterGroupHostNodes == null) {
                getLogger().debug("clusterGroupHostNodes == null");
                return;
            }

            if (clusterGroupHostNodes.isEmpty()) {
                getLogger().debug("clusterGroupHostNodes.isEmpty()");
                return;
            }

            for (ClusterHostGroupNode clusterHostGroupNode : clusterGroupHostNodes) {
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

                Future<?> submit = executor.submit(() -> {
                    String lockKey = RedisConst.keyLockScaleInGroup(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                    boolean lockSuccess = distributedRedisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
                    if (!lockSuccess) {
                        getLogger().debug("try spot lock distribute lock fail, key:{}", lockKey);
                        return;
                    }
                    try {
                        getLogger().debug("spot lock distribute lock, key:{}", lockKey);

                        String redisScaleInGroupCdKey = RedisConst.keyScaleInGroupCd(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                        if (this.distributedRedisLock.haveKey(redisScaleInGroupCdKey)) {
                            getLogger().debug("check cd, skip collectGroupEvictionEvent, request scale in cd exist, clusterId:{}, vmRole:{}, groupId:{}",
                                    clusterHostGroupNode.getClusterId(),
                                    clusterHostGroupNode.getVmRole(),
                                    clusterHostGroupNode.getGroupId());
                            return;
                        }

                        try {
                            fixClusterVmNodes(clusterHostGroupNode);
                            fixClusterUnknownVmNodes(clusterHostGroupNode);
                        } catch (Exception e) {
                            getLogger().error("scale group error, clusterId:" + clusterHostGroupNode.getClusterId() + ", groupId:" + clusterHostGroupNode.getGroupId(), e);
                            return;
                        }

                        getLogger().debug("begin scaleClusterVmRole, clusterId:{}, vmRole:{}, groupId:{}",
                                clusterHostGroupNode.getClusterId(),
                                clusterHostGroupNode.getVmRole(),
                                clusterHostGroupNode.getGroupId());

                        GroupEvictionEventCache groupEvictionEventCache = collectGroupEvictionEvent(clusterHostGroupNode);

                        //0 关闭竞价买入和缩容流程 1 关闭买入，开放缩容 2 开发买入，关闭缩容 3 开放买入和缩容
                        if(clusterHostGroupNode.getSpotState() != null
                                && (clusterHostGroupNode.getSpotState() == 0
                                || clusterHostGroupNode.getSpotState() == 2)) {
                            this.getLogger().warn("pause spot scale in, clusterId:{}, groupId:{}, spotState:{}",
                                    clusterHostGroupNode.getClusterId(),
                                    clusterHostGroupNode.getGroupId(),
                                    clusterHostGroupNode.getSpotState());
                            return;
                        }

                        if (needScaleInGroup(clusterHostGroupNode, groupEvictionEventCache)) {
                            scaleInGroup(clusterHostGroupNode, groupEvictionEventCache);
                        }
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

    private void fixClusterVmNodes(ClusterHostGroupNode clusterHostGroupNode) {
        getLogger().debug("begin fixClusterVmNodes, clusterId:{}, vmRole:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getVmRole(), clusterHostGroupNode.getGroupId());
        List<InfoClusterVm> infoClusterVms = this.infoClusterVmMapper.listByClusterIdAndGroupIdAndPurchaseType(clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                InfoClusterVm.VM_RUNNING);
        clusterHostGroupNode.setVmNodes(infoClusterVms);

        if (clusterHostGroupNode.getVmNodes() == null) {
            clusterHostGroupNode.setVmNodes(new ArrayList<>());
        }
        getLogger().debug("end fixClusterVmNodes, clusterId:{}, vmRole:{}, groupId:{}, vmNodes:{}",
                clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getVmRole(),
                clusterHostGroupNode.getGroupId(),
                clusterHostGroupNode.getVmNodes().size());
    }

    private void fixClusterUnknownVmNodes(ClusterHostGroupNode clusterHostGroupNode) {
        getLogger().debug("begin fixClusterUnknownVmNodes, clusterId:{}, vmRole:{}, groupId:{}", clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getVmRole(), clusterHostGroupNode.getGroupId());
        List<InfoClusterVm> unknownInfoClusterVms = this.infoClusterVmMapper.listByClusterIdAndGroupIdAndPurchaseType(clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                InfoClusterVm.VM_UNKNOWN);
        clusterHostGroupNode.setUnknownVmNodes(unknownInfoClusterVms);
        if (clusterHostGroupNode.getUnknownVmNodes() == null) {
            clusterHostGroupNode.setUnknownVmNodes(new ArrayList<>());
        }
        getLogger().debug("end fixClusterUnknownVmNodes, clusterId:{}, vmRole:{}, groupId:{}, unknownVmNodes:{}",
                clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getVmRole(),
                clusterHostGroupNode.getGroupId(),
                clusterHostGroupNode.getUnknownVmNodes().size());
    }

    /**
     * 是否需要重新从数据库读取实例组数据
     *
     * @return true需要读取，false不需要读取
     */
    private boolean needReloadClusterHostGroupNodes(List<ClusterHostGroupNode> clusterGroupNodes, Date lastRefreshTime) {
        if (clusterGroupNodes == null) {
            getLogger().debug("needReloadClusterGroupNodes true, clusterVmNodes == null");
            return true;
        }

        long countFromDb = confClusterHostGroupMapper.countByPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT, ConfClusterHostGroup.STATE_RUNNING);
        if (countFromDb != clusterGroupNodes.size()) {
            getLogger().debug("needReloadClusterGroupNodes true, countFromDb:{} != clusterGroupNodes.size:{}", countFromDb, clusterGroupNodes.size());
            return true;
        } else if (lastRefreshClusterGroupTime == null) {
            getLogger().debug("needReloadClusterGroupNodes true, lastRefreshClusterGroupTime == null");
            return true;
        } else if (System.currentTimeMillis() - lastRefreshClusterGroupTime.getTime() > 60000) {
            getLogger().debug("needReloadClusterGroupNodes true, System.currentTimeMillis() - lastRefreshClusterGroupTime.getTime():{}", System.currentTimeMillis() - lastRefreshClusterGroupTime.getTime());
            return true;
        }
        getLogger().debug("needReloadClusterGroupNodes false");
        return false;
    }

    /**
     * 是否存在其它在进行中(非竞价缩容）的任务
     *
     * @param clusterGroup 竞价实例组
     * @return true存在，false不存在
     */
    private boolean hasOtherScaleInTaskNotSpot(ClusterHostGroupNode clusterGroup) {
        int otherRunningTaskCount = confScalingTaskMapper.countByClusterIdAndClusterNameAndStateAndScaleInNotSpot(clusterGroup.getClusterId(),
                clusterGroup.getGroupName(),
                ConfScalingTask.SCALINGTASK_Create,
                ConfScalingTask.SCALINGTASK_Running);
        getLogger().debug("hasSpotScaleTask hasOtherScaleInTaskNotSpot(from db):{}", otherRunningTaskCount);
        if (otherRunningTaskCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 是否存在进行中的任务
     *
     * @param clusterGroup 竞价实例组
     * @return true存在，false不存在
     */
    private boolean hasSpotScaleTask(ClusterHostGroupNode clusterGroup) {
        InfoSpotGroupScaleTask runningTask = infoSpotGroupScaleTaskMapper.findLatestByClusterIdAndNotState(clusterGroup.getClusterId(), clusterGroup.getGroupId(), clusterGroup.getVmRole(),
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
                if (durationTime > TASK_TIMEOUT_TICKS_5M) {
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
     * 对竞价实例组进行缩容操作
     *
     * @param clusterGroup                竞价实例组
     * @param clusterGroupEvictEventCache 实例组的实例逐出记录
     */
    private void scaleInGroup(ClusterHostGroupNode clusterGroup, GroupEvictionEventCache clusterGroupEvictEventCache) {
        if (hasOtherScaleLockKey(clusterGroup)) {
            getLogger().warn("skip scaleInGroup, hasOtherScaleLockKey = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (hasOtherScaleInTaskNotSpot(clusterGroup)) {
            getLogger().warn("skip scaleInGroup, hasOtherScaleInTaskNotSpot(from db) = true, clusterId:{}, vmRole:{}, groupId:{}, groupName:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId(),
                    clusterGroup.getGroupName());
            return;
        }

        if (hasSpotScaleTask(clusterGroup)) {
            getLogger().warn("skip scaleOutGroup, hasSpotScaleTask(from db) = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        if (isGroupScaling(clusterGroup)) {
            getLogger().warn("skip scaleInGroup, isGroupScaling = true, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId());
            return;
        }

        InfoSpotGroupScaleTask spotGroupScaleInTask = createSpotGroupScaleInTask(clusterGroup, clusterGroupEvictEventCache);

        deleteClusterGroupEvictEvent(clusterGroup, clusterGroupEvictEventCache);

        invokeSpotGroupScaleInService(clusterGroup, clusterGroupEvictEventCache, spotGroupScaleInTask);

        createSpotGroupScaleInCd(clusterGroup);
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

    private void createSpotGroupScaleInCd(ClusterHostGroupNode clusterHostGroupNode) {
        String redisScaleInGroupCdKey = RedisConst.keyScaleInGroupCd(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
        try {
            this.distributedRedisLock.save(redisScaleInGroupCdKey, String.valueOf(System.currentTimeMillis()), 1);
            getLogger().debug("createSpotGroupScaleInCd redis key:{}", redisScaleInGroupCdKey);
        } catch (Exception e) {
            getLogger().debug("createSpotGroupScaleInCd redis error key:{}", redisScaleInGroupCdKey, e);
        }
    }

    /**
     * 删除redis中对应实例的
     *
     * @param clusterGroup            竞价实例组
     * @param groupEvictionEventCache 实例组的实例逐出记录
     */
    private void deleteClusterGroupEvictEvent(ClusterHostGroupNode clusterGroup, GroupEvictionEventCache groupEvictionEventCache) {
        for (InfoSpotGroupScaleTaskItem vmEvictionEvent : groupEvictionEventCache.getVmEvictionEvents()) {
            String redisEventKey = RedisConst.keyVmEvictionEvent(clusterGroup.getClusterId(), vmEvictionEvent.getHostname());
            try {
                this.spotRedissonClient.delete(redisEventKey);
                getLogger().debug("deleteClusterGroupEvictEvent key:{}, hostName:{}", redisEventKey, vmEvictionEvent.getHostname());
            } catch (Exception e) {
                getLogger().error("mapRemove error, key:" + redisEventKey + ", hostName:" + vmEvictionEvent.getHostname(), e);
            }
        }
    }

    /**
     * 调用竞价实例缩容接口
     *
     * @param clusterGroup            竞价实例组
     * @param groupEvictionEventCache 实例组的实例逐出记录
     * @param infoSpotGroupScaleTask  竞价实例组缩容任务
     */
    private void invokeSpotGroupScaleInService(ClusterHostGroupNode clusterGroup, GroupEvictionEventCache groupEvictionEventCache, InfoSpotGroupScaleTask infoSpotGroupScaleTask) {
        getLogger().info("invokeSpotGroupScaleInService, clusterId:{}, vmRole:{}, groupId:{}, groupEvictionEventCache:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getVmRole(),
                clusterGroup.getGroupId(),
                groupEvictionEventCache);

        int nonDeletedCount = 0;
        for (InfoSpotGroupScaleTaskItem vmEvictionEvent : groupEvictionEventCache.getVmEvictionEvents()) {
            nonDeletedCount++;
        }

        if (nonDeletedCount == 0) {
            getLogger().warn("invokeSpotGroupScaleInService return nonDeleted = 0, clusterId:{}, vmRole:{}, groupId:{}, groupEvictionEventCache:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getVmRole(),
                    clusterGroup.getGroupId(),
                    groupEvictionEventCache);
            infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
            infoSpotGroupScaleTask.setModifiedTime(new Date());
            infoSpotGroupScaleTask.setModifiedby("spot-task");
            this.infoSpotGroupScaleTaskMapper.updateState(infoSpotGroupScaleTask);
            return;
        }

        /**
         *      * String taskId
         *      * Stirng clusterId 集群ID
         *      * String groupId 实例组ID
         *      * List<String> vms 缩容机器vmname列表
         */
        List<String> vms = new ArrayList<>();
        for (InfoSpotGroupScaleTaskItem vmEvictionEvent : groupEvictionEventCache.getVmEvictionEvents()) {
            vms.add(vmEvictionEvent.getVmName());
        }

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
                || confClusterHostGroup.getSpotState() == 2)) {
            this.getLogger().warn("terminate spot scale in, clusterId:{}, groupId:{}, spotState:{}",
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    confClusterHostGroup.getSpotState());
            return;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("taskId", infoSpotGroupScaleTask.getTaskId());
        param.put("clusterId", clusterGroup.getClusterId());
        param.put("vmRole", clusterGroup.getVmRole());
        param.put("groupId", clusterGroup.getGroupId());
        param.put("expectCount", confClusterHostGroup.getExpectCount());
        param.put("vms", vms);

        getLogger().info("begin invoke composeService.spotScaleIn success, param:{}", param);
        ResultMsg resultMsg = composeService.spotScaleIn(param);
        if (resultMsg != null && resultMsg.getResult()) {
            getLogger().info("end invoke composeService.spotScaleIn success, resultMsg:{}", resultMsg);
            infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTING.getValue());
            infoSpotGroupScaleTask.setModifiedby("task");
            infoSpotGroupScaleTask.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
            getLogger().info("update infoSpotGroupScaleTask taskId:{}, EXECUTING state:{}", infoSpotGroupScaleTask.getTaskId(), infoSpotGroupScaleTask.getState());
        } else {
            getLogger().warn("end invoke composeService.spotScaleIn failure, resultMsg:{}", resultMsg);
            infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.EXECUTE_FAILURE.getValue());
            infoSpotGroupScaleTask.setModifiedby("task");
            infoSpotGroupScaleTask.setModifiedTime(new Date());
            infoSpotGroupScaleTaskMapper.updateByPrimaryKey(infoSpotGroupScaleTask);
            getLogger().info("update infoSpotGroupScaleTask taskId:{}, EXECUTE_FAILURE state:{}", infoSpotGroupScaleTask.getTaskId(), infoSpotGroupScaleTask.getState());
        }
    }

    /**
     * 创建竞价实例组的缩容任务数据
     *
     * @param clusterGroup                竞价实例组
     * @param clusterGroupEvictEventCache 实例组的实例逐出记录
     * @return 竞价实例组缩容任务
     */
    private InfoSpotGroupScaleTask createSpotGroupScaleInTask(ClusterHostGroupNode clusterGroup, GroupEvictionEventCache clusterGroupEvictEventCache) {
        this.getLogger().debug("begin createSpotGroupScaleInTask clusterId:{}, groupId:{}, vmRole:{}, insCount;{}, vmEvictionEvents size:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                clusterGroup.getExpectCount(),
                clusterGroupEvictEventCache.getVmEvictionEvents().size());

        InfoSpotGroupScaleTask infoSpotGroupScaleTask = new InfoSpotGroupScaleTask();
        infoSpotGroupScaleTask.setTaskId(UUID.randomUUID().toString());
        infoSpotGroupScaleTask.setGroupId(clusterGroup.getGroupId());
        infoSpotGroupScaleTask.setClusterId(clusterGroup.getClusterId());
        infoSpotGroupScaleTask.setVmRole(clusterGroup.getVmRole());
        infoSpotGroupScaleTask.setScaleMethod(ScaleMethods.SCALE_IN.getValue());
        infoSpotGroupScaleTask.setState(SpotGroupScaleTaskStates.WAITING.getValue());
        infoSpotGroupScaleTask.setCreatedby("task");
        infoSpotGroupScaleTask.setCreatedTime(new Date());
        infoSpotGroupScaleTaskMapper.insert(infoSpotGroupScaleTask);

        for (InfoSpotGroupScaleTaskItem vmEvictionEvent : clusterGroupEvictEventCache.getVmEvictionEvents()) {
            if (infoSpotGroupScaleTaskItemMapper.countByVmNameAndState(clusterGroup.getClusterId(),
                    clusterGroup.getGroupId(),
                    vmEvictionEvent.getVmName(),
                    SpotGroupScaleTaskStates.EXECUTE_SUCCESS.getValue()) > 0) {
                getLogger().debug("delete InfoSpotGroupScaleTaskItem:{}", vmEvictionEvent);
                infoSpotGroupScaleTaskItemMapper.deleteByPrimaryKey(vmEvictionEvent.getItemId());
                continue;
            }
            vmEvictionEvent.setTaskId(infoSpotGroupScaleTask.getTaskId());
            infoSpotGroupScaleTaskItemMapper.updateByPrimaryKey(vmEvictionEvent);
            this.getLogger().debug("insert task item clusterId:{}, groupId:{}, vmRole:{}, taskId;{}, itemId:{}",
                    clusterGroup.getClusterId(),
                    clusterGroup.getGroupId(),
                    clusterGroup.getVmRole(),
                    vmEvictionEvent.getTaskId(),
                    vmEvictionEvent.getItemId());
        }
        this.getLogger().debug("end createSpotGroupScaleInTask clusterId:{}, groupId:{}, vmRole:{}, insCount;{}, vmEvictionEvents size:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                clusterGroup.getExpectCount(),
                clusterGroupEvictEventCache.getVmEvictionEvents().size());
        return infoSpotGroupScaleTask;
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

    /**
     * 从redis中收集实例逐出事件
     *
     * @param clusterHostGroupNode 实例组
     * @return 实例组的逐出事件缓存对象
     */
    private GroupEvictionEventCache collectGroupEvictionEvent(ClusterHostGroupNode clusterHostGroupNode) {
        String redisScaleInGroupCdKey = RedisConst.keyScaleInGroupCd(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
        if (this.distributedRedisLock.haveKey(redisScaleInGroupCdKey)) {
            getLogger().debug("skip collectGroupEvictionEvent, request scale in cd exist, clusterId:{}, vmRole:{}, groupId:{}",
                    clusterHostGroupNode.getClusterId(),
                    clusterHostGroupNode.getVmRole(),
                    clusterHostGroupNode.getGroupId());
            return null;
        }

        for (InfoClusterVm infoClusterVm : clusterHostGroupNode.getVmNodes()) {
            String redisEventKey = RedisConst.keyVmEvictionEvent(clusterHostGroupNode.getClusterId(), infoClusterVm.getHostName());
            long lifeTime = System.currentTimeMillis() - infoClusterVm.getCreateEndtime().getTime();
            if (lifeTime < VM_CREATION_CD_5MIN) {
                getLogger().debug("skip find redisEventKey:{}, lifeTime:{} less than {}ms", redisEventKey, lifeTime, VM_CREATION_CD_5MIN);
                continue;
            }

            if (infoClusterVm.getHealthCheckTime() != null) {
                lifeTime = System.currentTimeMillis() - infoClusterVm.getHealthCheckTime().getTime();
            }

            try {
                String redisEventValue = this.spotRedissonClient.tryGetValueAsString(redisEventKey, null);
                getLogger().debug("get redis value by key:{},value:{}", redisEventKey, redisEventValue);
                if (StringUtils.isEmpty(redisEventValue)) {
                    if (lifeTime < TASK_TIMEOUT_TICKS_3M) {
                        getLogger().debug("skip not found redisEventKey:{}, lifeTime:{} less than {}ms",
                                redisEventKey,
                                lifeTime,
                                TASK_TIMEOUT_TICKS_3M);
                        continue;
                    }

                    getLogger().debug("not found redisEventKey:{}", redisEventKey);
                    LiveFailData liveFailData = scheduleEventManager.getLiveFailData(infoClusterVm.getClusterId(), infoClusterVm.getVmName());
                    VmEvictionEvent evictionEventFromHttp = scheduleEventManager.findEvictionEventFromHttp(infoClusterVm,
                            SCHEDULE_EVENT_HTTP_PORT,
                            liveFailData);
                    if (evictionEventFromHttp != null) {
                        if (evictionEventFromHttp.getEvictTime() == null) {
                            // 生成redis
                            redisEventValue = JSONObject.toJSONString(evictionEventFromHttp);
                            this.spotRedissonClient.trySaveString(redisEventKey, redisEventValue, VmEvictionEvent.EXPIRES_5MIN);
                            getLogger().debug("skip http get events from http pass, hostName:{}, internalip:{}", infoClusterVm.getHostName(), infoClusterVm.getInternalip());
                            scheduleEventManager.deleteLiveFailData(liveFailData);
                        } else {
                            // 需要逐出
                            markEviction(clusterHostGroupNode,
                                    infoClusterVm,
                                    evictionEventFromHttp,
                                    "探活到逐出事件," + infoClusterVm.getInternalip() + ":" + SCHEDULE_EVENT_HTTP_PORT);
                            getLogger().info("found eviction event from http response:{}, hostName:{}, internalip:{}",
                                    evictionEventFromHttp,
                                    infoClusterVm.getHostName(),
                                    infoClusterVm.getInternalip());
                            scheduleEventManager.deleteLiveFailData(liveFailData);
                        }
                    } else {
                        if (liveFailData.getFailedCount() >= 3) {
                            //需要逐出
                            VmEvictionEvent vmEvictionEvent = VmEvictionEvent.withShutdown(infoClusterVm.getVmName(), infoClusterVm.getHostName());
                            markEviction(clusterHostGroupNode,
                                    infoClusterVm,
                                    vmEvictionEvent,
                                    "连续三次探活失败," + infoClusterVm.getInternalip() + ":" + SCHEDULE_EVENT_HTTP_PORT);
                            getLogger().info("found eviction event from http failure:{}, hostName:{}, internalip:{},failCount:{}",
                                    vmEvictionEvent,
                                    infoClusterVm.getHostName(),
                                    infoClusterVm.getInternalip(),
                                    liveFailData.getFailedCount());
                            scheduleEventManager.deleteLiveFailData(liveFailData);
                        } else {
                            getLogger().debug("skip found eviction event hostName:{}, internalip:{},failCount:{}",
                                    infoClusterVm.getHostName(),
                                    infoClusterVm.getInternalip(),
                                    liveFailData.getFailedCount());
                            if (!scheduleEventManager.setLiveFailData(liveFailData)) {
                                //需要逐出
                                VmEvictionEvent vmEvictionEvent = VmEvictionEvent.withShutdown(infoClusterVm.getVmName(), infoClusterVm.getHostName());
                                markEviction(clusterHostGroupNode,
                                        infoClusterVm,
                                        vmEvictionEvent,
                                        "连续" + liveFailData.getFailedCount() + "次探活失败,REDIS异常," + infoClusterVm.getInternalip() + ":" + SCHEDULE_EVENT_HTTP_PORT);
                                getLogger().info("found eviction event from redis exception:{}, hostName:{}, internalip:{},failCount:{}",
                                        vmEvictionEvent,
                                        infoClusterVm.getHostName(),
                                        infoClusterVm.getInternalip(),
                                        liveFailData.getFailedCount());
                            }
                        }
                    }
                } else {
                    try {
                        if (infoClusterVm.getHealthCheckTime() == null
                                || new Date().getTime() - infoClusterVm.getHealthCheckTime().getTime() > VM_HEALTH_UPDATE_CD_60SEC) {
                            infoClusterVm.setHealthCheckTime(new Date());
                            infoClusterVmMapper.updateHealthCheckTime(infoClusterVm);
                            getLogger().debug("update health check time, clusterId:{}, groupId:{}, vmName:{}, healthCheckTime:{}",
                                    infoClusterVm.getClusterId(),
                                    infoClusterVm.getGroupId(),
                                    infoClusterVm.getVmName(),
                                    infoClusterVm.getHealthCheckTime());
                        } else {
                            getLogger().debug("skip update health check time, clusterId:{}, groupId:{}, vmName:{}, healthCheckTime:{}",
                                    infoClusterVm.getClusterId(),
                                    infoClusterVm.getGroupId(),
                                    infoClusterVm.getVmName(),
                                    infoClusterVm.getHealthCheckTime());
                        }
                    } catch (Exception e) {
                        getLogger().error("update health check time error, clusterId:{}, groupId:{}, vmName:{}, healthCheckTime:{}",
                                infoClusterVm.getClusterId(),
                                infoClusterVm.getGroupId(),
                                infoClusterVm.getVmName(),
                                infoClusterVm.getHealthCheckTime(), e);
                    }

                    VmEvictionEvent vmEvictionEvent = JSONUtil.toBean(redisEventValue, VmEvictionEvent.class);
                    if (vmEvictionEvent.getEvictTime() == null) {
                        getLogger().debug("skip found empty redisEventKey:{},vmEvictionEvent:{}", redisEventKey, vmEvictionEvent);
                    } else {
                        //需要逐出
                        getLogger().info("found eviction event from redis:{},redisEventKey:{}", vmEvictionEvent, redisEventKey);
                        vmEvictionEvent.setVmName(infoClusterVm.getVmName());
                        vmEvictionEvent.setHostname(infoClusterVm.getHostName());
                        markEviction(clusterHostGroupNode,
                                infoClusterVm,
                                vmEvictionEvent,
                                "获取到逐出事件," + infoClusterVm.getInternalip() + ":" + SCHEDULE_EVENT_HTTP_PORT);
                    }
                }
            } catch (Exception e) {
                getLogger().error("load redis error, key:{}", redisEventKey, e);
            }
        }

        Iterator<InfoClusterVm> iterator = clusterHostGroupNode.getUnknownVmNodes().iterator();
        while (iterator.hasNext()) {
            InfoClusterVm unknownVmNode = iterator.next();
            InfoClusterVm infoClusterVmFromDb = infoClusterVmMapper.findOne(unknownVmNode.getClusterId(),
                    unknownVmNode.getGroupId(),
                    unknownVmNode.getVmName());
            if (Objects.equals(infoClusterVmFromDb.getState(), InfoClusterVm.VM_DELETED)) {
                iterator.remove();
            } else if (StringUtils.isNotEmpty(infoClusterVmFromDb.getScaleinTaskId())) {
                ConfScalingTask confScalingTask = confScalingTaskMapper.selectByPrimaryKey(infoClusterVmFromDb.getClusterId(), infoClusterVmFromDb.getScaleinTaskId());
                if (confScalingTask != null
                        && (Objects.equals(confScalingTask.getState(), ConfScalingTask.SCALINGTASK_Running)
                        || Objects.equals(confScalingTask.getState(), ConfScalingTask.SCALINGTASK_Complete)
                        || Objects.equals(confScalingTask.getState(), ConfScalingTask.SCALINGTASK_Create))) {
                    iterator.remove();
                } else {
                    VmEvictionEvent vmEvictionEvent = VmEvictionEvent.withShutdown(unknownVmNode.getVmName(), unknownVmNode.getHostName());
                    markEviction(clusterHostGroupNode,
                            unknownVmNode,
                            vmEvictionEvent,
                            "状态已为unknown,重新启动缩容,");
                }
            } else {
                VmEvictionEvent vmEvictionEvent = VmEvictionEvent.withShutdown(unknownVmNode.getVmName(), unknownVmNode.getHostName());
                markEviction(clusterHostGroupNode,
                        unknownVmNode,
                        vmEvictionEvent,
                        "状态已为unknown,");
            }
        }

        long infoClusterVmCount = infoClusterVmMapper.countByClusterIdAndGroupIdAndPurchaseTypeAndState(clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getGroupId(),
                ConfClusterVm.PURCHASETYPE_SPOT,
                InfoClusterVm.VM_RUNNING);
        ConfClusterHostGroup byClusterIdAndGroupId = confClusterHostGroupMapper.findByClusterIdAndGroupId(clusterHostGroupNode.getClusterId(),
                clusterHostGroupNode.getGroupId());

        String scaleOutCdValue = this.distributedRedisLock.tryGetValueAsString(getLogger(),
                "SPOT_SCALE_OUT_CD:" + clusterHostGroupNode.getClusterId() + ":" + clusterHostGroupNode.getGroupId(),
                "2");
        if (StringUtils.equals(scaleOutCdValue, "1")
                || infoClusterVmCount >= byClusterIdAndGroupId.getExpectCount()) {
            List<InfoSpotGroupScaleTaskItem> infoSpotGroupScaleTaskItems = infoSpotGroupScaleTaskItemMapper.listByClusterIdAndGroupIdAndTaskId(clusterHostGroupNode.getClusterId(),
                    clusterHostGroupNode.getGroupId(),
                    "SPOT-WAIT");

            boolean returnEvictionEventCache = false;
            if (!infoSpotGroupScaleTaskItems.isEmpty()) {
                if (infoSpotGroupScaleTaskItems.size() > spotEvictionWaitCount) {
                    returnEvictionEventCache = true;
                } else {
                    if (System.currentTimeMillis() - infoSpotGroupScaleTaskItems.get(0).getCreatedTime().getTime() > spotEvictionWaitTime) {
                        returnEvictionEventCache = true;
                    }
                }
            }

            if (returnEvictionEventCache) {
                GroupEvictionEventCache groupEvictionEventCache = new GroupEvictionEventCache(clusterHostGroupNode.getClusterId(),
                        clusterHostGroupNode.getGroupId(),
                        infoSpotGroupScaleTaskItems);
                getLogger().info("end collectGroupEvictionEvent pass, SPOT_SCALE_OUT_CD is false or vmCount:{},expect:{},event cache:{}",
                        infoClusterVmCount,
                        byClusterIdAndGroupId.getExpectCount(),
                        groupEvictionEventCache);
                return groupEvictionEventCache;
            } else {
                GroupEvictionEventCache groupEvictionEventCache = new GroupEvictionEventCache(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
                getLogger().info("end collectGroupEvictionEvent fail, SPOT_SCALE_OUT_CD is false or vmCount:{},expect:{},event cache:{}",
                        infoClusterVmCount,
                        byClusterIdAndGroupId.getExpectCount(),
                        groupEvictionEventCache);
                return groupEvictionEventCache;
            }
        } else {
            GroupEvictionEventCache groupEvictionEventCache = new GroupEvictionEventCache(clusterHostGroupNode.getClusterId(), clusterHostGroupNode.getGroupId());
            getLogger().info("end collectGroupEvictionEvent fail, SPOT_SCALE_OUT_CD is true or vmCount:{},expect:{}, event cache:{}",
                    infoClusterVmCount,
                    byClusterIdAndGroupId.getExpectCount(),
                    groupEvictionEventCache);
            return groupEvictionEventCache;
        }
    }

    private void markEviction(ClusterHostGroupNode clusterGroup,
                              InfoClusterVm infoClusterVm,
                              VmEvictionEvent vmEvictionEvent,
                              String reason) {
        InfoSpotGroupScaleTaskItem infoSpotGroupScaleTaskItem = infoSpotGroupScaleTaskItemMapper.selectTop1ByClusterIdAndGroupIdAndVmNameAndTaskId(clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                vmEvictionEvent.getVmName(),
                "SPOT-WAIT");

        if (infoSpotGroupScaleTaskItem != null) {
            return;
        }

        // 原来探活失败, 需要逐出时, 先将VM状态改为未知, 实例组的insCount减1, 这样后面会将此机器扩容上去.
        // 现在先不做此操作,扩容操作由AzureFleet的补全通知触发
//        ResultMsg resultMsg = composeService.processEviction(clusterGroup.getClusterId(),
//                infoClusterVm.getHostName(),
//                reason);
        getLogger().info("composeService.processEviction error,clusterId:{},hostname:{},reason:{},result:{}",
                clusterGroup.getClusterId(),
                infoClusterVm.getHostName(),
                reason,
                "探活失败, 但是不通知Compose处理驱逐情况, 补全被驱逐的VM交给AzureFleet的主动通知");
//                resultMsg);

        getLogger().info("markEviction,clusterGroup:{},infoClusterVm:{},vmEvictionEvent:{},reason:{}",
                clusterGroup,
                infoClusterVm,
                vmEvictionEvent,
                reason);
        infoSpotGroupScaleTaskItem = new InfoSpotGroupScaleTaskItem();
        infoSpotGroupScaleTaskItem.setItemId(UUID.randomUUID().toString());
        infoSpotGroupScaleTaskItem.setTaskId("SPOT-WAIT");
        infoSpotGroupScaleTaskItem.setClusterId(clusterGroup.getClusterId());
        infoSpotGroupScaleTaskItem.setGroupId(clusterGroup.getGroupId());
        infoSpotGroupScaleTaskItem.setVmRole(clusterGroup.getVmRole());
        infoSpotGroupScaleTaskItem.setVmName(vmEvictionEvent.getVmName());
        infoSpotGroupScaleTaskItem.setHostname(infoClusterVm.getHostName());
        infoSpotGroupScaleTaskItem.setExpectedTime(vmEvictionEvent.getEvictTime());
        infoSpotGroupScaleTaskItem.setScaleMethod(ScaleMethods.SCALE_IN.getValue());
        infoSpotGroupScaleTaskItem.setState(SpotGroupScaleTaskStates.WAITING.getValue());
        infoSpotGroupScaleTaskItem.setCreatedby("task");
        infoSpotGroupScaleTaskItem.setCreatedTime(new Date());
        infoSpotGroupScaleTaskItem.setReason(reason);
        infoSpotGroupScaleTaskItemMapper.insert(infoSpotGroupScaleTaskItem);
        this.getLogger().info("insert task item clusterId:{}, groupId:{}, vmRole:{}, item:{}",
                clusterGroup.getClusterId(),
                clusterGroup.getGroupId(),
                clusterGroup.getVmRole(),
                infoSpotGroupScaleTaskItem);
    }

    private boolean httpHeadTest(String hostName, int port) {
        String url = "http://" + hostName + ":" + port;
        Request request = null;
        Response response = null;
        OkHttpClient okHttpClient = null;
        try {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .build();
            Request.Builder builder = new Request.Builder();
            request = new Request.Builder()
                    .url(url)
                    .head()
                    .build();
            Call call = okHttpClient.newCall(request);
            response = call.execute();
            if (response.isSuccessful()) {
                getLogger().debug("http head test pass, url:{}, code:{}", url, response.code());
                return true;
            }
            if (response.code() == HttpStatus.NOT_FOUND.value()) {
                getLogger().debug("http head test pass with NOT_FOUND, url:{}, code:{}", url, response.code());
                return true;
            }
            getLogger().warn("http head test not pass, url:{}, code:{}", url, response.code());
        } catch (Exception e) {
            getLogger().error("http head test error, url:{}, ex:{}", url, e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {

                }
            }
        }
        return false;
    }

    /**
     * 检查实例组是否需要缩容
     *
     * @param clusterHostGroupNode    实例组
     * @param groupEvictionEventCache 实例组逐出事件缓存
     * @return true需要立即缩容，false暂不需要缩容
     */
    private boolean needScaleInGroup(ClusterHostGroupNode clusterHostGroupNode, GroupEvictionEventCache groupEvictionEventCache) {
        if (groupEvictionEventCache == null) {
            getLogger().debug("needScaleInGroup = false, groupEvictionEventCache == null");
            return false;
        }

        if (groupEvictionEventCache.getVmEvictionEvents() == null) {
            getLogger().debug("needScaleInGroup = false, groupEvictionEventCache.getVmEvictionEvents() is null == true");
            return false;
        }

        if (groupEvictionEventCache.getVmEvictionEvents().isEmpty()) {
            getLogger().debug("needScaleInGroup = false, groupEvictionEventCache.getVmEvictionEvents().isEmpty() == true");
            return false;
        }
        return true;
    }
}
