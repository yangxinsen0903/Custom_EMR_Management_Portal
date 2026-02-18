package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.consts.JobNameConstant;
import com.sunbox.sdpcompose.manager.AzureServiceManager;
import com.sunbox.sdpcompose.manager.InfoClusterVmIndexManager;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.model.azure.fleet.request.*;
import com.sunbox.sdpcompose.model.azure.request.AzureAppendVMsRequest;
import com.sunbox.sdpcompose.model.azure.request.AzureSpotProfile;
import com.sunbox.sdpcompose.model.azure.request.AzureVMGroupsRequest;
import com.sunbox.sdpcompose.model.azure.request.AzureVMSpecRequest;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.service.scale.strategy.ComposeStrategyFactory;
import com.sunbox.service.scale.strategy.ScaleTaskStrategy;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : [niyang]
 * @className : ScaleOutServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/12 10:55 PM]
 */
@Service("Scaling")
public class ScalingServiceImpl implements IScalingService, BaseCommonInterFace {
    @Autowired
    private ConfClusterAppMapper confClusterAppMapper;

    @Autowired
    private IPlanExecService planExecService;

    @Autowired
    private ConfScalingTaskMapper taskMapper;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private ConfClusterMapper clusterMapper;

    @Autowired
    private ConfClusterTagMapper tagMapper;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private ConfScalingTaskVmMapper taskVmMapper;

    @Autowired
    private ConfScalingTaskVmMapper confScalingTaskVmMapper;

    @Autowired
    private ConfScalingVmDataVolMapper vmDataVolMapper;

    @Autowired
    private InfoClusterVmJobMapper vmJobMapper;

    @Autowired
    private ConfClusterVmMapper confClusterVmMapper;

    @Autowired
    private ConfClusterVmDataVolumeMapper dataVolumeMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfScalingTaskMapper confScalingTaskMapper;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    private ConfClusterAppMapper clusterAppMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmIndexMapper infoClusterVmIndexMapper;

    @Autowired
    private InfoClusterVmIndexHistoryMapper infoClusterVmIndexHistoryMapper;

    @Autowired
    private ThreadLocal<Map<String, Object>> threadLocal;

    @Autowired
    private ComposeStrategyFactory composeStrategyFactory;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper activityLogMapper;

    @Autowired
    private ConfScalingVmMapper confScalingVmMapper;

    @Autowired
    private IAzureFleetService azureFleetService;

    @Value("${vm.hostnamesuffix}")
    private String hostnamesuffix;

    @Value("${vm.username}")
    private String vmusername;

    @Value("${shein.api.resize.isgraceful:1}")
    private String resizeIsGraceful;

    @Value("${shein.api.resize.waitingtime:120}")
    private String scaleinWaitingtime;

    @Value("${part.max.size:4096}")
    private Integer maxPartSize;

    @Value("${sdp.scaleout.apps.start.activityid:e3096ac0-954e-11ed-922d-6045bdc792d8}")
    private String startActivityId;

    @Value("${sdp.scaleout.apps.checkstart.activityid:e3096afc-954e-11ed-922d-6045bdc792d8}")
    private String CheckStartActivityId;

    @Value("${sdp.scaleout.vm.provision.retrytimes:5}")
    private Integer sdpScaleOutVMProvisionRetryTimes;

    @Value("${sdp.scaleout.vm.provision.retrywaittimes:30}")
    private Integer sdpScaleOutVMProvisionRetryWaitTimes;

    @Value("${core.scalein.max.count:3}")
    private Integer scaleInMaxCount;

    @Value("${sdp.rm.azurefleet.switch:1}")
    private Integer azureFleetSwitch;

    /**
     * 扩容申请虚拟机
     *
     * @param messageParam
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

            String taskid = param.getString(ComposeConstant.Task_ID);

            //region 0.获取伸缩任务并更新任务状态
            ConfScalingTask task = taskMapper.selectByPrimaryKey(taskid);
            getLogger().info("获取伸缩任务：" + task.toString());
            //endregion 获取伸缩任务并更新任务状态

            // region 1. 获取要创建的vm
            Map<String, Object> res = null;
            Exception buildScalingAzureVmsRequestException = null;
            StringBuilder createSummary = new StringBuilder();
            try {
                if(azureFleetSwitch.equals(1)){
                    res = buildScaleOutAzureFleetVmsRequest(task,activityLogId);
                    createSummary = extractCreateVmsSummary(task, (AzureFleetVmsRequest)res.get("azure"));
                }else {
                    res = buildScalingAzureVmsRequest(task, activityLogId);
                }
            } catch (Exception e) {
                buildScalingAzureVmsRequestException = e;
            }

            if (res != null) {

                InfoClusterVmJob vmJob = (InfoClusterVmJob) res.get("job");

                // region 2. 调用接口发起vm申请
                Map<String, Object> azureApiLogs = new HashMap<>();
                azureApiLogs.put("clusterId", task.getClusterId());
                azureApiLogs.put("activityLogId", activityLogId);
                azureApiLogs.put("planId", activityLog.getPlanId());
                threadLocal.set(azureApiLogs);

                ResultMsg msg;
                if (azureFleetSwitch.equals(1)){
                    AzureFleetAppendVMsRequest azureFleetAppendVMsRequest
                            = (AzureFleetAppendVMsRequest) res.get("azure");
                    msg = azureFleetService.createAppendVms(azureFleetAppendVMsRequest,res.get("subscriptionId").toString());
                }else {
                    AzureAppendVMsRequest azureVmsRequest = (AzureAppendVMsRequest) res.get("azure");
                    msg = appendVirtualMachines(azureVmsRequest, task.getClusterId());
                }

                getLogger().info("res:" + msg.toString());

                if (msg.getResult()) {
                    JSONObject data = (JSONObject) msg.getData();
                    String jobid = data.getString("id");
                    param.put(JobNameConstant.Cluster_VM, jobid);
                    vmJob.setJobId(jobid);
                    vmJobMapper.updateByPrimaryKeySelective(vmJob);

                    if (!StringUtils.isEmpty(jobid)) {
                        planExecService.sendNextActivityMsg(activityLogId, param);
                        activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        activityLog.setLogs(createSummary + msg.getData().toString());
                    } else {
                        getLogger().error("not found createvm jobid.activityLogId:" + activityLogId);
                        activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                        activityLog.setLogs(createSummary + "not found createvm jobid.activityLogId:\n" + activityLogId + "\n"
                                + msg.getData().toString());
                    }
                } else {
                    activityLog.setLogs(createSummary + msg.getErrorMsg());
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    resultMsg.setResult(false);
                }
                planExecService.complateActivity(activityLog);

                msg.setResult(true);
            } else {
                if (buildScalingAzureVmsRequestException != null) {
                    activityLog.setLogs(createSummary + "构建Azure虚拟机参数失败:" + buildScalingAzureVmsRequestException.getMessage());
                } else {
                    if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_spot)) {
                        activityLog.setLogs(createSummary + "竞价失败,出价低于当前报价");
                    } else {
                        activityLog.setLogs(createSummary + "构建Azure申请虚拟机参数失败");
                    }
                }
                activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                planExecService.complateActivity(activityLog);
                resultMsg.setResult(false);
            }
        } catch (Exception e) {
            getLogger().error("createVM exception:", e);
            resultMsg.setResult(false);
            if (null != activityLog) {
                activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                activityLog.setLogs(e.getMessage());
                planExecService.complateActivity(activityLog);
            }
        }
        //endregion
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
     * @param request
     * @return
     */
    private StringBuilder extractCreateVmsSummary(ConfScalingTask task, AzureFleetVmsRequest request) {
        StringBuilder summary = new StringBuilder();
        try {
            summary.append("申请数:").append(task.getScalingCount()).append(";")
                    .append("申请前:").append(task.getBeforeScalingCount()).append(";")
                    .append("申请后:").append(task.getAfterScalingCount()).append(";");

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
     * 扩容申请vm（with 重试）
     *
     * @param azureVmsRequest
     * @return
     */
    private ResultMsg appendVirtualMachines(AzureAppendVMsRequest azureVmsRequest, String clusterId) {
        ResultMsg msg = new ResultMsg();

        Integer i = 0;
        while (true) {
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(clusterId);
            if (!confCluster.getState().equals(2)) {
                msg.setResult(false);
                msg.setErrorMsg("集群状态不为2。");
                return msg;
            }
            msg = azureService.appendVirtualMachines(azureVmsRequest);
            if (msg.getResult()) {
                return msg;
            }
            i++;
            if (i > sdpScaleOutVMProvisionRetryTimes) {
                return msg;
            }
            ThreadUtil.sleep(1000 * sdpScaleOutVMProvisionRetryWaitTimes);
        }
    }


    /**
     * 创建缩容任务
     *
     * @param task
     * @return
     */
    @Override
    public ResultMsg createScaleinTask(ConfScalingTask task) {
        if (task.getOperatiionType() != null
                && (task.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)
                || task.getOperatiionType().equals(ConfScalingTask.Operation_type_Scaling)
                || task.getOperatiionType().equals(ConfScalingTask.Operation_type_delete_group)
                || task.getOperatiionType().equals(ConfScalingTask.Operation_type_delete_Task_Vm))) {
            //region spot
            ResultMsg resultMsg = new ResultMsg();
            getLogger().info("scaling task:" + task.toString());

            if (StringUtils.isEmpty(task.getVmRole()) || StringUtils.equals(task.getVmRole(), "null")) {
                if (StringUtils.isNotEmpty(task.getClusterId())
                        && StringUtils.isNotEmpty(task.getGroupName())) {
                    ConfClusterHostGroup confClusterHostGroup = this.confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
                    if (confClusterHostGroup == null) {
                        resultMsg.setResult(false);
                        resultMsg.setErrorMsg("实例组" + task.getGroupName() + "未找到");
                        return resultMsg;
                    } else {
                        task.setVmRole(confClusterHostGroup.getVmRole());
                    }
                } else {
                    resultMsg.setResult(false);
                    resultMsg.setErrorMsg("集群id和实例组名称参数未找到。");
                    return resultMsg;
                }
            }

            if (!task.getVmRole().equalsIgnoreCase("core")
                    && !task.getVmRole().equalsIgnoreCase("task")) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("只允许core和task的实例组进行扩缩容。");
                return resultMsg;
            }

            // 集群同一时刻只能启动一个伸缩任务
            String lockKey = "scaling_" + task.getClusterId() + "_" + task.getVmRole() + "_" + task.getGroupName();
            boolean lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 200, 300);
            if (!lock) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("同一时刻只能启动一个缩容任务:" + task.getClusterId());
                getLogger().error("同一时刻只能启动一个缩容任务:" + task.getClusterId());
                return resultMsg;
            }

