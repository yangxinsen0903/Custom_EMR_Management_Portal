package com.sunbox.domain;

public class InfoClusterVmKey {
    private String clusterId;

    private String vmName;

    public static InfoClusterVmKey of(String clusterId, String vmName) {
        InfoClusterVmKey vmKey = new InfoClusterVmKey();
        vmKey.setClusterId(clusterId);
        vmKey.setVmName(vmName);
        return vmKey;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName == null ? null : vmName.trim();
    }
}