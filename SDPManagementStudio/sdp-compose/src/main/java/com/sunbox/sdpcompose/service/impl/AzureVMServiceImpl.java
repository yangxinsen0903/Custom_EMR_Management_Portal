package com.sunbox.sdpcompose.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.dao.mapper.InfoClusterVmClearLogMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.azure.*;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.domain.metaData.AvailabilityZone;
import com.sunbox.domain.metaData.keyVault;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.consts.JobNameConstant;
import com.sunbox.sdpcompose.consts.LockPrefixConstant;
import com.sunbox.sdpcompose.manager.AzureMetaDataManager;
import com.sunbox.sdpcompose.manager.AzureServiceManager;
import com.sunbox.sdpcompose.manager.InfoClusterVmIndexManager;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.sdpcompose.model.azure.fleet.request.*;
import com.sunbox.sdpcompose.model.azure.request.*;
import com.sunbox.sdpcompose.service.IAzureService;
import com.sunbox.sdpcompose.service.IVMService;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.sdpcompose.service.ambari.AmbariInfo;
import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.service.*;
import com.sunbox.util.DNSUtil;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.KeyVaultUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : AzureServiceImpl
 * @description : ]
 * @createTime : [2022/11/30 5:01 PM]
 */
@Qualifier("AzureVMServiceImpl")
@Service("azureVMService")
public class AzureVMServiceImpl implements IVMService, BaseCommonInterFace {

    @Value("${vm.username}")
    private String vmusername;

    @Value("${vm.hostnamesuffix}")
    private String hostnamesuffix;

    @Value("${compose.message.clientname}")
    private String clientname;

    @Value("${sdp.vm.core.minNum:3}")
    private Integer coreMinNum;

    @Value("${sdp.rm.azurefleet.switch:1}")
    private Integer azureFleetSwitch;

    /**
     * 销毁集群-删除资源组重试次数
     */
    @Value("${sdp.delete.vm.retrytimes:5}")
    private Integer sdpDeleteVMRetryTimes;

    /**
     * 销毁集群-删除资源组重试间隔秒数
     */
    @Value("${sdp.delete.vm.retrywaittimes:30}")
    private Integer sdpDeleteVMRetryWaitTimes;


    /**
     * 创建集群-申请资源重试次数
     */
    @Value("${sdp.create.vm.provision.retrytimes:5}")
    private Integer sdpCreateVMProvisionRetryTimes;

    /**
     * 创建集群-申请资源重试间隔秒数
     */
    @Value("${sdp.create.vm.provision.retrywaittimes:30}")
    private Integer sdpDeleteVMProvisionRetryWaitTimes;

    /**
     * 申请资源部分成功的情况下是否开启DNS检查
     */
    @Value("${sdp.create.vm.provision.checkdns:0}")
    private Integer sdpCreateVMProvisionCheckDNS;

    /**
     * 创建集群-申请资源删除失败vm查询时长
     */
    @Value("${sdp.create.vm.deletefailed.queryduration:600}")
    private Long deleteFailedVMsQueryDuration;

    /**
     * 创建集群-申请资源失败容错率 core 和 task
     * 申请失败节点数/(申请成功+申请失败)
     */
    @Value("${sdp.create.vm.errorrate:0.2}")
    private Double vmErrorRate = 0.2;

    /**
     * 创建集群-申请资源查询timeout
     */
    @Value("${sdp.create.vm.provision.query.timeout:900}")
    private Long vmProvisionQueryTimeOut;


    @Autowired
    private ThreadLocal<Map<String, Object>> threadLocal;

    @Autowired
    private ConfClusterMapper clusterMapper;

    @Autowired
    private ConfClusterVmMapper vmMapper;

    @Autowired
    private ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Autowired
    private ConfClusterTagMapper tagMapper;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private com.sunbox.service.IAzureService azureServiceSer;

    @Autowired
    private InfoClusterVmJobMapper vmJobMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private AzureMetaDataManager azureMetaDataManager;

    @Autowired
    private IMQProducerService producerService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private IPlanExecService planExecService;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private ConfScalingTaskMapper scalingTaskMapper;

    @Autowired
    private ConfClusterHostGroupMapper hostGroupMapper;

    @Autowired
    private ConfScalingTaskVmMapper scalingTaskVmMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private ConfScalingTaskMapper confScalingTaskMapper;

    @Autowired
    private InfoClusterVmRejectMapper vmRejectMapper;

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    ConfScalingVmMapper confScalingVmMapper;

    @Autowired
    IFullLogService fullLogService;

    @Autowired
    private InfoClusterVmIndexMapper infoClusterVmIndexMapper;

    @Autowired
    private InfoClusterVmIndexHistoryMapper infoClusterVmIndexHistoryMapper;

    @Autowired
    private IVMClearLogService ivmClearLogService;

    @Autowired
    private IVMDeleteService ivmDeleteService;

    @Autowired
    private InfoClusterVmClearLogMapper vmClearLogMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private IAzureFleetService azureFleetService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IAmbariService ambariService;

    @Autowired
    private IVmEventService vmEventService;

    @Autowired
    private IClusterFinalBlueprintService clusterFinalBlueprintService;

    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    /**
     * 创建虚拟机
     *
     * @return
     */
    @Override
    public ResultMsg createVms(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog = null;
        try {
            // 0. 解析参数
            JSONObject param = JSON.parseObject(messageParam);
            String activityLogId = param.getString("activityLogId");

            activityLog = planExecService.getInfoActivityLogByLogId(activityLogId);

            ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //创建过程中
            cluster.setState(ConfCluster.CREATING);
            clusterMapper.updateByPrimaryKey(cluster);

            Map<String, Object> azureApiLogs = new HashMap<>();
            azureApiLogs.put("clusterId", cluster.getClusterId());
            azureApiLogs.put("activityLogId", activityLogId);
            azureApiLogs.put("planId", activityLog.getPlanId());
            threadLocal.set(azureApiLogs);

            ResultMsg msg;
            InfoClusterVmJob vmJob;
            StringBuilder summary = new StringBuilder();
            if (azureFleetSwitch.equals(1)) {
                // 1. 获取要创建的vm 信息
                Map<String, Object> res = buildAzureFleetVmsRequest(cluster.getClusterId(), activityLogId);
                AzureFleetVmsRequest azureVmsRequest = (AzureFleetVmsRequest) res.get("azure");
                //生成请求摘要
                summary = extractCreateVmsSummary(azureVmsRequest);
                vmJob = (InfoClusterVmJob) res.get("job");
                getLogger().info(JSON.toJSONString(azureVmsRequest, SerializerFeature.DisableCircularReferenceDetect));

                // 2. 调用接口发起vm申请。
                msg = createVmsWithRetry(azureVmsRequest, cluster.getSubscriptionId());
            } else {
                // 1. 获取要创建的vm 信息
                Map<String, Object> res = bulidAzureVmsRequest(cluster.getClusterId(), activityLogId);
                AzureVmsRequest azureVmsRequest = (AzureVmsRequest) res.get("azure");
                vmJob = (InfoClusterVmJob) res.get("job");
                getLogger().info(JSON.toJSONString(azureVmsRequest, SerializerFeature.DisableCircularReferenceDetect));

                // 2. 调用接口发起vm申请。
                msg = createVmsWithRetry(azureVmsRequest);
            }


            getLogger().info("azureService.createVms result:" + msg.toString());

            if (msg.getResult()) {
                JSONObject data = (JSONObject) msg.getData();
                String jobid = data.getString("id");
                param.put(JobNameConstant.Cluster_VM, jobid);
                vmJob.setJobId(jobid);
                vmJobMapper.updateByPrimaryKeySelective(vmJob);

                if (!StringUtils.isEmpty(jobid)) {
                    planExecService.sendNextActivityMsg(activityLogId, param);
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    activityLog.setLogs(summary + msg.getData().toString());
                } else {
                    getLogger().error("not found createvm jobid.activityLogId:" + activityLogId);
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    activityLog.setLogs(summary + "not found createvm jobid.activityLogId:\n" + activityLogId + "\n"
                            + msg.getData().toString());
                }
            } else {
                activityLog.setLogs(summary + msg.getData().toString());
                activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }

            planExecService.complateActivity(activityLog);

            msg.setResult(true);

        } catch (Exception e) {
            getLogger().error("createVM exception:", e);
            resultMsg.setResult(false);
            if (null != activityLog) {
                activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                activityLog.setLogs(e.getMessage());
                planExecService.complateActivity(activityLog);
            }
        }
        return resultMsg;
    }

    /**
     * 生成请求的概要信息, 用于显示<p/>
     * 主要内容有:
     * <ol>
     *     <li>申请VMSku</li>
     *     <li>申请数量</li>
     *     <li>实际申请数量</li>
     * </ol>
     *
     * @param request
     * @return
     */
    private StringBuilder extractCreateVmsSummary(AzureFleetVmsRequest request) {
        StringBuilder summary = new StringBuilder();
        try {
            summary.append("申请VM数量为:");

            if (CollUtil.isNotEmpty(request.getVirtualMachineGroups())) {
                for (AzureVMGroupRequest group : request.getVirtualMachineGroups()) {
                    summary.append("(").append(group.getGroupName()).append(":");
                    VirtualMachineSpec spec = group.getVirtualMachineSpec();
                    if (Objects.nonNull(spec.getRegularProfile())) {
                        summary.append("按需数量=").append(spec.getRegularProfile().getCapacity())
                                .append(",");
                    }
                    if (Objects.nonNull(spec.getSpotProfile())) {
                        summary.append("竞价数量=").append(spec.getSpotProfile().getCapacity())
                                .append(",");
                    }
                    summary.append("vmsku=");
                    for (VMSizesProfile vmSku : spec.getVmSizesProfile()) {
                        summary.append(vmSku.getName()).append(",");
                    }
                    summary.append(");");
                }
            }

        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            summary.append(ex.getMessage());
        }
        if (summary.length() > 0) {
            summary.append("\n");
        }
        return summary;
    }

