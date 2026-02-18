package com.sunbox.sdpservice.service;

import com.sunbox.domain.*;
import com.sunbox.sdpservice.data.compose_cloud.ScaleInForDeleteTaskVmReq;
import com.sunbox.sdpservice.data.compose_cloud.ScaleInResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("SDP-COMPOSE")
@RestController
@RequestMapping("/compose")
public interface ComposeService {
    /*@GetMapping("/geVmSkus")
    ResultMsg geVmSkus();

    @GetMapping("/getMIList")
    ResultMsg getMIList();

    @GetMapping("/getLogsBlobContainerList")
    ResultMsg getLogsBlobContainerList();

    @GetMapping("/getSubnet")
    ResultMsg getSubnet();
*/
    @PostMapping("/createplan")
    ResultMsg createPlan(@RequestParam("clusterId") String clusterId,
                         @RequestParam("operationName") String operationName,
                         @RequestParam("releaseVersion") String releaseVersion);

    @PostMapping("/createplanforscaling")
    public ResultMsg createPlanforscaling(@RequestParam("clusterId") String clusterId,
                                          @RequestParam("operationName") String operationName,
                                          @RequestParam("releaseVersion") String releaseVersion,
                                          @RequestParam("taskId") String taskId);

    @RequestMapping("/startplan")
    ResultMsg startPlan(@RequestParam("planId") String planId);

    /*@GetMapping("/getDiskSku")
    ResultMsg getDiskSku();

    @GetMapping("/getNSGSku")
    ResultMsg getNSGSku();

    @GetMapping("/getSSHKeyPair")
    ResultMsg getSSHKeyPair();*/

    @PostMapping("/sendMessage")
    ResultMsg sendMessage(@RequestParam("clientname") String clientname,
                          @RequestParam("messagebody") String messagebody);

    @PostMapping("/getFirstActivity")
    InfoClusterOperationPlanActivityLogWithBLOBs getFirstActivity(@RequestParam("planId") String planId);

    /**
     * 校验用户自定义脚本URI是否包含 sdp.wgetpath domain
     *
     * @param customScriptUri 自定义脚本，例如：<br/>
     *                        https://sasdpscriptstmp.blob.core.windows.net/sunbox3/shell/customshell2.sh
     */
    @PostMapping("/checkCustomScriptUri")
    ResultMsg checkCustomScriptUri(@RequestParam("customScriptUri") String customScriptUri);

    @RequestMapping(value = "/uploadFileToBlob", method = RequestMethod.POST)
    ResultMsg uploadFileToBlob(@RequestParam("fileName") String fileName,
                               @RequestParam("fileContent") String fileContent,@RequestParam("region") String region);

    /**
     * ambari 查询状态
     *
     * @param activityLogId
     */
    @PostMapping("/getAmbariStatus")
    ResultMsg getAmbariStatus(@RequestParam("activityLogId") String activityLogId);

    /**
     * ansible 查询状态
     *
     * @param activityLogId
     */
    @PostMapping("/getAnsibleStatus")
    ResultMsg getAnsibleStatus(@RequestParam("activityLogId") String activityLogId);

    @GetMapping("/getClusterBlueprint")
    ResultMsg getClusterBlueprint(@RequestParam("clusterId") String clusterId);

    /**
     * 创建资源组
     *
     * @return resultMsg
     */
    @RequestMapping(value = "/createResourceGroup", method = RequestMethod.POST)
    ResultMsg createResourceGroup(@RequestBody String azureResourceGroupTagsRequest);

    /**
     * 查看资源组
     *
     * @param clusterId
     * @return resultMsg
     */
    @RequestMapping(value = "/getResourceGroup", method = RequestMethod.GET)
    ResultMsg getResourceGroup(@RequestParam("clusterId") String clusterId);

    /**
     * 删除资源组
     *
     * @param clusterId
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteResourceGroup", method = RequestMethod.GET)
    ResultMsg deleteResourceGroup(@RequestParam("clusterId") String clusterId);

    /**
     * 更新资源组标签-全量
     *
     * @param azureResourceGroupTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/updateResourceGroupTags", method = RequestMethod.POST)
    ResultMsg updateResourceGroupTags(@RequestBody String azureResourceGroupTagsRequest);

    /**
     * 更新资源组标签-增量
     *
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/addResourceGroupTags", method = RequestMethod.POST)
    ResultMsg addResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest);

    /**
     * 删除资源组标签
     *
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteResourceGroupTags", method = RequestMethod.POST)
    ResultMsg deleteResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest);

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     *
     * @return resultMsg
     */
    /*@RequestMapping(value = "/metas/supportedVMSkuList", method = RequestMethod.GET)
    ResultMsg supportedVMSkuList();*/

