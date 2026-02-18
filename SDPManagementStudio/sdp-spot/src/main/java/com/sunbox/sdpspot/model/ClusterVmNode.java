package com.sunbox.sdpspot.model;

public class ClusterVmNode {
    private final String clusterId;
    private final String vmRole;
    private final String groupId;
    private final String vmName;
    private final int insCount;

    public ClusterVmNode(String clusterId, String vmRole, String groupId, String vmName, int insCount) {
        this.clusterId = clusterId;
        this.vmRole = vmRole;
        this.groupId = groupId;
        this.vmName = vmName;
        this.insCount = insCount;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVmRole() {
        return vmRole;
    }

    public String getVmName() {
        return vmName;
    }

    public int getInsCount() {
        return insCount;
    }
}
