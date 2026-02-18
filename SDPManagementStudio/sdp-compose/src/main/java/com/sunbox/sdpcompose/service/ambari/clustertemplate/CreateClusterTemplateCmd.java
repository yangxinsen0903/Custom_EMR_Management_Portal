package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.service.ambari.blueprint.ClusterHostGroup;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.List;
import java.util.Map;

/**
 * 创建集群模板的请求参数
 * @author: wangda
 * @date: 2022/12/7
 */
public class CreateClusterTemplateCmd {

    /** 集群名称，不能重复 */
    String clusterName;

    /** Blueprint信息，一般由前一步生成 */
    Blueprint blueprint;

    /**
     * 管理员设置的自定义配置信息, 用于覆盖默认配置 <p/>
     * Key: 配置文件标识<br/>
     * Value: 该配置文件下的自定义配置, 也是一个Map类型
     * */
    Map<String, Map<String, Object>> configurations;

    /** 主机清单, 会配置到部署布局上面. */
    Map<HostGroupRole, List<String>> hosts;

    /** 主机组 */
    List<ClusterHostGroup> hostGroups;

    public List<ClusterHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<ClusterHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    public Map<String, Map<String, Object>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, Map<String, Object>> configurations) {
        this.configurations = configurations;
    }

    public Map<HostGroupRole, List<String>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<HostGroupRole, List<String>> hosts) {
        this.hosts = hosts;
    }
}
