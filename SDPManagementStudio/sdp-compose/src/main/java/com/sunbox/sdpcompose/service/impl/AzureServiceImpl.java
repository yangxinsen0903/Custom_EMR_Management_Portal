package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.domain.*;
import com.sunbox.domain.azure.VMDiskRequest;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.manager.AzureServiceManager;
import com.sunbox.sdpcompose.mapper.ConfClusterMapper;
import com.sunbox.sdpcompose.mapper.ConfClusterTagMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterPlaybookJobMapper;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.sdpcompose.model.azure.request.*;
import com.sunbox.sdpcompose.model.azure.response.AzureResponse;
import com.sunbox.sdpcompose.model.azure.response.ResourceGroupResponse;
import com.sunbox.sdpcompose.service.IAzureService;
import com.sunbox.sdpcompose.service.IFullLogService;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.HttpClientUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

/**
 * @author : [niyang]
 * @className : AzureServiceImpl
 * @description : [微软云接口交互]
 * @createTime : [2022/12/2 9:18 AM]
 */
@Service
public class AzureServiceImpl implements IAzureService, BaseCommonInterFace {

    @Value("${azure.request.url}")
    private String azureUrl;
    @Value("${playbook.request.url}")
    private String executePlaybookUrl;

    @Value("${sdp.api.azure.deletevms.timeout:600}")
    private Integer sdpApiAzureDeleteVmTimeOut;

    @Value("${sdp.api.azure.delete1vm.timeout:60}")
    private Integer sdpApiAzureDelete1VmTimeOut;

    private final String metasUri = "/api/v1/metas";

    private final static String RESOURCE_GROUP_TAGS_REGULAR = "^[\\u4E00-\\u9FA5-A-Za-z0-9]{1,100}$";

    @Autowired
    private InfoClusterPlaybookJobMapper infoClusterPlaybookJobMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    ConfClusterTagMapper confClusterTagMapper;

    @Autowired
    private IFullLogService fullLogService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ThreadLocal<Map<String, Object>> threadLocal;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    /**
     * 查询子网列表
     * GET /subnet
     *
     * @return
     */
    @Override
    public ResultMsg getSubnet(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用azure接口 /api/v1/metas/supportedSubnetList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_SUBNET_SKU+"/"+region);
        getLogger().info("查询子网列表, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        //返回数据中，没有status或者code进行标识请求的成功或者失败，所以通过返回http状态码和报文来判断
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_SUBNET_SKU);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询VM Sku列表
     * GET /vm/skus
     *
     * @return
     */
    @Override
    public ResultMsg geVmSkus(String region) {
        // 调用azure接口/vm/skus
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_VM_SKU,headerMap);
        getLogger().info("查询VM, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_VM_SKU);
        msg.setResult(true);
        return msg;
    }

