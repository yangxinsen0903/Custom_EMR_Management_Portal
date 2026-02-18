package com.sunbox.sdpcompose.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.*;
import com.sunbox.sdpcompose.hostgroup.ClusterHostGroupManager;
import com.sunbox.sdpcompose.manager.AzureServiceManager;
import com.sunbox.sdpcompose.mapper.ConfClusterMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmMapper;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.sdpservice.data.compose_cloud.ScaleInForDeleteTaskVmReq;
import com.sunbox.util.DNSUtil;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sunbox.sdp.ambari.client.model.customaction.HostRole;
import sunbox.sdp.ambari.client.model.customaction.QueryComponentInHostsResponse;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/compose")
public class ComposeController implements BaseCommonInterFace {

    @Autowired
    private IAzureService iAzureService;

    @Autowired
    private IPlanExecService planExecService;

    @Autowired
    private IMQProducerService imqProducerService;

    @Autowired
    private IPlayBookService playBookService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private IScalingService scalingService;

    @Autowired
    private IAmbariService ambariService;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private IAzureFleetService azureFleetService;

    @Autowired
    private com.sunbox.service.IAzureService azureServiceSer;

    @Value("${sdp.wgetpath}")
    private String blobpath;

    @GetMapping("/geVmSkus")
    public ResultMsg geVmSkus(@RequestParam String region) {
        return iAzureService.geVmSkus(region);
    }

    @GetMapping("/getSubnet")
    public ResultMsg getSubnet(@RequestParam String region) {
        return iAzureService.getSubnet(region);
    }

    @PostMapping("/createplan")
    public ResultMsg createPlan(@RequestParam("clusterId") String clusterId,
                                @RequestParam("operationName") String operationName,
                                @RequestParam("releaseVersion") String releaseVersion) {
        return planExecService.createPlanAndRun(clusterId, releaseVersion, operationName, null, null);
    }

    @PostMapping("/createplanforscaling")
    public ResultMsg createPlanforscaling(@RequestParam("clusterId") String clusterId,
                                          @RequestParam("operationName") String operationName,
                                          @RequestParam("releaseVersion") String releaseVersion,
                                          @RequestParam("taskId") String taskId) {
        return planExecService.createPlanAndRun(clusterId, releaseVersion, operationName, taskId, null);
    }

    @GetMapping("/getDiskSku")
    public ResultMsg getDiskSku(@RequestParam String region) {
        return iAzureService.getDiskSku(region);
    }

    @GetMapping("/getMIList")
    public ResultMsg getMIList(@RequestParam String region) {
        return iAzureService.getMIList(region);
    }

    @GetMapping("/getNSGSku")
    public ResultMsg getNSGSku(@RequestParam String region) {
        return iAzureService.getNSGSku(region);
    }

    @GetMapping("/getSSHKeyPair")
    public ResultMsg getSSHKeyPair(@RequestParam String region) {
        return iAzureService.getSSHKeyPair(region);
    }

    @PostMapping("/sendMessage")
    public ResultMsg sendMessage(@RequestParam("clientname") String clientname, @RequestParam("messagebody") String messagebody) {
        return imqProducerService.sendMessage(clientname, messagebody);
    }

    @PostMapping("/sendDelayMessage")
    public ResultMsg sendDelayMessage(@RequestParam("clientname") String clientname,
                                      @RequestParam("messagebody") String messagebody,
                                      @RequestParam("delay") Long delay) {
        return imqProducerService.sendScheduleMessage(clientname, messagebody, delay);
    }

    @PostMapping("/getFirstActivity")
    public InfoClusterOperationPlanActivityLogWithBLOBs getFirstActivity(@RequestParam("planId") String planId) {
        return planExecService.getFirstActivity(planId);
    }

    /**
     * 校验用户自定义脚本URI是否包含 sdp.wgetpath domain
     *
     * @param customScriptUri 自定义脚本，例如：<br/> https://sasdpscriptstmp.blob.core.windows.net/sunbox3/shell/customshell2.sh
     */
    @PostMapping("/checkCustomScriptUri")
    ResultMsg checkCustomScriptUri(@RequestParam("customScriptUri") String customScriptUri) {
        return playBookService.checkCustomScriptUri(customScriptUri);
    }

