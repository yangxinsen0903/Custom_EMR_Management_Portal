package com.sunbox.service;

import com.sunbox.domain.ConfScalingTask;

public interface IConfScalingTaskService {
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
    ConfScalingTask savePv2DiskScalingTask(String clusterId,
                                           String groupName,
                                           String vmRole,
                                           Integer newDataDiskIOPSReadWrite,
                                           Integer newDataDiskMBpsReadWrite,
                                           String scaleoutTaskId);
}
