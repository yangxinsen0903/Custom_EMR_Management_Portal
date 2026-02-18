package com.sunbox.service.scale.strategy;

import cn.hutool.core.comparator.CompareUtil;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.domain.result.ServiceResult;
import com.sunbox.domain.result.SingleResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CompleteEvictVmScaleTaskStrategyImpl extends ScaleTaskStrategy {
    /**
     * 调整数量
     *
     * @param task
     * @return
     */
    @Override
    public ResultMsg adjustScaleInCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale in count, task:{}", task);
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        List<InfoClusterVm> scalingVms = new ArrayList<>();
        List<InfoClusterVm> runningVms =
                infoClusterVmMapper.selectByClusterIdAndGroupNameAndStates(
                        task.getClusterId(),
                        task.getGroupName(),
                        InfoClusterVm.VM_RUNNING,
                        InfoClusterVm.VM_UNKNOWN);

        for (InfoClusterVm runningVm : runningVms) {
            if (StringUtils.equals(runningVm.getScaleinTaskId(), task.getTaskId())) {
                scalingVms.add(runningVm);
            }
        }

        if (task.getScalingCount() != scalingVms.size()) {
            getLogger().error("adjust scale in count error, task scaling count:{} not equal scalingVms size:{}, clusterId:{}, groupName:{}",
                    task.getScalingCount(),
                    scalingVms.size(),
                    task.getClusterId(),
                    task.getGroupName());
            resultMsg.setResultFail("当前没有可用的VM进行缩容,当前实例数(" + scalingVms.size() + "),期望数量(" + task.getScalingCount() + "/" + task.getExpectCount() + ")");
            resultMsg.setErrorMsg("当前没有可用的VM进行缩容,当前实例数(" + scalingVms.size() + "),期望数量(" + task.getScalingCount() + "/" + task.getExpectCount() + ")");
            //resultMsg.setRetcode("404");
            resultMsg.setResult(false);
            return resultMsg;
        }

        int actualAfterCount = runningVms.size() - task.getScalingCount();
        int actualScalingInCount = task.getScalingCount();
        ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
        confScalingTaskVm.setTaskId(task.getTaskId());
        confScalingTaskVm.setCount(actualScalingInCount);
        confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
        getLogger().info("adjust clusterId:{},groupName:{},confScalingTaskVm:{}",
                task.getClusterId(),
                task.getGroupName(),
                confScalingTaskVm);

        task.setBeforeScalingCount(runningVms.size());
        task.setScalingCount(actualScalingInCount);
        task.setAfterScalingCount(actualAfterCount);
        task.setExpectCount(task.getExpectCount()); // 竞价实例不修改期望值
        getLogger().info("adjust scale in count, vms size:{}, expect count:{}, actualScalingInCount:{}, clusterId:{}, groupName:{}",
                runningVms.size(),
                task.getExpectCount(),
                actualScalingInCount,
                task.getClusterId(),
                task.getGroupName());
        confScalingTaskNeoMapper.updateByPrimaryKey(task);
        return resultMsg;
    }

    @Override
    public ServiceResult validateScaleOutBeforeAdjustCount(ClusterScaleOutContext context, ConfScalingTask task) {
        ConfClusterHostGroup confClusterHostGroup = context.getConfClusterHostGroup();
        if (!PurchaseType.Spot.equalValue(confClusterHostGroup.getPurchaseType())) {
            getLogger().error("invalid conf cluster group purchase type,clusterId={},taskId={},group:{}",
                    task.getClusterId(),
                    task.getTaskId(),
                    confClusterHostGroup);
            return ServiceResult.failure("实例组的采购类型不匹配，非竞价实例类型");
        }
        return super.validateScaleOutBeforeAdjustCount(context, task);
    }

    @Override
    public SingleResult<ConfScalingTask> createScaleOutTask(ClusterScaleOutContext context, ConfScalingTask task) {
        ConfClusterHostGroup confClusterHostGroup = context.getConfClusterHostGroup();
        // 调整 ConfScalingTask的数据:
        if (CompareUtil.compare(confClusterHostGroup.getInsCount(), confClusterHostGroup.getExpectCount()) < 0
                && !Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Complete_Evict_Vm)) {
            getLogger().error("竞价实例当前实例数大于期望实例数不进行扩容,clusterId={},taskId={}, insCount={}, expectCount={}",
                    task.getClusterId(), task.getTaskId(), confClusterHostGroup.getInsCount(), confClusterHostGroup.getExpectCount());
            return SingleResult.failure("竞价实例当前实例数大于期望实例数不进行扩容由竞价服务触发扩容");
        } else {
            confClusterHostGroup.setExpectCount(task.getAfterScalingCount());
            confClusterHostGroupNeoMapper.updateByPrimaryKeySelective(confClusterHostGroup);
            getLogger().error("更新竞价实例期望数量由竞价任务触发扩容,clusterId={},taskId={},expectCount={}", task.getClusterId(), task.getTaskId(), confClusterHostGroup.getExpectCount());
        }
        // 保存数据
        super.createScaleOutTask(context, task);
        return SingleResult.success(task);
    }
}