    @GetMapping("/getClusterBlueprint")
    ResultMsg getClusterBlueprint(@RequestParam("clusterId") String clusterId) {
        return clusterService.getClusterBlueprint(clusterId);
    }

    /**
     * 创建资源组
     *
     * @return resultMsg
     */
    @RequestMapping(value = "/createResourceGroup", method = RequestMethod.POST)
    public ResultMsg createResourceGroup(@RequestBody String azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = iAzureService.createResourceGroup(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    @RequestMapping(value = "/uploadFileToBlob", method = RequestMethod.POST)
    public ResultMsg uploadFileToBlob(@RequestParam("fileName") String fileName, @RequestParam("fileContent") String fileContent,@RequestParam String region) {
        ResultMsg result = new ResultMsg();
        String blobPath = playBookService.uploadYml2MsBlob(fileName, fileContent,region);
        if (StringUtils.isEmpty(blobPath)) {
            result.setResult(false);
            result.setErrorMsg("上传文件失败，请重新上传：" + fileName);
        } else {
            result.setData(blobPath);
            result.setResult(true);
        }
        return result;
    }

    /**
     * 查看资源组
     *
     * @param clusterId
     * @return resultMsg
     */
    @RequestMapping(value = "/getResourceGroup", method = RequestMethod.GET)
    public ResultMsg getResourceGroup(@RequestParam("clusterId") String clusterId,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getResourceGroup(clusterId,region);
        return resultMsg;
    }


    /**
     * 更新资源组标签-全量
     *
     * @param azureResourceGroupTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/updateResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg updateResourceGroupTags(@RequestBody String azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = iAzureService.updateResourceGroupTags(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    /**
     * 更新资源组标签-增量
     *
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/addResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg addResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = iAzureService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 删除资源组标签
     *
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg deleteResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = iAzureService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     *
     * @return resultMsg
     */
    @RequestMapping(value = "/metas/supportedVMSkuList", method = RequestMethod.GET)
    public ResultMsg supportedVMSkuList(@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.supportedVMSkuList(region);
        return resultMsg;
    }

    @RequestMapping(value = "/getLogsBlobContainerList", method = RequestMethod.GET)
    public ResultMsg supportedLogsBlobContainerList(@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getLogsBlobContainerList(region);
        return resultMsg;
    }

    /**
     * 单个创建VM实例
     *
     * @param azureVMInstanceRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/createVMInstance", method = RequestMethod.POST)
    public ResultMsg createVMInstance(@RequestBody String azureVMInstanceRequest) {
        ResultMsg resultMsg = iAzureService.createVMInstance(azureVMInstanceRequest);
        return resultMsg;
    }

    /**
     * 单个删除VM实例
     *
     * @param vmName
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteVMInstance", method = RequestMethod.GET)
    public ResultMsg deleteVMInstance(@RequestParam("vmName") String vmName, @RequestParam(value = "dnsName", required = false) String dnsName, @RequestParam("region") String region) {
        ResultMsg resultMsg = iAzureService.deleteVMInstance(vmName, dnsName, region);
        return resultMsg;
    }

    /**
     * 批量创建VM实例
     *
     * @param appendVMsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/appendVirtualMachines", method = RequestMethod.POST)
    public ResultMsg appendVirtualMachines(@RequestBody String appendVMsRequest) {
        ResultMsg resultMsg = iAzureService.appendVirtualMachines(appendVMsRequest);
        return resultMsg;
    }

    /**
     * 批量删除VM实例
     *
     * @param deleteVMsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteVirtualMachines", method = RequestMethod.POST)
    public ResultMsg deleteVirtualMachines(@RequestBody String deleteVMsRequest) {
        ResultMsg resultMsg = azureServiceSer.deleteVirtualMachines(deleteVMsRequest);
        return resultMsg;
    }

    /**
     * 批量/单个VM扩容磁盘
     *
     * @param azureUpdateVirtualMachinesDiskSizeRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/updateVirtualMachinesDiskSize", method = RequestMethod.POST)
    public ResultMsg updateVirtualMachinesDiskSize(@RequestBody String azureUpdateVirtualMachinesDiskSizeRequest) {
        ResultMsg resultMsg = iAzureService.updateVirtualMachinesDiskSize(azureUpdateVirtualMachinesDiskSizeRequest);
        return resultMsg;
    }

    /**
     * 获取AZ列表
     */
    @RequestMapping(value = "/getazlist")
    public ResultMsg getAzList(@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getAzList(region);
        return resultMsg;
    }

    /**
     * 查询vmSku价格
     */
    @RequestMapping(value = "/getInstancePrice")
    public ResultMsg getInstancePrice(@RequestParam("skuName") String skuName,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getInstancePrice(skuName,region);
        return resultMsg;
    }

    /**
     * 竞价实例驱逐率
     */
    @PostMapping(value = "/spotEvictionRate")
    public ResultMsg spotEvictionRate(@RequestBody Map<String, Object> param) {
        List<String> skuNames = (List<String>)param.get("skuNames");
        ResultMsg resultMsg = iAzureService.spotEvictionRate(skuNames,param.get("region").toString());
        return resultMsg;
    }

    /**
     * 竞价实例历史价格
     */
    @PostMapping(value = "/spotPriceHistory")
    public ResultMsg spotPriceHistory(@RequestBody Map<String, Object> param) {
        List<String> skuNames = (List<String>)param.get("skuNames");
        String region = param.get("skuNames").toString();
        ResultMsg resultMsg = iAzureService.spotPriceHistory(skuNames,region);
        return resultMsg;
    }

    @GetMapping(value = "/provisionDetail")
    public ResultMsg provisionDetail(@RequestParam("jobId") String jobId,@RequestParam("region") String region) {
        ResultMsg resultMsg = iAzureService.provisionDetail(jobId,region);
        return resultMsg;
    }

    @PostMapping("/saveuserscript")
    public ResultMsg saveuserscript(@RequestBody String saveuserscript) {
        JSONObject jsonObject = JSON.parseObject(saveuserscript);
        ConfClusterScript confClusterScript = new ConfClusterScript();
        confClusterScript.setClusterId(jsonObject.getString("clusterId"));
        confClusterScript.setScriptPath(jsonObject.getString("scriptPath"));
        confClusterScript.setScriptName(jsonObject.getString("jobName"));
        confClusterScript.setScriptParam(jsonObject.getString("scriptParam"));
        confClusterScript.setRunTiming("ontime");
        confClusterScript.setConfScriptId(UUID.randomUUID().toString());

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(confClusterScript.getClusterId());

        if (Objects.isNull(cluster)) {
            ResultMsg result = new ResultMsg();
            result.setResult(false);
            result.setErrorMsg("没找到集群：clusterId = " + confClusterScript.getClusterId());
            return result;
        }

        // 状态为2时是运行中集群，其它状态均为不正常状态
        if (!Objects.equals(cluster.getState(), ConfCluster.CREATED)) {
            ResultMsg result = new ResultMsg();
            result.setResult(false);
            result.setErrorMsg("集群状态不能创建脚本执行任务，当前集群状态： " + cluster.getState());
            return result;
        }

        List<InfoClusterVm> vms = new CopyOnWriteArrayList<>();
        String[] groups = jsonObject.getString("groupName").toLowerCase(Locale.ROOT).split(",");

        for (String s : groups) {
            List<InfoClusterVm> vmList = infoClusterVmMapper
                    .selectByClusterIdAndGroupNameAndState(confClusterScript.getClusterId(), s,InfoClusterVm.VM_RUNNING);
            vms.addAll(vmList);
        }


        String nodelist = vms.stream().map(item -> {
            return item.getInternalip();
        }).collect(Collectors.joining(","));

        return playBookService.saveUserScriptPlayBookJob(confClusterScript, UUID.randomUUID().toString(),
                nodelist,cluster.getRegion());
    }


    @RequestMapping("/retryactivity")
    public ResultMsg retryactivity(@RequestParam("activityLogId") String activityLogId) {
        return planExecService.retryActivity(activityLogId);
    }


    @RequestMapping("/getblobpath")
    public ResultMsg getBolbPath() {
        ResultMsg msg = new ResultMsg();
        msg.setData(blobpath);
        msg.setResult(true);
        return msg;
    }

    @RequestMapping("/getRunningHostsFromYarn")
    public ResultMsg getRunningHostsFromYarn(@RequestParam("clusterId") String clusterId) {
        ResultMsg msg = new ResultMsg();
        List<String> runningHosts = ambariService.getRunningHostsFromYarn(clusterId);
        msg.setResult(true);
        msg.setData(runningHosts);
        return msg;
    }

    //region 伸缩

    /**
     * 批量删除VM实例
     *
     * @param scalingTask
     * @return resultMsg
     */
    @PostMapping(value = "/createScaleInTask")
    public ResultMsg createScaleInTask(@RequestBody ConfScalingTask scalingTask) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg = scalingService.createScaleinTask(scalingTask);
        return resultMsg;
    }

    @PostMapping(value = "/createScaleInTaskbyVms")
    public ResultMsg createScaleInTask(@RequestParam("clusterId") String clusterId,
                                       @RequestParam("groupName") String groupName,
                                       @RequestParam("vmRole") String vmRole,
                                       @RequestParam("vms") List<String> vms) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setErrorMsg("不支持的方法");
//        resultMsg = scalingService.createScaleinTask(null, clusterId, vmRole, groupName, vms);
        return resultMsg;
    }

