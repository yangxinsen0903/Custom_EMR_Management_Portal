package com.sunbox.service.scale.strategy;

import cn.hutool.core.util.StrUtil;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.*;
import com.sunbox.domain.result.ServiceResult;
import com.sunbox.domain.result.SingleResult;
import com.sunbox.service.consts.ComposeConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 扩缩容策略基类
 */
public abstract class ScaleTaskStrategy implements BaseCommonInterFace {
    @Autowired
    protected InfoClusterVmNeoMapper infoClusterVmMapper;

    @Autowired
    protected ConfScalingTaskVmNeoMapper confScalingTaskVmNeoMapper;

    @Autowired
    protected ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    protected ConfClusterHostGroupNeoMapper confClusterHostGroupNeoMapper;

    @Autowired
    protected ConfClusterNeoMapper confClusterMapper;

    @Autowired
    protected DistributedRedisLock redisLock;

    @Autowired
    protected ConfClusterVmDataVolumeMapper confClusterVmDataVolumeNeoMapper;

    @Autowired
    protected ConfScalingVmDataVolNeoMapper confScalingVmDataVolNeoMapper;

    @Autowired
    protected ConfClusterVmNeoMapper confClusterVmNeoMapper;

    public SingleResult<ClusterScaleOutContext> buildScaleOutContext(String vmUserName, ConfScalingTask confScalingTask) {
        ClusterScaleOutContext scaleOutContext = new ClusterScaleOutContext(vmUserName);
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(confScalingTask.getClusterId());
        if (confCluster == null) {
            getLogger().error("not found confCluster,clusterId:{}", confScalingTask.getClusterId());
            return SingleResult.failure("集群信息未找到: clusterId=" + confScalingTask.getClusterId());
        }
        scaleOutContext.setConfCluster(confCluster);

        ConfClusterHostGroup hostGroup = confClusterHostGroupNeoMapper.selectOneByGroupNameAndClusterId(confScalingTask.getClusterId(), confScalingTask.getGroupName());
        if (hostGroup == null) {
            getLogger().error("not found ConfClusterHostGroup,clusterId:{}, groupName:{}", confScalingTask.getClusterId(), confScalingTask.getGroupName());
            return SingleResult.failure("实例组未找到: clusterId=" + confCluster.getClusterId() +
                    " clusterName=" + confCluster.getClusterName() + " groupName=" + confScalingTask.getGroupName());
        }
        scaleOutContext.setConfClusterHostGroup(hostGroup);

        List<ConfClusterVm> confClusterVms = confClusterVmNeoMapper.getVmConfsByGroupName(confScalingTask.getGroupName(), confScalingTask.getClusterId());
        if (confClusterVms.size() != 1) {
            getLogger().error("invalid confClusterVm size:{},clusterId:{}, groupName:{}", confClusterVms.size(), confScalingTask.getClusterId(), confScalingTask.getGroupName());
            return SingleResult.failure("实例组配置未找到");
        }
        scaleOutContext.setConfClusterVms(confClusterVms);

        ConfScalingTask lastFinishedTaskWithState = confScalingTaskNeoMapper.findLastByScalingTypeAndState(confCluster.getClusterId(),
                hostGroup.getVmRole(),
                hostGroup.getGroupName(),
                ConfScalingTask.ScaleType_OUT,
                ConfScalingTask.SCALINGTASK_Failed);
        scaleOutContext.setLastFinishedTask(lastFinishedTaskWithState);

        return SingleResult.success(scaleOutContext);
    }

    public abstract ResultMsg adjustScaleInCount(ConfScalingTask task);

