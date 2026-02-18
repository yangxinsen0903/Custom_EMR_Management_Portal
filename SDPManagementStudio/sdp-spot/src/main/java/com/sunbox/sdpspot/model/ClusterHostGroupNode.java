package com.sunbox.sdpspot.model;

import com.sunbox.domain.InfoClusterVm;

import java.util.List;

public class ClusterHostGroupNode {
    private final String clusterId;
    private final String vmRole;
    private final String groupId;
    private final String groupName;
    private final Integer expectCount;
    private Integer spotState;

    private List<InfoClusterVm> vmNodes;
    private List<InfoClusterVm> unknownVmNodes;

    public ClusterHostGroupNode(String clusterId, String vmRole, String groupId, String groupName, Integer expectCount) {
        this.clusterId = clusterId;
        this.vmRole = vmRole;
        this.groupId = groupId;
        this.groupName = groupName;
        this.expectCount = expectCount;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getVmRole() {
        return vmRole;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public Integer getSpotState() {
        return spotState;
    }

    public void setSpotState(Integer spotState) {
        this.spotState = spotState;
    }

    public void setVmNodes(List<InfoClusterVm> vmNodes) {
        this.vmNodes = vmNodes;
    }

    public List<InfoClusterVm> getVmNodes() {
        return vmNodes;
    }

    public List<InfoClusterVm> getUnknownVmNodes() {
        return unknownVmNodes;
    }

    public void setUnknownVmNodes(List<InfoClusterVm> unknownVmNodes) {
        this.unknownVmNodes = unknownVmNodes;
    }
}
