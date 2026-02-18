package com.sunbox.domain.cluster;


public class WorkOrderApprovalResponse extends WorkOrderApprovalRequest{

    /**
     * 集群名称
     */
    private String clusterName;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
