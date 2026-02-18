package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.*;

/**
 * Blueprint中，host_groups数组中的一个对象.<br>
 * HostGroup是Ambari中安装集群的分组类型，可以是master 或 core 或 task <br/>
 *
 * @author: wangda
 * @date: 2022/12/5
 */
public class HostGroup {
    /** 主机组名称, ambari, master1, master2, core, task */
    @JsonProperty("name")
    String name;

    /** 主机组实例化主机的数量,可选 */
    @JsonProperty("cardinality")
    String cardinality;

    /** 主机组上安装的大数据组件列表 */
    @JsonProperty("components")
    List<ComponentObj> components = new ArrayList<>();

    @JsonProperty("configurations")
    List<Map<String, Map<String,Object>>> configurations = new ArrayList<>();

    @JsonIgnore
    HostGroupRole hostGroupRole;

    public HostGroup() {

    }

    /**
     * 用来检查大数据组件使用的Set
     */
    @JsonIgnore
    private Map<String, ComponentObj> componentMap = new HashMap<>();

    public List<Map<String, Map<String,Object>>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Map<String, Map<String,Object>>> configurations) {
        this.configurations = configurations;
    }

    public HostGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public List<ComponentObj> getComponents() {
        return components;
    }

    /**
     * 增加一组组件
     * @param components 组件列表
     */
    public void addComponents(List<ComponentObj> components) {
        this.components = components;
    }

    /**
     * 增加一个组件
     * @param component 组件
     */
    public void addComponent(ComponentObj component) {
        ComponentObj componentObj = componentMap.get(component.getName());
        if (Objects.isNull(componentObj)) {
            componentMap.put(componentObj.getName(), component);
            components.add(component);
        }
    }

    /**
     * 增加一个组件
     * @param componentName 组件的名称
     */
    public void addComponent(String componentName) {
        ComponentObj componentObj = componentMap.get(componentName);
        if (Objects.isNull(componentObj)) {
            componentObj = new ComponentObj(componentName);
            componentMap.put(componentObj.getName(), componentObj);
            components.add(componentObj);
        }
    }

    /**
     * 批量增加组件
     * @param components 组件名称列表
     */
    public void addComponent(List<String> components) {
        for (String componentName : components) {
            addComponent(componentName);
        }
    }

    public HostGroupRole getHostGroupRole() {
        return hostGroupRole;
    }

    public void setHostGroupRole(HostGroupRole hostGroupRole) {
        this.hostGroupRole = hostGroupRole;
    }
}