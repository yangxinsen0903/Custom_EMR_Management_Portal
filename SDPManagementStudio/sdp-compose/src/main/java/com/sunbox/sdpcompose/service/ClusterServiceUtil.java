package com.sunbox.sdpcompose.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.InfoClusterVm;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群服务的通用工具类
 * @date 2023/6/30
 */
public class ClusterServiceUtil {

    /**
     * 从主机列表中获取运行中的主机
     * @param vms
     * @return
     */
    public static List<String> getRunningHosts(List<InfoClusterVm> vms) {
        List<String> hosts = new ArrayList<>();
        if (CollectionUtil.isEmpty(vms)) {
            return hosts;
        }
        vms.stream().forEach(x -> {
            if (x.getState() == InfoClusterVm.VM_RUNNING) {
                hosts.add(x.getHostName());
            }
        });
        return hosts;
    }

    /**
     * 从主机列表中获取所有主机的信息
     * @param vms
     * @return
     */
    public static String getHostsInfo(List<InfoClusterVm> vms) {
        if (CollectionUtil.isEmpty(vms)) {
            return "";
        }

        List<String> hosts = new ArrayList<>();
        vms.stream().forEach(x -> {
            hosts.add(x.getHostName() + ":" + x.getState() + ":" + x.getMaintenanceMode());
        });
        return StrUtil.join(",", hosts);
    }

}
