package com.sunbox.sdpcompose.service;


import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmJob;
import com.sunbox.domain.ResultMsg;

import java.util.List;

public interface IVMService {

    /**
     * 创建虚拟机
     * @param messageParam
     * @return
     */
    ResultMsg createVms(String messageParam);

    /**
     *  查询虚拟机创建状态
     * @param messageParam
     * @return
     */
    ResultMsg queryVmsCreateJob(String messageParam);

    /**
     *  查询虚拟机扩容任务状态
     * @param messageParam
     * @return
     */
    ResultMsg queryVmsAppendJob(String messageParam);

    /**
     *  查询虚拟机缩容任务vm删除任务状态
     * @param messageParam
     * @return
     */
    ResultMsg queryScaleInVmsDeleteJob(String messageParam);


    /**
     *  查询虚拟机清理任务vm删除任务状态
     * @param messageParam
     * @return
     */
    ResultMsg queryClearVmsDeleteJob(String messageParam);

    /**
     * 删除虚拟机
     * @param messageparam
     * @return
     */
    ResultMsg deleteVms(String messageparam);

    /**
     * 查询删除虚拟机状态
     * @param messageparam
     * @return
     */
    ResultMsg queryDeleteVms(String messageparam);

    /**
     *  保存虚拟机信息
     * @param message 查询接口返回的数据或 订阅servicebus得到的消息数据
     * @return
     */
    ResultMsg saveClusterVmInfo(String message);

    /**
     * 保存扩容虚拟机信息
     * @param message
     * @return
     */
    ResultMsg saveClusterAppendVminfo(String message);


    /**
     *
     * @param activityLogId
     * @param cvmjobId
     * @param message
     * @return
     */
    ResultMsg commonSaveVMInfo(String activityLogId, String cvmjobId, JSONObject message);

    /**
     * 根据实例角色获取集群机器
     * @param clusterId
     * @return
     */
    List<InfoClusterVm> getRoleVms(String clusterId,String role);

    /**
     * 根据实例组获取集群机器(运行中）
     * @param clusterId
     * @return
     */
    List<InfoClusterVm> getGroupVms(String clusterId,String groupName);

    /**
     * 根据实例组获取集群机器(运行中）
     * @param clusterId
     * @return
     */
    List<InfoClusterVm> getGroupVms(String clusterId, String groupName, String vmSkuName);

    /**
     * 根据实例角色和实例运行状态获取集群机器
     * @param clusterId
     * @param role
     * @param state
     * @return
     */
    List<InfoClusterVm> getRoleVmsByState(String clusterId,String role,Integer state);

    /**
     * 根据实例组名称和实例运行状态获取集群机器
     * @param clusterId
     * @param groupName
     * @param state
     * @return
     */
    List<InfoClusterVm> getGroupVmsByState(String clusterId,String groupName,Integer state);

    /**
     * 获取集群所有机器
     * @param clusterId
     * @return
     */
    List<InfoClusterVm> getAllVms(String clusterId);

    /**
     * 获取集群扩容新增的机器
     * @param clusterId 集群ID
     * @param taskId 扩容任务ID
     * @return
     */
    List<InfoClusterVm> getScaleOutVms(String clusterId,String taskId);

    /**
     * 获取集群需要缩容的机器
     * @param clusterId 集群ID
     * @param taskId 缩容任务ID
     * @return
     */
    List<InfoClusterVm> getScaleInVms(String clusterId,String taskId);

    /**
     * 获取vmjob
     * @param clusterId
     * @param clusterName
     * @param operrationType
     * @param activityLogId
     * @return
     */
    InfoClusterVmJob getVmJob(String clusterId,
                              String clusterName,
                              String operrationType,
                              String activityLogId);

    /**
     * 缩容删除VMs
     * @param message
     * @return
     */
    ResultMsg deleteVmsForScaleIn(String message);

    /**
     * 清理创建集群或扩容过程中，异常的vm
     *
     * @param message
     * @return
     */
    ResultMsg deleteVmsForClearVM(String message);

    /**
     * 磁盘扩容
     * @param message
     * @return
     */
    ResultMsg ambariAddPart(String message);
    /**
     * 查询磁盘扩容状态
     * @param mesasge
     * @return
     */
    ResultMsg queryAddPartStatus(String mesasge);

    /**
     * 根据集群ID 虚拟机内网 扩容任务ID 获取虚拟机信息
     * @param clusterId
     * @param vmIps
     * @param scaleOutTaskId
     * @return
     */
    List<InfoClusterVm> getVMListByVMIps(String clusterId,List<String> vmIps,String scaleOutTaskId);

    /**
     * 根据集群ID和vm hostname 获取虚拟机信息
     * @param clusterId
     * @param hostNames
     * @return
     */
    List<InfoClusterVm> getVMListByHostNames(String clusterId,List<String> hostNames);

    /**
     * 保存剔除虚拟机信息
     *  -- 保存infoclusterreject表数据信息+更新infoclustervm state 状态为unknown -99
     * @param vms
     * @return
     */
    ResultMsg saveRejectVMs(List<InfoClusterVm> vms,String activityLogId);

    /**
     * ansible任务剔除节点
     * @param clusterId
     * @param vmIps
     * @param currentActivityLog
     * @return
     */
    ResultMsg ansibleJobRejectNode(String clusterId,
                                   List<String> vmIps,
                                   InfoClusterOperationPlanActivityLogWithBLOBs currentActivityLog);

    /**
     * ambari任务剔除节点
     * @param clusterId
     * @param vmHostNames
     * @param currentActivityLog
     * @return
     */
    ResultMsg ambariJobRejectNode(String clusterId,
                                  List<String> vmHostNames,
                                  InfoClusterOperationPlanActivityLogWithBLOBs currentActivityLog);

    /**
     * 判断是否可以剔除节点
     * 剔除节点包含ambari master角色节点 false
     * 剔除后core节点数量低于3个  false
     * @param clusterId
     * @param rejectVms
     * @return
     */
    ResultMsg checkRejectNode(String clusterId,List<InfoClusterVm> rejectVms);

    /**
     * 判断是否启动清理VM
     *
     * @param planId 计划ID
     * @return
     */
    ResultMsg checkIsStartClearVM(String planId);

    /**
     * 启动清理VM计划
     * -- 清理因异常被踢出集群的VM
     *
     * @param planId
     * @return
     */
    ResultMsg startClearVMPlan(String clusterId,String planId);


    /**
     * Spot实例因逐出事件更新InfoClusterVM状态为UNKONOWN
     *
     * @param clusterId
     * @param hostName
     * @return
     */
    ResultMsg updateInfoClusterVMStateForSpotEvictionEvent(String clusterId,String hostName);

    /**
     * 初始化自动补齐的驱逐VM
     * @param messageParam 参数
     * @return
     */
    ResultMsg initEvictVms(String messageParam);

    /**
     * 请求pv2磁盘性能调整
     * @param messageParam
     * @return
     */
    ResultMsg diskPerformanceAdjust(String messageParam);

    /**
     * 查询pv2磁盘调整性能结果
     * @param messageParam
     * @return
     */
    ResultMsg queryDiskPerformanceAdjust(String messageParam);
}
