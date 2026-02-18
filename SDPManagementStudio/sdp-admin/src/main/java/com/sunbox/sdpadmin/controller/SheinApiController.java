package com.sunbox.sdpadmin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.cluster.WorkorderCallbackRequest;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.filter.RegionMappingUtil;
import com.sunbox.sdpadmin.model.shein.request.*;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.SheinApiService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.web.BaseCommon;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : [niyang]
 * @className : SheinApiController
 * @description : [SHEIN平台数据接口]
 * @createTime : [2022/11/29 2:04 PM]
 */
@RestController
@RequestMapping(path ={"/sheinapi/v1","/trdapi/v1"})
public class SheinApiController extends BaseCommon {

    @Autowired
    private SheinApiService sheinApiService;

    @Autowired
    private AdminApiService adminApiService;

    @Autowired
    private ComposeService composeService;

    /**
     * 创建集群
     */
    @PostMapping("/createCluster")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel createCluster(@RequestBody String jsonStr) {
        return sheinApiService.createClusterNew(jsonStr);
    }

    /**
     * 1.查询集群版本信息
     * GET
     * @param releaseLabelPrefix 集群发行版本前缀
     * @param appVer             集群应用与版本号
     * @return
     */
    @GetMapping("/listClusterReleaseLabels")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel listClusterReleaseLabels(@RequestParam(name = "releaseLabelPrefix", required = false) String releaseLabelPrefix,
                                                       @RequestParam(name = "appVer", required = false) String appVer) {
        String appName = "";
        String appVersion = "";
        if (StringUtils.isNotEmpty(appVer)) {
            if (appVer.contains("@")) {
                String[] versionArray = appVer.split("@");
                appName = versionArray[0];
                appVersion = versionArray[1];
            } else {
                appVersion = appVer;
            }
        }
        return sheinApiService.listClusterReleaseLabels(releaseLabelPrefix, appVersion, appName);
    }

    /**
     * 查询集群实例组
     * GET
     * @param id EMR集群ID
     * @param emrlGType 集群实例组类型
     * @param dc 数据中心
     * @return
     */
    // @GetMapping("/listInstanceGroups")
   /* public SheinResponseModel listInstanceGroups(@RequestParam(name = "id") String id,
                                                 @RequestParam(name = "emrlGType", required = false) String emrlGType,
                                                 @RequestParam(name = "dc", required = false) String dc) {
        getLogger().info("查询集群实例组 入参 ： {}， {}， {}", id, emrlGType, dc);

        return sheinApiService.listInstanceGroups(id, emrlGType, dc);
    }
*/
    /**
     * 查询集群实例组-适配实例组id
     */
    @GetMapping("/listInstanceGroups")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel listInstanceGroups(@RequestParam(name = "id") String id,
                                                 @RequestParam(name = "emrlGType", required = false) String emrlGType,
                                                 @RequestParam(name = "dc", required = false) String dc,
                                                 @RequestParam(name = "insGpId", required = false) String insGpId) {
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        getLogger().info("查询集群实例组, 入参: {}, {}, {}, {}", id, emrlGType, dc, insGpId);
        return sheinApiService.listInstanceGroups(id, emrlGType, dc, insGpId);
    }

    /**
     * 4.查询集群实例组实例资源
     *
     * @param id         EMR集群ID
     * @param insGpId    集群实例队列ID
     * @param insGpTypes 集群实例队列类型
     * @param insStatus  PROVISIONING，BOOTSTRAPPING，RUNNING
     * @param dc         数据中心
     */
    @GetMapping("/listClusterGroupInstances")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel listClusterGroupInstances(@RequestParam(value = "id", required = false) String id,
                                                        @RequestParam(value = "insGpId", required = false) String insGpId,
                                                        @RequestParam(value = "insGpTypes", required = false) String insGpTypes,
                                                        @RequestParam(value = "insStatus", required = false) String insStatus,
                                                        @RequestParam(value = "dc", required = false) String dc) {
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        getLogger().info("查询集群实例组实例资源 入参：id:{}, insGpId:{}, insGpTypes:{}, insStatus:{}, dc:{}", id, insGpId, insGpTypes, insStatus, dc);
        return sheinApiService.listClusterGroupInstances(id, insGpId, insGpTypes, insStatus, dc);
    }

