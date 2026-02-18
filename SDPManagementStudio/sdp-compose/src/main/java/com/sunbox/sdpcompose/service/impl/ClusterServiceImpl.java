package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.sunbox.dao.mapper.*;
import com.sunbox.sdpcompose.mapper.ConfClusterVmMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.AmbariComponentLayout;
import com.sunbox.domain.metaData.SSHKeyPair;
import com.sunbox.domain.metaData.keyVault;
import com.sunbox.domain.server.Sys;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.consts.JobNameConstant;
import com.sunbox.sdpcompose.enums.AmbariHostState;
import com.sunbox.sdpcompose.hostgroup.ClusterHostGroupManager;
import com.sunbox.sdpcompose.manager.ConfScalingTaskManager;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpcompose.mapper.ConfScalingTaskMapper;
import com.sunbox.sdpcompose.model.azure.request.ConfigProperties;
import com.sunbox.sdpcompose.model.azure.request.UpdateClusterConfigData;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.sdpcompose.service.ambari.*;
import com.sunbox.sdpcompose.service.ambari.blueprint.*;
import com.sunbox.sdpcompose.service.ambari.configgeneerator.CustomConfigGenerator;
import com.sunbox.sdpcompose.service.ambari.configgeneerator.CustomConfigGeneratorFactory;
import com.sunbox.sdpcompose.service.ambari.enums.*;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.sdpcompose.util.NumberUtils;
import com.sunbox.sdpservice.service.ScaleService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.INeoAmbariService;
import com.sunbox.service.IVMClearLogService;
import com.sunbox.util.DistributedRedisLock;

import com.sunbox.util.KeyVaultUtil;
import com.sunbox.web.BaseCommonInterFace;
import io.jsonwebtoken.lang.Assert;
import io.jsonwebtoken.lang.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.ApiException;
import sunbox.sdp.ambari.client.api.ClustersApi;
import sunbox.sdp.ambari.client.api.CustomActionApi;
import sunbox.sdp.ambari.client.model.customaction.*;
import sunbox.sdp.ambari.client.model.customaction.enums.ConfigGroupField;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sunbox.domain.ConfCluster.CREATED;
import static com.sunbox.domain.InfoClusterVm.VM_RUNNING;

/**
 * @author : [niyang]
 * @className : ClusterServiceImpl
 * @description : [集群操作服务，包含创建集群、销毁集群等操作]
 * 2023.1.15 新增集群扩容缩容逻辑。
 * @createTime : [2022/11/30 4:48 PM]
 */
@Service("clusterservice")
public class ClusterServiceImpl implements IClusterService, BaseCommonInterFace {

    private static final Logger log = LoggerFactory.getLogger(ClusterServiceImpl.class);
    @Autowired
    private KeyVaultUtil keyVaultUtil;
    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmMapper vmMapper;

    @Autowired
    ConfClusterVmMapper confClusterVmMapper;

    @Autowired
    private ConfClusterAppMapper appMapper;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private ConfClusterVmDataVolumeMapper dataVolumeMapper;

    @Autowired
    AmbariConfigItemMapper itemMapper;

    @Autowired
    private ConfClusterAppsConfigMapper appsConfigMapper;

    @Autowired
    private InfoClusterComponentLayoutMapper clusterComponentLayoutMapper;

    @Autowired
    private AmbariComponentLayoutMapper ambariComponentLayoutMapper;

    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    @Autowired
    private IAmbariService ambariService;

    @Autowired
    private IPlanExecService planExecService;

    @Autowired
    private ConfScalingTaskMapper taskMapper;

    @Autowired
    private IPlayBookService playBookService;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private ConfClusterOpTaskMapper opTaskMapper;

    @Autowired
    private ConfGroupElasticScalingRuleMapper ruleMapper;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private ConfClusterOpTaskMapper confClusterOpTaskMapper;

    @Autowired
    private ConfClusterAppMapper clusterAppMapper;

    @Autowired
    private InfoAmbariConfigGroupMapper ambariConfigGroupMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private InfoClusterVmRejectMapper vmRejectMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfScalingTaskManager confScalingTaskManager;

    @Value("${compose.message.clientname}")
    private String clientname;

    @Value("${ambari.default.user}")
    private String username;

    @Value("${ambari.default.pwd}")
    private String password;

    @Value("${install.waitingtime:60}")
    private String waitingtme;

    @Value("${install.pendinghosttimeout:300}")
    private String pendingHostTimeOut;

    @Value("${hadoop.jmx.api.port:8088}")
    private int hadoopJmxApiPort;

    @Value("${activity.timeout}")
    private String activityTimeout;

    //ambari安装过程中降级开关
    @Value("${sdp.install.ambari.reduce:0}")
    private Integer sdpAmbariReduce;

    // 安装过程中检查agent注册server 重试次数
    @Value("${sdp.install.reghost.check.retrytimes:5}")
    private Integer installsdp_retry_cnt;

    // 安装过程中检查agent注册server重试间隔时间
    @Value("${sdp.install.reghost.check.retrywaittimes:30}")
    private Long installsdp_retry_waittimes;

    // 安装步骤中启动SDP重试功能开关
    @Value("${sdp.start.retry.enable:1}")
    private Integer startsdp_retry_enable;

    // 安装步骤中启动SDP重试次数
    @Value("${sdp.start.retrytimes:10}")
    private Integer startsdp_retry_cnt;

    // 安装步骤中启动SDP重试间隔时间
    @Value("${sdp.start.retrywaittimes:30}")
    private Long startsdp_retry_waittimes;

    // 安装过程中检查agent注册server 重试次数
    @Value("${sdp.install.configgroups.query.retrytimes:5}")
    private Integer sdpConfigGroupsQueryRetryTimes;

    // 安装过程中检查agent注册server 重试次数
    @Value("${sdp.install.configgroups.query.retrywaittimes:30}")
    private Integer sdpConfigGroupsQueryRetryWaittimes;

    // 安装组件重试次数
    @Value("${sdp.scaleout.hostcomponent.retrytimes:5}")
    private Integer sdpInstallHostComponentRetryTimes;

    // 安装组件重试间隔时间
    @Value("${sdp.scaleout.hostcomponent.retrywaittimes:30}")
    private Integer sdpInstallHostComponentRetryWaitTimes;

    // decommission 重试次数
    @Value("${sdp.scalein.decommission.retryCount:5}")
    private Integer sdpScaleInDecommissionRetryCount;

    // decommission 重试间隔时间
    @Value("${sdp.scalein.decommission.retryDuration:30}")
    private Integer sdpScaleInDecommissionRetryDuration;

    // 扩容启动组件重试次数
    @Value("${sdp.scaleout.starthostcomponent.retrytimes:5}")
    private Integer sdpScaleOutStartHostComponentRetryTimes;

    // 扩容启动组件重试间隔时间
    @Value("${sdp.scaleout.starthostcomponent.retrywaittimes:30}")
    private Integer sdpScaleOutStartHostComponentRetryWaitTimes;


    // 创建集群启动组件重试次数
    @Value("${sdp.start.apps.retrytimes:5}")
    private Integer sdpStartAppsetryTimes;

    // 创建集群启动组件重试间隔时间
    @Value("${sdp.start.apps.retrywaittimes:30}")
    private Integer sdpStartAppsRetryWaitTimes;

    /**
     * 是否开启Ambari Api调用调试模式, 1:开启  0:不开启 <br/> 默认开启调试模式,便于查问题
     */
    @Value("${ambari.api.debug}")
    private String debug = "1";

    //因异常导致的巡检超时状态，允许重试的次数
    @Value("${sdp.check.activity.timeout.retry:1}")
    private Integer sdpCheckActivityTimeoutRetry;

    //因异常导致的巡检超时状态，允许重试的次数内，每次间隔事件
    @Value("${sdp.check.activity.timeout.retry.duration:600}")
    private Integer sdpCheckActivityTimeoutRetryDuration;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    private ConfClusterHostGroupAppsConfigMapper confClusterHostGroupAppsConfigMapper;

    @Autowired
    private IVMClearLogService clearLogService;

    @Autowired
    private INeoAmbariService neoAmbariService;

