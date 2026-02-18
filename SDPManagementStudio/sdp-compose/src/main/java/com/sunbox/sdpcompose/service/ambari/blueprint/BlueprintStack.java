package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Blueprint Stack信息
 * @author: wangda
 * @date: 2022/12/4
 */
public class BlueprintStack {

    @JsonProperty("blueprint_name")
    String blueprintName;

    @JsonProperty("stack_name")
    String stackName;

    @JsonProperty("stack_version")
    String stackVersion;


    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getStackVersion() {
        return stackVersion;
    }

    public void setStackVersion(String stackVersion) {
        this.stackVersion = stackVersion;
    }
}
