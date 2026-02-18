/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.dao.mapper.InfoClusterVmNeoMapper;
import com.sunbox.dao.mapper.VmEventMapper;
import com.sunbox.dao.query.VmEventQueryParam;
import com.sunbox.domain.*;
import com.sunbox.domain.vmEvent.VmEventRequest;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.IVmEventService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * VM上下线事件的业务实现类
 * @author wangda
 * @date 2024/7/15
 */
@Service
public class VmEventServiceImpl implements IVmEventService, BaseCommonInterFace {

    @Autowired
    private ConfClusterNeoMapper confClusterMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmMapper;

    @Autowired
    private VmEventMapper vmEventMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Override
    public void saveVmEventsForScaleOutTask(String clusterId, String scaleOutTaskId) {
        try {
            List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectRunningVmsByTaskId(clusterId, scaleOutTaskId, InfoClusterVm.VM_RUNNING);
            saveVmEvents(infoClusterVms, true);
        } catch (Exception ex) {
            getLogger().error("记录VM上线事件时失败: clusterId=" + clusterId + " scaleoutTaskId=" + scaleOutTaskId, ex);
        }
    }

    @Override
    public void saveVmEventsForCreateCluster(String clusterId) {
        // 创建集群时, 将集群中所有运行中的VM都触发上线事件
        List<InfoClusterVm> runningVms = infoClusterVmMapper.selectByClusterIdAndState(clusterId, Arrays.asList(InfoClusterVm.VM_RUNNING));
        saveVmEvents(runningVms, true);
    }

    @Override
    public void saveVmEventsForDeleteCluster(String clusterId) {
        List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndState(clusterId, Arrays.asList(InfoClusterVm.VM_RUNNING));
        saveVmEvents(vms, false);
    }

    @Override
    public void saveVmEventsForScaleInTask(String clusterId, String scaleInTaskId) {
        try {
            List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectVmsByScaleInTaskId(clusterId, scaleInTaskId);
            saveVmEvents(infoClusterVms, false);
        } catch (Exception ex) {
            getLogger().error("记录VM下线事件时失败: clusterId=" + clusterId + " scaleinTaskId=" + scaleInTaskId, ex);
        }
    }

    @Override
    public void saveVmEvents(List<InfoClusterVm> vms, boolean isOnline) {
        // 查询到集群信息
        // 只发送状态为运行中的VM
        if (CollectionUtil.isEmpty(vms)) {
            getLogger().info("批量保存VM上下线事件时,上下线的VM为空,不处理");
            return;
        } else {
            getLogger().info("批量保存VM上下线事件,保存事件数量:" + vms.size());
        }

        // 生成VMEvent
        List<VmEvent> vmEvents = generateVmEvents(vms, isOnline);

        // 保存进数据库
        for (VmEvent vmEvent : vmEvents) {
            VmEvent tmp = vmEventMapper.selectByVmNameAndEventType(vmEvent.getVmName(),
                    isOnline?VmEvent.EVENT_TYPE_ONLINE: VmEvent.EVENT_TYPE_OFFLINE);
            if (Objects.isNull(tmp)) {
                vmEventMapper.insert(vmEvent);
            } else {
                getLogger().info("VM上下线事件已经存在,不再保存: {}", JSON.toJSONString(tmp));
            }
        }
    }

    private List<VmEvent> generateVmEvents(List<InfoClusterVm> vms, boolean isOnline) {
        List<VmEvent> vmEvents = new ArrayList<>();
        InfoClusterVm firstVm = vms.get(0);
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(firstVm.getClusterId());

        for (InfoClusterVm vm : vms) {
            VmEvent event = new VmEvent();
            event.setId(IdUtil.getSnowflake().nextId());
            event.setClusterId(vm.getClusterId());
            event.setClusterName(confCluster.getClusterName());
            event.setGroupName(vm.getGroupName());
            event.setVmName(vm.getVmName());
            event.setHostName(vm.getHostName());
            event.setVmId(vm.getVmid());
            event.setTriggerTime(new Date());
            event.setFinishTime(null);
            // 处理 purchaseType
            String purchaseType = VmEvent.PURCHASE_TYPE_SPOT;
            if (StrUtil.equalsIgnoreCase(firstVm.getPurchaseType(), ConfClusterVm.PURCHASETYPE_ONDEMOND + "")) {
                purchaseType = VmEvent.PURCHASE_TYPE_ONDEMAND;
            }
            event.setPurchaseType(purchaseType);

            if (isOnline) {
                event.setEventType(VmEvent.EVENT_TYPE_ONLINE);
            } else {
                event.setEventType(VmEvent.EVENT_TYPE_OFFLINE);
            }
            event.setState(VmEvent.STATE_INIT);
            vmEvents.add(event);
        }
        return vmEvents;
    }

    /**
     * 获取上下线主机列表
     * @param vmEventRequest
     * @return
     */
    @Override
    public ResultMsg getVmEventList(VmEventRequest vmEventRequest) {
        VmEventQueryParam param = new VmEventQueryParam();
        BeanUtils.copyProperties(vmEventRequest, param);
        int pageIndex = vmEventRequest.getPageIndex() == null ? 1 : vmEventRequest.getPageIndex();
        int pageSize = vmEventRequest.getPageSize() == null ? 20 : vmEventRequest.getPageSize();
        param.pager(pageIndex,pageSize);
        param.setStates(vmEventRequest.getState());
        List<VmEvent> vmEvents = vmEventMapper.selectPageList(param);
        int total = vmEventMapper.selectPageListCount(param);
        if (total>0){
            // 获取regionName
            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            for (VmEvent vmEvent : vmEvents) {
                String region = vmEvent.getRegion();
                vmEvent.setRegionName(regionMap.get(region));
            }
        }
        ResultMsg success = ResultMsg.SUCCESS();
        success.setData(vmEvents);
        success.setTotal(total);
        return success;
    }
}