    @PostMapping(value = "/createScaleOutTask")
    public ResultMsg createScaleOutTask(@RequestBody ConfScalingTask scalingTask) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg = scalingService.createScaleOutTask(scalingTask);
        return resultMsg;
    }

    @PostMapping(value = "/createScalePartOutTask")
    public ResultMsg createScalePartOutTask(@RequestBody ConfScalingTask scalingTask) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg = scalingService.createScalePartOutTask(scalingTask);
        return resultMsg;
    }

    @PostMapping(value = "/createConfigGroup")
    public ResultMsg createConfigGroup(@RequestParam("clusterId") String clusterId, @RequestParam("groupName") String groupName, @RequestParam("groupId") String groupId, @RequestParam("vmRole") String vmRole) {
        return clusterService.createConfigGroup(clusterId, groupName, groupId, vmRole);
    }


    @PostMapping(value = "/resizeCluster")
    public ResultMsg resizeCluster(@RequestBody Map<String, Object> resizeModel) {
        return scalingService.resizeClusterGroup(resizeModel);
    }

    //endregion

    /**
     * 重启大数据服务
     */
    @PostMapping(value = "/restartClusterService")
    public ResultMsg restartClusterService(@RequestBody Map<String, Object> param) {
        return clusterService.restartClusterService(param);
    }

    @GetMapping(value = "/activeComponentHostName/{clusterId}/{componentName}")
    public ResultMsg getActiveComponentHostName(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("componentName") String componentName) {
        return ambariService.getActiveComponentHostName(clusterId, componentName);
    }

    @GetMapping(value = "/componentHostName/{clusterId}/{componentName}")
    public ResultMsg getComponentHostName(@PathVariable("clusterId") String clusterId,
                                          @PathVariable("componentName") String componentName) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            getLogger().info("getComponentHostName,ClusterId:" + clusterId);
            ResultMsg cmsg = clusterService.checkClusterAvailable(clusterId);
            if (!cmsg.getResult()) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("集群状态不可用。");
                getLogger().warn("getComponentHostName，集群状态不可用，clusterId:" + clusterId);
                return resultMsg;
            }
            QueryComponentInHostsResponse componentInHosts = ambariService.getComponentInHosts(clusterId, componentName);

            List<HostRole> hosts = componentInHosts.getHosts(componentName, false);
            if (CollectionUtils.isNotEmpty(hosts)) {
                List<String> hostNameList = hosts.stream().map(HostRole::getHostName).collect(Collectors.toList());
                resultMsg.setResultSucces("Success");
                resultMsg.setData(hostNameList);
            } else {
                resultMsg.setResultFail("没有找到主机");
                resultMsg.setErrorMsg("没有找到主机");
            }
            return resultMsg;
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            resultMsg.setResultFail(ex.getMessage());
            resultMsg.setErrorMsg(ex.getMessage());
            return resultMsg;
        }
    }

    @PostMapping(value = "/scaleInForDeleteTaskVm")
    public ResultMsg scaleInForDeleteVm(@RequestBody Map<String, Object> param){
        return scalingService.scaleInForDeleteTaskVm(param.get("clusterId").toString(),
                param.get("groupId").toString(),
                null,
                (List<String>) param.get("vms"),
                null);
    }

    /**
     * 因需要删除虚拟机，所以产生缩容
     */
    @PostMapping(value = "/createScaleInForDeleteTaskVm")
    public ResultMsg createScaleInForDeleteTaskVm(@RequestBody ScaleInForDeleteTaskVmReq param){
        getLogger().debug("begin scaleInForDeleteTaskVm param:{}", param);
        return scalingService.scaleInForDeleteTaskVm(param.getClusterId(), param.getGroupId(), param.getScaleOutTaskId(), param.getVmNames(), param.getCreatedTime());
    }

    //region 竞价实例
    /**
     * 竞价实例逐出缩容
     *
     * @param param Stirng clusterId 集群ID
     *              String groupId 实例组ID
     *              List<String> vms 缩容数量
     * @return
     */
    @PostMapping(value = "/spot/scalein")
    public ResultMsg spotScaleIn(@RequestBody Map<String, Object> param) {
        return scalingService.scaleInGroupForSpot(param.get("taskId").toString(),
                param.get("clusterId").toString(),
                param.get("groupId").toString(),
                Integer.parseInt(param.get("expectCount").toString()),
                (List<String>) param.get("vms"));
    }

    /**
     * 竞价实例扩容
     *
     * @param param clusterId 集群ID
     *              groupId 实例组ID
     *              scaleCount 扩容数量
     * @return
     */
    @PostMapping(value = "/spot/scaleout")
    public ResultMsg spotScaleOut(@RequestBody Map<String, Object> param) {
        return scalingService.spotInsGroupScaleOut(param.get("taskId").toString(),
                param.get("clusterId").toString(),
                param.get("groupId").toString(),
                Integer.parseInt(param.get("expectCount").toString()),
                Integer.parseInt(param.get("scaleCount").toString())
        );
    }
    // endregion

    /**
     * 更新集群配置
     */
    @PostMapping(value = "/updateClusterConfig")
    public ResultMsg updateClusterConfig(@RequestBody String jsonStr) {
        ResultMsg resultMsg = clusterService.updateClusterConfig(jsonStr);
        return resultMsg;
    }

    @GetMapping(value = "/updateLocalHostGroup/{clusterId}")
    public ResultMsg updateLocalHostGroupFromAmbari(@PathVariable("clusterId") String clusterId) {
        return clusterService.updateLocalClusterConfig(clusterId);
    }

    /**
     * 获取SKU实时价格
     */
    @PostMapping(value = "/getSpotVmRealtimePrice")
    public ResultMsg<VmRealtimePriceModel> getSpotVmRealtimePrice(@RequestParam("skuName") String skuName,@RequestParam String region) {
        ResultMsg spotVmRealtimePrice = iAzureService.getSpotVmRealtimePrice(skuName,region);
        if (spotVmRealtimePrice.getResult() && spotVmRealtimePrice.getData() != null) {
            AzureServiceManager.VmRealtimePrice data = (AzureServiceManager.VmRealtimePrice) spotVmRealtimePrice.getData();
            VmRealtimePriceModel vmRealtimePriceModel = new VmRealtimePriceModel();
            vmRealtimePriceModel.setRtPrice(data.getRtPrice());
            vmRealtimePriceModel.setVmName(data.getVmName());
            vmRealtimePriceModel.setRtPrice(data.getRtPrice());
            return ResultMsg.SUCCESST(vmRealtimePriceModel);
        }
        return ResultMsg.FAILURE("获取实例规格价格失败," + spotVmRealtimePrice.getErrorMsg());
    }

    /**
     * 更新执行计划状态和百分比
     * @param planId
     * @return
     */
    @RequestMapping("/updatePlanStateAndPercent")
    public ResultMsg  updatePlanStateAndPercent(@RequestParam("planId") String planId){
        return planExecService.updatePlanStateAndPercent(planId);
    }

    /**
     * 更新执行计划名称
     * @param planId
     * @return
     */
    @RequestMapping("/updatePlanName")
    public ResultMsg  updatePlanName(@RequestParam("planId") String planId){
        return planExecService.updatePlanName(planId);
    }


    /**
     * Spot逐出事件VM状态处理
     * 接口逻辑：只将对应VM状态修改为UnKnown状态，以保证SDP元数据时效性。
     * VM的竞价逐出（缩容）流程由Spot服务发起。
     * @param clusterId 集群ID
     * @param hostName  hostname 域名
     * @param evictionReason  逐出原因:逐出事件/探活失败 （暂时没有使用预留字段）
     * @return
     */
    @RequestMapping("/processeviction")
    public ResultMsg  processEviction(@RequestParam("clusterId") String clusterId,
                                      @RequestParam("hostName") String hostName,
                                      @RequestParam("evictionReason") String evictionReason){

        return ivmService.updateInfoClusterVMStateForSpotEvictionEvent(clusterId,hostName);
    }



    /**
     * POD 解析域名
     * @param hostName  hostname 域名
     * @return
     */
    @RequestMapping("/dnsresolve")
    public String  dnsresolve( @RequestParam("hostName") String hostName){
        return DNSUtil.getIPByDNS(hostName);
    }


    @RequestMapping("/startplan")
    public ResultMsg startPlan(@RequestParam("planId") String planId){
        return planExecService.startPlan(planId);
    }


    /**
     * 清理集群ambari host历史数据
     * @param clusterId
     * @param startDate
     * @return
     */
    @PostMapping("/cleanAmbariHistory")
    public ResultMsg cleanAmbariHistory(@RequestParam("clusterId") String clusterId,
                                @RequestParam("startDate") String startDate) {
        return playBookService.cleanAmbariHistory(clusterId, startDate);
    }

    /**
     * 删除一个AzureFleet
     * @param confCluster
     * @param groupName
     * @return
     */
    @PostMapping("/deleteAzureFleet")
    public ResultMsg deleteAzureFleet(@RequestBody ConfCluster confCluster, @RequestParam("groupName") String groupName) {
        return azureFleetService.deleteAzureFleet(confCluster, groupName);
    }

    @PostMapping("/collectClusterInfoList")
    public ResultMsg getCollectClusterInfoList(@RequestBody InfoClusterInfoCollectLog infoClusterInfoCollectLog){
        return clusterService.collectClusterInfoList(infoClusterInfoCollectLog);
    }

    @PostMapping("/collectClusterInfoByClusterId")
    public ResultMsg getCollectClusterInfoListByClusterId(@RequestParam("clusterId") String clusterId){
        return clusterService.collectClusterInfo(clusterId);
    }
    @PostMapping("/testHostGroup/{clusterId}/{groupName}/{skuName}")
    public void testHostGroup(@PathVariable("clusterId") String clusterId,
                              @PathVariable("groupName") String groupName,
                              @PathVariable("skuName") String skuName,
                              @RequestParam("serviceName") String serviceName,
                              @RequestBody List<Map<String, Object>> desiredConfigs
    ){
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        ClusterHostGroupManager clusterHostGroupManager = new ClusterHostGroupManager(clusterId, confCluster.getClusterName());
        clusterHostGroupManager.saveHostGroup(groupName,skuName,serviceName,desiredConfigs);
    }
}
