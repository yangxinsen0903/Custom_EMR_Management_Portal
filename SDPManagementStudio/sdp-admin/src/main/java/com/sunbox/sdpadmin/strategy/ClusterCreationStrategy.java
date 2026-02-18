package com.sunbox.sdpadmin.strategy;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;
import com.sunbox.web.BaseCommonInterFace;

public interface ClusterCreationStrategy extends BaseCommonInterFace {
    ResultMsg createCluster(String clusterId, AdminSaveClusterRequest request);

    ResultMsg createAndStartPlan(String clusterId, String releaseVer, ConfCluster.CreationMode creationMode) ;

}