    /**
     * 查询磁盘Sku列表
     * GET /disk/sku
     *
     * @return
     */
    @Override
    public ResultMsg getDiskSku(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //调用azure接口 /api/v1/metas/supportedDiskSkuList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_DISK_SKU+"/"+region,headerMap);
        getLogger().info("查询磁盘Sku列表, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_DISK_SKU);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg getNSGSku(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedNSGSkuList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_NSG_SKU+"/"+region,headerMap);
        getLogger().info("NSGSku查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_NSG_SKU);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg getSSHKeyPair(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedSSHKeyPairList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_SSH_KEY_PAIR_SKU+"/"+region,headerMap);
        getLogger().info("ssh key pair 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_SSH_KEY_PAIR_SKU);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg getMIList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // /api/v1/metas/supportedManagedIdentityList
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doGet(azureUrl + metasUri + ComposeConstant.METADATA_MANAGED_IDENTITY+"/"+region,headerMap);
        getLogger().info("MI(Managed Identity) 查询结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        ResultMsg msg = this.azureResponse2resultMsg(jsonStr, ComposeConstant.METADATA_MANAGED_IDENTITY);
        msg.setResult(true);
        return msg;
    }

    /**
     * 批量创建VM实例
     * POST /vms
     *
     * @param azureVmsRequest
     * @return
     */
    @Override
    public ResultMsg createVms(AzureVmsRequest azureVmsRequest) {
        azureVmsRequest.getVirtualMachineGroups().forEach(virtualMachineGroup -> {
            virtualMachineGroup.getVirtualMachineSpec().setSshPublicKeyType("KeyVaultSecret");//写死使用密钥对的方式！
        });
        String reqStr = JSON.toJSONString(azureVmsRequest, SerializerFeature.DisableCircularReferenceDetect);
        getLogger().info("create vms,request azure param:" + reqStr);
        long start = System.currentTimeMillis();
        Date requestTime = new Date();
        String subscriptionId = metaDataItemService.getSubscriptionId(azureVmsRequest.getRegion());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String jsonStr = HttpClientUtil.doPost(azureUrl + "/api/v1/vms", reqStr,headerMap);
        getLogger().info("create vms, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);
        saveAzureApiLogs(null, null, null, requestTime, reqStr, jsonStr);
        if (null == jsonStr || "".equals(jsonStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        if (jsonObject.containsKey("id") && StringUtils.isNotBlank(jsonObject.getString("id"))) {
            ResultMsg msg = this.azureResponse2resultMsg(jsonStr, "/vms");
            msg.setData(jsonObject);
            msg.setResult(true);
            return msg;
        } else {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            return msg;
        }
    }

    @Override
    public ResultMsg getJobsStatus(String jobId,String subscriptionId) {
        Date requestTime = new Date();
        long start = System.currentTimeMillis();
        String url = azureUrl + "/api/v1/jobs/" + jobId;
        getLogger().info("getJobsStatus subscriptionId={}, req={}", subscriptionId, url);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(url,headerMap);
        getLogger().info("getJobsStatus elapse: {}, req {} , response {}", (System.currentTimeMillis() - start), url, respStr);
        saveAzureApiLogs(null, null, null, requestTime, jobId, respStr);
        if (null == respStr || "".equals(respStr)) {
            getLogger().warn("getJobsStatus error", jobId);
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("request error");
            JSONObject target = new JSONObject();
            target.put("status", "unknown");
            msg.setData(target);
            return msg;
        }
        JSONObject sourceData = JSONObject.parseObject(respStr);
        JSONObject target = new JSONObject();
        target.put("data", sourceData);
        target.put("status", 200);
        target.put("code", "success");
        return this.azureResponse2resultMsg(target.toJSONString(), "/api/v1/jobs");
    }

    /**
     * 查询job的执行状态接口 带接口响应超时
     *
     * @param jobId
     */
    @Override
    public ResultMsg getJobsStatusWithRequestTimeout(String jobId,String region) {
        ResultMsg msg = new ResultMsg();
        try {
            long start = System.currentTimeMillis();
            String url = azureUrl + "/api/v1/jobs/" + jobId;
            getLogger().info("getJobsStatus req {}", url);
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Map<String, String> headerMap = new HashMap<>();
            //订阅id
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            String respStr = HttpClientUtil.doGetWithRequestTimeOut(url, null, headerMap,60);
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
            target.put("data", sourceData);
            target.put("status", 200);
            target.put("code", "success");
            return this.azureResponse2resultMsg(target.toJSONString(), "/api/v1/jobs");
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * 执行Playbook
     * POST /execute-job/playbook
     *
     * @param azureExecuteJobPlaybookRequest
     * @return
     */
    @Override
    public ResultMsg executeJobPlaybook(AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest) {
        String jsonStr = JSON.toJSONString(azureExecuteJobPlaybookRequest, SerializerFeature.WriteNullStringAsEmpty);
        long start = System.currentTimeMillis();
        getLogger().info("execute play book request json {}", jsonStr);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,azureExecuteJobPlaybookRequest.getSubscriptionId());
        String reqUri = executePlaybookUrl + "/api/PlaybookExecute/execute";
        String respStr = HttpClientUtil.doPost(reqUri, jsonStr,headerMap);
        getLogger().info("execute play book response data, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (null == respStr || "".equals(respStr)) {
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setMsg("executeJobPlaybook error");
            return msg;
        }

        JSONObject sourceData = JSONObject.parseObject(respStr);
        JSONArray jsonArray = sourceData.getJSONArray("data");
        JSONObject target = new JSONObject();
        target.put("data", jsonArray);
        target.put("status", 200);
        target.put("code", "success");

        //更新 info_cluster_playbook_job
        String transactionId = azureExecuteJobPlaybookRequest.getTransactionId();
        InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJob = new InfoClusterPlaybookJobWithBLOBs();
        infoClusterPlaybookJob.setTransactionId(transactionId);
        infoClusterPlaybookJob.setJobStatus(InfoClusterPlaybookJob.JOB_RUNNING);//执行中
        infoClusterPlaybookJobMapper.updateByPrimaryKeySelective(infoClusterPlaybookJob);

        return this.azureResponse2resultMsg(target.toJSONString(), "/api/PlaybookExecute/execute");
    }

    @Override
    public ResultMsg queryPlaybookExecuteResult(String transactionId,
                                                String subscriptionId,
                                                String keyVaultResourceName,
                                                String secretResourceId) {
        String suc = "success";
        String fail = "fail";
        String processing = "processing";

        String reqUri = executePlaybookUrl + "/api/PlaybookExecute/GetTransactionExecuteResult/" + transactionId;
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        Map<String, String> params = new HashMap<>();
        params.put("sshKeyVaultName",keyVaultResourceName);
        params.put("sshPrivateSecretName",secretResourceId);
        params.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        getLogger().info("query playbook execute status reqUri {},params {},subscriptionId {}", reqUri, params,subscriptionId);
        JSONArray jsonArray;
        try {
            String respStr = HttpClientUtil.doGetWithRequestTimeOut(reqUri, params, headerMap, 80);
            getLogger().info("query playbook execute status response, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
            if (null == respStr || "".equals(respStr)) {
                getLogger().error("查询Playbook执行状态,没拿到数据，算执行中");
                ResultMsg msg = new ResultMsg();
                this.updatePlaybookJob(transactionId, 1);
                msg.setRetcode(processing);
                return msg;
            }
            JSONObject sourceData = JSONObject.parseObject(respStr);
            jsonArray = sourceData.getJSONArray("jobList");
        }catch (Exception e){
            getLogger().error("查询AzureFleetJob结果异常，",e);
            ResultMsg msg = new ResultMsg();
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
            return msg;
        }

        //全部子任务执行完成后，再进行判断任务是否成功
        //非空 且有1 运行中
        if (null != jsonArray && jsonArray.size() > 0) {
            for (int i = 0, len = jsonArray.size(); i < len; i++) {
                JSONObject jobJson = jsonArray.getJSONObject(i);
                Integer jobStatus = jobJson.getInteger("jobStatus");
                if (1 == jobStatus || 0 == jobStatus) {
                    getLogger().info("任务在运行中. transactionId={}, jobStatus={}, jobId={}",
                            transactionId, jobStatus, jobJson.getString("jobid"));
                    this.updatePlaybookJob(transactionId, 1);
                    ResultMsg msg = new ResultMsg();
                    msg.setRetcode(processing);
                    msg.setData(jsonArray);
                    return msg;
                }
            }
        }

        //非空 且有3 失败
        if (null != jsonArray && jsonArray.size() > 0) {
            for (int i = 0, len = jsonArray.size(); i < len; i++) {
                JSONObject jobJson = jsonArray.getJSONObject(i);
                Integer jobStatus = jobJson.getInteger("jobStatus");
                if (3 == jobStatus) {
                    getLogger().info("任务运行失败. transactionId={}, jobStatus={}, jobId={}",
                            transactionId, jobStatus, jobJson.getString("jobid"));
                    this.updatePlaybookJob(transactionId, 3);
                    ResultMsg msg = new ResultMsg();
                    msg.setRetcode(fail);
                    msg.setData(jsonArray);
                    return msg;
                }
            }
        }

        //非空 且全2
        if (null != jsonArray && jsonArray.size() > 0) {
            int len = jsonArray.size();
            int count = 0;
            for (int i = 0; i < len; i++) {
                JSONObject jobJson = jsonArray.getJSONObject(i);
                Integer jobStatus = jobJson.getInteger("jobStatus");
                if (2 == jobStatus) { //2==已完成
                    count++;
                }
            }
            if (count == len) {
                getLogger().info("任务运行成功. transactionId={}", transactionId);
                this.updatePlaybookJob(transactionId, 2);
                ResultMsg msg = new ResultMsg();
                msg.setRetcode(suc);
                msg.setData(jsonArray);
                return msg;
            }
        }

        //其余情况是执行中
        this.updatePlaybookJob(transactionId, 1);
        ResultMsg msg = new ResultMsg();
        msg.setRetcode(processing);
        msg.setData(jsonArray);
        return msg;

    }

    private void updatePlaybookJob(String transactionId, Integer jobStatus) {
        //更新 info_cluster_playbook_job
        InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJob = new InfoClusterPlaybookJobWithBLOBs();
        infoClusterPlaybookJob.setTransactionId(transactionId);
        infoClusterPlaybookJob.setJobStatus(jobStatus);//执行完成
        infoClusterPlaybookJobMapper.updateByPrimaryKeySelective(infoClusterPlaybookJob);
    }

    /**
     * 删除集群
     * DELETE /cluster/{clusterName}
     *
     * @param clusterName
     * @return
     */
    @Override
    public ResultMsg ruinCluster(String clusterName,String subscriptionId) {
        try {
            clusterName = URLEncoder.encode(clusterName, "UTF-8");
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
        }
        Assert.notEmpty(subscriptionId, "删除资源组时,Azure subscriptionId不能为空");

        //调用azure接口 /api/v1/rgs/{name} delete方式
        long start = System.currentTimeMillis();
        getLogger().info("销毁集群 ruinCluster subscriptionId ={}, req = {}", subscriptionId, clusterName);
        Date requestTime = new Date();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID, subscriptionId);
        String respStr = HttpClientUtil.doDelete(azureUrl + "/api/v1/rgs/rg-sdp-" + clusterName,headerMap);
        getLogger().info("ruinCluster return , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        saveAzureApiLogs(null, null, null, requestTime, clusterName, respStr);
        ResultMsg msg = new ResultMsg();

        //region 验证无效的返回结果

        if (null == respStr || "".equals(respStr)) {
            msg.setResult(false);
            msg.setMsg("ruin cluster error");
            return msg;
        }

        //返回的不是JsonString 属于异常请求
        if (!isJSON2(respStr)) {
            getLogger().error("respStr,非json格式string:" + respStr);
            msg.setResult(false);
            msg.setMsg(respStr);
            return msg;
        }

        //endregion 验证无效的返回结果

        msg.setResult(true);
        try {
            JSONObject jsonObject = JSONObject.parseObject(respStr);
            msg.setData(jsonObject);
        } catch (Exception e) {
            if (respStr.toLowerCase(Locale.ROOT).contains("already deleted")) {
                msg.setMsg(respStr);
                Map<String, Object> data = new HashMap<>();
                data.put("id", "000000");
                msg.setData(data);
                msg.setResult(true);
            } else {
                msg.setResult(false);
            }
        }
        return msg;
    }


    /**
     * 判断是否是json
     *
     * @param str
     * @return
     */
    private boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public ResultMsg createResourceGroup(String azureResourceGroupTagsRequest) {
        // 1）创建资源组字段前后一致 2）传入ClusterName，添加前缀后是否符合Azure对资源组名称的要求
        return createResourceGroup(JSON.parseObject(azureResourceGroupTagsRequest, AzureResourceGroupTagsRequest.class));
    }

    /**
     * 创建资源组
     *
     * @return
     */
    @Override
    public ResultMsg createResourceGroup(AzureResourceGroupTagsRequest azureResourceGroupTagsRequest) {
        String reqStr = JSON.toJSONString(azureResourceGroupTagsRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/rgs（POST）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.createResourceGroup, req: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(azureResourceGroupTagsRequest.getRegion());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/rgs", reqStr, headerMap);
        getLogger().info("AzureServiceImpl.createResourceGroup , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("createResourceGroup error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.createResourceGroup.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 查看资源组
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg getResourceGroup(String clusterId,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        ResultMsg msg = new ResultMsg();
        ConfCluster confCluster = checkCluster(clusterId);
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("没有找到集群.");
            return msg;
        }

        // 调用 azure 接口：/api/v1/rgs/{name}（GET）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.getResourceGroup, req: {}", confCluster.getClusterName());
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/rgs/rg-sdp-" + confCluster.getClusterName(),headerMap);
        getLogger().info("AzureServiceImpl.getResourceGroup, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("getResourceGroup error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getResourceGroup.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }

        ResourceGroupResponse response = new ResourceGroupResponse();
        response.setClusterId(clusterId);
        JSONObject tags = jsonObject.getJSONObject("tags");
        Map map = tags != null ? tags.toJavaObject(Map.class) : null;
        response.setTags(map);
        msg.setData(response);
        return msg;
    }


    private ConfCluster checkCluster(String clusterId) {
        ConfCluster cluster = null;
        if (StringUtils.isNotBlank(clusterId)) {
            cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        }
        return cluster;
    }

    @Override
    public ResultMsg updateResourceGroupTags(String azureResourceGroupTagsRequest) {
        ResultMsg msg = new ResultMsg();
        JSONObject reqJson = JSONObject.parseObject(azureResourceGroupTagsRequest);
        AzureResourceGroupTagsRequest request = new AzureResourceGroupTagsRequest();
        ConfCluster confCluster = checkCluster(reqJson.getString("clusterId"));
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("没有找到集群.");
            return msg;
        }
        if (!(confCluster.getState().equals(1) || confCluster.getState().equals(2))) {
            msg.setResult(false);
            msg.setErrorMsg("待创建或已销毁的集群不能添加标签.");
            return msg;
        }
        String region = confCluster.getRegion();

        request.setName(confCluster.getClusterName());
        request.setApiVersion("1.0");
        request.setTransactionId(UUID.randomUUID().toString());
        request.setTags(new HashMap<>());

        JSONObject tags = null;
        try {
            tags = reqJson.getJSONObject("tags");

            // 参数校验
            for (Map.Entry<String, Object> entry : tags.entrySet()) {
                String tagKey = entry.getKey();
                String tagValue = (String) entry.getValue();
                if ((!tagKey.matches(RESOURCE_GROUP_TAGS_REGULAR)) || (!tagValue.matches(RESOURCE_GROUP_TAGS_REGULAR))) {
                    // 不符合正则规则
                    msg.setResult(false);
                    msg.setErrorMsg("标签内容不符合规则");
                    return msg;
                }
            }
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误");
            return msg;
        }
        request.setTags(tags);

        String reqStr = JacksonUtils.toJson(request);

        // 调用 azure 接口：/api/v1/rgs/{name}/updateTags（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.updateResourceGroupTags, reqStr: {}", reqStr);
        Date requestTime = new Date();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/" + request.getName() + "/updateTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.updateResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        saveAzureApiLogs(confCluster.getClusterId(), null, null, requestTime, reqStr, respStr);
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("updateResourceGroupTags error.");
            return msg;
        }

        // 更新集群标签,先删除全部标签，再新增标签
        updateConfClusterTag(confCluster.getClusterId(), request);

        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.updateResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }

        ResourceGroupResponse response = new ResourceGroupResponse();
        response.setClusterId(confCluster.getClusterId());
        JSONObject tagsRes = jsonObject.getJSONObject("tags");
        Map map = tagsRes != null ? tagsRes.toJavaObject(Map.class) : null;
        response.setTags(map);
        msg.setData(response);
        return msg;
    }

    private void updateConfClusterTag(String clusterId, AzureResourceGroupTagsRequest request) {
        confClusterTagMapper.deleteByClusterId(clusterId);

        for (Map.Entry<String, Object> entry : request.getTags().entrySet()) {
            ConfClusterTag tag = new ConfClusterTag();
            tag.setClusterId(clusterId);
            tag.setTagGroup(entry.getKey());
            tag.setTagVal(String.valueOf(entry.getValue()));
            confClusterTagMapper.insert(tag);
        }
    }

    /**
     * 更新资源组标签 - 全量
     *
     * @param azureResourceGroupTagsRequest
     * @return{
     */
    @Override
    public ResultMsg updateResourceGroupTags(AzureResourceGroupTagsRequest azureResourceGroupTagsRequest) {
        String region=azureResourceGroupTagsRequest.getRegion();
        Assert.notEmpty(region,"Azure Region不能为空");
        String reqStr = JSON.toJSONString(azureResourceGroupTagsRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/rgs/{name}/updateTags（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.updateResourceGroupTags, reqStr: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/"  + azureResourceGroupTagsRequest.getName() + "/updateTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.updateResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("updateResourceGroupTags error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.updateResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg addResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        ResultMsg msg = new ResultMsg();
        JSONObject reqJson = JSONObject.parseObject(azureResourceGroupAddTagsRequest);
        AzureResourceGroupAddTagsRequest request = new AzureResourceGroupAddTagsRequest();

        ConfCluster confCluster = checkCluster(reqJson.getString("clusterId"));
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("没有找到集群.");
            return msg;
        }
        if (!(confCluster.getState().equals(1) || confCluster.getState().equals(2))) {
            msg.setResult(false);
            msg.setErrorMsg("待创建或已销毁的集群不能添加标签.");
            return msg;
        }
        String region = confCluster.getRegion();

        JSONArray tags = null;
        List<AzureResourceGroupAddTagsRequest.ResourceGroupTag> tagList = null;
        try {
            tags = reqJson.getJSONArray("tags");

            // 参数校验
            tagList = tags.toJavaList(AzureResourceGroupAddTagsRequest.ResourceGroupTag.class);
            Set<String> tagNameSet = new HashSet<>();
            for (AzureResourceGroupAddTagsRequest.ResourceGroupTag tag : tagList) {
                String tagName = tag.getTagName();
                tagNameSet.add(tagName);
                String tagValue = tag.getTagValue();
                if ((!tagName.matches(RESOURCE_GROUP_TAGS_REGULAR)) || (!tagValue.matches(RESOURCE_GROUP_TAGS_REGULAR))) {
                    // 不符合正则规则
                    msg.setResult(false);
                    msg.setErrorMsg("标签内容不符合规则");
                    return msg;
                }
            }

            // 标签重复性检验
            if (tagList.size() != tagNameSet.size()) {
                // 参数中包含重复标签
                msg.setResult(false);
                msg.setErrorMsg("参数中包含重复标签");
                return msg;
            }
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误");
            return msg;
        }

        // 标签有效性校验
        Set<String> tagKeySet = new HashSet<>();
        for (AzureResourceGroupAddTagsRequest.ResourceGroupTag resourceGroupTag : tagList) {
            tagKeySet.add(resourceGroupTag.getTagName());
        }
        boolean tagsExists = isTagsExists(confCluster.getClusterId(), tagKeySet);
        if (tagsExists) {
            msg.setResult(false);
            msg.setErrorMsg("标签已存在");
            return msg;
        }

        request.setName(confCluster.getClusterName());
        request.setApiVersion("1.0");
        request.setTransactionId(UUID.randomUUID().toString());
        request.setTags(tagList);

        String reqStr = JacksonUtils.toJson(request);

        // 调用 azure 接口：/api/v1/rgs/{name}/addTag（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.addResourceGroupTags, reqStr: {}", reqStr);
        Date requestTime = new Date();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/rg-sdp-" + request.getName() + "/addTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.addResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        saveAzureApiLogs(confCluster.getClusterId(), null, null, requestTime, reqStr, respStr);
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("addResourceGroupTags error.");
            return msg;
        }

        // 更新集群标签，新增标签
        addConfClusterTag(confCluster.getClusterId(), request);

        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.addResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }

        ResourceGroupResponse response = new ResourceGroupResponse();
        response.setClusterId(confCluster.getClusterId());
        JSONObject tagsRes = jsonObject.getJSONObject("tags");
        Map map = tagsRes != null ? tagsRes.toJavaObject(Map.class) : null;
        response.setTags(map);
        msg.setData(response);
        return msg;
    }

    private boolean isTagsExists(String clusterId, Set<String> tagKeySet) {
        List<ConfClusterTag> confClusterTagList = confClusterTagMapper.getTagsbyClusterId(clusterId);
        Set<String> existTagsSet = new HashSet<>();
        for (ConfClusterTag confClusterTag : confClusterTagList) {
            existTagsSet.add(confClusterTag.getTagGroup());
        }

        for (String tagKey : tagKeySet) {
            if (existTagsSet.contains(tagKey)) {
                return true;
            }
        }

        return false;
    }

    private void addConfClusterTag(String clusterId, AzureResourceGroupAddTagsRequest request) {
        List<AzureResourceGroupAddTagsRequest.ResourceGroupTag> tags = request.getTags();
        for (AzureResourceGroupAddTagsRequest.ResourceGroupTag tag : tags) {
            ConfClusterTag confClusterTag = new ConfClusterTag();
            confClusterTag.setClusterId(clusterId);
            confClusterTag.setTagGroup(tag.getTagName());
            confClusterTag.setTagVal(tag.getTagValue());
            confClusterTagMapper.insert(confClusterTag);
        }
    }

    /**
     * 更新资源组标签 - 增量
     *
     * @param azureResourceGroupAddTagsRequest
     * @return
     */
    @Override
    public ResultMsg addResourceGroupTags(AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest) {
        String region=azureResourceGroupAddTagsRequest.getRegion();
        Assert.notEmpty(region, "Azure Region不能为空");
        String reqStr = JSON.toJSONString(azureResourceGroupAddTagsRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/rgs/{name}/addTag（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.addResourceGroupTags, reqStr: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/rg-sdp-" + azureResourceGroupAddTagsRequest.getName() + "/addTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.addResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("addResourceGroupTags error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.addResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg deleteResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        ResultMsg msg = new ResultMsg();
        JSONObject reqJson = JSONObject.parseObject(azureResourceGroupAddTagsRequest);
        AzureResourceGroupAddTagsRequest request = new AzureResourceGroupAddTagsRequest();
        ConfCluster confCluster = checkCluster(reqJson.getString("clusterId"));
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("没有找到集群.");
            return msg;
        }
        String region = confCluster.getRegion();

        request.setName(confCluster.getClusterName());
        request.setApiVersion("1.0");
        request.setTransactionId(UUID.randomUUID().toString());
        JSONArray tagNames = null;
        try {
            tagNames = reqJson.getJSONArray("tagNames");
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误");
            return msg;
        }
        if (tagNames == null || tagNames.isEmpty()) {
            msg.setResult(false);
            msg.setErrorMsg("无有效标签名称");
            return msg;
        }
        List<String> tagNameList = tagNames.toJavaList(String.class);
        request.setTagNames(tagNameList);

        String reqStr = JacksonUtils.toJson(request);
        // 调用 azure 接口：/api/v1/rgs/{name}（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.deleteResourceGroupTags, respStr: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/rg-sdp-" + request.getName() + "/deleteTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.deleteResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("deleteResourceGroupTags error.");
            return msg;
        }

        // 删除集群标签
        deleteConfClusterTag(confCluster.getClusterId(), request);

        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.deleteResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }

        ResourceGroupResponse response = new ResourceGroupResponse();
        response.setClusterId(confCluster.getClusterId());
        JSONObject tagsRes = jsonObject.getJSONObject("tags");
        Map map = tagsRes != null ? tagsRes.toJavaObject(Map.class) : null;
        response.setTags(map);
        msg.setData(response);
        return msg;
    }

    private void deleteConfClusterTag(String clusterId, AzureResourceGroupAddTagsRequest request) {
        List<String> tagNames = request.getTagNames();
        for (String tagName : tagNames) {
            ConfClusterTagKey tagKey = new ConfClusterTagKey();
            tagKey.setClusterId(clusterId);
            tagKey.setTagGroup(tagName);
            confClusterTagMapper.deleteByPrimaryKey(tagKey);
        }
    }

    /**
     * 删除资源组标签
     *
     * @param azureResourceGroupAddTagsRequest
     * @return
     */
    @Override
    public ResultMsg deleteResourceGroupTags(AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest) {
        String region=azureResourceGroupAddTagsRequest.getRegion();
        Assert.notEmpty(region,"Azure Region不能为空");
        String reqStr = JSON.toJSONString(azureResourceGroupAddTagsRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/rgs/{name}（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.deleteResourceGroupTags, respStr: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/rgs/rg-sdp-" + azureResourceGroupAddTagsRequest.getName() + "/deleteTags", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.deleteResourceGroupTags , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("deleteResourceGroupTags error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.deleteResourceGroupTags.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     *
     * @param
     * @return
     */
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
        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/metas/supportedVMSkuList/"+region,headerMap);
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
    public ResultMsg createVMInstance(String azureVMInstanceRequest) {
        return createVMInstance(JSON.parseObject(azureVMInstanceRequest, AzureVMInstanceRequest.class));
    }

    /**
     * 单个创建VM实例
     *
     * @return
     */
    @Override
    public ResultMsg createVMInstance(AzureVMInstanceRequest azureVMInstanceRequest) {
        String reqStr = JSON.toJSONString(azureVMInstanceRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/vms/{clusterName}/{groupName}/{groupIndex}（POST）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.createVMInstance, reqStr: {}", reqStr);
        String subscriptionId = metaDataItemService.getSubscriptionId(azureVMInstanceRequest.getRegion());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/vms/" + azureVMInstanceRequest.getClusterName() + "/" + azureVMInstanceRequest.getGroupName() + "/" + azureVMInstanceRequest.getGroupIndex(), reqStr,headerMap);
        getLogger().info("AzureServiceImpl.createVMInstance , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("createVMInstance error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.createVMInstance.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 单个删除VM实例
     *
     * @return
     */
    @Override
    public ResultMsg deleteVMInstance(String vmName, String dnsName, String region) {
        Assert.notEmpty(region, "Azure Region不能为空");

        // 调用 azure 接口：/api/v1/vms/{vmName}（DELETE）
        ResultMsg msg = new ResultMsg();
        JSONObject jsonObject = null;
        try {
            String deleteDnsName = StrUtil.isBlank(dnsName)? "/": "/" + dnsName;
            long start = System.currentTimeMillis();
            getLogger().info("AzureServiceImpl.deleteVMInstance, reqstr: {}", vmName);
            String subscriptionId = metaDataItemService.getSubscriptionId(region);
            Map<String, String> headerMap = new HashMap<>();
            //订阅id
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            String respStr = HttpClientUtil.doDeleteWithRequestTimeOut(
                    azureUrl + "/api/v1/vms/" +region+"/"+ vmName + deleteDnsName,
                    headerMap,
                    sdpApiAzureDelete1VmTimeOut);
            getLogger().info("AzureServiceImpl.deleteVMInstance , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);

            if (StringUtils.isBlank(respStr)) {
                msg.setResult(false);
                msg.setErrorMsg("deleteVMInstance error.");
                return msg;
            }
            msg.setResult(true);
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            getLogger().info("AzureServiceImpl.deleteVMInstance.JSONObject.parseObject error,e: {}", e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg appendVirtualMachines(String appendVMsRequest) {
        return appendVirtualMachines(JSON.parseObject(appendVMsRequest, AzureAppendVMsRequest.class));
    }

    /**
     * 批量创建VM实例
     *
     * @return
     */
    public ResultMsg appendVirtualMachines(AzureAppendVMsRequest appendVMsRequest) {
        String reqStr = null;
        try {
            ResultMsg msg = new ResultMsg();
            reqStr = JSON.toJSONString(appendVMsRequest, SerializerFeature.DisableCircularReferenceDetect);

            // 调用 azure 接口：/api/v1/vms/appendVirtualMachines（PUT）
            long start = System.currentTimeMillis();
            getLogger().info("AzureServiceImpl.appendVirtualMachines, reqStr: {}", reqStr);
            Date requestTime = new Date();
            String subscriptionId = metaDataItemService.getSubscriptionId(appendVMsRequest.getRegion());
            Map<String, String> headerMap = new HashMap<>();
            //订阅id
            headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
            HttpClientUtil.HttpResult httpResult = HttpClientUtil.httpPut(azureUrl + "/api/v1/vms/appendVirtualMachines", reqStr,headerMap);
            String respStr = httpResult.getResponseBody();
            getLogger().info("AzureServiceImpl.appendVirtualMachines , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
            saveAzureApiLogs(null, null, null, requestTime, reqStr, respStr);

            if (StringUtils.isEmpty(respStr)) {
                msg.setResult(false);
                if (httpResult.getStatusCode() == HttpClientUtil.HttpResult.EXCEPTION_CODE) {
                    msg.setErrorMsg("调用Azure服务发生错误," + httpResult.getResponseBody() + "],request:" + reqStr);
                } else {
                    msg.setErrorMsg("Azure服务返回错误[" + httpResult.getStatusCode() + httpResult.getResponseBody() + "],request:" + reqStr);
                }
                return msg;
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = JSONObject.parseObject(respStr);
                msg.setResult(true);
            } catch (Exception e) {
                msg.setResult(false);
                getLogger().info("AzureServiceImpl.appendVirtualMachines.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
            }
            msg.setData(jsonObject);
            return msg;
        } catch (Exception e) {
            getLogger().error("appendVirtualMachines error,request:{}", appendVMsRequest);
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setErrorMsg(String.format("调用Azure申请虚拟机失败，请检查Azure服务,request:%s", reqStr));
            return msg;
        }
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
    public ResultMsg updateVirtualMachinesDiskSize(String request) {
        return updateVirtualMachinesDiskSize(JSON.parseObject(request, AzureUpdateVirtualMachinesDiskSizeRequest.class));
    }

    /**
     * 批量/单个VM扩容磁盘
     *
     * @return
     */
    @Override
    public ResultMsg updateVirtualMachinesDiskSize(AzureUpdateVirtualMachinesDiskSizeRequest request) {
        String reqStr = JSON.toJSONString(request, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/vms/updateVirtualMachinesDiskSize（PUT）
        getLogger().info("AzureServiceImpl.updateVirtualMachinesDiskSize, req: {}", reqStr);
        long start = System.currentTimeMillis();
        Date requestTime = new Date();
        String subscriptionId = metaDataItemService.getSubscriptionId(request.getRegion());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/vms/updateVirtualMachinesDiskSize", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.updateVirtualMachinesDiskSize , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        // 该方法直接通过feign调用，这里只能通过日志的MDC获取到clusterId
        saveAzureApiLogs(null, null, null, requestTime, reqStr, respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("updateVirtualMachinesDiskSize error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.updateVirtualMachinesDiskSize.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 获取AZ列表
     */
    @Override
    public ResultMsg getAzList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用 azure 接口：/api/v1/metas/supportedAvailabilityZoneList（GET）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/metas/supportedAvailabilityZoneList/"+region,headerMap);
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

    /**
     * 磁盘扩容
     */
    @Override
    public ResultMsg addPart(AzureAddPartRequest addPartRequest) {
        return updateVMsDiskSize(addPartRequest);
    }

    /**
     * 磁盘扩容
     */
    @Override
    public ResultMsg updateVMsDiskSize(AzureAddPartRequest addPartRequest) {
        String reqStr = JSON.toJSONString(addPartRequest, SerializerFeature.DisableCircularReferenceDetect);
        // 调用 azure 接口：/api/v1/vms/updateVMsDiskSize（PUT）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.addPart, req: {}", reqStr);
        Date requestTime = new Date();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,addPartRequest.getSubscriptionId());
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/vms/updateVMsDiskSize", reqStr,headerMap);
        getLogger().info("AzureServiceImpl.updateVMsDiskSize , elapse:{}ms, subscriptionId:{}, resp: {}",
                (System.currentTimeMillis() - start), addPartRequest.getSubscriptionId(), respStr);
        saveAzureApiLogs(null, null, null, requestTime, reqStr, respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("updateVMsDiskSize error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.updateVMsDiskSize.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg getSpotVmRealtimePrice(String skuName,String region) {
        AzureServiceManager.VmRealtimePrice vmRealtimePrice = AzureServiceManager.tryGetSpotVmRealtimePrice(getLogger(), this, skuName,region);
        return ResultMsg.SUCCESS(vmRealtimePrice);
    }

    @Override
    public ResultMsg getLogsBlobContainerList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        // 调用 azure 接口：/api/v1/metas/supportedLogsBlobContainerList（GET）
        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.getLogsBlobContainerList,begin");
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);

        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/metas/supportedLogsBlobContainerList/"+region,headerMap);
        getLogger().info("AzureServiceImpl.getLogsBlobContainerList , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setMsg("supportedLogsBlobContainerList error.");
            return msg;
        }
        msg.setResult(true);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getLogsBlobContainerList.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    @Override
    public ResultMsg getInstancePrice(String skuName,String region) {
        ResultMsg msg = new ResultMsg();
        Gson gson = new Gson();
        String redisPref = "price:";

        String skuPrice = redisLock.getValue(redisPref + skuName+":"+region);
        if (StringUtils.isNotBlank(skuPrice)) {
            Map<String, Object> respData = new HashMap<>();
            respData = gson.fromJson(skuPrice, respData.getClass());
            msg.setResult(true);
            msg.setData(respData);
            return msg;
        }

        ResultMsg resultMsg = getAzureInstancePrice(skuName,region);
        if (resultMsg.getResult()) {
            redisLock.save(redisPref + skuName+":"+region, gson.toJson(resultMsg.getData()), 60 * 5);
            return resultMsg;
        } else {
            return resultMsg;
        }
    }

    /**
     * 查询vmSku价格
     */
    public ResultMsg getAzureInstancePrice(String skuName,String region) {
        Assert.notEmpty(region, "查询vmSku价格 Azure Region不能为空");
        Assert.notEmpty(skuName, "查询vmSku价格 skuName不能为空");
        // 调用 azure 接口：/api/v1/price/spotInstance（GET）
        long start = System.currentTimeMillis();
        JSONObject params = new JSONObject();
        params.put("region", region);
        JSONArray vmSkuNames = new JSONArray(1);
        vmSkuNames.add(skuName);
        params.put("vmSkuNames", vmSkuNames);
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotInstance",params.toJSONString(),headerMap);
        getLogger().info("AzureServiceImpl.getInstancePrice , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isBlank(respStr)) {
            msg.setResult(false);
            msg.setErrorMsg("从Azure获取价格失败(" + skuName + ")");
            return msg;
        }

        msg.setResult(true);
        Gson gson = new Gson();
        Map<String, Object> respData = new HashMap<>();
        try {
            respData = gson.fromJson(respStr, respData.getClass());
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.getInstancePrice.Gson.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        msg.setData(respData);
        return msg;
    }

    /**
     * 竞价实例驱逐率
     */
    @Override
    public ResultMsg spotEvictionRate(List<String> skuNames,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        String reqStr = JSON.toJSONString(skuNames, SerializerFeature.DisableCircularReferenceDetect);
        getLogger().info("AzureServiceImpl.spotEvictionRate, reqStr: {}", reqStr);
        Date requestTime = new Date();
        // 调用 azure 接口：/api/v1/price/spotEvictionRate（POST）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotEvictionRate/"+region, reqStr,headerMap);
        getLogger().info("AzureServiceImpl.spotEvictionRate , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        // 该方法直接通过feign调用，这里只能通过日志的MDC获取到clusterId
        saveAzureApiLogs(null, null, null, requestTime, reqStr, respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("spotEvictionRate error.");
            return resultMsg;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.spotEvictionRate.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
        }
        resultMsg.setData(jsonObject.get("spotEvictionRateList"));
        return resultMsg;
    }

    /**
     * 竞价实例历史价格
     */
    @Override
    public ResultMsg spotPriceHistory(List<String> skuNames,String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        String reqStr = JSON.toJSONString(skuNames, SerializerFeature.DisableCircularReferenceDetect);
        getLogger().info("AzureServiceImpl.spotPriceHistory, reqStr: {}", reqStr);
        // 调用 azure 接口：/api/v1/price/spotPriceHistory（POST）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doPost(azureUrl + "/api/v1/price/spotPriceHistory/"+region, reqStr,headerMap);
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
        resultMsg.setData(jsonObject.get("spotPriceHistoryList"));
        return resultMsg;
    }

    /**
     * vm申请详细信息
     */
    @Override
    public ResultMsg provisionDetail(String jobId,String region) {
        ResultMsg resultMsg = new ResultMsg();
        // 调用 azure 接口：/api/v1/jobs/{id}/provisionDetail（GET）
        long start = System.currentTimeMillis();
        String subscriptionId = metaDataItemService.getSubscriptionId(region);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        String respStr = HttpClientUtil.doGet(azureUrl + "/api/v1/jobs/" + jobId + "/provisionDetail",headerMap);
        getLogger().info("Azure接口，VM申请明细接口返回结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("provisionDetail error.");
            getLogger().error("Azure接口，VM申请明细接口返回结果为空。");
            return resultMsg;
        }

        JSONObject respData = null;
        try {
            respData = JSON.parseObject(respStr);
        } catch (Exception e) {
            getLogger().error(": {}", e);
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
    /**
     * 更新PV2数据盘IOPS和MBPS
     * @param VMDiskRequest
     * @return
     */
    @Override
    public ResultMsg updateVMsDiskIOPSAndMbps(VMDiskRequest VMDiskRequest) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        // 调用 azure 接口：/api/v1/vms/updateVMsDiskIopsAndMbps put
        long start = System.currentTimeMillis();
        Map<String, String> headerMap = new HashMap<>();
        String bodyJson = JSON.toJSONString(VMDiskRequest);
        getLogger().info("AzureServiceImpl.updateVMsDiskIopsAndMbps ,bodyJson: {}", bodyJson);
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID, VMDiskRequest.getSubscriptionId());
        String respStr = HttpClientUtil.doPut(azureUrl + "/api/v1/vms/updateVMsDiskIopsAndMbps",bodyJson,headerMap);
        getLogger().info("AzureServiceImpl.updateVMsDiskIopsAndMbps , elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), respStr);
        if (StringUtils.isBlank(respStr)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("updateVMsDiskIopsAndMbps error.");
            return resultMsg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(respStr);
        } catch (Exception e) {
            getLogger().info("AzureServiceImpl.updateVMsDiskIopsAndMbps.JSONObject.parseObject error, respStr: {}, e: {}", respStr, e);
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

    /**
     * Azure接口日志收集
     */
    @Override
    public void saveAzureApiLogs(String clusterId, String activityLogId, String planId, Date requestTime,
                                 String requestParam, String responseBody) {
        try {
            Date responseTime = new Date();
            Map<String, Object> azureApiBaseData = threadLocal.get();
            String threadClusterId = null;
            String threadActivityLogId = null;
            String threadPlanId = null;
            if (azureApiBaseData != null) {
                threadClusterId = (azureApiBaseData.containsKey("clusterId") && azureApiBaseData.get("clusterId") != null) ? azureApiBaseData.get("clusterId").toString() : null;
                threadActivityLogId = (azureApiBaseData.containsKey("activityLogId") && azureApiBaseData.get("activityLogId") != null) ? azureApiBaseData.get("activityLogId").toString() : null;
                threadPlanId = (azureApiBaseData.containsKey("planId") && azureApiBaseData.get("planId") != null) ? azureApiBaseData.get("planId").toString() : null;
            }

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
            threadLocal.remove();
        } catch (Exception e) {
            // 捕获所有异常，防止异常对业务的影响
            getLogger().error("AzureServiceImpl.saveAzureApiLogs error. e: ", e);
            try {
                threadLocal.remove();
            } catch (Exception ex) {
                getLogger().error("AzureServiceImpl.saveAzureApiLogs threadLocal.remove() error. ex: ", ex);
            }
        }
    }

    public String getAzureUrl() {
        return azureUrl;
    }

    public void setAzureUrl(String azureUrl) {
        this.azureUrl = azureUrl;
    }

    public String getExecutePlaybookUrl() {
        return executePlaybookUrl;
    }

    public void setExecutePlaybookUrl(String executePlaybookUrl) {
        this.executePlaybookUrl = executePlaybookUrl;
    }

    public String getMetasUri() {
        return metasUri;
    }

    public InfoClusterPlaybookJobMapper getInfoClusterPlaybookJobMapper() {
        return infoClusterPlaybookJobMapper;
    }

    public void setInfoClusterPlaybookJobMapper(InfoClusterPlaybookJobMapper infoClusterPlaybookJobMapper) {
        this.infoClusterPlaybookJobMapper = infoClusterPlaybookJobMapper;
    }
}
