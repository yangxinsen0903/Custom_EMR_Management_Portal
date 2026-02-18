package com.sunbox.sdptask.task;

import cn.hutool.core.util.StrUtil;
import com.sunbox.dao.mapper.ConfClusterHostGroupNeoMapper;
import com.sunbox.dao.mapper.ConfScalingTaskNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpservice.data.compose_cloud.ScaleInForDeleteTaskVmReq;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdptask.consts.ComposeConstant;
import com.sunbox.sdptask.manager.ClusterSplitCreationTaskManager;
import com.sunbox.sdptask.manager.ComposeConfClusterManager;
import com.sunbox.sdptask.mapper.*;
import com.sunbox.sdptask.service.ScalingService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class PlanDaemonTask implements BaseCommonInterFace {
    @Autowired
    private ConfScalingTaskNeoMapper taskMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ComposeService planExecService;

    @Autowired
    private ConfClusterHostGroupNeoMapper confClusterHostGroupNeoMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private ConfClusterSplitTaskMapper confClusterSplitTaskMapper;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private ComposeConfClusterManager composeConfClusterManager;

    @Autowired
    ClusterSplitCreationTaskManager clusterSplitCreationTaskManager;

    private static ThreadPoolExecutor executor;

    @Autowired
    private ScalingService scalingService;
    private static Map<String, Future> submitMap = new ConcurrentHashMap<>();

    private static List<ConfClusterHostGroup> clusterHostGroupList;
    private static Long lastRefreshClusterHostGroupTicks;

    private static List<ConfClusterHostGroup> splitClusterHostGroupList;
    private static Long lastRefreshSplitClusterHostGroupTicks;

    private static final int DURATION_10SEC = 10 * 1000;
    private static final int DURATION_30SEC = 30 * 1000;
    private static final int DURATION_40SEC = 40 * 1000;
    private static final int DURATION_50SEC = 40 * 1000;
    private static final int DURATION_1MIN = 60 * 1000;
    /** 2分钟 */
    private static final int DURATION_2MIN = 2 * 60 * 1000;
    private static final int DURATION_5MIN = 5 * 60 * 1000;
    private static final int DURATION_10MIN = 10 * 60 * 1000;

    private static List<String> cacheKeys = new ArrayList<>();
    private static final int MIN_COUNTER = 0;
    private static final int MAX_COUNTER = 5;
    private static int counter = MIN_COUNTER;

    @PostConstruct
    public void init() {
        // 初始化线程池, 线程池最大为100个认为并行执行.
        executor = new ThreadPoolExecutor(5, 100,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Scheduled(fixedDelay = 30000L)
    public void start() {
        getLogger().info("开始调度扩缩容任务的执行...");

        // 从数据库加载所有集群的实例组. 包括全量创建和增量创建. 定时任务按照实例组来处理扩缩容.
        refreshAllHostgroupFromDB();

        for (ConfClusterHostGroup splitClusterHostGroup : splitClusterHostGroupList) {
            // 开始遍历处理每个实例组的扩缩容任务.
            getLogger().info("splitClusterHostGroupList 开始 clusterId:{},groupId:{}", splitClusterHostGroup.getClusterId(), splitClusterHostGroup.getGroupId());
            if (splitClusterHostGroup.getVmRole().equals("master")
                 || splitClusterHostGroup.getVmRole().equals("ambari")) {
                continue;
            }

            int activeCount = executor.getActiveCount();
            if (executor.getActiveCount() > 30) {
                getLogger().warn("lock executor activeCount:{} too large", activeCount);
                continue;
            }

            //region start execute thread
            String clusterId = splitClusterHostGroup.getClusterId();
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                getLogger().info("not found conf cluster from db,找不到对应的集群配置信息,clusterId:{}", clusterId);
                continue;
            }
            String lockKey;
            if (confCluster.getIsParallelScale()==0){
                // 不支持集群并行扩缩容, 给集群加锁, 这样集群内只能有一个扩缩容任务运行
                lockKey = "scale_group_schedule_lock:cluster:" + splitClusterHostGroup.getClusterId();
            } else {
                // 支持集群并行扩缩容, 给实例组加锁, 这样一个集群可以同时运行多个实例组扩缩容任务
                lockKey = "scale_group_schedule_lock:group:" + splitClusterHostGroup.getGroupId();
            }
            getLogger().info("split lockKey :{}",lockKey);

            // 这个Key缓存在本地的目的是防止PlanDaemonTask在30秒调度一次时, 上一次调度还没处理完的实例组, 本次又重复处理.
            // 本地线程池做了限制, 一个Pod最多开30个线程, 重复处理会占用线程池里面的线程数.
            if (inCacheKey(lockKey)) {
                getLogger().info("上一轮调度还未处理完此实例组，本轮跳过：key:{}, groupName:{}, clusterId={}", lockKey, splitClusterHostGroup.getGroupName(),
                        splitClusterHostGroup.getClusterId());
                continue;
            }

            boolean needStartThread = false;
            if (composeConfClusterManager.checkHostgroupInQueue(splitClusterHostGroup.getClusterId(), splitClusterHostGroup.getGroupName())) {
                needStartThread = true;
            } else if (counter == MAX_COUNTER) {
                needStartThread = true;
            }

            if (!needStartThread) {
                getLogger().info("skip because needStartThread is false,clusterId:{},groupName:{}", splitClusterHostGroup.getClusterId(), splitClusterHostGroup.getGroupName());
                continue;
            }
            Future<?> submit = executor.submit(() -> {
                boolean lockSuccess = this.redisLock.tryLock(lockKey);
                if (!lockSuccess) {
                    getLogger().info("调度实例组扩缩容时,获取锁失败[lockKey:{}, clusterId={}, groupName={}]",
                            lockKey, splitClusterHostGroup.getClusterId(), splitClusterHostGroup.getGroupName());
                    return;
                }
                try {
                    getLogger().info("将锁同时保存进本地缓存,key:{}", lockKey);
                    addToCacheKey(lockKey);
                    getLogger().info("将锁同时保存进本地缓存完成. {}", splitClusterHostGroup);

                    // 集群增量创建时,获取一个增量创建任务, 获取RUNNING状态的原因为:运行中的任务不能重复处理
                    ConfClusterSplitTask confClusterSplitTask = confClusterSplitTaskMapper.peekQueueHeadByClusterIdAndStateAndGroupNameAndVmRole(splitClusterHostGroup.getClusterId(),
                            splitClusterHostGroup.getGroupName(),
                            splitClusterHostGroup.getVmRole(),
                            ConfClusterSplitTask.State.RUNNING.getValue(),
                            ConfClusterSplitTask.State.WAITING.getValue());
                    if (confClusterSplitTask != null) {
                        // 执行一个增量创建任务, 这个里面会把所有的增量创建任务都生成
                        executeForSplitTask(splitClusterHostGroup, confClusterSplitTask);
                    } else {
                        if (splitClusterHostGroup.getCreatedTime() != null
                                && new Date().getTime() - splitClusterHostGroup.getCreatedTime().getTime() > 10000) {
                            //timeout and not found any splitTask
                            //update confCluster state to CREATED
                            // Why?
                            composeConfClusterManager.updateConfClusterState(splitClusterHostGroup.getClusterId(), ConfCluster.CREATED);
                        }
                    }
                } catch (Exception e) {
                    getLogger().error("调度实例组扩缩容失败: clusterId=" + splitClusterHostGroup.getClusterId() + ", groupName="
                            + splitClusterHostGroup.getGroupName(), e);
                } finally {
                    this.redisLock.unlock(lockKey);
                    removeFromCacheKey(lockKey);
                    composeConfClusterManager.deleteHostgroupFromQueue(splitClusterHostGroup.getClusterId(), splitClusterHostGroup.getGroupName());
                    getLogger().info("unlock distribute lock,key:{}", lockKey);
                }
            });
            //endregion
        }

        for (ConfClusterHostGroup confClusterHostGroup : clusterHostGroupList) {
            getLogger().info("ClusterHostGroupList 开始 clusterId:{},groupId:{}", confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupId());
            if (confClusterHostGroup.getVmRole().equals("master")
                 || confClusterHostGroup.getVmRole().equals("ambari")) {
             continue;
            }

            int activeCount = executor.getActiveCount();
            if (executor.getActiveCount() > 30) {
                getLogger().info("lock executor activeCount:{} too large", activeCount);
                continue;
            }

            //region start execute thread
            String clusterId = confClusterHostGroup.getClusterId();
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                getLogger().error("not found conf cluster from db,找不到对应的集群配置信息,clusterId:{}", clusterId);
                continue;
            }
            String lockKey;
            if (confCluster.getIsParallelScale()==0){
                lockKey = "scale_group_schedule_lock:cluster:" + confClusterHostGroup.getClusterId();
            } else {
                lockKey = "scale_group_schedule_lock:group:" + confClusterHostGroup.getGroupId();
            }
            getLogger().info("lockKey :{}",lockKey);
            if (inCacheKey(lockKey)) {
                getLogger().info("continue because inCacheKey,key:{},groupName:{}", lockKey, confClusterHostGroup.getGroupName());
                continue;
            }

            boolean needStartThread = false;
            if (composeConfClusterManager.checkHostgroupInQueue(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName())) {
                needStartThread = true;
            } else if (counter == MAX_COUNTER) {
                needStartThread = true;
            }

            if (!needStartThread) {
                getLogger().info("skip because needStartThread is false,clusterId:{},groupName:{}", confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
                continue;
            }
            Future<?> submit = executor.submit(() -> {
                boolean lockSuccess = this.redisLock.tryLock(lockKey);
                if (!lockSuccess) {
                    getLogger().info("try lock distribute lock fail,key:{}", lockKey);
                    return;
                }
                try {
                    getLogger().info("lock distribute lock,key:{}", lockKey);
                    addToCacheKey(lockKey);
                    getLogger().info("plan daemon task start thread,group:{}", confClusterHostGroup);

                    executeForScaleTask(confClusterHostGroup);
                } catch (Exception e) {
                    getLogger().error("execute schedule group:{}", confClusterHostGroup, e);
                } finally {
                    this.redisLock.unlock(lockKey);
                    removeFromCacheKey(lockKey);
                    composeConfClusterManager.deleteHostgroupFromQueue(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
                    getLogger().info("unlock distribute lock,key:{}", lockKey);
                }
            });
            //endregion
        }

        counter++;
        if (counter > MAX_COUNTER) {
            counter = MIN_COUNTER;
        }
        getLogger().info("调度扩缩容任务的执行完成");
    }

    /**
     * 从数据库中获取所有正在运行中的实例组, 用于后续的扩缩容任务调度
     */
    private void refreshAllHostgroupFromDB() {
        refreshRunningHostGroup();
        // 因为集群增量创建时,除了第一批VM随着集群创建完成,其他VM都是通过扩缩容任务创建的,所以需要在这里进行调度
        refreshRunningSplitClusterHostGroup();
        Iterator<ConfClusterHostGroup> iterator = clusterHostGroupList.iterator();
        while (iterator.hasNext()) {
            ConfClusterHostGroup confClusterHostGroup = iterator.next();
            for (ConfClusterHostGroup splitClusterHostGroup : splitClusterHostGroupList) {
                if (Objects.equals(confClusterHostGroup.getGroupId(), splitClusterHostGroup.getGroupId())) {
                    iterator.remove();
                }
            }
        }
        getLogger().info("plan daemon task start,group size:{},split group size:{},counter:{}",
                clusterHostGroupList.size(),
                splitClusterHostGroupList.size(),
                counter);
    }

    /**
     * 执行一个扩缩容任务
     * @param confClusterHostGroup 实例组
     * @throws InterruptedException
     */
    private void executeForScaleTask(ConfClusterHostGroup confClusterHostGroup) throws InterruptedException {
        ConfScalingTask currentTask = taskMapper.peekQueueHeadTask(
                confClusterHostGroup.getClusterId(),
                confClusterHostGroup.getVmRole(),
                confClusterHostGroup.getGroupName(),
                Arrays.asList(ConfScalingTask.SCALINGTASK_Create,
                        ConfScalingTask.SCALINGTASK_Running));

        //TODO: 如果currentTask没查到Create和Running状态的任务, 下面的Rollback就不会执行了.
        String lastExecutedTaskId = null;
        while (currentTask != null) {
            String rollbackTaskId = tryRollbackScaleOutTask(confClusterHostGroup, currentTask);
            if (StringUtils.isNotEmpty(rollbackTaskId)) {
                lastExecutedTaskId = rollbackTaskId;
            } else {
                //region current task is running or created
                if (!StrUtil.equals(lastExecutedTaskId, currentTask.getTaskId())) {
                    //region 新任务不是上一次执行的任务
                    getLogger().info("current task is not last executed task,lastExecutedTask:{},currentTask:{}",
                            lastExecutedTaskId,
                            currentTask);
                    if (isLastTaskIsCoolDown(confClusterHostGroup)) {
                        lastExecutedTaskId = executeSchedule(currentTask);
                    } else {
                        getLogger().info("wait lastFinishedTask cd, clusterId:{},currentTask:{}", currentTask.getClusterId(), currentTask.getTaskId());
                        Thread.sleep(5000);
                    }
                    //endregion
                } else {
                    //region 新任务是上一次执行的任务
                    getLogger().info("current task is last executed task,lastExecutedTask:{},currentTask:{}",
                            lastExecutedTaskId,
                            currentTask);
                    lastExecutedTaskId = executeSchedule(currentTask);
                    //endregion
                    Thread.sleep(5000);
                }
                //endregion
            }

            Thread.sleep(2000);
            currentTask = taskMapper.peekQueueHeadTask(
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getVmRole(),
                    confClusterHostGroup.getGroupName(),
                    Arrays.asList(ConfScalingTask.SCALINGTASK_Create,
                            ConfScalingTask.SCALINGTASK_Running));
            if (currentTask == null) {
                for (int index = 0; index < 10; index++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                for (int index = 0; index < 5; index++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        tryRollbackScaleOutTask(confClusterHostGroup, null);
    }

    private boolean isLastTaskIsCoolDown(ConfClusterHostGroup confClusterHostGroup) {
        ConfScalingTask lastFinishedTask = taskMapper.findLastTaskByStates(
                confClusterHostGroup.getClusterId(),
                confClusterHostGroup.getVmRole(),
                confClusterHostGroup.getGroupName(),
                ConfScalingTask.SCALINGTASK_Complete,
                ConfScalingTask.SCALINGTASK_Failed);
        if (lastFinishedTask == null) {
            return true;
        }

        if (Objects.equals(lastFinishedTask.getState(), ConfScalingTask.SCALINGTASK_Complete)) {
            if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_OUT)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_1MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            } else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_1MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_30SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            } else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_Part_OUT)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_1MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            }else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.scaleType_diskThroughput)) {
                if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                    return false;
                }
                return true;
            } else {
                if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() > DURATION_10SEC) {
                    return false;
                }
                return true;
            }
        } else if (Objects.equals(lastFinishedTask.getState(), ConfScalingTask.SCALINGTASK_Failed)) {
            if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_OUT)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_1MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            } else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_1MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            } else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.ScaleType_Part_OUT)) {
                if (StringUtils.equalsIgnoreCase(lastFinishedTask.getVmRole(), "CORE")) {
                    //region core
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_2MIN) {
                        return false;
                    }
                    return true;
                    //endregion
                } else {
                    //region other type maybe task
                    if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                        return false;
                    }
                    return true;
                    //endregion
                }
            }else if (Objects.equals(lastFinishedTask.getScalingType(), ConfScalingTask.scaleType_diskThroughput)) {
                if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
                    return false;
                }
                return true;
            } else {
                if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() > DURATION_1MIN) {
                    return false;
                }
                return true;
            }
        } else if (System.currentTimeMillis() - lastFinishedTask.getEndTime().getTime() < DURATION_10SEC) {
            return false;
        }
        return true;
    }

    /**
     * 执行一个增量创建集群的增量任务
     * @param confClusterHostGroup 集群的实例组
     * @param currentTask 增量任务
     * @throws InterruptedException
     */
    private void executeForSplitTask(ConfClusterHostGroup confClusterHostGroup, ConfClusterSplitTask currentTask) throws InterruptedException {
        String lastExecutedTaskId = null;

        // 使用While循环, 是为了一次性将一个实例组的所有增量任务都生成.
        while (currentTask != null) {
            // 清理实例组扩容失败所产生的VM, 返回的是清理VM的缩容任务ID
            String rollbackTaskId = tryRollbackScaleOutTask(confClusterHostGroup, null);
            if (StringUtils.isNotEmpty(rollbackTaskId)) {
                lastExecutedTaskId = rollbackTaskId;
            } else {
                // TODO: 这个两个ID能比较么? 而且if的两个分支代码一样.
                if (!StrUtil.equals(lastExecutedTaskId, currentTask.getId())) {
                    //region 新任务不是上一次执行的任务
                    getLogger().info("current split task is not last executed task,lastExecutedTask:{},currentTask:{}",
                            lastExecutedTaskId,
                            currentTask);
                    lastExecutedTaskId = clusterSplitCreationTaskManager.startSplitTask(currentTask, (createdConfScalingTask) -> {
                        executeSchedule(createdConfScalingTask);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    });
                    //endregion
                } else {
                    //region 新任务是上一次执行的任务
                    getLogger().info("current task is last executed task,lastExecutedTask:{},currentTask:{}",
                            lastExecutedTaskId,
                            currentTask);
                    lastExecutedTaskId = clusterSplitCreationTaskManager.startSplitTask(currentTask, (createdConfScalingTask) -> {
                        executeSchedule(createdConfScalingTask);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    });
                    //endregion
                }
            }

            Thread.sleep(5000);
            currentTask = confClusterSplitTaskMapper.peekQueueHeadByClusterIdAndStateAndGroupNameAndVmRole(confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupName(),
                    confClusterHostGroup.getVmRole(),
                    ConfClusterSplitTask.State.RUNNING.getValue(),
                    ConfClusterSplitTask.State.WAITING.getValue());
            if (currentTask == null) {
                //update confCluster state to CREATED
                composeConfClusterManager.updateConfClusterState(confClusterHostGroup.getClusterId(), ConfCluster.CREATED);
            }
        }
    }

    /**
     * 尝试是否要进行扩容任务的回滚
     * 如果需要回滚就产生指定机器的缩容任务<br/>
     * 只处理实例组失败的扩容任务.
     *
     * @param confClusterHostGroup
     * @return
     */
    private String tryRollbackScaleOutTask(ConfClusterHostGroup confClusterHostGroup, ConfScalingTask currentTask) {
        ConfScalingTask lastFailureTask = taskMapper.findLastFinishedTask(
                confClusterHostGroup.getClusterId(),
                confClusterHostGroup.getVmRole(),
                confClusterHostGroup.getGroupName(),
                ConfScalingTask.SCALINGTASK_Failed);
        if (lastFailureTask == null) {
            return null;
        }

        if (currentTask != null
                && StringUtils.equals(currentTask.getScaleoutTaskId(), lastFailureTask.getTaskId())) {
            return null;
        }

        // 只处理扩容,所以不是扩容时退出
        if (!lastFailureTask.getScalingType().equals(ConfScalingTask.ScaleType_OUT)) {
            return null;
        }

        String lockKey = "lock_rollback_task:" + confClusterHostGroup.getClusterId() + ":" + confClusterHostGroup.getGroupId() + ":" + lastFailureTask.getTaskId();
        String cdKey = "cd_rollback_task:" + confClusterHostGroup.getClusterId() + ":" + confClusterHostGroup.getGroupId() + ":" + lastFailureTask.getTaskId();
        boolean lockResult = this.redisLock.tryLock(lockKey);
        if (!lockResult) {
            return null;
        }

        try {
            if (this.redisLock.haveKey(cdKey)) {
                return null;
            }

            getLogger().info("找到失败的扩容任务,需要清理此失败任务的所有VM:{}", lastFailureTask);
            int rollbackTaskCount = taskMapper.countByScaleOutTaskIdAndScalingTypeAndState(
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupName(),
                    lastFailureTask.getTaskId(),
                    ConfScalingTask.ScaleType_IN,
                    ConfScalingTask.SCALINGTASK_Create,
                    ConfScalingTask.SCALINGTASK_Running);
            if (rollbackTaskCount > 0) {
                getLogger().info("already create rollback scale in task for ScaleOutTask, 失败扩容任务已经存在清理任务,不重复处理,跳过:{}", lastFailureTask);
                return null;
            }

            // 找到所有失败扩容任务申请的VM,供后续销毁使用. 只取未知状态/运行中状态, 因为已销毁状态不需要进行清理
            List<InfoClusterVm> infoClusterVms = this.infoClusterVmMapper.selectByClusterIdAndScaleOutTaskIdAndStates(lastFailureTask.getClusterId(),
                    lastFailureTask.getTaskId(),
                    InfoClusterVm.VM_UNKNOWN,
                    InfoClusterVm.VM_RUNNING);
            if (infoClusterVms.size() == 0) {
                getLogger().info("not found any vm in running state for ScaleOutTask:{}", lastFailureTask);
                return null;
            }

            List<String> vms = infoClusterVms.stream().map(InfoClusterVm::getVmName).collect(Collectors.toList());

            getLogger().info("try rollbackScaleOutTask taskId:{},clusterId:{},groupId:{},groupName:{},vmNames:{}",
                    lastFailureTask.getTaskId(),
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    confClusterHostGroup.getGroupName(),
                    vms);

            // 构造清理扩容失败任务的VM的请求对象. 生产清理任务在compose服务, 不能直接本地调用, 需要通过HTTP接口调用
            ScaleInForDeleteTaskVmReq scaleInForDeleteTaskVmReq = new ScaleInForDeleteTaskVmReq(
                    lastFailureTask.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    lastFailureTask.getTaskId(),
                    vms,
                    DateUtils.addMilliseconds(lastFailureTask.getCreateTime(), 100)
            );

            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(confClusterHostGroup.getClusterId());
            if (confCluster == null) {
                getLogger().error("not found conf cluster from db,找不到对应的集群信息,不需要进行VM清理操作,跳过. clusterId:{}", confClusterHostGroup.getClusterId());
                return null;
            } else if (!confCluster.getState().equals(ConfCluster.CREATED)) {
                getLogger().error("conf cluster' state is not eq CREATED,集群已被删除,不需要再进行VM清理操作,跳过. clusterId:{}", confClusterHostGroup.getClusterId());
                return null;
            }

            // 调用Compose服务,生成清理VM任务.
            getLogger().info("begin createScaleInForDeleteTaskVm,req:{}", scaleInForDeleteTaskVmReq);
            ResultMsg scaleInResult = composeService.createScaleInForDeleteTaskVm(scaleInForDeleteTaskVmReq);
            if (!scaleInResult.isSuccess()) {
                getLogger().info("error createScaleInForDeleteTaskVm,req:{},errorMsg:{}", scaleInForDeleteTaskVmReq, scaleInResult.getErrorMsg());
                return null;
            } else {
                this.redisLock.save(cdKey, "1", 10 * 60000);
            }

            // 将实例组放到队列中
            composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
            getLogger().info("success createScaleInForDeleteTaskVm,req:{},resp:{}", scaleInForDeleteTaskVmReq, scaleInResult.getData());
            if (scaleInResult.getData() == null) {
                return null;
            }
            return scaleInResult.getData().toString();
        } finally {
            this.redisLock.unlock(lockKey);
        }
    }

    /**
     * 从本地存储中删除一个Key
     * @param lockKey
     */
    private static void removeFromCacheKey(String lockKey) {
        synchronized (cacheKeys) {
            Iterator<String> iterator = cacheKeys.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (StringUtils.equals(next, lockKey)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 将一个Key保存到本地存储中
     * @param lockKey
     */
    private static void addToCacheKey(String lockKey) {
        synchronized (cacheKeys) {
            Iterator<String> iterator = cacheKeys.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (StringUtils.equals(next, lockKey)) {
                    iterator.remove();
                }
            }
            cacheKeys.add(lockKey);
        }
    }

    /**
     * 判断一个Key是否在本地缓存中
     * @param lockKey
     * @return
     */
    private static boolean inCacheKey(String lockKey) {
        Iterator<String> iterator = cacheKeys.iterator();
        try {
            boolean exist = false;
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (next.equals(lockKey)) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private void refreshRunningHostGroup() {
        if (clusterHostGroupList == null) {
            clusterHostGroupList = confClusterHostGroupNeoMapper.listRunningClusterHostGroup();
            lastRefreshClusterHostGroupTicks = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - lastRefreshClusterHostGroupTicks > DURATION_2MIN) {
                clusterHostGroupList = confClusterHostGroupNeoMapper.listRunningClusterHostGroup();
                lastRefreshClusterHostGroupTicks = System.currentTimeMillis();
            } else {
                int hostGroupCount = confClusterHostGroupNeoMapper.countRunningClusterHostGroup();
                if (hostGroupCount != clusterHostGroupList.size()) {
                    clusterHostGroupList = confClusterHostGroupNeoMapper.listRunningClusterHostGroup();
                    lastRefreshClusterHostGroupTicks = System.currentTimeMillis();
                }
            }
        }
    }

    private void refreshRunningSplitClusterHostGroup() {
        if (splitClusterHostGroupList == null) {
            splitClusterHostGroupList = confClusterHostGroupNeoMapper.listRunningSplitClusterHostGroup();
            lastRefreshSplitClusterHostGroupTicks = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - lastRefreshSplitClusterHostGroupTicks > DURATION_30SEC) {
                splitClusterHostGroupList = confClusterHostGroupNeoMapper.listRunningSplitClusterHostGroup();
                lastRefreshSplitClusterHostGroupTicks = System.currentTimeMillis();
            } else {
                int hostGroupCount = confClusterHostGroupNeoMapper.countRunningSplitClusterHostGroup();
                if (hostGroupCount != splitClusterHostGroupList.size()) {
                    splitClusterHostGroupList = confClusterHostGroupNeoMapper.listRunningSplitClusterHostGroup();
                    lastRefreshSplitClusterHostGroupTicks = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     *
     * @param currentTask
     * @return
     */
    private String executeSchedule(ConfScalingTask currentTask) {
        getLogger().info("executeSchedule found task from db, clusterId={}, groupName={}, taskId={}",
                currentTask.getClusterId(),
                currentTask.getGroupName(),
                currentTask.getTaskId());

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(currentTask.getClusterId());
        if (confCluster == null) {
            getLogger().error("not found conf cluster from db,clusterId:{}", currentTask.getClusterId());
            currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
            currentTask.setBegTime(new Date());
            currentTask.setEndTime(new Date());
            currentTask.appendRemark("找不到对应的集群配置信息");
            currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            taskMapper.updateByPrimaryKeySelective(currentTask);
            cleanScalingTaskIdFromInfoClusterVm(currentTask);
            composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            return currentTask.getTaskId();
        } else if (!confCluster.getState().equals(ConfCluster.CREATED)) {
            getLogger().error("conf cluster' state is not eq CREATED,clusterId:{}", currentTask.getClusterId());
            currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
            currentTask.setBegTime(new Date());
            currentTask.setEndTime(new Date());
            currentTask.appendRemark("集群已被删除");
            currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            taskMapper.updateByPrimaryKeySelective(currentTask);
            cleanScalingTaskIdFromInfoClusterVm(currentTask);
            composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            return currentTask.getTaskId();
        }

        InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(currentTask.getClusterId());
        if (infoCluster == null) {
            getLogger().error("not found info cluster from db,clusterId:{}", currentTask.getClusterId());
            currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
            currentTask.setBegTime(new Date());
            currentTask.setEndTime(new Date());
            currentTask.appendRemark("找不到对应的集群信息");
            currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            taskMapper.updateByPrimaryKeySelective(currentTask);
            cleanScalingTaskIdFromInfoClusterVm(currentTask);
            composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            return currentTask.getTaskId();
        }

        String planOperationName = null;
        if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
            planOperationName = InfoClusterOperationPlan.Plan_OP_ScaleIn;
        } else if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_OUT)) {
            // 如果是补足驱逐VM, 设置planOperationName为Plan_OP_ScaleOutEvictVm
            if (Objects.equals(currentTask.getOperatiionType(), ConfScalingTask.Operation_type_Complete_Evict_Vm)) {
                planOperationName = InfoClusterOperationPlan.Plan_OP_ScaleOutEvictVm;
            } else {
                planOperationName = InfoClusterOperationPlan.Plan_OP_ScaleOut;
            }
        } else if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_Part_OUT)) {
            planOperationName = InfoClusterOperationPlan.Plan_OP_Part_ScaleOut;
        } else if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.scaleType_diskThroughput)) {
            planOperationName = InfoClusterOperationPlan.Plan_OP_pv2DiskThroughput;
        }else {
            getLogger().error("invalid scaling type,clusterId={},groupName={},taskId={}",
                    currentTask.getClusterId(),
                    currentTask.getGroupName(),
                    currentTask.getTaskId());
            currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
            currentTask.setBegTime(new Date());
            currentTask.setEndTime(new Date());
            currentTask.appendRemark("无效的ScalingType:" + currentTask.getScalingType());
            currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
            taskMapper.updateByPrimaryKeySelective(currentTask);
            composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            return currentTask.getTaskId();
        }

        getLogger().info("====== 计算任务类型: planOperationName={}", planOperationName);

        if (Objects.equals(currentTask.getState(), ConfScalingTask.SCALINGTASK_Running)) {
            // 如果任务在运行中, 则先检查任务运行是否超时, 如果超时, 设置任务执行失败, 没超时, 直接返回当前执行的任务ID
            long runningTaskDurationMs = System.currentTimeMillis() - currentTask.getBegTime().getTime();
            if (runningTaskDurationMs > 1000 * 60 * 60 * 48) {
                getLogger().info("current task is running, terminate running task because time out({}ms),clusterId={},vmRole={},taskId={}",
                        runningTaskDurationMs,
                        currentTask.getClusterId(),
                        currentTask.getVmRole(),
                        currentTask.getTaskId());

                currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
                currentTask.setEndTime(new Date());
                currentTask.appendRemark("执行任务超时" + runningTaskDurationMs + "ms");
                currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
                taskMapper.updateByPrimaryKeySelective(currentTask);
                composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
                return currentTask.getTaskId();
            } else {
                getLogger().info("current task is running, duration({}ms),clusterId={},vmRole={},taskId={}",
                        runningTaskDurationMs,
                        currentTask.getClusterId(),
                        currentTask.getVmRole(),
                        currentTask.getTaskId());
                return currentTask.getTaskId();
            }
        }

        // 下面是调整扩缩容数量, 如:扩容前数量, 扩容后数量, 期望数量
        boolean adjustScaleParamPass = true;
        if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
            ResultMsg resultMsg = scalingService.adjustScaleInCount(currentTask);
            if (!resultMsg.getResult()) {
                adjustScaleParamPass = false;
                getLogger().warn("adjustScaleInCount not pass, clusterId={}, groupName={}, taskId={}, msg:{}",
                        currentTask.getClusterId(),
                        currentTask.getGroupName(),
                        currentTask.getTaskId(),
                        resultMsg.getMsg());

                currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
                currentTask.setBegTime(new Date());
                currentTask.setEndTime(new Date());
                currentTask.appendRemark(resultMsg.getErrorMsg());
                currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
                taskMapper.updateByPrimaryKeySelective(currentTask);
                cleanScalingTaskIdFromInfoClusterVm(currentTask);
                composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            }
        } else if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_OUT)) {
            ResultMsg resultMsg = scalingService.adjustScaleOutCount(currentTask);
            if (!resultMsg.getResult()) {
                adjustScaleParamPass = false;
                getLogger().warn("adjustScaleOutCount not pass, clusterId={}, groupName={}, taskId={}, msg:{}",
                        currentTask.getClusterId(),
                        currentTask.getGroupName(),
                        currentTask.getTaskId(),
                        resultMsg.getMsg());

                currentTask.setState(ConfScalingTask.SCALINGTASK_Failed);
                currentTask.setBegTime(new Date());
                currentTask.setEndTime(new Date());
                currentTask.appendRemark(resultMsg.getErrorMsg());
                currentTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
                taskMapper.updateByPrimaryKeySelective(currentTask);
                cleanScalingTaskIdFromInfoClusterVm(currentTask);
                composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
            }
        }

        if (adjustScaleParamPass) {
            // 调整扩缩容参数完成后, 开始调用compose服务, 执行扩缩容任务
            String lockKeyForTask = "start_task_plan_lock:" + currentTask.getTaskId();
            if (redisLock.tryLock(lockKeyForTask)) {
                try {
                    getLogger().info("planExecService createPlan begin, clusterId={}, clusterReleaseVer={}, planOperationName={}, taskId={}",
                            currentTask.getClusterId(),
                            confCluster.getClusterReleaseVer(),
                            planOperationName,
                            currentTask.getTaskId());

                    // 调用接口执行任务
                    ResultMsg plan = planExecService.createPlanforscaling(
                            currentTask.getClusterId(),
                            planOperationName,
                            confCluster.getClusterReleaseVer(),
                            currentTask.getTaskId());

                    if (plan.getResult()) {
                        getLogger().info("planExecService createPlan success, clusterId={}, clusterReleaseVer={}, planOperationName={}, taskId={}",
                                currentTask.getClusterId(),
                                confCluster.getClusterReleaseVer(),
                                planOperationName,
                                currentTask.getTaskId());
                    } else {
                        getLogger().error("planExecService createPlan error, clusterId={}, vmRole={}, taskId={}, error msg:{}",
                                currentTask.getClusterId(),
                                currentTask.getVmRole(),
                                currentTask.getTaskId(),
                                plan.getErrorMsg());
                    }
                    composeConfClusterManager.sendDaemonTaskSignal(currentTask.getClusterId(), currentTask.getGroupName());
                }catch (RuntimeException e){
                    getLogger().error("createPlanforscaling error:" , e);
                }finally {
                    //释放锁的原因:有一部分任务会被锁住,无法再次执行
                    redisLock.tryUnlock(lockKeyForTask);
                }
            } else {
                getLogger().error("lock error:" + lockKeyForTask);
            }
        }
        return currentTask.getTaskId();
    }

    private void cleanScalingTaskIdFromInfoClusterVm(ConfScalingTask currentTask) {
//        if (Objects.equals(currentTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
//            infoClusterVmMapper.cleanScaleInTaskId(currentTask.getClusterId(), currentTask.getGroupName(), currentTask.getTaskId());
//            getLogger().info("cleanScaleInTaskId clusterId:{}, groupName:{}, taskId:{}",
//                    currentTask.getClusterId(),
//                    currentTask.getGroupName(),
//                    currentTask.getTaskId());
//        }
    }

}