    /**
     * 5. 查询集群应用信息
     * GET
     * @param id
     * @param releaseLabel
     * @return
     */
    @GetMapping("/descClusterReleaseLabel")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel descClusterReleaseLabel(@RequestParam(name="id",required = false) String id,
                                                      @RequestParam(name="releaseLabel",required = false) String releaseLabel){
        return sheinApiService.descClusterReleaseLabel(id,releaseLabel);
    }

    /**
     * 6. 查询可用EMR集群信息
     * GET
     * @param dc
     * @return
     */
    @GetMapping("/listAvailableClusters")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel listAvailableClusters(@RequestParam String dc){
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        getLogger().info("查询可用EMR集群信息 入参：{}", dc);
        return sheinApiService.listAvailableClustersNew(dc);
    }

    /**
     * 7. 查询EMR集群详情
     * GET
     * @param id
     * @param dc
     * @return
     */
    @GetMapping("/descCluster")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel descCluster(@RequestParam String id,
                                          @RequestParam(required = false) String dc){
        return sheinApiService.descCluster(id, dc);
    }

    /**
     * 8. 查询EMR集群信息
     * GET
     * @param stateList
     * @param begtime
     * @param endtime
     * @param dc
     * @return
     */
    @GetMapping("/listClusters")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel listClusters(@RequestParam(name = "stateList",required = false) String stateList,
                                           @RequestParam(name = "begtime",required = false) String begtime,
                                           @RequestParam(name ="endtime",required = false) String endtime,
                                           @RequestParam(name = "dc",required = false) String dc){
        return sheinApiService.listClusters(stateList, begtime, endtime, dc);
    }

    /**
     * 获取vmsku列表
     * @return
     */
    @GetMapping("/getvmskulist/{region}")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getVmSkuList(@PathVariable String region){
        return sheinApiService.getVmSkuList(region);
    }

    /**
     * 获取子网列表
     * @return
     */
    @GetMapping("/getsubnetlist")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getSubnetList(@RequestParam String region){
        return sheinApiService.getSubnetList(region);
    }

    /**
     * 获取磁盘类型列表
     * @return
     */
    @GetMapping("/getosdisktypelist")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getOsDiskTypeList(@RequestParam String region){
        return sheinApiService.getOsDiskTypeList(region);
    }

    /**
     * 获取登录方式密钥对列表
     * @return
     */
    @GetMapping("/getkeypairlist")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getKeypairList(@RequestParam String region){
        return sheinApiService.getKeypairList( region);
    }

    /**
     * 获取主安全组列表
     * @return
     */
    @GetMapping("/getprimarysecuritygrouplist")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getPrimarySecurityGroupList(@RequestParam String region){
        return sheinApiService.getPrimarySecurityGroupList(region);
    }

    /**
     * 获取子安全组列表
     */
    @GetMapping("/getsubsecuritygrouplist")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getSubSecurityGroupList(@RequestParam(required = false)String region){
        return sheinApiService.getSubSecurityGroupList(region);
    }

    /**
     * 删除集群
     */
    @RequestMapping(value = "/terminateCluster", method = RequestMethod.POST)
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel deleteCluster(@RequestParam(value = "clusterId", required = false) String clusterId,
                                            @RequestParam(value = "fDel", required = false) String fDel) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (StringUtils.isEmpty(clusterId)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少请求参数");
            return sheinResponseModel;
        }

