package com.sunbox.service.scale.strategy;

import com.sunbox.domain.ConfScalingTask;
import com.sunbox.domain.ConfScalingTaskVm;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 手动扩缩容
 */
@Component
public class ManualScaleTaskStrategyImpl extends ScaleTaskStrategy {
    @Override
    public ResultMsg adjustScaleInCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale in count, task:{}", task);
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        List<InfoClusterVm> runningVms =
                infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                        task.getClusterId(),
                        task.getGroupName(),
                        InfoClusterVm.VM_RUNNING);

        if (task.getExpectCount() != null) {
            if (runningVms.size() <= task.getExpectCount()) {
                getLogger().error("adjust scale in count error, vms size:{} less equal expect count:{}, clusterId:{}, groupName:{}",
                        runningVms.size(),
                        task.getExpectCount(),
                        task.getClusterId(),
                        task.getGroupName());
                resultMsg.setResultFail("当前没有可用的VM进行缩容,当前实例数(" + runningVms.size() + "),期望数量(" + task.getExpectCount() + ")");
                resultMsg.setErrorMsg("当前没有可用的VM进行缩容,当前实例数(" + runningVms.size() + "),期望数量(" + task.getExpectCount() + ")");
                //resultMsg.setRetcode("404");
                resultMsg.setResult(false);
                return resultMsg;
            } else {
                int actualScalingInCount = task.getScalingCount();//runningVms.size() - task.getExpectCount();
                if (actualScalingInCount > 0) {
                    ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
                    confScalingTaskVm.setTaskId(task.getTaskId());
                    confScalingTaskVm.setCount(actualScalingInCount);
                    confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
                    getLogger().info("adjust clusterId:{},groupName:{},confScalingTaskVm:{}",
                            task.getClusterId(),
                            task.getGroupName(),
                            confScalingTaskVm);

                    task.setScalingCount(actualScalingInCount);
                    task.setBeforeScalingCount(runningVms.size());
                    task.setAfterScalingCount(runningVms.size() - actualScalingInCount);
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
        }
        return resultMsg;
    }
}