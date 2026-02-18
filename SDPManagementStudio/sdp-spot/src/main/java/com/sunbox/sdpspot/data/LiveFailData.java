package com.sunbox.sdpspot.data;

public class LiveFailData {
    private String clusterId;
    private String vmName;
    private Integer failedCount;

    public LiveFailData() {
    }

    public LiveFailData(String clusterId, String vmName, Integer failedCount) {
        this.clusterId = clusterId;
        this.vmName = vmName;
        this.failedCount = failedCount;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }
}