    public ServiceResult validateScaleOutBeforeAdjustCount(ClusterScaleOutContext context, ConfScalingTask task) {
//        ConfScalingTask lastFinishedTask = context.getLastFinishedTask();
//        if (lastFinishedTask != null) {
//            Integer runningVmSize = this.infoClusterVmMapper.countByScaleOutTaskIdAndState(lastFinishedTask.getClusterId(), lastFinishedTask.getTaskId(),
//                    InfoClusterVm.VM_RUNNING);
//            if (runningVmSize > 0) {
//                getLogger().error("can not create scale out task because lastFinishedTask is failed and has running vms:{},lastFinishedTask:{}",
//                        runningVmSize,
//                        lastFinishedTask);
//                return ServiceResult.failure(lastFinishedTask.getTaskId() +
//                        "的扩容任务执行失败，需要先销毁其中申请的实例，再提交新的扩容");
//            }
//        }

        ConfCluster confCluster = context.getConfCluster();

        if(!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
            getLogger().error("confCluster state is invalid,clusterId:{},state:{}",
                    task.getClusterId(),
                    confCluster.getState());
            return ServiceResult.failure("集群状态验证未通过");
        }

        // region 集群同一时刻只能启动一个扩缩容任务check
        ConfScalingTask currentTask = confScalingTaskNeoMapper.peekQueueHeadTask(task.getClusterId(),
                task.getVmRole(),
                task.getGroupName(),
                Arrays.asList(ConfScalingTask.SCALINGTASK_Create, ConfScalingTask.SCALINGTASK_Running));
        if (currentTask != null) {
            //手动任务需要加入到队列中,弹性的直接返回失败
            if (Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)) {
                String errorMsg = StrUtil.format("已存在运行中的扩缩容任务, 本次弹性扩缩容任务中止: clusterId={}, groupName={}, taskId={}",
                        currentTask.getClusterId(), currentTask.getGroupName(), currentTask.getTaskId());
                getLogger().error(errorMsg);
                return ServiceResult.failure(errorMsg);
            }
        }
        // endregion
        return ServiceResult.OK;
    }

    public ServiceResult validateScaleOutAfterAdjustCount(ClusterScaleOutContext context, ConfScalingTask task) {
        return ServiceResult.OK;
    }

    public SingleResult<ConfScalingTask> createScaleOutTask(ClusterScaleOutContext context, ConfScalingTask task) {
        ConfCluster confCluster = context.getConfCluster();
        ConfClusterVm confClusterVm = context.getConfClusterVms().get(0);

        List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeNeoMapper.selectByVmConfId(confClusterVm.getVmConfId());
        InfoClusterVm infoClusterVm = infoClusterVmMapper.selectOneByClusterId(confCluster.getClusterId());

        task.setDefaultUsername(infoClusterVm.getDefaultUsername());
        if (StringUtils.isEmpty(task.getDefaultUsername())) {
            task.setDefaultUsername(context.getVmUserName());
            getLogger().warn("clusterId:" + confCluster.getClusterId() + ", miss vmusername,use default vmusername:" + context.getVmUserName());
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
        confScalingTaskVm.setCreatedby("sysadmin");
        if (task.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
            getLogger().info("set confScalingTaskVm purchaseType to ConfClusterVm.PURCHASETYPE_SPOT");
            confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT);
        } else if (task.getOperatiionType().equals(ConfScalingTask.Operation_type_Complete_Evict_Vm)) {
            getLogger().info("补全驱逐SpotVM, 设置confScalingTaskVm的purchaseType为:ConfClusterVm.PURCHASETYPE_SPOT");
            confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT);
        } else {
            confScalingTaskVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_ONDEMOND);
        }
        confScalingTaskVm.setCreatedTime(new Date());
        // endregion

        // region 构建conf_scaling_task_vm_datavol
        List<ConfScalingVmDataVol> confScalingVmDataVols = new ArrayList<>();
        confClusterVmDataVolumes.stream().forEach(confClusterVmDataVolume -> {
            ConfScalingVmDataVol confScalingVmDataVol = new ConfScalingVmDataVol();
            confScalingVmDataVol.setCount(confClusterVmDataVolume.getCount());
            confScalingVmDataVol.setDataVolumeSize(confClusterVmDataVolume.getDataVolumeSize());
            confScalingVmDataVol.setVmDetailId(confScalingTaskVm.getVmDetailId());
            confScalingVmDataVol.setDataVolumeType(confClusterVmDataVolume.getDataVolumeType());
            confScalingVmDataVol.setLocalVolumeType(confClusterVmDataVolume.getLocalVolumeType());
            confScalingVmDataVol.setVmDataVolId(UUID.randomUUID().toString().replaceAll("-", ""));
            confScalingVmDataVols.add(confScalingVmDataVol);
        });
        // endregion

        // region 保存数据
        task.setCreateTime(new Date());
        task.setState(ConfScalingTask.SCALINGTASK_Create);
        getLogger().info("insert task:{}", task);
        confScalingTaskNeoMapper.insert(task);

        getLogger().info("insert confScalingTaskVm:{}", task);
        confScalingTaskVmNeoMapper.insert(confScalingTaskVm);

        getLogger().info("insert vmDataVols:{}", confScalingVmDataVols);
        confScalingVmDataVolNeoMapper.insertBatch(confScalingVmDataVols);
        // endregion

        return addTaskToWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleOut, task);
    }

    protected SingleResult<ConfScalingTask> addTaskToWaitQueue(ConfCluster confCluster, String planOPType, ConfScalingTask task) {
        getLogger().info("begin addTaskWaitQueue,clusterId={},planOpType={},taskId={}", confCluster.getClusterId(), planOPType, task.getTaskId());
        try {
            String queueKey = task.getClusterId() + ":" + task.getGroupName();
            redisLock.trySave(getLogger(), ComposeConstant.compose_cluster_vmrole_list + queueKey, "1");

            ConfScalingTask scalingTask = new ConfScalingTask();
            scalingTask.setTaskId(task.getTaskId());
            scalingTask.setInQueue(ConfScalingTask.IN_TAKS_WAIT_QUEUE);
            confScalingTaskNeoMapper.updateByPrimaryKeySelective(scalingTask);
            getLogger().info("finish addTaskWaitQueue,clusterId={},planOpType={},taskId={},queueKey={}", confCluster.getClusterId(), planOPType, task.getTaskId(), queueKey);
            return SingleResult.success(task);
        } catch (Exception ex) {
            getLogger().error("error addTaskWaitQueue,clusterId={},planOpType={},taskId={}", confCluster.getClusterId(), planOPType, task.getTaskId(), ex);
            return SingleResult.failure(ex.getMessage());
        }
    }
}
