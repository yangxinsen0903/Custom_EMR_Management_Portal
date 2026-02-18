package com.sunbox.sdpadmin.task;

import com.alibaba.fastjson.JSON;
import com.sunbox.dao.mapper.ClusterDestroyTaskMapper;
import com.sunbox.domain.ClusterDestroyTask;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.service.BizConfigService;
import com.sunbox.service.IConfigInfoService;
import com.sunbox.service.consts.DestroyStatusConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sunbox.constant.BizConfigConstants.DESTORYTASKKEY;
import static com.sunbox.constant.RedisLockKeys.*;

@Component
public class ClusterDaemonTask implements BaseCommonInterFace {

    //N秒内最多销毁M个集群
    private static final String CLUSTERDAEMONTASK_START = "ClusterDaemonTask_start";

    @Resource
    private DistributedRedisLock redisLock;

    @Resource
    private ClusterDestroyTaskMapper clusterDestroyTaskMapper;

    @Resource
    AdminApiService adminApiService;

    @Resource
    IConfigInfoService configInfoService;

    @Autowired
    BizConfigService bizConfigService;

    /**
     * 增加定时任务：定时销毁集群。
     * 每隔10秒钟（默认）从销毁任务表中取出M个状态为"待销毁“的销毁任务；
     * 检查是否触发限流，
     * 如果触发限流：退出任务；
     * 未触发限流：1. 更新集群状态为销毁中；2. 向ServiceBus发送销毁集群的消息；3.更新销毁任务表中状态为销毁中。
     * 销毁完成后：1. 更新销毁任务表中状态为已销毁或销毁失败；(在另一个task中, 查询是否有此任务, 有就更新)
     */
    @Scheduled(cron = "${cluster.destroy.task.time:1/10 * * * * ?}")
    public void start() {
        getLogger().info("ClusterDaemonTask,start");
        boolean lockResult = this.redisLock.tryLock(CLUSTERDAEMONTASK_START,TimeUnit.SECONDS,1,60);
        if (!lockResult) {
            getLogger().error("ClusterDaemonTask lock error key:{}", CLUSTERDAEMONTASK_START);
            return;
        }
        try {
            // 待销毁 , 销毁状态:待销毁1，销毁中2，已销毁3，销毁失败0
            List<ClusterDestroyTask> destroyTaskVoList = getTask(DestroyStatusConstant.DESTROY_STATUS_WAITING);
            if (CollectionUtils.isEmpty(destroyTaskVoList)) {
                return;
            }
            List<String> clusterIdList = destroyTaskVoList.stream().map(vo -> vo.getClusterId()).collect(Collectors.toList());

            List<String> noLimitIds = getNolimitIds(clusterIdList);
            //未触发限流, 销毁集群
            if (CollectionUtils.isEmpty(noLimitIds)) {
                return;
            }
            for (String key : noLimitIds) {
                ClusterDestroyTask clusterDestroyTask = destroyTaskVoList.stream().filter(clu -> clu.getClusterId().equalsIgnoreCase(key)).findFirst().get();
                String userName = clusterDestroyTask.getCreatedby();
                Map<String, String> map = new HashMap<>();
                map.put("clusterId", key);
                //ClusterDestroyTask res = destroyTaskVoList.stream().filter(clu -> clu.getClusterId().equalsIgnoreCase(key)).findFirst().get();
                map.put("fromTask", "1");
                ResultMsg resultMsg = adminApiService.deleteCluster(JSON.toJSONString(map), userName);
                if (resultMsg.getResult()) {
                    getLogger().info("ClusterDaemonTask,resultMsg:{}", resultMsg);
                    // 更新销毁任务表中状态为销毁中
                    updateBatchTaskById(Collections.singletonList(key), DestroyStatusConstant.DESTROY_STATUS_ING,new Date(),null);
                }else {
                    getLogger().error("ClusterDaemonTask,error,resultMsg:{}", resultMsg);
                    // 更新销毁任务表中状态为销毁失败
                    updateBatchTaskById(Collections.singletonList(key), DestroyStatusConstant.DESTROY_STATUS_FAIL,new Date(),null);
                }
            }

        } catch (Exception e) {
            getLogger().error("ClusterDaemonTask start error", e);
        } finally {
            this.redisLock.unlock(CLUSTERDAEMONTASK_START);
        }
    }

    /**
     * 获取销毁任务表中的需要销毁的集群id
     * @param clusterIdList
     * @return
     */
    public List<String> getNolimitIds(List<String> clusterIdList) {
        getLogger().info("ClusterDaemonTask ,getNolimitIds,clusterIdList{}", clusterIdList);
        if (CollectionUtils.isEmpty(clusterIdList)) {
            return new ArrayList<>();
        }
        List<String> noLimitIds = redisLock.getNolimitIds(DESTORYTASKKEY, clusterIdList);
        getLogger().info("ClusterDaemonTask ,getNolimitIds,noLimitIds:{}", noLimitIds);
        return noLimitIds;
    }

