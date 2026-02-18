package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterVmIndex;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterVmIndexMapper {
    InfoClusterVmIndex findByClusterIdAndVmRole(String clusterId, String vmRole);

    void updateEndIndex(InfoClusterVmIndex infoClusterVmIndex);

    void insertEndIndex(InfoClusterVmIndex infoClusterVmIndex);

    InfoClusterVmIndex findByClusterIdAndTaskId(String clusterId,String taskId);
}
