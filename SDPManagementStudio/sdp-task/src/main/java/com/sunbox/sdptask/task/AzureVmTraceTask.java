package com.sunbox.sdptask.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.AzureVmtraceInfoMapper;
import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.dao.mapper.InfoClusterVmNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.VmsResponse;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.domain.azure.AzureVmtraceInfo;
import com.sunbox.sdptask.mapper.ConfScalingTaskMapper;
import com.sunbox.domain.metaData.Subnet;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DateUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 17.Azure端僵尸机清理任务,SDP通过定时执行任务，从Azure端获取VM，并与SDP进行比较，找到Azure中存在但在SDP中不存在的VM，调用Azure Api将其删除。
 * 任务执行周期：2个小时执行一次
 * 处理的VM条件：半个小时前创建的VM
 */
@Component
public class AzureVmTraceTask implements BaseCommonInterFace {

    @Value("${hostname.domain:''}")
    private String hostnameDB;

    @Autowired
    private IMetaDataItemService metaDataItemService;
    @Autowired
    private IAzureService azureService;

    @Resource
    private InfoClusterVmNeoMapper clusterVmNeoMapper;

    @Resource
    private ConfClusterNeoMapper confClusterNeoMapper;

    @Resource
    private AzureVmtraceInfoMapper azureVmtraceInfoMapper;

    @Resource
    private ConfScalingTaskMapper taskMapper;

    @Scheduled(cron = "${azure.delete.task.time:0 0 0/2 * * ?}")
   // @Scheduled(cron = "1/30 * * * * ?")
    public void start() {
        getLogger().info("AzureVmTraceTask start,listAll,deleteVirtualMachines");
        Assert.notEmpty(hostnameDB, "hostname.domain 不能为空");
        //所有的azure服务出参
        List<Subnet> getsubnetlist = metaDataItemService.getsubnetlist();
        if (CollectionUtil.isEmpty(getsubnetlist)) {
            getLogger().error("AzureVmTraceTask getsubnetlist is empty");
            return;
        }
        //遍历jsonObjects,取出region和subnet,根据region分组,
        Map<String, List<String>> regionSubnetGroupMap = new HashMap<>();
        for (Subnet subnetItem : getsubnetlist) {
            String region = subnetItem.getRegion();
            String subnetId = subnetItem.getSubnetId();
            regionSubnetGroupMap.computeIfAbsent(region, k -> new ArrayList<>()).add(subnetId);
        }
        Map<String, String> regionMap = metaDataItemService.getRegionMap();
        Map<String, String> vmRegionMap = new HashMap<>();
        Assert.notEmpty(regionMap, "regionMap 不能为空");
        List<VmsResponse> vmRes = new ArrayList<>();

        getVmList(regionSubnetGroupMap, regionMap, vmRegionMap, vmRes);
        if (CollectionUtil.isEmpty(vmRes)) {
            getLogger().error("AzureVmTraceTask,getVmList,vmRes is empty");
            return;
        }
        //根据集群名称 SYS_SDP_CLUSTER  分组的azure服务出参, 因为getVmList出参中集群id(sdp-clusterId)不一定出现,所以按照集群名称(SYS_SDP_CLUSTER)分组
        Map<String, List<VmsResponse>> groupVmRes = groupVmsByClusterName(vmRes);
        String groupVmResJson = JSON.toJSONString(groupVmRes);
        getLogger().info("AzureVmTraceTask,groupVmResJson,all:{}", groupVmResJson);
        // 根据clusterId和vmName比对即可。
        //找到Azure中存在但在SDP中不存在的VM，调用Azure Api将其删除。
        if (CollectionUtil.isEmpty(groupVmRes)) {
            getLogger().error("AzureVmTraceTask,getGroupVmRes,groupVmRes is empty");
            return;
        }

        //循环groupVmRes, 过滤出需要删除的vm: needDelGroupVmRes
        // 判断 hostName   wu2dns.shein.com 是不是系统配置的值. ( config_core), 是的话, 才删除.
        Map<String, List<VmsResponse>> needDelGroupVmRes= getNeedGroupVm(groupVmRes);

        if (CollectionUtil.isEmpty(needDelGroupVmRes)) {
            getLogger().error("AzureVmTraceTask,getNeedGroupVm,needDelGroupVmRes is empty");
            return;
        }
        String needDelGroupVmResJson = JSON.toJSONString(needDelGroupVmRes);
        getLogger().info("AzureVmTraceTask,needDelGroupVmResJson,all:{}", needDelGroupVmResJson);
        // 记录和删除需要清理的vm
        saveAndDeleteVm(vmRegionMap, needDelGroupVmRes);

    }


