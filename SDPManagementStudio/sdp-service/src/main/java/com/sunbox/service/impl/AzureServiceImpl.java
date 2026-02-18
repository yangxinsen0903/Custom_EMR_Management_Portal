package com.sunbox.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.domain.azure.AzureResponse;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.INeoFullLogService;
import com.sunbox.service.IThirdApiFailedLogService;
import com.sunbox.service.consts.AzureConstant;
import com.sunbox.service.consts.ComposeConstant;
import com.sunbox.util.HttpClientUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : AzureServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/28 1:32 PM]
 */
@Service("azureApiService")
public class AzureServiceImpl implements IAzureService, BaseCommonInterFace {

    @Value("${rmapi.request.url}")
    private String azureUrl;

    @Value("${sdp.scripts.blobpath}")
    private String blobpath;

    @Value("${rmapi.metas.url:/api/v1/metas}")
    private String metasUri;

    @Value("${sdp.api.azure.delete1vm.timeout:60}")
    private Integer sdpApiAzureDelete1VmTimeOut;

    @Value("${sdp.api.azure.queryjob.timeout:80}")
    private Integer sdpApiAzureQueryJobTimeOut;

    @Value("${sdp.api.azure.provisiondetail.timeout:120}")
    private Integer sdpApiAzureProvisionDetailTimeOut;

    @Value("${sdp.api.azure.deletevms.timeout:600}")
    private Integer sdpApiAzureDeleteVmTimeOut;

    @Autowired
    private IThirdApiFailedLogService thirdApiFailedLogService;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    @Autowired
    private INeoFullLogService fullLogService;


//    @Autowired
//    private ThreadLocal<Map<String, Object>> threadLocal;

    //region 元数据接口

