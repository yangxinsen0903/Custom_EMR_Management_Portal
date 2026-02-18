package com.sunbox.domain;

/**
    * 集群标签表; 1*标签数量
    */
public class ConfClusterTag {
    /**
    * 集群ID
    */
    private String clusterId;

    /**
    * 标签组
    */
    private String tagGroup;

    /**
    * 标签值
    */
    private String tagVal;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTagGroup() {
        return tagGroup;
    }

    public void setTagGroup(String tagGroup) {
        this.tagGroup = tagGroup;
    }

    public String getTagVal() {
        return tagVal;
    }

    public void setTagVal(String tagVal) {
        this.tagVal = tagVal;
    }
}