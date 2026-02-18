package com.sunbox.domain.manager;

public interface UpdateStateManager {
    /**
     * 更新集群状态<br/>
     * 集群状态变更时, 同时需要更新子状态,子状态与增量创建在关. 如果集群是增量创建且没执行完,子状态会自动变更为运行中
     * @param clusterId 集群ID
     * @param destinationState 需要变更的集群状态
     */
    void updateConfClusterState(String clusterId, Integer destinationState);
}
