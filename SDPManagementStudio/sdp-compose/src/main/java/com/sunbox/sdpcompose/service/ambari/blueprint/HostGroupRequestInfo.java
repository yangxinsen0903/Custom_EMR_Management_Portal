package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.List;

/**
 * 请求生成主机分组的信息, 用来生成分组或计算该分组下面主机的配置信息使用.
 * @author: wangda
 * @date: 2022/12/8
 */
public class HostGroupRequestInfo {
    /** 主机分组名称 */
    private String name;

    /** 主机分组角色 */
    private HostGroupRole hostGroupRole;

    /** 该分组中主机数量 */
    private int hostCount;

    /** 该分组中的主机信息 */
    private List<HostInstance> hosts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HostGroupRole getHostGroupRole() {
        return hostGroupRole;
    }

    public void setHostGroupRole(HostGroupRole hostGroupRole) {
        this.hostGroupRole = hostGroupRole;
    }

    public List<HostInstance> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostInstance> hosts) {
        this.hosts = hosts;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }
}
