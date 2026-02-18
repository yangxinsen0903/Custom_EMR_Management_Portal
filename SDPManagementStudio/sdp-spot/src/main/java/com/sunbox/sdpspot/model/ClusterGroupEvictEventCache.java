package com.sunbox.sdpspot.model;

import com.sunbox.domain.InfoClusterVm;

import java.util.ArrayList;
import java.util.List;

public class ClusterGroupEvictEventCache {
    private final String clusterId;

    private final String groupId;

    private final List<InfoClusterVm> infoClusterVms = new ArrayList<>();

    public ClusterGroupEvictEventCache(String clusterId, String groupId) {
        this.clusterId = clusterId;
        this.groupId = groupId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<InfoClusterVm> getInfoClusterVms() {
        return infoClusterVms;
    }
}
