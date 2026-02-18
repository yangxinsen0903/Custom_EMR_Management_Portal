package com.sunbox.sdpcompose.model.azure.request;

import java.util.List;

public class UpdateClusterConfigData {

    private String clusterId;

    private String groupName;

    private String groupId;

    private List<ConfigProperties> clusterConfigs;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ConfigProperties> getClusterConfigs() {
        return clusterConfigs;
    }

    public void setClusterConfigs(List<ConfigProperties> clusterConfigs) {
        this.clusterConfigs = clusterConfigs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
