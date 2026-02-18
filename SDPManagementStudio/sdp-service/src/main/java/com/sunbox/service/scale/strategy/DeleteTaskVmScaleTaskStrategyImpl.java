package com.sunbox.service.scale.strategy;

import com.sunbox.domain.ConfScalingTask;
import com.sunbox.domain.ConfScalingTaskVm;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeleteTaskVmScaleTaskStrategyImpl extends ScaleTaskStrategy {
    @Override
    public ResultMsg adjustScaleInCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale in count, task:{}", task);
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);

        List<InfoClusterVm> scalingVms = new ArrayList<>();

        List<InfoClusterVm> runningVms =
                infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                        task.getClusterId(),
                        task.getGroupName(),
                        InfoClusterVm.VM_RUNNING);

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
            resultMsg.setResultFail("没有需要销毁的VM");
            resultMsg.setErrorMsg("没有需要销毁的VM");
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
        task.setExpectCount(actualAfterCount);
        getLogger().info("adjust scale in count, vms size:{}, expect count:{}, actualScalingInCount:{}, clusterId:{}, groupName:{}",
                runningVms.size(),
                task.getExpectCount(),
                actualScalingInCount,
                task.getClusterId(),
                task.getGroupName());
        confScalingTaskNeoMapper.updateByPrimaryKey(task);
        return resultMsg;
    }
}