    @Override
    public ResultMsg getAzList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用 azure 接口：/api/v1/metas/supportedAvailabilityZoneList（GET）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + metasUri+"/supportedAvailabilityZoneList/"+region,headerMap);
        getLogger().info("AzureServiceImpl.getAzList , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("getAzList error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getAzList.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        Object data = jsonObject.get("data");
        msg.setData(data);
        return msg;
    }

    @Override
    public ResultMsg supportedVMSkuList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用 azure 接口：/api/v1/metas/supportedVMSkuList（GET）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.supportedVMSkuList,begin");
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + metasUri+"/supportedVMSkuList/"+region,headerMap);
        getLogger().info("AzureServiceImpl.supportedVMSkuList , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("supportedVMSkuList error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.supportedVMSkuList.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg getSubnet(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用azure接口 /api/v1/metas/supportedSubnetList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_SUBNET_SKU+"/"+region,headerMap);
        getLogger().info("查询子网列表, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        //返回数据中，没有status或者code进行标识请求的成功或者失败，所以通过返回http状态码和报文来判断
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_SUBNET_SKU);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg getVmSkus(String region) {
        // 调用azure接口/vm/skus
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_VM_SKU+"/"+region,headerMap);
        getLogger().info("查询VM, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_VM_SKU);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg getVmList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/vms/listAll
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        Map<String, String> param=new HashMap<>();
        param.put("region", region);
        String jsonStr = HttpClientUtil.doPost(azureUrl + "/api/v1/vms/listAll/",JSON.toJSONString(param),headerMap);
        getLogger().info("查询VMlist, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }

        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setData(JSONObject.parseObject(jsonStr));
        return msg;
    }

    @Override
    public ResultMsg getVmList(JSONObject imParams) {
        Assert.notEmpty(imParams, "Azure imParams不能为空");
        //校验
        boolean region1 = imParams.containsKey("region");
        boolean subNetIds= imParams.containsKey("subNetIds");
        Assert.isTrue(region1&&subNetIds,"Azure imParams参数错误,不能为空");
        //重新构建 JSONObject
        JSONObject imparamJson = new JSONObject();
        imparamJson.put("region",imParams.getString("region"));
        imparamJson.put("subNetIds",imParams.getJSONArray("subNetIds"));
        String region =imParams.getString("region");
        // /api/v1/vms/listAll
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doPost(azureUrl + "/api/v1/vms/listAll/",imparamJson.toJSONString(),headerMap);
        getLogger().info("query VMlist, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }

        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setData(JSONObject.parseObject(jsonStr));
        return msg;
    }

    @Override
    public ResultMsg getDiskSku(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //调用azure接口 /api/v1/metas/supportedDiskSkuList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_DISK_SKU+"/"+region,headerMap);
        getLogger().info("查询磁盘Sku列表, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_DISK_SKU);
        msg.setResult(true);
        return msg;
    }

    /**
     * nsg sku查询
     */
    @Override
    public ResultMsg getNSGSku(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedNSGSkuList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_NSG_SKU+"/"+region,headerMap);
        getLogger().info("NSGSku查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_NSG_SKU);
        msg.setResult(true);
        return msg;
    }

    /**
     * SSHKeyPair 查询
     */
    @Override
    public ResultMsg getSSHKeyPair(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_SSH_KEY_PAIR_SKU+"/"+region,headerMap);
        getLogger().info("ssh key pair 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_SSH_KEY_PAIR_SKU);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询MI列表
     *
     * @return
     */
    @Override
    public ResultMsg getMIList(String region,String subscriptionId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedManagedIdentityList
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_MANAGED_IDENTITY+"/"+region,headerMap);
        getLogger().info("MI(Managed Identity) 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_MANAGED_IDENTITY);
        msg.setResult(true);
        return msg;
    }

    /**
     * 获取用户自定义脚本存储的blob域名
     *
     * @return
     */
    @Override
    public ResultMsg getBolbPath() {
        ResultMsg msg = new ResultMsg();
        msg.setData(blobpath);
        msg.setResult(true);
        return msg;
    }

    /**
     *  获取keyVault
     * @return
     */
    @Override
    public ResultMsg getKeyVaultList(String region,String subscriptionId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedKVList
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_KEY_VAULT+"/"+region,headerMap);
        getLogger().info("keyVault 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_KEY_VAULT);
        msg.setResult(true);
        return msg;
    }

    /**
     * SSHKeyPair 查询 根据资源id
     */
    @Override
    public ResultMsg getSSHKeyPairById(String kvId, String region,String subscriptionId) {
        // /rm-api/api/v1/metas/supportedSSHKeyPairList?kvId=xxxxxxx
        Assert.notEmpty(kvId, "kvId不能为空");
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_SSH_KEY_PAIR_SKU_KVID+"?kvId="+kvId,headerMap);
        getLogger().info("ssh key pair byId 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_SSH_KEY_PAIR_SKU_KVID);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询存储帐户列表
     *
     * @return
     */
    @Override
    public ResultMsg getStorageAccountList(String region, String subscriptionId) {
        // /rm-api/api/v1/metas/supportedStorageAccountList/{region}
        Assert.notEmpty(region, "Azure Region不能为空");
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_STORAGE_ACCOUNT+"/"+region,headerMap);
        getLogger().info("查询存储帐户列表 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_STORAGE_ACCOUNT);
        msg.setResult(true);
        return msg;
    }
    /**
     * 查询日志桶元数据根据id
     *
     * @return
     */
    @Override
    public ResultMsg getLogsBlobContainerListById(String saId, String region, String subscriptionId) {
        // /rm-api/api/v1/metas/supportedLogsBlobContainerList?saId=xxx
        Assert.notEmpty(saId, "saId不能为空");
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_LOGS_BLOB_CONTAINER+"?saId="+saId,headerMap);
        getLogger().info("查询日志桶元数据根据id 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_LOGS_BLOB_CONTAINER);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询虚拟网络列表
     *
     * @return
     */
    @Override
    public ResultMsg getNetworkList(String region) {
        // /rm-api/api/v1/metas/supportedNetworkList/{region}
        Assert.notEmpty(region, "Azure Region不能为空");
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_NET_WORK+"/"+region,headerMap);
        getLogger().info("查询虚拟网络列表 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_NET_WORK);
        msg.setResult(true);
        return msg;
    }
    /**
     * 根据id获取子网列表
     * @param vnetId
     * @return
     */
    @Override
    public ResultMsg getSubnetListById(String vnetId, String region) {
        // /rm-api/api/v1/metas/supportedSubnetList?vnetId=xxx
        Assert.notEmpty(vnetId, "vnetId不能为空");
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_SUBNET_SKU_BY_ID+"?vnetId="+vnetId,headerMap);
        getLogger().info("根据id获取子网列表 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_SUBNET_SKU_BY_ID);
        msg.setResult(true);
        return msg;
    }
    /**
     * 查询数据中心列表
     * @return
     */
    @Override
    public ResultMsg getRegionList(String subscriptionId) {
        // /rm-api/api/v1/metas/supportedRegionList
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.METADATA_REGION,headerMap);
        getLogger().info("查询数据中心列表 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.METADATA_REGION);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询订阅列表
     * @return
     */
    @Override
    public ResultMsg listSubscription() {
        // /rm-api/api/v1/metas/supportedSubscriptionList
        long start = System.currentTimeMillis();
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + AzureConstant.SUBSCRIPTION,null);
        getLogger().info("查询订阅列表,查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (StrUtil.isEmpty(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, AzureConstant.SUBSCRIPTION);
        msg.setResult(true);
        return msg;
    }
    //endregion

    //region 资源管理

    /**
     * 删除单个VM
     *
     * @param vmName
     * @return
     */
    @Override
    public ResultMsg deleteVMInstance(String clusterName, String vmName, String dnsName, String region) {
        Assert.notEmpty(region, "Azure Region不能为空");

        String deletedDnsName = StrUtil.isBlank(dnsName)? "/": "/" + dnsName;
        // 调用 azure 接口：/api/v1/vms/{vmName}（DELETE）
        ResultMsg msg = new ResultMsg();
        JSONObject jsonObject = null;
        String urlPath = "/api/v1/vms/" + clusterName + "/" + vmName + deletedDnsName;
        String fullUrl = azureUrl + urlPath;
        try {
            long start = System.currentTimeMillis();
            //订阅
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            getLogger().info("AzureServiceImpl.deleteVMInstance, url:{}, reqstr: {}", fullUrl, vmName);
            String respStr = HttpClientUtil.doDeleteWithRequestTimeOut(
                    fullUrl, headerMap,sdpApiAzureDelete1VmTimeOut);
            getLogger().info("AzureServiceImpl.deleteVMInstance , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);

            if (StringUtils.isBlank(respStr)) {
                msg.setResult(false);
                msg.setErrorMsg("deleteVMInstance error.");
                return msg;
            }
            msg.setResult(true);
            jsonObject = JSONObject.parseObject(respStr);
        }catch (SocketTimeoutException e){
            getLogger().error("请求接口超时，",e);
            InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs = new InfoThirdApiFailedLogWithBLOBs();
            failedLogWithBLOBs.withApiName("deleteVMInstance")
                    .withApiUrl(urlPath)
                    .withTimeOut(sdpApiAzureDelete1VmTimeOut)
                    .withFailedType(InfoThirdApiFailedLogWithBLOBs.FAILED_TYPE_TIMEOUT)
                    .withApiKeyParam(vmName)
                    .withRegion(region)
                    .withExceptionInfo(ExceptionUtils.getStackTrace(e))
                    .withCreatedTime(new Date());
            thirdApiFailedLogService.saveFailedLog(failedLogWithBLOBs);
        }
        catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            getLogger().info("AzureServiceImpl.deleteVMInstance.JSONObject.parseObject error,e: {}", e);
            InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs = new InfoThirdApiFailedLogWithBLOBs();
            failedLogWithBLOBs.withApiName("deleteVMInstance")
                    .withApiUrl(urlPath)
                    .withTimeOut(sdpApiAzureDelete1VmTimeOut)
                    .withFailedType(InfoThirdApiFailedLogWithBLOBs.FAILED_TYPE_EXCEPTION)
                    .withApiKeyParam(vmName)
                    .withRegion(region)
                    .withExceptionInfo(ExceptionUtils.getStackTrace(e))
                    .withCreatedTime(new Date());
            thirdApiFailedLogService.saveFailedLog(failedLogWithBLOBs);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 查询job的执行状态接口 带接口响应超时
     *
     * @param jobId
     */
    @Override
    public ResultMsg getJobsStatusWithRequestTimeout(String region, String jobId) {
        ResultMsg msg = new ResultMsg();
        Assert.notEmpty(region, "Azure Region不能为空");
        try {
            long start = System.currentTimeMillis();
            String url = azureUrl + "/api/v1/jobs/" + jobId;
            getLogger().info("getJobsStatus req {}", url);
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Map<String, String> headerMap = new HashMap<>();
            //订阅id
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            String respStr = HttpClientUtil.doGetWithRequestTimeOut(url, null,headerMap, sdpApiAzureQueryJobTimeOut);
            getLogger().info("getJobsStatus elapse:{}, req url: {},  response: {}", (System.currentTimeMillis() - start), url, respStr);
            if (null == respStr || "".equals(respStr)) {
                getLogger().warn("getJobsStatus error", jobId);
                msg.setResult(false);
                msg.setMsg("request error");
                JSONObject target = new JSONObject();
                target.put("status", "unknown");
                msg.setData(target);
                return msg;
            }
            JSONObject sourceData = JSONObject.parseObject(respStr);
            JSONObject target = new JSONObject();
            // 此处data存放的是完整的Azure返回报文, Azure返回的报文有自己固定的格式.
            // 通过azureResponse2resultMsg方法处理后, 返回来的ResultMsg中, data属性是这个完整的返回报文, 为了能正确处理,建议先转为字符串.
            target.put("data", sourceData);
            target.put("status", 200);
            target.put("code", "success");
            return this.azureResponse2resultMsg(target.toJSONString(), "/api/v1/jobs");
        }catch (SocketTimeoutException e){
            getLogger().error("查询任务结果超时，",e);
            InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs = new InfoThirdApiFailedLogWithBLOBs();
            failedLogWithBLOBs.withApiName("getJobsStatus")
                    .withApiUrl("/api/v1/jobs/" + jobId)
                    .withTimeOut(sdpApiAzureQueryJobTimeOut)
                    .withFailedType(InfoThirdApiFailedLogWithBLOBs.FAILED_TYPE_TIMEOUT)
                    .withApiKeyParam(jobId)
                    .withRegion(region)
                    .withExceptionInfo(ExceptionUtils.getStackTrace(e))
                    .withCreatedTime(new Date());
            thirdApiFailedLogService.saveFailedLog(failedLogWithBLOBs);
        }catch (Exception e) {
            getLogger().error("查询任务异常，",e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs = new InfoThirdApiFailedLogWithBLOBs();
            failedLogWithBLOBs.withApiName("getJobsStatus")
                    .withApiUrl("/api/v1/jobs/" + jobId)
                    .withTimeOut(sdpApiAzureQueryJobTimeOut)
                    .withFailedType(InfoThirdApiFailedLogWithBLOBs.FAILED_TYPE_EXCEPTION)
                    .withApiKeyParam(jobId)
                    .withRegion(region)
                    .withExceptionInfo(ExceptionUtils.getStackTrace(e))
                    .withCreatedTime(new Date());
            thirdApiFailedLogService.saveFailedLog(failedLogWithBLOBs);
        }
        return msg;
    }

    /**
     * vm申请详细信息
     *
     * @param jobId
     */
    @Override
    public ResultMsg provisionDetail(String jobId,String region) {
        ResultMsg resultMsg = new ResultMsg();
        // 调用 azure 接口：/api/v1/jobs/{id}/provisionDetail（GET）
        long start = System.currentTimeMillis();
        JSONObject respData = null;
        Assert.notEmpty(region,"Azure Region不能为空");
        try {
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Map<String, String> headerMap = new HashMap<>();
            //订阅id
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            String respStr = HttpClientUtil.doGetWithRequestTimeOut(azureUrl + "/api/v1/jobs/" + jobId + "/provisionDetail",
                    null, headerMap,sdpApiAzureProvisionDetailTimeOut);
            getLogger().info("Azure接口，VM申请明细接口返回结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
            if (StringUtils.isBlank(respStr)) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("provisionDetail error.");
                getLogger().error("Azure接口，VM申请明细接口返回结果为空。");
                return resultMsg;
            }
            respData = JSON.parseObject(respStr);
        } catch (Exception e) {
            getLogger().error("请求provisionDetail异常: {}", e);
        }

        if (respData.isEmpty()) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("provisionDetail error.");
        } else {
            resultMsg.setResult(true);
            resultMsg.setData(respData);
        }
        return resultMsg;
    }


    //endregion

    //region SPOT
    /**
     * 查询vmSku价格
     * {
     * "vmSkuName": "Standard_D4s_v5",
     * "spotUnitPricePerHourUSD": 0.019429,
     * "onDemandUnitPricePerHourUSD": 0.192
     * }
     *
     * @param skuNames
     */
    @Override
    public List<JSONObject> getInstancePriceList(List<String> skuNames,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用 azure 接口：/api/v1/price/spotInstance（GET）
        long start = System.currentTimeMillis();
        Map params = new HashMap();
        params.put("region", region);
        params.put("vmSkuNames", skuNames);
        String reqStr = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);

        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        getLogger().info("AzureServiceImpl.getInstancePrice, 订阅ID={}, reqStr: {}", subscriptionId, reqStr);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotInstance" ,reqStr,headerMap);
        getLogger().info("AzureServiceImpl.getInstancePrice , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            throw new RuntimeException("从Azure获取价格失败(返回报文:" + reqStr + ")");
        }
        try {
            JSONArray respData = JSON.parseArray(respStr);
            return respData.stream().map(v -> {
                return (JSONObject)v;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getInstancePrice.Gson.parseObject error, respStr: {}, e: {}", respStr, e);
            throw new RuntimeException("AzureServiceImpl.getInstancePrice.Gson.parseObject error, respStr: " + respStr, e);
        }
    }

    /**
     * {
     *    "vmSkuName": "Standard_D4s_v5",
     *    "spotUnitPricePerHourUSD": 0.019429,
     *    "onDemandUnitPricePerHourUSD": 0.192
     * }
     * @param skuName
     * @param region
     * @return
     */
    @Override
    public JSONObject getInstancePrice(String skuName,String region) {
        List<String> skuNames = new ArrayList<>();
        skuNames.add(skuName);

        List<JSONObject> skuPrices = getInstancePrice(skuNames, region);
        if (CollectionUtil.isNotEmpty(skuPrices)) {
            return skuPrices.get(0);
        } else {
            return null;
        }
    }

    /**
     * {
     *    "vmSkuName": "Standard_D4s_v5",
     *    "spotUnitPricePerHourUSD": 0.019429,
     *    "onDemandUnitPricePerHourUSD": 0.192
     * }
     * @param skuNames
     * @param region
     * @return
     */
    @Override
    public List<JSONObject> getInstancePrice(List<String> skuNames, String region){
        Assert.notEmpty(skuNames, "查询实例价格时,实例SKU不能为空");
        return getInstancePriceList(skuNames,region);
    }

    /**
     * 竞价实例驱逐率
     *
     * @param skuNames
     */
    @Override
    public List<JSONObject> spotEvictionRate(List<String> skuNames,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        Map params = new HashMap();
        params.put("region", region);
        params.put("vmSkuNames", skuNames);

        String reqStr = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
        getLogger().info("AzureServiceImpl.spotEvictionRate, reqStr: {}", reqStr);
        Date requestTime = new Date();
        // 调用 azure 接口：/api/v1/price/spotEvictionRate（POST）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotEvictionRate", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.spotEvictionRate , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        // 该方法直接通过feign调用，这里只能通过日志的MDC获取到clusterId
        if (StringUtils.isBlank(respStr)) {
            throw new RuntimeException("从Azure获取驱逐率失败: 返回数据为空");
        }

        JSONArray evictionRates = null;
        try {
            evictionRates = JSON.parseArray(respStr);
            return evictionRates.stream().map(v -> {
                return (JSONObject)v;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.spotEvictionRate.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
            throw new RuntimeException("AzureServiceImpl.spotEvictionRate.JSONObject.parseObject error, respStr: " + respStr, e);
        }
    }

    /**
     * 竞价实例历史价格
     *
     * @param skuNames
     */
    @Override
    public ResultMsg spotPriceHistory(List<String> skuNames,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        JSONObject params = new JSONObject(2);
        params.put("region", region);
        params.put("vmSkuNames", skuNames);

        String reqStr = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
        getLogger().info("AzureServiceImpl.spotPriceHistory, reqStr: {}", reqStr);
        // 调用 azure 接口：/api/v1/price/spotPriceHistory（POST）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotPriceHistory", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.spotPriceHistory , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("spotPriceHistory error.");
            return resultMsg;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.spotPriceHistory.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        resultMsg.setData(jsonObject.get("priceItems"));
        return resultMsg;
    }

    /**
     * @param skuName
     * @return
     */
    @Override
    public ResultMsg getSpotVmRealtimePrice(String skuName,String region) {
        getLogger().info("尝试获取spot单价信息，skuName:{},region:{}", skuName,region);
        JSONObject instancePriceJson = getInstancePrice(skuName,region);

        try {
            VmRealtimePriceModel vmRealtimePrice = new VmRealtimePriceModel();
            vmRealtimePrice.setVmName(instancePriceJson.getString("vmSkuName"));
            // spot
            vmRealtimePrice.setRtPrice(instancePriceJson.getDouble("spotUnitPricePerHourUSD"));
            // 标准价
            vmRealtimePrice.setStdPrice(instancePriceJson.getDouble("onDemandUnitPricePerHourUSD"));
            return ResultMsg.SUCCESST(vmRealtimePrice);
        } catch (Exception e) {
            getLogger().error("construct VmRealtimePrice error", e);
            return null;
        }


    }


    //endregion


    @Override
    public ResultMsg getFleet(String region, String clusterName, String groupName) {
        Assert.notEmpty(region, "getFleet Azure Region不能为空");
        Assert.notEmpty(clusterName, "getFleet clusterName 不能为空");
        Assert.notEmpty(groupName, "getFleet groupName 不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        // 调用 azure 接口：/api/v1/fleet/{cluster}/{group}（get）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/fleet/"+clusterName+"/"+groupName,headerMap);
        getLogger().info("AzureServiceImpl.getFleet , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("getFleet error.");
            return resultMsg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getFleet.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        resultMsg.setData(jsonObject);
        return resultMsg;
    }

    @Override
    public ResultMsg delFleet(String region, String clusterName, String groupName) {
        Assert.notEmpty(region, "delFleet Azure Region不能为空");
        Assert.notEmpty(clusterName, "delFleet clusterName 不能为空");
        Assert.notEmpty(groupName, "delFleet groupName 不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        // 调用 azure 接口：/api/v1/fleet/{cluster}/{group}（delete）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doDelete(azureUrl + "/api/v1/fleet/"+clusterName+"/"+groupName,headerMap);
        getLogger().info("AzureServiceImpl.delFleet , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("delFleet error.");
            return resultMsg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.delFleet.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        resultMsg.setData(jsonObject);
        return resultMsg;
    }

    /**
     * 统一转换resultMsg
     *
     * @param respStr
     * @return
     */
    private ResultMsg azureResponse2resultMsg(String respStr, String apiName) {
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(respStr)) {
            resultMsg.setErrorMsg("azure " + apiName + " api response content is empty.");
            resultMsg.setResult(false);
            return resultMsg;
        }
        try {
            if (!respStr.startsWith("{") && !respStr.endsWith("}")) {
                resultMsg.setErrorMsg("azure " + apiName + " api execution azureResponse2resultMsg method error:content is not json");
                resultMsg.setResult(false);
                return resultMsg;
            }
            AzureResponse azureResponse = JSON.parseObject(respStr, AzureResponse.class);
            if (null != azureResponse) {
                resultMsg.setRetcode(azureResponse.getStatus());
                resultMsg.setResult("success".equals(azureResponse.getCode()));
                resultMsg.setMsg(azureResponse.getMessage());
                // 此处是Azure返回来的完整报文,因为是Object类型,所以改代码的时候要关注一下返回来的真正的类型
                resultMsg.setData(azureResponse.getData());
            } else {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("azure " + apiName + " api response content in wrong format.");
            }
        } catch (Exception e) {
            getLogger().info("azure " + apiName + " api execution azureResponse2resultMsg error:" + e.getMessage(), e);
            resultMsg.setErrorMsg("azure " + apiName + " api execution azureResponse2resultMsg method error:" + e.getMessage());
            resultMsg.setResult(false);
        }
        return resultMsg;
    }
    @Override
    public List<AzurePriceHistory> getSkuInstancePrice(List<String> skuNames, String region){
        Assert.notEmpty(skuNames, "查询实例价格时,实例SKU不能为空");
        return azurePriceHistoryMapper.selectSkuLatest(region,skuNames);
    }


    @Override
    public ResultMsg deleteVirtualMachines(String deleteVMsRequest) {
        return deleteVirtualMachines(JSON.parseObject(deleteVMsRequest, AzureDeleteVMsRequest.class));
    }

    /**
     * 批量删除VM实例
     *
     * @return
     */
    public ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest) {
        ResultMsg msg = new ResultMsg();
        String reqStr = JSON.toJSONString(deleteVMsRequest, SerializerFeature.DisableCircularReferenceDetect);

        // 调用 azure 接口：/api/v1/vms/deleteVirtualMachines（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.deleteVirtualMachines, reqStr: {}", reqStr);
        Date requestTime = new Date();
        String subscriptionId = metaDataItemService.getSubscriptionId(deleteVMsRequest.getRegion());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        HttpClientUtil.HttpResult result = HttpClientUtil.httpPutWithRequestTimeOut(
                azureUrl + "/api/v1/vms/deleteVirtualMachines",
                reqStr,headerMap,
                sdpApiAzureDeleteVmTimeOut);
        getLogger().info("AzureServiceImpl.deleteVirtualMachines, elapse:{}ms, respStr: {}",
                (System.currentTimeMillis() - start), result.getResponseBody());
        saveAzureApiLogs(null, null, null, requestTime, reqStr, result.getResponseBody());

        if (result.getStatusCode() != 200) {
            msg.setResult(false);
            msg.setErrorMsg("deleteVirtualMachines error.");
            msg.setMsg(JSON.toJSONString(result.getResponseBody()));
            msg.setActimes(180);
            return msg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result.getResponseBody());
            msg.setResult(true);
        } catch (Exception e) {
            msg.setResult(false);
            getLogger().info("AzureServiceImpl.deleteVirtualMachines.JSONObject.parseObject error, respStr: {}, e: {}",
                    result.getResponseBody(), e);
        }
        msg.setData(jsonObject);
        return msg;
    }


    @Override
    public void saveAzureApiLogs(String clusterId, String activityLogId, String planId, Date requestTime,
                                 String requestParam, String responseBody) {
        try {
            Date responseTime = new Date();
         //   Map<String, Object> azureApiBaseData = threadLocal.get();
            String threadClusterId = null;
            String threadActivityLogId = null;
            String threadPlanId = null;
//            if (azureApiBaseData != null) {
//                threadClusterId = (azureApiBaseData.containsKey("clusterId") && azureApiBaseData.get("clusterId") != null) ? azureApiBaseData.get("clusterId").toString() : null;
//                threadActivityLogId = (azureApiBaseData.containsKey("activityLogId") && azureApiBaseData.get("activityLogId") != null) ? azureApiBaseData.get("activityLogId").toString() : null;
//                threadPlanId = (azureApiBaseData.containsKey("planId") && azureApiBaseData.get("planId") != null) ? azureApiBaseData.get("planId").toString() : null;
//            }

            if (StringUtils.isBlank(clusterId)) {
                if (StringUtils.isNotBlank(threadClusterId)) {
                    clusterId = threadClusterId;
                } else {
                    clusterId = MDC.get(ComposeConstant.Cluster_ID);
                }
            }
            if (StringUtils.isBlank(activityLogId) && StringUtils.isNotBlank(threadActivityLogId)) {
                activityLogId = threadActivityLogId;
            }
            if (StringUtils.isBlank(planId) && StringUtils.isNotBlank(threadPlanId)) {
                planId = threadPlanId;
            }

            InfoClusterFullLogWithBLOBs infoClusterFullLogWithBLOBs = new InfoClusterFullLogWithBLOBs();
            infoClusterFullLogWithBLOBs.setClusterId(clusterId);
            infoClusterFullLogWithBLOBs.setClusterName(null);
            infoClusterFullLogWithBLOBs.setActivityLogId(activityLogId);
            infoClusterFullLogWithBLOBs.setActionName(null);
            infoClusterFullLogWithBLOBs.setPlanId(planId);
            infoClusterFullLogWithBLOBs.setRequestTime(requestTime);
            infoClusterFullLogWithBLOBs.setResponseTime(responseTime);
            infoClusterFullLogWithBLOBs.setRequestParam(requestParam);
            infoClusterFullLogWithBLOBs.setResponseBody(responseBody);
            ResultMsg resultMsg = fullLogService.saveLog(infoClusterFullLogWithBLOBs);
            if (!resultMsg.getResult()) {
                getLogger().error("AzureServiceImpl.saveAzureApiLogs error. errorMsg: {}", resultMsg.getErrorMsg());
            }
            //threadLocal.remove();
        } catch (Exception e) {
            // 捕获所有异常，防止异常对业务的影响
            getLogger().error("AzureServiceImpl.saveAzureApiLogs error. e: ", e);
            try {
               // threadLocal.remove();
            } catch (Exception ex) {
                getLogger().error("AzureServiceImpl.saveAzureApiLogs threadLocal.remove() error. ex: ", ex);
            }
        }
    }


}
