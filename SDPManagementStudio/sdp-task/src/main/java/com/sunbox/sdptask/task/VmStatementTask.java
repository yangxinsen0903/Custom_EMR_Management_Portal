package com.sunbox.sdptask.task;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.MetaDataType;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdptask.mapper.ConfClusterMapper;
import com.sunbox.service.IAzureService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * vm对账任务
 */
@Component
public class VmStatementTask implements BaseCommonInterFace {
    @Autowired
    private InfoVmStatementMapper infoVmStatementMapper;

    @Autowired
    private InfoVmStatementItemMapper infoVmStatementItemMapper;

    @Autowired
    private InfoVmStatementResultMapper infoVmStatementResultMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmNeoMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private MetaDataItemMapper metaDataItemMapper;

    @Value("${vm.statement.task.time: 0 0 0,4,16 * * ?}")
    private String taskCron;

    /**
     * VM对账定时任务
     */
//    @Scheduled(cron = "${vm.statement.task.time:0 * * * * ?}")
    @Scheduled(cron = "${vm.statement.task.time: 0 0 0,4,16 * * ?}")
    public void start() {
        getLogger().info("VmStatementTask start, cron={}", taskCron);
        String lockKey = "VM-STATEMENT-TASK";
        boolean lockResult = this.redisLock.tryLock(lockKey);
        if (!lockResult) {
            getLogger().error("VmStatementTask lock error key:{}", lockKey);
            return;
        }

        try {
            // 获取数据中心
            MetaDataItem item = new MetaDataItem();
            item.setType(MetaDataType.REGION.getCode());
            List<String> regionObjests = metaDataItemMapper.selectMetaData(item);
            List<String> regions = regionObjests.stream().map(s -> {
                JSONObject json = JSON.parseObject(s);
                return json.getString("region");
            }).collect(Collectors.toList());

            for (String region : regions) {
                vmSettlement(region);
            }
        } finally {
            this.redisLock.tryUnlock(lockKey);
        }
    }

    /**
     * 一个数据中心下的vm对账
     * @param region
     */
    private void vmSettlement(String region) {
        InfoVmStatement infoVmStatement = null;
        try {
            Date now = new Date();
            int hour = DateUtil.hour(now, true);
            String statementId = null;
            if (hour <= 12) {
                statementId = "VMS-" +StringUtils.upperCase(region)+"-"+ DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "AM";
            } else {
                statementId = "VMS-" +StringUtils.upperCase(region)+"-"+ DateUtil.format(now, DatePattern.PURE_DATE_PATTERN) + "PM";
            }

            if (hasVmStatement(statementId)) {
                getLogger().error("has exist vm statement:" + statementId + " region:" + region);
                return;
            }

            // 创建VM对账记录
            infoVmStatement = createVmStatement(statementId, region);

            List<ConfCluster> confClusters = getClusterList(region);

            List<InfoVmStatementResult> vmResults = new ArrayList<>();

            // 从azure获取vm信息
            List<InfoVmStatementItem> vmFromAzure = syncVmFromAzure(region, infoVmStatement, vmResults);

            // 从yarn获取vm信息
            List<InfoVmStatementItem> vmFromYarn = syncVmFromYarn(infoVmStatement, confClusters, vmResults);

            // 从sdp获取vm信息
            List<InfoVmStatementItem> vmFromSdp = syncVmFromSdp(region, infoVmStatement, vmResults);

            for (InfoVmStatementResult vmResult : vmResults) {
                this.infoVmStatementResultMapper.insert(vmResult);
            }

            try {
                infoVmStatementMapper.updateStatus(infoVmStatement.getStatementId(), "FINISHED", new Date());
                getLogger().info("update to status FINISHED success, infoVmStatement:{}", infoVmStatement);
            } catch (Exception exception) {
                getLogger().error("update status to FINISHED error, infoVmStatement:{}", infoVmStatement, exception);
            }
        } catch (Exception e) {
            if (infoVmStatement != null) {
                try {
                    infoVmStatementMapper.updateStatus(infoVmStatement.getStatementId(), "FAILURE", new Date());
                    getLogger().error("update status to FAILURE success, infoVmStatement:{}", infoVmStatement);
                } catch (Exception exception) {
                    getLogger().error("update status to FAILURE error, infoVmStatement:{}", infoVmStatement, exception);
                }
            }
            getLogger().error("sync vm error", e);
        }
    }