            try {
                ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());
                if (confCluster == null) {
                    getLogger().error("not found confCluster,clusterId:{}", task.getClusterId());
                    resultMsg.setResult(false);
                    resultMsg.setErrorMsg("集群信息未找到");
                    return resultMsg;
                }

                if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
                    getLogger().error("confCluster state is invalid,clusterId:{},state:{}",
                            task.getClusterId(),
                            confCluster.getState());
                    resultMsg.setResult(false);
                    resultMsg.setErrorMsg("集群状态不正确");
                    return resultMsg;
                }


                // region 集群同一时刻只能启动一个扩缩容任务check
                ResultMsg runningTask = checkCanCreateScaleTask(task);
                getLogger().info("是否存在运行中的任务:{}", JSONUtil.toJsonStr(runningTask));
                if (runningTask.getResult()) {
                    //手动任务需要加入到队列中,弹性的直接返回失败
                    ConfScalingTask currentTask = (ConfScalingTask) runningTask.getData();
                    if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                        resultMsg.setResult(false);
                        String errorMsg = StrUtil.format("已存在运行中的伸缩任务:{}", currentTask.getTaskId());
                        resultMsg.setErrorMsg(errorMsg);
                        getLogger().error(errorMsg);
                        return resultMsg;
                    }
                }
                // endregion

