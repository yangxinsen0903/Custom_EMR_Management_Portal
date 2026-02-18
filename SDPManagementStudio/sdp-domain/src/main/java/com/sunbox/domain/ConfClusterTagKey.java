package com.sunbox.domain;

public class ConfClusterTagKey {
    private String clusterId;

    private String tagGroup;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getTagGroup() {
        return tagGroup;
    }

    public void setTagGroup(String tagGroup) {
        this.tagGroup = tagGroup == null ? null : tagGroup.trim();
    }
}