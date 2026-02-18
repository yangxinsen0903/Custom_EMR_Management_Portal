package com.sunbox.sdpcompose.service;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import com.sunbox.domain.ResultMsg;

public interface IPlanExecService {

    /**
     * 任务计划执行消息驱动
     * @param message 消息为jsonstring
     *                {
     *                  "activity":"implclass@method",
     *                  "param":jsonobject 上一环节传递
     *                }
     * @return
     */
    ResultMsg composeExecute(String message);


    /**
     * 启动执行计划
     * @param planId
     * @return
     */
    ResultMsg startPlan(String planId);

    /**
     *  获取下一个活动
     * @param planid 计划ID
     * @param currentActivity
     * @return
     */
    InfoClusterOperationPlanActivityLogWithBLOBs getNextActivity(String planid, String currentActivity);

    /**
     * 获取上一个活动
     *
     * @param planid
     * @param currentActivityLogId
     * @return
     */
    InfoClusterOperationPlanActivityLogWithBLOBs getPreviousActivity(String planid,String currentActivityLogId);

    /**
     * 创建集群
     * @param clusterId 集群ID
     * @param realseVersion 发行版本号
     * @param operationName 操作名称
     * @param taskId 扩缩容任务ID
     * @param opTaskid 操作任务ID
     * @return
     */
    ResultMsg createPlanAndRun(String clusterId, String realseVersion, String operationName, String taskId, String opTaskid);

    /**
     * 发送下一个活动的消息
     * @param currentActivityLogId
     * @param jsonMessage
     * @return
     */
    ResultMsg sendNextActivityMsg(String currentActivityLogId, JSONObject jsonMessage);

    /**
     * 发送上一步活动的消息
     *
     * @param previousActivity
     * @param delay
     * @return
     */
    ResultMsg sendPrevActivityMsg(InfoClusterOperationPlanActivityLogWithBLOBs previousActivity, Long delay);


    /**
     * 更新执行计划的状态和执行进度进度
     *
     * @param planId
     * @return
     */
    ResultMsg updatePlanStateAndPercent(String planId);

    ResultMsg updatePlanName(String planId);

   /**
     * 发送下一个带有延时的消息
     * @param currentActivityLogId
     * @param jsonMessage
     * @param second
     * @return
     */
    ResultMsg sendNextActivityDelayMsg(String currentActivityLogId,JSONObject jsonMessage,Long second);


    /**
     * 完成当前活动
     * @param activityLog
     * @return
     */
    ResultMsg complateActivity(InfoClusterOperationPlanActivityLogWithBLOBs activityLog);

    /**
     * 订阅job异步通知消息
     * 包含 创建完成消息，销毁操作完成消息
     * @param message
     * @return
     */
    ResultMsg receiveVmJobMessage(String message);

    /**
     * 根据activityLogId获取
     * @param activityLogId
     * @return
     */
    InfoClusterOperationPlanActivityLogWithBLOBs getInfoActivityLogByLogId(String activityLogId);

    /**
     * 根据activityId获取confCluster
     * @param activityLogId
     * @return
     */
    ConfCluster getConfClusterByActivityLogId(String activityLogId);

    /**
     * 根据planid获取执行计划第一个activity
     * @param planId
     * @return
     */
    InfoClusterOperationPlanActivityLogWithBLOBs getFirstActivity(String planId);


    /**
     * 判断Activity是否 是否超时
     * 超时的任务不再loop
     * @param activityLogId
     * @return
     */
    ResultMsg checkActivityLogTimeout(String activityLogId);


    /**
     * 活动重复,
     * 在此活动中会检查是否超时.
     * @param clientName 发送延时消息的客户端name，
     *                   需要在配置中心的配置
     * @param message 需要传递的消息
     * @param delaySecond 需要延时的second
     * @return
     */
    ResultMsg loopActivity(String clientName,String message,Long delaySecond,String activityLogId);


    /**
     * 重试活动
     * @param activityLogId 活动ID
     * @return
     */
    ResultMsg retryActivity(String activityLogId);

    /**
     * 超时巡检任务，自动重试活动专用
     *
     * @param activityLogId
     * @return
     */
    ResultMsg autoRetryActivityForTimeOut(String activityLogId);
}
