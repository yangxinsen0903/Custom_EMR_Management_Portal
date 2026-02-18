/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.vmEvent.VmEventRequest;

import java.util.List;

/**
 * VM事件Service
 * @author wangda
 * @date 2024/7/15
 */
public interface IVmEventService {
    /**
     * 批量保存VM
     * @param vms
     * @param isOnline 是否上线, true: VM上线  false: Vm下线
     */
    void saveVmEvents(List<InfoClusterVm> vms, boolean isOnline);

    /**
     * 保存扩容集群的上线事件
     * @param clusterId
     * @param scaleOutTaskId
     */
    void saveVmEventsForScaleOutTask(String clusterId, String scaleOutTaskId);

    /**
     * 保存创建集群的上线事件
     * @param clusterId
     */
    void saveVmEventsForCreateCluster(String clusterId);

    /**
     * 保存销毁集群的上线事件
     * @param clusterId
     */
    void saveVmEventsForDeleteCluster(String clusterId);

    /**
     * 保存缩容集群的下线事件
     * @param clusterId
     * @param scaleInTaskId
     */
    void saveVmEventsForScaleInTask(String clusterId, String scaleInTaskId);

    ResultMsg getVmEventList(VmEventRequest vmEventRequest);
}