    //销毁完成后：1. 更新销毁任务表中状态为已销毁或销毁失败； PlanExecServiceImpl
    //  @Scheduled(cron = "${cluster.destroy.listentask.time:1/5 * * * * ?}")

    /**
     * 删除集群id的缓存集合
     * @param clusterIdList
     */
    @Deprecated
    private void updateRedisLimit(List<String> clusterIdList) {
        if (CollectionUtils.isEmpty(clusterIdList)) {
            return;
        }
        for (String id : clusterIdList) {
            redisLock.mapRemove(CLUSTERDAEMONTASK_CLUSTER_STATUS, id);
            redisLock.mapRemove(CLUSTERDAEMONTASK_CLUSTER_TIME, id);
        }
    }

    /**
     * 获取不限流的任务
     *
     * @param limitTimePa
     * @param limitCountPa
     * @param clusterIdList 集群ids
     * @return
     */
    @Deprecated
    public List<String> getNolimitTask(Integer limitTimePa, Integer limitCountPa, List<String> clusterIdList) {
        if (CollectionUtils.isEmpty(clusterIdList) || limitTimePa < 1 || limitCountPa < 1) {
            return new ArrayList<>();
        }
        getLogger().info("ClusterDaemonTask getNolimitTask1 limitTimePa:{},limitCountPa:{},clusterIdList{}",limitTimePa,limitCountPa,clusterIdList);

        List<String> noLimitIds = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();
        // 正在销毁中的集群, id+时间
        Map<String, String> destroyIngCluster = new HashMap<>();
        long now = System.currentTimeMillis() / 1000L;
        for (String key : clusterIdList) {
            // key: ClusterDaemonTask+key, mkey: clusterId    mvalue: 目前的时间
            String status = redisLock.mapGetValue(CLUSTERDAEMONTASK_CLUSTER_STATUS, key);
            if (!"2".equals(status)) {
                //  不是销毁中才重新加入集合 ,更新时间, , 1:已加入集合
                redisLock.mapSetValue(CLUSTERDAEMONTASK_CLUSTER_STATUS, key, "1", TIME_24H, TimeUnit.SECONDS);
                redisLock.mapSetValue(CLUSTERDAEMONTASK_CLUSTER_TIME, key, String.valueOf(now), TIME_24H, TimeUnit.SECONDS);
                getLogger().info("ClusterDaemonTask getNolimitTask2 key:{}, ",key);
            }
        }
        // 要求统计所有正在销毁中的
        Map<String, String> stringStringMapStatus = redisLock.mapGet(CLUSTERDAEMONTASK_CLUSTER_STATUS);
        Map<String, String> stringStringMapTime = redisLock.mapGet(CLUSTERDAEMONTASK_CLUSTER_TIME);
        stringStringMapStatus.forEach((key, value) -> {
            if ("2".equals(value)) {
                destroyIngCluster.put(key, stringStringMapTime.get(key));
            }
        });
        getLogger().info("ClusterDaemonTask destroyIngCluster3 :{}, ",destroyIngCluster);
        //检查限制的时间内,有几个,
        destroyIngCluster.forEach((key, time) -> {
            if (now - Long.parseLong(time) <= limitTimePa) {
                count.getAndIncrement();
            }
        });
        //  全局设置限流参数：N秒内最多销毁M个集群
        // 查询状态2的数量 , 检查限制的时间内,有几个, 没超过就加入队列
        getLogger().info("ClusterDaemonTask getNolimitTask4 count.get():{},limitCountPa:{}", count.get(),limitCountPa);

        for (String key : clusterIdList) {
            // 如果id不在销毁中, 才会判断是否限制
            if (destroyIngCluster.containsKey(key)) {
                continue;
            }
            if (count.get() < limitCountPa) {
                // 加入队列. 集群状态变为2
                redisLock.mapSetValue(CLUSTERDAEMONTASK_CLUSTER_STATUS, key, "2", TIME_24H, TimeUnit.SECONDS);
                redisLock.mapSetValue(CLUSTERDAEMONTASK_CLUSTER_TIME, key, String.valueOf(now), TIME_24H, TimeUnit.SECONDS);
                // 数量需要重新计算, 时间建间隔短,不需要重新计算
                count.getAndIncrement();
                noLimitIds.add(key);
                getLogger().info("ClusterDaemonTask getNolimitTask6 ,noLimitIdsadding:{}", noLimitIds);
            }
        }
        return noLimitIds;
    }

    public List<ClusterDestroyTask> getTask(String status) {
        List<ClusterDestroyTask> clusterDestroyTasks = clusterDestroyTaskMapper.selectByStatusList(Collections.singletonList(status));
        return clusterDestroyTasks;
    }

    public void updateBatchTaskById(List<String> clusterIds, String destroy_status, Date startDestroyTime, Date endDestroyTime) {
        if (CollectionUtils.isEmpty(clusterIds)) {
            return;
        }
        clusterDestroyTaskMapper.updateBatchTaskById(clusterIds, destroy_status, startDestroyTime, endDestroyTime);
    }


}