    private List<ConfCluster> getClusterList(String region) {
        return this.confClusterMapper.selectByState(ConfCluster.CREATED, region);
    }

    private boolean hasVmStatement(String statementId) {
        InfoVmStatement infoVmStatement = this.infoVmStatementMapper.selectByPrimaryKey(statementId);
        if (infoVmStatement != null) {
            return true;
        }
        return false;
    }

    private InfoVmStatement createVmStatement(String statementId, String region) {
        Date now = new Date();
        //状态; SYNCING同步数据中 COMPARING对比中 FINISHED对比完成 FAILURE对比失败
        InfoVmStatement infoVmStatement = new InfoVmStatement();
        infoVmStatement.setStatementId(statementId);
        infoVmStatement.setStatus("SYNCING");
        infoVmStatement.setCreatedTime(now);
        infoVmStatement.setModifiedTime(now);
        infoVmStatement.setRegion(region);

        getLogger().info("create infoVmStatement:{}", infoVmStatement);
        infoVmStatementMapper.insert(infoVmStatement);
        return infoVmStatement;
    }

    private static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    private List<InfoVmStatementItem> syncVmFromAzure(String region, InfoVmStatement infoVmStatement, List<InfoVmStatementResult> vmResults) {
        getLogger().info("begin syncVmFromAzure infoVmStatement:{}", infoVmStatement);
        List<InfoVmStatementItem> infoVmStatementItems = new ArrayList<>();

        ResultMsg resultMsg = this.azureService.getVmList(region);
        Object data = resultMsg.getData();
        if (data == null) {
            return infoVmStatementItems;
        }

        Map<String, Object> dataMap = (Map<String, Object>) data;
        Object vmGroupListData = dataMap.get("data");
        if (vmGroupListData == null) {
            return infoVmStatementItems;
        }

        List<Map<String, Object>> vmGroupList = (List<Map<String, Object>>) vmGroupListData;
        for (Map<String, Object> vmGroup : vmGroupList) {
            String subnetId = toString(vmGroup.get("subnetId"), null);

            Object virtualMachines = vmGroup.get("virtualMachines");
            if (virtualMachines == null) {
                continue;
            }

            List<Map<String, Object>> vmList = (List<Map<String, Object>>) virtualMachines;
            for (Map<String, Object> vmNode : vmList) {
                InfoVmStatementItem infoVmStatementItem = new InfoVmStatementItem();
                infoVmStatementItem.setStatementId(infoVmStatement.getStatementId());
                infoVmStatementItem.setClusterId(null);
                infoVmStatementItem.setClusterName(null);
                infoVmStatementItem.setGroupName(null);
                infoVmStatementItem.setSku(null);
                infoVmStatementItem.setPurchaseType(null);
                infoVmStatementItem.setPrivateIp(toString(vmNode.get("privateIp"), null));
                infoVmStatementItem.setHostName(toString(vmNode.get("hostName"), null));
                infoVmStatementItem.setVmName(toString(vmNode.get("name"), null));
                infoVmStatementItem.setVmState(toString(vmNode.get("vmState"), null));
                infoVmStatementItem.setSubnet(subnetId);
                infoVmStatementItem.setNicId(toString(vmNode.get("nicName"), null));
                if (infoVmStatementItem.getVmName() == null && infoVmStatementItem.getNicId() != null) {
                    infoVmStatementItem.setVmName(infoVmStatementItem.getNicId().replace("nic-", "vm-"));
                }
                infoVmStatementItem.setNicState(toString(vmNode.get("nicState"), null));
                infoVmStatementItem.setVmRole(null);
                infoVmStatementItem.setPhysicalZone(toString(vmNode.get("zone"), null));
                Object createdTimeObject = vmNode.get("createdTime");
                if (createdTimeObject != null) {
                    infoVmStatementItem.setVmCreatedTime(DateUtil.parseUTC(createdTimeObject.toString()));
                }
                infoVmStatementItem.setCreatedTime(new Date());
                infoVmStatementItem.setModifiedTime(infoVmStatementItem.getCreatedTime());
                infoVmStatementItem.setVmSource("AZURE");

                if (infoVmStatementItem.getVmName() != null) {
                    infoVmStatementItem.setVmName(infoVmStatementItem.getVmName().trim());
                }

                if (infoVmStatementItem.getHostName() != null) {
                    infoVmStatementItem.setHostName(infoVmStatementItem.getHostName().trim());
                }

                infoVmStatementItemMapper.insert(infoVmStatementItem);

                resolveVmResults(vmResults, infoVmStatementItem);
                infoVmStatementItems.add(infoVmStatementItem);
            }
        }

        getLogger().info("end syncVmFromAzure size:" + infoVmStatementItems.size());
        return infoVmStatementItems;
    }