//                ConfScalingTask lastFailedScaleOutTask = confScalingTaskMapper.findLastByScalingTypeAndState(confCluster.getClusterId(),
//                                task.getVmRole(),
//                                task.getGroupName(),
//                                ConfScalingTask.ScaleType_OUT,
//                                ConfScalingTask.SCALINGTASK_Failed);
//                if (lastFailedScaleOutTask != null) {
//                    Integer runningVmSize = this.infoClusterVmMapper.countByScaleOutTaskIdAndState(lastFailedScaleOutTask.getClusterId(),
//                            lastFailedScaleOutTask.getTaskId(),
//                            InfoClusterVm.VM_RUNNING);
//                    if (runningVmSize > 0) {
//                        getLogger().error("can not create scale in task because lastFinishedTask is failed and has running vms:{},lastFinishedTask:{}",
//                                runningVmSize,
//                                lastFailedScaleOutTask);
//                        resultMsg.setResult(false);
//                        resultMsg.setErrorMsg(lastFailedScaleOutTask.getTaskId() +
//                                "的扩容任务执行失败，需要先销毁其中申请的实例，再提交新的缩容");
//                        return resultMsg;
//                    }
//                }

                // region 获取集群配置信息
                List<ConfClusterVm> confClusterVms
                        = confClusterVmMapper.getVmConfsByGroupName(task.getGroupName(), task.getClusterId());

                if (confClusterVms.size() != 1) {
                    resultMsg.setResult(false);
                    return resultMsg;
                }


                ResultMsg adjustResultMsg = adjustScaleInCount(task);
                if (!adjustResultMsg.getResult())
                    return adjustResultMsg;

                task.setState(ConfScalingTask.SCALINGTASK_Create);
                // endregion

                // region 获取scaling 可用的vms,按照vmname desc的顺序,支持重复执行
                List<InfoClusterVm> infoClusterVmList = new ArrayList<>();
                if (StrUtil.isNotEmpty(task.getScaleoutTaskId())) {
                    getLogger().info("createScaleinTask scale out task id:{}", task.getScaleoutTaskId());
                    InfoClusterVm queryParams = new InfoClusterVm();
                    queryParams.setScaleoutTaskId(task.getScaleoutTaskId());
                    queryParams.setClusterId(task.getClusterId());
                    queryParams.setState(InfoClusterVm.VM_RUNNING);
                    infoClusterVmList = infoClusterVmMapper.selectVmsByTaskId(queryParams);
                } else {
                    // 先查询是否有历史处理的数据
                    infoClusterVmList =
                            infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(confCluster.getClusterId(), task.getTaskId());

                    if (infoClusterVmList == null || infoClusterVmList.size() == 0) {
                        infoClusterVmList =
                                infoClusterVmMapper.selectVmsByGroupNameForScaleIn(confCluster.getClusterId(),
                                        task.getGroupName(),
                                        task.getScalingCount());
                        if (CollUtil.isEmpty(infoClusterVmList)) {
                            resultMsg.setResult(false);
                            resultMsg.setErrorMsg("有实例正在缩容,无可用实例进行缩容");
                            getLogger().error("create scale task error,info cluster vm in scale in task,clusterId={},groupName={}", task.getClusterId(), task.getGroupName());
                            return resultMsg;
                        }
                    }

                }
                // endregion

                // region 保存数据
                task.setCreateTime(new Date());
                taskMapper.insert(task);

                // endregion

                // region 更新infoclustervm表，写入scaleintaskid

                List<String> vmnames = new ArrayList<>();
                infoClusterVmList.stream().forEach(vm -> {
                    vmnames.add(vm.getVmName());
                });

                infoClusterVmMapper.updateScaleinTaskIdByClusterIdAndVmNames(
                        confCluster.getClusterId(),
                        vmnames,
                        task.getTaskId());

                // endregion

                addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleIn, task);
                resultMsg.setResult(true);
                resultMsg.setData(task.getTaskId());
            } catch (Exception e) {
                getLogger().error("createScaleInTask,e ", e);
                resultMsg.setResult(false);
                resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    if (lock) {
                        redisLock.unlock(lockKey);
                    }
                } catch (Exception e) {
                    getLogger().error("createScaleInTask, 释放锁异常", e);
                }
            }

            return resultMsg;
            //endregion
        } else {
            return createScaleInTaskAndSplit(task);
        }
    }

    private boolean isHbaseCluster(String clusterId) {
        boolean isHbaseCluster = false;
        List<ConfClusterApp> apps = confClusterAppMapper.getClusterAppsByClusterId(clusterId);
        for (ConfClusterApp app : apps) {
            if (app.getAppName() != null && "HBASE".equalsIgnoreCase(app.getAppName())) {
                isHbaseCluster = true;
                break;
            }
        }
        return isHbaseCluster;
    }

    public ResultMsg createScaleInTaskAndSplit(ConfScalingTask task) {
        ResultMsg resultMsg = new ResultMsg();
        getLogger().info("scaling task:" + task.toString());

        if (!task.getVmRole().equalsIgnoreCase("core")
                && !task.getVmRole().equalsIgnoreCase("task")) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("只允许core和task的实例组进行扩缩容。");
            return resultMsg;
        }

        if (task.getVmRole().equalsIgnoreCase("core")) {
            if (task.getExpectCount() != null && task.getExpectCount() < this.scaleInMaxCount) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("core实例组无法缩容到" + this.scaleInMaxCount + "以下");
                return resultMsg;
            }
        }

        // 集群同一时刻只能启动一个伸缩任务
        String lockkey = "scaling_" + task.getClusterId() + "_" + task.getVmRole() + "_" + task.getGroupName();
        boolean lock = false;
        try {
            lock = redisLock.tryLock(lockkey, TimeUnit.SECONDS, 200, 300);
            if (!lock) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("同一时刻只能启动一个缩容任务:" + task.getClusterId());
                getLogger().error("同一时刻只能启动一个缩容任务:" + task.getClusterId());
                return resultMsg;
            }


            // region 集群同一时刻只能启动一个扩缩容任务check
            ResultMsg runningTask = checkCanCreateScaleTask(task);
            getLogger().info("是否存在运行中的任务:{}", JSONUtil.toJsonStr(runningTask));
            if (runningTask.getResult()) {
                //手动任务需要加入到队列中,弹性的直接返回失败
                ConfScalingTask currentTask = (ConfScalingTask) runningTask.getData();
                if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                    resultMsg.setResult(false);
                    String errorMsg = StrUtil.format("已存在运行中的伸缩任务:{}", currentTask.getTaskId());
                    resultMsg.setErrorMsg(errorMsg);
                    getLogger().error(errorMsg);
                    return resultMsg;
                }
            }
            // endregion

            // region 获取集群配置信息
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());

            List<ConfClusterVm> confClusterVms
                    = confClusterVmMapper.getVmConfsByGroupName(task.getGroupName(), task.getClusterId());

            if (confClusterVms.size() != 1) {
                resultMsg.setResult(false);
                return resultMsg;
            }


            ResultMsg adjustResultMsg = adjustScaleInCount(task);
            if (!adjustResultMsg.getResult())
                return adjustResultMsg;

            task.setState(ConfScalingTask.SCALINGTASK_Create);
            // endregion

            // region 获取scaling 可用的vms,按照vmname desc的顺序,支持重复执行
            List<InfoClusterVm> infoClusterVmList = new ArrayList<>();
            if (StrUtil.isNotEmpty(task.getScaleoutTaskId())) {
                InfoClusterVm queryParams = new InfoClusterVm();
                queryParams.setScaleoutTaskId(task.getScaleoutTaskId());
                queryParams.setClusterId(task.getClusterId());
                queryParams.setState(InfoClusterVm.VM_RUNNING);
                infoClusterVmList = infoClusterVmMapper.selectVmsByTaskId(queryParams);
            } else {
                // 先查询是否有历史处理的数据
                infoClusterVmList =
                        infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(confCluster.getClusterId(), task.getTaskId());

                if (infoClusterVmList == null || infoClusterVmList.size() == 0) {
                    infoClusterVmList =
                            infoClusterVmMapper.selectVmsByGroupNameForScaleIn(confCluster.getClusterId(),
                                    task.getGroupName(),
                                    task.getScalingCount());
                    if (CollUtil.isEmpty(infoClusterVmList)) {
                        resultMsg.setResult(false);
                        resultMsg.setErrorMsg("有实例正在缩容,无可用实例进行缩容");
                        getLogger().error("create scale task error,info cluster vm in scale in task,clusterId={},groupName={}", task.getClusterId(), task.getGroupName());
                        return resultMsg;
                    }
                }

            }
            // endregion

            int taskCountPreTask = 0;
            boolean descendingSplit = false;
            if (task.getVmRole().equalsIgnoreCase("CORE")) {
                if (isHbaseCluster(task.getClusterId())) {
                    taskCountPreTask = 1;
                } else {
                    descendingSplit = true;
                    taskCountPreTask = 5;
                }
            } else if (task.getVmRole().equalsIgnoreCase("TASK")) {
                taskCountPreTask = 50;
            }

            int beforeScalingCount = task.getBeforeScalingCount();
            int fromSubListIndex = 0;
            int toSubListIndex = 0;
            int splitLoopIndex = 0;
            while (beforeScalingCount > task.getAfterScalingCount()) {
                if (descendingSplit) {
                    int oneTenthCount = beforeScalingCount / 10;
                    if (oneTenthCount < 1) {
                        oneTenthCount = 1;
                    }

                    taskCountPreTask = oneTenthCount;
                }

                int afterScalingCount = beforeScalingCount - taskCountPreTask;
                if (afterScalingCount < task.getAfterScalingCount()) {
                    afterScalingCount = task.getAfterScalingCount();
                }

                if (beforeScalingCount == afterScalingCount) {
                    continue;
                }

                toSubListIndex += taskCountPreTask;
                if (fromSubListIndex >= infoClusterVmList.size()) {
                    continue;
                }

                if (toSubListIndex >= infoClusterVmList.size()) {
                    toSubListIndex = infoClusterVmList.size();
                }

                if (beforeScalingCount - afterScalingCount < toSubListIndex - fromSubListIndex) {
                    toSubListIndex -= ((toSubListIndex - fromSubListIndex) - (beforeScalingCount - afterScalingCount));
                }

                List<InfoClusterVm> subInfoClusterVms = infoClusterVmList.subList(fromSubListIndex, toSubListIndex);

                List<String> vmnames = new ArrayList<>();
                subInfoClusterVms.stream().forEach(vm -> {
                    vmnames.add(vm.getVmName());
                });
                getLogger().info("infoClusterVmList subList taskCountPreTask:{},from:{},to:{},size:{},vmnames:{}",
                        taskCountPreTask,
                        fromSubListIndex,
                        toSubListIndex,
                        infoClusterVmList.size(),
                        vmnames);

                int actualScalingCount = beforeScalingCount - afterScalingCount;
                if (actualScalingCount > vmnames.size()) {
                    actualScalingCount = vmnames.size();
                }

                ConfScalingTask partTask = new ConfScalingTask();
                BeanUtils.copyProperties(task, partTask);
                partTask.setTaskId(UUID.randomUUID().toString());
                partTask.setBeforeScalingCount(beforeScalingCount);
                partTask.setAfterScalingCount(beforeScalingCount - actualScalingCount);
                partTask.setExpectCount(partTask.getAfterScalingCount());
                partTask.setScalingCount(actualScalingCount);
                partTask.setCreateTime(DateUtils.addMilliseconds(new Date(), ++splitLoopIndex * 100));
                partTask.setState(ConfScalingTask.SCALINGTASK_Create);

                // region 保存数据
                taskMapper.insert(partTask);
                getLogger().info("insert part task:{}", partTask);

                // endregion

                // region 更新infoclustervm表，写入scaleintaskid

                infoClusterVmMapper.updateScaleinTaskIdByClusterIdAndVmNames(
                        confCluster.getClusterId(),
                        vmnames,
                        partTask.getTaskId());

                // endregion

                addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleIn, partTask);

                beforeScalingCount -= taskCountPreTask;
                fromSubListIndex += taskCountPreTask;
            }
            resultMsg.setResult(true);
            resultMsg.setData(task.getTaskId());
        } catch (Exception e) {
            getLogger().error("createScaleInTask,e ", e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if (lock) {
                    redisLock.unlock(lockkey);
                }
            } catch (Exception e) {
                getLogger().error("createScaleInTask, 释放锁异常", e);
            }
        }

        return resultMsg;
    }

    private boolean hasRunningVmNode15(String clusterId, String groupName) {
        Integer integer = this.infoClusterVmMapper.countByClusterIdAndGroupNameAndState(clusterId, groupName, InfoClusterVm.VM_RUNNING);
        if (integer >= 15) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ResultMsg createScaleInTaskForDeleteTaskVm(String clusterId,
                                                      String vmRole,
                                                      String groupName,
                                                      String scaleOutTaskId,
                                                      List<String> vmNames,
                                                      Date createdTime) {
        getLogger().info("begin createScaleInTaskForDeleteVm,clusterId:{},vmRole:{},groupName:{},scaleOutTaskId:{},vmNames:{},createdTime:{}",
                clusterId,
                vmRole,
                groupName,
                scaleOutTaskId,
                vmNames,
                createdTime);
        ResultMsg resultMsg = new ResultMsg();

        if (!vmRole.equalsIgnoreCase("core")
                && !vmRole.equalsIgnoreCase("task")) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("只允许core和task的实例组进行缩容。");
            return resultMsg;
        }

        // 集群同一时刻只能启动一个伸缩任务
        String lockKey = "scaling_" + clusterId + "_" + vmRole + "_" + groupName;
        boolean lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 200, 300);
        if (!lock) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("同一时刻只能启动一个缩容任务:" + clusterId);
            getLogger().error("同一时刻只能启动一个缩容任务:" + clusterId);
            return resultMsg;
        }

        try {

            //region 判断节点是否已经加入集群，若没有加入直接清理机器的流程
            InfoClusterOperationPlan plan = planMapper.getPlanByScaleOutTaskId(clusterId, scaleOutTaskId);
            if (plan == null) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("未查询到扩容任务。");
                return resultMsg;
            }

            List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogs = activityLogMapper.getAllActivity(plan.getPlanId());

            AtomicBoolean delvmFlag = new AtomicBoolean(false);

            activityLogs.stream().forEach(x -> {

                //明确显示启动失败
                if (x.getActivityId().equalsIgnoreCase(CheckStartActivityId) &&
                        x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED)) {
                    delvmFlag.set(true);
                    return;
                }

                //启动指令未执行或失败
                if (x.getActivityId().equalsIgnoreCase(startActivityId) &&
                        (x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_PLANING)
                                || x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED))) {
                    delvmFlag.set(true);
                    return;
                }
            });
            //endregion

            // 直接删除vm的流程
            if (delvmFlag.get()) {
                //加入vm_reject
                List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(clusterId, scaleOutTaskId);
                ivmService.saveRejectVMs(vms, activityLogs.get(0).getActivityLogId());
                return ivmService.startClearVMPlan(clusterId, plan.getPlanId());
            }

            //region 构建缩容任务
            ConfScalingTask task = new ConfScalingTask();
            task.setTaskId(UUID.randomUUID().toString());
            task.setOperatiionType(ConfScalingTask.Operation_type_delete_Task_Vm);
            task.setExpectCount(null);
            task.setScalingCount(vmNames.size());
            task.setGroupName(groupName);
            task.setVmRole(vmRole);
            task.setClusterId(clusterId);
            task.setIsGracefulScalein(1);
            task.setScaleinWaitingtime(60);
            task.setScalingType(ConfScalingTask.ScaleType_IN);
            task.setState(ConfScalingTask.SCALINGTASK_Create);

            getLogger().info("scaling task:" + task.toString());
            //endregion

            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());
            if (confCluster == null) {
                getLogger().error("not found confCluster,clusterId:{}", task.getClusterId());
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群信息未找到");
                return resultMsg;
            }

            if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
                getLogger().error("confCluster state is invalid,clusterId:{},state:{}",
                        task.getClusterId(),
                        confCluster.getState());
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群状态不正确");
                return resultMsg;
            }

            // region 集群同一时刻只能启动一个扩缩容任务check
            ResultMsg runningTask = checkCanCreateScaleTask(task);
            if (runningTask.getResult()) {
                //手动任务需要加入到队列中,弹性的直接返回失败
                ConfScalingTask currentTask = (ConfScalingTask) runningTask.getData();
                if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                    resultMsg.setResult(false);
                    String errorMsg = StrUtil.format("已存在运行中的伸缩任务:{}", currentTask.getTaskId());
                    resultMsg.setErrorMsg(errorMsg);
                    getLogger().error(errorMsg);
                    return resultMsg;
                }
            }
            // endregion

            // region 保存数据
            if (createdTime != null) {
                task.setCreateTime(createdTime);
            } else {
                task.setCreateTime(new Date());
            }
            if (scaleOutTaskId != null) {
                task.setScaleoutTaskId(scaleOutTaskId);
            }
            taskMapper.insert(task);
            // endregion

            // region 更新infoclustervm表，写入scaleintaskid
            infoClusterVmMapper.updateScaleinTaskIdByClusterIdAndVmNames(
                    confCluster.getClusterId(),
                    vmNames,
                    task.getTaskId());
            // endregion

            addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleIn, task);

            resultMsg.setResult(true);
            resultMsg.setData(task.getTaskId());
        } catch (Exception e) {
            getLogger().error("begin createScaleInTaskForDeleteVm error,clusterId:{},vmRole:{},groupName:{},vmNames:{}",
                    clusterId,
                    vmRole,
                    groupName,
                    vmNames,
                    e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        } finally {
            redisLock.tryUnlock(lockKey);
        }

        return resultMsg;
    }

    private void addTaskWaitQueue(ConfCluster confCluster, String planOPType, ConfScalingTask task) {
        getLogger().info("begin addTaskWaitQueue,clusterId={},planOpType={},taskId={}", confCluster.getClusterId(), planOPType, task.getTaskId());
        try {
            String queueKey = task.getClusterId() + ":" + task.getGroupName();
            redisLock.trySave(getLogger(), ComposeConstant.compose_cluster_vmrole_list + queueKey, "1");

            ConfScalingTask scalingTask = new ConfScalingTask();
            scalingTask.setTaskId(task.getTaskId());
            scalingTask.setInQueue(ConfScalingTask.IN_TAKS_WAIT_QUEUE);
            taskMapper.updateByPrimaryKeySelective(scalingTask);
            getLogger().info("finish addTaskWaitQueue,clusterId={},planOpType={},taskId={},queueKey={}", confCluster.getClusterId(), planOPType, task.getTaskId(), queueKey);
        } catch (Exception ex) {
            getLogger().error("error addTaskWaitQueue,clusterId={},planOpType={},taskId={}", confCluster.getClusterId(), planOPType, task.getTaskId(), ex);
        }
    }

    @Override
    public ResultMsg adjustScaleInCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale in count, task:{}", task);
        ScaleTaskStrategy adjustScaleTaskStrategy = composeStrategyFactory.createScaleTaskStrategy(task);
        return adjustScaleTaskStrategy.adjustScaleInCount(task);
    }

    /**
     * 创建扩容任务
     *
     * @param task
     * @return
     */
    @Override
    public ResultMsg createScaleOutTask(ConfScalingTask task) {
        ResultMsg resultMsg = new ResultMsg();
        getLogger().info("create scaling out task:{}", task);

        // region 集群同一时刻只能启动一个伸缩任务
        String lockkey = "scaling_" + task.getClusterId() + "_" + task.getVmRole() + "_" + task.getGroupName();

        if (!task.getVmRole().equalsIgnoreCase("core")
                && !task.getVmRole().equalsIgnoreCase("task")) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("只允许core和task的实例组进行扩缩容。");
            return resultMsg;
        }
        // endregion

        boolean lock = false;
        try {
            lock = redisLock.tryLock(lockkey, TimeUnit.SECONDS, 200, 300);
            if (!lock) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群同一时刻只能启动一个伸缩任务:" + task.getClusterId());
                return resultMsg;
            }

            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());
            if (confCluster == null) {
                getLogger().error("not found confCluster,clusterId:{}", task.getClusterId());
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群信息未找到");
                return resultMsg;
            }

            if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
                getLogger().error("confCluster state is not CREATED,confCluster:{}", confCluster);
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群状态不正确");
                return resultMsg;
            }

            // region 集群同一时刻只能启动一个扩缩容任务check
            ResultMsg runningTask = checkCanCreateScaleTask(task);
            if (runningTask.getResult()) {
                //手动任务需要加入到队列中,弹性的直接返回失败
                ConfScalingTask currentTask = (ConfScalingTask) runningTask.getData();
                if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                    resultMsg.setResult(false);
                    String errorMsg = StrUtil.format("已存在运行中的伸缩任务:{}", currentTask.getTaskId());
                    resultMsg.setErrorMsg(errorMsg);
                    getLogger().error(errorMsg);
                    return resultMsg;
                }
            }
            // endregion

            ResultMsg adjustResultMsg = adjustScaleOutCount(task);
            if (!adjustResultMsg.getResult())
                return adjustResultMsg;

            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
            if (PurchaseType.Spot.equalValue(hostGroup.getPurchaseType())
                    && !Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_spot)
                    && !Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_delete_Task_Vm)) {
                if (CompareUtil.compare(hostGroup.getInsCount(), hostGroup.getExpectCount()) < 0
                        && !Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_create_group)) {
                    getLogger().error("竞价实例当前实例数小于期望实例数不进行扩容,clusterId={},taskId={}", task.getClusterId(), task.getTaskId());
                    return ResultMsg.FAILURE("竞价实例当前实例数小于期望实例数不进行扩容由竞价服务触发扩容");
                } else {
                    hostGroup.setExpectCount(task.getAfterScalingCount());
                    confClusterHostGroupMapper.updateByPrimaryKeySelective(hostGroup);
                    getLogger().error("更新竞价实例期望数量由竞价任务触发扩容,clusterId={},taskId={},expectCount={}", task.getClusterId(), task.getTaskId(), hostGroup.getExpectCount());
                    ResultMsg rm = new ResultMsg();
                    rm.setResult(true);
                    rm.setMsg("更新竞价实例期望数量由竞价任务触发扩容");
                    rm.setData(task.getTaskId());
                    return rm;
                }
            }

            task.setState(ConfScalingTask.SCALINGTASK_Create);

            // endregion

            // region 获取集群配置信息
            List<ConfClusterVm> confClusterVms
                    = confClusterVmMapper.getVmConfsByGroupName(task.getGroupName(), task.getClusterId());

            if (confClusterVms.size() != 1) {
                resultMsg.setResult(false);
                return resultMsg;
            }

            ConfClusterVm confClusterVm = confClusterVms.get(0);

            List<ConfClusterVmDataVolume> dataVolumes
                    = dataVolumeMapper.selectByVmConfId(confClusterVm.getVmConfId());
            // endregion

            List<InfoClusterVm> infoClusterVmList = infoClusterVmMapper.selectByClusterId(confCluster.getClusterId());

            // region confscalingtask

            task.setDefaultUsername(infoClusterVmList.get(0).getDefaultUsername());
            if (org.apache.commons.lang3.StringUtils.isEmpty(task.getDefaultUsername())) {
                task.setDefaultUsername(vmusername);
                getLogger().warn("clusterId:" + confCluster.getClusterId() + ", miss vmusername,use default vmusername:" + vmusername);
            }

            // endregion

            // region 构建conf_scaling_task_vm
            ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
            confScalingTaskVm.setVmDetailId(UUID.randomUUID().toString().replaceAll("-", ""));
            confScalingTaskVm.setTaskId(task.getTaskId());
            confScalingTaskVm.setCount(task.getScalingCount());
            confScalingTaskVm.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingTaskVm.setOsImageType(confClusterVm.getOsImageType());
            confScalingTaskVm.setMemory(confClusterVm.getMemory());
            confScalingTaskVm.setSku(confClusterVm.getSku());
            confScalingTaskVm.setOsImageid(confClusterVm.getOsImageid());
            confScalingTaskVm.setOsVersion(confClusterVm.getOsVersion());
            confScalingTaskVm.setOsVolumeSize(confClusterVm.getOsVolumeSize());
            confScalingTaskVm.setOsVolumeType(confClusterVm.getOsVolumeType());
            confScalingTaskVm.setVcpus(confClusterVm.getVcpus());
            confScalingTaskVm.setVmConfId(confClusterVm.getVmConfId());
            confScalingTaskVm.setCreatedby(task.getCreatedBy());
            if (task.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                getLogger().info("set confScalingTaskVm purchaseType to ConfClusterVm.PURCHASETYPE_SPOT");
                confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT);
            } else {
                confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_ONDEMOND);
            }
            confScalingTaskVm.setCreatedTime(new Date());
            // endregion

            // region 构建conf_scaling_task_vm_datavol

            List<ConfScalingVmDataVol> vmDataVols = new ArrayList<>();
            dataVolumes.stream().forEach(d -> {
                ConfScalingVmDataVol scalingVmDataVol = new ConfScalingVmDataVol();
                scalingVmDataVol.setCount(d.getCount());
                scalingVmDataVol.setDataVolumeSize(d.getDataVolumeSize());
                scalingVmDataVol.setVmDetailId(confScalingTaskVm.getVmDetailId());
                scalingVmDataVol.setDataVolumeType(d.getDataVolumeType());
                scalingVmDataVol.setLocalVolumeType(d.getLocalVolumeType());
                scalingVmDataVol.setVmDataVolId(UUID.randomUUID().toString().replaceAll("-", ""));
                vmDataVols.add(scalingVmDataVol);
            });

            // endregion

            // region 保存数据
            task.setCreateTime(new Date());
            getLogger().info("insert task:{}", task);
            taskMapper.insert(task);

            getLogger().info("insert confScalingTaskVm:{}", task);
            taskVmMapper.insert(confScalingTaskVm);

            getLogger().info("insert vmDataVols:{}", vmDataVols);
            vmDataVolMapper.insertBatch(vmDataVols);

            // endregion

            addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleOut, task);
            resultMsg.setResult(true);
            resultMsg.setData(task.getTaskId());
        } catch (Exception e) {
            getLogger().error("createScaleOutTask,e ", e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if (lock) {
                    redisLock.unlock(lockkey);
                }
            } catch (Exception e) {
                getLogger().error("createScaleOutTask, 释放锁异常", e);
            }
        }

        return resultMsg;
    }

    @Override
    public ResultMsg createScalePartOutTask(ConfScalingTask task) {
        ResultMsg resultMsg = new ResultMsg();
        getLogger().info("scaling part task:" + task.toString());

        // region 集群同一时刻只能启动一个伸缩任务
        if (!task.getVmRole().equalsIgnoreCase("core")) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("只允许core实例组进行磁盘扩容。");
            return resultMsg;
        }
        // endregion

        String lockKey = "scaling_" + task.getClusterId() + "_" + task.getVmRole() + "_" + task.getGroupName();

        boolean lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 200, 300);
        if (!lock) {
            throw new RuntimeException("集群同一时刻只能启动一个磁盘扩容任务:" + task.getClusterId());
        }

        try {
            // region 集群同一时刻只能启动一个扩缩容任务check
            ResultMsg runningTaskResult = checkCanCreateScaleTask(task);
            if (runningTaskResult.getResult()) {
                ConfScalingTask runningTask = (ConfScalingTask) runningTaskResult.getData();
                throw new RuntimeException("磁盘扩容时不能存在其它任务,当前已存在运行中的任务:" + runningTask.getTaskId());
            }
            // endregion

            // region 验证最大扩容数量
            if (task.getScalingCount() > maxPartSize) {
                throw new RuntimeException("磁盘最大扩容不能超过4096G");
            }
            task.setState(ConfScalingTask.SCALINGTASK_Create);

            // endregion

            // region 获取集群配置信息
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());

            List<ConfClusterVm> confClusterVms
                    = confClusterVmMapper.getVmConfsByGroupName(task.getGroupName(), task.getClusterId());

            if (confClusterVms.size() != 1) {
                resultMsg.setResult(false);
                return resultMsg;
            }
            // endregion

            List<InfoClusterVm> infoClusterVmList = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(confCluster.getClusterId(),
                    task.getGroupName(),
                    InfoClusterVm.VM_RUNNING);

            if (infoClusterVmList.isEmpty()) {
                resultMsg.setErrorMsg("未发现安在运行的实例信息");
                return resultMsg;
            }

            // region confscalingtask

            task.setDefaultUsername(infoClusterVmList.get(0).getDefaultUsername());
            if (org.apache.commons.lang3.StringUtils.isEmpty(task.getDefaultUsername())) {
                task.setDefaultUsername(vmusername);
                getLogger().warn("clusterId:" + confCluster.getClusterId() + ", miss vmusername,use default vmusername:" + vmusername);
            }

            // endregion

            task.setCreateTime(new Date());

            int VM_COUNT_PRE_TASK = 50;
            int leftCount = infoClusterVmList.size();
            int beginIndex = 0;
            int endIndex = 0;
            int splitIndex = 0;
            while (leftCount > 0) {
                int deltaCount = leftCount - VM_COUNT_PRE_TASK;
                if (deltaCount >= 0) {
                    endIndex += VM_COUNT_PRE_TASK;
                    deltaCount = VM_COUNT_PRE_TASK;
                } else {
                    endIndex += leftCount;
                    deltaCount = leftCount;
                }

                addSubScalePartOutTaskWaitQueue(confCluster,
                        confClusterVms,
                        task,
                        infoClusterVmList,
                        infoClusterVmList.subList(beginIndex, endIndex),
                        beginIndex,
                        endIndex,
                        splitIndex);
                beginIndex += deltaCount;
                leftCount -= deltaCount;
                splitIndex++;
            }

            resultMsg.setResult(true);
