package com.sunbox.sdptask.manager;

import com.sunbox.dao.mapper.ConfClusterHostGroupNeoMapper;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterSplitTask;
import com.sunbox.domain.manager.UpdateStateManager;
import com.sunbox.sdptask.consts.ComposeConstant;
import com.sunbox.sdptask.mapper.ConfClusterMapper;
import com.sunbox.sdptask.mapper.ConfClusterSplitTaskMapper;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposeConfClusterManager implements UpdateStateManager, BaseCommonInterFace {
    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfClusterMapper clusterMapper;

    @Autowired
    private ConfClusterHostGroupNeoMapper confClusterHostGroupNeoMapper;

    @Autowired
    private ConfClusterSplitTaskMapper confClusterSplitTaskMapper;

    @Override
    public void updateConfClusterState(String clusterId, Integer destinationState) {
        if (destinationState.equals(ConfCluster.FAILED)) {
            ConfCluster confCluster = new ConfCluster();
            confCluster.setClusterId(clusterId);
            confCluster.setState(destinationState);
            getLogger().info("updateClusterState clusterId:{},to:{},confCluster:{}",
                    clusterId,
                    destinationState,
                    confCluster);
            clusterMapper.updateByPrimaryKeySelective(confCluster);
            return;
        }

        if (hasUnfinishedSplitTask(clusterId)) {
            ConfCluster confCluster = new ConfCluster();
            confCluster.setClusterId(clusterId);
            confCluster.setState(destinationState);
            confCluster.setCreationSubState(ConfCluster.CreationSubState.RUNNING.getValue());
            getLogger().info("skip updateClusterState because has confClusterSpitTask clusterId:{},to:{},update creationSubState to:{},confCluster:{}",
                    clusterId,
                    destinationState,
                    ConfCluster.CreationSubState.RUNNING.getValue(),
                    confCluster);
            clusterMapper.updateByPrimaryKeySelective(confCluster);
        } else if (destinationState.equals(ConfCluster.CREATED)) {
            ConfCluster confCluster = new ConfCluster();
            confCluster.setClusterId(clusterId);
            confCluster.setState(destinationState);
            confCluster.setCreationSubState(ConfCluster.CreationSubState.FINISHED.getValue());
            getLogger().info("updateClusterState CREATED clusterId:{},to:{},confCluster:{}",
                    clusterId,
                    destinationState,
                    confCluster);
            clusterMapper.updateByPrimaryKeySelective(confCluster);
        } else {
            ConfCluster confCluster = new ConfCluster();
            confCluster.setClusterId(clusterId);
            confCluster.setState(destinationState);
            confCluster.setCreationSubState(ConfCluster.CreationSubState.FINISHED.getValue());
            getLogger().info("updateClusterState OTHER clusterId:{},to:{},confCluster:{}",
                    clusterId,
                    destinationState,
                    confCluster);
            clusterMapper.updateByPrimaryKeySelective(confCluster);
        }
    }

    public boolean hasUnfinishedSplitTask(String clusterId) {
        if (0 < confClusterSplitTaskMapper.countByClusterIdAndState(clusterId,
                ConfClusterSplitTask.State.WAITING.getValue(),
                ConfClusterSplitTask.State.RUNNING.getValue())) {
            return true;
        }
        return false;
    }

    /**
     * 将HostGroup信息保存到Redis的队列中,放到Redis中后,会被优先处理,否者会等候5轮后再处理
     * @param clusterId
     * @param groupName
     */
    public void sendDaemonTaskSignal(String clusterId, String groupName) {
        String queueKey = clusterId + ":" + groupName;
        redisLock.trySave(getLogger(), ComposeConstant.compose_cluster_vmrole_list + queueKey, "1");
    }

    /**
     * 检查HostGroup是否在Redis的队列中
     * @param clusterId
     * @param groupName
     * @return
     */
    public boolean checkHostgroupInQueue(String clusterId, String groupName) {
        String queueKey = clusterId + ":" + groupName;
        return redisLock.haveKey(ComposeConstant.compose_cluster_vmrole_list + queueKey);
    }

    /**
     * 将HostGroup从Redis的队列中删除
     * @param clusterId
     * @param groupName
     */
    public void deleteHostgroupFromQueue(String clusterId, String groupName) {
        String queueKey = clusterId + ":" + groupName;
        redisLock.delete(ComposeConstant.compose_cluster_vmrole_list + queueKey);
    }
}
