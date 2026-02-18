package com.sunbox.sdpcompose.model.azure.response;

import java.util.Map;

public class ResourceGroupResponse {

    public String clusterId;

    public Map<String, String> tags;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