        Map<String, String> clustermap = new HashMap<>();
        clustermap.put("clusterId", clusterId);
        clustermap.put("fDel", fDel);
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(clustermap));
        ResultMsg resultMsg = adminApiService.deleteCluster(jsonObject.toJSONString(), "sysadmin");
        return sheinApiService.resultMsg2SheinResponseModel(resultMsg);
    }

    /**
     * 更新资源组标签-全量
     */
    @RequestMapping(value = "/updateClusterTags", method = RequestMethod.POST)
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel updateResourceGroupTags(@RequestBody String azureResourceGroupTagsRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.updateResourceGroupTags(azureResourceGroupTagsRequest);
        return sheinResponseModel;
    }

    /**
     * 更新资源组标签-增量
     */
    @RequestMapping(value = "/addClusterTags", method = RequestMethod.POST)
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel addResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
        return sheinResponseModel;
    }

    /**
     * 删除资源组标签
     */
    @RequestMapping(value = "/delClusterTags", method = RequestMethod.POST)
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel deleteResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
        return sheinResponseModel;
    }

    /**
     * 集群实例组Resize（手动扩缩容）
     */
    @PostMapping("/modifyClusterInstanceGroup")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel modifyClusterInstanceGroup(@RequestBody Map<String, Object> resizeRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.modifyClusterInstanceGroup(resizeRequest, true);
        return sheinResponseModel;
    }

    /**
     * 集群多实例组Resize（多实例组手动扩缩容）
     */
    @PostMapping("/modifyClusterInstanceGroups")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel modifyClusterInstanceGroups(@RequestBody Map<String, Object> resizeRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.modifyClusterInstanceGroups(resizeRequest);
        return sheinResponseModel;
    }

    /**
     * 重启大数据服务
     */
    @PostMapping("/restartClusterService")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel restartClusterService(@RequestBody Map<String, Object> request) {
        SheinResponseModel sheinResponseModel = sheinApiService.restartClusterService(request);
        return sheinResponseModel;
    }

    /**
     * 查询重启任务结果
     */
    @GetMapping("/getRestartTaskResult")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel getRestartTaskResult(@RequestParam(required = false, value = "clusterId") String clusterId,
                                                   @RequestParam(required = false, value = "taskId") String taskId) {
        SheinResponseModel sheinResponseModel = sheinApiService.getRestartTaskResult(clusterId, taskId);
        return sheinResponseModel;
    }

    /**
     * 更新集群配置
     */
    @PostMapping("/updateClusterConfig")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel updateClusterConfig(@RequestBody UpdateClusterConfigData updateClusterConfigData) {
        SheinResponseModel sheinResponseModel = sheinApiService.updateClusterConfig(updateClusterConfigData);
        return sheinResponseModel;
    }

    /**
     * 执行脚本
     */
    @PostMapping("/saveAndExecuteScript")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel saveAndExecuteScript(@RequestBody SaveAndExecuteScriptData scriptRequest) {
        SheinResponseModel sheinResponseModel = sheinApiService.saveAndExecuteScript(scriptRequest);
        return sheinResponseModel;
    }

    /**
     * 实例组弹性伸缩规则-附加
     */
    @PostMapping("/saveElasticScalingRule")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel saveElasticScalingRule(@RequestBody SheinElasticScalingData sheinElasticScalingData) {
        SheinResponseModel sheinResponseModel = sheinApiService.saveElasticScalingRule(sheinElasticScalingData);
        return sheinResponseModel;
    }

    /**
     * 集群弹性伸缩规则-剥离
     */
    @PostMapping("/terminateElasticScalingRule")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel terminateElasticScalingRule(@RequestBody SheinElasticScalingData sheinElasticScalingData) {
        SheinResponseModel sheinResponseModel = sheinApiService.terminateElasticScalingRule(sheinElasticScalingData);
        return sheinResponseModel;
    }

    /**
     * 集群新增实例组
     */
    @PostMapping("/addInstanceGroup")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel addInstanceGroup(@RequestBody SheinInstanceGroupData instanceGroupData) {
        SheinResponseModel sheinResponseModel = sheinApiService.addInstanceGroup(instanceGroupData);
        return sheinResponseModel;
    }

    /**
     * 查询spot买入逐出统计
     *
     * @param clusterId 集群ID
     * @param skuName 实例名称
     * @param dc 数据中心
     * @param endTime 截止时间
     * @return
     */
    @GetMapping("/spotstatic")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel spotStatic(@RequestParam(value = "clusterId",required = false) String clusterId,
                                         @RequestParam(value = "skuName",required = false) String skuName,
                                         @RequestParam(value = "dc",required = false) String dc,
                                         @RequestParam(value = "endTime",required = false) String endTime){
        return sheinApiService.getSpotStatic(clusterId,skuName,endTime,dc);
    }


    /**
     * 更新实例组弹性数量范围
     *
     */
    @PostMapping("/updatescalevmscope")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel updateScaleVmScope(@RequestBody SheinElasticScalingData elasticScalingData) {
        SheinResponseModel sheinResponseModel = sheinApiService.updateScaleVmScope(elasticScalingData);
        return sheinResponseModel;
    }

    /**
     * 更新弹性伸缩规则
     *
     */
    @PostMapping("/updateesrule")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel updateEsRule(@RequestBody SheinElasticScalingRuleData elasticScalingData){
        return sheinApiService.updateEsRule(elasticScalingData);
    }

    /**
     * 新增弹性伸缩规则
     *
     * @param addRuleData
     * @return
     */
    @PostMapping("/addesrule")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel addesrule(@RequestBody SheinElasticScalingAddRuleData addRuleData){
        return sheinApiService.addEsRule(addRuleData);
    }

    /**
     * 管控全托管弹性扩缩容
     *
     * @param request
     * @return
     */
    @PostMapping("/fullCustodyControl")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel fullCustodyControl(@RequestBody FullCustodyRequest request){
        return sheinApiService.fullCustodyControl(request);
    }


    @GetMapping("/pendingscaletasks")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READ})
    public SheinResponseModel scalinglogs(@RequestParam(value = "clusterId",required = true) String clusterId,
                                          @RequestParam(value = "groupName",required = true) String groupName){
        return sheinApiService.getPendingSaleTask(clusterId,groupName);
    }

    @GetMapping("/getToken")
    public SheinResponseModel getToken(@RequestParam(value = "timestamp") String timestamp,
                                          @RequestParam(value = "ak") String ak,
                                       @RequestParam(value = "sign") String sign){

        return  sheinApiService.getToken(timestamp,ak,sign);
    }


    /**
     * 同步外部回调 .包含:创建, 销毁  /sheinapi/v1/workorder/callback
     * @param
     * @return
     */
    @PostMapping("/workorder/callback")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel workOrderCallback(@RequestBody WorkorderCallbackRequest workorderCallbackRequest) {
        return sheinApiService.workOrderCallback(workorderCallbackRequest);
    }


    /**
     * 变更PV2磁盘信息
     */
    @PostMapping("/pv2DiskInfo")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel pv2DiskInfo(@RequestBody SheinDiskPerformance request) {
        return sheinApiService.pv2DiskInfo(request);
    }

    /**
     * 获取VM实例详情信息
     * @param vmInstanceRequest
     * @return
     */
    @GetMapping("/vmInstanceDetail")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel vmInstanceDetail(VmInstanceDetailRequest vmInstanceRequest){
        return sheinApiService.vmInstanceDetail(vmInstanceRequest);
    }
    /**
     * 获取集群中VM实例列表
     * @param vmInstanceRequest
     * @return
     */
    @GetMapping("/vmInstancesByClusterId")
    @PermissionLimit(sheinPermission = {SheinParamConstant.READWRITE})
    public SheinResponseModel vmInstancesByClusterId( VmInstanceDetailRequest vmInstanceRequest){
        return sheinApiService.vmInstancesByClusterId(vmInstanceRequest);
    }
}
