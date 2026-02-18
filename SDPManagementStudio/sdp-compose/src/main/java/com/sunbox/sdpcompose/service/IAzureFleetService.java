package com.sunbox.sdpcompose.service;

import com.sunbox.domain.*;
import com.sunbox.domain.metaData.AvailabilityZone;
import com.sunbox.sdpcompose.model.azure.fleet.request.*;
import com.sunbox.sdpcompose.model.azure.fleet.response.AzureFleetInfo;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 调用基于AzureFleet实现的资源管理接口
 */
public interface IAzureFleetService {

    //region azure 接口
    /**
     * 调用Azure fleet 创建VM接口
     * @param azureVmsRequest
     * @return
     */
    ResultMsg createVms(AzureFleetVmsRequest azureVmsRequest,String subscriptionId);

    /**
     * 查询AzureFleet资源管理任务的状态
     * @param jobId
     * @param subscriptionId 订阅Id
     * @return
     */
    ResultMsg getJobsStatusWithRequestTimeout(String jobId,String subscriptionId);

    /**
     * 查询资源申请的详细信息
     * @param jobId
     * @param subscriptionId
     * @return
     */
    ResultMsg provisionDetail(String jobId,String subscriptionId);

    /**
     * 批量删除VM
     * @param deleteVMsRequest
     * @param subscriptionId
     * @return
     */
     ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest, String subscriptionId);

    /**
     * 删除集群
     * @param clusterName
     * @param subscriptionId
     * @return
     */
     ResultMsg deleteCluster(String clusterName,String subscriptionId);


    /**
     * 查询多个vmSku价格
     * {
     *   "vmSkuName": "Standard_D4s_v5",
     *   "spotUnitPricePerHourUSD": 0.019429,
     *   "onDemandUnitPricePerHourUSD": 0.192
     * }
     */
    ResultMsg getInstancePrice(List<String> skuNames,String region,String subscriptionId);

    /**
     * 查询单个VmSku价格
     * @param skuName
     * @param region
     * @param subscriptionId
     * @return
     */
    ResultMsg getInstancePrice(String skuName,String region,String subscriptionId);

    /**
     *  扩容创建虚拟机
     */
    ResultMsg createAppendVms(AzureFleetAppendVMsRequest azureVmsRequest,String subscriptionId);
    /**
     *  标准价格
     */
    BigDecimal getOndemondPrice(String skuName, String region, String subscriptionId);

    //endregion


    //region 构造请求参数

    /**
     * 构造spot profile
     * @param confCluster 集群信息
     * @param confClusterVm 集群实例组信息
     * @return
     */
    ResultMsg buildAzureSpotProfile (ConfCluster confCluster, ConfClusterVm confClusterVm);

    /**
     * 构造 base Profile
     * @param confCluster
     * @param confClusterVm
     * @return
     */
    ResultMsg buildAzureBaseProfile(ConfCluster confCluster,ConfClusterVm confClusterVm);

    /**
     * 构造 regular Profile
     * @param confCluster
     * @param confClusterVm
     * @return
     */
    ResultMsg buildAzureRegularProfile(ConfCluster confCluster,ConfClusterVm confClusterVm);

    /**
     * 构建vmSizesProfile
     * @param confCluster
     * @param confClusterVm
     * @param useMultiSku
     * @return
     */
    List<VMSizesProfile> buildVMSizesProfile(ConfCluster confCluster, ConfClusterVm confClusterVm, boolean useMultiSku);

    /**
     * 构建扩容请求报文
     * @param task
     * @param confCluster
     * @param beginIndex
     * @param infoClusterVmJob
     * @param confScalingTaskVms
     * @return
     */
    AzureFleetAppendVMsRequest buildAzureFleetAppendVMsRequest(ConfScalingTask task,
                                                               ConfCluster confCluster,
                                                               Integer beginIndex,
                                                               InfoClusterVmJob infoClusterVmJob,
                                                               List<ConfScalingTaskVm> confScalingTaskVms);

    /**
     * 构建虚拟机tags
     * @return
     */
    void buildVmsTags(Map<String, String> vmtagmap, ConfCluster confCluster, ConfClusterVm confClusterVm, AvailabilityZone zone, SpotProfile azureSpotProfile);
    //endregion

    /**
     * 从Azure查询AzureFleet详情
     * @param confCluster
     * @param groupName
     * @return
     */
    ResultMsg<AzureFleetInfo> getAzureFleetInfo(ConfCluster confCluster, String groupName);

    /**
     * 删除一个AzureFleet
     * @param confCluster
     * @param groupName
     */
    ResultMsg deleteAzureFleet(ConfCluster confCluster, String groupName);
}
