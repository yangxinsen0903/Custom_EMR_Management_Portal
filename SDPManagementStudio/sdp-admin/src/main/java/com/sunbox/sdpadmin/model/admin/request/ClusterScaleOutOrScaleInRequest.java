package com.sunbox.sdpadmin.model.admin.request;

import javax.validation.constraints.NotEmpty;


public class ClusterScaleOutOrScaleInRequest {

    // 集群id
    @NotEmpty(message = "集群id不能为空")
    private String clusterId;

    // 实例类型
    @NotEmpty(message = "实例类型不能为空")
    private String vmRole;

    // 实例组名称
    @NotEmpty(message = "实例组名称不能为空")
    private String groupName;

    // 扩容到的数量
    private Integer scaleOutCount;

    // 缩容到的数量
    private Integer scaleInCount;

    // 期望数量
    private Integer expectCount;

    // 是否执行集群启动前脚本（1：执行，0：不执行）

    private Integer enableBeforestartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterstartScript;

    // 是否优雅缩容（1：是，0：否）
    private Integer isGracefulScalein;

    // 优雅缩容等待时间（60 ～1800）
    private Integer scaleinWaitingtime;

    /** 是否暴力缩容DataNode，1:暴力缩容 0：不暴力缩容 */
    private Integer forceScaleinDataNode;

    private String requestId;
    private Integer deleteGroup;
    private Integer scaleOperationType;
    private String scaleByTaskId;
    private String createdBy;
    private String user;

    public Integer getForceScaleinDataNode() {
        return forceScaleinDataNode;
    }

    public void setForceScaleinDataNode(Integer forceScaleinDataNode) {
        this.forceScaleinDataNode = forceScaleinDataNode;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getScaleOutCount() {
        return scaleOutCount;
    }

    public void setScaleOutCount(Integer scaleOutCount) {
        this.scaleOutCount = scaleOutCount;
    }

    public Integer getScaleInCount() {
        return scaleInCount;
    }

    public void setScaleInCount(Integer scaleInCount) {
        this.scaleInCount = scaleInCount;
    }

    public Integer getEnableBeforestartScript() {
        return enableBeforestartScript;
    }

    public void setEnableBeforestartScript(Integer enableBeforestartScript) {
        this.enableBeforestartScript = enableBeforestartScript;
    }

    public Integer getEnableAfterstartScript() {
        return enableAfterstartScript;
    }

    public void setEnableAfterstartScript(Integer enableAfterstartScript) {
        this.enableAfterstartScript = enableAfterstartScript;
    }

    public Integer getIsGracefulScalein() {
        return isGracefulScalein;
    }

    public void setIsGracefulScalein(Integer isGracefulScalein) {
        this.isGracefulScalein = isGracefulScalein;
    }

    public Integer getScaleinWaitingtime() {
        return scaleinWaitingtime;
    }

    public void setScaleinWaitingtime(Integer scaleinWaitingtime) {
        this.scaleinWaitingtime = scaleinWaitingtime;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getDeleteGroup() {
        return deleteGroup;
    }

    public void setDeleteGroup(Integer deleteGroup) {
        this.deleteGroup = deleteGroup;
    }

    public void setScaleOperationType(Integer scaleOperationType) {
        this.scaleOperationType = scaleOperationType;
    }

    public Integer getScaleOperationType() {
        return scaleOperationType;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClusterScaleOutOrScaleInRequest{");
        sb.append("clusterId='").append(clusterId).append('\'');
        sb.append(", vmRole='").append(vmRole).append('\'');
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", scaleOutCount=").append(scaleOutCount);
        sb.append(", scaleInCount=").append(scaleInCount);
        sb.append(", expectCount=").append(expectCount);
        sb.append(", enableBeforestartScript=").append(enableBeforestartScript);
        sb.append(", enableAfterstartScript=").append(enableAfterstartScript);
        sb.append(", isGracefulScalein=").append(isGracefulScalein);
        sb.append(", scaleinWaitingtime=").append(scaleinWaitingtime);
        sb.append(", forceScaleinDataNode=").append(forceScaleinDataNode);
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append(", deleteGroup=").append(deleteGroup);
        sb.append(", scaleOperationType=").append(scaleOperationType);
        sb.append(", scaleByTaskId='").append(scaleByTaskId).append('\'');
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setScaleByTaskId(String scaleByTaskId) {
        this.scaleByTaskId = scaleByTaskId;
    }

    public String getScaleByTaskId() {
        return scaleByTaskId;
    }
}