    /**
     * 过滤,获取需要删除的数据
     * @param groupVmRes
     * @return
     */
    private Map<String, List<VmsResponse>> getNeedGroupVm(Map<String, List<VmsResponse>> groupVmRes) {
        Map<String, List<VmsResponse>> needDelGroupVm = new HashMap<>();

        for (Map.Entry<String, List<VmsResponse>> vmsResEntry : groupVmRes.entrySet()) {
            String clusterName = vmsResEntry.getKey();
            //如果集群是本系统的, 才需要删除
             ConfCluster confClusters = confClusterNeoMapper.selectLasestClusterByName(clusterName);
             if (confClusters==null){
                 continue;
             }
            String clusterId = confClusters.getClusterId();
             if(StringUtils.isBlank(clusterId)){
                 continue;
             }
            ArrayList<Integer> statesList = new ArrayList<>();
            statesList.add(InfoClusterVm.VM_RUNNING);
            statesList.add(InfoClusterVm.VM_DELETING);
            statesList.add(InfoClusterVm.VM_UNKNOWN);
            List<InfoClusterVm> infoClusterVms = clusterVmNeoMapper.selectByClusterIdAndState(clusterId,statesList);
            if(CollectionUtil.isEmpty(infoClusterVms)){
                continue;
            }
            Set<String> vmNameSet = infoClusterVms.stream().map(InfoClusterVm::getVmName).collect(Collectors.toSet());
            List<VmsResponse> vmsAllResponsesList = vmsResEntry.getValue();
            //需要删除的vm
            List<VmsResponse> vmsResList = new ArrayList<>();
            for (VmsResponse vmAllVo : vmsAllResponsesList) {
                //vm 在sdp中
                Boolean containsVm = vmNameSet.contains(vmAllVo.getName());
                if(containsVm){
                    continue;
                }
                String vmCreateTime = vmAllVo.getVmTimeCreated();
                LocalDateTime utcLocalDateTime = getLocalDateTime(vmCreateTime);
                //判断vm创建时间是否在30分钟之前
                Boolean isBefore30Min = DateUtil.checkTime(utcLocalDateTime, ChronoUnit.MINUTES, 30);
                if(!isBefore30Min){
                    continue;
                }
                Boolean isMatchHostName = Boolean.FALSE;
                String hostName = vmAllVo.getHostName();
                if (StringUtils.isNotBlank(hostName)) {
                    //判断 hostName  是否以此结尾, wu2dns.shein.com 是不是系统配置的值. ( config_core), 是的话, 才删除.
                    isMatchHostName = hostName.endsWith(hostnameDB);
                }
                if(!isMatchHostName){
                    continue;
                }
                // 找到Azure中存在但在SDP中不存在的VM
                vmsResList.add(vmAllVo);
            }
            needDelGroupVm.put(clusterName, vmsResList);
        }
        return needDelGroupVm;
    }

    /**
     * 根据集群名称,对azureService查询服务的出参进行分组整理
     * @param responseVms
     * @return
     */
    @NotNull
    private Map<String, List<VmsResponse>> groupVmsByClusterName(List<VmsResponse> responseVms) {
        Map<String, List<VmsResponse>> groupedVms = new HashMap<>();
        for (VmsResponse responseVm : responseVms) {
            // 得到集群名
            Map<String, String> tagsMap = responseVm.getTags();
            String clusterName = tagsMap.get(VmsResponse.SYS_SDP_CLUSTER);
            // 按集群名分组
            List<VmsResponse> vmsAllResponses = groupedVms.get(clusterName);
            if (CollectionUtil.isEmpty(vmsAllResponses)) {
                List<VmsResponse> vms = new ArrayList<>();
                vms.add(responseVm);
                groupedVms.put(clusterName, vms);
            } else {
                vmsAllResponses.add(responseVm);
            }
        }
        return groupedVms;
    }

