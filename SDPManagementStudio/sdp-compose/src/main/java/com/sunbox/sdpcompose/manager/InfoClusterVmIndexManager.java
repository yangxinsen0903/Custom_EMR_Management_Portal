package com.sunbox.sdpcompose.manager;

import com.sunbox.domain.InfoClusterVmIndex;
import com.sunbox.domain.InfoClusterVmIndexHistory;
import com.sunbox.sdpcompose.mapper.ConfScalingTaskMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmIndexHistoryMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmIndexMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmMapper;
import com.sunbox.util.DistributedRedisLock;
import org.slf4j.Logger;

import javax.sound.sampled.Line;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class InfoClusterVmIndexManager {
    private DistributedRedisLock redisLock;
    private InfoClusterVmIndexMapper infoClusterVmIndexMapper;
    private InfoClusterVmIndexHistoryMapper infoClusterVmIndexHistoryMapper;
    private InfoClusterVmMapper infoClusterVmMapper;
    private ConfScalingTaskMapper confScalingTaskMapper;
    private Logger logger;

    public InfoClusterVmIndexManager(Logger logger,
                                     DistributedRedisLock redisLock,
                                     InfoClusterVmMapper infoClusterVmMapper,
                                     ConfScalingTaskMapper confScalingTaskMapper,
                                     InfoClusterVmIndexMapper infoClusterVmIndexMapper,
                                     InfoClusterVmIndexHistoryMapper infoClusterVmIndexHistoryMapper) {
        this.logger = logger;
        this.redisLock = redisLock;
        this.infoClusterVmMapper = infoClusterVmMapper;
        this.confScalingTaskMapper = confScalingTaskMapper;
        this.infoClusterVmIndexMapper = infoClusterVmIndexMapper;
        this.infoClusterVmIndexHistoryMapper = infoClusterVmIndexHistoryMapper;
    }

    public int requestNewVmIndex(String clusterId, String vmRole, String taskId, Date taskCreatedTime, int scaleOutCount) {
        logger.info("requestNewVmIndex,clusterId:{},vmRole:{},taskId:{},taskCreatedTime:{},scaleOutCount:{}",
                clusterId,
                vmRole,
                taskId,
                taskCreatedTime,
                scaleOutCount);
        String lockKey = "request_vm_index:" + clusterId + ":" + vmRole;
        boolean lockResult = this.redisLock.tryLock(lockKey, TimeUnit.SECONDS, 30L, 10);
        if (!lockResult) {
            throw new RuntimeException("获取锁失败。");
        }
        logger.info("lock success,lockKey:{}", lockKey);

        try {
            //region 根据task_id 查询是否已经存在记录，用于适配重试的逻辑
            InfoClusterVmIndexHistory infoClusterVmIndexHistory = infoClusterVmIndexHistoryMapper.findByClusterIdAndVmRoleAndTaskId(clusterId, vmRole, taskId);
            if (infoClusterVmIndexHistory != null) {
                return infoClusterVmIndexHistory.getBeforeIndex();
            }
            //endregion

            InfoClusterVmIndex infoClusterVmIndex = this.infoClusterVmIndexMapper.findByClusterIdAndVmRole(clusterId, vmRole);
            if (infoClusterVmIndex == null) {
                int calculateVmIndex = calculateVmIndex(clusterId, vmRole, taskId, taskCreatedTime);
                infoClusterVmIndex = new InfoClusterVmIndex();
                infoClusterVmIndex.setClusterId(clusterId);
                infoClusterVmIndex.setVmRole(vmRole);
                infoClusterVmIndex.setCreateTime(new Date());
                infoClusterVmIndex.setModifiedTime(new Date());
                infoClusterVmIndex.setBeforeIndex(calculateVmIndex);
                infoClusterVmIndex.setDeltaIndex(scaleOutCount);
                infoClusterVmIndex.setAfterIndex(calculateVmIndex + scaleOutCount);
                infoClusterVmIndex.setTaskId(taskId);
                infoClusterVmIndex.setEndIndex(calculateVmIndex + scaleOutCount);
                infoClusterVmIndexMapper.insertEndIndex(infoClusterVmIndex);
                this.logger.info("requestNewVmIndex insert:{}", infoClusterVmIndex);
                createInfoClusterVmIndexHistory(clusterId,
                        vmRole,
                        taskId,
                        infoClusterVmIndex.getBeforeIndex(),
                        infoClusterVmIndex.getAfterIndex(),
                        infoClusterVmIndex.getDeltaIndex(),
                        infoClusterVmIndex.getModifiedTime());
                return calculateVmIndex;
            } else {
                int endIndex = infoClusterVmIndex.getEndIndex();
                infoClusterVmIndex.setClusterId(clusterId);
                infoClusterVmIndex.setVmRole(vmRole);
                infoClusterVmIndex.setModifiedTime(new Date());
                infoClusterVmIndex.setBeforeIndex(infoClusterVmIndex.getEndIndex());
                infoClusterVmIndex.setDeltaIndex(scaleOutCount);
                infoClusterVmIndex.setAfterIndex(infoClusterVmIndex.getEndIndex() + scaleOutCount);
                infoClusterVmIndex.setTaskId(taskId);
                infoClusterVmIndex.setEndIndex(infoClusterVmIndex.getEndIndex() + scaleOutCount);
                infoClusterVmIndexMapper.updateEndIndex(infoClusterVmIndex);
                this.logger.info("requestNewVmIndex update:{}", infoClusterVmIndex);
                createInfoClusterVmIndexHistory(clusterId,
                        vmRole,
                        taskId,
                        infoClusterVmIndex.getBeforeIndex(),
                        infoClusterVmIndex.getAfterIndex(),
                        infoClusterVmIndex.getDeltaIndex(),
                        infoClusterVmIndex.getModifiedTime());
                return endIndex;
            }
        } finally {
            this.redisLock.unlock(lockKey);
        }
    }

    private int calculateVmIndex(String clusterId, String vmRole, String taskId, Date taskCreatedTime) {
        this.logger.info("begin calculateVmIndex,clusterId:{},vmRole:{},taskId:{},taskCreatedTime:{}",
                clusterId,
                vmRole,
                taskId,
                taskCreatedTime);
        int create_count = infoClusterVmMapper.getCreateVmCountByVmRole(clusterId, vmRole);
        Integer scale_count = confScalingTaskMapper.getAllScaleOutVMCountByClusterAndVmRole(clusterId,
                vmRole,
                taskId,
                taskCreatedTime);
        if (scale_count == null) {
            scale_count = 0;
        }

        int endIndex = create_count + scale_count + 1;

        this.logger.info("end calculateVmIndex,clusterId:{},vmRole:{},taskId:{},taskCreatedTime:{},endIndex:{}",
                clusterId,
                vmRole,
                taskId,
                taskCreatedTime,
                endIndex);
        return endIndex;
    }

    private void createInfoClusterVmIndexHistory(String clusterId,
                                                 String vmRole,
                                                 String taskId,
                                                 Integer beforeCount,
                                                 Integer afterCount,
                                                 Integer deltaCount,
                                                 Date createdTime) {
        InfoClusterVmIndexHistory infoClusterVmIndexHistory = new InfoClusterVmIndexHistory();
        infoClusterVmIndexHistory.setClusterId(clusterId);
        infoClusterVmIndexHistory.setVmRole(vmRole);
        infoClusterVmIndexHistory.setCreateTime(new Date());
        infoClusterVmIndexHistory.setModifiedTime(new Date());
        infoClusterVmIndexHistory.setBeforeIndex(beforeCount);
        infoClusterVmIndexHistory.setDeltaIndex(deltaCount);
        infoClusterVmIndexHistory.setAfterIndex(afterCount);
        infoClusterVmIndexHistory.setTaskId(taskId);
        infoClusterVmIndexHistory.setCreateTime(createdTime);
        infoClusterVmIndexHistory.setModifiedTime(createdTime);
        infoClusterVmIndexHistoryMapper.insertHistory(infoClusterVmIndexHistory);
        this.logger.info("insert InfoClusterVmIndexHistory:{}", infoClusterVmIndexHistory);
    }
}