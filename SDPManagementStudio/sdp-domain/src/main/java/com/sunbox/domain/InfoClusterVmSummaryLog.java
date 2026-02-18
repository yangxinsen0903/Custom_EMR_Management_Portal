package com.sunbox.domain;

import java.util.Date;

public class InfoClusterVmSummaryLog {
    private String cid;

    private String clusterId;

    private String groupId;

    private String changeReason;

    private Integer changeCount;

    private Integer beforeChangeCount;

    private Integer afterChangeCount;

    private Date createTime;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason == null ? null : changeReason.trim();
    }

    public Integer getChangeCount() {
        return changeCount;
    }

    public void setChangeCount(Integer changeCount) {
        this.changeCount = changeCount;
    }

    public Integer getBeforeChangeCount() {
        return beforeChangeCount;
    }

    public void setBeforeChangeCount(Integer beforeChangeCount) {
        this.beforeChangeCount = beforeChangeCount;
    }

    public Integer getAfterChangeCount() {
        return afterChangeCount;
    }

    public void setAfterChangeCount(Integer afterChangeCount) {
        this.afterChangeCount = afterChangeCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "InfoClusterVmSummaryLog{" +
                "cid='" + cid + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", changeReason='" + changeReason + '\'' +
                ", changeCount=" + changeCount +
                ", beforeChangeCount=" + beforeChangeCount +
                ", afterChangeCount=" + afterChangeCount +
                ", createTime=" + createTime +
                '}';
    }
}