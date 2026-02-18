package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Blueprint下，host_groups节点中，components 节点中的元素。
 * @author: wangda
 * @date: 2022/12/5
 */
public class ComponentObj {
    /** 组件名称 */
    @JsonProperty("name")
    String name;

    public ComponentObj() {

    }

    public ComponentObj(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
