package com.sunbox.sdptask.service;

import com.sunbox.dao.mapper.ConfClusterHostGroupNeoMapper;
import com.sunbox.dao.mapper.ConfScalingTaskNeoMapper;
import com.sunbox.dao.mapper.ConfScalingTaskVmNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdptask.mapper.*;
import com.sunbox.service.scale.strategy.ComposeStrategyFactory;
import com.sunbox.service.scale.strategy.ScaleTaskStrategy;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author : [niyang]
 * @className : test
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/28 1:03 PM]
 */
@Service
public class ScalingService implements BaseCommonInterFace {
    @Autowired
    private ComposeStrategyFactory composeStrategyFactory;

    @Autowired
    private ConfScalingTaskVmNeoMapper confScalingTaskVmNeoMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    private ConfClusterHostGroupNeoMapper confClusterHostGroupNeoMapper;



    public ResultMsg adjustScaleInCount(ConfScalingTask task) {
        getLogger().info("begin adjust scale in count, task:{}", task);
        ScaleTaskStrategy adjustScaleTaskStrategy = composeStrategyFactory.createScaleTaskStrategy(task);
        return adjustScaleTaskStrategy.adjustScaleInCount(task);
    }


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
                    ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupNeoMapper.selectOneByGroupNameAndClusterId(task.getClusterId(), task.getGroupName());
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
                    confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
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
                    confScalingTaskNeoMapper.updateByPrimaryKey(task);
                    return resultMsg;
                } else {
                    int actualScalingOutCount = task.getExpectCount() - workvms.size();
                    ConfScalingTaskVm confScalingTaskVm = new ConfScalingTaskVm();
                    confScalingTaskVm.setTaskId(task.getTaskId());
                    confScalingTaskVm.setCount(actualScalingOutCount);
                    confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
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
                    confScalingTaskNeoMapper.updateByPrimaryKey(task);
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
            confScalingTaskVmNeoMapper.updateCount(confScalingTaskVm);
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
            confScalingTaskNeoMapper.updateByPrimaryKey(task);
        }
        return resultMsg;
    }
}
