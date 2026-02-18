package com.sunbox.sdpcompose.manager;

import cn.hutool.log.Log;
import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfScalingTask;
import com.sunbox.sdpcompose.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpcompose.mapper.ConfScalingTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ClusterHostGroupManager {
    private Logger logger;
    private ConfScalingTaskMapper confScalingTaskMapper;
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    public ClusterHostGroupManager(Logger logger,
                                   ConfScalingTaskMapper confScalingTaskMapper,
                                   ConfClusterHostGroupMapper confClusterHostGroupMapper) {
        this.logger = logger;
        this.confScalingTaskMapper = confScalingTaskMapper;
        this.confClusterHostGroupMapper = confClusterHostGroupMapper;
    }

    public void updateGroupExpectCount(String taskId) {
        if(StringUtils.isEmpty(taskId)) {
            return;
        }

        logger.info("begin updateGroupExpectCount taskId:{}", taskId);
        try {
            ConfScalingTask confScalingTask = this.confScalingTaskMapper.selectByPrimaryKey(taskId);

            if (confScalingTask == null) {
                logger.info("updateGroupExpectCount error confScalingTask is null,taskId:{}", taskId);
                return;
            }

            if (!confScalingTask.getScalingType().equals(ConfScalingTask.ScaleType_IN)) {
                logger.info("updateGroupExpectCount error confScalingTask scalingType not eq ConfScalingTask.ScaleType_IN({}),taskId:{}",
                        ConfScalingTask.ScaleType_IN,
                        taskId);
                return;
            }

            if (confScalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_spot)) {
                logger.info("updateGroupExpectCount error confScalingTask OperationType eq ConfScalingTask.Operation_type_spot({}),taskId:{}",
                        ConfScalingTask.Operation_type_spot,
                        taskId);
                return;
            }

            if (confScalingTask.getOperatiionType().equals(ConfScalingTask.Operation_type_delete_Task_Vm)) {
                logger.info("updateGroupExpectCount error confScalingTask OperationType eq ConfScalingTask.Operation_type_delete_Task_Vm({}),taskId:{}",
                        ConfScalingTask.Operation_type_delete_Task_Vm,
                        taskId);
                return;
            }

            ConfClusterHostGroup confClusterHostGroup = this.confClusterHostGroupMapper.selectOneByGroupNameAndClusterId(confScalingTask.getClusterId(),
                    confScalingTask.getGroupName());
            if (confClusterHostGroup == null) {
                logger.info("updateGroupExpectCount error confClusterHostGroup is null,task:{}", confScalingTask);
                return;
            }

            // 竞价实例的缩容需要修改实例组的期望值
            Integer beforeExpectCount = confClusterHostGroup.getExpectCount();
            logger.error("updateExpectCount clusterId:{},groupId:{},beforeExpectCount:{},afterExpectCount:{}",
                    confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    beforeExpectCount,
                    confScalingTask.getExpectCount());
            this.confClusterHostGroupMapper.updateExpectCount(confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getGroupId(),
                    confScalingTask.getExpectCount());
        } catch (Exception e) {
            logger.error("updateGroupExpectCount error,taskId:{}", taskId, e);
        }
    }
}
