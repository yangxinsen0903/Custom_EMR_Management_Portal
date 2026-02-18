package com.sunbox.sdpcompose.model.azure.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

public class AzureResourceGroupTagsRequest {

    private String apiVersion;

    private String transactionId;

    private String region;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> tags;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tagName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tagValue;

    public AzureResourceGroupTagsRequest() {
    }

    public AzureResourceGroupTagsRequest(String apiVersion, String transactionId, String name, HashMap<String, Object> tags, String tagName, String tagValue) {
        this.apiVersion = apiVersion;
        this.transactionId = transactionId;
        this.name = name;
        this.tags = tags;
        this.tagName = tagName;
        this.tagValue = tagValue;
    }

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

    // Azure资源组的命名方式
    public String getName() {
        return "rg-sdp-" +name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}