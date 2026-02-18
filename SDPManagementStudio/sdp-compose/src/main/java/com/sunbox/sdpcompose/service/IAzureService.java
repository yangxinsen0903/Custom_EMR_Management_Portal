package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.azure.VMDiskRequest;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.sdpcompose.model.azure.request.*;

import java.util.Date;
import java.util.List;

public interface IAzureService {
    ResultMsg getSubnet(String region);

    ResultMsg geVmSkus(String region);

    ResultMsg getDiskSku(String region);

    /**
     * nsg sku查询
     * */
    ResultMsg getNSGSku(String region);

    /**
     * SSHKeyPair 查询
     * */
    ResultMsg getSSHKeyPair(String region);

    /**
     * 查询MI列表
     * @return
     */
    ResultMsg getMIList(String region);

    ResultMsg createVms(AzureVmsRequest azureVmsRequest);

    /**
     * 查询job的执行状态接口
     * @param jobId
     * */
    ResultMsg getJobsStatus(String jobId,String subscriptionId);

    /**
     * 查询job的执行状态接口 带接口响应超时
     * @param jobId
     * */
    ResultMsg getJobsStatusWithRequestTimeout(String jobId,String region);

    ResultMsg executeJobPlaybook(AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest);

    /**
     * 查询Playbook执行状态/结果
     * GET /execute-job/{jobId}/state
     * @param transactionId
     * @return processing=执行中 success=执行成功 fail=执行失败
     */
    ResultMsg queryPlaybookExecuteResult(String transactionId,String subscriptionId,String keyVaultResourceName,String secretResourceId);

    ResultMsg ruinCluster(String clusterName,String subscriptionId);

    ResultMsg createResourceGroup(String azureResourceGroupTagsRequest);

    ResultMsg createResourceGroup(AzureResourceGroupTagsRequest azureResourceGroupTagsRequest);

    ResultMsg getResourceGroup(String clusterId,String region);

    ResultMsg updateResourceGroupTags(String azureResourceGroupTagsRequest);

    ResultMsg updateResourceGroupTags(AzureResourceGroupTagsRequest azureResourceGroupTagsRequest);

    ResultMsg addResourceGroupTags(String azureResourceGroupAddTagsRequest);

    ResultMsg addResourceGroupTags(AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest);

    ResultMsg deleteResourceGroupTags(String azureResourceGroupAddTagsRequest);

    ResultMsg deleteResourceGroupTags(AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest);

    ResultMsg supportedVMSkuList(String region);

    ResultMsg createVMInstance(String azureVMInstanceRequest);

    ResultMsg createVMInstance(AzureVMInstanceRequest azureVMInstanceRequest);

    ResultMsg deleteVMInstance(String vmName, String dnsName, String region);

    ResultMsg appendVirtualMachines(String appendVMsRequest);

    /**
     * 扩容时申请VM接口
     * @param appendVMsRequest
     * @return
     */
    ResultMsg appendVirtualMachines(AzureAppendVMsRequest appendVMsRequest);

    /**
     * 使用com.sunbox.service.IAzureService中的方法
     * @param deleteVMsRequest
     * @return
     */
    @Deprecated
    ResultMsg deleteVirtualMachines(String deleteVMsRequest);

    /**
     * 缩容时销毁VM使用该接口,使用com.sunbox.service.IAzureService中的方法
     * @param deleteVMsRequest
     * @return
     */
    @Deprecated
    ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest);

    ResultMsg updateVirtualMachinesDiskSize(String azureUpdateVirtualMachinesDiskSizeRequest);

    ResultMsg updateVirtualMachinesDiskSize(AzureUpdateVirtualMachinesDiskSizeRequest request);

    /**
     * 查询日志桶元数据
     */
    ResultMsg getLogsBlobContainerList(String region);

    ResultMsg getAzList(String region);

    /**
     * 查询vmSku价格
     * {
     *   "vmSkuName": "Standard_D4s_v5",
     *   "spotUnitPricePerHourUSD": 0.019429,
     *   "onDemandUnitPricePerHourUSD": 0.192
     * }
     */
    ResultMsg getInstancePrice(String skuName,String region);

    /**
     * 竞价实例驱逐率
     */
    ResultMsg spotEvictionRate(List<String> skuNames,String region);

    /**
     * 竞价实例历史价格
     */
    ResultMsg spotPriceHistory(List<String> skuNames,String region);

    /**
     * vm申请详细信息
     */
    ResultMsg provisionDetail(String jobId,String region);

    /**
     * 磁盘扩容
     *
     * @param addPartRequest
     * @return
     */
    ResultMsg addPart(AzureAddPartRequest addPartRequest);

    /**
     * 磁盘扩容
     */
    ResultMsg updateVMsDiskSize(AzureAddPartRequest reqStr);

    ResultMsg getSpotVmRealtimePrice(String skuName,String region);

    void saveAzureApiLogs(String clusterId, String activityLogId, String planId, Date requestTime,
                          String requestParam, String responseBody);

    /**
     * 更新PV2数据盘IOPS和MBPS
     * @param VMDiskRequest
     * @return
     */
    ResultMsg updateVMsDiskIOPSAndMbps(VMDiskRequest VMDiskRequest);
}
