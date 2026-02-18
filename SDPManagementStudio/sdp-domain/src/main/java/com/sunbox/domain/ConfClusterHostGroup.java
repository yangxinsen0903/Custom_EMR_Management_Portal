package com.sunbox.domain;

import java.util.Date;

public class ConfClusterHostGroup {
    /**
     * 状态常量 //状态;0 待创建 1 创建中 2 运行中  -1释放中 -2 已释放,-3已删除
     *
     * @return
     */
    public static final Integer STATE_WAIT_CREATE = 0;
    public static final Integer STATE_CREATING = 1;
    public static final Integer STATE_RUNNING = 2;
    public static final Integer STATE_SCALEINING = 3;
    public static final Integer STATE_SCALEOUTING = 4;
    public static final Integer STATE_RELEASING = -1;
    public static final Integer STATE_RELEASED = -2;
    public static final Integer STATE_DELETED = -3;

    // todo  审核的几个状态
//    /** 创建审核中 */
//    public final static int CREATE_AUDITING = 3;
//    /** 创建审核拒绝 */
//    public final static int CREATE_AUDIT_REJECT = -5;
//    /** 删除审核中 */
//    public final static int DELETE_AUDITING = 4;
//    /** 删除审核拒绝 */
//    public final static int DELETE_AUDIT_REJECT = -6;


    // 关闭买入和缩容
    public static final Integer SPOTSTATE_CLOSEALL= 0;
    // 关闭买入，开启缩容
    public static final Integer SPOTSTATE_ONLY_SCALEIN = 1;
    // 开启买入，关闭缩容
    public static final Integer SPOTSTATE_ONLY_SCALEOUT = 2;
    // 开启买入和缩容
    public static final Integer SPOTSTATE_OPENALL =3;


    private String groupId;

    private String clusterId;

    private String groupName;

    private String vmRole;

    private Integer insCount;

    private Integer purchaseType;

    private Integer state;

    private String yarnQueue;

    private String ambariConfigGroup;

    private String createdby;

    private Date createdTime;

    private String modifiedby;

    private Date modifiedTime;

    private Integer maxUnit;

    private Integer minUnit;
    private Integer enableAfterstartScript;
    private Integer enableBeforestartScript;
    private Integer expectCount;

    private Integer spotState;

    public Integer getSpotState() {
        return spotState;
    }

    public void setSpotState(Integer spotState) {
        this.spotState = spotState;
    }

    public Integer getMaxUnit() {
        return maxUnit;
    }

    public void setMaxUnit(Integer maxUnit) {
        this.maxUnit = maxUnit;
    }

    public Integer getMinUnit() {
        return minUnit;
    }

    public void setMinUnit(Integer minUnit) {
        this.minUnit = minUnit;
    }

    public Integer getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getAmbariConfigGroup() {
        return ambariConfigGroup;
    }

    public void setAmbariConfigGroup(String ambariConfigGroup) {
        this.ambariConfigGroup = ambariConfigGroup;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole == null ? null : vmRole.trim();
    }

    public Integer getInsCount() {
        return insCount;
    }

    public void setInsCount(Integer insCount) {
        this.insCount = insCount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getYarnQueue() {
        return yarnQueue;
    }

    public void setYarnQueue(String yarnQueue) {
        this.yarnQueue = yarnQueue == null ? null : yarnQueue.trim();
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby == null ? null : modifiedby.trim();
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setEnableAfterstartScript(Integer enableAfterstartScript) {
        this.enableAfterstartScript = enableAfterstartScript;
    }

    public Integer getEnableAfterstartScript() {
        return enableAfterstartScript;
    }

    public void setEnableBeforestartScript(Integer enableBeforestartScript) {
        this.enableBeforestartScript = enableBeforestartScript;
    }

    public Integer getEnableBeforestartScript() {
        return enableBeforestartScript;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    @Override
    public String toString() {
        return "ConfClusterHostGroup{" +
                "groupId='" + groupId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", insCount=" + insCount +
                ", purchaseType=" + purchaseType +
                ", state=" + state +
                ", yarnQueue='" + yarnQueue + '\'' +
                ", ambariConfigGroup='" + ambariConfigGroup + '\'' +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedby='" + modifiedby + '\'' +
                ", modifiedTime=" + modifiedTime +
                ", maxUnit=" + maxUnit +
                ", minUnit=" + minUnit +
                ", enableAfterstartScript=" + enableAfterstartScript +
                ", enableBeforestartScript=" + enableBeforestartScript +
                ", expectCount=" + expectCount +
                ", spotState=" + spotState +
                '}';
    }
}