package com.sunbox.sdpcompose.model.azure.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class AzureResourceGroupAddTagsRequest {

    private String apiVersion;

    private String transactionId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    private List<ResourceGroupTag> tags;

    private List<String> tagNames;

    private String region;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResourceGroupTag> getTags() {
        return tags;
    }

    public void setTags(List<ResourceGroupTag> tags) {
        this.tags = tags;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public static class ResourceGroupTag {

        private String tagName;

        private String tagValue;

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public String getTagValue() {
            return tagValue;
        }

        public void setTagValue(String tagValue) {
            this.tagValue = tagValue;
        }
    }
}