    private List<InfoVmStatementItem> syncVmFromYarn(InfoVmStatement infoVmStatement,
                                                     List<ConfCluster> confClusters,
                                                     List<InfoVmStatementResult> vmResults) {
        getLogger().info("begin syncVmFromYarn infoVmStatement:{}", infoVmStatement);
        List<InfoVmStatementItem> infoVmStatementItems = new ArrayList<>();

        for (ConfCluster confCluster : confClusters) {
            getLogger().info("syncVmFromYarn clusterId:{}", confCluster.getClusterId());

            ResultMsg resultMsg = getRunningHostsFromYarn(confCluster);
            if (!resultMsg.isResult()) {
                getLogger().error("getRunningHostsFromYarn clusterId:" + confCluster.getClusterId() + ", msg:" + resultMsg.getErrorMsg());
                continue;
            }

            Object data = resultMsg.getData();
            if (data == null) {
                getLogger().error("getRunningHostsFromYarn clusterId:" + confCluster.getClusterId() + ", data is null");
                continue;
            }

            List<String> vmHostList = (List<String>) data;
            for (String vmHost : vmHostList) {
                InfoVmStatementItem infoVmStatementItem = new InfoVmStatementItem();
                infoVmStatementItem.setStatementId(infoVmStatement.getStatementId());
                infoVmStatementItem.setClusterId(confCluster.getClusterId());
                infoVmStatementItem.setClusterName(confCluster.getClusterName());
                infoVmStatementItem.setGroupName(null);
                infoVmStatementItem.setSku(null);
                infoVmStatementItem.setPurchaseType(null);
                infoVmStatementItem.setPrivateIp(null);
                infoVmStatementItem.setHostName(vmHost);
                infoVmStatementItem.setVmName(null);
                infoVmStatementItem.setVmState("Succeeded");
                infoVmStatementItem.setSubnet(null);
                infoVmStatementItem.setNicId(null);
                infoVmStatementItem.setNicState(null);
                infoVmStatementItem.setVmRole(null);
                infoVmStatementItem.setPhysicalZone(null);
                infoVmStatementItem.setVmCreatedTime(new Date());
                infoVmStatementItem.setModifiedTime(infoVmStatementItem.getCreatedTime());
                infoVmStatementItem.setVmSource("YARN");

                if (infoVmStatementItem.getVmName() != null) {
                    infoVmStatementItem.setVmName(infoVmStatementItem.getVmName().trim());
                }

                if (infoVmStatementItem.getHostName() != null) {
                    infoVmStatementItem.setHostName(infoVmStatementItem.getHostName().trim());
                }

                infoVmStatementItemMapper.insert(infoVmStatementItem);

                resolveVmResults(vmResults, infoVmStatementItem);
                infoVmStatementItems.add(infoVmStatementItem);
            }

        }

        getLogger().info("end syncVmFromYarn size:" + infoVmStatementItems.size());
        return infoVmStatementItems;
    }

