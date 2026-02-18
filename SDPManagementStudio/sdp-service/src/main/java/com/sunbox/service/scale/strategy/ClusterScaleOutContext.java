package com.sunbox.service.scale.strategy;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ConfScalingTask;

import java.util.List;

public class ClusterScaleOutContext {
    private ConfCluster confCluster;
    private ConfClusterHostGroup confClusterHostGroup;
    private List<ConfClusterVm> confClusterVms;
    private String vmUserName;
    //上一次执行的任务
    private ConfScalingTask lastFinishedTask;

    public ConfScalingTask getLastFinishedTask() {
        return lastFinishedTask;
    }

    public void setLastFinishedTask(ConfScalingTask lastFinishedTask) {
        this.lastFinishedTask = lastFinishedTask;
    }

    public ClusterScaleOutContext(String vmUserName) {
        this.vmUserName = vmUserName;
    }

    public String getVmUserName() {
        return vmUserName;
    }

    public void setConfCluster(ConfCluster confCluster) {
        this.confCluster = confCluster;
    }

    public ConfCluster getConfCluster() {
        return confCluster;
    }

    public void setConfClusterHostGroup(ConfClusterHostGroup hostGroup) {
        confClusterHostGroup = hostGroup;
    }

    public ConfClusterHostGroup getConfClusterHostGroup() {
        return confClusterHostGroup;
    }

    public void setConfClusterVms(List<ConfClusterVm> confClusterVms) {
        this.confClusterVms = confClusterVms;
    }

    public List<ConfClusterVm> getConfClusterVms() {
        return confClusterVms;
    }
}