//            resultMsg.setBizid(plan_msg.getBizid());
            resultMsg.setExt1(task.getTaskId());
        } catch (Exception e) {
            getLogger().error("createScaleOutTask,e ", e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(e.getMessage());
        } finally {
            redisLock.tryUnlock(lockKey);
        }

        return resultMsg;
    }

    private void addSubScalePartOutTaskWaitQueue(ConfCluster confCluster,
                                                 List<ConfClusterVm> confClusterVms,
                                                 ConfScalingTask originalTask,
                                                 List<InfoClusterVm> totalVmList,
                                                 List<InfoClusterVm> subList,
                                                 int beginIndex,
                                                 int endIndex,
                                                 int splitIndex) {
        ConfClusterVm confClusterVm = confClusterVms.get(0);

        List<ConfClusterVmDataVolume> dataVolumes
                = dataVolumeMapper.selectByVmConfId(confClusterVm.getVmConfId());


        ConfScalingTask confScalingTask = new ConfScalingTask();
        BeanUtils.copyProperties(originalTask, confScalingTask);
        confScalingTask.setTaskId(UUID.randomUUID().toString());
        confScalingTask.setClusterId(originalTask.getClusterId());
        confScalingTask.setScalingType(ConfScalingTask.ScaleType_Part_OUT);
        confScalingTask.setBeforeScalingCount(originalTask.getBeforeScalingCount());
        confScalingTask.setAfterScalingCount(originalTask.getAfterScalingCount());
        confScalingTask.setScalingCount(originalTask.getScalingCount());
        confScalingTask.setExpectCount(originalTask.getExpectCount());
        confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
        confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
        confScalingTask.setBegTime(DateUtils.addMilliseconds(originalTask.getBegTime(), splitIndex * 100));
        confScalingTask.setVmRole(originalTask.getVmRole());
        // EsRuleName是重要信息不能随便改
        confScalingTask.setEsRuleName(String.format("磁盘扩容拆分(%d-%d/%d)", beginIndex + 1, endIndex, totalVmList.size()));
        confScalingTask.setGroupName(originalTask.getGroupName());
        confScalingTask.setCreateTime(DateUtils.addMilliseconds(originalTask.getBegTime(), splitIndex * 100));
        confScalingTask.setInQueue(ConfScalingTask.IN_TAKS_WAIT_QUEUE);
        taskMapper.insert(confScalingTask);
        //加入队列,用于创建计划排队
        String queueKey = confScalingTask.getClusterId() + ":" + confScalingTask.getGroupName();
        redisLock.trySave(getLogger(), ComposeConstant.compose_cluster_vmrole_list + queueKey, "1");

        getLogger().info("insert confScalingTask:{}", confScalingTask);

        for (InfoClusterVm infoClusterVm : subList) {
            ConfScalingVm confScalingVm = new ConfScalingVm();
            confScalingVm.setRecordId(UUID.randomUUID().toString());
            confScalingVm.setClusterId(confScalingTask.getClusterId());
            confScalingVm.setScalingType(confScalingTask.getScalingType());
            confScalingVm.setBeforeScalingCount(confScalingTask.getBeforeScalingCount());
            confScalingVm.setAfterScalingCount(confScalingTask.getAfterScalingCount());
            confScalingVm.setScalingCount(confScalingTask.getScalingCount());
            confScalingVm.setExpectCount(confScalingTask.getExpectCount());
            confScalingVm.setOperationType(confScalingTask.getOperatiionType());
            confScalingVm.setCreatedBy(confScalingTask.getCreatedBy());
            confScalingVm.setDefaultUsername(confScalingTask.getDefaultUsername());
            confScalingVm.setGroupId(infoClusterVm.getGroupId());
            confScalingVm.setGroupName(confScalingTask.getGroupName());
            confScalingVm.setTaskId(confScalingTask.getTaskId());
            confScalingVm.setPurchaseType(infoClusterVm.getPurchaseType());
            confScalingVm.setVmName(infoClusterVm.getVmName());
            confScalingVm.setHostName(infoClusterVm.getHostName());
            confScalingVm.setVmConfId(infoClusterVm.getVmConfId());
            confScalingVm.setInternalIp(infoClusterVm.getInternalip());
            confScalingVm.setSkuName(infoClusterVm.getSkuName());
            confScalingVm.setVmRole(infoClusterVm.getVmRole());
            confScalingVm.setEsRuleName(confScalingTask.getEsRuleName());
            confScalingVm.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingVm.setBegTime(confScalingTask.getBegTime());
            confScalingVm.setCreateTime(confScalingTask.getCreateTime());
            getLogger().info("insert confScalingVm:{}", confScalingVm);
            confScalingVmMapper.insertSelective(confScalingVm);
        }

        if (splitIndex == 0) {
            // region 构建conf_scaling_task_vm
            ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
            confScalingTaskVm.setVmDetailId(UUID.randomUUID().toString().replaceAll("-", ""));
            confScalingTaskVm.setTaskId(confScalingTask.getTaskId());
            confScalingTaskVm.setCount(confScalingTask.getScalingCount());
            confScalingTaskVm.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingTaskVm.setOsImageType(confClusterVm.getOsImageType());
            confScalingTaskVm.setMemory(confClusterVm.getMemory());
            confScalingTaskVm.setSku(confClusterVm.getSku());
            confScalingTaskVm.setOsImageid(confClusterVm.getOsImageid());
            confScalingTaskVm.setOsVersion(confClusterVm.getOsVersion());
            confScalingTaskVm.setOsVolumeSize(confClusterVm.getOsVolumeSize());
            confScalingTaskVm.setOsVolumeType(confClusterVm.getOsVolumeType());
            confScalingTaskVm.setVcpus(confClusterVm.getVcpus());
            confScalingTaskVm.setVmConfId(confClusterVm.getVmConfId());
            confScalingTaskVm.setCreatedby("sysadmin");
            if (Objects.equals(confScalingTask.getOperatiionType(), ConfScalingTask.Operation_type_spot)) {
                getLogger().info("set confScalingTaskVm purchaseType to ConfClusterVm.PURCHASETYPE_SPOT");
                confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT);
            } else {
                confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_ONDEMOND);
            }
            confScalingTaskVm.setCreatedTime(new Date());
            // endregion

            // region 构建conf_scaling_task_vm_datavol

            List<ConfScalingVmDataVol> vmDataVols = new ArrayList<>();
            dataVolumes.stream().forEach(d -> {
                ConfScalingVmDataVol scalingVmDataVol = new ConfScalingVmDataVol();
                scalingVmDataVol.setCount(d.getCount());
                scalingVmDataVol.setDataVolumeSize(d.getDataVolumeSize());
                scalingVmDataVol.setVmDetailId(confScalingTaskVm.getVmDetailId());
                scalingVmDataVol.setDataVolumeType(d.getDataVolumeType());
                scalingVmDataVol.setLocalVolumeType(d.getLocalVolumeType());
                scalingVmDataVol.setVmDataVolId(UUID.randomUUID().toString().replaceAll("-", ""));
                vmDataVols.add(scalingVmDataVol);
            });

            // endregion

            // region 保存数据
            taskVmMapper.insert(confScalingTaskVm);
            getLogger().info("insert confScalingTaskVm:{}", confScalingTaskVm);
            vmDataVolMapper.insertBatch(vmDataVols);
            getLogger().info("insert vmDataVols:{}", vmDataVols);
        }
    }

    @Override
    public ResultMsg adjustScaleOutCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale out count, task:{}", task);
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        List<InfoClusterVm> workvms =
                infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                        task.getClusterId(),
                        task.getGroupName(),
                        InfoClusterVm.VM_RUNNING);

        if (task.getExpectCount() != null) {
            if (workvms.size() >= task.getExpectCount()) {
                getLogger().error("adjust scale out count error, vms size:{} greater equal expect count:{}, clusterId:{}, groupName:{}",
                        workvms.size(),
                        task.getExpectCount(),
                        task.getClusterId(),
                        task.getGroupName());
                resultMsg.setResultFail("当前实例组不需要扩容,当前实例数(" + workvms.size() + "),期望数量(" + task.getExpectCount() + ")");
                resultMsg.setRetcode("404");
                resultMsg.setResult(false);
                return resultMsg;
            } else {
                //region spot expectCount 再次验证
                if (task.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                    ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
                    if (confClusterHostGroup == null) {
                        resultMsg.setResultFail("当前实例组不存在");
                        resultMsg.setRetcode("404");
                        resultMsg.setResult(false);
                        return resultMsg;
                    }

                    if (!Objects.equals(confClusterHostGroup.getState(), ConfClusterHostGroup.STATE_RUNNING)) {
                        resultMsg.setResultFail("当前实例组状态不正确");
                        resultMsg.setRetcode("404");
                        resultMsg.setResult(false);
                        return resultMsg;
                    }

                    if (confClusterHostGroup.getExpectCount().intValue() < task.getExpectCount().intValue()) {
                        resultMsg.setResultFail("当前实例组期望值不匹配,taskExpectCount:" + task.getExpectCount() + ",groupExpectCount:" + confClusterHostGroup.getExpectCount());
                        resultMsg.setRetcode("404");
                        resultMsg.setResult(false);
                        return resultMsg;
                    }

                    if (confClusterHostGroup.getExpectCount().intValue() < task.getScalingCount().intValue() + workvms.size()) {
                        resultMsg.setResultFail("当前伸缩数量超过了期望值,taskExpectCount:" + task.getExpectCount() + ",groupExpectCount:" + confClusterHostGroup.getExpectCount() +
                                "scalingCount:" + task.getScalingCount() + ",workVms:" + workvms.size());
                        resultMsg.setRetcode("404");
                        resultMsg.setResult(false);
                        return resultMsg;
                    }
                    //endregion

                    int actualScalingOutCount = task.getExpectCount() - workvms.size();
                    ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
                    confScalingTaskVm.setTaskId(task.getTaskId());
                    confScalingTaskVm.setCount(actualScalingOutCount);
                    confScalingTaskVmMapper.updateCount(confScalingTaskVm);
                    getLogger().info("adjust clusterId:{},groupName:{},confScalingTaskVm:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            confScalingTaskVm);

                    task.setScalingCount(actualScalingOutCount);
                    task.setBeforeScalingCount(workvms.size());
                    task.setAfterScalingCount(workvms.size() + actualScalingOutCount);
                    task.setExpectCount(task.getAfterScalingCount());
                    getLogger().info("adjust scale out count, vms size:{}, expect count:{}, actualScalingOutCount:{}, clusterId:{}, groupName:{}",
                            workvms.size(),
                            task.getExpectCount(),
                            actualScalingOutCount,
                            task.getClusterId(),
                            task.getGroupName());
                    getLogger().info("adjustScaleOutCount non spot task result:{}", task);
                    confScalingTaskMapper.updateByPrimaryKey(task);
                    return resultMsg;
                } else {
                    int actualScalingOutCount = task.getExpectCount() - workvms.size();
                    ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
                    confScalingTaskVm.setTaskId(task.getTaskId());
                    confScalingTaskVm.setCount(actualScalingOutCount);
                    confScalingTaskVmMapper.updateCount(confScalingTaskVm);
                    getLogger().info("adjust clusterId:{},groupName:{},confScalingTaskVm:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            confScalingTaskVm);

                    task.setScalingCount(actualScalingOutCount);
                    task.setBeforeScalingCount(workvms.size());
                    task.setAfterScalingCount(workvms.size() + actualScalingOutCount);
                    getLogger().info("adjust scale out count, vms size:{}, expect count:{}, actualScalingOutCount:{}, clusterId:{}, groupName:{}",
                            workvms.size(),
                            task.getExpectCount(),
                            actualScalingOutCount,
                            task.getClusterId(),
                            task.getGroupName());
                    getLogger().info("adjustScaleOutCount non spot task result:{}", task);
                    confScalingTaskMapper.updateByPrimaryKey(task);
                    return resultMsg;
                }
            }
        }

        // region 弹性扩容数据校验
        Integer actualScalingOutCount = 0;
        if (task.getMaxCount() != null && task.getMinCount() != null) {
            // 最大可用扩容数量
            Integer availableVmSize = task.getMaxCount() - workvms.size();
            if (availableVmSize <= 0) {
                resultMsg.setResultFail("不需要申请新的VM来进行扩容,vms.size:" + workvms.size() + ",MinCount:" + task.getMinCount() + ",MaxCount:" + task.getMaxCount());
                resultMsg.setRetcode("400");
                return resultMsg;
            }

            // 实际容的数量
            if (task.getScalingCount().compareTo(availableVmSize) > 0) {
                // 大于可用机器
                actualScalingOutCount = availableVmSize;
            } else {
                // 小于等于可用机器
                actualScalingOutCount = task.getScalingCount();
            }


            ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
            confScalingTaskVm.setTaskId(task.getTaskId());
            confScalingTaskVm.setCount(actualScalingOutCount);
            confScalingTaskVmMapper.updateCount(confScalingTaskVm);
            getLogger().info("adjust scale rule clusterId:{},groupName:{},confScalingTaskVm:{}",
                    task.getClusterId(),
                    task.getGroupName(),
                    confScalingTaskVm);

            // 修正数据
            task.setScalingCount(actualScalingOutCount);
            task.setBeforeScalingCount(workvms.size());
            task.setAfterScalingCount(workvms.size() + actualScalingOutCount);
            task.setExpectCount(task.getAfterScalingCount());
            getLogger().info("adjustScaleOutCount scale rule task result:{}", task);
            confScalingTaskMapper.updateByPrimaryKey(task);
        }
        return resultMsg;
    }

    @Override
    public ResultMsg scaleInForDeleteTaskVm(String clusterId,
                                            String groupId,
                                            String scaleOutTaskId,
                                            List<String> vmNames,
                                            Date createdTime) {
        ConfClusterHostGroup confClusterHostGroup =
                confClusterHostGroupMapper.selectByPrimaryKey(groupId);

        return createScaleInTaskForDeleteTaskVm(clusterId,
                confClusterHostGroup.getVmRole(),
                confClusterHostGroup.getGroupName(),
                scaleOutTaskId,
                vmNames,
                createdTime);
    }

    /**
     * 检查是否可以创建扩缩容任务
     *
     * @param task
     * @return
     */
    private ResultMsg checkCanCreateScaleTask(ConfScalingTask task) {
        ResultMsg resultMsg = new ResultMsg();
        ConfScalingTask runningTask = taskMapper.peekQueueHeadTask(task.getClusterId(),
                task.getVmRole(),
                task.getGroupName(),
                Arrays.asList(ConfScalingTask.SCALINGTASK_Create, ConfScalingTask.SCALINGTASK_Running));
        if (runningTask != null) {
            resultMsg.setResult(true);
            resultMsg.setData(runningTask);
        }
        return resultMsg;
    }

    private Map<String, Object> buildScalingAzureVmsRequest(ConfScalingTask task,
                                                            String activityLogId) throws IllegalStateException {
        getLogger().info("buildAzureVmsRequest taskId:{}, vmRole:{}, clusterId:{}, activityLogId:{}",
                task.getTaskId(),
                task.getVmRole(),
                task.getClusterId(),
                activityLogId);
        HashMap<String, Object> res = new HashMap<>();

        AzureAppendVMsRequest azureVmsRequest = new AzureAppendVMsRequest();
        azureVmsRequest.setApiVersion("V1");

        List<AzureVMGroupsRequest> virtualMachineGroups = new ArrayList<>();

        //region 1.获取cluster conf
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());

        if (confCluster == null) {
            getLogger().error("not found cluster config for id:" + task.getClusterId());
            return null;
        }

        azureVmsRequest.setRegion(confCluster.getRegion());

        if (!confCluster.getState().equals(ConfCluster.CREATED)) {
            //需要是正在运行的集群
            getLogger().error("集群状态不符合伸缩要求 cluster:{}", confCluster);
            throw new IllegalStateException("集群状态不正常");
        }

        //region 查询正在扩容的同vmrole任务
        InfoClusterVmIndexManager infoClusterVmIndexManager = new InfoClusterVmIndexManager(this.getLogger(),
                this.redisLock,
                this.infoClusterVmMapper,
                this.confScalingTaskMapper,
                this.infoClusterVmIndexMapper,
                this.infoClusterVmIndexHistoryMapper);
        int begindex = infoClusterVmIndexManager.requestNewVmIndex(
                task.getClusterId(),
                task.getVmRole(),
                task.getTaskId(),
                task.getCreateTime(),
                task.getScalingCount());
        getLogger().info("requestNewVmIndex:{}", begindex);

        //endregion 1.获取cluster conf

        //region 2.设置MI数据

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

        //endregion 设置MI数据

        //region 3.获取vmjob
        InfoClusterVmJob vmJob =
                ivmService.getVmJob(task.getClusterId(), confCluster.getClusterName(), "scaleout", activityLogId);
        InfoClusterOperationPlanActivityLogWithBLOBs activityLogWithBLOBs = activityLogMapper.selectByPrimaryKey(activityLogId);
        if (vmJob.getJobStatus() > 0) {
            getLogger().info("scale out VM Job is running ,transactionid:" + vmJob.getTransactionId());
            return null;
        }

        azureVmsRequest.setTransactionId(vmJob.getTransactionId());
        azureVmsRequest.setClusterName(confCluster.getClusterName());

        //endregion 获取vmjob

        //region 4.获取集群标签
        List<ConfClusterTag> clusterTags =
                tagMapper.getTagsbyClusterId(confCluster.getClusterId());
        HashMap<String, String> tagmap = new HashMap<>();

        if (null != clusterTags && clusterTags.size() > 0) {
            clusterTags.stream().forEach(x -> {
                tagmap.put(x.getTagGroup(), x.getTagVal());
            });
        }
        //endregion

        //region 5. 查询伸缩VM配置数据
        List<ConfScalingTaskVm> confScalingTaskVms = taskVmMapper.getScalingVmConfig(task.getTaskId());
        // endregion

        //region 6.构建 VMGroup entity
        confScalingTaskVms.stream().forEach(item -> {
            AzureVMGroupsRequest vmGroup = new AzureVMGroupsRequest();
            vmGroup.setGroupName(task.getVmRole());
            vmGroup.setCount(item.getCount());

            ConfClusterVm confClusterVm = confClusterVmMapper.selectByClusterIdAndVmConfId(confCluster.getClusterId(), item.getVmConfId());
            Integer provisionType = confClusterVm.getProvisionType();
            if (provisionType != null) {
                if (provisionType == ConfClusterVm.PROVISION_TYPE_VM_Standalone.intValue()) {
                    vmGroup.setProvisionType("VM_Standalone");
                } else if (provisionType == ConfClusterVm.PROVISION_TYPE_VMSS_Flexible.intValue()) {
                    vmGroup.setProvisionType("VMSS_Flexible");
                }
            }

            AzureVMSpecRequest spec = new AzureVMSpecRequest();

            AzureSpotProfile azureSpotProfile = null;
            ResultMsg resultMsg = AzureServiceManager.buildAzureSpotProfile(getLogger(), azureService, item, confClusterVmMapper, confCluster.getClusterId(),confCluster.getRegion());
            if (!resultMsg.isResult()) {
                return;
            } else if (resultMsg.getData() != null) {
                azureSpotProfile = (AzureSpotProfile) resultMsg.getData();
                getLogger().info("set AzureSpotProfile:{}", azureSpotProfile);
                spec.setSpotProfile(azureSpotProfile);
            }

            //region VMMI
            spec.setUserAssignedIdentityResourceIds(miList);
            spec.setSkuName(item.getSku());

            //endregion

            //region 镜像设置
            spec.setOsImageType(item.getOsImageType());
            if (item.getOsImageType().equalsIgnoreCase("CustomImage")) {
                spec.setCustomOSImageId(item.getOsImageid());
            }
            if (item.getOsImageType().equalsIgnoreCase("MarketplaceImage")) {
                spec.setMarketplaceOSImageName(item.getOsImageid());
            }
            //endregion

            //region 域名 用户名 可用区 密钥对
            spec.setHostNameSuffix(hostnamesuffix);
            spec.setStartupScriptBlobUrl(null);
            //使用集群创建时的vmusername
            spec.setUserName(task.getDefaultUsername());
            spec.setZone(confCluster.getZone());
            spec.setSshPublicKeySecretName(confCluster.getKeypairId());

            //endregion

            //region 子网
            spec.setSubnetResourceId(confCluster.getSubnet());
            //endregion

            //region 安全组
            if (vmGroup.getGroupName().equalsIgnoreCase("ambari")
                    || vmGroup.getGroupName().equalsIgnoreCase("master")) {
                // ambari and master use masterSecurityGroup
                spec.setNsgResourceId(confCluster.getMasterSecurityGroup());

            } else {
                // core and task use SlaveSecurityGroup
                spec.setNsgResourceId(confCluster.getSlaveSecurityGroup());
            }

            //endregion 安全组

            //region 系统盘
            spec.setOsDiskSku(item.getOsVolumeType());
            spec.setOsDiskSizeGB(item.getOsVolumeSize());
            spec.setSshPublicKeyType("KeyVaultSecret");
            spec.setSshPublicKey(null);

            //endregion

            //region 设置数据盘参数
            List<ConfScalingVmDataVol> vmDataVolumes =
                    vmDataVolMapper.getDataVolByVmConfId(item.getVmDetailId());

            // TODO: 对于L系列的主机，数据盘不进行设置，默认大小和数量都为0即可。 此处需根据磁盘类弄判断， 现在暂时使用主机名是否包含L来区分。
            if (null != vmDataVolumes && vmDataVolumes.size() > 0
                    && item.getSku().indexOf("L") == -1) { //TODO: 此处判断需要调整
                spec.setDataDiskSku(vmDataVolumes.get(0).getDataVolumeType());
                spec.setDataDiskSizeGB(vmDataVolumes.get(0).getDataVolumeSize());
                spec.setDataDiskCount(vmDataVolumes.get(0).getCount());
            }
            //endregion

            //region 设置VM标签
            HashMap<String, String> vmtagmap = JSON.parseObject(JSON.toJSONString(tagmap),
                    new TypeReference<HashMap<String, String>>() {
                    });
            if (null != vmtagmap) {
                //region 系统预设标签

                vmtagmap.put("sdp-group", task.getGroupName());
                vmtagmap.put("sdp-role", task.getVmRole());
                vmtagmap.put("sdp-version", confCluster.getClusterReleaseVer());
                vmtagmap.put("sdp-purchasetype", Convert.toStr(item.getPurchaseType()));
                if (activityLogWithBLOBs != null){
                    vmtagmap.put("sdp-planid",activityLogWithBLOBs.getPlanId());
                }

                //endregion 系统预设标签

                spec.setVirtualMachineTags(vmtagmap);
            } else {
                vmtagmap = new HashMap<>();
            }

            if (azureSpotProfile != null) {
                vmtagmap.put("sdp-spot-demand-price", String.valueOf(azureSpotProfile.getDemandPricePerHour()));
                vmtagmap.put("sdp-spot-bid-price", String.valueOf(azureSpotProfile.getMaxPricePerHour()));
            }

            //endregion

            vmGroup.setVirtualMachineSpec(spec);

            //region 设置扩容beginIndex
            // 获取正在运行的集群中，当前扩容实例组所在角色的实例数量
            int count = infoClusterVmMapper.getClusterInstanceCountByVmRoleAndState(confCluster.getClusterId(),
                    task.getVmRole().toLowerCase(),
                    null);
            getLogger().info("扩容当前角色的vm数量为:{},begindex:{}", count, begindex);
            count++;
            vmGroup.setBeginIndex(begindex);
            //endregion

            virtualMachineGroups.add(vmGroup);
        });

        //endregion

        if (virtualMachineGroups.isEmpty()) {
            getLogger().error("buildAzureVmsRequest error virtualMachineGroups is empty,taskId:{}, vmRole:{}, clusterId:{}, activityLogId:{}",
                    task.getTaskId(),
                    task.getVmRole(),
                    task.getClusterId(),
                    activityLogId);
            return null;
        }

        azureVmsRequest.setVirtualMachineGroups(virtualMachineGroups);


        res.put("azure", azureVmsRequest);
        res.put("job", vmJob);
        return res;
    }

    /**
     * 构造扩容请求报文
     * @param task
     * @param activityLogId
     * @return
     */
    private Map<String,Object> buildScaleOutAzureFleetVmsRequest(ConfScalingTask task,String activityLogId){
        getLogger().info("buildAzureVmsRequest taskId:{}, vmRole:{}, clusterId:{}, activityLogId:{}",
                task.getTaskId(),
                task.getVmRole(),
                task.getClusterId(),
                activityLogId);

        //region 2.获取cluster conf
        ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());

        if (confCluster == null) {
            getLogger().error("not found cluster config for id:" + task.getClusterId());
            return null;
        }

        if (!confCluster.getState().equals(ConfCluster.CREATED)) {
            //需要是正在运行的集群
            getLogger().error("集群状态不符合伸缩要求 cluster:{}", confCluster);
            throw new IllegalStateException("集群状态不正常");
        }

        //region 3.查询正在扩容的同vmrole任务
        InfoClusterVmIndexManager infoClusterVmIndexManager = new InfoClusterVmIndexManager(this.getLogger(),
                this.redisLock,
                this.infoClusterVmMapper,
                this.confScalingTaskMapper,
                this.infoClusterVmIndexMapper,
                this.infoClusterVmIndexHistoryMapper);
        int begindex = infoClusterVmIndexManager.requestNewVmIndex(
                task.getClusterId(),
                task.getVmRole(),
                task.getTaskId(),
                task.getCreateTime(),
                task.getScalingCount());
        getLogger().info("requestNewVmIndex:{}", begindex);

        //region 4.获取vmjob
        InfoClusterVmJob vmJob =
                ivmService.getVmJob(task.getClusterId(), confCluster.getClusterName(), "scaleout", activityLogId);
        InfoClusterOperationPlanActivityLogWithBLOBs activityLogWithBLOBs = activityLogMapper.selectByPrimaryKey(activityLogId);
        if (vmJob.getJobStatus() > 0) {
            getLogger().info("scale out VM Job is running ,transactionid:" + vmJob.getTransactionId());
            return null;
        }

        //region 5. 查询伸缩VM配置数据
        List<ConfScalingTaskVm> confScalingTaskVms = taskVmMapper.getScalingVmConfig(task.getTaskId());

        confScalingTaskVms.stream().forEach(item->{
            List<ConfScalingVmDataVol> vmDataVolumes =
                    vmDataVolMapper.getDataVolByVmConfId(item.getVmDetailId());
            item.setVmDataVolumes(vmDataVolumes);
        });
        // endregion
        AzureFleetAppendVMsRequest azureFleetAppendVMsRequest = azureFleetService.buildAzureFleetAppendVMsRequest(task,
                confCluster,
                begindex,
                vmJob,
                confScalingTaskVms);
        HashMap<String, Object> res = new HashMap<>();
        res.put("azure", azureFleetAppendVMsRequest);
        res.put("job", vmJob);
        res.put("subscriptionId",confCluster.getSubscriptionId());
        return res;

    }


    /**
     * shein接口resize
     *
     * @param param
     * @return
     */
    @Override
    public ResultMsg resizeClusterGroup(Map<String, Object> param) {
        ResultMsg msg = new ResultMsg();
        String clusterId = param.get("clusterId").toString();
        String groupName = param.get("igName").toString();

        Integer isGracefulScalein = null;
        Integer waitingtime = null;
        if (!param.containsKey("isGracefulScalein")) {
            isGracefulScalein = Integer.parseInt(resizeIsGraceful);
            waitingtime = Integer.parseInt(scaleinWaitingtime);
        } else {
            isGracefulScalein = Integer.parseInt(param.get("isGracefulScalein").toString());
            waitingtime = Integer.parseInt(param.containsKey("scaleinWaitingtime") ? param.get("scaleinWaitingtime").toString() : "0");
        }

        String lockKey = "request-scale:" + clusterId + ":" + groupName;
        boolean lockResult = this.redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
        if (!lockResult) {
            msg.setResult(false);
            msg.setErrorMsg("执行冲突，请重新提交");
            return msg;
        }

        try {
            Integer insCnt = Integer.parseInt(param.get("insCnt").toString());
            // 查询当前实例组存在的VM
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId,
                    groupName,
                    InfoClusterVm.VM_RUNNING);
            int beforecount =0;
            if (vms == null || vms.size() ==0){
                beforecount = 0;
            }else{
                beforecount=vms.size();
            }

            // 缩容
            if (beforecount > insCnt) {

                int createOrRunningCount = this.confScalingTaskMapper.countByScalingTypeAndState(clusterId,
                        groupName,
                        ConfScalingTask.ScaleType_OUT,
                        ConfScalingTask.ScaleType_IN,
                        ConfScalingTask.SCALINGTASK_Create,
                        ConfScalingTask.SCALINGTASK_Running);
                if (createOrRunningCount > 0) {
                    msg.setResult(false);
                    msg.setErrorMsg("当前实例组存在正在执行的扩缩容任务，请稍后提交");
                    return msg;
                }

                // 缩容
                ConfScalingTask confScalingTask = new ConfScalingTask();

                confScalingTask.setTaskId(UUID.randomUUID().toString());
                confScalingTask.setScalingType(ConfScalingTask.ScaleType_IN);
                confScalingTask.setEsRuleId(null);
                confScalingTask.setEsRuleName(null);
                confScalingTask.setClusterId(clusterId);
                //
                confScalingTask.setVmRole(vms.get(0).getVmRole().toLowerCase());
                confScalingTask.setGroupName(groupName);
                confScalingTask.setBeforeScalingCount(vms.size());
                confScalingTask.setAfterScalingCount(insCnt);
                confScalingTask.setExpectCount(insCnt);
                confScalingTask.setScalingCount(vms.size() - insCnt);
                confScalingTask.setIsGracefulScalein(isGracefulScalein);
                confScalingTask.setScaleinWaitingtime(waitingtime);
                confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
                confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
                confScalingTask.setBegTime(new Date());
                getLogger().info("invoke createScaleinTask task:{}", confScalingTask);
                return createScaleinTask(confScalingTask);
            }

            if (beforecount < insCnt) {
                // 扩容

                int beforeCount =0;
                String vmRole =null;
                if (vms!=null && vms.size()>0){
                    beforeCount = vms.size();
                    vmRole = vms.get(0).getVmRole();
                }else{
                    beforeCount = 0;
                    ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(clusterId,groupName);
                    vmRole = hostGroup.getVmRole();
                }

                ConfScalingTask confScalingTask = new ConfScalingTask();
                confScalingTask.setClusterId(clusterId);
                confScalingTask.setTaskId(UUID.randomUUID().toString());
                confScalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
                confScalingTask.setEsRuleId(null);
                confScalingTask.setEsRuleName(null);
                confScalingTask.setBeforeScalingCount(beforeCount);
                confScalingTask.setAfterScalingCount(insCnt);
                confScalingTask.setExpectCount(insCnt);
                confScalingTask.setScalingCount(insCnt - beforeCount);
                confScalingTask.setIsGracefulScalein(null);
                confScalingTask.setScaleinWaitingtime(null);
                confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
                confScalingTask.setEnableAfterstartScript(1);
                confScalingTask.setEnableBeforestartScript(1);
                confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
                confScalingTask.setBegTime(new Date());
                confScalingTask.setGroupName(groupName);
                confScalingTask.setVmRole(vmRole != null ? vmRole : "");
                getLogger().info("invoke createScaleOutTask task:{}", confScalingTask);
                return createScaleOutTask(confScalingTask);
            }
            msg.setResult(false);
            msg.setErrorMsg("Instance 数量未发生变化，不进行resize");
            return msg;
        } catch (Exception e) {
            getLogger().error("resizeClusterGroup error,clusterId:{},groupName:{}", clusterId, groupName, e);

            msg.setResult(false);
            msg.setErrorMsg("发生了错误" + e.getMessage());
            return msg;
        } finally {
            this.redisLock.unlock(lockKey);
        }
    }

    /**
     * 竞价实例组扩容
     *
     * @param clusterId
     * @param groupId
     * @param scaleCount
     * @return
     */
    @Override
    public ResultMsg spotInsGroupScaleOut(String spotTaskId,
                                          String clusterId,
                                          String groupId,
                                          Integer expectCount,
                                          Integer scaleCount) {

        ConfClusterHostGroup confClusterHostGroup =
                confClusterHostGroupMapper.selectByPrimaryKey(groupId);

        ConfScalingTask scalingTask = new ConfScalingTask();
        scalingTask.setTaskId(spotTaskId);
        scalingTask.setClusterId(clusterId);
        scalingTask.setExpectCount(expectCount);
        scalingTask.setScalingCount(scaleCount);
        scalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
        scalingTask.setOperatiionType(ConfScalingTask.Operation_type_spot);
        scalingTask.setGroupName(confClusterHostGroup.getGroupName());
        scalingTask.setVmRole(confClusterHostGroup.getVmRole());
        scalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
        scalingTask.setEnableBeforestartScript(1);
        scalingTask.setEnableAfterstartScript(1);
        return createScaleOutTask(scalingTask);

    }

    /**
     * 竞价实例组逐出缩容
     *
     * @param clusterId
     * @param groupId
     * @param vmNames
     * @return
     */
    @Override
    public ResultMsg scaleInGroupForSpot(String spotTaskId, String clusterId, String groupId, Integer expectCount, List<String> vmNames) {
        getLogger().info("begin scaleInGroupForSpot,spotTaskId:{},clusterId:{},groupId:{},expectCount:{},vmNames:{}",
                spotTaskId,
                clusterId,
                groupId,
                expectCount,
                vmNames);
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(spotTaskId)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("竞价逐出任务id为null");
            return resultMsg;
        }

        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(groupId);
        if (confClusterHostGroup == null) {
            getLogger().error("error scaleInGroupForSpot not found confClusterHostGroup,spotTaskId:{},clusterId:{},groupId:{}",
                    spotTaskId,
                    clusterId,
                    groupId);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("没有找到对应的实例组");
            return resultMsg;
        }

        String groupName = confClusterHostGroup.getGroupName();
        String vmRole = confClusterHostGroup.getVmRole();

        //region 构建缩容任务
        ConfScalingTask task = new ConfScalingTask();
        task.setTaskId(spotTaskId);
        task.setOperatiionType(ConfScalingTask.Operation_type_spot);
        task.setExpectCount(expectCount);
        task.setScalingCount(vmNames.size());
        task.setGroupName(groupName);
        task.setVmRole(vmRole);
        task.setClusterId(clusterId);
        task.setIsGracefulScalein(1);
        task.setScaleinWaitingtime(60);
        task.setScalingType(ConfScalingTask.ScaleType_IN);
        task.setState(ConfScalingTask.SCALINGTASK_Create);

        getLogger().info("scaleInGroupForSpot task:{}", task);

        //endregion

        if (!task.getVmRole().equalsIgnoreCase("core")
                && !task.getVmRole().equalsIgnoreCase("task")) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("只允许core和task的实例组进行缩容。");
            return resultMsg;
        }

        // 集群同一时刻只能启动一个伸缩任务
        String lockKey = "scaling_" + task.getClusterId() + "_" + task.getVmRole() + "_" + task.getGroupName();
        boolean lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 200, 300);
        if (!lock) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("同一时刻只能启动一个缩容任务:" + task.getClusterId());
            getLogger().error("同一时刻只能启动一个缩容任务:" + task.getClusterId());
            return resultMsg;
        }

        try {
            ConfCluster confCluster = clusterMapper.selectByPrimaryKey(task.getClusterId());
            if (confCluster == null) {
                getLogger().error("not found confCluster,clusterId:{}", task.getClusterId());
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群信息未找到");
                return resultMsg;
            }

            if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
                getLogger().error("confCluster state is invalid,clusterId:{},state:{}",
                        task.getClusterId(),
                        confCluster.getState());
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群状态不正确");
                return resultMsg;
            }


            // region 集群同一时刻只能启动一个扩缩容任务check
            ResultMsg runningTask = checkCanCreateScaleTask(task);
            if (runningTask.getResult()) {
                //手动任务需要加入到队列中,弹性的直接返回失败
                ConfScalingTask currentTask = (ConfScalingTask) runningTask.getData();
                if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                    resultMsg.setResult(false);
                    String errorMsg = StrUtil.format("已存在运行中的伸缩任务:{}", currentTask.getTaskId());
                    resultMsg.setErrorMsg(errorMsg);
                    getLogger().error(errorMsg);
                    return resultMsg;
                }
            }
            // endregion

            // region 更新infoclustervm表，写入scaleintaskid
            infoClusterVmMapper.updateScaleinTaskIdByClusterIdAndVmNames(
                    confCluster.getClusterId(),
                    vmNames,
                    task.getTaskId());
            // endregion

            ResultMsg validateResult = adjustScaleInCount(task);
            if (!validateResult.isResult()) {
                return validateResult;
            }

            // region 保存数据
            task.setCreateTime(new Date());
            taskMapper.insert(task);

            // endregion


            addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleIn, task);

            resultMsg.setResult(true);
            resultMsg.setData(task.getTaskId());
            return resultMsg;
        } catch (Exception e) {
            getLogger().info("error scaleInGroupForSpot,spotTaskId:{},clusterId:{},groupId:{},expectCount:{},vmNames:{}",
                    spotTaskId,
                    clusterId,
                    groupId,
                    expectCount,
                    vmNames,
                    e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            return resultMsg;
        } finally {
            redisLock.tryUnlock(lockKey);
        }
    }
}
