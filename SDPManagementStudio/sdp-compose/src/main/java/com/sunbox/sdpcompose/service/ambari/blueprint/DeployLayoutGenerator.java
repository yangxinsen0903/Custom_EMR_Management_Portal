package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.List;
import java.util.Map;

/**
 * 部署布局抽象类, 根据传入的主机主的角色 和 服务清单,生成布局
 *
 * @author: wangda
 * @date: 2022/12/5
 */
public abstract class DeployLayoutGenerator {

    /**
     * 生成布局
     * @param stackName Stack 名，如：SDP-1.0.0
     * @param hostGroups HostGroup组信息，即Master组，Core组，Task组 分别多少台主机
     * @param services 需要部署的服务列表
     * @param isHa 是否是HA
     * @return 布局
     */
    public abstract List<HostGroup> generate(String stackName, Map<HostGroupRole, Integer> hostGroups, List<String> services, boolean isHa);

    public abstract List<HostGroup> generate(String stackName, List<ClusterHostGroup> hostGroups, List<String> services, boolean isHa);

}
