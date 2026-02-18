package com.sunbox.sdptask.manager;

import com.sunbox.dao.mapper.ConfClusterHostGroupNeoMapper;
import com.sunbox.dao.mapper.ConfScalingTaskNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdptask.mapper.ConfClusterSplitTaskMapper;
import com.sunbox.sdptask.mapper.InfoClusterVmMapper;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Component
public class ClusterSplitCreationTaskManager implements BaseCommonInterFace {

    @Autowired
    ComposeConfClusterManager composeConfClusterManager;

    @Autowired
    InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    ConfClusterSplitTaskMapper confClusterSplitTaskMapper;

    @Autowired
    ConfClusterHostGroupNeoMapper confClusterHostGroupNeoMapper;

    @Autowired
    ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    ComposeService composeService;

    /**
     * 调用Compose服务接口, 创建一个扩容任务, 并将扩容任务的ID保存到增量创建任务的taskId字段里
     * @param confClusterSplitTask
     * @param executeScheduleConsumer
     * @return
     */
    public String startSplitTask(ConfClusterSplitTask confClusterSplitTask, Consumer<ConfScalingTask> executeScheduleConsumer) {
        getLogger().info("startSplitTask task:{}", confClusterSplitTask);

        if (Objects.equals(confClusterSplitTask.getState(), ConfClusterSplitTask.State.WAITING.getValue())) {
            //region waiting
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupNeoMapper.selectOneByGroupNameAndClusterId(confClusterSplitTask.getClusterId(),
                    confClusterSplitTask.getGroupName());
            if (confClusterHostGroup == null) {
                getLogger().error("not found ConfClusterHostGroup clusterId:{},groupName:{},confClusterSplitTask:{}",
                        confClusterSplitTask.getClusterId(),
                        confClusterSplitTask.getGroupName(),
                        confClusterSplitTask);
                confClusterSplitTask.setState(ConfClusterSplitTask.State.FAILURE.getValue());
                confClusterSplitTask.setRemark("没有找到实例组" + confClusterSplitTask.getGroupName());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                getLogger().info("update confClusterSplitTask:{}", confClusterSplitTask);
                composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
                return confClusterSplitTask.getTaskId();
            }

            if (Objects.equals(confClusterHostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                confClusterHostGroup.setExpectCount(confClusterHostGroup.getExpectCount() + confClusterSplitTask.getScalingOutCount());
                confClusterHostGroup.setModifiedby("split-create");
                confClusterHostGroup.setModifiedTime(new Date());
                confClusterHostGroupNeoMapper.updateByPrimaryKeySelective(confClusterHostGroup);
                getLogger().info("update confClusterHostGroup:{}",  confClusterHostGroup);

                getLogger().info("createScaleOutTask success,update group expectCount because group is spot group,confClusterSplitTask:{}",
                        confClusterSplitTask);
                confClusterSplitTask.setState(ConfClusterSplitTask.State.SUCCESS.getValue());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTask.setTaskId("NONE_SPOT_GROUP");
                confClusterSplitTask.setTaskType(ConfClusterSplitTask.TaskType.SCALE_OUT_TASK.getValue());
                confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                getLogger().info("update confClusterSplitTask:{}",  confClusterSplitTask);
                composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
                return confClusterSplitTask.getId();
            }

            //region not spot group
            List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                    confClusterSplitTask.getClusterId(),
                    confClusterSplitTask.getGroupName(),
                    InfoClusterVm.VM_RUNNING);

            int afterScaleCount = infoClusterVms.size() + confClusterSplitTask.getScalingOutCount();

            ConfScalingTask confScalingTask = new ConfScalingTask();
            confScalingTask.setTaskId(confClusterSplitTask.getId());
            confScalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
            confScalingTask.setEsRuleId(null);
            confScalingTask.setEsRuleName(null);
            confScalingTask.setBeforeScalingCount(infoClusterVms.size());
            confScalingTask.setAfterScalingCount(afterScaleCount);
            confScalingTask.setScalingCount(confClusterSplitTask.getScalingOutCount());
            confScalingTask.setExpectCount(afterScaleCount);
            confScalingTask.setIsGracefulScalein(null);
            confScalingTask.setScaleinWaitingtime(null);
            confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_Scaling);
            confScalingTask.setEnableAfterstartScript(confClusterHostGroup.getEnableAfterstartScript());
            confScalingTask.setEnableBeforestartScript(confClusterHostGroup.getEnableBeforestartScript());
            confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingTask.setBegTime(new Date());
            confScalingTask.setVmRole(confClusterSplitTask.getVmRole().toLowerCase());
            confScalingTask.setClusterId(confClusterSplitTask.getClusterId());
            confScalingTask.setGroupName(confClusterHostGroup.getGroupName());
            confScalingTask.setCreatedBy("split-task");
            confScalingTask.setRemark(String.format("创建集群[%d台/%d批]",
                    confClusterSplitTask.getScalingOutCount(),
                    confClusterSplitTask.getSortIndex()));
            confScalingTask.setCreateTime(new Date());
            ResultMsg scaleOutTaskResult = composeService.createScaleOutTask(confScalingTask);
            if (!scaleOutTaskResult.isResult()) {
                getLogger().error("createScaleOutTask error,scaleOutTask confClusterSplitTask:{},confScalingTask:{},errorMsg:{}",
                        confClusterSplitTask,
                        confScalingTask,
                        scaleOutTaskResult.getErrorMsg());
                confClusterSplitTask.setState(ConfClusterSplitTask.State.FAILURE.getValue());
                confClusterSplitTask.setRemark(scaleOutTaskResult.getErrorMsg());
                confClusterSplitTask.setTaskId(confScalingTask.getTaskId());
                confClusterSplitTask.setTaskType(ConfClusterSplitTask.TaskType.SCALE_OUT_TASK.getValue());
                confClusterSplitTask.setModifiedTime(new Date());
            } else {
                getLogger().info("createScaleOutTask success,scaleOutTask confClusterSplitTask:{},confScalingTask:{}",
                        confClusterSplitTask,
                        confScalingTask);
                confClusterSplitTask.setState(ConfClusterSplitTask.State.RUNNING.getValue());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTask.setTaskId(confScalingTask.getTaskId());
                confClusterSplitTask.setTaskType(ConfClusterSplitTask.TaskType.SCALE_OUT_TASK.getValue());
            }
            confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
            composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
            //endregion
            //endregion
        } else {
            //region running
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupNeoMapper.selectOneByGroupNameAndClusterId(confClusterSplitTask.getClusterId(),
                    confClusterSplitTask.getGroupName());
            if (confClusterHostGroup == null) {
                getLogger().error("not found ConfClusterHostGroup clusterId:{},groupName:{},confClusterSplitTask:{}",
                        confClusterSplitTask.getClusterId(),
                        confClusterSplitTask.getGroupName(),
                        confClusterSplitTask);
                confClusterSplitTask.setState(ConfClusterSplitTask.State.FAILURE.getValue());
                confClusterSplitTask.setRemark("没有找到实例组" + confClusterSplitTask.getGroupName());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                getLogger().info("update confClusterSplitTask:{}", confClusterSplitTask);
                composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
                return confClusterSplitTask.getTaskId();
            }

            ConfScalingTask confScalingTask = confScalingTaskNeoMapper.selectByPrimaryKey(confClusterSplitTask.getId());
            if (confScalingTask == null) {
                getLogger().error("not found confScalingTask clusterId:{},groupName:{},taskId:{}",
                        confClusterSplitTask.getClusterId(),
                        confClusterSplitTask.getGroupName(),
                        confClusterSplitTask.getId());
                confClusterSplitTask.setState(ConfClusterSplitTask.State.FAILURE.getValue());
                confClusterSplitTask.setRemark("没有找到扩容任务" + confClusterSplitTask.getGroupName());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                composeConfClusterManager.sendDaemonTaskSignal(confClusterSplitTask.getClusterId(), confClusterSplitTask.getGroupName());
                getLogger().info("update confClusterSplitTask to failure:{}", confClusterSplitTask);
                return confClusterSplitTask.getId();
            }

            //region not spot group
            if (Objects.equals(confClusterHostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                confClusterSplitTask.setState(ConfClusterSplitTask.State.SUCCESS.getValue());
                confClusterSplitTask.setModifiedTime(new Date());
                confClusterSplitTask.setTaskId("NONE_SPOT_GROUP");
                confClusterSplitTask.setTaskType(ConfClusterSplitTask.TaskType.SCALE_OUT_TASK.getValue());
                getLogger().info("createScaleOutTask success,update group expectCount because group is spot group,confClusterSplitTask:{}",
                        confClusterSplitTask);
                confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                composeConfClusterManager.sendDaemonTaskSignal(confClusterHostGroup.getClusterId(), confClusterHostGroup.getGroupName());
            } else {
                if (confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Failed)) {
                    getLogger().info("confScalingTask is failed,confClusterSplitTask:{},confScalingTask:{}",
                            confClusterSplitTask,
                            confScalingTask);
                    confClusterSplitTask.setState(ConfClusterSplitTask.State.FAILURE.getValue());
                    confClusterSplitTask.setModifiedTime(new Date());
                    confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);
                    composeConfClusterManager.sendDaemonTaskSignal(confClusterSplitTask.getClusterId(), confClusterSplitTask.getGroupName());
                } else if (confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Complete)) {
                    getLogger().info("confScalingTask is success,confClusterSplitTask:{},confScalingTask:{}",
                            confClusterSplitTask,
                            confScalingTask);
                    confClusterSplitTask.setState(ConfClusterSplitTask.State.SUCCESS.getValue());
                    confClusterSplitTask.setModifiedTime(new Date());
                    confClusterSplitTaskMapper.updateByPrimaryKeySelective(confClusterSplitTask);

                    getLogger().info("update confClusterSplitTask to success:{}", confClusterSplitTask);
                    composeConfClusterManager.sendDaemonTaskSignal(confClusterSplitTask.getClusterId(), confClusterSplitTask.getGroupName());
                } else if (confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Create)) {
                    getLogger().info("confScalingTask is created,try start plan,confClusterSplitTask:{},confScalingTask:{}",
                            confClusterSplitTask,
                            confScalingTask);
                    executeScheduleConsumer.accept(confScalingTask);
                }
                //endregion
            }
            //endregion
        }
        return confClusterSplitTask.getId();
    }
}
