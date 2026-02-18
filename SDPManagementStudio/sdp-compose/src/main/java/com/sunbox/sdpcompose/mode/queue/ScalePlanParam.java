package com.sunbox.sdpcompose.mode.queue;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ScalePlanParam {
    private String clusterId;
    private String clusterReleaseVer;
    private String planOpScaleOut;
    private String taskId;
    private String vmRole;
    private String groupName;
    private String createTime;

    public ScalePlanParam(String clusterId, String clusterReleaseVer, String planOpScaleOut, String taskId, String vmRole, String groupName, String createTime) {
        this.clusterId = clusterId;
        this.clusterReleaseVer = clusterReleaseVer;
        this.planOpScaleOut = planOpScaleOut;
        this.taskId = taskId;
        this.vmRole = vmRole;
        this.groupName = groupName;
        this.createTime = createTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterReleaseVer() {
        return clusterReleaseVer;
    }

    public void setClusterReleaseVer(String clusterReleaseVer) {
        this.clusterReleaseVer = clusterReleaseVer;
    }

    public String getPlanOpScaleOut() {
        return planOpScaleOut;
    }

    public void setPlanOpScaleOut(String planOpScaleOut) {
        this.planOpScaleOut = planOpScaleOut;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public static String getScaleTaskQueueKey(String queuefix, String queueName) {
        return StrUtil.format("{}:{}", queuefix, queueName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clusterId", clusterId)
                .append("clusterReleaseVer", clusterReleaseVer)
                .append("planOpScaleOut", planOpScaleOut)
                .append("taskId", taskId)
                .append("vmRole", vmRole)
                .append("groupName", groupName)
                .append("createTime", createTime)
                .toString();
    }
}
