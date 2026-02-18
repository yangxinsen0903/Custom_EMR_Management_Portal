package com.sunbox.sdpspot.model;

import com.sunbox.domain.InfoSpotGroupScaleTaskItem;

import java.util.ArrayList;
import java.util.List;

public class GroupEvictionEventCache {
    private final String clusterId;

    private final String groupId;

    private List<InfoSpotGroupScaleTaskItem> vmEvictionEvents;

    public GroupEvictionEventCache(String clusterId, String groupId) {
        this.clusterId = clusterId;
        this.groupId = groupId;
    }

    public GroupEvictionEventCache(String clusterId, String groupId, List<InfoSpotGroupScaleTaskItem> vmEvictionEvents) {
        this.clusterId = clusterId;
        this.groupId = groupId;
        this.vmEvictionEvents = vmEvictionEvents;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<InfoSpotGroupScaleTaskItem> getVmEvictionEvents() {
        return vmEvictionEvents;
    }

    @Override
    public String toString() {
        return "GroupEvictionEventCache{" +
                "clusterId='" + clusterId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", vmEvictionEvents=" + vmEvictionEvents +
                '}';
    }
}
