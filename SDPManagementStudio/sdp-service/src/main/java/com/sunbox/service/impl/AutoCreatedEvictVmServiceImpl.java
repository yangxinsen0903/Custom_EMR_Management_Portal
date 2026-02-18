/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.AutoCreatedEvictVmMapper;
import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.domain.AutoCreatedEvictVm;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.EvictVmStateType;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.service.AutoCreatedEvictVmService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author wangda
 * @date 2024/7/4
 */
@Service("AutoCreatedEvictVmService")
public class AutoCreatedEvictVmServiceImpl implements AutoCreatedEvictVmService, BaseCommonInterFace {

    @Autowired
    private ConfClusterNeoMapper confClusterNeoMapper;

    @Autowired
    private AutoCreatedEvictVmMapper autoCreatedEvictVmMapper;

    @Override
    public ResultMsg handleEvictVmEvent(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // 收到消息后处理工作
        // 1. 解析成JSONObject
        // 2. 生成AutoCreatedEvictVm实例
        // 3. 检查VM是否已存在,如果已存在,则不存.
        // 4. 不存在, 保存进数据库
        getLogger().info("收到补全驱逐VM的事件: {}", message);
        JSONObject vmJson = JSON.parseObject(message);
        String clusterName = vmJson.getJSONObject("tags").getString("SYS_SDP_CLUSTER");
        String vmName = vmJson.getString("name");
        String vmid = vmJson.getString("uniqueId");

        // 检查VM是否已经接收过了
        AutoCreatedEvictVm checkVm = autoCreatedEvictVmMapper.selectByVmId(vmid);
        if (Objects.nonNull(checkVm)) {
            getLogger().error("补全的VM已经存在,忽略此VM: vmid=" + vmid + " vmName=" + vmName);
            return ResultMsg.SUCCESS();
        }
        getLogger().info("检查集群信息: clusterName={} vmName={}", clusterName, vmName);
        ConfCluster confCluster = confClusterNeoMapper.selectLasestClusterByName(clusterName);

        // state
        EvictVmStateType state = EvictVmStateType.INIT;
        if (Objects.isNull(confCluster)) {
            state = EvictVmStateType.CLUSTER_NOT_EXIST;
        } else if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
            state = EvictVmStateType.CLUSTER_NOT_RUNNING;
        }

        String groupName = vmJson.getJSONObject("tags").getString("SYS_SDP_GROUP");
        String priority = vmJson.getString("priority");

        // 实例组类型
        PurchaseType purchaseType = PurchaseType.Standard;
        if (StrUtil.containsIgnoreCase(priority, "Spot")) {
            purchaseType = PurchaseType.Spot;
        }

        // VM角色
        String vmRole = "task";
        if (StrUtil.containsIgnoreCase(groupName, "ambari")) {
            vmRole = "ambari";
        } else if (StrUtil.containsIgnoreCase(groupName, "master")) {
            vmRole = "master";
        } else if (StrUtil.containsIgnoreCase(groupName, "core")) {
            vmRole = "core";
        }

        // ClusterId
        String clusterId = Objects.isNull(confCluster)? "": confCluster.getClusterId();

        AutoCreatedEvictVm vm = new AutoCreatedEvictVm();
        vm.setClusterId(clusterId);
        vm.setClusterName(clusterName);
        vm.setVmRole(vmRole);
        vm.setGroupName(groupName);
        vm.setVmName(vmName);
        vm.setVmid(vmid);
        vm.setPurchaseType(purchaseType.name());
        vm.setState(state.name());
        vm.setEventContent(message);
        vm.setCreateTime(new Date());

        autoCreatedEvictVmMapper.insert(vm);
        return ResultMsg.SUCCESS();
    }
}