    /**
     * 单个创建VM实例
     *
     * @param azureVMInstanceRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/createVMInstance", method = RequestMethod.POST)
    ResultMsg createVMInstance(@RequestBody String azureVMInstanceRequest);

    /**
     * 单个删除VM实例
     *
     * @param vmName
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteVMInstance", method = RequestMethod.GET)
    ResultMsg deleteVMInstance(@RequestParam("vmName") String vmName);

    /**
     * 批量/单个VM扩容磁盘
     *
     * @param azureUpdateVirtualMachinesDiskSizeRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/updateVirtualMachinesDiskSize", method = RequestMethod.POST)
    ResultMsg updateVirtualMachinesDiskSize(@RequestBody String azureUpdateVirtualMachinesDiskSizeRequest);

    /**
     * 获取AZ列表
     */
    /*@RequestMapping(value = "/getazlist")
    ResultMsg getAzList();*/

    /**
     * 提交用户自定义脚本任务
     *
     * @param saveuserscript
     * @return
     */
    @PostMapping("/saveuserscript")
    ResultMsg saveuserscript(@RequestBody String saveuserscript);


    /**
     * 重试活动
     *
     * @param activityLogId
     * @return
     */
    @RequestMapping("/retryactivity")
    ResultMsg retryactivity(@RequestParam("activityLogId") String activityLogId);


    /**
     * 获取blob地址
     *
     * @return
     */
    /*@RequestMapping("/getblobpath")
    ResultMsg getBolbPath();*/

    /**
     * 从Yarn获取运行中的主机
     * @param clusterId 集群ID
     * @return
     */
    @RequestMapping("/getRunningHostsFromYarn")
    public ResultMsg getRunningHostsFromYarn(@RequestParam("clusterId") String clusterId);

    /**
     * 缩容
     *
     * @param scalingTask 缩容任务对象
     * @return
     */
    @PostMapping(value = "/createScaleInTask")
    ResultMsg createScaleInTask(@RequestBody ConfScalingTask scalingTask);

    /**
     * 指定vm进行缩容
     *
     * @param clusterId
     * @param groupName
     * @param vmRole
     * @param vms
     * @return
     */
    @PostMapping(value = "/createScaleInTaskbyVms")
    ResultMsg createScaleInTask(@RequestParam("clusterId") String clusterId,
                                @RequestParam("groupName") String groupName,
                                @RequestParam("vmRole") String vmRole,
                                @RequestParam("vms") List<String> vms);

    /**
     * 扩容
     *
     * @param scalingTask 扩容任务对象
     * @return
     */
    @PostMapping(value = "/createScaleOutTask")
    ResultMsg createScaleOutTask(@RequestBody ConfScalingTask scalingTask);

    /**
     * 扩容
     *
     * @param scalingTask 扩容任务对象
     * @return
     */
    @PostMapping(value = "/createScalePartOutTask")
    ResultMsg createScalePartOutTask(@RequestBody ConfScalingTask scalingTask);


    /**
     * 查询一个active状态的组件所在的主机
     *
     * @param clusterId     集群ID
     * @param componentName 组件名， 大写。可以参考BDComponent枚举
     * @return $.data里面是主机名
     */
    @GetMapping(value = "/activeComponentHostName/{clusterId}/{componentName}")
    ResultMsg getActiveComponentHostName(@PathVariable("clusterId") String clusterId,
                                         @PathVariable("componentName") String componentName);

    /**
     * resize
     *
     * @param resizeModel
     * @return
     */
    @PostMapping(value = "/resizeCluster")
    public ResultMsg resizeCluster(@RequestBody Map<String, Object> resizeModel);

    /**
     * 重启大数据服务
     */
    @PostMapping(value = "/restartClusterService")
    ResultMsg restartClusterService(@RequestBody Map<String, Object> param);

    /**
     * 查询一个active状态的组件所在的主机
     *
     * @param clusterId     集群ID
     * @param componentName 组件名， 大写。可以参考BDComponent枚举
     * @return $.data里面是主机名
     */
    @GetMapping(value = "/componentHostName/{clusterId}/{componentName}")
    ResultMsg getComponentHostName(@PathVariable("clusterId") String clusterId,
                                   @PathVariable("componentName") String componentName);

