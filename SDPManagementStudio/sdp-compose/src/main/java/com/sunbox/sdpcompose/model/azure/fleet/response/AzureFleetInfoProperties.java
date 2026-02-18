/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.model.azure.fleet.response;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.sdpcompose.model.azure.fleet.request.RegularProfile;
import com.sunbox.sdpcompose.model.azure.fleet.request.SpotProfile;
import com.sunbox.sdpcompose.model.azure.fleet.request.VMSizesProfile;

import java.util.List;

/**
 * Azure Api返回的AzureFleet报文中,Properties字段
 * @author wangda
 * @date 2024/7/10
 */
public class AzureFleetInfoProperties {
    /** 竞价实例的信息 */
    private SpotProfile spotPriorityProfile;
    /** 按需的信息 */
    private RegularProfile regularPriorityProfile;
    /** VM列表 */
    private List<VMSizesProfile> vmSizesProfile;
    /** 系统(OS)的信息 */
    private JSONObject computeProfile;
    private String provisioningState;
    private String uniqueId;
    private String timeCreated;

    public RegularProfile getRegularPriorityProfile() {
        return regularPriorityProfile;
    }

    public void setRegularPriorityProfile(RegularProfile regularPriorityProfile) {
        this.regularPriorityProfile = regularPriorityProfile;
    }

    public SpotProfile getSpotPriorityProfile() {
        return spotPriorityProfile;
    }

    public void setSpotPriorityProfile(SpotProfile spotPriorityProfile) {
        this.spotPriorityProfile = spotPriorityProfile;
    }

    public List<VMSizesProfile> getVmSizesProfile() {
        return vmSizesProfile;
    }

    public void setVmSizesProfile(List<VMSizesProfile> vmSizesProfile) {
        this.vmSizesProfile = vmSizesProfile;
    }

    public JSONObject getComputeProfile() {
        return computeProfile;
    }

    public void setComputeProfile(JSONObject computeProfile) {
        this.computeProfile = computeProfile;
    }

    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}
