package com.sunbox.service;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.VmRealtimePriceModel;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;

import java.util.Date;
import java.util.List;

/**
 * @author : [niyang]
 * @className : IAzureService
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/28 1:32 PM]
 */
public interface IAzureService {

    //region 元数据接口

    ResultMsg getAzList(String region);

    ResultMsg supportedVMSkuList(String region);

    //查询子网列表
    ResultMsg getSubnet(String region);

    ResultMsg getVmSkus(String region);

    ResultMsg getVmList(String region);

    /**
     * 查询VM列表
     * @param imParams 数据中心,子网ids..
     * @return
     */
    ResultMsg getVmList(JSONObject imParams);

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
    ResultMsg getMIList(String region,String subscriptionId);

    /**
     *  获取用户自定义脚本存储的blob域名
     * @return
     */
    ResultMsg getBolbPath();

    /**
     *  获取keyVault
     * @return
     */
    ResultMsg getKeyVaultList(String region,String subscriptionId);

    /**
     *  SSHKeyPair 查询 根据kvId
     * @return
     */
    ResultMsg getSSHKeyPairById(String kvId,String region,String subscriptionId);


    /**
     * 查询存储帐户列表
     *
     * @return
     */
    ResultMsg getStorageAccountList(String region, String subscriptionId);

    /**
     * 查询日志桶元数据根据id
     *
     * @return
     */
    ResultMsg getLogsBlobContainerListById(String saId, String region,String subscriptionId);

    /**
     * 查询虚拟网络
     *
     * @return
     */
    ResultMsg getNetworkList(String region);

    /**
     * 根据id获取子网列表
     * @param vnetId
     * @return
     */
    ResultMsg getSubnetListById(String vnetId, String region);

    /**
     * 查询数据中心
     * @return
     */
    ResultMsg getRegionList(String subscriptionId);

    ResultMsg listSubscription();


    //endregion

    //region 资源管理

    /**
     *  删除单个VM
     * @param vmName
     * @return
     */
    ResultMsg deleteVMInstance(String clusterName, String vmName, String dnsName, String region);



    /**
     * 查询job的执行状态接口 带接口响应超时
     * @param jobId
     * */
    ResultMsg getJobsStatusWithRequestTimeout(String region, String jobId);


    /**
     * vm申请详细信息
     */
    ResultMsg provisionDetail(String jobId,String region);

    //endregion

    //region SPOT
    /**
     * 查询vmSku价格
     * {
     *   "vmSkuName": "Standard_D4s_v5",
     *   "spotUnitPricePerHourUSD": 0.019429,
     *   "onDemandUnitPricePerHourUSD": 0.192
     * }
     */
    List<JSONObject> getInstancePriceList(List<String> skuNames,String region);

    JSONObject getInstancePrice(String skuName, String region);

    List<JSONObject> getInstancePrice(List<String> skuNames, String region);

    List<AzurePriceHistory> getSkuInstancePrice(List<String> skuNames, String region);

    /**
     * 竞价实例驱逐率
     */
    List<JSONObject> spotEvictionRate(List<String> skuNames,String region);

    /**
     * 竞价实例历史价格
     */
    ResultMsg spotPriceHistory(List<String> skuNames,String region);

    /**
     *
     * @param skuName
     * @return
     */
    ResultMsg<VmRealtimePriceModel> getSpotVmRealtimePrice(String skuName,String region);

    //endregion
    //region

    /**
     * 获取fleet
     * @param region
     * @param clusterName
     * @param groupName
     * @return
     */
    ResultMsg getFleet(String region,String clusterName,String groupName);

    /**
     * 删除fleet
     * @param region
     * @param clusterName
     * @param groupName
     * @return
     */
    ResultMsg delFleet(String region,String clusterName,String groupName);
    //endregion

    ResultMsg deleteVirtualMachines(String deleteVMsRequest);

    /**
     * 缩容时销毁VM使用该接口
     * @param deleteVMsRequest
     * @return
     */
    ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest);
    /**
     * Azure接口日志收集
     */
    void saveAzureApiLogs(String clusterId, String activityLogId, String planId, Date requestTime,
                          String requestParam, String responseBody);


}