    @PostMapping(value = "/createConfigGroup")
    ResultMsg createConfigGroup(@RequestParam("clusterId") String clusterId,
                                @RequestParam("groupName") String groupName,
                                @RequestParam("groupId") String groupId,
                                @RequestParam("vmRole") String vmRole
    );

    /**
     * 因需要删除虚拟机，所以产生缩容
     * @param param Stirng clusterId 集群ID
     *              String groupId 实例组ID
     *              List<String> vms 缩容机器vmname列表
     * @return
     */
    @PostMapping(value = "/scaleInForDeleteTaskVm")
    ResultMsg scaleInForDeleteTaskVm(@RequestBody Map<String, Object> param);

    /**
     * 因需要删除虚拟机，所以产生缩容
     *
     * @return 缩容任务的任务ID
     */
    @PostMapping(value = "/createScaleInForDeleteTaskVm")
    ResultMsg createScaleInForDeleteTaskVm(@RequestBody ScaleInForDeleteTaskVmReq param);

    /**
     * 竞价实例逐出缩容
     *
     * @param param String taskId
     *              Stirng clusterId 集群ID
     *              String groupId 实例组ID
     *              List<String> vms 缩容机器vmname列表
     * @return
     */
    @PostMapping(value = "/spot/scalein")
    ResultMsg spotScaleIn(@RequestBody Map<String, Object> param);


    /**
     * 竞价实例扩容
     *
     * @param param String taskId
     *              String clusterId 集群ID
     *              String groupId 实例组ID
     *              Integer scaleCount 扩容数量
     * @return
     */
    @PostMapping(value = "/spot/scaleout")
    ResultMsg spotScaleOut(@RequestBody Map<String, Object> param);

    @PostMapping(value = "/growpart")
    ResultMsg growPart(@RequestBody String jsonStr);

    /**
     * 更新集群配置
     */
    @PostMapping(value = "/updateClusterConfig")
    ResultMsg updateClusterConfig(@RequestBody String jsonStr);

    /**
     * 查询vmSku价格
     */
   /* @RequestMapping(value = "/getInstancePrice")
    ResultMsg getInstancePrice(@RequestParam("skuName") String skuName);*/

    /**
     * 竞价实例驱逐率
     */
    /*@PostMapping(value = "/spotEvictionRate")
    ResultMsg spotEvictionRate(@RequestBody Map<String, Object> params);*/

    /**
     * 竞价实例历史价格
     */
   /* @PostMapping(value = "/spotPriceHistory")
    ResultMsg spotPriceHistory(@RequestBody Map<String, Object> params);*/

    /**
     * 获取SKU实时价格
     */
    /*@PostMapping(value = "/getSpotVmRealtimePrice")
    ResultMsg<VmRealtimePriceModel> getSpotVmRealtimePrice(@RequestParam("skuName") String skuName);*/

    /**
     * 更新执行计划状态和百分比
     * @param planId
     * @return
     */
    @RequestMapping("/updatePlanStateAndPercent")
    ResultMsg  updatePlanStateAndPercent(@RequestParam("planId") String planId);

    /**
     * 更新执行计划名称
     * @param planId
     * @return
     */
    @RequestMapping("/updatePlanName")
    public ResultMsg  updatePlanName(@RequestParam("planId") String planId);

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
    ResultMsg  processEviction(@RequestParam("clusterId") String clusterId,
                                      @RequestParam("hostName") String hostName,
                                      @RequestParam("evictionReason") String evictionReason);


    /**
     * POD 解析域名
     * @param hostName  hostname 域名
     * @return
     */
    @RequestMapping("/dnsresolve")
    String dnsresolve( @RequestParam("hostName") String hostName);

    /**
     * 清理集群ambari host历史数据
     * @param clusterId
     * @param startDate
     * @return
     */
    @PostMapping("/cleanAmbariHistory")
    ResultMsg cleanAmbariHistory(@RequestParam("clusterId") String clusterId,
                                        @RequestParam("startDate") String startDate);

    /**
     * 删除一个AzureFleet
     * @param confCluster
     * @param groupName
     * @return
     */
    @PostMapping("/deleteAzureFleet")
    ResultMsg deleteAzureFleet(@RequestBody ConfCluster confCluster, @RequestParam("groupName") String groupName);

    @PostMapping("/collectClusterInfoList")
    public ResultMsg getCollectClusterInfoList(@RequestBody InfoClusterInfoCollectLog infoClusterInfoCollectLog);

    @PostMapping("/collectClusterInfoByClusterId")
    public ResultMsg getCollectClusterInfoListByClusterId(@RequestParam("clusterId") String clusterId);

}
