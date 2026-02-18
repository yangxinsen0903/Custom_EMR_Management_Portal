package com.sunbox.sdpadmin.service;

import com.sunbox.domain.*;
import com.sunbox.domain.ambari.DiskPerformanceRequest;
import com.sunbox.sdpadmin.model.admin.request.ClusterCancelScalingTaskRequest;
import com.sunbox.sdpadmin.model.admin.request.ClusterScaleOutOrScaleInRequest;
import com.sunbox.sdpadmin.model.admin.request.ClusterScalingLogData;
import com.sunbox.sdpadmin.model.admin.request.ConfGroupElasticScalingData;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AdminApiService {

    ResultMsg clusterOverview();

    ResultMsg queryClusterList(String jsonStr);

    ResultMsg getReleases();

    ResultMsg getReleaseApps(String jsonStr);

    ResultMsg getSceneApps(String jsonStr);

    ResultMsg getReleaseConfigList(String jsonStr);

    ResultMsg getAzureDiskSkuList(String region);

    ResultMsg getClassificationList(String releaseVersion);

    ResultMsg getNetWorkList();

    ResultMsg getPrimarySecurityGroupList(String region);

    ResultMsg getSubSecurityGroupList(String region);

    ResultMsg getKeyPairList(String region);

    ResultMsg createCluster(String jsonStr, String userName);

    ResultMsg getCluster(String jsonStr);

    ResultMsg getTagKeyList();

    ResultMsg checkConnect(String jsonStr, HttpServletResponse httpServletResponse);

    ResultMsg checkClusterName(String jsonStr);

    ResultMsg geVmSkus(String region);

    ResultMsg getMIList(String region,String subscriptionId);

    ResultMsg getTagValueList(String jsonStr);

    ResultMsg getServicelist(HttpServletRequest request);

    ResultMsg getSystemlist(HttpServletRequest request);

    ResultMsg getJobDetail(String jsonStr);

    ResultMsg getJobList(String jsonStr);

    ResultMsg getJobListNew(String jsonStr);

    /**
     * 删除集群
     * @param jsonStr
     * @param userName 正确的userName
     * @return
     */
    ResultMsg deleteCluster(String jsonStr, String userName);

    ResultMsg checkCustomScriptUri(String customScriptUri);

    /**
     * ambari 查询状态
     *
     * @param activityLogId
     */
    ResultMsg getAmbariStatus(String activityLogId);

    /**
     * ansible 查询状态
     *
     * @param activityLogId
     */
    ResultMsg getAnsibleStatus(String activityLogId);

    /**
     * 查询集群数据
     *
     * @param clusterId 集群id
     * @return 集群创建时，输入的json结构
     */
    ResultMsg queryClusterInfo(String clusterId);

    /**
     * 查询集群数据
     *
     * @param clusterId 集群id
     * @param fetchScalingRules 是否获取弹性伸缩规则
     * @return 集群创建时，输入的json结构
     */
    ResultMsg queryClusterInfo(String clusterId, boolean fetchScalingRules);

    /**
     * 查Ambari数据库名是否手动设置
     *
     * @return true: 手动设置   false: 自动生成
     */
    boolean queryAmbariDbNameManual();

    ResultMsg queryBaseScript(String jsonStr);

    ResultMsg getBlobContent(String filePath);

    /**
     * 创建一个脚本
     *
     * @param script
     * @return
     */
    ResultMsg createBaseScript(BaseScript script, MultipartFile file);

    String getClusterBlueprint(String clusterId);

    ResultMsg createResourceGroup(String azureResourceGroupTagsRequest);

    ResultMsg getResourceGroup(String clusterId);

    ResultMsg deleteResourceGroup(String clusterId);

    ResultMsg updateResourceGroupTags(String azureResourceGroupTagsRequest);

    ResultMsg addResourceGroupTags(String azureResourceGroupTagsRequest);

    ResultMsg deleteResourceGroupTags(String azureResourceGroupAddTagsRequest);

    ResultMsg supportedVMSkuList(String region);

    /**
     * 集群实例扩容
     *
     * @param request
     * @return
     */
    ResultMsg clusterScaleOut(ClusterScaleOutOrScaleInRequest request);

    /**
     * 集群实例缩容
     *
     * @param request
     * @return
     */
    ResultMsg clusterScaleIn(ClusterScaleOutOrScaleInRequest request);

    /**
     * 取消集群的扩缩容任务
     * @param request
     * @return
     */
    ResultMsg clusterCancelScalingTask(ClusterCancelScalingTaskRequest request);

    /**
     * 弹性伸缩记录
     *
     * @param request
     * @return
     */
    ResultMsg clusterScalingLog(ClusterScalingLogData request);

    /**
     * 获取弹性伸缩规则
     *
     * @param request
     * @return
     */
    ResultMsg getElasticScalingRule(ConfGroupElasticScalingData request);

    /**
     * 修改实例组弹性伸缩配置
     *
     * @param request
     * @return
     */
    ResultMsg updateGroupElasticScaling(ConfGroupElasticScalingData request);

    /**
     * 更新全托管弹性扩缩容参数
     * @param request
     * @return
     */
    ResultMsg updateGroupESFullCustodyParam(ConfGroupElasticScalingData request);

    /**
     * 添加弹性伸缩规则
     *
     * @param request
     * @return
     */
    ResultMsg postElasticScalingRule(ConfGroupElasticScalingData request);

    /**
     * 修改弹性伸缩规则（全量参数）
     *
     * @param request
     * @return
     */
    ResultMsg updateElasticScalingRule(ConfGroupElasticScalingData request);

    /**
     * 启停用弹性伸缩规则
     */
    ResultMsg updateElasticScalingRuleValid(String esRuleId, Integer isValid);

    /**
     * 删除弹性伸缩规则
     *
     * @param request
     * @return
     */
    ResultMsg deleteElasticScalingRule(ConfGroupElasticScalingData request);

    ResultMsg createVMInstance(String azureVMInstanceRequest);

    ResultMsg deleteVMInstance(String vmName);

    ResultMsg updateVirtualMachinesDiskSize(String azureUpdateVirtualMachinesDiskSizeRequest);

    /**
     * 获取实例组
     *
     * @param clusterId
     * @return
     */
    ResultMsg getVMGroupsByClusterId(String clusterId);

    /**
     * 获取AZ列表
     */
    ResultMsg getAzList(String region);

    /**
     * 节点信息概览
     *
     * @param param
     * @return
     */
    ResultMsg getVmOverview(Map param);

    /**
     * 获取实例列表-带参数 分页
     *
     * @param param groupName,
     *              vmName
     *              state
     * @return
     */
    ResultMsg getVMlistByClusterId(Map param);

    ResultMsg queryScriptJobList(String scriptJobListRequest);

    ResultMsg getLogsBlobContainerList();

    /**
     * 获取执行计划信息
     *
     * @param planId
     * @return
     */
    ResultMsg getPlanInfoByPlanId(String planId);

    /**
     * 获取扩缩容任务信息
     *
     * @param taskId
     * @return
     */
    ResultMsg getScaleTaskInfoByTaskId(String taskId);


    ResultMsg deleteGroup(String clusterId, String groupName, String vmRole);

    /**
     * 新增实例组接口
     *
     * @param jsonStr
     * @return
     */
    ResultMsg addGroup(String jsonStr);

    ResultMsg growPart(String jsonStr);

    ResultMsg sdpVersionInfo();

    ResultMsg getTaskInfo(String taskId);

    ResultMsg getVmListByPlanId(String planId);

    ResultMsg deleteScaleOutTaskVms(String taskId);

    ResultMsg getScaleCountInQueue(ConfScalingTask scalingTask);

    /**
     * 竞价实例历史价格
     */
    ResultMsg spotPriceHistory(Map<String, Object> param);

    /**
     * 查询Spot的价格与驱逐率历史记录.
     * @param region 地区(数据中心)
     * @param skuName 需要查询的主机 Sku名称
     * @param periodDays 查询几天的时间段,默认为7天
     * @return
     */
    List<AzurePriceHistory> spotPriceAndEvictionRateHistory(String region, String skuName, Integer periodDays);

    ResultMsg getJobQueryParamDict();


    /**
     * 处理历史没有任务名称的计划
     *
     * @return
     */
    ResultMsg processWithOutPlanName();

    /**
     * 处理存量VM的VMID
     * @return
     */
    ResultMsg processWithOutVmId( String region);


    /**
     * 获取集群的可用镜像
     * @param clusterId 集群ID
     * @return
     */
    ResultMsg getAvailableImage(String clusterId);

    /**
     * 查询失败日志By条件
     *
     * @param param
     * @return
     */
    ResultMsg getFailedLogsByParam(Map param);

    /**
     * 查询失败日志详情
     *
     * @param Id
     * @return
     */
    ResultMsg getFailedLogById(Long Id);

    /**
     * 运维功能-删除锁
     *
     * @param keyName
     * @return
     */
    ResultMsg opsDelLockKey(String keyName);

    /**
     * 运维功能-补发第一条消息
     * @param planId
     * @return
     */
    ResultMsg opsSendFirstMessage(String planId);

    /**
     * 获取全部的Stack Version
     * @return
     */
    ResultMsg getStackVersions();
    /**
     * 集群并行或串行扩缩容
     */
    int updateClusterParallel(String clusterId, Integer isParallelScale);

    /**
     * 更新集群中销毁白名单的状态
     * @return
     */
    int updateDestroyStatus(String clusterId, Integer isWhiteAddr);

    /**
     * 更新PV2数据盘IOPS和MBPS
     * @param request
     * @return
     */
    ResultMsg updateDiskIOPSAndThroughput(DiskPerformanceRequest request);


}