    private ResultMsg getRunningHostsFromYarn(ConfCluster confCluster) {
        int maxRetry = 3;
        String errMsg = "";
        while(maxRetry>0) {
            try {
                return this.composeService.getRunningHostsFromYarn(confCluster.getClusterId());
            } catch (Exception e) {
                getLogger().error("getRunningHostsFromYarn error", e);
                errMsg = e.getMessage();
            }
            maxRetry --;
        }
        return ResultMsg.FAILURE(errMsg);
    }

    private List<InfoVmStatementItem> syncVmFromSdp(String region, InfoVmStatement infoVmStatement, List<InfoVmStatementResult> vmResults) {
        getLogger().info("begin syncVmFromSdp infoVmStatement:{}", infoVmStatement);
        List<InfoClusterVmWithConf> infoClusterVms = infoClusterVmNeoMapper
                .selectByState(ConfCluster.CREATED, InfoClusterVm.VM_RUNNING, region);
        List<InfoVmStatementItem> infoVmStatementItems = new ArrayList<>();
        for (InfoClusterVmWithConf infoClusterVm : infoClusterVms) {
            InfoVmStatementItem infoVmStatementItem = new InfoVmStatementItem();
            infoVmStatementItem.setStatementId(infoVmStatement.getStatementId());
            infoVmStatementItem.setClusterId(infoClusterVm.getClusterId());
            infoVmStatementItem.setClusterName(infoClusterVm.getClusterName());
            infoVmStatementItem.setGroupName(infoClusterVm.getGroupName());
            infoVmStatementItem.setSku(infoClusterVm.getSkuName());
            infoVmStatementItem.setPurchaseType(infoClusterVm.getPurchaseType());
            infoVmStatementItem.setPrivateIp(infoClusterVm.getInternalip());
            infoVmStatementItem.setHostName(infoClusterVm.getHostName());
            infoVmStatementItem.setVmName(infoClusterVm.getVmName());
            infoVmStatementItem.setVmState("Succeeded");
            infoVmStatementItem.setSubnet(infoClusterVm.getSubnet());
            infoVmStatementItem.setPhysicalZone(infoClusterVm.getPhysicalZone());
            infoVmStatementItem.setZoneName(infoClusterVm.getZoneName());
            infoVmStatementItem.setSubnet(infoClusterVm.getSubnet());
            infoVmStatementItem.setNicId(null);
            infoVmStatementItem.setNicState(null);
            infoVmStatementItem.setCpu(infoClusterVm.getCpu());
            infoVmStatementItem.setMemory(infoClusterVm.getMemory());
            infoVmStatementItem.setVmRole(infoClusterVm.getVmRole());
            infoVmStatementItem.setVmCreatedTime(infoClusterVm.getCreateEndtime());
            infoVmStatementItem.setCreatedTime(new Date());
            infoVmStatementItem.setModifiedTime(infoVmStatementItem.getCreatedTime());
            infoVmStatementItem.setVmSource("SDP");

            if (infoVmStatementItem.getVmName() != null) {
                infoVmStatementItem.setVmName(infoVmStatementItem.getVmName().trim());
            }

            if (infoVmStatementItem.getHostName() != null) {
                infoVmStatementItem.setHostName(infoVmStatementItem.getHostName().trim());
            }
            infoVmStatementItemMapper.insert(infoVmStatementItem);

            resolveVmResults(vmResults, infoVmStatementItem);
            infoVmStatementItems.add(infoVmStatementItem);
        }

        getLogger().info("end syncVmFromSdp size:" + infoVmStatementItems.size());
        return infoVmStatementItems;
    }

