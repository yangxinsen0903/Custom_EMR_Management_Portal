package com.sunbox.sdpspot.manager;

import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.sdpspot.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpspot.model.ClusterHostGroupNode;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ConfClusterHostGroupManager {
    public static List<ClusterHostGroupNode> listRunningClusterHostGroupNodes(Logger logger, ConfClusterHostGroupMapper confClusterHostGroupMapper) {
        List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.listByPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT, ConfClusterHostGroup.STATE_RUNNING);

        List<ClusterHostGroupNode> clusterHostGroupNodes = new ArrayList<>();
        for (ConfClusterHostGroup confClusterHostGroup : confClusterHostGroups) {
            ClusterHostGroupNode clusterHostGroupNode = new ClusterHostGroupNode(confClusterHostGroup.getClusterId(),
                    confClusterHostGroup.getVmRole(),
                    confClusterHostGroup.getGroupId(),
                    confClusterHostGroup.getGroupName(),
                    confClusterHostGroup.getExpectCount());
            clusterHostGroupNode.setSpotState(confClusterHostGroup.getSpotState());
            clusterHostGroupNodes.add(clusterHostGroupNode);
        }
        logger.debug("listRunningClusterHostGroupNodes size:{}", clusterHostGroupNodes.size());
        return clusterHostGroupNodes;
    }
}
