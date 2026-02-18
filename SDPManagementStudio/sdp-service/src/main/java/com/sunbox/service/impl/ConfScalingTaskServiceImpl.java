package com.sunbox.service.impl;

import com.sunbox.dao.mapper.ConfScalingTaskNeoMapper;
import com.sunbox.domain.ConfScalingTask;
import com.sunbox.service.IConfScalingTaskService;
import com.sunbox.service.consts.ComposeConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ConfScalingTaskServiceImpl implements IConfScalingTaskService, BaseCommonInterFace {
    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    /**
     * 保存pv2磁盘调整任务
     * @param clusterId
     * @param groupName
     * @param vmRole
     * @param newDataDiskIOPSReadWrite
     * @param newDataDiskMBpsReadWrite
     * @param scaleoutTaskId
     * @return
     */
    @Override
    public ConfScalingTask savePv2DiskScalingTask(String clusterId,
                                                  String groupName,
                                                  String vmRole,
                                                  Integer newDataDiskIOPSReadWrite,
                                                  Integer newDataDiskMBpsReadWrite,
                                                  String scaleoutTaskId) {
        ConfScalingTask confScalingTask = new ConfScalingTask();
        confScalingTask.setTaskId(UUID.randomUUID().toString());
        confScalingTask.setClusterId(clusterId);
        confScalingTask.setScalingType(ConfScalingTask.scaleType_diskThroughput);
        confScalingTask.setBeforeScalingCount(newDataDiskIOPSReadWrite);
        confScalingTask.setAfterScalingCount(newDataDiskMBpsReadWrite);
        confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
        confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
        confScalingTask.setBegTime(new Date());
        confScalingTask.setVmRole(vmRole.toLowerCase());
        confScalingTask.setGroupName(groupName);
        confScalingTask.setCreateTime(new Date());
        confScalingTask.setInQueue(ConfScalingTask.IN_TAKS_WAIT_QUEUE);
        confScalingTask.setScaleoutTaskId(scaleoutTaskId);
        confScalingTaskNeoMapper.insert(confScalingTask);
        String queueKey = clusterId + ":" + groupName;
        redisLock.trySave(getLogger(), ComposeConstant.compose_cluster_vmrole_list + queueKey, "1");
        return confScalingTask;
    }
}
