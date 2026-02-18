package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 集群的主机组对象
 * @author: wangda
 * @date: 2023/2/18
 */
public class ClusterHostGroup {
    /** 实例组的角色 */
    private HostGroupRole role;

    /** 集群实例组的Id */
    private String groupId;

    /** 集群实例组的名称 */
    private String groupName;

    /** 实例组的自定义配置 */
    private List<ConfClusterHostGroupAppsConfig> groupConfgs;

    /** 实例组中的主机 */
    private List<HostInstance> hosts;

    /** HostGroup中的vmSku名称,此属性不会传给Ambari,只在SDP业务逻辑中使用 */
    @JsonIgnore
    private String vmSkuName;

    /**
     * 分组主机配置
     * @return
     */
    public Map<String, List<ConfClusterHostGroupAppsConfig>> groupByConfigClassification() {
        if (Objects.isNull(groupConfgs)) {
            return new HashMap<>();
        }
        Map<String, List<ConfClusterHostGroupAppsConfig>> grouped = groupConfgs.stream().collect(Collectors.groupingBy(ConfClusterHostGroupAppsConfig::getAppConfigClassification));
        return grouped;
    }

    public HostGroupRole getRole() {
        return role;
    }

    public void setRole(HostGroupRole role) {
        this.role = role;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ConfClusterHostGroupAppsConfig> getGroupConfgs() {
        return groupConfgs;
    }

    public void setGroupConfgs(List<ConfClusterHostGroupAppsConfig> groupConfgs) {
        this.groupConfgs = groupConfgs;
    }

    public List<HostInstance> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostInstance> hosts) {
        this.hosts = hosts;
    }

    public String getVmSkuName() {
        return vmSkuName;
    }

    public void setVmSkuName(String vmSkuName) {
        this.vmSkuName = vmSkuName;
    }
}
