package com.sunbox.sdpadmin.model.admin.request;

import java.util.List;

public class InstanceGroupVersion {
    private List<ClusterApp> clusterApps;
    private String clusterReleaseVer;

    public List<ClusterApp> getClusterApps() { return clusterApps; }
    public void setClusterApps(List<ClusterApp> value) { this.clusterApps = value; }

    public String getClusterReleaseVer() { return clusterReleaseVer; }
    public void setClusterReleaseVer(String value) { this.clusterReleaseVer = value; }
}