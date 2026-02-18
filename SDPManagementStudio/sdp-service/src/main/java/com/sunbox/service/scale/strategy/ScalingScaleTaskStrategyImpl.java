package com.sunbox.service.scale.strategy;

import com.sunbox.domain.ConfScalingTask;
import com.sunbox.domain.ConfScalingTaskVm;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 弹性扩缩容
 */
@Component
public class ScalingScaleTaskStrategyImpl extends ScaleTaskStrategy {
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

        // region 弹性缩容数据校验
        Integer realScaleCount = 0;
        if (task.getMaxCount() != null && task.getMinCount() != null) {
            if (runningVms.size() == 0) {
                resultMsg.setResultFail("当前没有可用的VM进行缩容,MinCount:" + task.getMinCount() +
                        ",MaxCount:" + task.getMaxCount());
                resultMsg.setErrorMsg("当前没有可用的VM进行缩容,MinCount:" + task.getMinCount() +
                        ",MaxCount:" + task.getMaxCount());
                //resultMsg.setRetcode("404");
                resultMsg.setResult(false);
                return resultMsg;
            }

            // 最大可用缩容数量
            Integer availableVmSize = runningVms.size() - task.getMinCount();
            if (availableVmSize <= 0) {
                resultMsg.setResultFail("弹性缩容后将没有可用的VM,runningVms:" + runningVms.size() +
                        ",MinCount:" + task.getMinCount() +
                        ",MaxCount:" + task.getMaxCount());
                resultMsg.setRetcode("400");
                return resultMsg;
            }

            // 实际缩容的数量
            if (task.getScalingCount().compareTo(availableVmSize) > 0) {
                // 大于可用机器
                realScaleCount = availableVmSize;
            } else {
                // 小于等于可用机器
                realScaleCount = task.getScalingCount();
            }

            int validateExpectCount = runningVms.size() - realScaleCount;
            // 缩容只考虑下线 扩容只考虑上限
            if (task.getScalingType().equals(ConfScalingTask.ScaleType_OUT)) {
                if (validateExpectCount > task.getMaxCount()) {
                    resultMsg.setResultFail("弹性扩容数量验证未通过,runningVms:" + runningVms.size() +
                            ",realScaleCount:" + realScaleCount +
                            ",validateExpectCount:" + validateExpectCount +
                            ",MinCount:" + task.getMinCount() +
                            ",MaxCount:" + task.getMaxCount());
                    resultMsg.setRetcode("401");
                    return resultMsg;
                }
            } else if (task.getScalingType().equals(ConfScalingTask.ScaleType_IN)) {
                if (validateExpectCount < task.getMinCount()) {
                    resultMsg.setResultFail("弹性缩容数量验证未通过,runningVms:" + runningVms.size() +
                            ",realScaleCount:" + realScaleCount +
                            ",validateExpectCount:" + validateExpectCount +
                            ",MinCount:" + task.getMinCount() +
                            ",MaxCount:" + task.getMaxCount());
                    resultMsg.setRetcode("401");
                    return resultMsg;
                }
            } else {
                resultMsg.setResultFail("未知的缩容类型" + task.getScalingType() + ",taskId:" + task.getTaskId());
                resultMsg.setRetcode("400");
                return resultMsg;
            }

            // 修正数据
            ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
            confScalingTaskVm.setTaskId(task.getTaskId());
            confScalingTaskVm.setCount(realScaleCount);
            confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
            getLogger().info("adjust scale rule clusterId:{},groupName:{},confScalingTaskVm:{}",
                    task.getClusterId(),
                    task.getGroupName(),
                    confScalingTaskVm);

            task.setScalingCount(realScaleCount);
            task.setBeforeScalingCount(runningVms.size());
            task.setAfterScalingCount(validateExpectCount);
            task.setExpectCount(task.getAfterScalingCount());
            confScalingTaskNeoMapper.updateByPrimaryKey(task);
            getLogger().info("adjustScaleInCount scale rule task:", task);
        }
        return resultMsg;
    }
}