    /**
     * 发送创建集群资源请求到azure
     *
     * @param azureVmsRequest
     * @return
     */
    private ResultMsg createVmsWithRetry(AzureVmsRequest azureVmsRequest) {
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                msg = azureService.createVms(azureVmsRequest);
                if (msg.getResult()) {
                    return msg;
                }
            } catch (Exception e) {
                getLogger().error("createVms,异常", e);
            }
            i++;
            if (i > sdpCreateVMProvisionRetryTimes) {
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpDeleteVMProvisionRetryWaitTimes);
        }
    }


    /**
     * 发送创建集群资源请求到azure
     *
     * @param azureVmsRequest
     * @return
     */
    private ResultMsg createVmsWithRetry(AzureFleetVmsRequest azureVmsRequest, String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                msg = azureFleetService.createVms(azureVmsRequest, subscriptionId);
                if (msg.getResult()) {
                    return msg;
                }
            } catch (Exception e) {
                getLogger().error("createVms,异常", e);
            }
            i++;
            if (i > sdpCreateVMProvisionRetryTimes) {
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpDeleteVMProvisionRetryWaitTimes);
        }
    }

    /**
     * 查询虚拟机创建状态
     *
     * @param messageParam
     * @return
     */
    @Override
    public ResultMsg queryVmsCreateJob(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String cvmjobId = param.getString(JobNameConstant.Cluster_VM);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        ResultMsg msg = azureService.getJobsStatus(cvmjobId, confCluster.getSubscriptionId());

        if (msg.getData() != null) {
            // 这个是调用Azure接口返回来的完整数据,不有管前面的各种转换
            JSONObject response = (JSONObject) msg.getData();
            JobQueryResponse queryResp = convertJsonResult(response);

            if (response.containsKey("status")) {
                // 获取申请资源的数量
                Map<String, Integer> reqGroupVmCountMap = getCreateClusterRequestVmCount(confCluster.getClusterId());
                // 申请资源完成, 保存申请信息
                if (queryResp.isSuccessed(reqGroupVmCountMap)) {
                    resultMsg = commonSaveVMInfo(activityLogId, cvmjobId, response);
                    return resultMsg;
                }

                // 申请资源失败
                if (queryResp.isFailed()) {

                    Long ttl = System.currentTimeMillis() - currentLog.getBegtime().getTime();

                    //失败过快，等一等
                    if (ttl < 1000 * 75) {
                        getLogger().warn("失败速度过快，sleep 60s");
                        resultMsg = planExecService.loopActivity(clientname, messageParam,
                                60l, activityLogId);
                        return resultMsg;
                    }

                    //降级或重试
                    // AzureFleet不进行降级处理,申请失败马上中止
//                    ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "create");
//                    if (rrMsg.getResult()) {
//                        return rrMsg;
//                    }
                    // 如果返回了Failed,说明申请资源失败,将所有申请失败的资源全部清理掉
                    syncDeleteAllRequestVms(response, confCluster, currentLog);

                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs(response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                // 申请资源状态未知
                if (response.getString("status").equalsIgnoreCase("unknown")) {
                    getLogger().warn("集群创建申请资源异常的查询结果，等待30s，" + messageParam);
                    //异常的查询结果 延时30s
                    resultMsg = planExecService.loopActivity(clientname, messageParam,
                            30l, activityLogId);
                    return resultMsg;
                }

                //region 集群创建查询任务超时，查询detail 处理卡住的VM
                Long duri = new Date().getTime() / 1000 - currentLog.getBegtime().getTime() / 1000;

                if (duri > vmProvisionQueryTimeOut) {
                    // 任务执行超时, 中止任务
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT);
                    currentLog.setLogs("从Azure查询申请进度超时,可以重试：\n" + response.toJSONString());
                    return planExecService.complateActivity(currentLog);
//                    ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "create");
//                    if (rrMsg.getResult()) {
//                        return rrMsg;
//                    }
                }
                //endregion

                // 继续查询
                resultMsg = planExecService.loopActivity(clientname, messageParam,
                        10l, activityLogId);
                return resultMsg;
            } else {
                getLogger().error("集群创建查询资源创建结果异常：" + messageParam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            getLogger().error("集群创建查询资源创建结果异常：" + messageParam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(msg.toString());
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    /**
     * 找到集群的各个实例组申请VM数量
     * @param clusterId
     * @return
     */
    private Map<String, Integer> getCreateClusterRequestVmCount(String clusterId) {
        Map<String, Integer> reqCountMap = new HashMap<>();

        List<ConfClusterHostGroup> hostgroups = hostGroupMapper.selectByClusterId(clusterId);
        for (ConfClusterHostGroup hostgroup : hostgroups) {
            // 实例队列不获取, 因为实例队列有可能是按vCores申请的.
            reqCountMap.put(hostgroup.getGroupName(), hostgroup.getInsCount());
        }

        getLogger().info("DEBUG: 获取创建集群各实例组申请数量信息: clusterId={}, info={}", clusterId, JSON.toJSONString(reqCountMap));

        return reqCountMap;
    }

    /**
     * 将查询Job接口返回来的JSON转换为人能看懂的Object
     * @param result
     * @return
     */
    private JobQueryResponse convertJsonResult(Object result) {
        JobQueryResponse jobResp;
        if (result instanceof JSONObject) {
            jobResp = ((JSONObject) result).toJavaObject(JobQueryResponse.class);
        } else if (result instanceof String) {
            jobResp = JSON.parseObject((String) result, JobQueryResponse.class);
        } else {
            throw new RuntimeException("将Job接口返回的JSON转为JobQueryResponse时出错, 类型不正确. result:"
                    + result);
        }
        return jobResp;
    }

    /**
     * 同步删除所有从Azure申请下来的VM
     * @param response 通过Job接口查询返回来的JSON格式的响应数据
     * @param confCluster 集群
     * @param currentLog 当前执行步骤
     */
    private void syncDeleteAllRequestVms(JSONObject response, ConfCluster confCluster, InfoClusterOperationPlanActivityLogWithBLOBs currentLog) {
        JobQueryResponse queryResp = response.toJavaObject(JobQueryResponse.class);
        List<VirtualMachineResponse> allVms = queryResp.getAllVms();
        List<String> vmNames = allVms.stream().map(VirtualMachineResponse::getName).collect(Collectors.toList());
        List<String> dnsNames = allVms.stream().map(VirtualMachineResponse::getHostName).collect(Collectors.toList());
        getLogger().info("从Azure请求创建的VM不成功, 这些VM将全部被删除: {}", StrUtil.join(",", vmNames));
        syncDeleteVms(vmNames, dnsNames, confCluster, currentLog.getActivityId());
    }

    /**
     * 创建集群-申请资源失败降级or重试
     *
     * @param cvmjobId     创建任务ID
     * @param activityLog  执行步骤信息
     * @param confCluster  集群信息
     * @param messageParam 流程消息
     * @return 返回false 失败
     * 返回true
     */
    private ResultMsg createOrScaleOutJobRetryOrReduce(String cvmjobId,
                                                       InfoClusterOperationPlanActivityLogWithBLOBs activityLog,
                                                       ConfCluster confCluster,
                                                       JSONObject messageParam,
                                                       String operation) {
        ResultMsg resultMsg = new ResultMsg();
        JSONObject provisionDetail;

        if (azureFleetSwitch.equals(1)) {
            provisionDetail = getAzureFleetVMProvisionDetail(cvmjobId, confCluster.getSubscriptionId());
        } else {
            provisionDetail = getAzureVMProvisionDetail(cvmjobId, confCluster.getRegion());
        }

        // 下面的代码取消调用,因为provisionDetail改为Job后, 认为会全部成功
//        ResultMsg ckpdc = checkProvisionDetailComplete(provisionDetail, activityLog, confCluster);
//
//        if (!ckpdc.getResult()) {
//            getLogger().warn(ckpdc.getErrorMsg() + "发送Loop消息。");
//            // provisiondetail 数据不完整，需要发送loop消息等待数据完整。
//            resultMsg.setResult(true);
//            planExecService.loopActivity(clientname, messageParam.toJSONString(), 60L, activityLog.getActivityLogId());
//            return resultMsg;
//        }

        saveProvisionDetailToFullLog(cvmjobId, activityLog, confCluster, provisionDetail);

        if (Objects.isNull(provisionDetail)) {
            return resultMsg;
        }

        JobQueryResponse jobResp = provisionDetail.toJavaObject(JobQueryResponse.class);
        //region 扩容任务保存provisiondetail结果中成功的部分
        //是否有VM已经申请成功
        if (!operation.equalsIgnoreCase("create") && jobResp.getData().size()>0) {
            getLogger().info("扩容任务保存provisiondetail结果中成功部分，业务开始");
            // 不清楚此处为啥要包装一层, 但是看saveClusterAppendVminfo()方法的入参不需要包装. 所以此处先去掉包装.
            JSONObject response = buildAzureScaleOutVmResponse(confCluster, cvmjobId, jobResp);
//            saveClusterAppendVminfo(response.toJSONString());
            saveClusterAppendVminfo(JSON.toJSONString(jobResp));
            getLogger().info("扩容任务保存provisiondetail结果中成功部分，业务结束");
        }
        //endregion

        // 把降级的VM给删掉, 返回结果:
        // rows: 失败的VM   data: 全量的数据
        ResultMsg processMsg = processCreateOrScaleOutJobDetailMessage(confCluster, provisionDetail, activityLog.getActivityLogId(), operation);
        if (!processMsg.getResult()) {
            //重试
            Integer retryCnt = 0;
            JSONObject azure_retryObj;
            if (messageParam.containsKey(JobNameConstant.Param_Azure_Retry_obj)) {
                azure_retryObj = messageParam.getJSONObject(JobNameConstant.Param_Azure_Retry_obj);
                retryCnt = azure_retryObj.getInteger("count");
            } else {
                azure_retryObj = new JSONObject();
            }
            retryCnt++;
            getLogger().info("创建集群-申请资源重试次数:" + retryCnt);
            if (retryCnt > sdpCreateVMProvisionRetryTimes) {
                getLogger().info("创建集群-申请资源重试次数超过限制：" + sdpCreateVMProvisionRetryTimes);
                resultMsg.setErrorMsg(processMsg.getErrorMsg());
                resultMsg.setResult(false);
                return resultMsg;
            }
            azure_retryObj.put("count", retryCnt);
            // 发送重试消息
            return sendAzureCreateVMRetryMessage(azure_retryObj, activityLog, Long.valueOf(processMsg.getActimes()));
        } else {
            //降级，保存数据
            if (operation.equalsIgnoreCase("create")) {
                //创建集群
                JSONObject response = buildAzureCreateVmResponse(confCluster, cvmjobId, processMsg.getData());
                return commonSaveVMInfo(activityLog.getActivityLogId(), cvmjobId, response);
            } else {
                //集群扩容
                JSONObject response = buildAzureScaleOutVmResponse(confCluster, cvmjobId, jobResp);
                saveClusterAppendVminfo(response.toJSONString());
                activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                //region 保存DNS异常的数据信息到taskremark
                if (StringUtils.isNotEmpty(processMsg.getExt2())) {
                    saveDnsExpToTaskRemark(processMsg.getExt2(), cvmjobId);
                }
                //endregion 保存DNS异常的数据信息到taskremark
                planExecService.complateActivity(activityLog);
                messageParam.remove(JobNameConstant.Param_Azure_Retry_obj);
                return planExecService.sendNextActivityMsg(activityLog.getActivityLogId(), messageParam);
            }
        }
    }

    /**
     * 检测ProvisionDetail数据是否完整
     * 由于Azure的ProvisionDetail接口为聚合接口，存在数据不完整的情况，
     * 在使用该接口时需要校验数据的完整性。
     * 数据的不完整性主要表现在deployDetailResults已经有结果，对于Succeeded的deploy，
     * 在provisionedVmGroups属性中没有给出详细的VM信息。
     * 判断的基本逻辑： 统计deployDetailResults中Succeeded的VM数量 与provisionedVmGroups中对应的角色VM数量比较
     * provisionedVmGroups中的VM数量要>=deployDetailResults中Succeeded的VM数量
     *
     * @param detailMsg
     * @param activityLog
     * @param confCluster
     * @return
     */
    private ResultMsg checkProvisionDetailComplete(JSONObject detailMsg,
                                                   InfoClusterOperationPlanActivityLogWithBLOBs activityLog,
                                                   ConfCluster confCluster) {
        Assert.notNull(detailMsg, "检查申请VM是否完成时，参数[VM结果]为空。");
        ResultMsg msg = new ResultMsg<>();

        //region provisonDetail接口返回success
        if (detailMsg != null &&
                detailMsg.containsKey("provisionStatus") &&
                detailMsg.getString("provisionStatus").equalsIgnoreCase("Succeed")) {
            JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
            if (vmGroups != null && vmGroups.size() > 0) {
                msg.setResult(true);
                return msg;
            }
        }
        //endregion

        //region 统计deployDetailResults中成功的VM 按角色
        JSONArray deployDetailResults = detailMsg.getJSONArray("deployDetailResults");

        if (deployDetailResults == null) {
            getLogger().warn("deployDetailResults结果为空。");
            msg.setResult(false);
            return msg;
        }


        int cntAmb = 0, cntMst = 0, cntCor = 0, cntTsk = 0;

        for (int i = 0; i < deployDetailResults.size(); i++) {
            JSONObject deploy = deployDetailResults.getJSONObject(i);

            if (deploy.containsKey("provisionState") &&
                    deploy.getString("provisionState").equalsIgnoreCase("Succeeded")) {
                String deployName = deploy.getString("deployName");
                if (deployName.startsWith(confCluster.getClusterName() + "-amb-")) {
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms != null && vms.size() > 0) {
                        cntAmb += vms.size();
                    }
                }

                if (deployName.startsWith(confCluster.getClusterName() + "-cor-")) {
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms != null && vms.size() > 0) {
                        cntCor += vms.size();
                    }
                }
                if (deployName.startsWith(confCluster.getClusterName() + "-mst-")) {
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms != null && vms.size() > 0) {
                        cntMst += vms.size();
                    }
                }
                if (deployName.startsWith(confCluster.getClusterName() + "-tsk-")) {
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms != null && vms.size() > 0) {
                        cntTsk += vms.size();
                    }
                }
            }
        }
        getLogger().info("cnAmb:{},cntMst:{},cntCor:{},cntTsk:{}", cntAmb, cntMst, cntCor, cntTsk);
        //endregion

        //region 统计provisionedVmGroups各个vmrole成功的数量
        JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
        Integer cntCore = 0, cntMaster = 0, cntAmbari = 0, cntTask = 0;

        for (int i = 0; i < vmGroups.size(); i++) {
            JSONObject group = vmGroups.getJSONObject(i);
            if (group != null && group.containsKey("groupName") && group.containsKey("count")) {
                String groupName = group.getString("groupName");
                Integer count = group.getInteger("count");
                switch (groupName) {
                    case "core":
                        cntCore = count;
                        break;
                    case "master":
                        cntMaster = count;
                        break;
                    case "ambari":
                        cntAmbari = count;
                        break;
                    case "task":
                        cntTask = count;
                        break;
                }
            }
        }
        getLogger().info("cntAmbari:{},cntMaster:{},cntCore:{},cntTask:{}", cntAmbari, cntMaster, cntCore, cntTask);
        //endregion

        //region 逻辑判断
        if (cntAmbari < cntAmb) {
            msg.setResult(false);
            msg.setErrorMsg("Ambari数据不完整。");
            return msg;
        }

        if (cntMaster < cntMst) {
            msg.setResult(false);
            msg.setErrorMsg("Master数据不完整。");
            return msg;
        }

        if (cntCore < cntCor) {
            msg.setResult(false);
            msg.setErrorMsg("Core数据不完整。");
            return msg;
        }

        if (cntTask < cntTsk) {
            msg.setResult(false);
            msg.setErrorMsg("Task数据不完整。");
            return msg;
        }

        //endregion

        msg.setResult(true);
        return msg;

    }

    /**
     * 检查ProvisionDetail 返回结果的VM是否完成DNS注册
     * 检查逻辑：解析hostname指向的A记录是否与返回的IP一致
     *
     * @param vmHostnames 数据来自 provisionedVmGroups.virtualMachines
     * @return 返回DNS未注册或DNSA记录不正确的VM
     */
    private List<VirtualMachineResponse> checkProvisionVMGroupsDNS(List<VirtualMachineResponse> vmHostnames) {
        List<VirtualMachineResponse> result = new ArrayList<>();
        try {
            //启动DNS前，先等待10s，给privateDNS生效时间
            ThreadUtil.sleep(1000 * 10);
            for (VirtualMachineResponse vm : vmHostnames) {
                String hostName = vm.getHostName();
                String ip = vm.getPrivateIp();
                String dnsip = DNSUtil.getIPByDNS(hostName);
                if (StringUtils.isEmpty(dnsip) || !dnsip.equalsIgnoreCase(ip)) {
                    getLogger().warn("DNS未注册或DNS解析IP与实际不符，DNSA记录：{},VM信息：{}", dnsip, vm);
                    result.add(vm);
                }
            }
        } catch (Exception e) {
            getLogger().error("检查DNS注册异常，", e);
        }
        return result;
    }

    private JSONArray checkProvisionVMGroupsDNSBak(JSONArray vms) {
        JSONArray errorJsonA = new JSONArray();
        try {
            //启动DNS前，先等待10s，给privateDNS生效时间
            ThreadUtil.sleep(1000 * 10);
            vms.stream().forEach(item -> {
                if (item instanceof JSONObject) {
                    JSONObject jsonitem = (JSONObject) item;
                    String hostName = jsonitem.getString("hostName");
                    String ip = jsonitem.getString("privateIp");
                    String dnsip = DNSUtil.getIPByDNS(hostName);
                    if (StringUtils.isEmpty(dnsip) || !dnsip.equalsIgnoreCase(ip)) {
                        getLogger().warn("DNS未注册或DNS解析IP与实际不符，DNSA记录：{},VM信息：{}", dnsip, item);
                        errorJsonA.add(item);
                    }
                }
            });
        } catch (Exception e) {
            getLogger().error("检查DNS注册异常，", e);
        }
        return errorJsonA;
    }


    /**
     * 保存DNS注册异常信息到ScalingTask Remark字段
     *
     * @param dnsErrorVms
     * @param vmjobId
     * @return
     */
    private ResultMsg saveDnsExpToTaskRemark(String dnsErrorVms, String vmjobId) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterVmJob infoClusterVmJob = vmJobMapper.getVmJobByJobId(vmjobId);
            if (null == infoClusterVmJob) {
                getLogger().error(" infoClusterVmJobMapper.getVmJobByJobId(jobId) result null {} ", vmjobId);
                msg.setResult(false);
                msg.setErrorMsg("not found job info,jobid:" + vmjobId);
                return msg;
            }

            //region 获取扩容任务信息
            InfoClusterOperationPlanActivityLog activityLog =
                    planActivityLogMapper.selectByPrimaryKey(infoClusterVmJob.getActivityLogId());
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLog.getPlanId());

            ConfScalingTask task = scalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());
            //endregion

            List<VirtualMachineResponse> vms = JSON.parseArray(dnsErrorVms, VirtualMachineResponse.class);
            if (CollectionUtil.isNotEmpty(vms)) {
                StringBuilder builder = new StringBuilder();
                vms.stream().forEach(item -> {
                    builder.append(item.getHostName()).append(" ").append(item.getPrivateIp()).append("\n");
                });
                task.appendRemark(builder.toString());
                scalingTaskMapper.updateByPrimaryKeySelective(task);
                msg.setResult(true);
            }
        } catch (Exception e) {
            getLogger().error("保存DNS异常信息到TaskRemark发生异常，", e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }


    /**
     * 构造创建集群申请资源Azure返回的结构体
     *
     * @param confCluster
     * @param jobId
     * @param data
     * @return
     */
    private JSONObject buildAzureCreateVmResponse(ConfCluster confCluster, String jobId, Object data) {
        JSONObject response = new JSONObject();
        response.put("id", jobId);
        response.put("name", "create-cluster-" + confCluster.getClusterName());
        response.put("type", "CreateVirtualMachines");
        response.put("status", "Completed");
        response.put("data", data);
        return response;
    }

    /**
     * 构造集群扩容申请资源Azure返回的结构体
     *
     * @param confCluster
     * @param jobId
     * @param data
     * @return
     */
    private JSONObject buildAzureScaleOutVmResponse(ConfCluster confCluster, String jobId, JobQueryResponse data) {
        JSONObject response = new JSONObject();
        response.put("id", jobId);
        response.put("name", "append-cluster-" + confCluster.getClusterName());
        response.put("type", "AppendVirtualMachines");
        response.put("status", "Completed");
        response.put("data", data);
        return response;
    }

    /**
     * 保存provisionDetail的到fullLog日志表
     *
     * @param cvmjobId
     * @param activityLog
     * @param confCluster
     * @param detailMsg
     * @return
     */
    private void saveProvisionDetailToFullLog(String cvmjobId, InfoClusterOperationPlanActivityLogWithBLOBs activityLog,
                                              ConfCluster confCluster, JSONObject detailMsg) {
        InfoClusterFullLogWithBLOBs fullLog = new InfoClusterFullLogWithBLOBs();
        fullLog.setPlanId(activityLog.getPlanId());
        fullLog.setClusterId(confCluster.getClusterId());
        fullLog.setRequestTime(new Date());
        fullLog.setRequestParam(cvmjobId);
        fullLog.setResponseBody(detailMsg.toJSONString());
        fullLogService.saveLog(fullLog);
    }

    /**
     * 发送Azure重试下消息
     *
     * @param retryParam 重试参数
     * @param currentLog 当前步骤对象
     * @return
     */
    private ResultMsg sendAzureCreateVMRetryMessage(JSONObject retryParam,
                                                    InfoClusterOperationPlanActivityLogWithBLOBs currentLog,
                                                    Long delay) {

        ResultMsg msg = new ResultMsg();
        try {
            // 获取上一步
            InfoClusterOperationPlanActivityLogWithBLOBs preActivityLog =
                    planExecService.getPreviousActivity(currentLog.getPlanId(), currentLog.getActivityLogId());
            //更新当前步骤为待执行
            currentLog.setState(0);
            planActivityLogMapper.updateByPrimaryKeySelective(currentLog);

            JSONObject parmJsobj = JSON.parseObject(preActivityLog.getParaminfo());
            parmJsobj.put(JobNameConstant.Param_Azure_Retry_obj, retryParam);
            preActivityLog.setParaminfo(JSON.toJSONString(parmJsobj));

            return planExecService.sendPrevActivityMsg(preActivityLog, delay);
        } catch (Exception e) {
            getLogger().error("发送Azure申请资源，retry消息异常，", e);
            msg.setResult(false);
            msg.setErrorMsg("发送Azure申请资源，retry消息异常，" + ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }

    /**
     * 调用Azure接口获取VM provisonDetail 信息 with retry
     *
     * @param jobId
     * @return
     */
    private JSONObject getAzureVMProvisionDetail(String jobId, String region) {
        Integer i = 0;
        while (true) {
            try {
                ResultMsg detailMsg = azureService.provisionDetail(jobId, region);
                if (detailMsg.getResult() && detailMsg.getData() != null) {
                    JSONObject datajson = JSON.parseObject(JSON.toJSONString(detailMsg.getData()));
                    return datajson;
                }
            } catch (Exception e) {
                getLogger().error("获取Azure provision detail 异常，", e);
            }
            i++;
            if (i > 3) {
                return null;
            }
            getLogger().info("获取Azure provision detail,重试 :" + i);
            ThreadUtil.sleep(1000 * 30);
        }
    }

    /**
     * 调用Azure Fleet接口获取VM provisonDetail 信息 with retry
     *
     * @param jobId
     * @param subscritionId
     * @return
     */
    private JSONObject getAzureFleetVMProvisionDetail(String jobId, String subscritionId) {
        Integer i = 0;
        while (true) {
            try {
//                ResultMsg detailMsg = azureFleetService.provisionDetail(jobId, subscritionId);
                // 基于Azure Fleet的ProvisionDetail接口未实现, 采用job接口来替代
                ResultMsg detailMsg = azureFleetService.getJobsStatusWithRequestTimeout(jobId, subscritionId);
                if (detailMsg.getResult() && detailMsg.getData() != null) {
                    JSONObject datajson = JSON.parseObject(JSON.toJSONString(detailMsg.getData()));
                    return datajson;
                }
            } catch (Exception e) {
                getLogger().error("获取Azure provision detail 异常，", e);
            }
            i++;
            if (i > 3) {
                throw new RuntimeException("获取Azure provision detail " + i + " 次后，仍然无法获取VM创建结果。");
            }
            getLogger().info("获取Azure provision detail,重试 :" + i);
            ThreadUtil.sleep(1000 * 10);
        }
    }

    /**
     * 处理创建或扩容任务的明细，判定是否可以需要重试或直接降级<br/>
     * 失败的节点中包含ambari 和 master节点的场景需要重试<br/>
     * 若遇到429 流控，需要sleep 默认180s分钟后再次重试<br/>
     * 若只有core或task节点，且失败的节点数量占比低于容错率直接调用节点删除接口剔除失败的VM，高于容错率需要删除后重试<br/>
     *
     * @param confCluster
     * @param detailMsg
     * @param operation   操作类型 create scaleout
     * @return ResultMsg
     * ResultMsg.result false 需要重试
     * true 降级成功，当前步骤认定为成功，流程继续
     */
    private ResultMsg processCreateOrScaleOutJobDetailMessage(ConfCluster confCluster,
                                                              JSONObject detailMsg,
                                                              String activityLogId,
                                                              String operation) {
        ResultMsg msg = new ResultMsg();

        JobQueryResponse queryResp = detailMsg.toJavaObject(JobQueryResponse.class);
        //是否有VM已经申请成功
        if (queryResp.getData().size() > 0) {
            //判断是否可以降级
            ResultMsg ckresult;
            if (operation.equalsIgnoreCase("create")) {
                ckresult = checkCreateJobReduce(confCluster, detailMsg);
            } else {
                ckresult = checkScaleOutJobReduce(detailMsg, activityLogId);
            }
            List<String> vmNames = new ArrayList<>();
            List<String> dnsNames = new ArrayList<>();
            for (VirtualMachineResponse vm : queryResp.getAllVms()) {
                vmNames.add(vm.getName());
                dnsNames.add(vm.getHostName());
            }
            // ckresult中, rows: 创建失败的VM
            getLogger().info("ckresult:" + ckresult);
            if (ckresult.getRows() != null && ckresult.getRows().size() > 0) {
                // 删除失败的VM, 若操作失败当前步骤失败
                getLogger().info("发起删除失败VM的请求：" + ckresult.getRows());
                ResultMsg delmsg = syncDeleteVms(vmNames, dnsNames, confCluster, activityLogId);
                getLogger().info("完成删除失败VM的请求。");
                if (delmsg.getResult()) {
                    getLogger().info("开始查询删除任务进度，jobid：" + delmsg.getBizid());
                    ResultMsg queryMsg = syncQueryVMDeleteJob(delmsg.getBizid(), confCluster.getSubscriptionId());
                    if (!queryMsg.getResult()) {
                        //删除查询失败
                        return queryMsg;
                    }
                    getLogger().info("完成查询删除任务进度。");
                } else {
                    return delmsg;
                }
            }
            // 返回false 重试逻辑，返回true 执行降级逻辑,Data 里为vmGroups 用于保存数据
            return ckresult;

        } else {
            if (checkFlowControl(detailMsg)) {
                //限流
                //发送 180s重试延时消息
                msg.setResult(false);
                msg.setActimes(180);
            } else {
                //发送重试 30s 延时消息
                msg.setResult(false);
                msg.setActimes(30);
            }
        }
        return msg;
    }

    /**
     * 判断是否流控
     *
     * @param detailMsg
     * @return
     */
    private boolean checkFlowControl(JSONObject detailMsg) {
        return false;
    }


    private ResultMsg checkCreateJobReduce(ConfCluster confCluster, JSONObject detailMsg) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            JobQueryResponse queryResp = detailMsg.toJavaObject(JobQueryResponse.class);
            // 已经完成的VM数量
            Integer cntAmbari = queryResp.getTotalVMCountByRole("ambari");
            Integer cntMaster = queryResp.getTotalVMCountByRole("master");
            Integer cntCore = queryResp.getTotalVMCountByRole("core");
            Integer cntTask = queryResp.getTotalVMCountByRole("task");
            // 失败的VM数量
            Integer fCntCore = queryResp.getVMCount(JobQueryResponse.STATE_FAILED, "core");
            Integer fCntTask = queryResp.getVMCount(JobQueryResponse.STATE_FAILED, "task");

            List<VirtualMachineResponse> failedVMs = queryResp.getAllFailVms();
            List<String> failedVmNames = failedVMs.stream().map(VirtualMachineResponse::getName).collect(Collectors.toList());
            resultMsg.setRows(failedVmNames);

            getLogger().info("cntAmbari:{},cntMaster:{},cntCore:{},cntTask:{},fCntCore:{},fCntTask:{}",
                    cntAmbari, cntMaster, cntCore, cntTask, fCntCore, fCntTask);

            Integer delayTimes = 30;
            //region 处理不可以降级的场景
            if (confCluster.getIsHa().equals(1)) {
                // 高可用失败的节点不能包含ambari 和master
                if (cntAmbari < 1 || cntMaster < 2) {
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    return resultMsg;
                }
            } else {
                // 非高可用失败的节点不能包含ambari
                if (cntAmbari < 1) {
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    return resultMsg;
                }
            }

            if (cntCore < 3) {
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                return resultMsg;
            }

            //region 失败节点占比大于容错率
            Double coreErrorRate = 0d;
            if (cntCore + fCntCore > 0) {
                coreErrorRate = fCntCore.doubleValue() / (cntCore + fCntCore);
            }
            Double taskErrorRate = 0d;
            if (cntTask + fCntTask > 0) {
                taskErrorRate = fCntTask.doubleValue() / (cntTask + fCntTask);
            }
            if (coreErrorRate > vmErrorRate || taskErrorRate > vmErrorRate) {
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                resultMsg.setErrorMsg("失败节点占比大于容错率。");
                return resultMsg;
            }
            //endregion

            //endregion
            resultMsg.setData(queryResp.getData());
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("检测VM申请资源异常是否可以降级异常，", e);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    /**
     * 创建集群申请资源申请，判断是否可以降级<br/>
     * 由于Azure Fleet的provisionDetail接口不实现, 通过Job接口返回数据.但是数据格式不一样,所以处理判断方式也发生变化.
     * 原来的方法作个备份便于后续问题查证.
     *
     * @param confCluster
     * @param detailMsg
     * @return 返回false，需要删除失败的VM，发送重试请求，返回ture 删除失败的节点
     */
    private ResultMsg checkCreateJobReduceBak(ConfCluster confCluster, JSONObject detailMsg) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            Integer delayTimes = 30;
            JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
            // 已经完成的VM数量
            Integer cntAmbari = 0, cntMaster = 0, cntCore = 0, cntTask = 0;
            // 失败的VM数量
            Integer fCntCore = 0, fCntTask = 0;

            JSONArray failedVMs = detailMsg.getJSONArray("failedVMs");
            List<String> failedVmList = new CopyOnWriteArrayList<>();

            //region 失败的vm分类统计
            for (int k = 0; k < failedVMs.size(); k++) {
                String vmName = failedVMs.getString(k);
                if (StringUtils.isNotEmpty(vmName)) {
                    String[] vmNamssplit = vmName.split("-");
                    String role = vmNamssplit[vmNamssplit.length - 2];
                    failedVmList.add(vmName);
                    switch (role) {
                        case "cor":
                            fCntCore++;
                            break;
                        case "tsk":
                            fCntTask++;
                            break;
                    }
                }
            }
            // 返回失败的VM
            resultMsg.setRows(failedVmList);
            //endregion

            //region 统计各个vmrole成功的数量
            for (int i = 0; i < vmGroups.size(); i++) {
                JSONObject group = vmGroups.getJSONObject(i);
                if (group != null && group.containsKey("groupName") && group.containsKey("count")) {
                    String groupName = group.getString("groupName");
                    Integer count = group.getInteger("count");
                    switch (groupName) {
                        case "core":
                            cntCore = count;
                            break;
                        case "master":
                            cntMaster = count;
                            break;
                        case "ambari":
                            cntAmbari = count;
                            break;
                        case "task":
                            cntTask = count;
                            break;
                    }
                }
            }
            //endregion

            getLogger().info("cntAmbari:{},cntMaster:{},cntCore:{},cntTask:{},fCntCore:{},fCntTask:{}",
                    cntAmbari, cntMaster, cntCore, cntTask, fCntCore, fCntTask);

            //region 处理不可以降级的场景
            if (confCluster.getIsHa().equals(1)) {
                // 高可用失败的节点不能包含ambari 和master
                if (cntAmbari < 1 || cntMaster < 2) {
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    return resultMsg;
                }
            }

            if (confCluster.getIsHa().equals(0)) {
                // 非高可用失败的节点不能包含ambari
                if (cntAmbari < 1) {
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    return resultMsg;
                }
            }

            if (cntCore < 3) {
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                return resultMsg;
            }

            //region 失败节点占比大于容错率
            Double coreErrorRate = 0d;
            if (cntCore + fCntCore > 0) {
                coreErrorRate = fCntCore.doubleValue() / (cntCore + fCntCore);
            }
            Double taskErrorRate = 0d;
            if (cntTask + fCntTask > 0) {
                taskErrorRate = fCntTask.doubleValue() / (cntTask + fCntTask);
            }
            if (coreErrorRate > vmErrorRate || taskErrorRate > vmErrorRate) {
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                resultMsg.setErrorMsg("失败节点占比大于容错率。");
                return resultMsg;
            }
            //endregion

            //endregion
            resultMsg.setData(vmGroups);
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("检测VM申请资源异常是否可以降级异常，", e);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    /**
     * 扩容申请资源异常，判断是否可以降级
     *
     * @param detailMsg
     * @param activityLogId
     * @return
     */
    public ResultMsg checkScaleOutJobReduce(JSONObject detailMsg, String activityLogId) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            JobQueryResponse queryResp = detailMsg.toJavaObject(JobQueryResponse.class);
            Integer delayTimes = 30;

            // 获取所有创建成功的VM
            List<VirtualMachineResponse> allSuccessVms = queryResp.getAllSuccessVms();

            if (CollectionUtil.isNotEmpty(allSuccessVms)) {
                List<VirtualMachineResponse> dnsErrorVms = checkProvisionVMGroupsDNS(allSuccessVms);
                if (CollectionUtil.isNotEmpty(dnsErrorVms)) {
                    //返回 DNS注册异常的VMs
                    resultMsg.setExt2(JSON.toJSONString(dnsErrorVms));
                }
            } else {
                getLogger().warn("Provision接口返回结果中无可用的成功节点。");
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                resultMsg.setErrorMsg("Provision接口返回结果中无可用的成功节点。");
                return resultMsg;
            }

            // 获取所有创建失败的VM
            List<VirtualMachineResponse> allFailVms = queryResp.getAllFailVms();
            getLogger().info("cntCompleted:{},failedCnt:{}", allSuccessVms.size(), allFailVms.size());
            Integer failedCnt = allFailVms.size();
            Integer cntCompleted = allSuccessVms.size();;

            //region 失败节点占比大于容错率
            Double errorRate = 0d;
            if (failedCnt + cntCompleted > 0) {
                errorRate = failedCnt.doubleValue() / (failedCnt + cntCompleted);
            }

            // 失败率大于容错率且实例组类型为ondemand
            if (errorRate > vmErrorRate) {
                InfoClusterOperationPlanActivityLogWithBLOBs activityLogWithBLOBs
                        = planActivityLogMapper.selectByPrimaryKey(activityLogId);
                InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLogWithBLOBs.getPlanId());
                ConfScalingTask scalingTask = confScalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());
                // 非竞价类型的扩容需要考虑失败率
                if (!scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                    getLogger().warn("失败节点占比大于容错率");
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    resultMsg.setData(queryResp.getData());
                    resultMsg.setErrorMsg("失败节点占比大于容错率。");
                    return resultMsg;
                }
            }
            //endregion

            resultMsg.setData(queryResp.getData());
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("检测VM申请资源异常是否可以降级异常，", e);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    public ResultMsg checkScaleOutJobReduceBak(JSONObject detailMsg, String activityLogId) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            Integer delayTimes = 30;
            JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
            // 已经完成的VM数量
            Integer cntCompleted = 0;
            // 失败的VM数量
            Integer failedCnt = 0;

            JSONArray failedVMs = detailMsg.getJSONArray("failedVMs");
            List<String> failedVmList = new CopyOnWriteArrayList<>();

            for (int i = 0; i < failedVMs.size(); i++) {
                failedVmList.add(failedVMs.getString(i));
            }
            resultMsg.setRows(failedVmList);
            failedCnt = failedVMs.size();

            if (vmGroups.size() > 0 &&
                    vmGroups.getJSONObject(0).containsKey("count") &&
                    vmGroups.getJSONObject(0).getInteger("count") > 0) {
                JSONObject group = vmGroups.getJSONObject(0);
                cntCompleted = group.getInteger("count");
                //region 检查VM是否完成DNS注册
                if (sdpCreateVMProvisionCheckDNS.equals(1) && group.containsKey("virtualMachines")) {
                    JSONArray vms = group.getJSONArray("virtualMachines");
                    if (vms != null && vms.size() > 0) {
//                        JSONArray dnsErrorVms = checkProvisionVMGroupsDNS(vms);
//                        JSONArray dnsErrorVms = checkProvisionVMGroupsDNS(null);
//                        if (dnsErrorVms != null && dnsErrorVms.size() > 0) {
//                            //返回 DNS注册异常的VMs
//                            resultMsg.setExt2(dnsErrorVms.toJSONString());
//                        }
                    }
                }
                //endregion 检查VM是否完成DNS注册
            } else {
                getLogger().warn("Provision接口返回结果中无可用的成功节点。");
                resultMsg.setResult(false);
                resultMsg.setActimes(delayTimes);
                resultMsg.setErrorMsg("Provision接口返回结果中无可用的成功节点。");
                return resultMsg;
            }

            getLogger().info("cntCompleted:{},failedCnt:{}", cntCompleted, failedCnt);

            //region 失败节点占比大于容错率
            Double ErrorRate = 0d;
            if (failedCnt + cntCompleted > 0) {
                ErrorRate = failedCnt.doubleValue() / (failedCnt + cntCompleted);
            }

            // 失败率大于容错率且实例组类型为ondemand
            if (ErrorRate > vmErrorRate) {
                InfoClusterOperationPlanActivityLogWithBLOBs activityLogWithBLOBs
                        = planActivityLogMapper.selectByPrimaryKey(activityLogId);
                InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLogWithBLOBs.getPlanId());
                ConfScalingTask scalingTask = confScalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());
                // 非竞价类型的扩容需要考虑失败率
                if (!scalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                    getLogger().warn("失败节点占比大于容错率");
                    resultMsg.setResult(false);
                    resultMsg.setActimes(delayTimes);
                    resultMsg.setData(vmGroups);
                    resultMsg.setErrorMsg("失败节点占比大于容错率。");
                    return resultMsg;
                }
            }
            //endregion

            resultMsg.setData(vmGroups);
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("检测VM申请资源异常是否可以降级异常，", e);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    /**
     * 同步批量删除VM
     *
     * @param vmNames
     * @return
     */
    private ResultMsg syncDeleteVms(List<String> vmNames, List<String> dnsNames, ConfCluster confCluster, String activityLogId) {

        ResultMsg resultMsg = new ResultMsg();

        //region 生成vm任务
        InfoClusterVmJob vmJob =
                getVmJob(confCluster.getClusterId()
                        , confCluster.getClusterName()
                        , InfoClusterOperationPlan.Plan_OP_DeleteFailedVMs
                        , activityLogId);
        //endregion

        //region 构建删除虚拟机请求体
        AzureDeleteVMsRequest deleteVMsRequest = buildAzureVmsDeleteReqByVMNames(vmJob, vmNames, dnsNames);
        getLogger().info("delete_FailedVMs_Request:" + deleteVMsRequest.toString());
        //endregion

        //region 请求azure接口
        ResultMsg msg = deleteVirtualMachines(deleteVMsRequest, confCluster.getSubscriptionId());
        getLogger().info(confCluster.getClusterName() + "delete_FailedVMs_Request,--end_deleteVms");
        getLogger().info("delete_FailedVMs_Request,Result:" + msg);
        //endregion

        if (msg.getResult() && msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            String jobid = response.getString("id");
            resultMsg.setResult(true);
            resultMsg.setBizid(jobid);
        } else {
            resultMsg.setErrorMsg(msg.getErrorMsg());
        }

        return resultMsg;
    }

    /**
     * 同步查询删除VM任务结果
     *
     * @param jobId
     * @return
     */
    private ResultMsg syncQueryVMDeleteJob(String jobId, String subscriptionId) {
        ResultMsg resultMsg = new ResultMsg();

        Long begTime = System.currentTimeMillis() / 1000;
        while (true) {
            try {
                ResultMsg msg = azureService.getJobsStatus(jobId, subscriptionId);
                if (msg.getData() != null) {
                    JSONObject response = (JSONObject) msg.getData();
                    if (response.containsKey("status")) {
                        if (response.getString("status").equalsIgnoreCase("Completed")) {
                            resultMsg.setResult(true);
                            return resultMsg;
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().error("查询删除FailedVMs任务结果异常,", e);
            }

            Long endTime = System.currentTimeMillis() / 1000;

            if ((endTime - begTime) > deleteFailedVMsQueryDuration) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("删除FailedVMs任务结果查询超时。");
                getLogger().error("删除FailedVMs任务结果查询超时");
                return resultMsg;
            }
            ThreadUtil.sleep(1000 * 10);
        }
    }

    /**
     * 查询虚拟机扩容任务状态
     *
     * @param messageParam
     * @return
     */
    @Override
    public ResultMsg queryVmsAppendJob(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String cvmjobId = param.getString(JobNameConstant.Cluster_VM);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        ResultMsg msg = azureService.getJobsStatus(cvmjobId, confCluster.getSubscriptionId());

        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            JobQueryResponse queryResp = convertJsonResult(response);

            if (response.containsKey("status")) {
                if (queryResp.isSuccessed()) {
                    String lockkey = LockPrefixConstant.Save_Cluster_VM + cvmjobId;
                    boolean lock = redisLock.tryLock(lockkey, TimeUnit.SECONDS, 0, 300);
                    if (lock) {
                        ResultMsg savemsg = saveClusterAppendVminfo(response.toJSONString());
                        if (lock) {
                            try {
                                redisLock.unlock(lockkey);
                            } catch (Exception e) {
                                getLogger().error("锁释放异常，", e);
                            }
                        }

                        if (savemsg.getResult()) {
                            //发送下一个环节的消息
                            ResultMsg sendmasg = planExecService.sendNextActivityMsg(activityLogId, param);
                            if (sendmasg.getResult()) {
                                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                                currentLog.setLogs(Convert.toStr(savemsg.getData()));
                                msg.setResult(true);
                            } else {
                                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                                currentLog.setLogs(sendmasg.getErrorMsg() + Convert.toStr(savemsg.getData()));
                                msg.setResult(false);
                                msg.setErrorMsg("发送消息失败。");
                            }

                        } else {
                            currentLog.setLogs(savemsg.getErrorMsg() + Convert.toStr(savemsg.getData()));
                            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        }
                        planExecService.complateActivity(currentLog);
                        return msg;
                    } else {
                        msg.setResult(false);
                        getLogger().error("重复保存。");
                    }
                    return msg;
                }

                if (queryResp.isFailed()) {
                    Long ttl = System.currentTimeMillis() - currentLog.getBegtime().getTime();
                    //失败过快，等一等
                    if (ttl < 1000 * 75) {
                        getLogger().warn("失败速度过快，sleep 60s");
                        resultMsg = planExecService.loopActivity(clientname, messageParam,
                                60l, activityLogId);
                        return resultMsg;
                    }

                    // 降级or重试
                    // AzureFleet在查询失败后，不进行重试或降级
//                    ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "scaleout");
//                    if (rrMsg.getResult()) {
//                        return rrMsg;
//                    }
                    // 请求申请资源失败, 删除所有请求的VM,主要是因为AzureFleet不清不行, 失败的VM仍然占用Capacity
                    syncDeleteAllRequestVms(response, confCluster, currentLog);

                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs(response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                if (response.getString("status").equalsIgnoreCase("unknown")) {
                    // 继续查询
                    getLogger().warn("集群扩容查询资源创建结果未知：" + messageParam);
                    resultMsg = planExecService.loopActivity(clientname, messageParam,
                            30l, activityLogId);
                    return resultMsg;
                }
                //region 集群扩容VM查询任务超时，查询detail 处理卡住的VM
                Long duri = new Date().getTime() / 1000 - currentLog.getBegtime().getTime() / 1000;

                if (duri > vmProvisionQueryTimeOut) {
                    // AzureFleet超时后，不进行重试或降级，直接失败
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT);
                    currentLog.setLogs("向Azure查询申请VM状态超时： \n" + response.toJSONString());
                    return planExecService.complateActivity(currentLog);
//                    ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "scaleout");
//                    if (rrMsg.getResult()) {
//                        return rrMsg;
//                    }
                }
                //endregion
                // 继续查询
                resultMsg = planExecService.loopActivity(clientname, messageParam,
                        10l, activityLogId);
                return resultMsg;
            } else {
                // 降级or重试
                // AzureFleet返回报文格式不正确时，不进行重试或降级
//                ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "scaleout");
//                if (rrMsg.getResult()) {
//                    return rrMsg;
//                }

                getLogger().error("集群扩容查询资源创建结果异常：" + messageParam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            // AzureFleet返回报文格式不正确时，不进行重试或降级
//            ResultMsg rrMsg = createOrScaleOutJobRetryOrReduce(cvmjobId, currentLog, confCluster, param, "scaleout");
//            if (rrMsg.getResult()) {
//                return rrMsg;
//            }
            getLogger().error("集群扩容查询资源创建结果异常：" + messageParam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(msg.toString());
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    @Override
    public ResultMsg ambariAddPart(String message) {
        getLogger().info("磁盘扩容开始,{}", message);
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = new InfoClusterOperationPlanActivityLogWithBLOBs();
        try {
            JSONObject param = JSON.parseObject(message);
            String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
            String taskId = param.getString(ComposeConstant.Task_ID);
            //endregion

            //region 查询task信息
            ConfScalingTask task = confScalingTaskMapper.selectByPrimaryKey(taskId);
            getLogger().info("task add part,task:{}", task);
            //endregion

            //region 获取aciton数据
            currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            if (currentLog.getBegtime() == null) {
                currentLog.setBegtime(new Date());
            }
            //endregion

            //region task 数据校验
            if (task == null) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("task为空");
                throw new RuntimeException("task为空");
            }

            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(task.getClusterId());

            //region 生成vm任务
            InfoClusterVmJob vmJob =
                    getVmJob(confCluster.getClusterId()
                            , confCluster.getClusterName()
                            , InfoClusterOperationPlan.Plan_OP_Part_ScaleOut
                            , activityLogId);
            // endregion

            // region 构建缩容删除虚拟机请求体
            AzureAddPartRequest addPartRequest = buildAzureAddPartRequest(vmJob, task.getTaskId(), task.getGroupName(), task.getVmRole(), task.getScalingCount());
            addPartRequest.setRegion(confCluster.getRegion());
            addPartRequest.setSubscriptionId(confCluster.getSubscriptionId());
            getLogger().info("addPartRequest:{}", addPartRequest);
            // endregion

            // region 请求azure接口
            Map<String, Object> azureApiLogs = new HashMap<>();
            azureApiLogs.put("clusterId", confCluster.getClusterId());
            azureApiLogs.put("activityLogId", activityLogId);
            azureApiLogs.put("planId", currentLog.getPlanId());
            threadLocal.set(azureApiLogs);
            ResultMsg msg = azureService.addPart(addPartRequest);
            getLogger().info(confCluster.getClusterName() + "addPartRequest,--end_addPart");
            // endregion

            if (msg.getResult() && msg.getData() != null) {
                JSONObject response = (JSONObject) msg.getData();
                String jobid = response.getString("id");
                param.put(JobNameConstant.Saleout_Part_Job, jobid);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                getLogger().info(confCluster.getClusterName() + "--end_sendNextmsg");
            } else {
                currentLog.setLogs(msg.toString());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            //endregion
            getLogger().info("磁盘扩容完成");
            return ResultMsg.SUCCESS();
        } catch (Exception ex) {
            getLogger().error("磁盘扩容异常", ex);
            return ResultMsg.FAILURE(ex.getMessage());
        } finally {
            planExecService.complateActivity(currentLog);
        }
    }

    /**
     * 构建磁盘扩容的消息体
     *
     * @param vmJob        job信息
     * @param taskId       扩容任务ID
     * @param groupName    实例组
     * @param vmRole       实例角色
     * @param scalingCount 扩容大小
     * @return
     */
    private AzureAddPartRequest buildAzureAddPartRequest(InfoClusterVmJob vmJob, String taskId, String groupName, String vmRole, Integer scalingCount) {
        getLogger().info("buildAzureAddPartRequest vmJob:{},taskId:{},groupName:{},vmRole:{},scalingCount:{}",
                vmJob,
                taskId,
                groupName,
                vmRole,
                scalingCount);
        AzureAddPartRequest request = new AzureAddPartRequest();
        request.setApiVersion("1.0");
        request.setTransactionId(vmJob.getTransactionId());
        request.setClusterName(vmJob.getClusterName());

        getLogger().info("confScalingVmMapper selectByTaskId:{}", taskId);
        List<ConfScalingVm> confScalingVms = confScalingVmMapper.selectByTaskId(taskId);
        List<String> vmNames = confScalingVms.stream().map(p -> p.getVmName()).collect(Collectors.toList());
        request.setVmNames(vmNames);
        request.setNewDataDiskSizeGB(scalingCount);
        return request;
    }

    @Override
    public ResultMsg queryAddPartStatus(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String dvmjobId = param.getString(JobNameConstant.Saleout_Part_Job);
        String activityLogId = param.getString("activityLogId");

//        String region = param.getString("region");
        String taskId = param.getString(ComposeConstant.Task_ID);

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        ResultMsg msg = azureService.getJobsStatus(dvmjobId, confCluster.getSubscriptionId());
        getLogger().info("查询磁盘扩容状态返回:{}", JSONUtil.toJsonStr(msg));
        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            if (response.containsKey("status")) {
                if (response.getString("status").equalsIgnoreCase("Completed")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(activityLogId, param);
                    return resultMsg;
                }

                // 销毁虚拟机操作azure 返回失败
                if (response.getString("status").equalsIgnoreCase("Failed")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs("磁盘扩容失败，调用Azure接口返回了【失败】结果，dvmJobId：" + dvmjobId + "，Azure返回：" + response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                if (response.getString("status").equalsIgnoreCase("unknown")) {
                    // 继续查询
                    getLogger().warn("磁盘扩容失败，调用Azure接口返回了【未知】结果，dvmJobId：" + dvmjobId + "，Azure返回：" + response.toJSONString());
                    resultMsg = planExecService.loopActivity(clientname, messageParam,
                            30l, activityLogId);
                    return resultMsg;
                }

                resultMsg = planExecService.loopActivity(clientname, messageParam,
                        10L, activityLogId);
                return resultMsg;
            } else {
                getLogger().error("磁盘扩容处理失败,Azure返回:{},message:{}", response, messageParam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("磁盘扩容处理失败，dvmJobId：" + dvmjobId + "，Azure返回：" + response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            getLogger().error("磁盘扩容处理发生异常 dvmjobId:{},message:{}", dvmjobId, messageParam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("磁盘扩容处理发生异常，dvmJobId：" + dvmjobId + "，msg：" + msg);
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    /**
     * 查询虚拟机缩容任务vm删除任务状态
     *
     * @param messageParam
     * @return
     */
    @Override
    public ResultMsg queryScaleInVmsDeleteJob(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String dvmjobId = param.getString(JobNameConstant.Delete_VM);
        String activityLogId = param.getString("activityLogId");
        String taskId = param.getString(ComposeConstant.Task_ID);

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        // vm 已经删除或不存在的情况下
        if (dvmjobId.equals("000000")) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            //更新集群状态
            //infoClusterVmMapper.updateVmsStatusByScaleInTaskId(cluster.getClusterId(), taskId, InfoClusterVm.VM_DELETED);
            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(activityLogId, param);
            return resultMsg;
        }
        ResultMsg msg = azureService.getJobsStatus(dvmjobId, cluster.getSubscriptionId());

        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            if (response.containsKey("status")) {
                if (response.getString("status").equalsIgnoreCase("Completed")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    // 更新虚拟机状态
                    infoClusterVmMapper.updateVmsStatusByScaleInTaskId(cluster.getClusterId(),
                            taskId,
                            InfoClusterVm.VM_DELETED);
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(activityLogId, param);
                    return resultMsg;
                }

                // 销毁虚拟机操作azure 返回失败
                if (response.getString("status").equalsIgnoreCase("Failed") ||
                        response.getString("status").equalsIgnoreCase("unknown")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs("销毁进程查询返回失败：" + response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                resultMsg = planExecService.loopActivity(clientname, messageParam,
                        10L, activityLogId);
                return resultMsg;
            } else {
                getLogger().error("集群扩容查询资源创建结果异常：" + messageParam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            getLogger().error("集群扩容查询资源创建结果异常：" + messageParam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(msg.toString());
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    /**
     * 查询虚拟机清理任务vm删除任务状态
     *
     * @param messageParam
     * @return
     */
    @Override
    public ResultMsg queryClearVmsDeleteJob(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String dvmjobId = param.getString(JobNameConstant.Delete_VM);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        // vm 已经删除或不存在的情况下
        // 上一步骤会首先使用异步删除, 如果异步删除成功, 传6个0给当前步骤,所以当发现有00000时,
        // 说明删除操作是异步的,不需要再去向Azure查询了
        if (dvmjobId.equals("000000")) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            //更新集群状态
            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(activityLogId, param);
            return resultMsg;
        }

        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        ResultMsg msg = azureService.getJobsStatus(dvmjobId, confCluster.getSubscriptionId());

        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());

        // 上一个创建或扩容流程的planID 存在清理任务的Plan表 opTaskId字段
        List<InfoClusterVmReject> vmRejectList = vmRejectMapper.getVmRejectsByPlanId(plan.getOpTaskId());

        List<String> vmNames = new ArrayList<>();

        vmRejectList.stream().forEach(x -> {
            vmNames.add(x.getVmName());
        });

        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            if (response.containsKey("status")) {
                if (response.getString("status").equalsIgnoreCase("Completed")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    // 更新虚拟机状态
                    infoClusterVmMapper.batchUpdateVMState(plan.getClusterId(), vmNames, InfoClusterVm.VM_DELETED);
                    vmRejectMapper.updateVmRejectsByPlanId(plan.getOpTaskId());
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(activityLogId, param);
                    return resultMsg;
                }

                // 销毁虚拟机操作azure 返回失败
                if (response.getString("status").equalsIgnoreCase("Failed")
                        || response.getString("status").equalsIgnoreCase("unknown")) {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs("销毁进程查询返回失败：" + response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                resultMsg = planExecService.loopActivity(clientname, messageParam,
                        10L, activityLogId);
                return resultMsg;
            } else {
                getLogger().error("集群查询资源创建结果异常：" + messageParam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            getLogger().error("集群查询资源结果异常：" + messageParam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(msg.toString());
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    /**
     * 虚拟机信息公共保存方法
     *
     * @param activityLogId
     * @param cvmjobId
     * @param message
     * @return
     */
    @Override
    public ResultMsg commonSaveVMInfo(String activityLogId, String cvmjobId, JSONObject message) {
        ResultMsg msg = new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planActivityLogMapper.selectByPrimaryKey(activityLogId);

        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        String lockkey = LockPrefixConstant.Save_Cluster_VM + cvmjobId;
        String actionendkey = "actend_" + confCluster.getClusterId();
        boolean lock = redisLock.tryLock(lockkey, TimeUnit.SECONDS, 0, 300);
        try {
            getLogger().info(" commonSaveVMInfo 拿锁结果 {} ", lock);
            if (lock) {
                //region 存在补偿集成，判断是否可以执行防止重复执行
                String flag = redisLock.getValue(actionendkey);
                getLogger().info("actionendkey,flag:" + actionendkey + "," + flag);
                if (StringUtils.isNotEmpty(flag) && !message.containsKey("retry_time")) {
                    msg.setResult(true);
                    getLogger().info("activityLogId：" + activityLogId + "，已执行完成，无需重复执行。");
                    return msg;
                }
                //endreigon

                ResultMsg savemsg = saveClusterVmInfo(message.toJSONString());
                if (savemsg.getResult()) {
                    //发送下一个环节的消息
                    JSONObject param = new JSONObject();
                    param.put("clusterId", savemsg.getExt1());

                    ResultMsg sendmasg = planExecService.sendNextActivityMsg(activityLogId, param);
                    if (sendmasg.getResult()) {
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        currentLog.setLogs(Convert.toStr(savemsg.getData()));
                        //完成设置标记
                        redisLock.save(actionendkey, "1", 900);
                        getLogger().info("保存完成，设置redisflag");
                    } else {
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        currentLog.setLogs(savemsg.getData() + sendmasg.getErrorMsg());
                    }
                } else {
                    currentLog.setLogs(Convert.toStr(savemsg.getData()) + savemsg.getErrorMsg());
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                }
                getLogger().info("保存查询Azure Job结果后, Logs字段存储内容为: {}", currentLog.getLogs());
                planExecService.complateActivity(currentLog);
                msg.setResult(true);
                return msg;
            }
        } catch (Exception e) {
            getLogger().error("commonSaveVMInfo,", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
            planExecService.complateActivity(currentLog);
            msg.setResult(false);
            msg.setErrorMsg("commonSaveVMInfo Exception" + e.getMessage());
        } finally {
            try {
                if (lock) {
                    redisLock.unlock(lockkey);
                }
            } catch (Exception e) {
                getLogger().error("释放锁异常，", e);
            }
        }
        return msg;
    }


    /**
     * 保存虚拟机信息
     *
     * @param message 查询接口返回的数据或 订阅servicebus得到的消息数据
     * @return
     */
    @Override
    public ResultMsg saveClusterVmInfo(String message) {
        ResultMsg msg = new ResultMsg();
        JobQueryResponse jobQueryResponse = null;
        getLogger().info("保存虚拟机信息 saveClusterVmInfo 传入的报文 {}", message);
        try {
            jobQueryResponse = JSONObject.parseObject(message, JobQueryResponse.class);
        } catch (Exception e) {
            getLogger().error("创建vm完成之后，将数据保存出现异常 {}", e);
        }
        if (null == jobQueryResponse || !"Completed".equals(jobQueryResponse.getStatus())) {
            getLogger().info("jobQueryResponse is null or jobQueryResponse.getStatus() is not Completed, return");
            msg.setResult(false);
            msg.setErrorMsg("job status is not completed.");
            return msg;
        }

        // 查询 infoClusterVmJob
        String jobId = jobQueryResponse.getId();
        InfoClusterVmJob infoClusterVmJob = vmJobMapper.getVmJobByJobId(jobId);
        if (null == infoClusterVmJob) {
            getLogger().error(" infoClusterVmJobMapper.getVmJobByJobId(jobId) result null {} ", jobId);
            msg.setResult(false);
            msg.setErrorMsg("not found job info,jobid:" + jobId);
            return msg;
        }
        getLogger().info("保存虚拟机信息 saveClusterVmInfo 查询 infoClusterVmJob 结果 {}", JSONObject.toJSONString(infoClusterVmJob));
        String clusterId = infoClusterVmJob.getClusterId();

        String transactionId = infoClusterVmJob.getTransactionId();
        Date begTime = infoClusterVmJob.getBegTime();
        Date endTime = new Date();

        // 查询 conf_cluster_vm
        List<ConfClusterVm> confClusterVmList = vmMapper.getVmConfs(clusterId);
        if (CollectionUtils.isEmpty(confClusterVmList)) {
            getLogger().error(" confClusterVmMapper.getVmConfs(clusterId) result empty {} ", clusterId);
            msg.setResult(false);
            msg.setErrorMsg("not fount cluster config info,clusterId:" + clusterId);
            return msg;
        }
        getLogger().info(" 查询 conf_cluster_vm 结果 {} ", JSONObject.toJSONString(confClusterVmList));

        // 查询 conf_host_group_vm_sku
        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByClusterId(clusterId);
        if (CollectionUtils.isEmpty(confHostGroupVmSkus)) {
            getLogger().error(" confHostGroupVmSkuMapper.selectByClusterId(clusterId) result empty {} ", clusterId);
            msg.setResult(false);
            msg.setErrorMsg("not fount cluster config sku info,clusterId:" + clusterId);
            return msg;
        }
        getLogger().info(" 查询 conf_host_group_vm_sku 结果 {} ", JSONObject.toJSONString(confHostGroupVmSkus));

        /**
         * 查询 conf cluster
         * */
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);
        if (null == confCluster) {
            getLogger().error("查询 conf cluster null ");
            msg.setResult(false);
            msg.setErrorMsg("not fount cluster config info info ,clusterId:" + clusterId);
            return msg;
        }
        Integer isHa = confCluster.getIsHa();// 1=高可用
        boolean isHaOk = 1 == isHa.intValue();
        /**
         * 保存 info_cluster
         * */
        InfoCluster infoCluster = new InfoCluster();
        infoCluster.setClusterId(clusterId);
        infoCluster.setAmbariUsername("");
        infoCluster.setAmbariPassword("");

        List<VirtualMachineGroupResponse> data = jobQueryResponse.getData();

        List<VirtualMachineGroupResponse> ambariVmsGroup = data.stream()
                .filter(virtualMachineGroupResponse -> "ambari".equals(virtualMachineGroupResponse.getGroupName()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ambariVmsGroup)) {
            List<VirtualMachineResponse> ambariMachList = ambariVmsGroup.get(0).getVirtualMachines();
            if (!CollectionUtils.isEmpty(ambariMachList)) {
                VirtualMachineResponse virtualMachineResponseAmbari = ambariMachList.get(0);
                infoCluster.setAmbariHost(virtualMachineResponseAmbari.getPrivateIp()); //设置host
            }
        }

        infoCluster.setClusterCreateBegtime(begTime);
        infoCluster.setClusterCreateEndtime(endTime);
        getLogger().info(" 更新cluster info {} ", JSONObject.toJSONString(infoCluster));

        //region 构造infoclustervm数据，保存
        List<String> masterIpList = new ArrayList<>();
        List<InfoClusterVm> toSaveInfoClusterVmList = new ArrayList<>();
        data.forEach(virtualMachineGroupResponse -> {
            List<VirtualMachineResponse> virtualMachineResponseList = virtualMachineGroupResponse.getVirtualMachines();

            virtualMachineResponseList.forEach(virtualMachineResponse -> {
                ConfHostGroupVmSku confVmSku = getConfVmSku(confClusterVmList,confHostGroupVmSkus, virtualMachineResponse);
                if (confVmSku == null) {
                    getLogger().error("保存vm数据未获取到配置信息。");
                    return;
                }
                InfoClusterVm infoClusterVm = new InfoClusterVm();
                infoClusterVm.setClusterId(clusterId);
                infoClusterVm.setVmName(virtualMachineResponse.getName());
                infoClusterVm.setVmid(virtualMachineResponse.getUniqueId());
                infoClusterVm.setVmConfId(confVmSku.getVmConfId());
                infoClusterVm.setHostName(virtualMachineResponse.getHostName().toLowerCase());
                infoClusterVm.setInternalip(virtualMachineResponse.getPrivateIp());
                infoClusterVm.setDefaultUsername(vmusername); //todo
                infoClusterVm.setState(InfoClusterVm.VM_RUNNING);
                infoClusterVm.setVmid(virtualMachineResponse.getUniqueId());
//                getSpotPrice(infoClusterVm, virtualMachineResponse,confCluster.getRegion(),confCluster.getSubscriptionId());
                infoClusterVm.setSkuName(confVmSku.getSku());
                complateVmPrice(infoClusterVm, virtualMachineResponse, confCluster.getRegion());
                String vmRole = confVmSku.getVmRole().toLowerCase();
                infoClusterVm.setVmRole(vmRole);
                if (isHaOk) {
                    if ("master".equals(vmRole)) {
                        masterIpList.add(virtualMachineResponse.getPrivateIp());
                    }
                } else {
                    if ("ambari".equals(vmRole)) {
                        masterIpList.add(virtualMachineResponse.getPrivateIp());
                    }
                }

                infoClusterVm.setGroupName(confVmSku.getGroupName());
                infoClusterVm.setGroupId(confVmSku.getGroupId());
                infoClusterVm.setPurchaseType(null == confVmSku.getPurchaseType() ? "" : confVmSku.getPurchaseType().toString());
                infoClusterVm.setImageid(confVmSku.getOsImageid());
                infoClusterVm.setCreateTranscationId(transactionId);
                infoClusterVm.setCreateJobId(jobId);
                infoClusterVm.setCreateBegtime(begTime);
                infoClusterVm.setCreateEndtime(endTime);
                toSaveInfoClusterVmList.add(infoClusterVm);
            });
        });

        List<InfoClusterVm> infoClusterVmList = infoClusterVmMapper.selectByClusterId(clusterId);
        if (CollectionUtils.isEmpty(infoClusterVmList)) {
            getLogger().info("保存虚拟机，未查询到数据，执行批处理进行保存");
            infoClusterVmMapper.insertBatch(toSaveInfoClusterVmList);
        }
        //endregion 构造infoclustervm数据,保存


        //region 保存infocluster
        Map<String, List<InfoClusterVm>> rolemap = toSaveInfoClusterVmList.stream().collect(Collectors.groupingBy(x -> {
            return x.getVmRole();
        }));

        if (rolemap.containsKey("task")) {
            infoCluster.setTaskVmsCount(rolemap.get("task").size());
        } else {
            infoCluster.setTaskVmsCount(0);
        }
        if (rolemap.containsKey("master")) {
            infoCluster.setMasterVmsCount(rolemap.get("master").size());
        } else {
            infoCluster.setMasterVmsCount(0);
        }
        infoCluster.setCoreVmsCount(rolemap.get("core").size());
        infoCluster.setAmbariCount(1);

        String masterIps = masterIpList.stream().collect(Collectors.joining(","));
        infoCluster.setMasterIps(masterIps);
        infoClusterMapper.updateByPrimaryKeySelective(infoCluster);

        //endregion

        getLogger().info(" 批量保存vmlist {} ", JSONObject.toJSONString(toSaveInfoClusterVmList));

        // 如果有申请错误的信息,记录下来错误详情
        if (Objects.nonNull(jobQueryResponse.getMessage())) {
            msg.setData(JSON.toJSONString(jobQueryResponse.getMessage()));
        }

        // 活动ID返回
        msg.setBizid(infoClusterVmJob.getActivityLogId());
        msg.setExt1(clusterId);
        msg.setResult(true);
        return msg;
    }
    /**
     * 获取ConfHostGroupVmSku
     *
     * @param confHostGroupVmSkus    配置信息列表
     * @param virtualMachineResponse 返回的请求
     * @return
     */
    private ConfHostGroupVmSku getConfVmSku(List<ConfClusterVm> confClusterVms,List<ConfHostGroupVmSku> confHostGroupVmSkus, VirtualMachineResponse virtualMachineResponse) {
        try {
            String groupId = virtualMachineResponse.getTags().get("sdp-groupid");
            if (StrUtil.isBlank(groupId)) {
                groupId = virtualMachineResponse.getTags().get("sdp-groupId");
            }
            String sku = virtualMachineResponse.getVmSize();

            String finalGroupId = groupId; // 下面的Lambda表达式用到了groupId,所以此处重新赋值一次

            Optional<ConfClusterVm> confClusterVm = confClusterVms.stream().filter(x -> {
                return x.getGroupId().equalsIgnoreCase(finalGroupId);
            }).findFirst();

            if (confClusterVm.isPresent()) {
                ConfClusterVm clusterVm = confClusterVm.get();
                Optional<ConfHostGroupVmSku> confHostGroupVmSku = confHostGroupVmSkus.stream().filter(x -> {
                    return x.getGroupId().equalsIgnoreCase(finalGroupId) && x.getSku().equalsIgnoreCase(sku);
                }).peek(c -> c.setOsImageid(clusterVm.getOsImageid())).findFirst();

                if (confHostGroupVmSku.isPresent()) {
                    return confHostGroupVmSku.get();
                }
            }
            getLogger().info("传入的虚拟机创建信息Tag为: groupid={}, sku={}, 在confClusterVm或ConfHostGroupVmSku没未找到.", groupId, sku);
            return null;
        } catch (Exception e) {
            getLogger().error("根据azure返回的查询配置信息异常，", e);
            return null;
        }
    }

    /**
     * 补全一个VM的价格, 对于查询出来的价格,将进行短暂的缓存, 60秒<br/>
     * 会补全
     * 1. 首先会从Azure返回的Tag中获取;<br/>
     * 2. 如果没有, 再从缓存中获取.<br/>
     * 3. 如果缓存中也没有, 默认为空,不从Azure查询.<br/>
     *
     * @param vm
     * @param region
     */
    private void complateVmPrice(InfoClusterVm vm, VirtualMachineResponse vmResponse, String region) {
        try {
            // 按需价格
            BigDecimal ondemandPrice = vmResponse.getTagByName("sdp-spot-demand-price", null, BigDecimal.class);
            if (Objects.isNull(ondemandPrice)) {
                // 如果在Tag中没有找到按需的价格, 从历史价格表中找
                AzurePriceHistory cachedPrice = priceCache.get(region+":"+vm.getSkuName());
                if (Objects.isNull(cachedPrice)) {
                    cachedPrice = azurePriceHistoryMapper.selectLatestPrice(region, vm.getSkuName());
                    if (Objects.isNull(cachedPrice)) {
                        // 没查到, 就生成一个空的历史价格
                        cachedPrice = new AzurePriceHistory();
                    }
                    priceCache.put(region+":"+vm.getSkuName(), cachedPrice);
                }
                vm.setOndemondPrice(cachedPrice.getOndemandUnitPrice());
            } else {
                vm.setOndemondPrice(ondemandPrice);
            }

            // 竞价价格
            vm.setSpotPrice(vmResponse.getTagByName("sdp-spot-bid-price", null, BigDecimal.class));
        } catch (Exception e) {
            getLogger().error("补全Vm的价格出错:" + e.getMessage(), e);
        }
    }

    private TimedCache<String, AzurePriceHistory> priceCache = CacheUtil.newTimedCache(60000);

    /**
     * 获取竞价实例价格
     *
     * @param infoClusterVm
     * @param virtualMachineResponse
     * @return
     */
    private boolean getSpotPrice(InfoClusterVm infoClusterVm, VirtualMachineResponse virtualMachineResponse,String region,String subscriptionId) {
        try {
            if (virtualMachineResponse.getTags() != null && virtualMachineResponse.getTags().size() > 0) {
                if (virtualMachineResponse.getTags().containsKey("sdp-spot-demand-price")) {
                    infoClusterVm.setOndemondPrice(new BigDecimal(virtualMachineResponse.getTags().get("sdp-spot-demand-price")));
                }else{
                    //多机型tags里,没有按需价格
                    String skuName = virtualMachineResponse.getVmSize();
                    BigDecimal ondemondPrice = azureFleetService.getOndemondPrice(skuName, region, subscriptionId);
                    infoClusterVm.setOndemondPrice(ondemondPrice);
                }

                if (virtualMachineResponse.getTags().containsKey("sdp-spot-bid-price")) {
                    infoClusterVm.setSpotPrice(new BigDecimal(virtualMachineResponse.getTags().get("sdp-spot-bid-price")));
                }
            }
            return true;
        } catch (Exception e) {
            getLogger().error("竞价实例获取价格异常，", e);
            return false;
        }
    }

    /**
     * 保存扩容虚拟机信息
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg saveClusterAppendVminfo(String message) {
        ResultMsg msg = new ResultMsg();
        JobQueryResponse jobQueryResponse = null;
        StringBuilder summary = new StringBuilder();
        try {
            //region 解析数据包
            getLogger().info("保存扩容虚拟机信息 saveClusterScaleoutVmInfo 传入的报文 {}", message);
            try {
                jobQueryResponse = JSONObject.parseObject(message, JobQueryResponse.class);
            } catch (Exception e) {
                getLogger().error("创建Scaleoutvm完成之后，将数据保存出现异常 {}", e);
                summary.append("解析报文出错,报文内容:").append(message);
            }
            if (null == jobQueryResponse || !"Completed".equals(jobQueryResponse.getStatus())) {
                getLogger().info("jobQueryResponse is null or jobQueryResponse.getStatus() is not Completed, return");
//                msg.setResult(false);
//                msg.setErrorMsg("job status is not completed.");
//                return msg;
            }
            //endregion

            //region 查询 infoClusterVmJob
            String jobId = jobQueryResponse.getId();
            InfoClusterVmJob infoClusterVmJob = vmJobMapper.getVmJobByJobId(jobId);
            if (null == infoClusterVmJob) {
                getLogger().error(" infoClusterVmJobMapper.getVmJobByJobId(jobId) result null {} ", jobId);
                msg.setResult(false);
                msg.setErrorMsg("not found job info,jobid:" + jobId);
                return msg;
            }
            getLogger().info("保存虚拟机信息 savescaleoutClusterVmInfo 查询 infoClusterVmJob 结果 {}",
                    JSONObject.toJSONString(infoClusterVmJob));

            /**
             * 查询 conf cluster
             * */
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(infoClusterVmJob.getClusterId());
            if (null == confCluster) {
                getLogger().error("查询 conf cluster null");
                msg.setResult(false);
                msg.setErrorMsg("not fount cluster config info info ,clusterId:" + infoClusterVmJob.getClusterId());
                return msg;
            }
            //endregion
            //region 获取扩容任务信息
            InfoClusterOperationPlanActivityLog activityLog =
                    planActivityLogMapper.selectByPrimaryKey(infoClusterVmJob.getActivityLogId());
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(activityLog.getPlanId());

            ConfScalingTask task = scalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());
            //endregion

            ConfClusterHostGroup hostGroup = hostGroupMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
            //region 扩容vm配置信息
            List<ConfScalingTaskVm> scalingTaskVms = scalingTaskVmMapper.getScalingVmConfig(task.getTaskId());
            // 单一group扩容，不支持多个group同时扩容
            ConfScalingTaskVm scalingTaskVm = scalingTaskVms.get(0);
            //endregion

            // region 构造infoclustervm数据包
            String clusterId = infoClusterVmJob.getClusterId();

            String transactionId = infoClusterVmJob.getTransactionId();
            Date begTime = infoClusterVmJob.getBegTime();
            Date endTime = new Date();
            List<InfoClusterVm> toSaveInfoClusterVmList = new CopyOnWriteArrayList<>();
            List<VirtualMachineGroupResponse> data = jobQueryResponse.getData();
            summary.append("申请返回资源组数量:").append(data.size()).append(",");
            for (VirtualMachineGroupResponse group : data) {
                Integer groupTotalVmCount = jobQueryResponse.getVMCount(null, group.getGroupName());
                Integer groupSuccessVmCount = jobQueryResponse.getVMCount(JobQueryResponse.STATE_SUCCEEDED, group.getGroupName());
                summary.append("(").append(group.getGroupName()).append(" VM数:")
                        .append(groupTotalVmCount).append(", 成功数:")
                        .append(groupSuccessVmCount)
                        .append(");");

                List<VirtualMachineResponse> successVms = jobQueryResponse.getSuccessVmByRole(group.getGroupName());
                for (VirtualMachineResponse vm : successVms) {
                    InfoClusterVm infoClusterVm = new InfoClusterVm();
                    infoClusterVm.setSkuName(vm.getVmSize());
                    infoClusterVm.setClusterId(clusterId);
                    infoClusterVm.setVmName(vm.getName());
                    infoClusterVm.setVmid(vm.getUniqueId());
                    //创建集群时的conf_cluster_vm集群
                    infoClusterVm.setVmConfId(scalingTaskVm.getVmConfId());
                    infoClusterVm.setHostName(vm.getHostName().toLowerCase());
                    infoClusterVm.setInternalip(vm.getPrivateIp());
                    //获取当前集群数据
                    infoClusterVm.setDefaultUsername(task.getDefaultUsername());
                    infoClusterVm.setState(InfoClusterVm.VM_RUNNING);
                    infoClusterVm.setScaleoutTaskId(task.getTaskId());
                    String vmRole = task.getVmRole();
                    infoClusterVm.setVmRole(vmRole);
                    infoClusterVm.setGroupName(task.getGroupName());
                    if (hostGroup != null) {
                        infoClusterVm.setGroupId(hostGroup.getGroupId());
                    }
                    // VM 工程模式开
                    infoClusterVm.setMaintenanceMode(InfoClusterVm.MaintenanceModeON);

//                    getSpotPrice(infoClusterVm, vm,confCluster.getRegion(),confCluster.getSubscriptionId());
                    complateVmPrice(infoClusterVm, vm, confCluster.getRegion());
                    infoClusterVm.setPurchaseType(null == scalingTaskVm.getPurchaseType() ? "" : scalingTaskVm.getPurchaseType().toString());
                    infoClusterVm.setImageid(scalingTaskVm.getOsImageid());
                    infoClusterVm.setCreateTranscationId(transactionId);
                    infoClusterVm.setCreateJobId(jobId);
                    infoClusterVm.setCreateBegtime(begTime);
                    infoClusterVm.setCreateEndtime(endTime);
                    //todo:niyang set price from vmtagmap(key:"sdp-spot-demand-price", "sdp-spot-bid-price")

                    // 扩容集群的vm配置ID
                    infoClusterVm.setScaleVmDetailId(scalingTaskVm.getVmDetailId());
                    toSaveInfoClusterVmList.add(infoClusterVm);
                }
            }
            //endregion

            //region 保存infoclustervm
            List<InfoClusterVm> infoClusterVmList =
                    infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(clusterId, task.getTaskId());

            if (CollectionUtils.isEmpty(infoClusterVmList) && toSaveInfoClusterVmList != null && toSaveInfoClusterVmList.size() > 0) {
                getLogger().info("保存虚拟机，未查询到数据，执行批处理进行保存");
                infoClusterVmMapper.insertBatch(toSaveInfoClusterVmList);
            } else {
                List<InfoClusterVm> appendSaveInfoClusterVMList = new CopyOnWriteArrayList<>();
                toSaveInfoClusterVmList.stream().forEach(x -> {
                    Optional<InfoClusterVm> vm = infoClusterVmList.stream().filter(y -> y.getClusterId().equalsIgnoreCase(x.getClusterId())
                            && y.getVmName().equalsIgnoreCase(x.getVmName())).findFirst();
                    if (!vm.isPresent()) {
                        appendSaveInfoClusterVMList.add(x);
                    }
                });
                getLogger().info("增量保存虚拟机，" + appendSaveInfoClusterVMList);
                if (appendSaveInfoClusterVMList != null && appendSaveInfoClusterVMList.size() > 0) {
                    infoClusterVmMapper.insertBatch(appendSaveInfoClusterVMList);
                }
            }
            //endregion
            // 保存返回的错误信息
            if (Objects.nonNull(jobQueryResponse.getMessage())) {
                summary.append("\n").append(JSON.toJSONString(jobQueryResponse.getMessage()));
            }
            msg.setData(summary);
            msg.setResult(true);
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getMessage(e) + ExceptionUtils.getStackTrace(e));
            getLogger().error("保存扩容资源信息异常，", e);
        }
        return msg;
    }

    /**
     * 保存blueprint
     *
     * @param clusterId
     */
    private void saveBlueprint(String clusterId) {
        Assert.notEmpty(clusterId, "保存blueprint clusterId is null");
        InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(clusterId);
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        //获取集群的Ambari Blueprint；
        String clusterName = clusterService.getSdpClusterNameforAmbari(clusterId);
        AmbariInfo ambariInfo = clusterService.getAmbariInfo(clusterId);
        Blueprint originBlueprint = ambariService.getBlueprint(ambariInfo, clusterName);
        if (originBlueprint != null) {
            //并将blueprint保存到数据库中
            InfoClusterFinalBlueprint blueprint = new InfoClusterFinalBlueprint();
            blueprint.setClusterId(confCluster.getClusterId());
            blueprint.setClusterName(confCluster.getClusterName());
            blueprint.setBlueprintContent(JacksonUtils.toJson(originBlueprint));
            blueprint.setAmbariHost(infoCluster.getAmbariHost());
            blueprint.setCreateTime(new Date());
            clusterFinalBlueprintService.insertOrUpdate(blueprint);
        } else {
            getLogger().error("保存blueprint失败，clusterId:{}", clusterId);
        }
    }

    /**
     * 销毁集群删除虚拟机
     * azureVMService@deleteVms
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg deleteVms(String messageparam) {

        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
//        String region = param.getString("region");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //保存blueprint
            this.saveBlueprint(confCluster.getClusterId());
            getLogger().info(confCluster.getClusterName() + "--begin_deleteambaridb");
            // 删除ambari数据库
            deleteAmbariDb(confCluster);
            getLogger().info(confCluster.getClusterName() + "--end_deleteambaridb");
            Map<String, Object> azureApiLogs = new HashMap<>();
            azureApiLogs.put("clusterId", confCluster.getClusterId());
            azureApiLogs.put("activityLogId", activityLogId);
            azureApiLogs.put("planId", currentLog.getPlanId());
            threadLocal.set(azureApiLogs);
            ResultMsg msg = ruinCluster(confCluster.getClusterName(), confCluster.getSubscriptionId());
            getLogger().info(confCluster.getClusterName() + "--end_deleteVms");
            if (msg.getResult() && msg.getData() != null) {
                JSONObject response = (JSONObject) msg.getData();
                String jobid = "";
                // 此处判断是否资源组存在,如果不存在,可认为已经删除成功
                // ruinCluster return , elapse:
                String detail = response.getString("detail");
                getLogger().info("删除资源组返回信息: response={}, detail={}", JSON.toJSONString(response), detail);
                if (StrUtil.contains(detail, "resource group")
                        && StrUtil.contains(detail, "not found")) {
                    getLogger().info("资源组不存在,认为已经删除成功: errMsg={}", detail);
                    jobid = "000000";
                } else {
                    jobid = response.getString("id");
                }
                JSONObject paraminfo = new JSONObject();
                paraminfo.put(JobNameConstant.Delete_VM, jobid);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);

                // 更新集群状态
                ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);

                getLogger().info(confCluster.getClusterName() + "--end_getcluster");
                //销毁过程中
                cluster.setState(ConfCluster.DELETING);
                clusterMapper.updateByPrimaryKey(cluster);
                getLogger().info(confCluster.getClusterName() + "--end_updateClusterName");


                //删除cluster之后， key vault 需要删除敏感数据
                String clusterName = cluster.getClusterName();
                keyVault keyVault = metaDataItemService.getkeyVault(confCluster.getRegion());
                keyVaultUtil.delSecret("ambari-db-user-" + clusterName, keyVault.getEndpoint());
                keyVaultUtil.delSecret("ambari-db-pwd-" + clusterName, keyVault.getEndpoint());
                keyVaultUtil.delSecret("ambari-pwd-" + clusterName, keyVault.getEndpoint());
                keyVaultUtil.delSecret("hivemetadata-db-user-" + clusterName, keyVault.getEndpoint());
                keyVaultUtil.delSecret("hivemetadata-db-pwd-" + clusterName, keyVault.getEndpoint());
                getLogger().info(confCluster.getClusterName() + "--end_delkeyVaultUtil");

                planExecService.sendNextActivityMsg(activityLogId, paraminfo);
                getLogger().info(confCluster.getClusterName() + "--end_sendNextmsg");
            } else {
                currentLog.setLogs(msg.toString());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("---Delete VM Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        getLogger().info("--end_complateActivity");
        return resultMsg;
    }

    /**
     * 删除集群对应azure上的资源组
     *
     * @param clusterName 集群名称
     * @return
     */
    private ResultMsg ruinCluster(String clusterName, String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                msg = azureService.ruinCluster(clusterName, subscriptionId);
                if (msg.getResult()) {
                    return msg;
                }
            } catch (Exception e) {
                getLogger().error("删除资源组异常，", e);
            }
            i++;
            if (i > sdpDeleteVMRetryTimes) {
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpDeleteVMRetryWaitTimes);
        }
    }

    /**
     * 缩容删除VMs
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg deleteVmsForScaleIn(String message) {

        ResultMsg resultMsg = new ResultMsg();

        // region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);


        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }


        if (StringUtils.isEmpty(taskId)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("taskId 为空");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //region 获取缩容任务要下线的机器列表
            List<InfoClusterVm> vmList = infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(confCluster.getClusterId(), taskId);
            //endregion

            if (vmList == null || vmList.size() == 0) {
                getLogger().warn("使用taskid获取缩容任务要下线的机器列表为空。");
                vmList = vmClearLogMapper.selectInfoClusterVmsByPlanId(currentLog.getPlanId());
            }

            //region 异步删除处理
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());

            // 判断是否为竞价逐出引起的缩容: planName=竞价逐出 && 缩容任务
            boolean skipInvokeAzureDelete = StrUtil.equals(plan.getPlanName(), "竞价逐出") &&
                    StrUtil.equalsIgnoreCase(plan.getOperationType(), InfoClusterOperationPlan.Plan_OP_ScaleIn);

            if (skipInvokeAzureDelete){
                // 如果跳过从Azure删除VM, 直接更新虚拟机状态为已删除
                infoClusterVmMapper.updateVmsStatusByScaleInTaskId(confCluster.getClusterId(), taskId, InfoClusterVm.VM_DELETED);
                getLogger().info("从Azure删除VM时,任务是竞价逐出任务, 跳过从Azure删除VM操作, 直接更新VM状态为已删除, 真正删除操作由Azure主动逐出负责:{}", vmList);

                // 更新执行结果为已完成
                param.put(JobNameConstant.Delete_VM, "000000");
                planExecService.sendNextActivityMsg(activityLogId, param);

                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.complateActivity(currentLog);

                resultMsg.setResult(true);
                return resultMsg;
            } else {
                getLogger().info("从Azure删除VM时,任务不是是竞价逐出任务,正常删除");
            }

            ResultMsg asyncDelMsg = ivmDeleteService.saveToAsyncDelete(confCluster.getRegion(), vmList, plan);
            if (asyncDelMsg.getResult()) {
                //异步删除提交完成
                param.put(JobNameConstant.Delete_VM, "000000");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                //region 更新机器的状态为删除中
                List<String> vmNames = new CopyOnWriteArrayList<>();
                vmList.stream().forEach(x -> {
                    vmNames.add(x.getVmName());
                });
                infoClusterVmMapper.batchUpdateVMState(confCluster.getClusterId(), vmNames, InfoClusterVm.VM_DELETING);
                //endregion
                planExecService.sendNextActivityMsg(activityLogId, param);
                planExecService.complateActivity(currentLog);
                return resultMsg;
            } else {
                getLogger().info("不符合异步删除要求：" + asyncDelMsg.getErrorMsg());
            }
            //endregion

            //region 生成vm任务
            InfoClusterVmJob vmJob = getVmJob(confCluster.getClusterId()
                            , confCluster.getClusterName()
                            , InfoClusterOperationPlan.Plan_OP_ScaleIn
                            , activityLogId);
            // endregion

            // region 请求azure接口
            Map<String, Object> azureApiLogs = new HashMap<>();
            azureApiLogs.put("clusterId", confCluster.getClusterId());
            azureApiLogs.put("activityLogId", activityLogId);
            azureApiLogs.put("planId", currentLog.getPlanId());
            threadLocal.set(azureApiLogs);

            ResultMsg msg = sendDeleteVMRequest(confCluster, vmList, vmJob);
            getLogger().info(confCluster.getClusterName() + "deleteVMsRequest,--end_deleteVms");
            // endregion

            if (msg.getResult() && msg.getData() != null) {
                JSONObject response = (JSONObject) msg.getData();
                String jobid = response.getString("id");
                param.put(JobNameConstant.Delete_VM, jobid);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                getLogger().info(confCluster.getClusterName() + "--end_sendNextmsg");
            } else {
                currentLog.setLogs(msg.toString());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("---Delete VM Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        getLogger().info("--end_complateActivity");
        return resultMsg;
    }

    /**
     * 缩容批量销毁VM with 重试
     *
     * @param deleteVMsRequest
     * @return
     */
    private ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest, String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                if (azureFleetSwitch.equals(1)) {
                    msg = azureFleetService.deleteVirtualMachines(deleteVMsRequest, subscriptionId);
                } else {
                    msg = azureServiceSer.deleteVirtualMachines(deleteVMsRequest);
                }
                if (msg.getResult()) {
                    return msg;
                }
                // 429 限流，额外 sleep 180s
                if (msg.getActimes() != null) {
                    ThreadUtil.sleep(1000 * 180);
                }
            } catch (Exception e) {
                getLogger().error("删除VM，异常", e);
            }
            getLogger().info("删除VM请求，重试：" + i);
            i++;
            if (i > sdpDeleteVMRetryTimes) {
                msg.setErrorMsg("删除VM请求,超过重试次数。");
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpDeleteVMRetryWaitTimes);
        }
    }


    private ResultMsg deleteSingleVirtualMachine(String vmName, String dnsName, String region) {
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                msg = azureService.deleteVMInstance(vmName, dnsName, region);
                if (msg.getResult()) {
                    return msg;
                }
            } catch (Exception e) {
                getLogger().error("删除单个VM，异常", e);
            }
            getLogger().info("删除单个VM请求，重试：" + i);
            i++;
            if (i > sdpDeleteVMRetryTimes) {
                msg.setErrorMsg("删除单个VM请求,超过重试次数。");
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpDeleteVMRetryWaitTimes);
        }
    }


    /**
     * 缩容批量销毁VM
     *
     * @param deleteVMsRequest
     * @return
     */
    private ResultMsg deleteVirtualMachinesNoRetry(AzureDeleteVMsRequest deleteVMsRequest) {
        ResultMsg msg = new ResultMsg();
        try {
            msg = azureServiceSer.deleteVirtualMachines(deleteVMsRequest);
            return msg;
        } catch (Exception e) {
            getLogger().error("删除VM，异常", e);
        }
        return msg;
    }


    /**
     * 构建缩容删除vm的请求体
     *
     * @param vmJob
     * @param vmList
     * @return
     */
    private AzureDeleteVMsRequest buildAzureVmsDeleteReq(InfoClusterVmJob vmJob, List<InfoClusterVm> vmList) {
        AzureDeleteVMsRequest request = new AzureDeleteVMsRequest();
        request.setApiVersion("1.0");
        request.setTransactionId(vmJob.getTransactionId());
        request.setClusterName(vmJob.getClusterName());
        request.setRegion(vmJob.getRegion());
        List<String> vmnames = new CopyOnWriteArrayList<>();
        vmList.stream().forEach(x -> {
            vmnames.add(x.getVmName());
        });
        request.setVmNames(vmnames);

        List<String> dnsNames = new ArrayList<>();
        vmList.stream().forEach(x -> {
            dnsNames.add(x.getHostName());
        });
        request.setDnsNames(dnsNames);
        return request;
    }

    /**
     * 构建缩容删除vm的请求体
     *
     * @param vmJob
     * @param vmList
     * @return
     */
    private AzureDeleteVMsRequest buildAzureVmsDeleteReqByVMNames(InfoClusterVmJob vmJob, List<String> vmList, List<String> dnsNames) {
        AzureDeleteVMsRequest request = new AzureDeleteVMsRequest();
        request.setApiVersion("1.0");
        request.setTransactionId(vmJob.getTransactionId());
        request.setClusterName(vmJob.getClusterName());
        request.setVmNames(vmList);
        request.setDnsNames(dnsNames);
        request.setRegion(vmJob.getRegion());
        return request;
    }


    /**
     * 删除Ambari数据库
     *
     * @param confCluster
     * @return
     */
    private ResultMsg deleteAmbariDb(ConfCluster confCluster) {
        ResultMsg msg = new ResultMsg();
        Connection connect = null;
        String region = confCluster.getRegion();
        try {
            if (confCluster.getAmbariDbAutocreate() == 1
                    && (confCluster.getIsEmbedAmbariDb() == null
                    || confCluster.getIsEmbedAmbariDb().equals(0))) {
                Class.forName("com.mysql.jdbc.Driver");
                String jdbcurl = "jdbc:mysql://" + confCluster.getAmbariDburl() + ":" + confCluster.getAmbariPort() + "/"
                        + confCluster.getAmbariDatabase() + "?useSSL=false";
                getLogger().info("delete db url:" + jdbcurl);
                keyVault keyVault = metaDataItemService.getkeyVault(region);
                String account = keyVaultUtil.getSecretVal("ambari-db-user-" + confCluster.getClusterName(), keyVault.getEndpoint());
                String password = keyVaultUtil.getSecretVal("ambari-db-pwd-" + confCluster.getClusterName(), keyVault.getEndpoint());
                Properties userinfo = new Properties();
                userinfo.put("user", account);
                userinfo.put("password", password);
                connect = DriverManager.getConnection(jdbcurl, userinfo);

                Statement stmt = connect.createStatement();
                boolean rs = stmt.execute("DROP DATABASE IF EXISTS  " + confCluster.getAmbariDatabase() + ";");
                msg.setResult(true);
            }
        } catch (Exception e) {
            getLogger().error("Delete Ambari Db,", e);
            msg.setResult(false);
        } finally {
            try {
                if (connect != null && !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException e) {
                getLogger().error("Delete ambari db jdbc Close has exception,", e);
            }
        }
        return msg;
    }

    /**
     * 查询删除虚拟机状态<br/>
     * azureVMService@queryDeleteVms
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg queryDeleteVms(String messageparam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String dvmjobId = param.getString(JobNameConstant.Delete_VM);
        String activityLogId = param.getString("activityLogId");


        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        // 查询集群信息
        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        // vm 已经删除或不存在的情况下
        if (StrUtil.equals(dvmjobId,"000000")) {
            currentLog.setState(2);
            //已释放状态
            cluster.setState(ConfCluster.DELETED);
            clusterMapper.updateByPrimaryKey(cluster);

            // 生成VM下线事件
            saveVmOfflineEvent(cluster.getClusterId());

            // 更新虚拟机状态
            setClusterVMDestroyState(cluster.getClusterId());

            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(activityLogId, new JSONObject());
            return resultMsg;
        } else if (StrUtil.isBlank(dvmjobId)) {
            getLogger().info("查询删除进展时,没有Jobid,不需要再查询: activityLogId={}, clusterId={} clusterName={}",
                    activityLogId, cluster.getClusterId(), cluster.getClusterName());
            currentLog.setState(2);
            //已释放状态
            cluster.setState(ConfCluster.DELETED);
            clusterMapper.updateByPrimaryKey(cluster);

            // 生成VM下线事件
            saveVmOfflineEvent(cluster.getClusterId());

            // 更新虚拟机状态
            setClusterVMDestroyState(cluster.getClusterId());

            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(activityLogId, new JSONObject());
            return resultMsg;
        }
        ResultMsg msg = azureService.getJobsStatus(dvmjobId, cluster.getSubscriptionId());

        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            if (response.containsKey("status")) {
                if (response.getString("status").equalsIgnoreCase("Completed")) {
                    currentLog.setState(2);
                    //更新集群状态
//                    ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
                    //已释放状态
                    cluster.setState(ConfCluster.DELETED);
                    cluster.setModifiedTime(new Date());
                    clusterMapper.updateByPrimaryKey(cluster);

                    // 生成VM下线事件
                    saveVmOfflineEvent(cluster.getClusterId());

                    // 更新虚拟机状态
                    setClusterVMDestroyState(cluster.getClusterId());

                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(activityLogId, new JSONObject());
                    return resultMsg;
                }

                // 销毁虚拟机操作azure 返回失败
                if (response.getString("status").equalsIgnoreCase("Failed")) {
                    currentLog.setState(2);
                    //更新集群状态
//                    ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
                    //已释放状态
                    cluster.setState(ConfCluster.DELETED);
                    clusterMapper.updateByPrimaryKey(cluster);
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs("销毁进程查询返回失败：" + response.toJSONString());
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }

                // 销毁中时, 发送循环等待消息,在此
                resultMsg = planExecService.loopActivity(clientname, messageparam,
                        10L, activityLogId);
                return resultMsg;
            } else {
                getLogger().error("集群扩容查询资源创建结果异常：" + messageparam);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.toJSONString());
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
        } else {
            getLogger().error("集群扩容查询资源创建结果异常：" + messageparam);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(msg.toString());
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
    }

    /**
     * 保存VM下线事件
     */
    private void saveVmOfflineEvent(String clusterId) {
        getLogger().info("保存VM下线事件, clusterId={}", clusterId);
        vmEventService.saveVmEventsForDeleteCluster(clusterId);
    }

    /**
     * 更新销毁集群的机器状态为已删除
     *
     * @param clusterId
     */
    private void setClusterVMDestroyState(String clusterId) {
        infoClusterVmMapper.updateVMStateByClusterId(clusterId, InfoClusterVm.VM_DELETED);
    }

    /**
     * 根据实例角色获取集群机器
     *
     * @param clusterId
     * @param role
     * @return
     */
    @Override
    public List<InfoClusterVm> getRoleVms(String clusterId, String role) {
        return infoClusterVmMapper.selectByClusterIdAndRole(clusterId, role);
    }

    @Override
    public List<InfoClusterVm> getGroupVms(String clusterId, String groupName) {
        return infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, groupName, InfoClusterVm.VM_RUNNING);
    }

    @Override
    public List<InfoClusterVm> getGroupVms(String clusterId, String groupName, String vmSkuName) {
        return infoClusterVmMapper.selectByClusterIdAndGroupNameAndSkuNameAndState(clusterId, groupName,
                vmSkuName, InfoClusterVm.VM_RUNNING);
    }


    /**
     * 根据实例角色和实例运行状态获取集群机器
     *
     * @param clusterId
     * @param role
     * @param state
     * @return
     */
    @Override
    public List<InfoClusterVm> getRoleVmsByState(String clusterId, String role, Integer state) {
        return infoClusterVmMapper.selectByClusterIdAndRoleAndState(clusterId, role, state);
    }

    /**
     * 根据实例组名称和实例运行状态获取集群机器
     *
     * @param clusterId
     * @param groupName
     * @param state
     * @return
     */
    @Override
    public List<InfoClusterVm> getGroupVmsByState(String clusterId, String groupName, Integer state) {
        getLogger().info("getGroupVmsByState,参数：clusterId:{},groupName:{},state:{}", clusterId, groupName, state);
        return infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, groupName, state);
    }

    /**
     * 获取集群所有机器
     *
     * @param clusterId
     * @return
     */
    @Override
    public List<InfoClusterVm> getAllVms(String clusterId) {
        return infoClusterVmMapper.selectByClusterId(clusterId);
    }

    /**
     * 获取集群扩容新增的机器
     *
     * @param clusterId 集群ID
     * @param taskId    扩容任务ID
     * @return
     */
    @Override
    public List<InfoClusterVm> getScaleOutVms(String clusterId, String taskId) {
        return infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(clusterId, taskId);
    }

    /**
     * 获取集群需要缩容的任务
     *
     * @param clusterId 集群ID
     * @param taskId    缩容任务ID
     * @return
     */
    @Override
    public List<InfoClusterVm> getScaleInVms(String clusterId, String taskId) {
        return infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(clusterId, taskId);
    }

    /**
     * 构造创建集群时申请资源的请求参数
     *
     * @param clusterId
     * @return
     */
    private Map<String, Object> bulidAzureVmsRequest(String clusterId, String activityLogId) {
        getLogger().info("buildAzureVmsRequest clusterId:{}, activityLogId:{}", clusterId, activityLogId);
        HashMap<String, Object> res = new HashMap<>();
        AzureVmsRequest azureVmsRequest = new AzureVmsRequest();

        azureVmsRequest.setApiVersion("V1");

        List<VmGroups> virtualMachineGroups = new ArrayList<>();

        // 1.获取cluster conf
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);
        Set<String> miSet = new HashSet<>();
        if (!StringUtils.isEmpty(confCluster.getLogMI())) {
            miSet.add(confCluster.getLogMI());
        }
        if (!StringUtils.isEmpty(confCluster.getVmMI())) {
            miSet.add(confCluster.getVmMI());
        }

        List<String> miList = new ArrayList<>();
        for (String s : miSet) {
            miList.add(s);
        }


        if (confCluster == null) {
            getLogger().error("not found cluster config for id:" + clusterId);
            return null;
        }
        //新增数据中心
        azureVmsRequest.setRegion(confCluster.getRegion());

        InfoClusterVmJob vmJob =
                getVmJob(clusterId, confCluster.getClusterName(), "create", activityLogId);
        if (vmJob.getJobStatus() > 0) {
            getLogger().info("Create VM Job is running ,transactionid:" + vmJob.getTransactionId());
            return null;
        }
        azureVmsRequest.setTransactionId(vmJob.getTransactionId());
        azureVmsRequest.setClusterName(confCluster.getClusterName());

        //2. 查询clustervm 配置数据
        List<ConfClusterVm> confClusterVms = vmMapper.getVmConfs(clusterId);
        if (null == confClusterVms || confClusterVms.size() == 0) {
            getLogger().error("not found cluster vm config for id:" + clusterId);
            return null;
        }

        // 4.获取集群标签
        List<ConfClusterTag> clusterTags =
                tagMapper.getTagsbyClusterId(confCluster.getClusterId());
        HashMap<String, String> tagmap = new HashMap<>();

        if (null != clusterTags && clusterTags.size() > 0) {
            clusterTags.stream().forEach(x -> {
                tagmap.put(x.getTagGroup(), x.getTagVal());
            });
        }
        AtomicInteger coreCnt = new AtomicInteger();
        AtomicInteger taskCnt = new AtomicInteger();

        // 4.build VMGroup entity
        confClusterVms.stream().forEach(item -> {
            VmGroups vmGroup = new VmGroups();
            SpecClass spec = new SpecClass();

            AzureSpotProfile azureSpotProfile = null;
            ResultMsg resultMsg = AzureServiceManager.buildAzureSpotProfile(getLogger(), azureService, item, confCluster.getClusterId(), confCluster.getRegion());
            if (!resultMsg.isResult()) {
                return;
            } else if (resultMsg.getData() != null) {
                azureSpotProfile = (AzureSpotProfile) resultMsg.getData();
                getLogger().info("set AzureSpotProfile:{}", azureSpotProfile);
                spec.setSpotProfile(azureSpotProfile);
            }
            //azure的group对应SDP的VMRole
            vmGroup.setGroupName(item.getVmRole());
            vmGroup.setCount(item.getCount());

            if (item.getVmRole().equalsIgnoreCase("core")) {
                coreCnt.addAndGet(item.getCount());
            }
            if (item.getVmRole().equalsIgnoreCase("task")) {
                taskCnt.addAndGet(item.getCount());
            }

            spec.setUserAssignedIdentityResourceIds(miList);
            spec.setSkuName(item.getSku());
            spec.setOsImageType(item.getOsImageType());
            if (item.getOsImageType().equalsIgnoreCase("CustomImage")) {
                spec.setCustomOSImageId(item.getOsImageid());
            }
            if (item.getOsImageType().equalsIgnoreCase("MarketplaceImage")) {
                spec.setMarketplaceOSImageName(item.getOsImageid());
            }

            spec.setHostNameSuffix(hostnamesuffix);
            spec.setStartupScriptBlobUrl(item.getInitScriptPath());
            spec.setUserName(vmusername);
            spec.setZone(confCluster.getZone());

            //TODO: Ssh PublicKeySecretName 名称, 此处需要改为ResourceId或KeyVault URL
            spec.setSshPublicKeySecretName(confCluster.getKeypairId());
            Map<String, String> meta = azureMetaDataManager.findSshKeyByName(confCluster.getRegion(), confCluster.getKeypairId());
            spec.setSshPublicKeySecretResourceId(meta.get("secretResourceId"));
            spec.setSshKeyVaultId(meta.get("sshKeyVaultId"));

            spec.setSubnetResourceId(confCluster.getSubnet());

            if (vmGroup.getGroupName().equalsIgnoreCase("ambari")
                    || vmGroup.getGroupName().equalsIgnoreCase("master")) {
                // ambari and master use masterSecurityGroup
                spec.setNsgResourceId(confCluster.getMasterSecurityGroup());
            } else {
                // core and task use SlaveSecurityGroup
                spec.setNsgResourceId(confCluster.getSlaveSecurityGroup());
            }

            spec.setOsDiskSku(item.getOsVolumeType());
            spec.setOsDiskSizeGB(item.getOsVolumeSize());
            List<ConfClusterVmDataVolume> vmDataVolumes =
                    confClusterVmDataVolumeMapper.selectByVmConfId(item.getVmConfId());

            // TODO: 对于L系列的主机，数据盘不进行设置，默认大小和数量都为0即可。 此处需根据磁盘类弄判断， 现在暂时使用主机名是否包含L来区分。
            if (null != vmDataVolumes && vmDataVolumes.size() > 0
                    && item.getSku().indexOf("L") == -1) { //TODO: 此处判断需要调整
                spec.setDataDiskSku(vmDataVolumes.get(0).getDataVolumeType());
                spec.setDataDiskSizeGB(vmDataVolumes.get(0).getDataVolumeSize());
                spec.setDataDiskCount(vmDataVolumes.get(0).getCount());
            }

            Map specmap = (Map) tagmap.clone();
            specmap.put("sdp-groupid", item.getGroupId());
            specmap.put("sdp-purchasetype", Convert.toStr(item.getPurchaseType()));
            specmap.put("sdp-sku", item.getSku());
            specmap.put("sdp-clusterid", item.getClusterId());
            specmap.put("sdp-clustername", confCluster.getClusterName());
            specmap.put("sdp-groupname", vmGroup.getGroupName());
            if (azureSpotProfile != null) {
                specmap.put("sdp-spot-demand-price", String.valueOf(azureSpotProfile.getDemandPricePerHour()));
                specmap.put("sdp-spot-bid-price", String.valueOf(azureSpotProfile.getMaxPricePerHour()));
            }
            spec.setVirtualMachineTags(specmap);

            vmGroup.setVirtualMachineSpec(spec);

            Integer provisionType = item.getProvisionType();
            if (provisionType != null) {
                if (provisionType == ConfClusterVm.PROVISION_TYPE_VM_Standalone.intValue()) {
                    vmGroup.setProvisionType("VM_Standalone");
                } else if (provisionType == ConfClusterVm.PROVISION_TYPE_VMSS_Flexible.intValue()) {
                    vmGroup.setProvisionType("VMSS_Flexible");
                }
            }
            virtualMachineGroups.add(vmGroup);
        });

        boolean getindexresult = createClusterBeginIndex(confCluster, coreCnt.get(), taskCnt.get());
        if (!getindexresult) {
            getLogger().error("创建集群生成index失败。");
        }
        azureVmsRequest.setClusterTags(tagmap);

        azureVmsRequest.setVirtualMachineGroups(virtualMachineGroups);

        res.put("azure", azureVmsRequest);
        res.put("job", vmJob);
        return res;
    }

    /**
     * 构造AzureFleet创建集群的请求报文
     *
     * @param clusterId
     * @param activityLogId
     * @return
     */
    private Map<String, Object> buildAzureFleetVmsRequest(String clusterId, String activityLogId) {
        getLogger().info("AzureFleetVmsRequest clusterId:{}, activityLogId:{}", clusterId, activityLogId);
        HashMap<String, Object> res = new HashMap<>();
        AzureFleetVmsRequest azureFleetVmsRequest = new AzureFleetVmsRequest();

        azureFleetVmsRequest.setApiVersion("V1");

        List<AzureVMGroupRequest> virtualMachineGroups = new ArrayList<>();

        //region  1.获取cluster conf
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster == null) {
            getLogger().error("not found cluster config for id:" + clusterId);
            return null;
        }
        //endregion

        //region 2. 获取 Job
        InfoClusterVmJob vmJob =
                getVmJob(clusterId, confCluster.getClusterName(), "create", activityLogId);
        if (vmJob.getJobStatus() > 0) {
            getLogger().info("Create VM Job is running ,transactionid:" + vmJob.getTransactionId());
            return null;
        }
        //endregion

        //集群名称
        azureFleetVmsRequest.setClusterName(confCluster.getClusterName());
        //数据中心
        azureFleetVmsRequest.setRegion(confCluster.getRegion());
        azureFleetVmsRequest.setTransactionId(vmJob.getTransactionId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        azureFleetVmsRequest.setRequestTimestamp(sdf.format(new Date()));


        //2. 查询clustervm 配置数据
        List<ConfClusterVm> confClusterVms = vmMapper.getVmConfs(clusterId);
        if (null == confClusterVms || confClusterVms.size() == 0) {
            getLogger().error("not found cluster vm config for id:" + clusterId);
            return null;
        }

        // 4.获取集群标签
        List<ConfClusterTag> clusterTags =
                tagMapper.getTagsbyClusterId(confCluster.getClusterId());
        HashMap<String, String> tagmap = new HashMap<>();

        if (null != clusterTags && clusterTags.size() > 0) {
            clusterTags.stream().forEach(x -> {
                tagmap.put(x.getTagGroup(), x.getTagVal());
            });
        }
        azureFleetVmsRequest.setClusterTags(tagmap);
        AtomicInteger coreCnt = new AtomicInteger();
        AtomicInteger taskCnt = new AtomicInteger();
        AvailabilityZone zone = metaDataItemService.getAZ(confCluster.getRegion(), confCluster.getZone());
        // 4.build VMGroup entity
        confClusterVms.stream().forEach(item -> {

            AzureVMGroupRequest vmGroup = new AzureVMGroupRequest();

            VirtualMachineSpec spec = new VirtualMachineSpec();
            getLogger().info("begin build baseProfile");

            List<ConfClusterVmDataVolume> vmDataVolumes =
                    confClusterVmDataVolumeMapper.selectByVmConfId(item.getVmConfId());
            item.setVmDataVolumes(vmDataVolumes);
            ResultMsg baseProfilerRes = azureFleetService.buildAzureBaseProfile(confCluster, item);
            if (baseProfilerRes.getResult() && baseProfilerRes.getData() != null) {
                BaseProfile baseProfile = (BaseProfile) baseProfilerRes.getData();
                spec.setBaseProfile(baseProfile);
            } else {
                getLogger().error("创建集群时, 生成BaseProfile失败:" + baseProfilerRes.getErrorMsg());
                return;
            }

            getLogger().info("begin build spotProfile");
            ResultMsg spotresultMsg = azureFleetService.buildAzureSpotProfile(confCluster, item);
            SpotProfile azureSpotProfile = null;
            if (!spotresultMsg.isResult()) {
                getLogger().error("创建集群时,生成SpotProfile失败, 退出:" + spotresultMsg.getErrorMsg());
                return;
            } else if (spotresultMsg.getData() != null) {
                azureSpotProfile = (SpotProfile) spotresultMsg.getData();
                getLogger().info("set AzureSpotProfile:{}", JSON.toJSONString(azureSpotProfile));
                spec.setSpotProfile(azureSpotProfile);
            }

            getLogger().info("begin build regularProfile");
            ResultMsg regularResultMsg = azureFleetService.buildAzureRegularProfile(confCluster, item);

            if (!regularResultMsg.isResult()) {
                getLogger().error("创建集群时, 生成RegularProfile失败,退出:" + regularResultMsg.getErrorMsg());
                return;
            } else if (regularResultMsg.getData() != null) {
                RegularProfile regularProfile = (RegularProfile) regularResultMsg.getData();
                getLogger().info("set regularProfile:{}", regularProfile);
                spec.setRegularProfile(regularProfile);
            }
            getLogger().info("begin build vMSizesProfile");
            // 如果只有按需的实例, 则不需要多VMSku
//            boolean useMultiSku = Objects.nonNull(spec.getSpotProfile());
            boolean useMultiSku = false;
            spec.setVmSizesProfile(azureFleetService.buildVMSizesProfile(confCluster, item, useMultiSku));

            //azure的group对应SDP的VMRole
            vmGroup.setGroupName(item.getGroupName());
            vmGroup.setVmRole(item.getVmRole());
            vmGroup.setCount(item.getCount());

            if (item.getVmRole().equalsIgnoreCase("core")) {
                vmGroup.setBeginIndex(coreCnt.get());
                coreCnt.addAndGet(item.getCount());
            }
            if (item.getVmRole().equalsIgnoreCase("task")) {
                vmGroup.setBeginIndex(taskCnt.get());
                taskCnt.addAndGet(item.getCount());
            }

            //构建虚拟机tags
            Map specmap = (Map) tagmap.clone();
            azureFleetService.buildVmsTags(specmap, confCluster, item, zone, azureSpotProfile);
            spec.setVirtualMachineTags(specmap);

            vmGroup.setVirtualMachineSpec(spec);
            vmGroup.setProvisionType("Azure_Fleet");
            virtualMachineGroups.add(vmGroup);
        });

        boolean getindexresult = createClusterBeginIndex(confCluster, coreCnt.get(), taskCnt.get());
        if (!getindexresult) {
            getLogger().error("创建集群生成index失败。");
        }

        azureFleetVmsRequest.setVirtualMachineGroups(virtualMachineGroups);

        res.put("azure", azureFleetVmsRequest);
        res.put("job", vmJob);
        return res;
    }

    /**
     * 创建集群生成task和core类型实例组的vm beginIndex
     *
     * @param confCluster
     * @param coreCnt
     * @param taskCnt
     * @return
     */
    private boolean createClusterBeginIndex(ConfCluster confCluster, Integer coreCnt, Integer taskCnt) {

        try {
            InfoClusterVmIndexManager infoClusterVmIndexManager = new InfoClusterVmIndexManager(this.getLogger(),
                    this.redisLock,
                    this.infoClusterVmMapper,
                    this.confScalingTaskMapper,
                    this.infoClusterVmIndexMapper,
                    this.infoClusterVmIndexHistoryMapper);

            if (coreCnt != null && coreCnt > 0) {
                int begindex = infoClusterVmIndexManager.requestNewVmIndex(
                        confCluster.getClusterId(),
                        "core",
                        "create",
                        new Date(),
                        coreCnt);
                getLogger().info("requestNewVmIndex_core:{}", begindex);
            }

            if (taskCnt != null && taskCnt > 0) {
                int begindex = infoClusterVmIndexManager.requestNewVmIndex(
                        confCluster.getClusterId(),
                        "task",
                        "create",
                        new Date(),
                        taskCnt);
                getLogger().info("requestNewVmIndex_task:{}", begindex);
            }
        } catch (Exception e) {
            getLogger().error("创建集群生成index,异常", e);
            return false;
        }
        return true;
    }

    /**
     * 获取vmjob
     *
     * @param clusterId
     * @param clusterName
     * @param operrationType
     * @param activityLogId
     * @return
     */
    @Override
    public InfoClusterVmJob getVmJob(String clusterId,
                                     String clusterName,
                                     String operrationType,
                                     String activityLogId) {

        HashMap<String, Object> paramap = new HashMap<>();
        paramap.put("clusterId", clusterId);
        paramap.put("operationType", operrationType);
        paramap.put("activityLogId", activityLogId);

        InfoClusterVmJob job = vmJobMapper.getVmJobByClusterIdAndOperation(paramap);
        if (job != null) {
            return job;
        } else {
            job = new InfoClusterVmJob();
            job.setClusterId(clusterId);
            job.setJobStatus(0);
            job.setOperationType(operrationType);
            job.setTransactionId(UUID.randomUUID().toString());
            job.setBegTime(new Date());
            job.setClusterName(clusterName);
            job.setActivityLogId(activityLogId);
            vmJobMapper.insertSelective(job);
            return job;
        }
    }


    /**
     * 根据集群ID 虚拟机内网 扩容任务ID 获取虚拟机信息
     *
     * @param clusterId
     * @param vmIps
     * @param scaleOutTaskId
     * @return
     */
    @Override
    public List<InfoClusterVm> getVMListByVMIps(String clusterId, List<String> vmIps, String scaleOutTaskId) {
        List<InfoClusterVm> rejectVms =
                infoClusterVmMapper.getVMListByClusterIdAndIpsAndScaleOutTaskId(clusterId, vmIps, scaleOutTaskId);
        getLogger().info("ByIps_RejectVms:" + rejectVms.toString());
        return rejectVms;
    }

    /**
     * 根据集群ID和vm hostname 获取虚拟机信息
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public List<InfoClusterVm> getVMListByHostNames(String clusterId, List<String> hostNames) {
        getLogger().info("clusterId:{},hostNames:{}", clusterId, hostNames);
        List<InfoClusterVm> rejectVms =
                infoClusterVmMapper.getVMListByClusterIdAndHostNamesAndScaleOutTaskId(clusterId, hostNames, null);
        getLogger().info("ByHostName_RejectVms:" + rejectVms.toString());

        return rejectVms;
    }

    /**
     * 保存剔除虚拟机信息
     * -- 保存infoclusterreject表数据信息+更新infoclustervm state 状态为unknown -99
     *
     * @param vms
     * @return
     */
    @Override
    public ResultMsg saveRejectVMs(List<InfoClusterVm> vms, String activityLogId) {

        ResultMsg msg = new ResultMsg();
        try {
            List<String> vmNames = new ArrayList<>();
            List<InfoClusterVmReject> vmRejects = new ArrayList<>();
            InfoClusterOperationPlanActivityLogWithBLOBs activityLog =
                    planActivityLogMapper.selectByPrimaryKey(activityLogId);

            vms.stream().forEach(x -> {
                vmNames.add(x.getVmName());

                InfoClusterVmReject vmReject = new InfoClusterVmReject();
                vmReject.setRejectId(UUID.randomUUID().toString().replaceAll("-", ""));
                vmReject.setClusterId(x.getClusterId());
                vmReject.setActivityLogId(activityLogId);
                vmReject.setActivityCnName(activityLog.getActivityCnname());
                vmReject.setCreatedTime(new Date());
                vmReject.setHostName(x.getHostName());
                vmReject.setInternalip(x.getInternalip());
                vmReject.setVmName(x.getVmName());
                vmReject.setPlanId(activityLog.getPlanId());
                vmRejects.add(vmReject);
            });

            //region 更新infoclustervm state 为UNKNOWN
            infoClusterVmMapper.batchUpdateVMState(vms.get(0).getClusterId(), vmNames, InfoClusterVm.VM_UNKNOWN);
            vmRejectMapper.bastchInsert(vmRejects);
            //endregion

            msg.setResult(true);

        } catch (Exception e) {
            getLogger().error("保存剔除节点信息异常", e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * ansible任务剔除节点
     *
     * @param clusterId
     * @param vmIps
     * @param currentActivityLog
     * @return
     */
    @Override
    public ResultMsg ansibleJobRejectNode(String clusterId, List<String> vmIps, InfoClusterOperationPlanActivityLogWithBLOBs currentActivityLog) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentActivityLog.getPlanId());
            String scaleOutTaskId = null;
            if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut)
                    && StringUtils.isNotEmpty(plan.getScalingTaskId())) {
                scaleOutTaskId = plan.getScalingTaskId();
            }
            List<InfoClusterVm> vms = getVMListByVMIps(clusterId, vmIps, scaleOutTaskId);

            ResultMsg ckmsg = checkRejectNode(clusterId, vms);
            if (!ckmsg.getResult()) {
                return ckmsg;
            }
            return saveRejectVMs(vms, currentActivityLog.getActivityLogId());
        } catch (Exception e) {
            getLogger().error("处理剔除节点异常，", e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return msg;
        }

    }

    /**
     * ambari任务剔除节点
     *
     * @param clusterId
     * @param vmHostNames
     * @param currentActivityLog
     * @return
     */
    @Override
    public ResultMsg ambariJobRejectNode(String clusterId, List<String> vmHostNames, InfoClusterOperationPlanActivityLogWithBLOBs currentActivityLog) {

        ResultMsg msg = new ResultMsg();
        try {
            List<InfoClusterVm> vms = getVMListByHostNames(clusterId, vmHostNames);

            ResultMsg ckmsg = checkRejectNode(clusterId, vms);
            if (!ckmsg.getResult()) {
                return ckmsg;
            }
            return saveRejectVMs(vms, currentActivityLog.getActivityLogId());
        } catch (Exception e) {
            getLogger().error("处理剔除节点异常，", e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }

    /**
     * 判断是否可以剔除节点
     * 剔除节点包含ambari master角色节点 false
     * 剔除后core节点数量低于3个  false
     *
     * @param clusterId
     * @param rejectVms
     * @return
     */
    @Override
    public ResultMsg checkRejectNode(String clusterId, List<InfoClusterVm> rejectVms) {

        ResultMsg msg = new ResultMsg();
        try {
            if (rejectVms == null || rejectVms.size() == 0) {
                msg.setResult(true);
                return msg;
            }

            // 获取当前集群core类型节点个数
            int core_cnt = infoClusterVmMapper.getClusterInstanceCountByVmRoleAndState(
                    clusterId,
                    "core",
                    InfoClusterVm.VM_RUNNING);

            // 按照节点类型分类group
            Map<String, List<InfoClusterVm>> groupVmRole = rejectVms.stream().collect(Collectors.groupingBy(item -> {
                return item.getVmRole();
            }));

            if (groupVmRole != null && (groupVmRole.containsKey("ambari") || groupVmRole.containsKey("master"))) {
                msg.setResult(false);
                getLogger().error("剔除的节点中包含ambari或master节点");
                msg.setErrorMsg("剔除的节点中包含ambari或master节点");
                return msg;
            }

            if (groupVmRole != null && groupVmRole.containsKey("core")) {
                int left_core_size = core_cnt - groupVmRole.get("core").size();
                if (left_core_size < coreMinNum) {
                    msg.setResult(false);
                    getLogger().error("剔除节点后，Core节点低于最小数量：" + coreMinNum);
                    msg.setErrorMsg("剔除节点后，Core节点低于最小数量：" + coreMinNum);
                    return msg;
                }
            }
            msg.setResult(true);
            return msg;

        } catch (Exception e) {
            getLogger().error("剔除节点校验异常，", e);
            msg.setResult(false);
            msg.setErrorMsg("剔除节点校验异常:" + ExceptionUtils.getStackTrace(e));
            return msg;
        }

    }

    /**
     * 判断是否启动清理VM计划
     *
     * @param planId 计划ID
     * @return
     */
    @Override
    public ResultMsg checkIsStartClearVM(String planId) {
        ResultMsg msg = new ResultMsg();

        List<InfoClusterVmReject> vmRejects = vmRejectMapper.getVmRejectsByPlanId(planId);
        if (vmRejects != null && vmRejects.size() > 0) {
            getLogger().info("发现需要清理的VM，启动清理计划，vms：" + vmRejects.toString());
            msg.setResult(true);
            return msg;
        }
        getLogger().info("没有发现需要清理的VM，无需启动清理计划。");
        return msg;
    }

    /**
     * 启动清理VM计划
     *
     * @param planId
     * @return
     */
    @Override
    public ResultMsg startClearVMPlan(String clusterId, String planId) {
        ResultMsg msg = new ResultMsg();
        try {
            getLogger().info("启动vm清理计划，plan_id:" + planId);
            ResultMsg ckMsg = checkIsStartClearVM(planId);
            if (!ckMsg.getResult()) {
                msg.setResult(true);
                return msg;
            }
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            ResultMsg planMsg = planExecService.createPlanAndRun(
                    clusterId,
                    confCluster.getClusterReleaseVer(),
                    InfoClusterOperationPlan.Plan_OP_ClearVMs,
                    null, planId);
            if (!planMsg.getResult()) {
                getLogger().error("创建清理vm计划失败，" + planMsg.toString());
            }
            return planMsg;
        } catch (Exception e) {
            getLogger().error("创建清理vm计划异常，", e);
            return msg;
        }
    }

    /**
     * 清理创建集群或扩容过程中，异常的vm<br/>
     * 清理步骤:azureVMService@deleteVmsForClearVM
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg deleteVmsForClearVM(String message) {
        ResultMsg resultMsg = new ResultMsg();

        // region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);


        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());

        //endregion

        try {
            //region 查询需要清理的vms
            // clearvm 使用optaskid 存储 上一个planid
            // 被清理的主机来自于之前的扩容任务, 创建清理VM任务时, 会将上次的扩容任务的planId保存到清理VM任务中,
            // 所以本次清理时, 需要根据上次的扩容的任务ID来获取待清理的VM
            List<InfoClusterVmReject> vmRejectList = vmRejectMapper.getVmRejectsByPlanId(plan.getOpTaskId());

            if (vmRejectList == null || vmRejectList.size() == 0) {
                getLogger().error("未查询到可以清理的Vm，planId：" + plan.getPlanId());
                param.put(JobNameConstant.Delete_VM, "000000");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                planExecService.complateActivity(currentLog);
                getLogger().info("--end_complateActivity");
                return resultMsg;
            }

            List<String> hostNames = new ArrayList<>();

            vmRejectList.stream().forEach(x -> {
                hostNames.add(x.getHostName());
            });
            //endregion

            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //region 获取任务要下线的机器列表
            List<InfoClusterVm> vmList =
                    infoClusterVmMapper.getVMListByClusterIdAndHostNamesAndScaleOutTaskId(
                            confCluster.getClusterId(),
                            hostNames,
                            null);
            //endregion

            //region 保存vm 到删除监控表
            ivmClearLogService.insertClearVms(confCluster.getClusterId(), plan.getPlanId(), vmList);
            //endregion

            //region 异步删除处理
            ResultMsg asyncDelMsg = ivmDeleteService.saveToAsyncDelete(confCluster.getRegion(), vmList, plan);
            if (asyncDelMsg.getResult()) {
                //异步删除提交完成
                // 使用6个0, 表示让后一步不用处理, 直接跳过去.
                param.put(JobNameConstant.Delete_VM, "000000");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                //region 更新机器的状态为删除中
                List<String> vmNames = new CopyOnWriteArrayList<>();
                vmList.stream().forEach(x -> {
                    vmNames.add(x.getVmName());
                });
                infoClusterVmMapper.batchUpdateVMState(confCluster.getClusterId(), vmNames, InfoClusterVm.VM_DELETING);
                //endregion
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(activityLogId, param);
                return resultMsg;
            }
            //endregion
            getLogger().info("不符合异步删除要求：" + asyncDelMsg.getErrorMsg());

            //region 生成vm任务
            InfoClusterVmJob vmJob =
                    getVmJob(confCluster.getClusterId()
                            , confCluster.getClusterName()
                            , InfoClusterOperationPlan.Plan_OP_ClearVMs
                            , activityLogId);
            //endregion

            ResultMsg msg = sendDeleteVMRequest(confCluster, vmList, vmJob);

            if (msg.getResult() && msg.getData() != null) {
                JSONObject response = (JSONObject) msg.getData();
                String jobid = response.getString("id");
                param.put(JobNameConstant.Delete_VM, jobid);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                getLogger().info(confCluster.getClusterName() + "--end_sendNextmsg");
            } else {
                currentLog.setLogs(msg.toString());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("---Delete VM Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        getLogger().info("--end_complateActivity");
        return resultMsg;
    }


    private ResultMsg sendDeleteVMRequest(ConfCluster confCluster, List<InfoClusterVm> vmList, InfoClusterVmJob vmJob) {

        ResultMsg msg = new ResultMsg();
        if (vmList == null || vmList.size() == 0) {
            return msg;
        }

        if (vmList != null && vmList.size() > 1) {
            //region 构建删除虚拟机请求体
            AzureDeleteVMsRequest deleteVMsRequest = buildAzureVmsDeleteReq(vmJob, vmList);
            getLogger().info("deleteVMsRequest:" + deleteVMsRequest.toString());
            //endregion

            //region 请求azure接口
            msg = deleteVirtualMachines(deleteVMsRequest, confCluster.getSubscriptionId());
            getLogger().info(confCluster.getClusterName() + "deleteVMsRequest,--end_deleteVms");
            //endregion
            return msg;
        }

        if (vmList != null && vmList.size() == 1) {
            msg = deleteSingleVirtualMachine(vmList.get(0).getVmName(), vmList.get(0).getHostName(), confCluster.getRegion());
            return msg;
        }
        return msg;
    }


    /**
     * Spot实例因逐出事件更新InfoClusterVM状态为UNKONOWN
     *
     * @param clusterId
     * @param hostName
     * @return
     */
    @Override
    public ResultMsg updateInfoClusterVMStateForSpotEvictionEvent(String clusterId, String hostName) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterVm vm = infoClusterVmMapper.selectVMByClusterIdAndHostName(clusterId, hostName);
            if (vm != null && vm.getState() == InfoClusterVm.VM_RUNNING) {

                //region 更新VM状态
                vm.setState(InfoClusterVm.VM_UNKNOWN);
                infoClusterVmMapper.updateByPrimaryKeySelective(vm);
                //endregion 更新VM状态

                //region 更新hostgroup inscount 数据
                hostGroupMapper.updateHostGroupInsCountForEvication(vm.getClusterId(), vm.getGroupName());
                //endregion

                //region 删除逐出VMDNS记录
                List<String> vmNames = new ArrayList<>();
                vmNames.add(vm.getVmName());
                List<String> dnsNames = new ArrayList<>();
                dnsNames.add(vm.getHostName());
                ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(vm.getClusterId());
                syncDeleteVms(vmNames, dnsNames, confCluster, "00000");
                //endregion 删除逐出VMDNS记录

                msg.setResult(true);
            }
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            getLogger().error("逐出事件更新VM状态异常：", e);
        }
        return msg;
    }

    @Override
    public ResultMsg initEvictVms(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();

        // region 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);


        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        //endregion

        /*
        1. 获取到所有VM
        2. 检查VM的有效性: socket ping一下
            如果VM无效, 将VM清理掉
        3. 修改扩容数量
        */
        currentLog.setEndtime(new Date());
        currentLog.setState(InfoClusterOperationPlanActivityLogWithBLOBs.ACTION_COMPLETED);
        planExecService.complateActivity(currentLog);
        planExecService.sendNextActivityMsg(activityLogId, param);
        getLogger().info("--end_complateActivity");
        return resultMsg;
    }

    /**
     * 请求pv2磁盘性能调整
     * @return
     */
    @Override
    public ResultMsg diskPerformanceAdjust(String messageParam) {
        ResultMsg resultMsg = new ResultMsg();
        JSONObject param = JSON.parseObject(messageParam);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());
        String scalingTaskId = plan.getScalingTaskId();

        ConfScalingTask confScalingTask = confScalingTaskMapper.selectByPrimaryKey(scalingTaskId);
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        List<InfoClusterVm> infoClusterVms;
        if (StrUtil.isEmpty(confScalingTask.getScaleoutTaskId())){
            //获取集群中正在运行的所有主机名称
            infoClusterVms = infoClusterVmMapper.selectRunningVmsByClusterIdAndGroupName(confScalingTask.getClusterId(),
                    confScalingTask.getGroupName());
        }else{
            infoClusterVms = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskIdAndState(confScalingTask.getClusterId(),
                    confScalingTask.getScaleoutTaskId(),
                    InfoClusterVm.VM_RUNNING);
        }
        List<String> vmNames = infoClusterVms.stream().map(InfoClusterVm::getVmName).collect(Collectors.toList());
        VMDiskRequest VMDiskRequest = new VMDiskRequest();
        VMDiskRequest.setSubscriptionId(confCluster.getSubscriptionId());
        VMDiskRequest.setClusterName(confCluster.getClusterName());
        VMDiskRequest.setRegion(confCluster.getRegion());
        VMDiskRequest.setApiVersion("4.0");
        VMDiskRequest.setNewDataDiskIOPSReadWrite(confScalingTask.getBeforeScalingCount());
        VMDiskRequest.setNewDataDiskMBpsReadWrite(confScalingTask.getAfterScalingCount());
        VMDiskRequest.setTransactionId(UUID.randomUUID().toString());
        VMDiskRequest.setGroupName(confScalingTask.getGroupName());
        VMDiskRequest.setVmNames(vmNames);
        resultMsg = azureService.updateVMsDiskIOPSAndMbps(VMDiskRequest);
        getLogger().info("请求pv2磁盘性能调整返回信息:{}",resultMsg);
        if (resultMsg.getResult()) {
            JSONObject data = (JSONObject) resultMsg.getData();
            String jobid = data.getString("id");
            if (StrUtil.isNotEmpty(jobid)) {
                currentLog.setEndtime(new Date());
                currentLog.setState(InfoClusterOperationPlanActivityLogWithBLOBs.ACTION_COMPLETED);
                planExecService.complateActivity(currentLog);
                param.put(JobNameConstant.Cluster_VM, jobid);
                //10s以后在执行下一步
                planExecService.sendNextActivityDelayMsg(activityLogId, param, 10L);
                resultMsg=ResultMsg.SUCCESS("请求pv2磁盘性能调整成功");
            } else {
                getLogger().error("not found diskAdjustment jobid,activityLogId:" + activityLogId);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("not found diskAdjustment jobid,activityLogId:" + activityLogId);
                planExecService.complateActivity(currentLog);
                resultMsg=ResultMsg.FAILURE("请求pv2磁盘性能调整失败,not found diskAdjustment jobid,activityLogId:{}",activityLogId);
            }
        }else {
            resultMsg = ResultMsg.FAILURE("请求pv2磁盘性能调整失败,返回结果失败");
        }
        return resultMsg;
    }

    /**
     * 查询pv2磁盘性能调整结果
     * @param messageParam
     * @return
     */
    @Override
    public ResultMsg queryDiskPerformanceAdjust(String messageParam) {
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageParam);
        String cvmjobId = param.getString(JobNameConstant.Cluster_VM);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);

        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        ResultMsg msg = azureService.getJobsStatus(cvmjobId, confCluster.getSubscriptionId());

        if (msg.getData() != null) {
            // 这个是调用Azure接口返回来的完整数据,不有管前面的各种转换
            JSONObject response = (JSONObject) msg.getData();
            String status = response.getString("status");
            // 申请资源完成, 保存申请信息
            if ("Completed".equals(status)) {
                currentLog.setEndtime(new Date());
                currentLog.setState(InfoClusterOperationPlanActivityLogWithBLOBs.ACTION_COMPLETED);
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(activityLogId,param);
                return ResultMsg.SUCCESS("磁盘性能调整成功");
            }else {
                long ttl = System.currentTimeMillis() - currentLog.getBegtime().getTime();
                //失败过快，等一等
                if (ttl < 1000 * 30) {
                    getLogger().warn("查询pv2磁盘调整性能结果失败速度过快，sleep 60s");
                    return planExecService.loopActivity(clientname, messageParam,
                            60L, activityLogId);
                }
            }
        }
        getLogger().info("查询pv2磁盘性能调整结果异常：{}", messageParam);
        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        currentLog.setLogs(msg.toString());
        planExecService.complateActivity(currentLog);
        return ResultMsg.FAILURE("查询pv2磁盘性能调整结果异常");
    }
}
