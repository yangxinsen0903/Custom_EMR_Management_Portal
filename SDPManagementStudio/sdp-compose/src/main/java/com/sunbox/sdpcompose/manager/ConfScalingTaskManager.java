package com.sunbox.sdpcompose.manager;

import com.sunbox.domain.ConfScalingTask;
import com.sunbox.sdpcompose.mapper.ConfScalingTaskMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConfScalingTaskManager {
    @Autowired
    ConfScalingTaskMapper confScalingTaskMapper;

    /**
     * 检查集群是否存在已创建或正在运行的任务
     *
     * @param clusterId
     * @return true：存在任务，false：不存在任务
     */
    public boolean hasCreatedOrRunningScalingTask(String clusterId) {
        List<Integer> states = new ArrayList<>();
        states.add(ConfScalingTask.SCALINGTASK_Create);
        states.add(ConfScalingTask.SCALINGTASK_Running);
        int count = confScalingTaskMapper.countByClusterIdAndStates(clusterId, states);
        if (count > 0) {
            return true;
        }
        return false;
    }
}
