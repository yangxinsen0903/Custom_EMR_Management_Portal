/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.model.azure.fleet.response;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.sdpcompose.model.azure.fleet.request.RegularProfile;
import com.sunbox.sdpcompose.model.azure.fleet.request.SpotProfile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Azure Api返回的Azure Fleet详细信息, 接口: /api/v1/fleet/{cluster}/{group}
 * @author wangda
 * @date 2024/7/10
 */
public class AzureFleetInfo {
    private String id;
    private String type;
    private List<String> zones;
    private JSONObject tags;

    private JSONObject identity;

    private AzureFleetInfoProperties properties;

    public Integer getVmCapacity() {
        SpotProfile spotPriorityProfile = properties.getSpotPriorityProfile();
        if (Objects.isNull(spotPriorityProfile)) {
            RegularProfile regularPriorityProfile = properties.getRegularPriorityProfile();
            if (Objects.nonNull(regularPriorityProfile)) {
                return regularPriorityProfile.getCapacity();
            }
            throw new RuntimeException("Azure接口返回的AzureFleet信息中,没有容量数据");
        } else {
            return spotPriorityProfile.getCapacity();
        }
    }
    /** 是否有SpotProfile */
    public boolean hasSpotProfile() {
        return Objects.nonNull(properties.getSpotPriorityProfile());
    }
    public Integer getSpotVmCapacity() {
        SpotProfile spotPriorityProfile = properties.getSpotPriorityProfile();
        if (Objects.nonNull(spotPriorityProfile)) {
            return spotPriorityProfile.getCapacity();
        }
        throw new RuntimeException("Azure接口返回的AzureFleet信息中,没有Spot容量数据");
    }

    /** 是否有RegularProfile */
    public boolean hasRegularProfile() {
        return Objects.nonNull(properties.getRegularPriorityProfile());
    }

    public Integer getRegularVmCapacity() {
        RegularProfile regularPriorityProfile = properties.getRegularPriorityProfile();
        if (Objects.nonNull(regularPriorityProfile)) {
            return regularPriorityProfile.getCapacity();

        }
        throw new RuntimeException("Azure接口返回的AzureFleet信息中,没有Regular容量数据");
    }

    public AzureFleetInfoProperties getProperties() {
        return properties;
    }

    public void setProperties(AzureFleetInfoProperties properties) {
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getZones() {
        return zones;
    }

    public void setZones(List<String> zones) {
        this.zones = zones;
    }

    public JSONObject getTags() {
        return tags;
    }

    public void setTags(JSONObject tags) {
        this.tags = tags;
    }

    public JSONObject getIdentity() {
        return identity;
    }

    public void setIdentity(JSONObject identity) {
        this.identity = identity;
    }
}
