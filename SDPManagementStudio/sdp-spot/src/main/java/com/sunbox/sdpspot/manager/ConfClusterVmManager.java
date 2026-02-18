package com.sunbox.sdpspot.manager;

import com.sunbox.domain.ConfClusterVm;
import com.sunbox.sdpspot.mapper.ConfClusterVmMapper;
import com.sunbox.sdpspot.model.ClusterVmNode;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ConfClusterVmManager implements BaseCommonInterFace {
    @Autowired
    ConfClusterVmMapper confClusterHostGroupMapper;

    public List<ClusterVmNode> listRunningClusterVmNodes() {
        List<ConfClusterVm> confClusterVms = confClusterHostGroupMapper.listByPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT, ConfClusterVm.STATE_RUNNING);

        List<ClusterVmNode> clusterGroups = new ArrayList<>();
        for (ConfClusterVm confClusterVm : confClusterVms) {
            ClusterVmNode clusterGroup = new ClusterVmNode(confClusterVm.getClusterId(),
                    confClusterVm.getVmRole(),
                    confClusterVm.getGroupId(),
                    confClusterVm.getVmConfId(),
                    confClusterVm.getCount());
            clusterGroups.add(clusterGroup);
        }
        return clusterGroups;
    }
}