    private void resolveVmResults(List<InfoVmStatementResult> vmResults, InfoVmStatementItem vmItem) {
        InfoVmStatementResult vmStatementResult = null;
        for (InfoVmStatementResult vmResult : vmResults) {
            if (StringUtils.equals(vmResult.getHostName(), vmItem.getHostName())) {
                vmStatementResult = vmResult;
                break;
            }

            if (StringUtils.equals(vmResult.getVmName(), vmItem.getVmName())) {
                vmStatementResult = vmResult;
                break;
            }
        }

        if (vmStatementResult == null) {
            vmStatementResult = new InfoVmStatementResult();
            getLogger().info("new result vmName:{},hostName:{},source:{}",
                    vmItem.getVmName(),
                    vmItem.getHostName(),
                    vmItem.getVmSource());

            vmStatementResult.setStatementId(vmItem.getStatementId());
            vmStatementResult.setClusterId(vmItem.getClusterId());
            vmStatementResult.setClusterName(vmItem.getClusterName());
            vmStatementResult.setGroupName(vmItem.getGroupName());
            vmStatementResult.setSku(vmItem.getSku());
            vmStatementResult.setPurchaseType(vmItem.getPurchaseType());
            vmStatementResult.setCpu(vmItem.getCpu());
            vmStatementResult.setZoneName(vmItem.getZoneName());
            vmStatementResult.setPurchaseType(vmItem.getPurchaseType());
            vmStatementResult.setPrivateIp(vmItem.getPrivateIp());
            vmStatementResult.setHostName(vmItem.getHostName());
            vmStatementResult.setVmName(vmItem.getVmName());
            vmStatementResult.setVmState(vmItem.getVmState());
            vmStatementResult.setSubnet(vmItem.getSubnet());
            vmStatementResult.setNicId(vmItem.getNicId());
            vmStatementResult.setNicState(vmItem.getNicState());
            vmStatementResult.setVmRole(vmItem.getVmRole());
            vmStatementResult.setVmCreatedTime(vmItem.getVmCreatedTime());
            vmStatementResult.setCreatedTime(new Date());
            vmStatementResult.setModifiedTime(vmStatementResult.getCreatedTime());
            vmStatementResult.setAzureResult("N");
            vmStatementResult.setSdpResult("N");
            vmStatementResult.setYarnResult("N");

            if (vmItem.getVmSource().equals("SDP")) {
                vmStatementResult.setSdpResult("Y");
            } else if (vmItem.getVmSource().equals("YARN")) {
                vmStatementResult.setYarnResult("Y");
            } else if (vmItem.getVmSource().equals("AZURE")) {
                vmStatementResult.setAzureResult("Y");
            }

            vmResults.add(vmStatementResult);
        } else {
            if (vmItem.getVmSource().equals("SDP")) {
                vmStatementResult.setClusterId(vmItem.getClusterId());
                vmStatementResult.setClusterName(vmItem.getClusterName());
                vmStatementResult.setGroupName(vmItem.getGroupName());
                vmStatementResult.setSku(vmItem.getSku());
                vmStatementResult.setPurchaseType(vmItem.getPurchaseType());
                vmStatementResult.setPrivateIp(vmItem.getPrivateIp());
                vmStatementResult.setHostName(vmItem.getHostName());
                vmStatementResult.setVmName(vmItem.getVmName());
                vmStatementResult.setVmState("Succeeded");
                vmStatementResult.setSubnet(vmItem.getSubnet());
                vmStatementResult.setZoneName(vmItem.getZoneName());
                vmStatementResult.setPhysicalZone(vmItem.getPhysicalZone());
                vmStatementResult.setCpu(vmItem.getCpu());
                vmStatementResult.setMemory(vmItem.getMemory());
                vmStatementResult.setVmRole(vmItem.getVmRole());
                vmStatementResult.setVmCreatedTime(vmItem.getVmCreatedTime());
                vmStatementResult.setSdpResult("Y");
            } else if (vmItem.getVmSource().equals("YARN")) {
                vmStatementResult.setYarnResult("Y");
            } else if (vmItem.getVmSource().equals("AZURE")) {
                vmStatementResult.setNicId(vmItem.getNicId());
                vmStatementResult.setNicState(vmItem.getNicState());
                if (vmItem.getVmCreatedTime() != null) {
                    vmStatementResult.setVmCreatedTime(vmItem.getVmCreatedTime());
                }
                if (vmItem.getVmState() != null) {
                    vmStatementResult.setVmState(vmItem.getVmState());
                }
                if (vmItem.getPhysicalZone() != null) {
                    vmStatementResult.setPhysicalZone(vmItem.getPhysicalZone());
                }
                vmStatementResult.setAzureResult("Y");
            }
        }
    }
}