    /**
     * 循环region, 调用azureService, 获取 vmRegionMap,vmRes
     * @param regionSubnetGroupMap region以及对应的Subnet
     * @param regionMap region和regionName
     * @param vmRegionMap vmName和对应的region, 用于azureService删除服务的入参
     * @param vmRes azureService服务出参
     */
    private void getVmList(Map<String, List<String>> regionSubnetGroupMap, Map<String, String> regionMap,
                           Map<String, String> vmRegionMap, List<VmsResponse> vmRes) {

        for (Map.Entry<String, String> entry : regionMap.entrySet()) {
            String region = entry.getKey();
            if (StringUtils.isBlank(region)) {
                continue;
            }
            JSONObject jsonObjectVm = new JSONObject();
            jsonObjectVm.put("region", region);
            List<String> subNet = regionSubnetGroupMap.get(region);
            if (!CollectionUtil.isEmpty(subNet)) {
                subNet = subNet.stream().distinct().collect(Collectors.toList());
            }
            jsonObjectVm.put("subNetIds", subNet);
            ResultMsg azureServiceVmMsg = azureService.getVmList(jsonObjectVm);
            if (azureServiceVmMsg == null || !azureServiceVmMsg.getResult() || azureServiceVmMsg.getData() == null) {
                continue;
            }
            String jsonString = JSON.toJSONString(azureServiceVmMsg.getData());
            if (StringUtils.isBlank(jsonString)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(jsonString);
            JSONArray datas = jsonObject.getJSONArray("datas");
            if (datas != null && !datas.isEmpty()) {
                List<VmsResponse> vmsResponses = datas.toJavaList(VmsResponse.class);
                vmRes.addAll(vmsResponses);
                List<String> vmCollect = vmsResponses.stream().map(VmsResponse::getName).collect(Collectors.toList());
                for (String vmName : vmCollect) {
                    vmRegionMap.put(vmName, region);
                }
            }
        }
    }

    /**
     * 记录和删除需要清理的vm
     * @param vmRegionMap vmName和对应的region, 用于azureService删除服务的入参
     * @param needDelGroupVmRes
     */
    private void saveAndDeleteVm(Map<String, String> vmRegionMap, Map<String, List<VmsResponse>> needDelGroupVmRes) {

        for (Map.Entry<String, List<VmsResponse>> vmsResEntry : needDelGroupVmRes.entrySet()) {
            String clusterName = vmsResEntry.getKey();
            List<VmsResponse> allResponseList = vmsResEntry.getValue();
            if (StringUtils.isBlank(clusterName) || CollectionUtil.isEmpty(allResponseList)) {
                continue;
            }
            try {
                //需要删除的vmName
                List<String> vmNamesList = new ArrayList<>();
                List<String> dnsNamesList = new ArrayList<>();
                for (VmsResponse vmAllVo : allResponseList) {
                    vmNamesList.add(vmAllVo.getName());
                    dnsNamesList.add(vmAllVo.getHostName());
                }
                // 因为getVmList出参中集群id(sdp-clusterId)不一定出现,所以按照集群名称(SYS_SDP_CLUSTER)分组
                ConfCluster confCluster = confClusterNeoMapper.selectLasestClusterByName(clusterName);
                //保存到表中 azure_vmtrace_info
                for (VmsResponse vmsAllResponse : allResponseList) {
                    //3. 逻辑修改
                    // 检查到某个VM在SDP中不存在时，再检查一下这个VM是否已经存在于     azure_cleaned_vms_record 表中，如果已经存在，则只增加invoke_count次数.
                    // 如果不存在，新增
                    List<AzureVmtraceInfo> azureVmtraceInfoList = azureVmtraceInfoMapper.selectByVMName(vmsAllResponse.getName());
                    //删除前, 判断改VM所在的实例组是否有排队或进行中的扩缩容任务. 如果有, 不进行删除.
                    ResultMsg resultMsg = checkCanCreateScaleTask(vmsAllResponse.getTags().get("sdp-clusterId"), vmsAllResponse.getTags().get("sdp-role"),
                            vmsAllResponse.getTags().get("sdp-groupName"));
                    if (resultMsg.getResult()) {
                        getLogger().error("AzureVmTraceTask,checkCanCreateScaleTask,runningTask:{}", resultMsg.getData());
                        //存在任务, 跳过
                        continue;
                    }
                    if (CollectionUtil.isEmpty(azureVmtraceInfoList)) {
                        AzureVmtraceInfo azureVmtraceInfo = new AzureVmtraceInfo();
                        azureVmtraceInfo.setVmName(vmsAllResponse.getName());
                        azureVmtraceInfo.setHostName(vmsAllResponse.getHostName());
                        azureVmtraceInfo.setUniqueId(vmsAllResponse.getUniqueId());
                        azureVmtraceInfo.setPrivateIp(vmsAllResponse.getPrivateIp());
                        azureVmtraceInfo.setZone(vmsAllResponse.getZone());
                        azureVmtraceInfo.setPriority(vmsAllResponse.getPriority());
                        azureVmtraceInfo.setVmSize(vmsAllResponse.getVmSize());
                        azureVmtraceInfo.setClusterId(vmsAllResponse.getTags().get("sdp-clusterId"));
                        azureVmtraceInfo.setClusterName(vmsAllResponse.getTags().get(VmsResponse.SYS_SDP_CLUSTER));
                        azureVmtraceInfo.setVmRole(vmsAllResponse.getTags().get("sdp-role"));
                        azureVmtraceInfo.setGroupId(vmsAllResponse.getTags().get("sdp-groupId"));
                        azureVmtraceInfo.setGroupName(vmsAllResponse.getTags().get("sdp-groupName"));
                        azureVmtraceInfo.setVmCreatedTime(getDate(vmsAllResponse.getVmTimeCreated()));
                        azureVmtraceInfo.setCreatedby("AzureVmTraceTask");
                        azureVmtraceInfo.setCreatedTime(new Date());
                        azureVmtraceInfo.setInvokeCount(1);
                        azureVmtraceInfo.setModifiedTime(new Date());
                        azureVmtraceInfoMapper.insertSelective(azureVmtraceInfo);
                    } else {
                        AzureVmtraceInfo azureVmtraceInfo = new AzureVmtraceInfo();
                        azureVmtraceInfo.setInvokeCount(azureVmtraceInfoList.get(0).getInvokeCount() + 1);
                        azureVmtraceInfo.setId(azureVmtraceInfoList.get(0).getId());
                        azureVmtraceInfo.setModifiedTime(new Date());
                        azureVmtraceInfoMapper.updateByPrimaryKeySelective(azureVmtraceInfo);
                    }
                }
                AzureDeleteVMsRequest azureDeleteVMsRequest = new AzureDeleteVMsRequest();
                String regionVal = vmRegionMap.get(allResponseList.get(0).getName());
                azureDeleteVMsRequest.setRegion(regionVal);
                if (confCluster != null) {
                    azureDeleteVMsRequest.setApiVersion(confCluster.getClusterReleaseVer());
                }
                azureDeleteVMsRequest.setClusterName(clusterName);
                azureDeleteVMsRequest.setTransactionId(UUID.randomUUID().toString().replaceAll("-", ""));
                azureDeleteVMsRequest.setVmNames(vmNamesList);
                azureDeleteVMsRequest.setDnsNames(dnsNamesList);
                getLogger().info("AzureVmTraceTask,azureDeleteVMsRequest,inParam:{}", azureDeleteVMsRequest);
                ResultMsg resultMsg = azureService.deleteVirtualMachines(azureDeleteVMsRequest);
                azureVmtraceInfoMapper.updateVmsDelResponseByClusterName(JSON.toJSONString(resultMsg), clusterName);
            } catch (Exception e) {
                getLogger().error("AzureVmTraceTask,saveAndDeleteVm,e", e);
            }
        }
    }

    private LocalDateTime getLocalDateTime(String vmCreateTime) {
        LocalDateTime utcLocalDateTime;
        try {
            ZonedDateTime zonedDateTime = getZonedDate(vmCreateTime);
            // 或者直接使用UTC时区（如果你的意图是保留UTC时间）
            utcLocalDateTime = zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            getLogger().error("AzureVmTraceTask,getLocalDateTime,vmCreateTime" + vmCreateTime, e);
            throw new RuntimeException("parse time exception");
        }

        return utcLocalDateTime;
    }

    private Date getDate(String vmCreateTime) {
        Date vmDate;
        try {
            ZonedDateTime zonedDateTime = getZonedDate(vmCreateTime);
            // 或者直接使用UTC时区（如果你的意图是保留UTC时间）
            Instant instant = zonedDateTime.toInstant();
            vmDate = Date.from(instant);
        } catch (Exception e) {
            getLogger().error("AzureVmTraceTask,getDate,vmCreateTime" + vmCreateTime, e);
            throw new RuntimeException("parse time exception");
        }
        return vmDate;
    }

    private ZonedDateTime getZonedDate(String vmCreateTime) {
        // 使用 ZonedDateTime 解析, 2025-03-03T03:21:33.292Z
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(vmCreateTime);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a XXX", Locale.ENGLISH);
//        // 解析为带时区的日期时间
//        ZonedDateTime zonedDateTime = ZonedDateTime.parse(vmCreateTime, formatter);
        return zonedDateTime;
    }

    private ResultMsg checkCanCreateScaleTask(String clusterId, String vmRole, String groupName) {
        ResultMsg resultMsg = new ResultMsg();
        ConfScalingTask runningTask = taskMapper.peekQueueHeadTask(clusterId, vmRole, groupName,
                Arrays.asList(ConfScalingTask.SCALINGTASK_Create, ConfScalingTask.SCALINGTASK_Running));
        getLogger().info("AzureVmTraceTask,checkCanCreateScaleTask,param:{},{},{},{}", clusterId,vmRole,groupName,runningTask);
        if (runningTask != null) {
            resultMsg.setResult(true);
            resultMsg.setData(runningTask);
            return resultMsg;
        }
        resultMsg.setResult(false);
        return resultMsg;
    }

}