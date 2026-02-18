package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterVmIndexHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterVmIndexHistoryMapper {
    InfoClusterVmIndexHistory findByClusterIdAndVmRoleAndTaskId(String clusterId, String vmRole, String taskId);

    void updateHistory(InfoClusterVmIndexHistory infoClusterVmIndex);

    void insertHistory(InfoClusterVmIndexHistory infoClusterVmIndex);
}
