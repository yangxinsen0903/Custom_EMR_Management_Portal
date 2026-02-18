/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.sunbox.domain.DiffVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.VmDiffType;
import com.sunbox.sdpadmin.mapper.ConfClusterMapper;
import com.sunbox.sdpadmin.mapper.InfoClusterVmMapper;
import com.sunbox.sdpadmin.service.OpsService;
import com.sunbox.sdpservice.service.ComposeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangda
 * @date 2023/7/25
 */
@Slf4j
@Service
public class OpsServiceImpl implements OpsService {

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private ComposeService composeService;

    @Override
    public List<DiffVm> generateSdpYarnDiffVms() {
        List<DiffVm> diffResult = new ArrayList<>();

        // 查询所有的未销毁的集群
        Map queryParam = new HashMap<>();
        queryParam.put("emrStatus", Arrays.asList(2));
        List<Map> clusterList = confClusterMapper.selectByObject(queryParam);

        // 遍历每个集群，比对SDP与Yarn中的虚机
        for (Map clusterMap : clusterList) {
            String clusterId = (String) clusterMap.get("cluster_id");

            // 获取集群的运行中主机列表
            List<Map> sdpRunningVms = infoClusterVmMapper.selectAllRunningVms(clusterId, Arrays.asList("core", "task"));

            // 从Yarn中获取集群的运行中主机列表
            ResultMsg runningHostsResp = composeService.getRunningHostsFromYarn(clusterId);
            List<String> yarnRunningVms;
            Map<String, String> yarnRunningVmsMap = new HashMap<>();
            if (runningHostsResp.getResult()) {
                yarnRunningVms = (List<String>) runningHostsResp.getData();
                yarnRunningVmsMap = yarnRunningVms.stream().collect(Collectors.toMap(vm -> vm, vm -> vm));
            }

            // 比对两个列表，找出差异
            Date statTime = new Date();
            for (Map vm : sdpRunningVms) {
                String hostName = (String) vm.get("host_name");
                if (!yarnRunningVmsMap.containsKey(hostName)) {
                    diffResult.add(buildDiffVm(vm, statTime));
                }
            }
        }

        return diffResult;
    }

    private DiffVm buildDiffVm(Map host, Date statTime) {
        DiffVm diffVm = new DiffVm();
        diffVm.setId(UUID.randomUUID().toString());
        diffVm.setStatTime(DateUtil.format(statTime, "yyyy-MM-dd HH:mm:ss"));
        diffVm.setDiffType(VmDiffType.SDP_YARN.name());
        diffVm.setClusterId(Convert.toStr(host.get("cluster_id")));
        diffVm.setClusterName(Convert.toStr(host.get("cluster_name")));
        diffVm.setGroupName(Convert.toStr(host.get("group_name")));
        diffVm.setVmRole(Convert.toStr(host.get("vm_role")));
        diffVm.setVmName(Convert.toStr(host.get("vm_name")));
        diffVm.setHostName(Convert.toStr(host.get("host_name")));
        diffVm.setIp(Convert.toStr(host.get("internalIp")));
        diffVm.setPurchaseType(Convert.toStr(host.get("purchase_type")));
        diffVm.setSku(Convert.toStr( host.get("sku_name")));
        diffVm.setCpu(Convert.toStr(host.get("vcpus")));
        diffVm.setMemory(Convert.toStr(host.get("memory")));
        diffVm.setCurrentState(Convert.toStr(host.get("state")));

        return diffVm;
    }
}