    @Autowired
    private InfoClusterVmClearLogMapper clearLogMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private InfoClusterInfoCollectLogMapper infoClusterInfoCollectLogMapper;
    /**
     * 调用ambari接口安装集群
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg createSDPCluster(String messageparam) {

        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            ResultMsg ckmsg = getRegHosts(confCluster.getClusterId(),null);

            // region 集群vm注册数量检查
            if (!ckmsg.getResult()){
                // 重试
                if (!param.containsKey("createSDP_retry") || param.getInteger("createSDP_retry") < installsdp_retry_cnt){
                    int retry_cnt = 0;
                    if (param.containsKey("createSDP_retry")){
                        retry_cnt = param.getInteger("createSDP_retry");
                    }
                    retry_cnt++;
                    param.put("createSDP_retry",retry_cnt);
                    getLogger().warn("createSDP_retry，vm注册数量有缺失，重试次数："+ retry_cnt);
                    planExecService.loopActivity(clientname,JSON.toJSONString(param),installsdp_retry_waittimes,activityLogId);
                    return resultMsg;
                }else{
                    // 重试完成后依旧，有丢失注册，进入剔除节点判断
                    ResultMsg ckrmsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),ckmsg.getRows(),currentLog);

                    if (!ckrmsg.getResult()){
                        // 剔除节点失败
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        currentLog.setLogs("VM注册Ambariserver节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        getLogger().error("VM注册AmbariServer节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        planExecService.complateActivity(currentLog);
                        return resultMsg;
                    }
                }
            }

            param.remove("createSDP_retry");

            //endregion

            InProgressResult msg;
            if (StringUtils.isEmpty(confCluster.getSrcClusterId())) {
                CreateClusterCmd cmd = buildCreateClusterCmd(confCluster.getClusterId());
                getLogger().info("CreateClusterCMD:" + JSON.toJSONString(cmd));
                msg = ambariService.createCluster(cmd);
                param.put(JobNameConstant.Switch_Ambari_Reduce,1);
                param.put("ambariInfo", cmd.getAmbarInfo());

            } else {
                DuplicateClusterCmd cmd = buildDuplicateClusterCmd(confCluster);
                msg = ambariService.duplicateCluster(cmd);
                param.put(JobNameConstant.Switch_Ambari_Reduce,1);
                param.put("ambariInfo", cmd.getAmbarInfo());
            }

            if (msg.isSuccess()) {
                param.put(JobNameConstant.Install_Cluster_App, msg.getRequestId());
                param.put("clusterName", msg.getClusterName());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                //更新集群配置组状态未运行中
                confClusterHostGroupMapper.updateByClusterId(confCluster.getClusterId(), ConfClusterHostGroup.STATE_RUNNING);
                planExecService.sendNextActivityMsg(activityLogId, param);
            } else {
                currentLog.setLogs(msg.getMessage());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    public void testCreateCluster(String clusterId) {
        CreateClusterCmd cmd = buildCreateClusterCmd(clusterId);
        getLogger().info("CreateClusterCMD:" + JSON.toJSONString(cmd));
        ambariService.createCluster(cmd);
    }

    /**
     * 调用ambari接口查询集群安装进度
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg querySDPClusterInstallProcess(String messageparam) {
        ResultMsg resultMsg = new ResultMsg();
        try {

            // 0. 解析参数
            JSONObject param = JSON.parseObject(messageparam);
            String rpjobId = param.getString(JobNameConstant.Install_Cluster_App);
            String activityLogId = param.getString("activityLogId");


            InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                    = planExecService.getInfoActivityLogByLogId(activityLogId);
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            getLogger().info("获取ActivityLog:" + currentLog.toString());


            if(Objects.equals(rpjobId,"000000")){
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("已经执行成功，无须重复执行。");
                param.remove(JobNameConstant.Switch_Ambari_Reduce);
                param.remove(JobNameConstant.Switch_Ambari_Retry);
                param.remove(JobNameConstant.Param_Ambari_Retry_obj);

                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            QueryProgressCmd qcmd = getQueryProgressCmd(rpjobId, confCluster);
            QueryProgressResult msg ;
            try {
                msg = ambariService.queryCreateClusterProgress(qcmd);
            }catch (Exception e){
                getLogger().error("请求ambari异常，",e);
                resultMsg = planExecService.loopActivity(clientname, messageparam, 30L, activityLogId);
                return resultMsg;
            }
            getLogger().info("请求Ambari结束。");
            if (msg != null) {
                msg.setTaskList(null);
                getLogger().info("查询Ambari接口返回:" + JSONObject.toJSONString(msg));
            }

            //region 任务执行成功
            if (msg.isSuccess()) {
                getLogger().info("集群启动完成。");
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(JSON.toJSONString(msg));
                param.remove(JobNameConstant.Switch_Ambari_Reduce);
                param.remove(JobNameConstant.Switch_Ambari_Retry);
                param.remove(JobNameConstant.Param_Ambari_Retry_obj);
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }
            //endregion

            //region 任务执行失败
            if (msg.isFail()) {
                //region 重试
                ResultMsg ckretryMsg = checkAmbariRetry(param);
                if (ckretryMsg.getResult()){
                    JSONObject retryObj = JSON.parseObject(JSON.toJSONString(ckretryMsg.getData()));
                    ResultMsg sendmsg = sendAmbariRetryMessage(retryObj,currentLog);

                    if (!sendmsg.getResult()){
                        getLogger().error("发送重试消息失败，忽略，流程继续。");
                    }else {
                        return resultMsg;
                    }
                }
                getLogger().warn("重试："+ckretryMsg.getErrorMsg());
                //endregion

                //region 降级
                if(sdpAmbariReduce.equals(1) && param.containsKey(JobNameConstant.Switch_Ambari_Reduce) &&
                        param.getInteger(JobNameConstant.Switch_Ambari_Reduce).equals(1)){
                    try {
                        getLogger().info("处理安装过程中异常的VM，clusterId:"+confCluster.getClusterId());
                        QueryProgressResult rs = ambariService.queryCreateClusterProgressWithAllTask(qcmd);
                        List<String> failhosts=rs.getFailHosts();
                        getLogger().error("ambari任务failhost："+failhosts);
                        ResultMsg rejectMsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),failhosts,currentLog);

                        deleteAmbariFailedHost(confCluster,failhosts);

                        if (rejectMsg.getResult()){
                            resultMsg.setResult(true);
                            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                            currentLog.setLogs(JSON.toJSONString(msg));
                            planExecService.complateActivity(currentLog);
                            param.remove(JobNameConstant.Switch_Ambari_Reduce);
                            param.remove(JobNameConstant.Switch_Ambari_Retry);
                            param.remove(JobNameConstant.Param_Ambari_Retry_obj);
                            planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);

                            return resultMsg;
                        }
                    }catch (Exception e){
                        getLogger().error("降级异常，",e);
                    }
                 }
                //endregion

                return ambariFailed(currentLog, msg);
            }

            //endregion

            //region 任务pending
            if (msg.isPending()) {
                Long timeout = Long.parseLong(pendingHostTimeOut);
                Long duration = (System.currentTimeMillis() - currentLog.getBegtime().getTime()) / 1000;
                if (duration.compareTo(timeout) > 0) {
                    //region 重试
                    ResultMsg ckretryMsg = checkAmbariRetry(param);

                    if (ckretryMsg.getResult()){
                        JSONObject retryObj = JSON.parseObject(JSON.toJSONString(ckretryMsg.getData()));
                        ResultMsg sendmsg = sendAmbariRetryMessage(retryObj,currentLog);

                        if (!sendmsg.getResult()){
                            getLogger().error("发送重试消息失败，忽略，流程继续。");
                        }else {
                            return resultMsg;
                        }
                    }
                    getLogger().warn("重试："+ckretryMsg.getErrorMsg());
                    //endregion

                    //region 降级
                    if(sdpAmbariReduce.equals(1) && param.containsKey(JobNameConstant.Switch_Ambari_Reduce)
                            && param.getInteger(JobNameConstant.Switch_Ambari_Reduce).equals(1)){
                        try {
                            getLogger().info("处理安装过程中异常的VM，clusterId:"+confCluster.getClusterId());
                            QueryProgressResult rs = ambariService.queryCreateClusterProgressWithAllTask(qcmd);
                            List<String> failhosts=rs.getFailHosts();
                            getLogger().error("ambari任务failhost："+failhosts);
                            deleteAmbariFailedHost(confCluster,failhosts);
                            ResultMsg rejectMsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),failhosts,currentLog);
                            if (rejectMsg.getResult()){
                                resultMsg.setResult(true);
                                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                                currentLog.setLogs(JSON.toJSONString(msg));
                                planExecService.complateActivity(currentLog);
                                param.remove(JobNameConstant.Switch_Ambari_Reduce);
                                param.remove(JobNameConstant.Switch_Ambari_Retry);
                                param.remove(JobNameConstant.Param_Ambari_Retry_obj);
                                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                                return resultMsg;
                            }
                        }catch (Exception e){
                            getLogger().error("降级异常，",e);
                        }
                    }
                    //endregion
                    return ambariFailed(currentLog, msg);
                }
                getLogger().info("PendingHost,缓冲时间内，继续loopquery。");
            }
            //endregion

            resultMsg = planExecService.loopActivity(clientname, messageparam, 30L, activityLogId);
        } catch (Exception e) {
            getLogger().error("querySDPClusterInstallProcess:", e);
        }
        return resultMsg;
    }

    /**
     * 删除ambari安装SDP过程中的失败VM
     *
     * @param confCluster
     * @param failhosts
     * @return
     */
    private ResultMsg deleteAmbariFailedHost(ConfCluster confCluster,List<String> failhosts){
        ResultMsg msg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                ambariService.deleteHosts(getAmbariInfo(confCluster.getClusterId()),
                        getSdpClusterNameforAmbari(confCluster.getClusterId()),
                        failhosts);
                getLogger().info("完成删除failhosts。");
                msg.setResult(true);
                return msg;
            } catch (Exception e) {
                getLogger().error("删除失败的hosts 异常。",e);
            }
            i++;
            if (i > 5){
                return msg;
            }
            Integer delay = (int)Math.pow(2,i);
            ThreadUtil.sleep(1000 * delay);
        }
    }

    @Override
    public ResultMsg enableClusterAutoStart(ConfCluster confCluster) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            ambariService.enableClusterAutoStart(
                    this.getAmbariInfo(confCluster.getClusterId()),
                    this.getSdpClusterNameforAmbari(confCluster.getClusterId()),
                    this.getClusterSevice(confCluster.getClusterId()));
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("集群自动启动配置异常，", e);
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    /**
     * 查询步骤发送Amabari重试下消息
     *
     * @param retryParam 重试参数
     * @param currentLog 当前步骤对象
     * @return
     */
    private ResultMsg sendAmbariRetryMessage(JSONObject retryParam, InfoClusterOperationPlanActivityLogWithBLOBs currentLog){

        ResultMsg msg = new ResultMsg();
        try {
            // 获取上一步
            InfoClusterOperationPlanActivityLogWithBLOBs preActivityLog =
                    planExecService.getPreviousActivity(currentLog.getPlanId(), currentLog.getActivityLogId());
            //更新当前步骤为待执行
            currentLog.setState(0);
            planActivityLogMapper.updateByPrimaryKeySelective(currentLog);

            JSONObject parmJsobj = JSON.parseObject(preActivityLog.getParaminfo());
            parmJsobj.put(JobNameConstant.Param_Ambari_Retry_obj, retryParam);
            preActivityLog.setParaminfo(JSON.toJSONString(parmJsobj));

            return planExecService.sendPrevActivityMsg(preActivityLog, startsdp_retry_waittimes);
        }catch (Exception e){
            getLogger().error("发送Ambari，retry消息异常，",e);
            msg.setResult(false);
            msg.setErrorMsg("发送Ambari，retry消息异常，"+ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }

    /**
     * ambari 降级
     *
     * @param clusterId
     * @param hostNames
     * @param activityLog
     * @return
     */
    private ResultMsg ambariReduce(String clusterId,List<String> hostNames,InfoClusterOperationPlanActivityLogWithBLOBs activityLog){
        return ivmService.ambariJobRejectNode(clusterId,hostNames,activityLog);
    }


    /**
     * 构建查询ambari查询任务消息体
     *
     * @param rpjobId     任务ID
     * @param confCluster 集群信息
     * @return
     */
    private QueryProgressCmd getQueryProgressCmd(String rpjobId, ConfCluster confCluster) {
        if (Objects.isNull(rpjobId)) {
            getLogger().warn("向Ambari查询任务进展时, requestId 为空");
            return null;
        }
        QueryProgressCmd qcmd = new QueryProgressCmd();
        qcmd.setClusterName(getSdpClusterNameforAmbari(confCluster.getClusterId()));
        qcmd.setAmbariInfo(getAmbariInfo(confCluster.getClusterId()));
        qcmd.setRequestId(Long.valueOf(rpjobId));
        getLogger().info("请求Ambari参数：{}", qcmd);
        return qcmd;
    }


    /**
     * @param activityLog
     * @param msg
     * @return
     */
    private ResultMsg ambariFailed(InfoClusterOperationPlanActivityLogWithBLOBs activityLog, QueryProgressResult msg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(false);
        activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        activityLog.setLogs(JSON.toJSONString(msg));
        planExecService.complateActivity(activityLog);
        return resultMsg;
    }


    /**
     * 调用ambari接口启动集群
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg startSDPClusterApps(String messageparam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            JSONObject paramjson = JSON.parseObject(currentLog.getParaminfo());
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());

            InProgressResult clusterResult = startAllClusterServices(confCluster, ambariInfo);
            if (clusterResult != null && clusterResult.isSuccess()) {
                String jobid;
                if (clusterResult.getRequestId() ==null){
                    jobid = "000000";
                }else{
                     jobid = clusterResult.getRequestId() + "";
                }
                paramjson.put(JobNameConstant.Install_Cluster_App, jobid);
                paramjson.put("jobName",JobNameConstant.Start_Cluster_Apps);
                // 下一步允许重试
                paramjson.put(JobNameConstant.Switch_Ambari_Retry,1);
                paramjson.put(JobNameConstant.Switch_Ambari_Reduce,1);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, paramjson);
                resultMsg.setResult(true);
            }else{
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                if (Objects.isNull(clusterResult)) {
                    currentLog.setLogs("发起启动集群请求失败: 请求启动服务时Ambari返回空报文,需登录Ambari查看具体出错原因。");
                } else {
                    currentLog.setLogs("发起启动集群请求失败: " + JSON.toJSONString(clusterResult));
                }
            }
        } catch (Exception e) {
            getLogger().error("---Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     *  创建集群发送启动集群组件的请求，with重试
     *
     * @param confCluster
     * @param ambariInfo
     * @return
     */
    private InProgressResult startAllClusterServices(ConfCluster confCluster, AmbariInfo ambariInfo) {
        InProgressResult clusterResult = null;
        Integer i = 0;
        while(true){
            try {
                if (!confCluster.getState().equals(1)){
                    getLogger().error("集群状态不可用,终止Ambari启动集群: clusterId={}, clusterName={}, state={}",
                            confCluster.getClusterId(), confCluster.getClusterName(), confCluster.getState());
                    clusterResult = new InProgressResult();
                    clusterResult.setSuccess(false)
                            .setMessage("集群状态不可用，可能在安装过程中被提前销毁，终止Ambari启动集群：state=" + confCluster.getStateStr())
                            .setClusterName(confCluster.getClusterName())
                            .setRequestId(0L);
                    return clusterResult;
                }
                clusterResult = ambariService.startAllClusterServices(ambariInfo,
                        getSdpClusterNameforAmbari(confCluster.getClusterId()), confCluster.getClusterId());
                if (clusterResult.isSuccess()){
                    return clusterResult;
                }
            }catch (Exception e){
                getLogger().error("发送Ambari请求-启动集群组件，异常",e);
            }
            i++;
            if (i > sdpStartAppsetryTimes){
                return clusterResult;
            }
            ThreadUtil.sleep(1000 * sdpStartAppsRetryWaitTimes);
        }
    }

    /**
     * 检测Ambari是否可以retry
     *
     * @param param
     * @return
     */
    private ResultMsg checkAmbariRetry(JSONObject param) {
        ResultMsg msg = new ResultMsg();

        if (startsdp_retry_enable.equals(0)){
            msg.setResult(false);
            msg.setErrorMsg("重试功能未开启，请在配置中心SDP-Compose模块开启，配置项：sdp.start.retry.enable ，（0 关闭 1 开启）");
            getLogger().warn(msg.getErrorMsg());
            return msg;
        }

        if (!param.containsKey(JobNameConstant.Switch_Ambari_Retry)
                || param.getInteger(JobNameConstant.Switch_Ambari_Retry).equals(0)){
            msg.setResult(false);
            msg.setErrorMsg("当前步骤暂不支持重试。");
            getLogger().warn(msg.getErrorMsg());
            return msg;
        }
        if (!param.containsKey(JobNameConstant.Param_Ambari_Retry_obj)){
            //未重试过，构建重试信息结构体
            Map<String,Object> ansibleResryObj = new HashMap<>();
            ansibleResryObj.put("count",1);
            msg.setResult(true);
            msg.setData(ansibleResryObj);
            return msg;
        }else{
            //已有重试记录
            JSONObject restyJsonObj = param.getJSONObject(JobNameConstant.Param_Ambari_Retry_obj);
            Integer retryCnt = restyJsonObj.getInteger("count");

            if (retryCnt >= startsdp_retry_cnt){
                msg.setResult(false);
                msg.setErrorMsg("重试次数达到最大重试次数："  + startsdp_retry_cnt);
                getLogger().warn(msg.getErrorMsg());
                return msg;
            }
            retryCnt++;
            restyJsonObj.put("count",retryCnt);

            msg.setResult(true);
            msg.setData(restyJsonObj);
            return msg;
        }
    }

    /**
     * 批量设置集群组件自动启动
     *
     * @return
     */
    @Override
    @Scheduled(initialDelay = 20000L, fixedDelay = 300000L)
    public void batchEnableClusterAutoStart() {

        String lock_key = "batchEnableClusterAutoStart";
        boolean lock = redisLock.tryLock(lock_key);
        if (lock) {
            getLogger().info("启动定时设置ambariAutostart");
            try {
                List<InfoClusterVm> vms = infoClusterVmMapper.selectAllAvaliableAmbari();

                CopyOnWriteArrayList<InfoClusterVm> vms2 = new CopyOnWriteArrayList<>();
                vms.stream().forEach(x->{
                    boolean ckresult = confScalingTaskManager.hasCreatedOrRunningScalingTask(x.getClusterId());
                    if (!ckresult){
                        vms2.add(x);
                    }
                });

                getLogger().info("需要设置的集群数量："+vms2.size());
                getLogger().info("开始取消集群自启动");
                vms2.stream().forEach(x -> {
                    getLogger().info("取消自启动的集群Ambari HostName：" + x.getHostName());
                    if (!isHostConnectable(x.getInternalip(), 8080)) {
                        getLogger().error(x.getInternalip() + ",网络不可达。");
                        return;
                    }
                    String clusterName = this.getSdpClusterNameforAmbari(x.getClusterId());
                    AmbariInfo ambariInfo = this.getAmbariInfo(x.getClusterId());
                    List<String> clusterServices = this.getClusterSevice(x.getClusterId());

                    try {
                        //getLogger().info("取消自启动请求Ambari开始："+x.getInternalip());
                        ambariService.disableClusterAutoStart(
                                ambariInfo,
                                clusterName,
                                clusterServices);
                        // getLogger().info("取消自启动请求Ambari结束："+x.getInternalip());
                    } catch (Exception e) {
                        getLogger().error(x.getClusterId()+":取消集群自启动，异常", e);
                        return;
                    }
                });
                getLogger().info("完成取消集群自启动");
                ThreadUtil.sleep(1000 * 10);

                vms2.stream().forEach(x -> {
                    getLogger().info("启用自启动的集群Ambari HostName：" + x.getHostName());
                    if (!isHostConnectable(x.getInternalip(), 8080)) {
                        getLogger().error(x.getInternalip() + ",网络不可达。");
                        return;
                    }
                    String clusterName = this.getSdpClusterNameforAmbari(x.getClusterId());
                    AmbariInfo ambariInfo = this.getAmbariInfo(x.getClusterId());
                    List<String> clusterServices = this.getClusterSevice(x.getClusterId());
                    try {
                        //getLogger().info("设置自启动请求Ambari开始："+x.getInternalip());
                        ambariService.enableClusterAutoStart(
                                ambariInfo,
                                clusterName,
                                clusterServices);
                        //getLogger().info("设置自启动请求Ambari结束："+x.getInternalip());
                    } catch (Exception e) {
                        getLogger().info(x.getClusterId()+":设置集群自启动，异常", e);
                    }
                });
            } catch (Exception e) {
                getLogger().error("设置集群自启动处理异常", e);
            } finally {
                if (lock) {
                    redisLock.unlock(lock_key);
                }
            }
        } else {
            getLogger().error("获取锁失败。");
        }
        getLogger().info("结束启动定时设置ambariAutostart");
    }

    private boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 1500);
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * sleep
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg sleep(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        try {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            Long wating = Long.valueOf(waitingtme);
            planExecService.sendNextActivityDelayMsg(activityLogId, param, wating);
            resultMsg.setResult(true);
        } catch (Exception e) {
            getLogger().error("-- Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }


    /**
     * ambariserver中新增host
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg ambariAddHosts(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 获取当前aciton信息
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            ResultMsg ckmsg = getRegHosts(confCluster.getClusterId(),taskId);

            // region 集群vm注册数量检查
            if (!ckmsg.getResult()){
                // 重试
                if (!param.containsKey("addhost_retry") || param.getInteger("addhost_retry") < installsdp_retry_cnt){
                    int retry_cnt = 0;
                    if (param.containsKey("addhost_retry")){
                        retry_cnt = param.getInteger("addhost_retry");
                    }
                    retry_cnt++;
                    param.put("addhost_retry",retry_cnt);
                    getLogger().warn("addhost_retry，vm注册数量有缺失，重试次数："+ retry_cnt);
                    planExecService.loopActivity(clientname,JSON.toJSONString(param),installsdp_retry_waittimes,activityLogId);
                    return resultMsg;
                }else{
                    // 重试完成后依旧，有丢失注册，进入剔除节点判断
                    ResultMsg ckrmsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),ckmsg.getRows(),currentLog);

                    if (!ckrmsg.getResult()){
                        // 剔除节点失败
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        currentLog.setLogs("VM注册Ambariserver节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        getLogger().error("VM注册AmbariServer节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        planExecService.complateActivity(currentLog);
                        return resultMsg;
                    }
                }
            }

            //endregion

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            // region 新增集群机器
            List<InfoClusterVm> vms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);
            List<String> hosts = ClusterServiceUtil.getRunningHosts(vms);

            if (hosts.size() == 0){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("hosts为空。可能主机全部降级，初始待扩容主机列表："+ ClusterServiceUtil.getHostsInfo(vms));
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(false);
                return resultMsg;
            }

            getLogger().info("clusterName:" + confCluster.getClusterName() + "add hosts:" + hosts);
            ambariService.addHosts(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), hosts);
            getLogger().info("clusterName:" + confCluster.getClusterName() + "add hosts 完成。");

            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // endregion

            //region 发送下一环节消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;

    }

    @Override
    public ResultMsg ambariAddHostsToConfigGroup(String message) {
        getLogger().info("begin ambariAddHostsToConfigGroup,{}", message);
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = new InfoClusterOperationPlanActivityLogWithBLOBs();
        try {
            // 获取参数
            JSONObject param = JSON.parseObject(message);
            String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
            String taskId = param.getString(ComposeConstant.Task_ID);

            currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            if (currentLog.getBegtime() == null) {
                currentLog.setBegtime(new Date());
            }

            // 获取扩缩容任务
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            //region task 数据校验
            if (task == null) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("task为空");
                planExecService.complateActivity(currentLog);
                return ResultMsg.FAILURE("task为空");
            }

            // 只针对task和core实例组做ambari配置组操作，其他实例组跳过。
            if (!StrUtil.equalsIgnoreCase(task.getVmRole(), "task")
                    && !StrUtil.equalsIgnoreCase(task.getVmRole(), "core")) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                getLogger().info("skip ambariAddHostsToConfigGroup,clusterId:{},groupName:{},activityLogId:{},vmRole={},message={}",
                        task.getClusterId(),
                        task.getGroupName(),
                        activityLogId,
                        task.getVmRole(),
                        message);
                return ResultMsg.SUCCESS();
            }
            //endregion

            String clusterId = task.getClusterId();
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

            // region 集群vm注册数量检查
            ResultMsg ckmsg = getRegHosts(clusterId,taskId);
            if (!ckmsg.getResult()){
                // 重试
                if (!param.containsKey("addConfigGroup_retry") || param.getInteger("addConfigGroup_retry") < installsdp_retry_cnt){
                    int retry_cnt = 0;
                    if (param.containsKey("addConfigGroup_retry")){
                        retry_cnt = param.getInteger("addConfigGroup_retry");
                    }
                    retry_cnt++;
                    param.put("addConfigGroup_retry",retry_cnt);
                    getLogger().warn("vm注册数量有缺失，重试次数："+retry_cnt);
                    planExecService.loopActivity(clientname,JSON.toJSONString(param),installsdp_retry_waittimes,activityLogId);
                    return ResultMsg.SUCCESS();
                }else{
                    // 重试完成后依旧，有丢失注册，进入剔除节点判断
                    ResultMsg ckrmsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),ckmsg.getRows(),currentLog);

                    if (!ckrmsg.getResult()){
                        // 剔除节点失败
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        currentLog.setLogs("VM注册Ambariserver节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        getLogger().error("VM注册AmbariServer节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        planExecService.complateActivity(currentLog);
                        return ResultMsg.FAILURE("VM注册Ambariserver节点丢失，剔除流程失败");
                    }
                }
            }
            //移除重试
            param.remove("addConfigGroup_retry");

            /*
            处理扩容的ConfigGroup逻辑如下:
            1. 获取本次扩容的VM
            2. 从中找到所有的Sku并去重
            3. 调用ClusterHostGroupManager方法,完成配置组变更
             */
            List<InfoClusterVm> scaleOutAllVms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);
            List<InfoClusterVm> scaleOutVms = scaleOutAllVms.stream().filter(x->{
                return x.getState() == VM_RUNNING;
            }).collect(Collectors.toList());

            //region 判断扩容的vm是否有效
            if (scaleOutVms == null || scaleOutVms.size() == 0){
                String errorMsg = "集群扩容时,获取扩容VM列表时为空,中止扩容. clusterId=" + clusterId
                        + ", taskId=" + taskId
                        + ", groupName=" + task.getGroupName();
                getLogger().error(errorMsg);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(errorMsg);
                planExecService.complateActivity(currentLog);
                return ResultMsg.FAILURE(errorMsg);
            }
            //endregion

            ClusterHostGroupManager hostGroupManager = new ClusterHostGroupManager(clusterId, confCluster.getClusterName());
            List<String> scaleOutSkus =  scaleOutVms.stream()
                    .map(InfoClusterVm::getSkuName)
                    .distinct()
                    .collect(Collectors.toList());
            HostGroupRole hostGroupRole = null;
            if ("core".equalsIgnoreCase(task.getVmRole())) {
                hostGroupRole = HostGroupRole.CORE;
            }else if ("task".equalsIgnoreCase(task.getVmRole())){
                hostGroupRole = HostGroupRole.TASK;
            }
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(clusterId, task.getGroupName());

            CustomConfigGenerator generator = CustomConfigGeneratorFactory.tryCreate(hostGroupRole);
            for (String vmSku : scaleOutSkus) {
                // HDFS
//                getLogger().info("开始处理HDFS配置组. clusterId={}, groupName={}, vmSku={}", clusterId, task.getGroupName(), vmSku);
//                hostGroupManager.saveHostGroup(task.getGroupName(), vmSku, "HDFS", new ArrayList<>());


                ConfHostGroupVmSku confHostGroupVmSku = confHostGroupVmSkuMapper.selectOneByClusterIdAndSku(clusterId, vmSku);
                HostInstance hostInstance = new HostInstance();
                hostInstance.setHostRole(hostGroupRole.name().toLowerCase());
                hostInstance.setvCpu(Integer.parseInt(confHostGroupVmSku.getVcpus()));
                hostInstance.setMemoryGB(Integer.parseInt(confHostGroupVmSku.getMemory()));
                List<BlueprintConfiguration> generate = generator.generate(hostInstance);
                // classificatio -> {[type,properties],}
                Map<String,Map<String, Object>> configs = new HashMap<>();
                for (BlueprintConfiguration blueprintConfiguration : generate) {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("type",blueprintConfiguration.getConfigItemName().toLowerCase());
                    config.put("properties",blueprintConfiguration.getProperties());
                    configs.put(blueprintConfiguration.getConfigItemName(),config);
                }
                //TODO 创建实例组参数未添加
                List<ConfClusterHostGroupAppsConfig> confClusterHostGroupAppsConfigs = confClusterHostGroupAppsConfigMapper.selectByGroupId(confClusterHostGroup.getGroupId());
                HashMap<String, List<ConfClusterHostGroupAppsConfig>> configMaps = new HashMap<>();
                for (ConfClusterHostGroupAppsConfig confClusterHostGroupAppsConfig : confClusterHostGroupAppsConfigs) {
                    List<ConfClusterHostGroupAppsConfig> configList = configMaps.computeIfAbsent(confClusterHostGroupAppsConfig.getAppName().toUpperCase(), k -> new ArrayList<>());
                    configList.add(confClusterHostGroupAppsConfig);
                }
                boolean isConfigYarn = false;
                for (Map.Entry<String, List<ConfClusterHostGroupAppsConfig>> configEntry : configMaps.entrySet()) {
                    String key = configEntry.getKey();
                    List<ConfClusterHostGroupAppsConfig> value = configEntry.getValue();
                    Map<String,Map<String, Object>> currentConfigs = new HashMap<>();
                    if ("YARN".equalsIgnoreCase(key)){
                        currentConfigs = configs;
                        isConfigYarn = true;
                    }
                    for (ConfClusterHostGroupAppsConfig confClusterHostGroupAppsConfig : value) {
                        String classification = confClusterHostGroupAppsConfig.getAppConfigClassification().toLowerCase();
                        Map<String, Object> classificationConfig = currentConfigs.computeIfAbsent(classification, k -> {
                            HashMap<String, Object> cnfMap = new HashMap<>();
                            cnfMap.put("type",classification);
                            cnfMap.put("properties",new HashMap<>());
                            return cnfMap;
                        });
                        Map<String,Object> properties = (Map<String, Object>) classificationConfig.get("properties");
                        properties.put(confClusterHostGroupAppsConfig.getConfigItem(),confClusterHostGroupAppsConfig.getConfigVal());

                    }
                    // YARN
                    getLogger().info("开始处理"+key+"配置组. clusterId={}, groupName={}, vmSku={}", clusterId, task.getGroupName(), vmSku);
                    hostGroupManager.saveHostGroup(task.getGroupName(), vmSku, key, new ArrayList<>(currentConfigs.values()));
                }
                if (!isConfigYarn){
                    hostGroupManager.saveHostGroup(task.getGroupName(), vmSku, "YARN", new ArrayList<>(configs.values()));
                }


            }

            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            getLogger().info("begin planExecService.sendNextActivityMsg activityLogId:{},param:{}", activityLogId, param);
            ResultMsg sendNextActivityResult = planExecService.sendNextActivityMsg(activityLogId, param);
            getLogger().info("end planExecService.sendNextActivityMsg activityLogId:{},result:{}", activityLogId, sendNextActivityResult);
            getLogger().info("end ambariAddHostsToConfigGroup success,message:{}", message);
        } catch (Exception ex) {
            getLogger().error("end ambariAddHostsToConfigGroup error,message:{}", message, ex);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(ex));
            return ResultMsg.FAILURE("将实例添加配置组中异常");
        } finally {
            planExecService.complateActivity(currentLog);
        }
        return ResultMsg.SUCCESS();
    }

    // 备份原来的方法:ambariAddHostsToConfigGroup
    private ResultMsg ambariAddHostsToConfigGroupBak(String message) {
        getLogger().info("begin ambariAddHostsToConfigGroup,{}", message);
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = new InfoClusterOperationPlanActivityLogWithBLOBs();
        try {
            // 获取参数
            JSONObject param = JSON.parseObject(message);
            String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
            String taskId = param.getString(ComposeConstant.Task_ID);

            currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            if (currentLog.getBegtime() == null) {
                currentLog.setBegtime(new Date());
            }

            // 获取扩缩容任务
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            //region task 数据校验
            if (task == null) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("task为空");
                planExecService.complateActivity(currentLog);
                return ResultMsg.FAILURE("task为空");
            }

            // 只针对task和core实例组做ambari配置组操作，其他实例组跳过。
            if (!StrUtil.equalsIgnoreCase(task.getVmRole(), "task")
                    && !StrUtil.equalsIgnoreCase(task.getVmRole(), "core")) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                getLogger().info("skip ambariAddHostsToConfigGroup,clusterId:{},groupName:{},activityLogId:{},vmRole={},message={}",
                        task.getClusterId(),
                        task.getGroupName(),
                        activityLogId,
                        task.getVmRole(),
                        message);
                return ResultMsg.SUCCESS();
            }
            //endregion

            // ///////////////////////////////////////////
            // 获取 [集群的实例组信息]，[集群信息]，[扩容的虚拟机信息]
            // ///////////////////////////////////////////
            String clusterId = task.getClusterId();
            String groupName = task.getGroupName();
            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(clusterId, groupName);
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

            //region 获取扩容实例
            List<InfoClusterVm> scaleOutAllVms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);

            List<InfoClusterVm> scaleOutVms = scaleOutAllVms.stream().filter(x->{
                return x.getState() == VM_RUNNING;
            }).collect(Collectors.toList());

            //endregion

            //region 判断扩容的vm是否有效
            if (scaleOutVms == null || scaleOutVms.size() == 0){
                getLogger().error("新增的Vmlist为空。");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("新增的Vmlist为空");
                planExecService.complateActivity(currentLog);
                return ResultMsg.FAILURE("新增的Vmlist为空");
            }
            //endregion

            // region 集群vm注册数量检查
            ResultMsg ckmsg = getRegHosts(confCluster.getClusterId(),taskId);
            if (!ckmsg.getResult()){
                // 重试
                if (!param.containsKey("addConfigGroup_retry") || param.getInteger("addConfigGroup_retry") < installsdp_retry_cnt){
                    int retry_cnt = 0;
                    if (param.containsKey("addConfigGroup_retry")){
                        retry_cnt = param.getInteger("addConfigGroup_retry");
                    }
                    retry_cnt++;
                    param.put("addConfigGroup_retry",retry_cnt);
                    getLogger().warn("vm注册数量有缺失，重试次数："+retry_cnt);
                    planExecService.loopActivity(clientname,JSON.toJSONString(param),installsdp_retry_waittimes,activityLogId);
                    return ResultMsg.SUCCESS();
                }else{
                    // 重试完成后依旧，有丢失注册，进入剔除节点判断
                    ResultMsg ckrmsg = ivmService.ambariJobRejectNode(confCluster.getClusterId(),ckmsg.getRows(),currentLog);

                    if (!ckrmsg.getResult()){
                        // 剔除节点失败
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        currentLog.setLogs("VM注册Ambariserver节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        getLogger().error("VM注册AmbariServer节点丢失，剔除流程失败，lost vms:"+ckmsg.getRows());
                        planExecService.complateActivity(currentLog);
                        return ResultMsg.FAILURE("VM注册Ambariserver节点丢失，剔除流程失败");
                    }
                }
            }

            //移除重试
            param.remove("addConfigGroup_retry");

            //正在运行的实例(从Azure返回的主机表中查询该实例组的所有运行中的主机信息）
            //endregion

            //region 正在运行的实例
            List<InfoClusterVm> runningVms = ivmService.getGroupVmsByState(confCluster.getClusterId(), task.getGroupName(), VM_RUNNING);
            getLogger().info("获取到runningVms：{}",runningVms);
            List<InfoClusterVm> runningVmsWithOutMaintance = new CopyOnWriteArrayList<>();
            runningVmsWithOutMaintance = runningVms.stream().filter(x->x.getMaintenanceMode()==null || x.getMaintenanceMode().equals(0)).collect(Collectors.toList());

            // 上面的所有代码，都是为了获取可以添加到配置组的主机列表
            // 得到了可以添加到配置组的主机列表后，就可以进行修改配置组操作了

            AmbariInfo ambariInfo = getAmbariInfo(clusterId);
            String ambariClusterName = getSdpClusterNameforAmbari(clusterId);
            CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(api.getApiClient());

            //生成configgroup
            List<ConfClusterHostGroupAppsConfig> appConfigsByGroupId = confClusterHostGroupAppsConfigMapper.selectByGroupId(hostGroup.getGroupId());
            List<ConfigGroup> configGroups = genConfigGroups(groupName, confCluster, ambariClusterName, appConfigsByGroupId, task.getVmRole());

            //region兼容老数据,没有group将创建hostgroup
            if (hostGroup == null) {
                // SDP新增一个配置组
                hostGroup = new ConfClusterHostGroup();
                hostGroup.setGroupName(task.getGroupName());
                hostGroup.setGroupId(UUID.randomUUID().toString());
                hostGroup.setInsCount(task.getAfterScalingCount());
                hostGroup.setClusterId(clusterId);
                hostGroup.setVmRole(task.getVmRole());
                hostGroup.setState(ConfClusterHostGroup.STATE_RUNNING);
                hostGroup.setPurchaseType(NumberUtils.toInteger(scaleOutVms.get(0).getPurchaseType(), ConfClusterVm.PURCHASETYPE_ONDEMOND));
                hostGroup.setCreatedTime(cn.hutool.core.date.DateUtil.date());
                confClusterHostGroupMapper.insertSelective(hostGroup);

                // SDP更新这个实例组的主机的配置组ID，这块可能有问题， 因为一个实例组有多个配置组。
                ConfClusterVm updateConfClusterVm = new ConfClusterVm();
                updateConfClusterVm.setClusterId(clusterId);
                updateConfClusterVm.setGroupName(groupName);
                updateConfClusterVm.setVmRole(task.getVmRole());
                updateConfClusterVm.setGroupId(hostGroup.getGroupId());
                confClusterVmMapper.updateGroupId(updateConfClusterVm);

                // 更新集群的主机的配置组ID，把主机划分到某一个配置组中。
                InfoClusterVm updateInfoClusterVm = new InfoClusterVm();
                updateInfoClusterVm.setClusterId(clusterId);
                updateInfoClusterVm.setGroupName(groupName);
                updateInfoClusterVm.setVmRole(task.getVmRole());
                updateInfoClusterVm.setGroupId(hostGroup.getGroupId());
                confClusterVmMapper.updateGroupId(updateConfClusterVm);
                vmMapper.updateGroupId(updateInfoClusterVm);
                //将运行中的也加到配置组中
                scaleOutVms.addAll(runningVmsWithOutMaintance);
            }

            // 获取当前实例组下面的所有Ambari配置组信息
            //endregion

            List<InfoAmbariConfigGroup> infoAmbariConfigGroups = ambariConfigGroupMapper.selectByGroupIdAndStates(hostGroup.getGroupId(), Arrays.asList(InfoAmbariConfigGroup.STATE_RUNNING));
            for (ConfigGroup configGroup : configGroups) {
                Optional<InfoAmbariConfigGroup> optAmbariConfigGroup = infoAmbariConfigGroups.stream().filter(infoAmbariConfigGroup ->
                        StrUtil.equalsIgnoreCase(groupName, infoAmbariConfigGroup.getSdpGroupName())
                                && StrUtil.equalsIgnoreCase(configGroup.getTag(), infoAmbariConfigGroup.getAmbariTag())
                ).findFirst();
                if (!optAmbariConfigGroup.isPresent()) {
                    optAmbariConfigGroup = infoAmbariConfigGroups.stream().filter(infoAmbariConfigGroup ->
                            StrUtil.contains(infoAmbariConfigGroup.getAmbariGroupName(), groupName)
                                    && StrUtil.equalsIgnoreCase(configGroup.getTag(), infoAmbariConfigGroup.getAmbariTag())).findFirst();
                }
                if (optAmbariConfigGroup.isPresent()) {
                    InfoAmbariConfigGroup exitAmbariConfigGroup = optAmbariConfigGroup.get();
                    configGroup.setId(exitAmbariConfigGroup.getAmbariId());
                    configGroup.setGroupName(exitAmbariConfigGroup.getAmbariGroupName());
                }

                //region 添加host到configGroup
                configGroup.setHosts(new ArrayList<>());
                for (InfoClusterVm infoClusterVm : scaleOutVms) {
                    boolean exist = false;
                    for (HostRole host : configGroup.getHosts()) {
                        if (StringUtils.equals(host.getHostName(), infoClusterVm.getHostName())) {
                            exist = true;
                            break;
                        }
                    }
                    if (exist) {
                        continue;
                    }

                    HostRole hostRole = new HostRole();
                    hostRole.setHostName(infoClusterVm.getHostName());
                    configGroup.getHosts().add(hostRole);
                }
                getLogger().info("scaleOutVms,configGroup:{}",configGroup);

                for (InfoClusterVm runningVm : runningVmsWithOutMaintance) {
                    boolean exist = false;
                    for (HostRole host : configGroup.getHosts()) {
                        if (StringUtils.equals(host.getHostName(), runningVm.getHostName())) {
                            exist = true;
                            break;
                        }
                    }
                    if (exist) {
                        continue;
                    }

                    HostRole hostRole = new HostRole();
                    hostRole.setHostName(runningVm.getHostName());
                    configGroup.getHosts().add(hostRole);
                }
                getLogger().info("add hosts finished configGroup:{}", configGroup);
                //endregion

                //region 调用ambariserver查询configgroup
                QueryConfigGroupsResponse queryConfigGroupsResponse = getQueryConfigGroupsResponse(ambariClusterName, api);
                //endregion

                //region 判断ambari中是否已经存在当前配置组
                if (queryConfigGroupsResponse == null) {
                    queryConfigGroupsResponse = new QueryConfigGroupsResponse();
                }
                if (queryConfigGroupsResponse.getItems() == null) {
                    queryConfigGroupsResponse.setItems(new ArrayList<>());
                }
                getLogger().info("find configGroupWrapper(groupName:{},tag:{}) from queryConfigGroupsResponse's items",
                        groupName,
                        configGroup.getTag());

                Optional<ConfigGroupWrapper> first = queryConfigGroupsResponse.getItems().stream().filter(p ->
                        StrUtil.endWithIgnoreCase(p.getConfigGroup().getGroupName(), groupName)
                                && StrUtil.equalsIgnoreCase(p.getConfigGroup().getTag(), configGroup.getTag())).findFirst();
                //endregion

                if (!first.isPresent()) {

                    //region 当前实例组为新增实例组，使用创建实例组接口
                    CreateConfigGroupResponse createResponse = createAmbariConfigGroup(groupName, ambariClusterName, api, configGroup);
                    if (createResponse != null && createResponse.getSingleId() != null) {
                        configGroup.setId(createResponse.getSingleId().longValue());
                    }
                    //endregion

                } else {
                    //region 已经存在的实例组，使用更新实例组接口
                    getLogger().info("found configGroupWrapper(groupName:{},tag:{}) from queryConfigGroupsResponse's items:{}",
                            groupName,
                            configGroup.getTag(),
                            queryConfigGroupsResponse.getItems());
                    ConfigGroupWrapper configGroupWrapper = first.get();
                    ConfigGroup configGroupFromCustomActionApi = configGroupWrapper.getConfigGroup();
                    if (configGroup.getId() == null) {
                        configGroup.setId(configGroupFromCustomActionApi.getId());
                    }
                    getLogger().info("set clusterId:{},groupName:{} configGroupFromCustomActionApi's desiredConfigs:{} to configGroup's desiredConfigs:{}",
                            clusterId,
                            groupName,
                            configGroupFromCustomActionApi.getDesiredConfigs(),
                            configGroup.getDesiredConfigs());
                    configGroup.setDesiredConfigs(configGroupFromCustomActionApi.getDesiredConfigs());
                    updateAmbariConfigGroup(ambariClusterName, api, configGroup);
                    //endregion
                }
                if (!optAmbariConfigGroup.isPresent()) {
                    //region 保存Ambari配置组信息至数据库表
                    saveInfoAmbariConfigGroup(hostGroup, confCluster, configGroup);
                    //endregion
                }
            }
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            getLogger().info("begin planExecService.sendNextActivityMsg activityLogId:{},param:{}", activityLogId, param);
            ResultMsg sendNextActivityResult = planExecService.sendNextActivityMsg(activityLogId, param);
            getLogger().info("end planExecService.sendNextActivityMsg activityLogId:{},result:{}", activityLogId, sendNextActivityResult);
            getLogger().info("end ambariAddHostsToConfigGroup success,message:{}", message);
        } catch (Exception ex) {
            getLogger().error("end ambariAddHostsToConfigGroup error,message:{}", message, ex);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(ex));
            return ResultMsg.FAILURE("将实例添加配置组中异常");
        } finally {
            planExecService.complateActivity(currentLog);
        }
        return ResultMsg.SUCCESS();
    }

    /**
     * 更新ambariserver configGroup
     *
     * @param ambariClusterName
     * @param api
     * @param configGroup
     */
    private UpdateConfigGroupResponse updateAmbariConfigGroup(String ambariClusterName, CustomActionApi api, ConfigGroup configGroup) {
        getLogger().info("begin custom action api updateConfigGroup clusterName:{},configGroup:{}",
                ambariClusterName,
                configGroup);
        UpdateConfigGroupResponse updateConfigGroupResponse =null;
        Integer i =0;
        while(true) {
            try {
                updateConfigGroupResponse = api.updateConfigGroup(ambariClusterName, configGroup);
                getLogger().info("end custom action api updateConfigGroup clusterName:{},configGroup:{},response:{}",
                        ambariClusterName,
                        configGroup,
                        updateConfigGroupResponse);
                return updateConfigGroupResponse;
            }catch (Exception e){
                i++;
                if (i > sdpConfigGroupsQueryRetryTimes){
                    throw e;
                }
                getLogger().error("更新ConfigGroups异常",e);
                getLogger().error("更新ConfigGroups异常，重试中，第"+i+"次");
            }
            ThreadUtil.sleep(1000* sdpConfigGroupsQueryRetryWaittimes);
        }
    }

    /**
     * 创建ambari configGroup
     *
     * @param groupName
     * @param ambariClusterName
     * @param api
     * @param configGroup
     */
    private CreateConfigGroupResponse createAmbariConfigGroup(String groupName, String ambariClusterName, CustomActionApi api, ConfigGroup configGroup) {
        getLogger().info("not found configGroupWrapper(groupName:{},tag:{}) from queryConfigGroupsResponse's items",
                groupName,
                configGroup.getTag());
        getLogger().info("begin custom action api createConfigGroup clusterName:{},configGroup:{}",
                ambariClusterName,
                configGroup);

        CreateConfigGroupResponse createResponse = null;
        Integer i =0;
        while (true) {
            try {
                createResponse = api.createConfigGroup(ambariClusterName, Collections.singletonList(configGroup));
                getLogger().info("end custom action api createConfigGroup clusterName:{},configGroup:{},response:{}",
                        ambariClusterName,
                        configGroup,
                        createResponse);
                return createResponse;
            }catch (Exception e){
                i++;
                if (i > sdpConfigGroupsQueryRetryTimes){
                    throw e;
                }
                getLogger().error("创建ConfigGroups异常",e);
                getLogger().error("创建ConfigGroups异常，重试中，第"+i+"次");
            }
            ThreadUtil.sleep(1000* sdpConfigGroupsQueryRetryWaittimes);
        }
    }

    /**
     * 保存InfoAmbariConfigGroup
     *
     * @param hostGroup
     * @param confCluster
     * @param configGroup
     */
    private void saveInfoAmbariConfigGroup(ConfClusterHostGroup hostGroup, ConfCluster confCluster, ConfigGroup configGroup) {
        InfoAmbariConfigGroup ambariConfigGroup = new InfoAmbariConfigGroup();
        ambariConfigGroup.setClusterId(confCluster.getClusterId());
        ambariConfigGroup.setGroupId(hostGroup.getGroupId());
        ambariConfigGroup.setAmbariId(configGroup.getId());
        ambariConfigGroup.setAmbariServiceName(configGroup.getServiceName());
        ambariConfigGroup.setAmbariGroupName(configGroup.getGroupName());
        ambariConfigGroup.setSdpGroupName(hostGroup.getGroupName());
        ambariConfigGroup.setAmbariTag(configGroup.getTag());
        ambariConfigGroup.setAmbariClusterName(configGroup.getClusterName());
        ambariConfigGroup.setAmbariDescription(configGroup.getDescription());
        ambariConfigGroup.setState(InfoAmbariConfigGroup.STATE_RUNNING);
        ambariConfigGroup.setCreatedTime(new Date());
        ambariConfigGroup.setConfId(UUID.randomUUID().toString());
        getLogger().info("ambariConfigGroupMapper insert group:{}", ambariConfigGroup);
        ambariConfigGroupMapper.insert(ambariConfigGroup);
    }

    /**
     *  查询集群所有的configGroup
     *
     * @param ambariClusterName
     * @param api
     * @return
     */
    private QueryConfigGroupsResponse getQueryConfigGroupsResponse(String ambariClusterName, CustomActionApi api) {
        getLogger().info("custom action api queryConfigGroups clusterName:{},queryField:{},queryValue:{}",
                ambariClusterName,
                ConfigGroupField.GROUP_NAME,
                "*");

        QueryConfigGroupsResponse queryConfigGroupsResponse = null;
        Integer i =0;
        while (true) {
            try {
                queryConfigGroupsResponse = api.queryConfigGroups(ambariClusterName, ConfigGroupField.GROUP_NAME, "*");
                getLogger().debug("custom action api queryConfigGroups clusterName:{},queryField:{},queryValue:{},response:{}",
                        ambariClusterName,
                        ConfigGroupField.GROUP_NAME,
                        "*",
                        queryConfigGroupsResponse);
                return queryConfigGroupsResponse;
            }catch (Exception e){
                i++;
                if (i > sdpConfigGroupsQueryRetryTimes){
                    throw e;
                }
                getLogger().error("查询ConfigGroups异常",e);
                getLogger().error("查询ConfigGroups异常，重试中，第"+i+"次");
            }
            ThreadUtil.sleep(1000* sdpConfigGroupsQueryRetryWaittimes);
        }
    }

    /**
     * 配置新增的实例上需要的大数据组件
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg configAddSDP(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region 获取实例组扩容前正在运行的实例
            List<InfoClusterVm> gvms = ivmService.getGroupVmsByState(confCluster.getClusterId(), task.getGroupName(), VM_RUNNING);

            Optional<InfoClusterVm> vm = gvms.stream().filter(x -> {
                return (x.getScaleoutTaskId() == null || x.getScaleoutTaskId().equals(""))&& (x.getMaintenanceMode() == null|| x.getMaintenanceMode().equals(InfoClusterVm.MaintenanceModeOFF));
            }).findFirst();

            List<String> components = new ArrayList<>();
            String vmhostName =null;

            if (vm.isPresent()){
                vmhostName = vm.get().getHostName();
            }
            components = getComponentsByHostForScaleOut(ambariInfo,confCluster,task,vmhostName);
            //endregion
            getLogger().info("需要安装的组件：" + components);
            if (components.size() == 0) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("未找到实例上安装的组件。");
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            //endregion

            //region 配置新增主机需要的组件
            List<InfoClusterVm> vms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);
            List<String> hosts = ClusterServiceUtil.getRunningHosts(vms);

            if (hosts.size() == 0){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("hosts为空。可能主机全部降级，初始待扩容主机列表："+ ClusterServiceUtil.getHostsInfo(vms));
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(false);
                return resultMsg;
            }

            ambariService.configHostComponent(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), hosts, components);
            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            //endregion

            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            // endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    private List<String> getComponents(ConfScalingTask task, ConfCluster confCluster, List<String> components) {
        ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(confCluster.getClusterId(), task.getGroupName());
        if (hostGroup != null) {
            List<InfoClusterComponentLayout> clusterComponentLayouts = clusterComponentLayoutMapper.getComponentsByClusterIdAndHostGroup(confCluster.getClusterId(), hostGroup.getGroupId());
            if (CollUtil.isNotEmpty(clusterComponentLayouts)) {
                components = clusterComponentLayouts.stream().map(p -> p.getComponentCode()).collect(Collectors.toList());
            }
        }
        if (CollUtil.isEmpty(components)) {
            List<ConfClusterApp> clusterApps = clusterAppMapper.getClusterAppsByClusterId(confCluster.getClusterId());
            List<String> serviceCodes = clusterApps.stream().map(p -> p.getAppName()).collect(Collectors.toList());
            List<AmbariComponentLayout> ambariComponentLayouts = ambariComponentLayoutMapper.queryByHostGroupAndServiceCode(
                    confCluster.getClusterReleaseVer(),
                    task.getVmRole().toUpperCase(),
                    serviceCodes,
                    confCluster.getIsHa());
            if (CollUtil.isNotEmpty(ambariComponentLayouts)) {
                components = ambariComponentLayouts.stream().map(p -> p.getComponentCode()).collect(Collectors.toList());
            }
        }
        return components;
    }

    /**
     * 新增服务器安装SDP大数据组件
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg installAddSDP(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region 新增实例
            List<InfoClusterVm> vms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);
            List<String> hosts = ClusterServiceUtil.getRunningHosts(vms);

            if (hosts.size() == 0){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("hosts为空。可能主机全部降级，初始待扩容主机列表："+ ClusterServiceUtil.getHostsInfo(vms));
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(false);
                return resultMsg;
            }

            InProgressResult response = installHostComponent(confCluster, ambariInfo, hosts);
            if (response != null && response.isSuccess()) {
                resultMsg.setResult(true);
                param.put(JobNameConstant.Install_Cluster_App, response.getRequestId() + "");
                //开启重试
                param.put(JobNameConstant.Switch_Ambari_Retry,1);
                //开启降级
                param.put(JobNameConstant.Switch_Ambari_Reduce,1);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.getMessage());
            }
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 发送扩容实例安装组件请求
     *
     * @param confCluster
     * @param ambariInfo
     * @param hosts
     * @return
     */
    private InProgressResult installHostComponent(ConfCluster confCluster, AmbariInfo ambariInfo, List<String> hosts) {

        InProgressResult response = null;
        Integer i = 0;
        while (true) {
            try {
                response = ambariService.installHostComponent(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), hosts);
                getLogger().info("installaddsdp,response:" + JSON.toJSONString(response));
                if (response.isSuccess()){
                    return response;
                }
            }catch (Exception e){
                getLogger().error("扩容安装HostComponent异常",e);
                getLogger().error("扩容安装HostComponent异常，重试中，第"+i+"次");
            }
            finally {
                i++;
                if (i > sdpInstallHostComponentRetryTimes){
                    return response;
                }
                ThreadUtil.sleep(1000 * sdpInstallHostComponentRetryWaitTimes);
            }
        }
    }

    /**
     * core 节点数据平衡指令发送
     * 非core节点，不执行。
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg dataBalanceForCore(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        //region 判断是否是core节点，非core节点跳过本步骤
        if (!task.getVmRole().equalsIgnoreCase("core")) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs("非core节点跳过，进行下一步。");
            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(activityLogId, param);
            return resultMsg;
        }

        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region core节点数据平衡
            ambariService.rebalanceDataForCore(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()),confCluster.getClusterId());
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs("");
            //endregion
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        //region 发送下一环节消息
        planExecService.sendNextActivityMsg(activityLogId, param);
        return resultMsg;
    }

    /**
     * 启动新增实例的组件
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg startAddHostComponents(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region 新增实例
            List<InfoClusterVm> vms = ivmService.getScaleOutVms(confCluster.getClusterId(), taskId);
            List<String> hosts = ClusterServiceUtil.getRunningHosts(vms);

            if (hosts.size() == 0){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("hosts为空。可能主机全部降级，初始待扩容主机列表："+ ClusterServiceUtil.getHostsInfo(vms));
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(false);
                return resultMsg;
            }
            //endregion

            //region 获取实例组当前扩容的机器上已经安装的组件

            String vmhostName = hosts.get(0);
            List<String> components =getComponentsByHostForScaleOut(ambariInfo,confCluster,task,vmhostName);

            getLogger().info("实例上需要启动的组件：" + components.toString());
            if (components.size() == 0) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("未找到实例上安装的组件。");
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            //endregion

            InProgressResult response = startHostComponent(confCluster, ambariInfo, hosts, components);
            if (response != null && response.isSuccess()) {
                resultMsg.setResult(true);
                if (response.getRequestId() == null ){
                    // 任务已经执行过了，ambari不会返回requsteId
                    param.put(JobNameConstant.Install_Cluster_App, "000000");
                }else {
                    param.put(JobNameConstant.Install_Cluster_App, response.getRequestId() + "");
                    param.put(JobNameConstant.Switch_Ambari_Reduce,1);
                    param.put(JobNameConstant.Switch_Ambari_Retry,1);
                }
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(response.getMessage());
            }

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }


    /**
     * 根据hostName获取已安装的大数据组件
     * @param ambariInfo
     * @param confCluster
     * @param task
     * @param hostName
     * @return
     */
    private List<String> getComponentsByHostForScaleOut(AmbariInfo ambariInfo,
                                             ConfCluster confCluster,
                                             ConfScalingTask task,
                                             String hostName){
        List<String> components = new ArrayList<>();

        if (StringUtils.isEmpty(hostName)){
            getLogger().info("未找到之前运行VM,从ambari_template或cluster_template中获取component信息");
            components = getComponents(task, confCluster, components);
            return components;
        }else{
            getLogger().info("已经安装组件的VM：" +hostName);
            try {
                components = ambariService.getComponentsByHost(ambariInfo,
                        getSdpClusterNameforAmbari(confCluster.getClusterId()), hostName);
                return components;
            }catch (Exception e){
                getLogger().error("根据hostname查询安装的组件列表异常，",e);
                getLogger().info("ambari_template，获取component信息");
            }
            components = getComponents(task, confCluster, components);
            return components;
        }
    }

    /**
     * 发送新增机器启动大数据组件请求
     *
     * @param confCluster
     * @param ambariInfo
     * @param hosts
     * @param components
     * @return
     */
    private InProgressResult startHostComponent(ConfCluster confCluster, AmbariInfo ambariInfo, List<String> hosts, List<String> components) {
        InProgressResult response = null;
        Integer i = 0;
        while (true) {
            try {
                response = ambariService.startHostComponent(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), hosts, components);
                getLogger().info("installaddsdp,response:" + JSON.toJSONString(response));
                if (response.isSuccess()){
                    return response;
                }
            }catch (Exception e){
                getLogger().error("扩容启动HostComponent异常",e);
                getLogger().error("扩容启动HostComponent异常，重试中，第"+i+"次");
            }
            i++;
            if (i > sdpScaleOutStartHostComponentRetryTimes){
                return response;
            }
            ThreadUtil.sleep(1000 * sdpScaleOutStartHostComponentRetryWaitTimes);
        }
    }

    /**
     * core dataNode节点decommionsion
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg dataNodeDecommionsion(String message) {
        return decommissionCommont(message, "DATANODE");
    }


    @Nullable
    private ResultMsg decommissionCommont(String message, String commonpentName) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        // endregion

        // region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        // endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            // region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            // endregion

            // region 获取需要缩容的机器

            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            if (vms == null || vms.size()==0){
                getLogger().warn("InfoClusterVM未查询到数据，取ClearLog中的数据。");
                vms = clearLogMapper.selectInfoClusterVmsByPlanId(currentLog.getPlanId());
            }
            getLogger().info("需要缩容的VM：" + vms.toString());
            List<String> hosts =null;

            if(commonpentName.equalsIgnoreCase("DATANODE") && task.getVmRole().equalsIgnoreCase("core")){
                hosts = getHostsWithRunningCommonpent(commonpentName, confCluster, ambariInfo, vms);
            }
            if(commonpentName.equalsIgnoreCase("NODEMANAGER")){
                hosts = getHostsWithRunningCommonpent(commonpentName, confCluster, ambariInfo, vms);
            }

            getLogger().info("满足缩容条件的VM：" + hosts);
            if (hosts == null || hosts.size() == 0) {
                //没有datanode节点
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(commonpentName + "不存在，跳过");

                param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            //endregion

            //region decommission

            InProgressResult response = decommissionComponentWithRetry(commonpentName, confCluster, ambariInfo, hosts);
            if (response.isSuccess()) {
                resultMsg.setResult(true);
                param.put(JobNameConstant.Decommonsion_Comonpent, response.getRequestId() + "");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                // 非datanode节点，decommonsion失败忽略
                if (!commonpentName.equalsIgnoreCase("DATANODE")) {
                    param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs(response.getMessage());
                    // region 发送下一个环节的消息
                    planExecService.sendNextActivityMsg(activityLogId, param);
                } else {
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    currentLog.setLogs(response.getMessage());
                }
            }

            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            // 非datanode节点，decommonsion异常忽略
            if (!commonpentName.equalsIgnoreCase("DATANODE")) {
                param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
            } else {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            }
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 获取正在运行组件的Hosts
     * 支持 DataNode NodeManager
     * @param commonpentName
     * @param confCluster
     * @param ambariInfo
     * @param vms
     * @return
     */
    private List<String> getHostsWithRunningCommonpent(String commonpentName, ConfCluster confCluster, AmbariInfo ambariInfo, List<InfoClusterVm> vms) {
        List<String> hostNames = new CopyOnWriteArrayList<>();

        vms.stream().forEach(x->{
            hostNames.add(x.getHostName());
        });

        if (commonpentName.equalsIgnoreCase("DATANODE")){
            return ambariService.getHostsWithInServiceDataNode(confCluster.getClusterId(),hostNames);
        }

        if (commonpentName.equalsIgnoreCase("NODEMANAGER")){
            return ambariService.getHostsWithRunningNodeManager(confCluster.getClusterId(),hostNames);
        }

        return hostNames;
    }

    /**
     * 发送decommission请求到Ambari with retry
     * 支持 DATANODE NODEMANAGER
     * @param commonpentName
     * @param confCluster
     * @param ambariInfo
     * @param hosts
     * @return
     */
    private InProgressResult decommissionComponentWithRetry(String commonpentName, ConfCluster confCluster, AmbariInfo ambariInfo, List<String> hosts) {
        Integer i = 0;
        while(true) {
            InProgressResult response=null;
            try {
                  response = ambariService.decommissionComponent(
                        ambariInfo,
                        getSdpClusterNameforAmbari(confCluster.getClusterId()),
                        hosts,
                        commonpentName);
                if (response.isSuccess()) {
                    return  response;
                }
            } catch (Exception e) {
                getLogger().error("发送decommissionComponent，异常",e);
            }
            i++;
            getLogger().warn("发送decommissionComponent,重试："+i);
            if (i > sdpScaleInDecommissionRetryCount){
                return response;
            }
            ThreadUtil.sleep(1000 * sdpScaleInDecommissionRetryDuration);
        }
    }

    /**
     * 发送decommission请求到Ambari with retry
     * 支持 DATANODE NODEMANAGER
     * @param commonpentName
     * @param confCluster
     * @param ambariInfo
     * @param hosts
     * @return
     */
    private ResultMsg decommissionComponentOneByOne(String commonpentName, ConfCluster confCluster, AmbariInfo ambariInfo, List<String> hosts) {
        ResultMsg resultMsg = new ResultMsg();
        hosts.stream().forEach(host->{
            try {
                InProgressResult response = ambariService.decommissionComponent(
                        ambariInfo,
                        getSdpClusterNameforAmbari(confCluster.getClusterId()),
                        host,
                        commonpentName);
                ThreadUtil.sleep(500);
            } catch (Exception e) {
                getLogger().error("发送decommissionComponent，异常", e);
                resultMsg.setResult(false);
            }
        });
        return resultMsg;
    }



    /**
     *  获取组件状态是Started的HostName
     * @param ambariInfo
     * @param clusterId
     * @param componentName
     * @param inHosts
     * @return
     */
    public List<String> getComponentStartedHosts(AmbariInfo ambariInfo, String clusterId, String componentName, List<String> inHosts) {
        getLogger().info("decommission checkHost,检查前主机：{}", inHosts);
        Integer i=0;
        while (true) {
            try {
                QueryComponentInHostsResponse response = ambariService.getComponentInHosts(clusterId, componentName);
                List<String> finalHosts = response.getHostsByComponentStarted(componentName, inHosts);
                getLogger().info("decommission checkHost,检查后主机：{}", finalHosts);
                if (finalHosts !=null && finalHosts.size() >0){
                    return finalHosts;
                }
            } catch (Exception ex) {
                getLogger().error("从Ambari获取一个组件所在的所有主机列表出错：" + ex.getMessage(), ex);
            }
            i++;
            getLogger().warn("从Ambari获取一个组件所在的所有主机列表,重试："+i);
            if (i > sdpScaleInDecommissionRetryCount){
                return inHosts;
            }
            ThreadUtil.sleep(1000 * sdpScaleInDecommissionRetryDuration);
        }
    }

    public List<String> getExistHosts(AmbariInfo ambariInfo, String clusterName, List<String> inHosts) {
        try {
            getLogger().info("stopComponent checkHost,检查前主机：{}", inHosts);
            List<String> hosts = ambariService.queryAllHosts(ambariInfo, clusterName);
            Set inHostsSet = new HashSet<>(inHosts);

            List<String> finalHosts = new ArrayList<>();
            for (String host : hosts) {
                if (inHostsSet.contains(host)) {
                    finalHosts.add(host);
                }
            }
            getLogger().info("stopComponent checkHost,检查后主机：{}", finalHosts);
            return finalHosts;

        } catch (Exception ex) {
            getLogger().error("从Ambari获取所有主机列表出错：" + ex.getMessage(), ex);
            return inHosts;
        }
    }

    /**
     * 根据集群组件名称获取机器列表中安装的hosts
     *
     * @param ambariInfo    ambari信息
     * @param clusterName   集群名称
     * @param vms           vm列表
     * @param componentName 集群组件名称
     * @return
     */
    private List<String> getHostsByComponentName(AmbariInfo ambariInfo, String clusterName, List<InfoClusterVm> vms, String componentName) {
        List<String> hosts = new CopyOnWriteArrayList<>();
        vms.stream().forEach(vm -> {
            List<String> components = ambariService.getComponentsByHost(ambariInfo, clusterName, vm.getHostName());
            if (components.contains(componentName.toUpperCase())) {
                hosts.add(vm.getHostName());
            }
        });
        return hosts;
    }


    /**
     * task nodeManger 节点的Decommionsion
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg nodeManagerDecommionsion(String message) {
        return decommissionCommont(message, "NODEMANAGER");
    }

    /**
     * hbase RegionServerDecommionsion 节点的Decommionsion
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg hbaseRegionServerDecommionsion(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        // endregion

        // region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        // endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            // 非Hbase场景
            if (!confCluster.getScene().equalsIgnoreCase("hbase")){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            // region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            // endregion

            // region 获取需要缩容的机器

            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            getLogger().info("需要缩容的集群：" + vms.toString());


            if (vms == null || vms.size() == 0) {
                //没有datanode节点
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            List<String> hosts = vms.stream().map(InfoClusterVm::getHostName).collect(Collectors.toList());
            hosts = getComponentStartedHosts(ambariInfo, confCluster.getClusterId(), "HBASE_REGIONSERVER", hosts);
            //endregion

            //region decommission
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            InProgressResponse response = actionApi.decommissionRegionServer(getSdpClusterNameforAmbari(confCluster.getClusterId()),
                    hosts);

            if (response.getRequestId() != null) {
                resultMsg.setResult(true);
                param.put(JobNameConstant.Decommonsion_Comonpent, response.getRequestId() + "");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("response 异常");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
            }
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            param.put(JobNameConstant.Decommonsion_Comonpent, "000000");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 查询DecommionsionStatus
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryDecommionsionStatus(String message) {
        ResultMsg resultMsg = new ResultMsg();
        try {

            // 0. 解析参数
            JSONObject param = JSON.parseObject(message);
            String rpjobId = param.getString(JobNameConstant.Decommonsion_Comonpent);
            String taskId = param.getString(ComposeConstant.Task_ID);
            String activityLogId = param.getString("activityLogId");

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            if (rpjobId.equals("000000")) {
                // 跳过
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("没有目标组件。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            //region 查询task信息
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            getLogger().info("task:" + task.toString());
            //endregion

            Long duration = (System.currentTimeMillis() - currentLog.getBegtime().getTime()) / 1000;
            Long waitingtime = 60L;

            //region 优雅decommission
            if (task.getIsGracefulScalein().equals(1) && task.getScaleinWaitingtime() != null) {
                waitingtime = Long.parseLong(task.getScaleinWaitingtime() + "");
                if (duration.compareTo(waitingtime) > 0) {
                    getLogger().info("waitingtime:" + waitingtime + ",durationtime:" + duration + "，优雅decommission结束进入下一个步骤。");
                    resultMsg.setResult(true);
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs("waitingtime:" + waitingtime + ",durationtime:" + duration + "，优雅decommission结束进入下一个步骤。");
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                    return resultMsg;
                }
            }
            //endregion 优雅decommission

            getLogger().info("获取ActivityLog:" + currentLog.toString());
            QueryProgressCmd qcmd = new QueryProgressCmd();
            JSONObject paramjson = JSON.parseObject(currentLog.getParaminfo());
            qcmd.setClusterName(getSdpClusterNameforAmbari(confCluster.getClusterId()));
            qcmd.setAmbariInfo(getAmbariInfo(confCluster.getClusterId()));
            qcmd.setRequestId(Long.valueOf(rpjobId));
            getLogger().info("查询decommission进度请求Ambari参数：" + qcmd);
            QueryProgressResult msg = ambariService.queryCreateClusterProgress(qcmd);
            getLogger().info("请求Ambari结束。");

            if (msg != null) {
                msg.setTaskList(null);
                getLogger().info("查询Ambari接口返回:" + JSONObject.toJSONString(msg));
            }
            if (msg.isSuccess()) {
                getLogger().info(taskId + ":Decommission任务完成。");
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(JSON.toJSONString(msg));
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);

                return resultMsg;
            }

            if (msg.isFail()) {
                if (task.getVmRole().equalsIgnoreCase("task")) {
                    resultMsg.setResult(true);
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs(JSON.toJSONString(msg));
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                    return resultMsg;
                } else {
                    return ambariFailed(currentLog, msg);
                }
            }

            if (msg.isPending()) {
                Long timeout = Long.parseLong(pendingHostTimeOut);
                if (duration.compareTo(timeout) > 0) {
                    if (task.getVmRole().equalsIgnoreCase("task")) {
                        resultMsg.setResult(true);
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        currentLog.setLogs(JSON.toJSONString(msg));
                        planExecService.complateActivity(currentLog);
                        planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                        return resultMsg;
                    } else {
                        return ambariFailed(currentLog, msg);
                    }
                }
                getLogger().info("PendingHost,缓冲时间内，继续loopquery。");
            }

            resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
        } catch (Exception e) {
            getLogger().error("queryDecommionsionStatusProcess:", e);
        }
        return resultMsg;
    }


    /**
     * 查询datanodeDecommission
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryDataNodeDecommission(String message) {
        ResultMsg resultMsg = new ResultMsg();
        try {

            // 0. 解析参数
            JSONObject param = JSON.parseObject(message);
            String rpjobId = param.getString(JobNameConstant.Decommonsion_Comonpent);
            String taskId = param.getString(ComposeConstant.Task_ID);
            String activityLogId = param.getString("activityLogId");

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                    = planExecService.getInfoActivityLogByLogId(activityLogId);
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            if (rpjobId.equals("000000")) {
                // 跳过
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("没有目标组件。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            //region 查询task信息
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            getLogger().info("task:" + task.toString());
            //endregion

            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);

            List<String> vmHostnames = new ArrayList<>();

            vms.stream().forEach(x -> {
                vmHostnames.add(x.getHostName());
            });

            ResultMsg qrymsg = ambariService.queryDataNodeDecommionsionProcess(confCluster.getClusterId(), vmHostnames);

            if (qrymsg.getResult()) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(true);
            } else {
                resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
            }
        } catch (Exception e) {
            getLogger().error("queryDecommionsionStatusProcess:", e);
        }
        return resultMsg;
    }

    /**
     * 查询nodemanager Decommission
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryNodeManagerDecommission(String message) {
        ResultMsg resultMsg = new ResultMsg();

        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String rpjobId = param.getString(JobNameConstant.Decommonsion_Comonpent);
        String taskId = param.getString(ComposeConstant.Task_ID);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);


        try {
            if (rpjobId.equals("000000")) {
                // 跳过
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("没有目标组件。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            //region 查询task信息
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            getLogger().info("task:" + task.toString());
            //endregion

            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);

            List<String> vmHostnames = new ArrayList<>();

            vms.stream().forEach(x -> {
                vmHostnames.add(x.getHostName());
            });

            ResultMsg qrymsg = ambariService.queryNodeManagerDecommionsionProcess(confCluster.getClusterId(), vmHostnames);

            if (qrymsg.getResult()) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(true);
            } else {
                Long beg=currentLog.getBegtime().getTime();
                if ((System.currentTimeMillis()-beg)>300*1000){
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                    planExecService.complateActivity(currentLog);
                    resultMsg.setResult(true);
                    return resultMsg;
                }
                resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
            }
        } catch (Exception e) {
            getLogger().error("queryDecommionsionStatusProcess:", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
            planExecService.complateActivity(currentLog);
        }
        return resultMsg;
    }


    /**
     * 查询HBase RegionServerDecommission 进度
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryHbaseRegionServerDecommission(String message) {
        ResultMsg resultMsg = new ResultMsg();

        JSONObject param = JSON.parseObject(message);
        String rpjobId = param.getString(JobNameConstant.Decommonsion_Comonpent);
        String taskId = param.getString(ComposeConstant.Task_ID);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = null;

        try {

            // 0. 解析参数
            currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            if (rpjobId.equals("000000")) {
                // 跳过
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("没有目标组件。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            RegionServerDecommissionProgress progress = actionApi.queryRegionServerDecommissionProgress(
                    getSdpClusterNameforAmbari(confCluster.getClusterId()),
                    Integer.parseInt(rpjobId));

            if (progress.isProcessCompleted()) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
            } else {
                Long beg=currentLog.getBegtime().getTime();
                if ((System.currentTimeMillis()-beg)>300*1000){
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                    planExecService.complateActivity(currentLog);
                    resultMsg.setResult(true);
                    return resultMsg;
                }
                planExecService.loopActivity(clientname, message, 10L, activityLogId);
            }
        } catch (Exception e) {
            getLogger().error("queryHbaseRegionServerDecommionsionStatusProcess:", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
            planExecService.complateActivity(currentLog);
        }
        return resultMsg;
    }

    /**
     * 关闭指定机器的组件
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg closeComponentByHost(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region 获取需要缩容的机器

            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            if (vms == null || vms.size() == 0) {
                //没有datanode节点
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("不存在VM，跳过");

                param.put(JobNameConstant.Close_Commonpent, "000000");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            //endregion

            //region 缩容节点切换到工程模式
            infoClusterVmMapper.updateMaintenanceModeByScaleinTaskId(taskId, confCluster.getClusterId(),InfoClusterVm.MaintenanceModeON);
            //endregion

            List<String> hosts = vms.stream().map(InfoClusterVm::getHostName).collect(Collectors.toList());
            hosts = getExistHosts(ambariInfo, confCluster.getAmbariClusterName(), hosts);

            //region stopHostAllComponents
            InProgressResult response = ambariService.stopHostAllComponents(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), hosts);
            if (response.isSuccess()) {
                resultMsg.setResult(true);
                param.put(JobNameConstant.Close_Commonpent, response.getRequestId() + "");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                //节点忽略失败
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                param.put(JobNameConstant.Close_Commonpent, "000000");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                currentLog.setLogs(response.getMessage());
            }
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            //task节点忽略异常
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            param.put(JobNameConstant.Close_Commonpent, "000000");
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 查询关闭组件的进度
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryCloseComponentStatus(String message) {
        ResultMsg resultMsg = new ResultMsg();

        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String rpjobId = param.getString(JobNameConstant.Close_Commonpent);
        String taskId = param.getString(ComposeConstant.Task_ID);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        try {
            if (rpjobId.equals("000000")) {
                // 跳过
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("没有目标组件。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }

            //region 查询task信息
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            getLogger().info("task:" + task.toString());
            //endregion

            getLogger().info("获取ActivityLog:" + currentLog.toString());
            QueryProgressCmd qcmd = new QueryProgressCmd();
            JSONObject paramjson = JSON.parseObject(currentLog.getParaminfo());
            qcmd.setClusterName(getSdpClusterNameforAmbari(confCluster.getClusterId()));
            qcmd.setAmbariInfo(getAmbariInfo(confCluster.getClusterId()));
            qcmd.setRequestId(Long.valueOf(rpjobId));
            getLogger().info("查询closeCommonpent进度请求Ambari参数：" + qcmd);
            QueryProgressResult msg = ambariService.queryCreateClusterProgress(qcmd);
            getLogger().info("请求Ambari结束。");

            if (msg != null) {
                msg.setTaskList(null);
                getLogger().info("查询Ambari接口返回:" + JSONObject.toJSONString(msg));
            }

            if (msg.isSuccess()) {
                getLogger().info(taskId + ":closeCommonpent任务完成。");
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(JSON.toJSONString(msg));
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                return resultMsg;
            }

            if (msg.isFail()) {
                    // task 节点忽略错误
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs(JSON.toJSONString(msg));
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                    resultMsg.setResult(true);
                    return resultMsg;
            }

            if (msg.isPending()) {
                Long timeout = Long.parseLong(pendingHostTimeOut);
                Long duration = (System.currentTimeMillis() - currentLog.getBegtime().getTime()) / 1000;
                if (duration.compareTo(timeout) > 0) {
                        // task 节点忽略pendingtimeout
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        currentLog.setLogs(JSON.toJSONString(msg));
                        planExecService.complateActivity(currentLog);
                        planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                        resultMsg.setResult(true);
                        return resultMsg;
                }
                getLogger().info("PendingHost,缓冲时间内，继续loopquery。");
            }
            Long beg=currentLog.getBegtime().getTime();
            if ((System.currentTimeMillis()-beg)>300*1000){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(JSON.toJSONString(msg));
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                resultMsg.setResult(true);
                return resultMsg;
            }
            resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
        } catch (Exception e) {
            getLogger().error("querycloseCommonpentProcess:", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
            planExecService.complateActivity(currentLog);
            planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
        }
        return resultMsg;
    }

    @Override
    public ResultMsg shutdownAmbariAgent(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String taskId = param.getString(ComposeConstant.Task_ID);
        String activityLogId = param.getString("activityLogId");
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            if (vms == null || vms.size() == 0) {
                //没有datanode节点
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("不存在VM，跳过");

                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            List<String> hosts = vms.stream().map(InfoClusterVm::getInternalip).collect(Collectors.toList());
            getLogger().info("开始执行关闭ambari-agent,clusterId:{},hosts:{}",confCluster.getClusterId(),hosts);
            playBookService.shutdownAmbariAgent(confCluster.getClusterId(), hosts);
            getLogger().info("结束执行关闭ambari-agent,clusterId:{},hosts:{}",confCluster.getClusterId(),hosts);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            planExecService.sendNextActivityMsg(activityLogId, param);

        }catch (Throwable t){
            getLogger().error("--Exception---", t);
            resultMsg.setResult(false);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            currentLog.setLogs(t.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * ambari中删除hosts
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg deleteAmbariHosts(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region 获取需要缩容的机器
            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            if (vms == null || vms.size() == 0) {
                //没有datanode节点
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("不存在VM，跳过");

                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            //endregion

            List<String> hosts = new CopyOnWriteArrayList<>();
            vms.stream().forEach(vm -> {
                hosts.add(vm.getHostName());
            });

            //region deleteHosts
            DeleteHostResult response = ambariService.deleteHosts(ambariInfo,
                    getSdpClusterNameforAmbari(confCluster.getClusterId()),
                    hosts);
            if (response.isAllDeleted()) {
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
            } else {
                List<String> deleteFailedhosts = response.getDeleteFailHosts();
                if (deleteFailedhosts != null && deleteFailedhosts.size() > 0) {
                    param.put("deleteFailedHosts", deleteFailedhosts);
                }

                //task节点忽略失败
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);

                currentLog.setLogs(JSON.toJSONString(response));
            }
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 清理ambari中的残留del失败的hosts
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg clearAmbariHosts(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //region 获取需要缩容的机器
            List<InfoClusterVm> vms = ivmService.getScaleInVms(confCluster.getClusterId(), taskId);
            if (vms == null || vms.size() == 0) {
                getLogger().warn("使用taskId获取缩容机器列表为空，从clearLog获取");
                vms = clearLogMapper.selectInfoClusterVmsByPlanId(currentLog.getPlanId());

                if (vms == null || vms.size() == 0) {
                    //没有datanode节点
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs("不存在VM，忽略。");
                    // region 发送下一个环节的消息
                    planExecService.sendNextActivityMsg(activityLogId, param);
                    // endregion
                    planExecService.complateActivity(currentLog);
                    return resultMsg;
                }
            }
            //endregion

            // 为了保底, 将本次缩容的VM全部都从Ambari删除一轮: 1. 从集群中删除; 2. 从集群外删除
            List<String> vmHostNames = vms.stream().map(InfoClusterVm::getHostName).collect(Collectors.toList());
            getLogger().info("最后从Ambari清理一遍缩容的VM....");
            ResultMsg clearResult = neoAmbariService.deleteAmbariHosts(confCluster.getClusterId(), vmHostNames);
            getLogger().info("最后从Ambari清理一遍缩容的VM完成: result={}, failed={}", clearResult.getResult(),
                    clearResult.getRows());

            //region 获取ambari中包含的机器
            List<String> ambarihosts = neoAmbariService.queryAllHosts(confCluster.getClusterId());
            //endregion

            if (ambarihosts == null || ambarihosts.size() == 0) {
                getLogger().error("ambari 没有查询到主机。");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("ambari 没有查询到主机，忽略。");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            List<String> hosts = getDeleteHosts(vms, ambarihosts);

            if (hosts == null || hosts.size() == 0) {
                getLogger().info("没有找到需要清理的AmbariHost，跳过。");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("可删除的主机为空，忽略。");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            getLogger().info("可以删除的主机列表：{}", hosts);

            //region deleteHosts
            ResultMsg deleteMsg = neoAmbariService.deleteAmbariHosts(confCluster.getClusterId(),hosts);
            getLogger().info("删除AmbariHost结果：{}",deleteMsg);
            if (!deleteMsg.getResult()){
                if (deleteMsg.getRows().size()>0){
                    List<String> hostnames = (List<String>) deleteMsg.getRows();
                    neoAmbariService.saveAmbariHostDelete(confCluster.getClusterId(),currentLog.getPlanId(),hostnames);
                }
            }
            //endregion

            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            planExecService.complateActivity(currentLog);
            return resultMsg;
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            Long durtion = System.currentTimeMillis() - currentLog.getBegtime().getTime() / 1000;
            if (durtion.compareTo(600L) > 0) {
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
            } else {
                planExecService.loopActivity(clientname, message, 30L, activityLogId);
                return resultMsg;
            }
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 清理ambari中的残留del失败的hosts-创建集群或扩容流程完成后清理VM
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg clearAmbariHostsForClearVM(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 查询需要清理的vms

            // clearvm 使用optaskid 存储 上一个planid
            List<InfoClusterVmReject> vmRejectList = vmRejectMapper.getVmRejectsByPlanId(plan.getOpTaskId());

            if (vmRejectList==null || vmRejectList.size()==0){
                getLogger().error("未查询到可以清理的Vm，planId："+plan.getPlanId());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
                planExecService.complateActivity(currentLog);
                getLogger().info("--end_complateActivity");
                return resultMsg;
            }

            List<String> hostNames=new ArrayList<>();

            vmRejectList.stream().forEach(x->{
                hostNames.add(x.getHostName());
            });

            //endregion

            //region 获取ambari中包含的机器
            List<String> ambarihosts = neoAmbariService.queryAllHosts(confCluster.getClusterId());
            //endregion

            if (ambarihosts == null || ambarihosts.size() == 0) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("ambari 没有查询到主机，忽略。");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }
            // 可删除的主机
            List<String> hosts = hostNames.stream().filter(x->ambarihosts.contains(x)).collect(Collectors.toList());

            if (hosts == null || hosts.size() == 0) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("可删除的主机为空，忽略。");
                // region 发送下一个环节的消息
                planExecService.sendNextActivityMsg(activityLogId, param);
                // endregion
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            getLogger().info("可以删除的主机列表：{}", hosts);

            //region deleteHosts
            ResultMsg deleteMsg = neoAmbariService.deleteAmbariHosts(confCluster.getClusterId(), hosts);
            getLogger().info("删除AmbariHost结果：{}",deleteMsg);
            if (!deleteMsg.getResult()) {
                if (deleteMsg.getRows().size()>0){
                    List<String> hostnames = (List<String>) deleteMsg.getRows();
                    neoAmbariService.saveAmbariHostDelete(confCluster.getClusterId(),currentLog.getPlanId(),hostnames);
                }
            }

            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
            planExecService.complateActivity(currentLog);
            return resultMsg;
            //endregion

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 获取可以删除的机器 hostname
     *
     * @param vms
     * @param hosts
     * @return
     */
    private List<String> getDeleteHosts(List<InfoClusterVm> vms, List<String> hosts) {
        CopyOnWriteArrayList<String> deleteHosts = new CopyOnWriteArrayList<>();

        vms.stream().forEach(x -> {
            if (hosts.contains(x.getHostName())) {
                deleteHosts.add(x.getHostName());
            }
        });
        return deleteHosts;
    }

    /**
     * core 节点检查数据块
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg checkDataForCore(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        getLogger().info("checkDataForCore task:" + task);
        //endregion
        try {
            playBookService.hdfsFSck(task.getClusterId());
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            planExecService.sendNextActivityMsg(activityLogId, param);
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;

    }


    /**
     * 创建集群，扩缩容等任务失败 收集日志
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg collectLogs(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }

        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            //region 获取相关的VM
            List<InfoClusterVm> agentvms = getCollectLogVMs(plan.getOpTaskId());

            if (agentvms == null || agentvms.size() == 0) {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs("not found vms");
                planExecService.complateActivity(currentLog);
                return resultMsg;
            }

            StringBuilder sb = new StringBuilder();

            agentvms.stream().forEach(x -> {
                sb.append(x.getInternalip() + ",");
            });
            String nodelist = sb.substring(0, sb.lastIndexOf(","));

            //endregion
            ResultMsg msg = playBookService.savePlaybook(confCluster.getClusterReleaseVer(), BaseImageScripts.RUNTIMING_COLLECTLOG, confCluster.getClusterId(), activityLogId, nodelist, null,confCluster.getRegion());

            if (msg.getResult()) {
                param.put(JobNameConstant.Run_PlayBook, msg.getBizid());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId, param);
            } else {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(msg.getErrorMsg());
            }
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 失败计划任务的planId
     *
     * @param planId
     * @return
     */
    private List<InfoClusterVm> getCollectLogVMs(String planId){
        List<InfoClusterVm> clusterVms = new CopyOnWriteArrayList<>();

        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);

        //region 创建集群
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Create)){
            clusterVms= ivmService.getAllVms(plan.getClusterId());
        }
        //endregion

        //region 扩容
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_ScaleOut)){
            clusterVms = ivmService.getScaleOutVms(plan.getClusterId(),plan.getScalingTaskId());
        }
        //endregion

        //region 磁盘扩容
        if (plan.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Part_ScaleOut)){
            clusterVms = ivmService.getScaleOutVms(plan.getClusterId(),plan.getScalingTaskId());
        }
        //endregion

        return clusterVms.stream().filter(x->{
            return x.getState() == VM_RUNNING;
        }).collect(Collectors.toList());
    }

    /**
     * 优雅缩容等待
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg gracefullWating(String message) {
        getLogger().info("begin gracefullWating:{}", message);
        ResultMsg resultMsg = new ResultMsg();
        try {

            // 0. 解析参数
            JSONObject param = JSON.parseObject(message);
            String taskId = param.getString(ComposeConstant.Task_ID);
            String activityLogId = param.getString("activityLogId");

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);

            //region 查询task信息
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
            getLogger().info("gracefullWating task:{}", task);
            //endregion

            //region 无优雅配置
            if (task.getIsGracefulScalein() == null || task.getIsGracefulScalein().equals(0)) {
                getLogger().info("gracefullWating error,clusterId:{},groupName:{},taskId:{},isGracefulScalein={}",
                        task.getClusterId(),
                        task.getGroupName(),
                        task.getTaskId(),
                        task.getIsGracefulScalein());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("无优雅缩容配置，跳过。");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                return resultMsg;
            }
            //endregion

            Long duration = (System.currentTimeMillis() - currentLog.getBegtime().getTime()) / 1000;
            Long waitingtime = 60L;

            //region 优雅缩容等待
            if (task.getIsGracefulScalein().equals(1) && task.getScaleinWaitingtime() != null) {
                waitingtime = Long.parseLong(task.getScaleinWaitingtime() + "");
                if (duration.compareTo(waitingtime) > 0) {
                    getLogger().info("gracefullWating error,clusterId:{},groupName:{},taskId:{},isGracefulScalein={},waitingTime:{},durationTime:{},decommission over goto next step",
                            task.getClusterId(),
                            task.getGroupName(),
                            task.getTaskId(),
                            task.getIsGracefulScalein(),
                            waitingtime,
                            duration);
                    resultMsg.setResult(true);
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                    currentLog.setLogs("waitingtime:" + waitingtime + ",durationtime:" + duration + "，优雅decommission结束进入下一个步骤。");
                    planExecService.complateActivity(currentLog);
                    planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                    return resultMsg;
                } else {
                    List<InfoClusterVm> vms = ivmService.getScaleInVms(task.getClusterId(), taskId);
                    List<String> hosts = vms.stream().map(p -> p.getHostName()).collect(Collectors.toList());
                    getLogger().info("优雅缩容-需要等待的VM,clusterId={},hosts={}", task.getClusterId(), hosts);
                    ResultMsg running = ambariService.getNodesWithContainerRunning(task.getClusterId());
                    getLogger().info("优雅缩容-有Container运行的节点,res={}", running);
                    if (running.getResult()) {
                        List<String> nodeHosts = new ArrayList<>();
                        Collection<String> intersection = CollUtil.intersection(running.getRows(), hosts);
                        if (CollUtil.isNotEmpty(intersection)) {
                            nodeHosts.addAll(intersection);
                        }
                        boolean isRunning = nodeHosts.size() > 0;
                        if (isRunning) {
                            getLogger().info("Yarn队列中存在运行的任务,clusterId={},node={}", task.getClusterId(), nodeHosts);
                            resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
                        } else {
                            resultMsg.setResult(true);
                            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                            planExecService.complateActivity(currentLog);
                            planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                        }
                    }else {
                        String errMsg = "优雅缩容等待获取的Yarn任务信息异常,结束优雅等待，异常信息："+resultMsg.getErrorMsg();
                        getLogger().error(errMsg);
                        resultMsg.setResult(true);
                        currentLog.setLogs(errMsg);
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        planExecService.complateActivity(currentLog);
                        planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
                    }
                }
            } else {
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
            }
            return resultMsg;
            //endregion
        } catch (Exception e) {
            getLogger().error("gracefullWating error,message:{}", message, e);
        }
        return resultMsg;
    }

    private String getHost(String amHostHttpAddress) {
        return StrUtil.split(amHostHttpAddress, ":").get(0);
    }

    /**
     * 重启大数据服务
     */
    @Override
    public ResultMsg restartClusterService(Map<String, Object> param) {
        ResultMsg resultMsg = new ResultMsg();
        String clusterId = param.get("clusterId").toString();
        String serviceName = param.get("serviceName").toString().toUpperCase();
        String releaseVersion = param.get("releaseVersion").toString().toUpperCase();
        String groupId = param.containsKey("groupId") ? param.get("groupId").toString() : null;

        String taskId = UUID.randomUUID().toString();
        ConfClusterOpTask confClusterOpTask = new ConfClusterOpTask();
        confClusterOpTask.setTaskId(taskId);
        confClusterOpTask.setClusterId(clusterId);
        confClusterOpTask.setServiceName(serviceName);
        confClusterOpTask.setGroupId(groupId);
        confClusterOpTask.setOpreationType(ConfClusterOpTask.OP_TYPE_RESTART);
        confClusterOpTask.setState(0);
        confClusterOpTask.setCreateTime(new Date());

        try {
            confClusterOpTaskMapper.insert(confClusterOpTask);
            ResultMsg planResult = planExecService.createPlanAndRun(clusterId, releaseVersion, InfoClusterOperationPlan.Plan_OP_ClusterService_Restart, null, taskId);
            if (!planResult.getResult()) {
                return planResult;
            }
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("taskId", taskId);
            resultData.put("clusterId", clusterId);
            resultData.put("serviceName", serviceName);
            resultMsg.setResult(true);
            resultMsg.setData(resultData);
        } catch (Exception e) {
            getLogger().error("ClusterServiceImpl.restartClusterService error. param: {}, e: {}", JSON.toJSONString(param), e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("ClusterServiceImpl.restartClusterService error.");
        }
        return resultMsg;
    }

    /**
     * 重启大数据服务
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg restartSDPService(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        // endregion

        // region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        // endregion

        // region 获取plan信息
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(currentLog.getPlanId());
        // endregion

        // region 查询optask信息
        ConfClusterOpTask task = opTaskMapper.selectByPrimaryKey(plan.getOpTaskId());
        getLogger().info("task:" + task.toString());
        // endregion

        // region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        // endregion

        String groupName = null;
        String groupId = task.getGroupId();
        if (StringUtils.isNotBlank(groupId)) {
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(groupId);
            groupName = confClusterHostGroup.getGroupName();
        }

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            // region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            // endregion

            InProgressResult inProgressResult = null;
            if (groupName != null) {
                inProgressResult = ambariService.restartHostsComponents(ambariInfo, confCluster.getClusterId(), task.getServiceName(), groupName);
            } else {
                inProgressResult = ambariService.restartService(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), task.getServiceName());
            }

            if (inProgressResult.isSuccess()) {
                param.put(JobNameConstant.Restart_Cluster_Service, inProgressResult.getRequestId() + "");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(param.toJSONString());
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), param);
            } else {
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(inProgressResult.getMessage());
            }

        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(e.getMessage());
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 查询大数据服务重启进度
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg QuerySDPServiceRestartProcess(String message) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            // 0. 解析参数
            JSONObject param = JSON.parseObject(message);
            String rpjobId = param.getString(JobNameConstant.Restart_Cluster_Service);
            String activityLogId = param.getString("activityLogId");

            InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

            getLogger().info("获取ActivityLog:" + currentLog.toString());
            JSONObject paramjson = JSON.parseObject(currentLog.getParaminfo());

            QueryProgressCmd qcmd = getQueryProgressCmd(rpjobId, confCluster);
            QueryProgressResult msg = ambariService.queryCreateClusterProgress(qcmd);
            getLogger().info("请求Ambari结束。");
            if (msg != null) {
                msg.setTaskList(null);
                getLogger().info("查询Ambari接口返回:" + JSONObject.toJSONString(msg));
            }
            if (msg.isSuccess()) {
                getLogger().info("服务重启完成。");
                resultMsg.setResult(true);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs(JSON.toJSONString(msg));
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(currentLog.getActivityLogId(), paramjson);
                return resultMsg;
            }

            if (msg.isFail()) {
                return ambariFailed(currentLog, msg);
            }

            if (msg.isPending()) {
                Long timeout = Long.parseLong(pendingHostTimeOut);
                Long duration = (System.currentTimeMillis() - currentLog.getBegtime().getTime()) / 1000;
                if (duration.compareTo(timeout) > 0) {
                    return ambariFailed(currentLog, msg);
                }
                getLogger().info("PendingHost,缓冲时间内，继续loopquery。");
            }
            resultMsg = planExecService.loopActivity(clientname, message, 30L, activityLogId);
        } catch (Exception e) {
            getLogger().error("QuerySDPServiceRestartProcess:", e);
        }
        return resultMsg;
    }

    @Override
    public ResultMsg createConfigGroup(String clusterId, String groupName, String groupId, String vmRole) {
        getLogger().info("创建集群配置组开始,clusterId={},groupName={},groupId={}", clusterId, groupName, groupId);
        try {
            AmbariInfo ambariInfo = getAmbariInfo(clusterId);
            CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(api.getApiClient());

            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            String ambariClusterName = getSdpClusterNameforAmbari(clusterId);
            List<ConfClusterHostGroupAppsConfig> appConfigsByGroupId = confClusterHostGroupAppsConfigMapper.selectByGroupId(groupId);
            List<ConfigGroup> configGroups = genConfigGroups(groupName, confCluster, ambariClusterName, appConfigsByGroupId, vmRole);
            List<CreateConfigGroupResponse> responses = new ArrayList<>();
            for (ConfigGroup configGroup : configGroups) {
                InfoAmbariConfigGroup ambariConfigGroup = new InfoAmbariConfigGroup();
                ambariConfigGroup.setClusterId(clusterId);
                ambariConfigGroup.setGroupId(groupId);
                ambariConfigGroup.setAmbariServiceName(configGroup.getServiceName());
                ambariConfigGroup.setAmbariGroupName(configGroup.getGroupName());
                ambariConfigGroup.setAmbariTag(configGroup.getTag());
                ambariConfigGroup.setAmbariClusterName(ambariClusterName);
                ambariConfigGroup.setAmbariDescription(configGroup.getDescription());
                ambariConfigGroup.setState(InfoAmbariConfigGroup.STATE_CREATING);
                ambariConfigGroup.setCreatedTime(new Date());
                ambariConfigGroup.setSdpGroupName(groupName);
                ambariConfigGroup.setConfId(UUID.randomUUID().toString());
                getLogger().info("ambariConfigGroupMapper insert group:{}", ambariConfigGroup);
                ambariConfigGroupMapper.insertSelective(ambariConfigGroup);

                getLogger().info("集群分组参数:clusterId={},reqBody={}", clusterId, JSONUtil.toJsonStr(configGroups));
                CreateConfigGroupResponse groupResponse = api.createConfigGroup(ambariClusterName, Arrays.asList(configGroup));
                responses.add(groupResponse);
                getLogger().info("创建集群配置组返回:clusterId={},groupId={},res={}", clusterId, groupId, JSONUtil.toJsonStr(groupResponse));
                Integer configGroupId = groupResponse.getResources().get(0).getConfigGroup().getId();

                InfoAmbariConfigGroup updateAmbariId = new InfoAmbariConfigGroup();
                updateAmbariId.setConfId(ambariConfigGroup.getConfId());
                updateAmbariId.setAmbariId(Long.parseLong(configGroupId + ""));
                updateAmbariId.setState(InfoAmbariConfigGroup.STATE_RUNNING);
                ambariConfigGroupMapper.updateByPrimaryKeySelective(updateAmbariId);
            }
            getLogger().info("创建集群配置组完成,{}", clusterId);
        } catch (Exception ex) {
            getLogger().error("创建集群配置组异常,{}", clusterId, ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
        return ResultMsg.SUCCESS();
    }

    @Override
    public ResultMsg deleteAmbariHostGroup(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

//            ClusterHostGroupManager hostGroupManager = new ClusterHostGroupManager(confCluster.getClusterId(),
//                    confCluster.getClusterName());

            //region 获取ambari信息
            AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
            getLogger().info("ambari_info:" + JSON.toJSONString(ambariInfo));
            //endregion

            //region deleteConfigGroup
            CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(api.getApiClient());
            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
            if (hostGroup != null && Objects.equals(task.getDeleteGroup(), ConfScalingTask.SCALINGTASK_DELETE_GROUP)) {
                List<InfoAmbariConfigGroup> infoAmbariConfigGroups = ambariConfigGroupMapper.selectByGroupIdAndStates(hostGroup.getGroupId(),
                        Arrays.asList(InfoAmbariConfigGroup.STATE_RUNNING, InfoAmbariConfigGroup.STATE_SCALEIN, InfoAmbariConfigGroup.STATE_SCALEOUT));
                if (CollUtil.isNotEmpty(infoAmbariConfigGroups)) {
                    Map<Long, List<InfoAmbariConfigGroup>> collect = infoAmbariConfigGroups.stream()
                            .collect(Collectors.groupingBy(InfoAmbariConfigGroup::getAmbariId));
                    for (Long ambariConfigGroupId : collect.keySet()) {
                        api.deleteConfigGroup(getSdpClusterNameforAmbari(confCluster.getClusterId()), ambariConfigGroupId);
                    }
                }
                ambariConfigGroupMapper.updateStateByGroupId(hostGroup.getGroupId(), InfoAmbariConfigGroup.STATE_DELETE);
            }

            resultMsg.setResult(true);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            // region 发送下一个环节的消息
            planExecService.sendNextActivityMsg(activityLogId, param);
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
            planExecService.sendNextActivityMsg(activityLogId, param);
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 缩容清理Ganglia数据
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg clearGangliaData(String message) {
        ResultMsg resultMsg = new ResultMsg();
        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        String taskId = param.getString(ComposeConstant.Task_ID);
        //endregion

        ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        //region 查询task信息
        ConfScalingTask task = taskMapper.selectByPrimaryKey(taskId);
        getLogger().info("task:" + task.toString());
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime() == null) {
            currentLog.setBegtime(new Date());
        }
        //endregion

        if (!confCluster.getEnableGanglia().equals(1)){
            param.put(JobNameConstant.Run_PlayBook,"");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs("未安装ganglia，忽略。");
            planExecService.sendNextActivityMsg(activityLogId,param);
            planExecService.complateActivity(currentLog);
            resultMsg.setResult(true);
            getLogger().error("未安装ganglia，忽略。");
            return resultMsg;
        }

        //region task 数据校验
        if (task == null) {
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("task为空");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }
        //endregion

        try {
            // 1.获取安装的ambari server的机器
            List<InfoClusterVm> ambarivm=ivmService.getRoleVms(confCluster.getClusterId(),"ambari");

            List<InfoClusterVm> allvms=infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(confCluster.getClusterId(),taskId);


            if (ambarivm==null || ambarivm.size()==0){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("not found ambari vm");
                param.put(JobNameConstant.Run_PlayBook,"");
                planExecService.sendNextActivityMsg(activityLogId,param);
                planExecService.complateActivity(currentLog);
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("not found ambari vm");
                getLogger().error("not found ambari vm:"+message);
                return resultMsg;
            }

            String nodelist=ambarivm.get(0).getInternalip();

            StringBuilder sb=new StringBuilder();
            allvms.stream().forEach(x->{
                        sb.append(x.getHostName().split(".")[0]+",");
                    }
            );
            String ganglialist=sb.substring(0,sb.lastIndexOf(","));

            // 2. 执行playbook
            ResultMsg msg= playBookService.savePlaybook(
                    confCluster.getClusterReleaseVer(),
                    BaseImageScripts.RUNTIMING_CLEAER_GANGLIA_DATA,
                    confCluster.getClusterId(),
                    activityLogId,
                    nodelist,
                    ganglialist,confCluster.getRegion());

            if (msg.getResult()){
                param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
                planExecService.sendNextActivityMsg(activityLogId,param);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            }else{
                currentLog.setLogs(msg.getErrorMsg());
                param.put(JobNameConstant.Run_PlayBook,"");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            }
            planExecService.complateActivity(currentLog);

            return resultMsg;
        } catch (Exception e) {
            getLogger().error("--Exception---", e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            param.put(JobNameConstant.Run_PlayBook,"");
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
            planExecService.sendNextActivityMsg(activityLogId, param);
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 清理VM时清理Ganglia数据
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg clearGangliaDataForClearVM(String message) {
        return null;
    }

    private List<ConfigGroup> genConfigGroups(String groupName,
                                              ConfCluster confCluster,
                                              String ambariClusterName,
                                              List<ConfClusterHostGroupAppsConfig> appConfigsByConfigId,
                                              String vmRole) {
        getLogger().info("begin genConfigGroups groupName:{},confCluster:{},ambariClusterName:{},appConfigsByConfigId:{},vmRole:{}",
                groupName,
                confCluster,
                ambariClusterName,
                appConfigsByConfigId,
                vmRole);

        List<ConfigGroup> configGroups = new ArrayList<>();
        // 按AppName分组,AppName是大数据服务,如HDFS,Yarn
        Map<String, List<ConfClusterHostGroupAppsConfig>> groupByAppNameMap = appConfigsByConfigId.stream()
                .collect(Collectors.groupingBy(ConfClusterHostGroupAppsConfig::getAppName));
        Iterator<Map.Entry<String, List<ConfClusterHostGroupAppsConfig>>> itrGroupByAppNameMap = groupByAppNameMap.entrySet().iterator();
        while (itrGroupByAppNameMap.hasNext()) {
            // 一个AppName,对应一个配置组,如HDFS对应一个配置组, Yarn对应一个配置组
            Map.Entry<String, List<ConfClusterHostGroupAppsConfig>> nextGroupByAppName = itrGroupByAppNameMap.next();
            // service就是大数据服务名,如HDFS或Yarn
            String service = nextGroupByAppName.getKey();
            ConfigGroup config = new ConfigGroup();
            config.setGroupName(confCluster.getClusterName() + "_" + service + "_" + groupName);
            config.setServiceName(service.toUpperCase());
            config.setClusterName(ambariClusterName);
            config.setDescription(config.getServiceName());
            config.setTag(service);
            List<Map<String, Object>> desiredConfigs = new ArrayList<>();
            // 配置按配置文件名分组   配置文件名 -> 配置List
            Map<String, List<ConfClusterHostGroupAppsConfig>> groupByClassification = nextGroupByAppName.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(ConfClusterHostGroupAppsConfig::getAppConfigClassification));
            Iterator<Map.Entry<String, List<ConfClusterHostGroupAppsConfig>>> itrGroupByClassification = groupByClassification.entrySet().iterator();
            while (itrGroupByClassification.hasNext()) {
                Map.Entry<String, List<ConfClusterHostGroupAppsConfig>> nextGroupByClassification = itrGroupByClassification.next();
                String classification = nextGroupByClassification.getKey();
                Map<String, Object> desiredConfig = new HashMap<>();
                Map<String, String> properties = new HashMap<>();
                desiredConfig.put("type", classification);
                desiredConfig.put("properties", properties);
                for (ConfClusterHostGroupAppsConfig hostGroupAppsConfig : nextGroupByClassification.getValue()) {
                    properties.put(hostGroupAppsConfig.getConfigItem(), hostGroupAppsConfig.getConfigVal());
                }
                desiredConfigs.add(desiredConfig);
            }
            config.setDesiredConfigs(desiredConfigs);
            getLogger().info("construct ConfigGroup clusterName:{},service:{},groupName:{},config:{}",
                    confCluster.getClusterName(),
                    service,
                    groupName,
                    config);
            configGroups.add(config);
        }

//        if (CollUtil.isEmpty(configGroups)) {
        AmbariInfo ambariInfo = getAmbariInfo(confCluster.getClusterId());
        //正在运行的实例
        List<InfoClusterVm> runningVms = ivmService.getGroupVmsByState(confCluster.getClusterId(), groupName, VM_RUNNING);
        //获取组件信息
        Optional<InfoClusterVm> vm = runningVms.stream().filter(x -> (x.getScaleoutTaskId() == null || x.getScaleoutTaskId().equals(""))
                && (x.getMaintenanceMode()==null || x.getMaintenanceMode().equals(0))).findFirst();

        List<String> components = new ArrayList<>();
        if (!vm.isPresent()) {
            getLogger().info("未找到之前运行VM,从ambari_template或cluster_template中获取component信息");
            ConfScalingTask task = new ConfScalingTask();
            task.setClusterId(confCluster.getClusterId());
            task.setGroupName(groupName);
            task.setVmRole(vmRole);
            components = getComponents(task, confCluster, components);
        } else {
            //region 获取当前vm上的组件
            getLogger().info("正在运行的vm：" + vm.get());
            components = ambariService.getComponentsByHost(ambariInfo, getSdpClusterNameforAmbari(confCluster.getClusterId()), vm.get().getHostName());
            //endregion
        }
        List<String> serviceNames = new ArrayList<>();
        for (String component : components) {
            ConfigGroup configGroup = new ConfigGroup();
            Optional<BDComponent> parse = BDComponent.parse(component);
            if (!parse.isPresent()) {
                getLogger().info("不存在的component:{}", component);
                continue;
            }
            String service = parse.get().getService().name();
            if (serviceNames.contains(service)) {
                continue;
            }
            serviceNames.add(service);
            configGroup.setGroupName(confCluster.getClusterName() + "_" + service + "_" + groupName);
            configGroup.setServiceName(service);
            configGroup.setClusterName(ambariClusterName);
            configGroup.setDescription(configGroup.getServiceName());
            configGroup.setTag(service);
            addConfigGroupIfNotExists(configGroups, configGroup);
        }
//        }

        getLogger().warn("新建configGroup生成configGroup内容：{}", JacksonUtils.toJson(configGroups));

        // 处理自定义配置：生成配置，处理spark配置
        handleNewConfigGroupConfig(configGroups, groupName, confCluster, vmRole);

        return configGroups;
    }

    private void addConfigGroupIfNotExists(List<ConfigGroup> configGroups, ConfigGroup newConfigGroup) {
        for (ConfigGroup group : configGroups) {
            if (StrUtil.equals(group.getGroupName(), newConfigGroup.getGroupName())) {
                return;
            }
        }
        configGroups.add(newConfigGroup);
    }

    private void handleNewConfigGroupConfig(List<ConfigGroup> configGroups, String groupName, ConfCluster confCluster, String vmRole) {
        List<BlueprintConfiguration> customConfigs = generateGroupCustomConfig(groupName, confCluster, vmRole);
        for (BlueprintConfiguration customConfig : customConfigs) {
            BDService service = ConfigClassification.parseToService(customConfig.getConfigItemName());
            if (Objects.isNull(service)) {
                getLogger().warn("处理新配置组的自定义配置时，根据配置文件类型没找到对应的Service。 [configItemName={}]", customConfig.getConfigItemName());
                continue;
            }

            ConfigGroup configGroup = findConfigGroupByServiceName(configGroups, service);
            if (Objects.isNull(configGroup)) {
                getLogger().error("处理新配置组的自定义配置时，没找到Service对应的配置组，跳过。 [service={}]", service);
                continue;
            }

            // 如果不存在，就新增（不是覆盖，因为生成的配置没有手动配置的优先级高
            configGroup.completeOneDesiredConfig(customConfig.getConfigItemName(), customConfig.getProperties());
        }

        // 处理spark3配置
        for (ConfigGroup configGroup : configGroups) {
            configGroup.handleSpark3Config();
            configGroup.addHostGroupToYarnSite();
        }
        getLogger().info("创建新实例组生成自定义配置完成：[clusterId={}, config:{}]", confCluster.getClusterId(), JacksonUtils.toJson(configGroups));
    }

    ConfigGroup findConfigGroupByServiceName(List<ConfigGroup> configGroups, BDService service) {
        Optional<ConfigGroup> opt = configGroups.stream().filter(configGroup -> {
            return service.name().equalsIgnoreCase(configGroup.getServiceName());
        }).findFirst();

        return opt.isPresent()?opt.get(): null;
    }

    List<BlueprintConfiguration> generateGroupCustomConfig(String groupName, ConfCluster confCluster, String vmRole) {
        List<BlueprintConfiguration> configurations = new ArrayList<>();
        List<ConfClusterVm> roleVmSpecList = confClusterVmMapper.getVmConfsByGroupName(groupName, confCluster.getClusterId());
        if (CollectionUtil.isEmpty(roleVmSpecList)) {
            getLogger().warn("创建新实例组-生成实例组配置时，没找实例组对应的主机规格信息。 [clusterId={}, groupName={}, vmRole={}]",
                    confCluster.getClusterId(), groupName, vmRole);
        }

        if (CollectionUtil.isNotEmpty(roleVmSpecList)) {
            ConfClusterVm vmSpec = roleVmSpecList.get(0);

            // 生成Group的NodeManager自定义配置
            HostInstance instance = new HostInstance();
            instance.setvCpu(Convert.toInt(vmSpec.getVcpus(), 0));
            instance.setMemoryGB(Convert.toInt(vmSpec.getMemory(), 0));
            if (Objects.equals(instance.getvCpu(), 0) || Objects.equals(instance.getMemoryGB(), 0)) {
                getLogger().warn("创建新实例组-生成实例组配置时，实例组的主机配置不正确，cpu 或 内存为0。 [clusterId={}, groupName={}, vmRole={}, vcpus={}, memoryGB={}]",
                        confCluster.getClusterId(), groupName, vmRole, vmSpec.getVcpus(), vmSpec.getMemory());
            } else {
                HostGroupRole role = HostGroupRole.parse(vmRole);
                configurations = ambariService.generateCustomConfig(instance, role);
            }

            // 生成Task实例组的多磁盘配置
            List<ConfClusterVmDataVolume> dataVols = dataVolumeMapper.selectByVmConfId(vmSpec.getVmConfId());
            if (CollectionUtil.isEmpty(dataVols)) {
                getLogger().warn("创建新实例组-生成实例组多磁盘配置时，没找到实例组的数据盘配置, 不生成多磁盘配置。 [clusterId={}, groupName={}, vmConfId={}]",
                        confCluster.getClusterId(), groupName, vmSpec.getVmConfId());
            } else {
                if (StrUtil.equalsIgnoreCase(vmRole, HostGroupRole.TASK.name())) {
                    // 只有Task实例组才根据磁盘数量,生成多磁盘配置
                    Integer diskCount = dataVols.get(0).getCount();
                    DiskInfo diskInfo = new DiskInfo();
                    diskInfo.setCount(diskCount);
                    instance.addDiskInfo(diskInfo);
                    List<Map<String, BlueprintConfiguration>> multiDiskConfigs = ambariService.generateOneTaskGroupMultiDiskConfig(
                            confCluster.getClusterReleaseVer(), Arrays.asList(instance), ConfigItemType.parse(confCluster.getIsHa()));
                    for (Map<String, BlueprintConfiguration> multiDiskConfig : multiDiskConfigs) {
                        configurations.addAll(multiDiskConfig.values());
                    }
                } else {
                    getLogger().info("创建新实例组-生成实例组多磁盘配置时，实例组不是TASK类型，跳过不处理。[clusterId={}, groupName={}, vmConfId={}]",
                            confCluster.getClusterId(), groupName, vmSpec.getVmConfId());
                }
            }
        }
        return configurations;
    }


    /**
     * 构建clustercmd用于创建
     *
     * @param clusterId
     * @return
     */
    private CreateClusterCmd buildCreateClusterCmd(String clusterId) {
        CreateClusterCmd cmd = new CreateClusterCmd();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        cmd.setClusterId(clusterId);
        cmd.setHa(confCluster.getIsHa() == 1);
        cmd.setEnableGanglia(Objects.isNull(confCluster.getEnableGanglia()) ? 0 : confCluster.getEnableGanglia());
        cmd.setClusterName(getSdpClusterNameforAmbari(clusterId));
        cmd.setBlueprintName("blue-print-" + confCluster.getClusterName());
        String[] vers = confCluster.getClusterReleaseVer().split("-");

        cmd.setStackName(vers[0]);
        cmd.setStackVersion(vers[1]);

        cmd.setServices(getClusterSevice(clusterId));
//        if (confCluster.getIsHa() == 1) {
//            cmd.setAmbariHosts(getVmRoleHosts(clusterId, "ambari"));
//            cmd.setMasterHosts(getVmRoleHosts(clusterId, "master"));
//        } else {
//            cmd.setMasterHosts(getVmRoleHosts(clusterId, "ambari"));
//        }
//        cmd.setCoreHosts(getVmRoleHosts(clusterId, "core"));
//        cmd.setTaskHosts(getVmRoleHosts(clusterId, "task"));
        cmd.setDbConfigs(getDBConnectInfos(clusterId));
        cmd.setConfigurations(getConfigurations(clusterId));
        cmd.setAmbarInfo(getAmbariInfo(clusterId));
        cmd.setMiClientId(confCluster.getVmMIClientId());
        cmd.setMiTenantId(confCluster.getVmMITenantId());


        // 查询并生成HostGroup，主要是Ambari和Master在高可用和非高可用的情况下不一样。
        List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectByClusterId(clusterId);

        Map<HostGroupRole, List<ConfClusterHostGroup>> groupedConfClusterHostGroups = confClusterHostGroups.stream().collect(Collectors.groupingBy(group -> {
            return HostGroupRole.parse(group.getVmRole());
        }));

        // 针对非高可用的Ambari作特殊处理：非高可用时，Ambari转为Master
        if (confCluster.getIsHa() == 1) {
            List<ConfClusterHostGroup> ambariHostGroup = groupedConfClusterHostGroups.get(HostGroupRole.AMBARI);
            if (CollectionUtil.isEmpty(ambariHostGroup)) {
                throw new RuntimeException("生成集群配置模板的主机组信息，参数错误：未找到Ambari主机组，集群类型：高可用");
            }
            generateHostGroup(clusterId, cmd, ambariHostGroup.get(0));

            List<ConfClusterHostGroup> masterHostGroup = groupedConfClusterHostGroups.get(HostGroupRole.MASTER);
            if (CollectionUtil.isEmpty(masterHostGroup)) {
                throw new RuntimeException("生成集群配置模板的主机组信息，参数错误：未找到Master主机组，集群类型：高可用");
            }
            generateHostGroup(clusterId, cmd, masterHostGroup.get(0));
        } else {
            List<ConfClusterHostGroup> masterHostGroup = groupedConfClusterHostGroups.get(HostGroupRole.AMBARI);
            if (CollectionUtil.isEmpty(masterHostGroup)) {
                throw new RuntimeException("生成集群配置模板的主机组信息，参数错误：未找到Ambari主机组，集群类型：非高可用");
            }
            ClusterHostGroup hostGroup = generateNonHaAmbariHostGroup(clusterId, masterHostGroup.get(0));
            cmd.addClusterHostGroup(hostGroup);
        }

        for (ConfClusterHostGroup confClusterHostGroup : confClusterHostGroups) {
            HostGroupRole role = HostGroupRole.parse(confClusterHostGroup.getVmRole());
            // Master与Ambari跳过, 因为上面已经处理过了.
            if (role == HostGroupRole.MASTER || role == HostGroupRole.AMBARI) {
                continue;
            }

            // Task和Core要按Vm Sku分组生成hostGroup
            if (role == HostGroupRole.TASK || role == HostGroupRole.CORE) {
                List<ConfHostGroupVmSku> vmHostGroupSkus = confHostGroupVmSkuMapper.selectByClusterIdAndGroupId(confClusterHostGroup.getClusterId(),
                        confClusterHostGroup.getGroupId());
                List<String> vmSkuNames = vmHostGroupSkus.stream()
                        .map(ConfHostGroupVmSku::getSku)
                        .distinct()
                        .collect(Collectors.toList());
                for (String vmSkuName : vmSkuNames) {
                    generateHostGroup(clusterId, cmd, confClusterHostGroup, vmSkuName);
                }
            }
        }

        return cmd;
    }

    private ResultMsg getRegHosts(String clusterId,String taskId) {
        ResultMsg msg = new ResultMsg();
        List<InfoClusterVm> clusterVms;

        if (StringUtils.isNotEmpty(taskId)){
            clusterVms = ivmService.getScaleOutVms(clusterId,taskId);
        }else{
            clusterVms = ivmService.getAllVms(clusterId);
        }
        List<String> runningvms = new CopyOnWriteArrayList<>();

        clusterVms.stream().forEach(x->{
            if (x.getState() == VM_RUNNING){
                runningvms.add(x.getHostName());
            }
        });

        if (runningvms == null || runningvms.size() == 0){
            getLogger().error("获取Ambari RegHost可用的vm为空。");
            msg.setResult(false);
            msg.setErrorMsg("获取Ambari RegHost可用的vm为空");
            msg.setTotal(0);
            return msg;
        }
        return checkRegHostNameActive(clusterId,runningvms);
    }


    private ResultMsg checkRegHostNameActive(String clusterId,List<String> hostNames){
        ResultMsg ckmsg = new ResultMsg();
        try {
            // 1.获取已经注册到ambari到vm，加入到集群前，不指定集群名称
            List<String> regVms = ambariService.queryHosts(getAmbariInfo(clusterId),
                    "",
                    AmbariHostState.HEALTHY);
            getLogger().info("已经注册到ambari server vm：" + regVms);
            // 2.查询lostvm
            List<String> lostVms = hostNames.stream().filter(x -> {
                // 检查已注册的主机中, 是否包含本次申请的主机. 只取不包含的.
                return !regVms.contains(x.toLowerCase());
            }).collect(Collectors.toList());

            if (lostVms != null && lostVms.size() > 0) {
                ckmsg.setResult(false);
                ckmsg.setRows(lostVms);
                ckmsg.setData(hostNames);
                getLogger().warn("存在未注册到ambari server的VM" + lostVms);
                return ckmsg;
            } else {
                ckmsg.setResult(true);
                ckmsg.setData(hostNames);
                return ckmsg;
            }
        }catch (Exception e){
            getLogger().warn("查询已注册VM发生异常，",e);
            ckmsg.setResult(false);
            ckmsg.setRows(hostNames);
            ckmsg.setErrorMsg("查询已注册VM发生异常，" + ExceptionUtils.getStackTrace(e));
            return ckmsg;
        }
    }

    @NotNull
    private ClusterHostGroup generateNonHaAmbariHostGroup(String clusterId, ConfClusterHostGroup masterHostGroup) {
        ClusterHostGroup hostGroup = new ClusterHostGroup();
        hostGroup.setRole(HostGroupRole.MASTER);
        hostGroup.setGroupId(masterHostGroup.getGroupId());
        hostGroup.setGroupName("ambari");
        List<ConfClusterHostGroupAppsConfig> hostGroupConfig = confClusterHostGroupAppsConfigMapper.selectByGroupId(masterHostGroup.getGroupId());
        hostGroup.setGroupConfgs(hostGroupConfig);

        // 非高可用很特殊，使用前端传过来的ambari的主机，但是角色是MASTER。
        List<HostInstance> hosts = getVmGroupHosts(clusterId, "ambari");
        hostGroup.setHosts(hosts);
        return hostGroup;
    }

    /**
     * 生成配置组
     * @param clusterId 集群ID
     * @param cmd 创建集群的命令对象
     * @param confClusterHostGroup 集群的hostGroup对象
     */
    private void generateHostGroup(String clusterId, CreateClusterCmd cmd, ConfClusterHostGroup confClusterHostGroup) {
        generateHostGroup(clusterId, cmd, confClusterHostGroup, null);
    }

    /**
     * 生成配置组
     * @param clusterId 集群ID
     * @param cmd 创建集群的命令对象
     * @param confClusterHostGroup 集群的hostGroup对象
     * @param vmSkuName 集群HostGroup中主机的skuVmName
     */
    private void generateHostGroup(String clusterId, CreateClusterCmd cmd, ConfClusterHostGroup confClusterHostGroup, String vmSkuName) {
        ClusterHostGroup hostGroup = new ClusterHostGroup();
        hostGroup.setRole(HostGroupRole.parse(confClusterHostGroup.getVmRole()));
        hostGroup.setGroupId(confClusterHostGroup.getGroupId());
        hostGroup.setGroupName(confClusterHostGroup.getGroupName());
        if (StrUtil.isNotBlank(vmSkuName)) {
            hostGroup.setVmSkuName(vmSkuName);
            // 带SkuName的HostGroup,名称后面加上SkuName, 否则会重名
            hostGroup.setGroupName(confClusterHostGroup.getGroupName() + "_" + vmSkuName);
            // 2024-12-10: Core实例组不按SkuName命名
//            if (Objects.equals(hostGroup.getRole(), HostGroupRole.CORE)) {
//                hostGroup.setGroupName(HostGroupRole.CORE.name());
//            } else {
//                hostGroup.setGroupName(confClusterHostGroup.getGroupName() + "_" + vmSkuName);
//            }
        }

        List<HostInstance> hosts = getVmGroupHosts(clusterId, confClusterHostGroup.getGroupName(), vmSkuName);
        if (CollectionUtil.isEmpty(hosts)) {
            getLogger().info("生成创建集群的配置组失败，跳过。clusterId={}, clusterName={}, hostGroupName={}, 原因: 该配置组没有VM",
                   clusterId, cmd.getClusterName(), confClusterHostGroup.getGroupName() );
            return;
        }
        hostGroup.setHosts(hosts);

        List<ConfClusterHostGroupAppsConfig> hostGroupConfig = confClusterHostGroupAppsConfigMapper.selectByGroupId(confClusterHostGroup.getGroupId());
        hostGroup.setGroupConfgs(hostGroupConfig);
        cmd.addClusterHostGroup(hostGroup);
    }

    /**
     * 获取 vmserver
     *
     * @param clusterId
     * @param groupName
     * @return
     */
    private List<HostInstance> getVmGroupHosts(String clusterId, String groupName) {
        return getVmGroupHosts(clusterId, groupName, null);
    }

    /**
     * 获取 vmserver
     *
     * @param clusterId
     * @param groupName
     * @param skuVmName skuName
     * @return
     */
    private List<HostInstance> getVmGroupHosts(String clusterId, String groupName, String skuVmName) {
        List<HostInstance> vmRoleHosts = new ArrayList<>();
        List<InfoClusterVm> vms = new ArrayList<>();
        if (StrUtil.isNotBlank(skuVmName)) {
            vms = ivmService.getGroupVms(clusterId, groupName, skuVmName);
        } else {
            vms = ivmService.getGroupVms(clusterId, groupName);
        }

        if (!CollectionUtils.isEmpty(vms)) {
            List<ConfHostGroupVmSku> vmSkus = confHostGroupVmSkuMapper.selectByClusterIdAndGroupName(clusterId, groupName);
            Map<String, ConfHostGroupVmSku> vmSkuMap = vmSkus.stream()
                    .collect(Collectors.toMap(ConfHostGroupVmSku::getSku, Function.identity(), (k1, k2) -> k2));

            List<ConfClusterVm> confvms = confClusterVmMapper.getVmConfsByGroupName(groupName, clusterId);
            List<ConfClusterVmDataVolume> vmDataVolumes = dataVolumeMapper.selectByVmConfId(confvms.get(0).getVmConfId());
            List<DiskInfo> diskInfos = new ArrayList<>();
            vmDataVolumes.stream().forEach(item -> {
                DiskInfo diskInfo = new DiskInfo();
                diskInfo.setSizeGB(item.getDataVolumeSize());
                diskInfo.setSkuName(item.getDataVolumeType());
                diskInfo.setCount(item.getCount());
                diskInfos.add(diskInfo);
            });

            vms.stream().forEach(x -> {
                HostInstance hostInstance = new HostInstance();
                hostInstance.setHostName(x.getHostName());
                hostInstance.setHostRole(x.getVmRole());
                ConfHostGroupVmSku vmSku = vmSkuMap.get(x.getSkuName());
                if (Objects.nonNull(vmSku)) {
                    hostInstance.setMemoryGB(Convert.toInt(vmSku.getMemory()));
                    hostInstance.setvCpu(Convert.toInt(vmSku.getVcpus()));
                } else {
                    hostInstance.setMemoryGB(Convert.toInt(confvms.get(0).getMemory()));
                    hostInstance.setvCpu(Convert.toInt(confvms.get(0).getVcpus()));
                }
                hostInstance.setOsDiskSize(confvms.get(0).getOsVolumeSize());
                hostInstance.setDisks(diskInfos);
                vmRoleHosts.add(hostInstance);
            });
        }
        return vmRoleHosts;
    }

    /**
     * 构建复制集群请求ambari创建集群的数据报文
     *
     * @param confCluster
     * @return
     */
    private DuplicateClusterCmd buildDuplicateClusterCmd(ConfCluster confCluster) {
        DuplicateClusterCmd cmd = new DuplicateClusterCmd();
        BeanUtils.copyProperties(buildCreateClusterCmd(confCluster.getClusterId()), cmd);
        cmd.setOriginClusterName(getSdpClusterNameforAmbari(confCluster.getSrcClusterId()));
        cmd.setOriginAmbariInfo(getAmbariInfo(confCluster.getSrcClusterId()));
        cmd.setSrcClusterId(confCluster.getSrcClusterId());
        return cmd;
    }


    /**
     * 根据clusterid 获取 ambari集群名称
     * 去除 - 特殊字符
     *
     * @param clusterId
     * @return
     */
    @Override
    public String getSdpClusterNameforAmbari(String clusterId) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        return confCluster.getClusterName().replaceAll("-", "");
    }

    private static final String RUNING_CLUSTEER_INFO_COLLECT_TASK_LOCK_KEY = "RUNING_CLUSTEER_INFO_COLLECT_TASK_LOCK_KEY";
    @PostConstruct
    public void loadRuningClusterInfoCollectTask(){
        if (!redisLock.tryLock(RUNING_CLUSTEER_INFO_COLLECT_TASK_LOCK_KEY)) {
            getLogger().info("加载集群收集信息获取锁失败 key: {}",RUNING_CLUSTEER_INFO_COLLECT_TASK_LOCK_KEY);
            return;
        }
        try {
            List<InfoClusterInfoCollectLog> infoClusterInfoCollectLogs = infoClusterInfoCollectLogMapper.selectAll(new InfoClusterInfoCollectLog());
            for (InfoClusterInfoCollectLog infoClusterInfoCollectLog : infoClusterInfoCollectLogs) {
                if (Objects.equals(infoClusterInfoCollectLog.getState(), InfoClusterInfoCollectLog.RUNING)) {
                    checkAnsibleTask(infoClusterInfoCollectLog);
                }
            }
        } catch (Exception e) {
            getLogger().error("loadRuningClusterInfoCollectTask发生异常",e);
        }finally {
            redisLock.unlock(RUNING_CLUSTEER_INFO_COLLECT_TASK_LOCK_KEY);
        }
    }
    @Override
    public ResultMsg collectClusterInfo(String clusterId) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster == null) {
            return ResultMsg.FAILURE("集群不存在");
        }
        if (confCluster.getState() !=  CREATED) {
            return ResultMsg.FAILURE("集群状态不是<已创建>");
        }

        List<String> clusterHostIps = infoClusterVmMapper.selectByClusterId(clusterId).stream()
                .filter(host->host.getState() == VM_RUNNING)
                .map(InfoClusterVm::getInternalip).collect(Collectors.toList());
        String logId = UUID.randomUUID().toString();
        InfoClusterInfoCollectLog infoClusterInfoCollectLog = new InfoClusterInfoCollectLog();
        infoClusterInfoCollectLog.setClusterId(clusterId);
        infoClusterInfoCollectLog.setState(InfoClusterInfoCollectLog.RUNING);
        if (!infoClusterInfoCollectLogMapper.selectAll(infoClusterInfoCollectLog).isEmpty()) {
            return ResultMsg.FAILURE("已存在正在收集信息的任务");
        }
        infoClusterInfoCollectLog.setClusterName(confCluster.getClusterName());
        infoClusterInfoCollectLog.setCreateTime(new Date());
        infoClusterInfoCollectLog.setId(logId);
        infoClusterInfoCollectLog.setFilePath(String.join(File.separator, confCluster.getLogPath(), clusterId, logId));

        infoClusterInfoCollectLog.setHostIps(StrUtil.join(",",clusterHostIps));
        getLogger().info("开始执行集群信息收集 clusterID: {} clusterName: {} logId:{}", clusterId, confCluster.getClusterName(), logId);
        ResultMsg resultMsg = playBookService.collectClusterInfo(confCluster.getClusterId(),infoClusterInfoCollectLog.getFilePath(), clusterHostIps);
        if (!resultMsg.isSuccess()){
            return resultMsg;
        }
        infoClusterInfoCollectLog.setAnsibleTransactionId(resultMsg.getBizid());
        infoClusterInfoCollectLogMapper.insertSelective(infoClusterInfoCollectLog);
        checkAnsibleTask(infoClusterInfoCollectLog);
        return ResultMsg.SUCCESS("创建任务成功 [任务id:" + logId + "]");
    }
    private final CopyOnWriteArraySet<String> clusterCollectSet =  new CopyOnWriteArraySet<>();
    private final ExecutorService threadPool = new ThreadPoolExecutor(10,100,120, TimeUnit.SECONDS,new ArrayBlockingQueue<>(65536));

    /**
     * 查询ansible任务状态
     * @param infoClusterInfoCollectLog
     */
    private synchronized void checkAnsibleTask(InfoClusterInfoCollectLog infoClusterInfoCollectLog){
        String clusterId = infoClusterInfoCollectLog.getClusterId();
        if (clusterCollectSet.contains(clusterId)){
            getLogger().warn("查询收集集群信息状态重复执行 clusterId: {}",clusterId);
            return ;
        }
        synchronized (this){
            if (clusterCollectSet.contains(clusterId)){
                getLogger().warn("查询收集集群信息状态重复执行 clusterId: {}",clusterId);
                return;
            }
            clusterCollectSet.add(clusterId);
        }
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(confCluster.getRegion(), SSHKeyPair.privateKeyType);

        getLogger().warn("开始查询收集集群信息状态 clusterId: {}",clusterId);
        threadPool.execute(()->{
            long timeout = System.currentTimeMillis() + 1800_000;
            String transactionId = infoClusterInfoCollectLog.getAnsibleTransactionId();
            String clusterName = infoClusterInfoCollectLog.getClusterName();
            String logId = infoClusterInfoCollectLog.getId();
            String hostIps = infoClusterInfoCollectLog.getHostIps();
            int count = 0;
            while (System.currentTimeMillis() < timeout ) {
                try {
                    Thread.sleep(30_000);

                    ResultMsg result = playBookService.queryPlaybookJob(transactionId,sshKeyPair.getSubscriptionId(),sshKeyPair.getKeyVaultResourceName(),sshKeyPair.getKeyVaultResourceId());
                    if (result.getRetcode().equalsIgnoreCase("success")) {
                        InfoClusterInfoCollectLog clusterInfoCollectLog = new InfoClusterInfoCollectLog();
                        clusterInfoCollectLog.setId(logId);
                        clusterInfoCollectLog.setState(InfoClusterInfoCollectLog.DONE);
                        clusterInfoCollectLog.setFinishTime(new Date());
                        infoClusterInfoCollectLogMapper.updateByPrimaryKey(clusterInfoCollectLog);
                        getLogger().info("收集集群信息成功 clusterId:{} clusterName:{}", clusterId, clusterName);
                        break;
                    } else if (result.getRetcode().equalsIgnoreCase("fail")) {
                        if (count < 5) {
                            count++;
                            ResultMsg playbookResult = playBookService.collectClusterInfo(clusterId, infoClusterInfoCollectLog.getFilePath(), StrUtil.split(hostIps, ","));
                            if (playbookResult.isSuccess()) {
                                transactionId = playbookResult.getBizid();
                                InfoClusterInfoCollectLog clusterInfoCollectLog = new InfoClusterInfoCollectLog();
                                clusterInfoCollectLog.setId(logId);
                                clusterInfoCollectLog.setAnsibleTransactionId(transactionId);
                                infoClusterInfoCollectLogMapper.updateByPrimaryKey(clusterInfoCollectLog);
                                getLogger().error("收集集群信息失败开始重试 clusterId:{} clusterName:{} info:{}", clusterId, clusterName, result.getErrorMsg());
                                continue;
                            }
                        }
                        InfoClusterInfoCollectLog clusterInfoCollectLog = new InfoClusterInfoCollectLog();
                        clusterInfoCollectLog.setId(logId);
                        clusterInfoCollectLog.setState(InfoClusterInfoCollectLog.FAIL);
                        clusterInfoCollectLog.setFinishTime(new Date());
                        infoClusterInfoCollectLogMapper.updateByPrimaryKey(clusterInfoCollectLog);
                        getLogger().error("收集集群信息失败5次 clusterId:{} clusterName:{} info:{}", clusterId, clusterName, result.getErrorMsg());
                        break;
                    }

                } catch (InterruptedException e) {
                    getLogger().error("查询任务状态发生异常 id:" + transactionId, e);
                }
            }
        });

    }

    @Override
    public ResultMsg collectClusterInfoList(InfoClusterInfoCollectLog infoClusterInfoCollectLog) {
        try {
            ResultMsg resultMsg = ResultMsg.SUCCESS();
            Long count = infoClusterInfoCollectLogMapper.count(infoClusterInfoCollectLog);
            resultMsg.setTotal(count);
            if (count == null || count == 0L){
                resultMsg.setData(new ArrayList());
                resultMsg.setTotal(0);
                return resultMsg;
            }
            if (infoClusterInfoCollectLog.getPageIndex() != null && infoClusterInfoCollectLog.getPageSize()!= null ){
                infoClusterInfoCollectLog.setPageIndex((infoClusterInfoCollectLog.getPageIndex() - 1) * infoClusterInfoCollectLog.getPageSize());

            }
            List<InfoClusterInfoCollectLog> infoClusterInfoCollectLogs = infoClusterInfoCollectLogMapper.selectAll(infoClusterInfoCollectLog);
            resultMsg.setData(infoClusterInfoCollectLogs);

            return resultMsg;
        }catch (Throwable t){
            return ResultMsg.FAILURE(t.getMessage());
        }

    }

    /**
     * 获取集群需要安装的应用
     *
     * @param clusterId
     * @return
     */
    private List<String> getClusterSevice(String clusterId) {
        List<ConfClusterApp> capps = appMapper.getClusterAppsByClusterId(clusterId);
        List<String> apps = new ArrayList<>();
        capps.stream().forEach(x -> {
            apps.add(x.getAppName().toUpperCase(Locale.ROOT));
        });
        return apps;
    }

    /**
     * 根据实例角色获取机器数据
     *
     * @param clusterId
     * @param vmRole
     * @return
     */
    private List<HostInstance> getVmRoleHosts(String clusterId, String vmRole) {
        List<HostInstance> vmRoleHosts = new ArrayList<>();
        List<InfoClusterVm> vms = ivmService.getRoleVms(clusterId, vmRole);
        if (!CollectionUtils.isEmpty(vms)) {
            List<ConfClusterVm> confvms = confClusterVmMapper.getVmConfsByRole(vmRole, clusterId);
            List<ConfClusterVmDataVolume> vmDataVolumes = dataVolumeMapper.selectByVmConfId(confvms.get(0).getVmConfId());
            List<DiskInfo> diskInfos = new ArrayList<>();
            vmDataVolumes.stream().forEach(item -> {
                DiskInfo diskInfo = new DiskInfo();
                diskInfo.setSizeGB(item.getDataVolumeSize());
                diskInfo.setSkuName(item.getDataVolumeType());
                diskInfo.setCount(item.getCount());
                diskInfos.add(diskInfo);
            });

            vms.stream().forEach(x -> {
                HostInstance hostInstance = new HostInstance();
                hostInstance.setHostName(x.getHostName());
                hostInstance.setHostRole(vmRole);
                hostInstance.setMemoryGB(Convert.toInt(confvms.get(0).getMemory()));
                hostInstance.setvCpu(Convert.toInt(confvms.get(0).getVcpus()));
                hostInstance.setOsDiskSize(confvms.get(0).getOsVolumeSize());
                hostInstance.setDisks(diskInfos);
                vmRoleHosts.add(hostInstance);
            });
        }
        return vmRoleHosts;
    }



    /**
     * 获取数据库连接信息（包含ambari和hive）
     *
     * @param clusterId
     * @return
     */
    private List<DBConnectInfo> getDBConnectInfos(String clusterId) {
        // 数据库这块需要改两块， 一个是hive-site, 一个是hive-env
        // hive-site
        List<DBConnectInfo> dbConnectInfos = new ArrayList<>();
        ConfCluster conf = confClusterMapper.selectByPrimaryKey(clusterId);
        DBConnectInfo dbConnectInfo = new DBConnectInfo();
        dbConnectInfo.setAppType(DBAppType.HIVE_SITE);
        dbConnectInfo.setDriverClassName("com.mysql.jdbc.Driver");
        dbConnectInfo.setConnectionUrl("jdbc:mysql://" + conf.getHiveMetadataDburl() + ":" + conf.getHiveMetadataPort() + "/" + conf.getHiveMetadataDatabase() + "?useSSL=false");
        keyVault keyVault = metaDataItemService.getkeyVault(conf.getRegion());
        dbConnectInfo.setPassword(keyVaultUtil.getSecretVal("hivemetadata-db-pwd-" + conf.getClusterName(),keyVault.getEndpoint()));
        dbConnectInfo.setUserName(keyVaultUtil.getSecretVal("hivemetadata-db-user-" + conf.getClusterName(),keyVault.getEndpoint()));
        dbConnectInfos.add(dbConnectInfo);

        // hive-enb
        DBConnectInfo dbConnectInfo2 = new DBConnectInfo();
        dbConnectInfo2.setAppType(DBAppType.HIVE_ENV);
        dbConnectInfo2.setDbName(conf.getHiveMetadataDatabase());
        dbConnectInfos.add(dbConnectInfo2);

        return dbConnectInfos;
    }


    /**
     * 获取集群配置项信息
     *
     * @param clusterId
     * @return
     */
    private Map<String, Map<String, Object>> getConfigurations(String clusterId) {
        Map<String, Map<String, Object>> configmap = new HashMap<>();
        List<ConfClusterAppsConfig> configs = appsConfigMapper.getAppConfigsByConfigId(clusterId);
        Map<String, List<ConfClusterAppsConfig>> map = configs.stream().collect(Collectors.groupingBy(item -> {
            return item.getAppConfigClassification();
        }));

        map.entrySet().stream().forEach(x -> {
            Map<String, Object> objectMap = new HashMap<>();
            x.getValue().stream().forEach(item -> {
                objectMap.put(item.getConfigItem(), item.getConfigVal());
            });
            configmap.put(x.getKey(), objectMap);
        });

        return configmap;
    }

    /**
     * 获取集群对应的Ambari信息
     *
     * @param clusterId
     * @return
     */
    @Override
    public AmbariInfo getAmbariInfo(String clusterId) {
        InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(clusterId);
        Assert.notNull(infoCluster, "没找到集群信息，clusterId=" + clusterId);
        String baseUri = "http://" + infoCluster.getAmbariHost() + ":8080/api/v1";
        return AmbariInfo.of(baseUri, username, password);
    }

    /**
     * 检测集群的可用性
     *
     * @param clusterId 集群id
     * @return
     */
    @Override
    public ResultMsg checkClusterAvailable(String clusterId) {
        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        ResultMsg msg = new ResultMsg();
        if (null == cluster || null == cluster.getState()) {
            msg.setResult(false);
            msg.setErrorMsg("检查集群是否可用未查询到对象");
            return msg;
        }
        getLogger().info("检查集群是否可用。");
        if (cluster.getState().equals(ConfCluster.DELETING)
                || cluster.getState().equals(ConfCluster.DELETED)) {
            msg.setResult(false);
            msg.setErrorMsg("集群已不可用: " + cluster.getStateStr());
            getLogger().warn("检查集群是否可用 返回数据{}", JSONObject.toJSONString(cluster));
        } else {
            msg.setResult(true);
            msg.setErrorMsg("集群可用");
        }
        return msg;
    }

    /**
     * 获取集群的blueprint
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg getClusterBlueprint(String clusterId) {
        AmbariInfo ambariInfo = getAmbariInfo(clusterId);
        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        ClustersApi clustersApi = new ClustersApi(ambariInfo.getAmbariApiClient());
        String blueprintString = null;
        try {
            String clusterName = Strings.replace(cluster.getClusterName(), "-", "");
            blueprintString = clustersApi.getBlueprintString(clusterName);
        } catch (ApiException e) {
            getLogger().error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        ResultMsg result = new ResultMsg();
        result.setResult(true);
        result.setData(blueprintString);
        return result;
    }

    /**
     * 重置弹性伸缩规则启用状态
     *
     * @param clusterId
     * @param is_valid
     */
    @Override
    public void resetScalingRule(String clusterId, int is_valid) {
        List<ConfGroupElasticScalingRule> confGroupElasticScalingRules = ruleMapper.selectByClusterId(clusterId);
        if (!CollectionUtils.isEmpty(confGroupElasticScalingRules)) {
            for (ConfGroupElasticScalingRule scalingRule : confGroupElasticScalingRules) {
                ConfGroupElasticScalingRule resetScalingRule = new ConfGroupElasticScalingRule();
                resetScalingRule.setEsRuleId(scalingRule.getEsRuleId());
                resetScalingRule.setIsValid(0);
                ruleMapper.updateByPrimaryKeySelective(resetScalingRule);
            }
        }
        elasticScalingRuleChangeNotice();
    }

    /**
     * 弹性伸缩规则变更通知
     */
    private void elasticScalingRuleChangeNotice() {
        try {
            scaleService.metricChange();
        } catch (Exception e) {
            getLogger().error("ClusterServiceImpl.elasticScalingRuleChangeNotice error. e: ", e);
        }
    }

    /**
     * 更新集群配置
     */
    @Override
    public ResultMsg updateClusterConfig(String jsonStr) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData("update cluster config success.");

        Gson gson = new Gson();
        UpdateClusterConfigData updateClusterConfigData = new UpdateClusterConfigData();
        updateClusterConfigData = gson.fromJson(jsonStr, updateClusterConfigData.getClass());

        String clusterId = updateClusterConfigData.getClusterId();
        String groupId = updateClusterConfigData.getGroupId();
        List<ConfigProperties> clusterConfigs = updateClusterConfigData.getClusterConfigs();

        List<BlueprintConfiguration> clusterDefaultConfigs = new ArrayList<>();
        List<InstanceGroupConfiguration> groupConfigs = new ArrayList<>();

        // 获取ambari信息
        AmbariInfo ambariInfo = getAmbariInfo(clusterId);

        for (ConfigProperties clusterConfig : clusterConfigs) {
            String confItemName = clusterConfig.getConfItemName();
            Map<String, Object> confs = clusterConfig.getConfs();
            BlueprintConfiguration blueprintConfiguration = new BlueprintConfiguration();
            blueprintConfiguration.setConfigItemName(confItemName);
            blueprintConfiguration.putProperties(confs);
            clusterDefaultConfigs.add(blueprintConfiguration);
        }

        try {
            if (!StringUtils.isBlank(groupId)) {
                // 配置组配置更新
                InstanceGroupConfiguration configGroupConfiguration = new InstanceGroupConfiguration(groupId);
                configGroupConfiguration.setGroupCfgs(clusterDefaultConfigs);
                groupConfigs.add(configGroupConfiguration);
                ambariService.updateClusterConfig(ambariInfo, clusterId, groupConfigs, null);
            } else {
                // 集群默认配置更新
                ambariService.updateClusterConfig(ambariInfo, clusterId, null, clusterDefaultConfigs);
            }
        } catch (Exception e) {
            getLogger().error("ClusterServiceImpl.updateClusterConfig to ambari error. ambariInfo: {}, clusterId: {}, groupConfigs: {}, clusterDefaultConfigs: {}, e: {}",
                    gson.toJson(ambariInfo), clusterId, gson.toJson(groupConfigs), gson.toJson(clusterDefaultConfigs), e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("updateClusterConfig error.");
        }
        return resultMsg;
    }

    @Override
    public ResultMsg updateLocalClusterConfig(String clusterId) {
        getLogger().info("开始从Ambari获取配置组并更新到集群中：clusterId={}", clusterId);
        // 查询当前Ambari中所有的配置组
        AmbariInfo ambariInfo = getAmbariInfo(clusterId);
        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());

        // 循环重试， 每次查不到结果就休眠5秒，最多查10次。
        QueryConfigGroupsResponse queryConfigGroupsResponse = null;
        Integer loopLimit = 0;
        while (loopLimit < 10) {
            loopLimit++;
            queryConfigGroupsResponse = api.queryConfigGroups(getSdpClusterNameforAmbari(clusterId), ConfigGroupField.GROUP_NAME, "*");
            getLogger().info("查询配置返回:clusterId={},res={}", clusterId, JSONUtil.toJsonStr(queryConfigGroupsResponse));
            if (Objects.isNull(queryConfigGroupsResponse) || CollUtil.isEmpty(queryConfigGroupsResponse.getItems())) {
                getLogger().info("Ambari中不存在配置组，等待一会儿后重试查询，clusterId={}", clusterId);
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    getLogger().error(e.getMessage(), e);
                }
            } else {
                break;
            }
        }

        if (Objects.isNull(queryConfigGroupsResponse) || CollUtil.isEmpty(queryConfigGroupsResponse.getItems())) {
            getLogger().info("Ambari中不存在配置组，SDP不需要处理，clusterId={}", clusterId);
            return ResultMsg.SUCCESS();
        }

        // 查询集群所有的实例组
        List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectByClusterId(clusterId);

        // 遍历Ambari中的配置组，找到对应的实例组，更新到数据库中
        for (ConfigGroupWrapper item : queryConfigGroupsResponse.getItems()) {
            ConfigGroup ambariConfigGroup = item.getConfigGroup();
            // 由于一个实例组可能会拆分多个配置组, 所以通过配置组中的Host来反向查实例组
            HostRole hostRole = CollectionUtil.get(ambariConfigGroup.getHosts(), 0);
            if (Objects.isNull(hostRole)) {
                getLogger().error("从Ambari查询返回的配置组中不包含主机名,跳过此配置组. 配置组名=" + ambariConfigGroup.getGroupName()
                        + " " + ambariConfigGroup.getTag());
                continue;
            }

            Optional<ConfClusterHostGroup> optHostGroup = getHostGroupByHost(clusterId, hostRole.getHostName());

            if (!optHostGroup.isPresent()) {
                getLogger().error("根据HostName未找到实例组：clusterId={}, 配置组名={}, hostName={}",
                        clusterId, ambariConfigGroup.getGroupName(), hostRole.getHostName());
                continue;
            }

            // TODO: 检查是否已经保存的Ambari配置组，唯一标识为：ambariId
            ConfClusterHostGroup sdpHostGroup = optHostGroup.get();
            String groupId = sdpHostGroup.getGroupId();

            InfoAmbariConfigGroup toSaveConfigGroup = new InfoAmbariConfigGroup();
            toSaveConfigGroup.setClusterId(clusterId);
            toSaveConfigGroup.setGroupId(groupId);
            toSaveConfigGroup.setSdpGroupName(sdpHostGroup.getGroupName());
            toSaveConfigGroup.setAmbariId(ambariConfigGroup.getId());
            // ambari不返回ServiceName，直接使用Tag
            toSaveConfigGroup.setAmbariServiceName(ambariConfigGroup.getTag());
            toSaveConfigGroup.setAmbariGroupName(ambariConfigGroup.getGroupName());
            toSaveConfigGroup.setAmbariTag(ambariConfigGroup.getTag());
            toSaveConfigGroup.setAmbariClusterName(ambariConfigGroup.getClusterName());
            toSaveConfigGroup.setAmbariDescription(ambariConfigGroup.getDescription());
            toSaveConfigGroup.setState(InfoAmbariConfigGroup.STATE_RUNNING);
            toSaveConfigGroup.setCreatedTime(new Date());
            toSaveConfigGroup.setConfId(UUID.randomUUID().toString());
            getLogger().info("ambariConfigGroupMapper insert group:{}", ambariConfigGroup);
            ambariConfigGroupMapper.insertSelective(toSaveConfigGroup);
        }
        return ResultMsg.SUCCESS();
    }

    /**
     * 根据集群ID和主机名,查询该主机所属的实例组
     * @param clusterId
     * @param hostName
     * @return
     */
    private Optional<ConfClusterHostGroup> getHostGroupByHost(String clusterId, String hostName) {
        InfoClusterVm infoClusterVm = infoClusterVmMapper.selectVMByClusterIdAndHostName(clusterId, hostName);
        if (Objects.isNull(infoClusterVm)) {
            return Optional.ofNullable(null);
        }
        return Optional.ofNullable(confClusterHostGroupMapper.selectByPrimaryKey(infoClusterVm.getGroupId()));
    }

    /**
     * 检查执行中状态且超时的任务
     *
     */
    @Override
    @Scheduled(initialDelay = 10000L, fixedDelay =30000L)
    public void checkTimeoutActivity() {
        getLogger().info("=====开始巡检执行超时的任务步骤....");
        try {
            // cluster_id, plan_id,activity_log_id
            List<HashMap<String, Object>> activtitys =
                    planActivityLogMapper.getRunningStateAndTimeoutActivity(Convert.toInt(activityTimeout, 1800));
            if (activtitys == null || activtitys.size() == 0) {
                getLogger().info("=====没有执行超时的任务步骤,退出");
                return;
            }
            getLogger().info("巡检到执行超时的任务步骤：{}", activtitys);
            activtitys.stream().forEach(x -> {
                String activity_log_id = x.get("activity_log_id").toString();
                InfoClusterOperationPlanActivityLogWithBLOBs current_log = planActivityLogMapper.selectByPrimaryKey(activity_log_id);

                Integer ckrs = checkIsCompleteActivity(current_log);
                getLogger().info("超时步骤检查,获取到check结果：logId={}, 结果:", activity_log_id, ckrs);
                if (ckrs.equals(0)){
                    // 啥也不干
                }
                if (ckrs.equals(1)){
                    // 发送重试消息
                    planExecService.autoRetryActivityForTimeOut(current_log.getActivityLogId());
                }
                if (ckrs.equals(2)){
                    //按照超时处理
                    current_log.setState(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT);
                    planExecService.complateActivity(current_log);
                }

            });
            getLogger().info("=====巡检执行超时的任务步骤结束");
        }catch (Exception e) {
            getLogger().error("巡检异常任务步骤异常，", e);
        }
    }


    /**
     * 检查当前步骤是否可以以TimeOut状态完成<br/>
     *  检查逻辑：没有达到重试次数上限且距离上次重试时间大于间隔时间 返回 1<br/>
     *          没有达到重试次数上限且距离上次重试时间小于间隔时间的 返回 0<br/>
     *          没有进行过重试的返回 1<br/>
     *          达到重试次数上限且距离上次重试时间大于间隔时间的 返回 2<br/>
     *          达到重试次数上限且距离上次重试时间小于间隔时间的 返回 0<br/>
     *
     * @param current_log
     * @return 1 需要发送重试消息，2以TimeOut状态完成，0 间隔时间内不处理
     */
    private Integer checkIsCompleteActivity(InfoClusterOperationPlanActivityLogWithBLOBs current_log){
        Long nowc= System.currentTimeMillis()/1000;
        try {
            getLogger().info("巡检超时步骤, current_log:"+current_log.toString());
            //region 没有进行过重试, 返回 1
            if (current_log.getRetryCount() == null) {
                getLogger().info("超时步骤没有进行过重试, 需要进行重试: {}", current_log.getActivityLogId());
                current_log.setRetryCount(1);
                current_log.setLastRetryTime(new Date());
                planActivityLogMapper.updateByPrimaryKeySelective(current_log);
                return 1;
            }
            //endregion 没有进行过重试
            if (current_log.getRetryCount() < sdpCheckActivityTimeoutRetry) {
                //region 没有达到重试次数
                //getLogger().info("重试次数小于最大重试次数。");
                Long last = current_log.getLastRetryTime().getTime() / 1000;

                // 大于时间间隔
                if (nowc - last > sdpScaleInDecommissionRetryDuration) {
                    getLogger().info("超时步骤重试次数小于最大重试次数，大于时间间隔,需要进行重试: {}", current_log.getActivityLogId());
                    current_log.setRetryCount(current_log.getRetryCount() + 1);
                    current_log.setLastRetryTime(new Date());
                    planActivityLogMapper.updateByPrimaryKeySelective(current_log);
                    return 1;
                } else {
                    //没有达到重试次数上限且距离上次重试时间小于间隔时间的 返回
                    getLogger().info("超时步骤重试次数小于最大重试次数，且距离上次重试时间小于间隔时间, 不需要重试: {}", current_log.getActivityLogId());
                    return 0;
                }
                //endregion
            } else {
                //region 达到重试次数
                Long last = current_log.getLastRetryTime().getTime() / 1000;
                // 大于时间间隔
                if (nowc - last > sdpScaleInDecommissionRetryDuration) {
                    getLogger().info("超时步骤达到最大重试次数,设置步骤状态为执行超时: {}", current_log.getActivityLogId());
                    return 2;
                } else {
                    //达到重试次数上限且距离上次重试时间小于间隔时间的 返回
                    getLogger().info("超时步骤达到重试次数上限且距离上次重试时间小于间隔时间,不进行处理: {}", current_log.getActivityLogId());
                    return 0;
                }
                //endregion
            }
        }catch (Exception e){
            getLogger().error("超时步骤检查出错, checkIsCompleteActivity，异常:",e);
            return 2;
        }

    }

    private void setAmbariDebugState(ApiClient client) {
        if (Objects.isNull(client)) {
            return;
        }
        client.setDebugging(Objects.equals(this.debug, "1"));
    }

}
