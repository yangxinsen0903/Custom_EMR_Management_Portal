package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ConfScalingTask;
import com.sunbox.domain.ResultMsg;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 扩容操作
 */
public interface IScalingService {

    /**
     * 扩容申请虚拟机
     *
     * @param message
     * @return
     */
    ResultMsg createVms(String message);


    /**
     * 创建缩容任务
     *
     * @param task
     * @return
     */
    ResultMsg createScaleinTask(ConfScalingTask task);

    /**
     * 指定机器缩容（仅支持core和task角色的实例）
     * @param clusterId 集群ID
     * @param vmnames 集群实例名称vmname列表
     * @return 缩容任务的任务ID
     */
    ResultMsg createScaleInTaskForDeleteTaskVm(String clusterId,
                                               String vmrole,
                                               String groupname,
                                               String scaleOutTaskId,
                                               List<String> vmnames,
                                               Date createdTime);

    /**
     * 创建扩容任务
     *
     * @param task
     * @return
     */
    ResultMsg createScaleOutTask(ConfScalingTask task);

    /**
     * shein接口resize
     *
     * @param param
     * @return
     */
    ResultMsg resizeClusterGroup(Map<String, Object> param);

    /**
     *  调整缩容数量
     * @param scalingTask
     * @return
     */
    ResultMsg adjustScaleInCount(ConfScalingTask scalingTask);

    /**
     *  调整扩容数量
     * @param scalingTask
     * @return
     */
    ResultMsg adjustScaleOutCount(ConfScalingTask scalingTask);

    /**
     * 因需要删除虚拟机，所以产生缩容
     * @return
     */
    ResultMsg scaleInForDeleteTaskVm(String clusterId,
                                     String groupId,
                                     String scaleOutTaskId,
                                     List<String> vmNames,
                                     Date createdTime);

    /**
     * 竞价实例组扩容
     * @return
     */
    ResultMsg spotInsGroupScaleOut(String spotTaskId,
                                   String clusterId,
                                   String groupId,
                                   Integer expectCount,
                                   Integer scaleCount);

    /**
     * 竞价实例组逐出缩容
     * @return
     */
    ResultMsg scaleInGroupForSpot(String spotTaskId,
                                  String clusterId,
                                  String groupId,
                                  Integer expectCount,
                                  List<String> vmNames);

    ResultMsg createScalePartOutTask(ConfScalingTask scalingTask);
}

