package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.cluster.WorkorderCallbackRequest;
import com.sunbox.sdpadmin.model.shein.request.*;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;


public interface SheinApiService {
    SheinResponseModel descClusterReleaseLabel(String id, String releaseLabel);

    SheinResponseModel listAvailableClusters(String dc);

    SheinResponseModel listAvailableClustersNew(String dc);

    SheinResponseModel listClusters(String stateList, String begtime, String endtime, String dc);

    /**
     * @param releaseLabelPrefix 例如 1.0
     * @param appVersion         例如 3.5.7
     * @param appName            例如 ZOOKEEPER
     */
    SheinResponseModel listClusterReleaseLabels(String releaseLabelPrefix, String appVersion, String appName);

    SheinResponseModel listClusterGroupInstances(String id, String insGpId, String insGpTypes, String insStatus, String dc);

    SheinResponseModel terminateCluster(String dc, String id);

    SheinResponseModel createCluster(String jsonStr);

    SheinResponseModel createClusterNew(String jsonStr);

    SheinResponseModel getVmSkuList(String region);

    SheinResponseModel getSubnetList(String region);

    SheinResponseModel getOsDiskTypeList(String region);

    SheinResponseModel getKeypairList(String region);

    SheinResponseModel getPrimarySecurityGroupList(String region);

    SheinResponseModel getSubSecurityGroupList(String region);

    /**
     * 查询EMR集群详情
     * @param clusterId
     * @param dc
     * */
    SheinResponseModel descCluster(String clusterId, String dc);

    /**
     * 更新资源组标签-全量
     */
    SheinResponseModel updateResourceGroupTags(String azureResourceGroupTagsRequest);

    /**
     * 更新资源组标签-增量
     */
    SheinResponseModel addResourceGroupTags(String azureResourceGroupAddTagsRequest);

    /**
     * 删除资源组标签
     */
    SheinResponseModel deleteResourceGroupTags(String azureResourceGroupAddTagsRequest);

    /**
     * 查询实例组信息
     *
     * @param clusterId
     * @param vmRole
     * @param dc
     * @return
     */
    SheinResponseModel listInstanceGroups(String clusterId, String vmRole, String dc);

    SheinResponseModel listInstanceGroups(String clusterId, String vmRole, String dc, String groupName);

    SheinResponseModel resultMsg2SheinResponseModel(ResultMsg resultMsg);

    /**
     * 手动扩缩容
     *
     * @param param
     * @return
     */
    SheinResponseModel modifyClusterInstanceGroup(Map<String, Object> param, boolean checkInsCnt);

    /**
     * 多实例组手动扩缩容
     */
    SheinResponseModel modifyClusterInstanceGroups(Map<String, Object> param);

    /**
     * 重启大数据服务
     */
    SheinResponseModel restartClusterService(Map<String, Object> param);

    /**
     * 查询重启任务结果
     */
    SheinResponseModel getRestartTaskResult(String clusterId, String taskId);

    /**
     * 更新集群配置
     */
    SheinResponseModel updateClusterConfig(UpdateClusterConfigData updateClusterConfigData);

    /**
     * 执行脚本
     */
    SheinResponseModel saveAndExecuteScript(SaveAndExecuteScriptData scriptRequest);

    /**
     * 集群弹性伸缩规则-附加
     */
    SheinResponseModel saveElasticScalingRule(SheinElasticScalingData sheinElasticScalingData);

    /**
     * 集群弹性伸缩规则-剥离
     */
    SheinResponseModel terminateElasticScalingRule(SheinElasticScalingData sheinElasticScalingData);

    /**
     * 集群新增实例组
     */
    SheinResponseModel addInstanceGroup(SheinInstanceGroupData instanceGroupData);

    /**
     * 查询spot买入逐出统计
     *
     * @param clusterId
     * @param skuName
     * @param endTime yyyy-MM-dd HH:mm:ss
     * @param dc
     * @return
     */
    SheinResponseModel getSpotStatic(String clusterId,String skuName,String endTime,String dc);

    /**
     * 更新弹性规则的实例数量范围
     *
     * @param elasticScalingData
     * @return
     */
    SheinResponseModel updateScaleVmScope(SheinElasticScalingData elasticScalingData);


    /**
     * 更新已经存在的弹性规则
     *
     * @param elasticScalingData
     * @return
     */
    SheinResponseModel updateEsRule(SheinElasticScalingRuleData elasticScalingData);

    /**
     * 新增弹性规则
     *
     * @param elasticScalingData
     * @return
     */
    SheinResponseModel addEsRule(SheinElasticScalingAddRuleData elasticScalingData);

    /**
     * 获取指定实例组正在运行和队列中的任务
     *
     * @param clusterId
     * @param groupName
     * @return
     */
    SheinResponseModel getPendingSaleTask(String clusterId,String groupName);

    /**
     * 获取Token
     * @param time
     * @param ak
     * @param sign
     * @return
     */
    SheinResponseModel getToken(String time,String ak,String sign);

    /**
     * 同步外部回调 .包含:创建, 销毁
     * @param workorderCallbackRequest
     * @return
     */
    SheinResponseModel workOrderCallback(WorkorderCallbackRequest workorderCallbackRequest);

    /**
     * pv2磁盘调整
     * @param diskPerformance
     * @return
     */
    SheinResponseModel pv2DiskInfo(SheinDiskPerformance diskPerformance);

    /**
     * 获取VM实例详情信息
     * @param vmInstanceRequest
     * @return
     */
    SheinResponseModel vmInstanceDetail(VmInstanceDetailRequest vmInstanceRequest);
    /**
     * 获取集群中VM实例列表
     * @param vmInstanceRequest
     * @return
     */
    SheinResponseModel vmInstancesByClusterId(VmInstanceDetailRequest vmInstanceRequest);

    /**
     * 管控全托管弹性扩缩容
     * @param request
     * @return
     */
    SheinResponseModel fullCustodyControl(FullCustodyRequest request);
}
