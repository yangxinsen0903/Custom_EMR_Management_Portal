package com.sunbox.sdpadmin.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.domain.ambari.CleanAmbariHistoryRequest;
import com.sunbox.domain.ambari.DiskPerformanceRequest;
import com.sunbox.domain.cluster.UpdateDestroyStatusRequest;
import com.sunbox.domain.enums.ReleaseVersion;
import com.sunbox.domain.metaData.*;
import com.sunbox.domain.vmEvent.VmEventRequest;
import com.sunbox.domain.vmSku.ConfHostGroupVmSkuRequest;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpadmin.model.admin.request.*;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.IComposeMetaService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : AdminApiController
 * @description : [管控台前端数据接口]
 * @createTime : [2022/11/29 2:05 PM]
 */
@RestController
@RequestMapping("/admin")
public class AdminApiController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    @Autowired
    private AdminApiService adminApiService;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private IComposeMetaService composeMetaService;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private ConfClusterHostGroupMapper hostGroupMapper;

    @Autowired
    private IConfigInfoService configInfoService;

    @Autowired
    private BizConfigService bizConfigService;

    @Autowired
    private IVmEventService vmEventService;

    @Autowired
    private IConfHostGroupVmSkuService confHostGroupVmSkuService;

    /**
     * 集群概览
     */
    @GetMapping("/api/clusterOverview")
    public ResultMsg clusterOverview() {
        return adminApiService.clusterOverview();
    }

    /**
     * 查询集群列表数据
     *
     * @return
     */
    @PostMapping("/api/queryclusterlist")
    public ResultMsg queryClusterList(@RequestBody String jsonStr) {
        return adminApiService.queryClusterList(jsonStr);
    }

    /**
     * 获取集群可用的发行版本
     *
     * @return
     */
    @GetMapping("/api/getreleases")
    public ResultMsg getReleases() {
        return adminApiService.getReleases();
    }

    /**
     * 获取发行版本可用的应用组件
     *
     * @return
     */
    @PostMapping("/api/getreleaseapps")
    public ResultMsg getReleaseApps(@RequestBody String jsonStr) {
        return adminApiService.getReleaseApps(jsonStr);
    }

    @PostMapping("/api/getsceneapps")
    public ResultMsg getSceneApps(@RequestBody String jsonStr) {
        return adminApiService.getSceneApps(jsonStr);
    }

    /**
     * 获取发行版本可用的应用组件配置列表
     *
     * @return
     */
    @PostMapping("/api/getreleaseconfiglist")
    public ResultMsg getReleaseConfigList(@RequestBody String jsonStr) {
        return adminApiService.getReleaseConfigList(jsonStr);
    }

    /**
     * 获取子网列表
     *
     * @return
     */
    @GetMapping("/api/getsubnetlist")
    public ResultMsg getSubnetList(@RequestParam String region) {
        List<Subnet> dataItem = metaDataItemService.getsubnetlist(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取vmsku列表
     *
     * @return
     */
    @GetMapping("/api/getvmskulist")
    public ResultMsg getVmSkuList(@RequestParam String region) {
        List<VMSku> dataItem = metaDataItemService.getVMSKURecommend(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取日志容器
     * @return
     */
    @GetMapping("/api/getLogsBlobContainerList")
    public ResultMsg getLogsBlobContainerList(@RequestParam String region) {
        List<LogsBlobContainer> logsBlobContainerList = metaDataItemService.getLogsBlobContainerList(region);
        return ResultMsg.SUCCESS(logsBlobContainerList);
    }

    @GetMapping("/api/getMIList")
    public ResultMsg getMIList(@RequestParam String region) {
        List<ManagedIdentity> dataItem = metaDataItemService.getMIList(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取磁盘类型列表
     *
     * @return
     */
    @GetMapping("/api/getosdisktypelist")
    public ResultMsg getOsDiskTypeList(@RequestParam String region) {
        List<DiskSku> dataItem = metaDataItemService.getosdisktypelist(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取组件配置分类列表
     *
     * @return
     */
    @GetMapping("/api/getclassificationlist")
    public ResultMsg getClassificationList(@RequestParam(value = "releaseVersion", required = false) String releaseVersion) {
        if (StrUtil.isBlank(releaseVersion)) {
            releaseVersion = ReleaseVersion.SDP_1_0.getVersionValue();
        }
        ReleaseVersion.validate(releaseVersion);
        return adminApiService.getClassificationList(releaseVersion);
    }

    /**
     * 获取可用网络列表
     *
     * @return
     */
    @GetMapping("/api/getnetworklist")
    public ResultMsg getNetWorkList() {
        return adminApiService.getNetWorkList();
    }

    /**
     * 获取主安全组列表
     *
     * @return
     */
    @GetMapping("/api/getprimarysecuritygrouplist")
    public ResultMsg getPrimarySecurityGroupList(@RequestParam String region) {
        List<NSGSku> dataItem = metaDataItemService.getPrimaryNSGList(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取子安全组列表
     *
     * @return
     */
    @GetMapping("/api/getsubsecuritygrouplist")
    public ResultMsg getSubSecurityGroupList(@RequestParam String region) {
        List<NSGSku> dataItem = metaDataItemService.getPrimaryNSGList(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 获取登录方式密钥对列表
     *
     * @return
     */
    @GetMapping("/api/getkeypairlist")
    public ResultMsg getKeyPairList(@RequestParam String region) {
        List<SSHKeyPair> dataItem = metaDataItemService.getkeypairlist(region);
        return ResultMsg.SUCCESS(dataItem);
    }

    /**
     * 创建集群接口
     */
    @PostMapping("/api/createcluster")
    //@PermissionLimit(role = {"Maintainer","Administrator"}) 去掉,员工可以通过工单来创建集群
    public ResultMsg createCluster(@RequestBody String jsonStr) {
        try {
            return adminApiService.createCluster(jsonStr, getUserInfo().getUserName());
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    /**
     * 新增Task实例组
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/addgroup")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg addGroup(@RequestBody String jsonStr) {
        return adminApiService.addGroup(jsonStr);
    }

    /**
     * 磁盘扩容
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/growpart")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg growPart(@RequestBody String jsonStr) {
        return adminApiService.growPart(jsonStr);
    }

    /**
     * 查询集群接口
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/getcluster")
    public ResultMsg getCluster(@RequestBody String jsonStr) {
        return adminApiService.getCluster(jsonStr);
    }

    /**
     * 获取标签key列表
     *
     * @return
     */
    @GetMapping("/api/gettagkeylist")
    public ResultMsg getTagKeyList() {
        return adminApiService.getTagKeyList();
    }

    /**
     * 获取标签value列表
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/gettagvaluelist")
    public ResultMsg getTagValueList(@RequestBody String jsonStr) {
        return adminApiService.getTagValueList(jsonStr);
    }


    /**
     * 获取服务列表
     *
     * @return
     */
    @GetMapping("/api/getservicelist")
    public ResultMsg getServicelist(HttpServletRequest request) {
        ResultMsg<Map<String, String>> msg = new ResultMsg();
        try {
            msg = adminApiService.getServicelist(request);
        }catch (Exception e){
            logger.error("get getServicelist exception", e);
            msg.setResult(true);
            return msg;
        }
        return msg;
    }

    /**
     * 获取系统列表
     *
     * @return
     */
    @GetMapping("/api/getsystemlist")
    public ResultMsg getSystemlist(HttpServletRequest request) {
        long start = System.currentTimeMillis();
        ResultMsg<Map<String, String>> msg = new ResultMsg();
        try {
            msg = adminApiService.getSystemlist(request);
        }catch (Exception e){
            logger.error("get getSystemlist exception", e);
            msg.setResult(true);
            return msg;
        }
        getLogger().info("调用System全流程耗时：{} ms", (System.currentTimeMillis()-start));
        return msg;
    }


    /**
     * 检查数据库连接xx
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/checkconnect")
    public ResultMsg checkConnect(@RequestBody String jsonStr, HttpServletResponse httpServletResponse) {
        getLogger().info("checkConnectJson:"+jsonStr);
        return adminApiService.checkConnect(jsonStr, httpServletResponse);
    }

    /**
     * 检查集群名称是否重复
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/checkclustername")
    public ResultMsg checkClusterName(@RequestBody String jsonStr) {
        return adminApiService.checkClusterName(jsonStr);
    }

    /**
     * 获取任务列表
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/getjoblist")
    public ResultMsg getJobList(@RequestBody String jsonStr) {
        return adminApiService.getJobList(jsonStr);
    }

    /**
     * 获取任务详情
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/getjobdetail")
    public ResultMsg getJobDetail(@RequestBody String jsonStr) {
        return adminApiService.getJobDetail(jsonStr);
    }

    /**
     * 删除集群
     *
     * @param jsonStr
     * @return
     */
    @PostMapping("/api/deletecluster")
    // @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg deleteCluster(@RequestBody String jsonStr) {
        String userName = getUserInfo().getUserName();
        return adminApiService.deleteCluster(jsonStr, userName);
    }

    /**
     * 删除实例组
     *
     * @param deleteGroupModel
     * @return
     */
    @PostMapping("/api/deleteGroup")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg deleteGroup(@RequestBody DeleteGroupModel deleteGroupModel) {
        return adminApiService.deleteGroup(deleteGroupModel.getClusterId(), deleteGroupModel.getGroupName(), deleteGroupModel.getVmRole());
    }

    /**
     * 校验用户自定义脚本URI是否包含 sdp.wgetpath domain
     *
     * @param customScriptUri 自定义脚本，例如：<br/> https://sasdpscriptstmp.blob.core.windows.net/sunbox3/shell/customshell2.sh
     */
    @PostMapping("/api/checkcustomscripturi")
    public ResultMsg checkCustomScriptUri(@RequestParam("customScriptUri") String customScriptUri) {
        return adminApiService.checkCustomScriptUri(customScriptUri);
    }

    /**
     * ambari 查询状态
     *
     * @param activityLogId
     */
    @GetMapping("/api/getambaristatus")
    public ResultMsg getAmbariStatus(@RequestParam("activityLogId") String activityLogId) {
        return adminApiService.getAmbariStatus(activityLogId);
    }

    /**
     * ansible 查询状态
     *
     * @param activityLogId
     */
    @GetMapping("/api/getansiblestatus")
    public ResultMsg getAnsibleStatus(@RequestParam("activityLogId") String activityLogId) {
        return adminApiService.getAnsibleStatus(activityLogId);
    }

    /**
     * 查询集群详情信息
     *
     * @param clusterId 集群ID
     * @param fetchScalingRules 是否获取扩缩容规则，可为空， 默认为true（获取）
     * @return
     */
    @GetMapping("/api/getClusterDetail")
    //@PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg queryClusterInfo(@RequestParam("clusterId") String clusterId,
                                      @RequestParam(name = "fetchScalingRules", required=false) String fetchScalingRules) {
        Boolean fetchScalingRulesFlag = Convert.toBool(fetchScalingRules, true);
        return adminApiService.queryClusterInfo(clusterId, fetchScalingRulesFlag);
    }

    @GetMapping("/api/getAmbariDbNameManual")
    public ResultMsg getAmbariDbNameManual() {
        boolean manual = adminApiService.queryAmbariDbNameManual();
        ResultMsg result = new ResultMsg();
        result.setData(manual);
        result.setResult(true);
        return result;
    }

    @GetMapping("/api/downloadClusterBlueprint")
    ResultMsg getClusterBlueprint(@RequestParam("clusterId") String clusterId, HttpServletResponse response) {
        try {
            String blueprint = adminApiService.getClusterBlueprint(clusterId);
            response.setHeader("Content-Description", "File Transfer");
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + clusterId + "-blueprint.json");
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.getOutputStream().write(blueprint.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return new ResultMsg();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ResultMsg msg = new ResultMsg();
            msg.setData(ex.getMessage());
            return msg;
        }
    }

    @PostMapping("/api/uploadbasescript")
    ResultMsg uploadBaseScript(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        String scriptName = req.getParameter("scriptName");
        String remark = req.getParameter("remark");
        System.out.println(scriptName + "   " + remark);
        // 准备参数
        BaseScript script = new BaseScript();
        script.setScriptName(scriptName);
        script.setRemark(remark);
        return adminApiService.createBaseScript(script, file);
    }

    @GetMapping("/api/getBlobContent")
    ResultMsg getBlobContent(@RequestParam("filePath") String filePath) {
        return adminApiService.getBlobContent(filePath);
    }

    @RequestMapping(value = "/api/getbasescriptlist", method = RequestMethod.POST)
    public ResultMsg getBaseScriptList(@RequestBody String baseScriptQueryStr) {
        ResultMsg resultMsg = adminApiService.queryBaseScript(baseScriptQueryStr);
        return resultMsg;
    }

    /**
     * 创建资源组todo
     */
    @RequestMapping(value = "/api/createResourceGroup", method = RequestMethod.POST)
    public ResultMsg createResourceGroup(@RequestBody String azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = adminApiService.createResourceGroup(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    /**
     * 查看资源组
     */
    @RequestMapping(value = "/api/getResourceGroup", method = RequestMethod.GET)
    public ResultMsg getResourceGroup(@RequestParam("clusterId") String clusterId) {
        ResultMsg resultMsg = adminApiService.getResourceGroup(clusterId);
        return resultMsg;
    }

    /**
     * 删除资源组
     */
    @RequestMapping(value = "/api/deleteResourceGroup", method = RequestMethod.GET)
    public ResultMsg deleteResourceGroup(@RequestParam("clusterId") String clusterId) {
        ResultMsg resultMsg = adminApiService.deleteResourceGroup(clusterId);
        return resultMsg;
    }

    /**
     * 更新资源组标签-全量
     */
    @RequestMapping(value = "/api/updateResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg updateResourceGroupTags(@RequestBody String azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = adminApiService.updateResourceGroupTags(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    /**
     * 更新资源组标签-增量
     */
    @RequestMapping(value = "/api/addResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg addResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = adminApiService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 删除资源组标签
     */
    @RequestMapping(value = "/api/deleteResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg deleteResourceGroupTags(@RequestBody String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = adminApiService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     */
    @RequestMapping(value = "/api/metas/supportedVMSkuList", method = RequestMethod.GET)
    public ResultMsg supportedVMSkuList(@RequestParam String region) {
        ResultMsg resultMsg = adminApiService.supportedVMSkuList(region);
        return resultMsg;
    }

    /**
     * 集群实例扩容
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/scaleout")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg clusterScaleOut(@Valid @RequestBody ClusterScaleOutOrScaleInRequest request) {
        getLogger().info("clusterScaleOut request:{}", request.toString());
        request.setCreatedBy(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.clusterScaleOut(request);
        return resultMsg;
    }

    /**
     * 集群实例缩容
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/scalein")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg clusterScaleIn(@Valid @RequestBody ClusterScaleOutOrScaleInRequest request) {
        getLogger().info("clusterScaleIn request:{}", request.toString());
        request.setCreatedBy(getUserInfo().getUserName());
        request.setUser(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.clusterScaleIn(request);
        return resultMsg;
    }

    /**
     * 弹性伸缩记录
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/scalingLog")
    public ResultMsg clusterScalingLog(@Valid @RequestBody ClusterScalingLogData request) {
        ResultMsg resultMsg = adminApiService.clusterScalingLog(request);
        return resultMsg;
    }

    /**
     * 撤销任务
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/cancelScalingTask")
    public ResultMsg clusterCancelScalingTask(@Valid @RequestBody ClusterCancelScalingTaskRequest request) {
        getLogger().info("cancelScalingTask request:{}", request.toString());
        BaseUserInfo userInfo = getUserInfo();
        request.setCreatedBy(userInfo.getUserName());
        request.setUser(userInfo.getUserName());
        request.setUserRealName(userInfo.getRealName());
        ResultMsg resultMsg = adminApiService.clusterCancelScalingTask(request);
        return resultMsg;
    }

    /**
     * 获取弹性伸缩规则
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/getElasticScalingRule")
    public ResultMsg getElasticScalingRule(@RequestBody ConfGroupElasticScalingData request) {
        ResultMsg resultMsg = adminApiService.getElasticScalingRule(request);
        return resultMsg;
    }

    /**
     * 修改实例组弹性伸缩配置
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/updateGroupElasticScaling")
    public ResultMsg updateGroupElasticScaling(@RequestBody ConfGroupElasticScalingData request) {
        request.setUserName(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.updateGroupElasticScaling(request);
        return resultMsg;
    }

    /**
     * 修改实例组全托管弹性伸缩参数
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/updateGroupESFullCustodyParam")
    public ResultMsg updateGroupESFullCustodyParam(@RequestBody ConfGroupElasticScalingData request) {
        request.setUserName(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.updateGroupESFullCustodyParam(request);
        return resultMsg;
    }

    /**
     * 添加弹性伸缩规则
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/postElasticScalingRule")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg postElasticScalingRule(@Valid @RequestBody ConfGroupElasticScalingData request) {
        request.setUserName(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.postElasticScalingRule(request);
        return resultMsg;
    }

    /**
     * 修改弹性伸缩规则（全量参数）
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/updateElasticScalingRule")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg updateElasticScalingRule(@Valid @RequestBody ConfGroupElasticScalingData request) {
        request.setUserName(getUserInfo().getUserName());
        ResultMsg resultMsg = adminApiService.updateElasticScalingRule(request);
        return resultMsg;
    }

    /**
     * 启停用弹性伸缩规则
     */
    @PostMapping(value = "/api/cluster/updateElasticScalingRuleValid")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg updateElasticScalingRuleValid(@RequestParam("esRuleId") String esRuleId, @RequestParam("isValid") Integer isValid) {
        ResultMsg resultMsg = adminApiService.updateElasticScalingRuleValid(esRuleId, isValid);
        return resultMsg;
    }

    /**
     * 删除弹性伸缩规则
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/api/cluster/deleteElasticScalingRule")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg deleteElasticScalingRule(@RequestBody ConfGroupElasticScalingData request) {
        ResultMsg resultMsg = adminApiService.deleteElasticScalingRule(request);
        return resultMsg;
    }

    /**
     * 单个创建VM实例
     */
    @RequestMapping(value = "/api/createVMInstance", method = RequestMethod.POST)
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg createVMInstance(@RequestBody String azureVMInstanceRequest) {
        ResultMsg resultMsg = adminApiService.createVMInstance(azureVMInstanceRequest);
        return resultMsg;
    }

    /**
     * 单个删除VM实例todo
     */
    @RequestMapping(value = "/api/deleteVMInstance", method = RequestMethod.GET)
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg deleteVMInstance(@RequestParam("vmName") String vmName) {
        ResultMsg resultMsg = adminApiService.deleteVMInstance(vmName);
        return resultMsg;
    }

    /**
     * 批量/单个VM扩容磁盘todo
     */
    @RequestMapping(value = "/api/updateVirtualMachinesDiskSize", method = RequestMethod.POST)
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public ResultMsg updateVirtualMachinesDiskSize(@RequestBody String azureUpdateVirtualMachinesDiskSizeRequest) {
        ResultMsg resultMsg = adminApiService.updateVirtualMachinesDiskSize(azureUpdateVirtualMachinesDiskSizeRequest);
        return resultMsg;
    }

    /**
     * 获取AZ列表(可用区域)
     */
    @RequestMapping(value = "/api/getazlist", method = RequestMethod.GET)
    public ResultMsg getAzList(@RequestParam String region) {
        List<AvailabilityZone> dataItem =  metaDataItemService.getazlist(region);
        return ResultMsg.SUCCESS(dataItem);

    }

    @PostMapping("/api/saveUserCustomerScript")
    public ResultMsg saveUserCustomerScript(@RequestBody String userScriptRequest) {
        return composeService.saveuserscript(userScriptRequest);
    }

    /**
     * 节点信息概览
     *
     * @param param
     * @return
     */
    @PostMapping("/api/getVmOverview")
    public ResultMsg getVmOverview(@RequestBody Map param) {
        return adminApiService.getVmOverview(param);
    }

    @PostMapping("/api/getvmList")
    public ResultMsg getvmList(@RequestBody Map param) {
        return adminApiService.getVMlistByClusterId(param);
    }


    @RequestMapping("/api/getVMGroupsByClusterId")
    public ResultMsg getVMGroupsByClusterId(@RequestParam("clusterId") String clusterId) {
        return adminApiService.getVMGroupsByClusterId(clusterId);
    }


    @RequestMapping(value = "/api/scriptjoblist", method = RequestMethod.POST)
    public ResultMsg queryScriptJobList(@RequestBody String scriptJobListRequest) {
        ResultMsg resultMsg = adminApiService.queryScriptJobList(scriptJobListRequest);
        return resultMsg;
    }


    @RequestMapping(value = "/api/retryactivity", method = RequestMethod.GET)
    public ResultMsg retry(@RequestParam("activityLogId") String activityLogId) {
        return composeService.retryactivity(activityLogId);
    }

    @RequestMapping(value = "/api/sdpVersionInfo", method = RequestMethod.GET)
    public ResultMsg sdpVersionInfo() {
        return adminApiService.sdpVersionInfo();
    }

    @PostMapping("/api/getTaskInfo")
    public ResultMsg getTaskInfo(@RequestBody ConfScalingTask task) {
        return adminApiService.getTaskInfo(task.getTaskId());
    }

    /**
     * 根据PlanId,查询这个Plan涉及到的所有VM, 包括对清理异常VM的处理
     * @param planId
     * @return
     */
    @GetMapping("/api/getVmListByPlanId")
    public ResultMsg getVmListByPlanId(@RequestParam("planId") String planId) {
        return adminApiService.getVmListByPlanId(planId);
    }

    @GetMapping("/api/getJobQueryParamDict")
    public ResultMsg getJobQueryParamDict(){
        return adminApiService.getJobQueryParamDict();
    }

    @RequestMapping(value = "/api/plan/info", method = RequestMethod.GET)
    public ResultMsg getPlanInfoByPlanId(@RequestParam("planId") String planId) {
        return adminApiService.getPlanInfoByPlanId(planId);
    }
    @PostMapping("/api/deleteScaleOutTaskVms")
    public ResultMsg deleteScaleOutTaskVms(@RequestBody ConfScalingTask task) {
        return adminApiService.deleteScaleOutTaskVms(task.getTaskId());
    }

    @PostMapping(value = "/api/createScaleInTaskbyVms")
    public ResultMsg createScaleInTaskbyVms(@RequestBody Map<String, Object> param) {
        getLogger().info(param.toString());
        return adminApiService.sdpVersionInfo();
    }

    /**
     * 获取地区列表
     * @return
     */
    @GetMapping(value = "/api/getRegionDetail")
    public ResultMsg getRegionDetail() {
        List<Region> dataItem=new ArrayList<>();
        BaseUserInfo userInfo = getUserInfo();
        List<String> userRegionList = userInfo.getUserRegionList();
        if (CollUtil.isNotEmpty(userRegionList)) {
            dataItem = metaDataItemService.getRegionList();
            dataItem = dataItem.stream().filter(region -> userRegionList.contains(region.getRegion())).collect(Collectors.toList());
        }
        return ResultMsg.SUCCESS(dataItem);
    }

    @RequestMapping(value = "/api/scale/info", method = RequestMethod.GET)
    public ResultMsg getScaleInfoByTaskid(@RequestParam("taskId") String taskId) {
        return adminApiService.getScaleTaskInfoByTaskId(taskId);
    }

    /**
     * 查询扩容中的队列数据
     */
    @PostMapping(value = "/api/getScaleCountInQueue")
    public ResultMsg getScaleCountInQueue(@RequestBody ConfScalingTask task) {
        return adminApiService.getScaleCountInQueue(task);
    }

    /**s
     * 查询vmSku价格(多机型实例)
     */
    @PostMapping(value = "/api/spot/getInstancePriceList")
    public ResultMsg getInstancePriceList(@RequestBody Map<String, Object> param) {
        List<String> skuNames = (List<String>)param.get("skuNames");
        return composeMetaService.getInstancePriceList(skuNames,param.get("region").toString());
    }

    /**
     * 竞价实例驱逐率
     */
    @PostMapping(value = "/api/spot/spotEvictionRate")
    public ResultMsg spotEvictionRate(@RequestBody Map<String, Object> param) {
        List<String> skuNames = (List<String>)param.get("skuNames");
        List<JSONObject> evictionRates = azureService.spotEvictionRate(skuNames, param.get("region").toString());
        return ResultMsg.SUCCESS(evictionRates);
    }

    /**
     * 竞价实例历史价格
     */
    @PostMapping(value = "/api/spot/spotPriceHistory")
    public ResultMsg spotPriceHistory(@RequestBody Map<String, Object> param) {
        return adminApiService.spotPriceHistory(param);
    }

    /**
     * 竞价实例历史驱逐率
     */
    @PostMapping(value = "/api/spot/spotPriceAndEvictionRateHistory")
    public ResultMsg spotPriceAndEvictionRateHistory(@RequestBody Map<String, Object> param) {
        String region = MapUtil.getStr(param, "region");
        String skuName = MapUtil.getStr(param, "skuName");
        Integer periodDays = MapUtil.getInt(param, "periodDays", 7);
        List<AzurePriceHistory> histories = adminApiService.spotPriceAndEvictionRateHistory(region, skuName, periodDays);
        return ResultMsg.SUCCESS(histories);
    }

    @GetMapping(value = "/api/man/processnoplanname")
    public ResultMsg processwithoutplanname() {
        return adminApiService.processWithOutPlanName();
    }

    @GetMapping(value = "/api/man/processvmid")
    public ResultMsg processHistoryvmid(@RequestParam String region){
        return  adminApiService.processWithOutVmId(region);
    }


    @GetMapping(value = "/api/getAvailableImage")
    public ResultMsg getAvailableImage(@RequestParam("clusterId") String clusterId, @RequestParam(name = "clusterName", required = false) String clusterName) {
        return adminApiService.getAvailableImage(clusterId);
    }

    /**
     * POD 解析域名
     * @param hostName  hostname 域名
     * @return
     */
    @RequestMapping("/api/dnsresolve")
    public String dnsresolve( @RequestParam("hostName") String hostName){
        return composeService.dnsresolve(hostName);
    }

    /**
     * 更新HostGroup spotstate
     * @param clusterId 集群ID
     * @param groupName 实例组
     * @param spotState 竞价状态
     * @return
     */
    @RequestMapping("/api/updatespotstate")
    public ResultMsg updateSpotState( @RequestParam("clusterId") String clusterId,
                                   @RequestParam("groupName") String groupName,
                                   @RequestParam("spotState") Integer spotState){
        ResultMsg msg = new ResultMsg();
        logger.info("更新Sopt实例组买入逐出功能开，clusterId={},groupName={},spotState={}",clusterId,groupName,spotState);
        int i = hostGroupMapper.updateHostGroupSpotStateByClusterIdAndGroupName(clusterId,groupName,spotState);
        if (i>0){
            msg.setResult(true);
        }else{
            msg.setResult(false);
        }
        return msg;
    }


    /**
     *
     * @param apiName
     * @param failedType
     * @param begTime
     * @param endTime
     * @param apiKeyParam
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping("/api/getfailedlogs")
    public ResultMsg getFailedLogs(@RequestParam(value = "region",required = false) String region,
                                   @RequestParam(value = "apiName",required = false) String apiName,
                                   @RequestParam(value = "failedType",required = false)Integer failedType,
                                   @RequestParam(value = "beginReportDate",required = false) String begTime,
                                   @RequestParam(value = "endReportDate",required = false) String endTime,
                                   @RequestParam(value = "apiKeyParam",required = false) String apiKeyParam,
                                   @RequestParam(value = "pageIndex",required = true) Integer pageIndex,
                                   @RequestParam(value = "pageSize",required = true) Integer pageSize){

        //region 参数构建
        Map<String,Object> mapParam= new HashMap<>();
        if (StringUtils.isNotEmpty(apiName)){
            mapParam.put("apiName",apiName);
        }
        if (failedType != null){
            mapParam.put("failedType",failedType);
        }
        if (StringUtils.isNotEmpty(begTime)){
            mapParam.put("begTime",begTime);
        }
        if (StringUtils.isNotEmpty(endTime)){
            mapParam.put("endTime",endTime);
        }
        if (StringUtils.isNotEmpty(apiKeyParam)){
            mapParam.put("apiKeyParam",apiKeyParam);
        }
        if (pageIndex != null){
            mapParam.put("pageIndex",pageIndex-1);
        }
        if (pageSize !=null){
            mapParam.put("pageSize",pageSize);
        }
        mapParam.put("region",region);
        //endregion

        ResultMsg msg = adminApiService.getFailedLogsByParam(mapParam);

        return msg;
    }

    /**
     *
     * @param id
     * @return
     */
    @RequestMapping("/api/getfailedlogbyid")
    public ResultMsg getFailedLogs(@RequestParam(value = "id") Long id){
        return adminApiService.getFailedLogById(id);
    }

    /**
     *  运维接口-删除锁
     * @param keyname
     * @return
     */
    @GetMapping("/api/opsdellock")
    public ResultMsg delLockKey(@RequestParam(value = "keyname") String keyname){
        return adminApiService.opsDelLockKey(keyname);
    }

    /**
     *  运维接口- 发送第一条消息
     *
     * @param planId
     * @return
     */
    @GetMapping("/api/sendfirstmsg")
    public ResultMsg sendFirstMsg(@RequestParam(value = "planId") String planId){
        return adminApiService.opsSendFirstMessage(planId);
    }

    /**
     * 获取版本信息
     */
    @GetMapping("/api/getStackVersions")
    public ResultMsg getStackVersions() {
        return adminApiService.getStackVersions();
    }

    /**
     * 集群并行或串行扩缩容
     */
    @PostMapping("/api/updateClusterParallel")
    public ResultMsg updateClusterParallel(@RequestBody Map<String, Object> param) {
        String clusterId=(String) param.get("clusterId");
        Integer isParallelScale=(Integer)param.get("isParallelScale");
        int i = adminApiService.updateClusterParallel(clusterId, isParallelScale);
        if (i>0){
            return ResultMsg.SUCCESS();
        }
        return ResultMsg.FAILURE("更新集群并行或串行扩缩容失败!");
    }

    /**
     * 获取集群销毁限流全局配置
     */
    @GetMapping("/api/getDestoryClusterLimitConfig")
    public ResultMsg getDestoryClusterLimitConfig() {

        return bizConfigService.getDestoryClusterLimitConfig();

    }

    /**
     * 保存集群销毁限流全局配置
     */
    @PostMapping("/api/saveDestoryClusterLimitConfig")
    public ResultMsg saveDestoryClusterLimitConfig(@RequestBody Map<String, String> param) {

        return bizConfigService.updateDestoryClusterLimitConfig(param);
    }

    @GetMapping("/api/getGroupedBizConfigs")
    public ResultMsg getGroupedBizConfigs() {
        List<BizConfigGroup> groupedConfigs = bizConfigService.getGroupedConfigs();
        return ResultMsg.SUCCESS(groupedConfigs);
    }

    @PostMapping("/api/updateBizConfigs")
    public ResultMsg updateBizConfigs(@RequestBody UpdateBizConfigRequest request) {
        BizConfig bizConfig = BeanUtil.copyProperties(request, BizConfig.class);
        bizConfigService.updateBizConfig(bizConfig);
        return ResultMsg.SUCCESS();
    }

    /**
     * 获取上下线主机列表
     * @param vmEventRequest
     * @return
     */
    @GetMapping("/api/getVmEventList")
    public ResultMsg getVmEventList(VmEventRequest vmEventRequest) {
        return vmEventService.getVmEventList(vmEventRequest);
    }

    /**
     * 清理集群ambari host历史数据
     * @return
     */
    @PostMapping("/api/cleanAmbariHistory")
    public ResultMsg cleanAmbariHistory(@RequestBody @Valid CleanAmbariHistoryRequest request) {
        return composeService.cleanAmbariHistory(request.getClusterId(), request.getStartDate());
    }

    /**
     * 查询组件列表
     *
     * @return
     */
    @GetMapping("/api/querycomponentlist")
    public ResultMsg queryComponentList() {
        return configInfoService.queryComponentList();
    }

    /**
     * 查询配置文件列表
     *
     * @return
     */
    @GetMapping("/api/queryprofileslist")
    public ResultMsg queryProfilesList(@RequestParam(value = "releaseVersion",required = false) List<String> releaseVersion) {
        return configInfoService.queryProfilesList(releaseVersion);
    }

    /**
     * 查询默认配置列表
     *
     * @return
     */
    @PostMapping("/api/queryconfiglist")
    public ResultMsg queryConfigList(@RequestBody AmbariConfigItemRequest request) {
        return configInfoService.queryConfigList(request);
    }
    //根据ID查询一条默认配置 /admin/api/queryconfigbyid
    @GetMapping("/api/queryconfigbyid")
    public ResultMsg queryConfigById(@RequestParam(value = "id") String id) {
        return configInfoService.queryConfigById(id);
    }

    //新增配置
    @PostMapping("/api/addconfig")
    public ResultMsg addConfig(@RequestBody AmbariConfigAddRequest request) {
        return configInfoService.addConfig(request);
    }
    //修改一个配置 /admin/api/updateconfig
    @PostMapping("/api/updateconfig")
    public ResultMsg updateConfig(@RequestBody AmbariConfigAddRequest request) {
        return configInfoService.updateConfig(request);
    }

    //删除一个配置
    @PostMapping("/api/deleteconfig")
    public ResultMsg deleteConfig(@RequestBody AmbariConfigItem request) {
        return configInfoService.deleteConfig(request);
    }

    /**
     * 更新集群中销毁白名单的状态
     *
     * @return
     */
    @PostMapping("/api/updatedestroystatus")
    public ResultMsg updateDestroyStatus(@Valid @RequestBody UpdateDestroyStatusRequest request) {
        int updated = adminApiService.updateDestroyStatus(request.getClusterId(), request.getIsWhiteAddr());
        if (updated>0){
            return ResultMsg.SUCCESS();
        }
        return ResultMsg.FAILURE("更新集群中销毁白名单的状态失败!");
    }
    /**
     * 根据vmConfigId获取集群中主机规格信息
     *
     * @return
     */
    @PostMapping("/api/listConfHostGroupVmSku")
    public ResultMsg listConfHostGroupVmSku(@Valid @RequestBody ConfHostGroupVmSkuRequest request) {
        return confHostGroupVmSkuService.listConfHostGroupVmSku(request);
    }
    @PostMapping("/api/collectClusterInfoList")
    public ResultMsg getCollectClusterInfoList(@RequestBody InfoClusterInfoCollectLog infoClusterInfoCollectLog){
        return composeService.getCollectClusterInfoList(infoClusterInfoCollectLog);
    }

    @PostMapping("/api/collectClusterInfoByClusterId")
    public ResultMsg getCollectClusterInfoListByClusterId(@RequestBody InfoClusterInfoCollectLog infoClusterInfoCollectLog){
        return composeService.getCollectClusterInfoListByClusterId(infoClusterInfoCollectLog.getClusterId());
    }

    /**
     * 查询工单的审批情况
     * @param request
     * @return
     */
    @GetMapping("/api/queryorderapproval")
    public ResultMsg queryOrderApproval(OrderApprovalRequest request) {
        return bizConfigService.queryOrderApproval(request);

    }

    /**
     * 更新PV2数据盘IOPS和MBPS
     * @param request
     * @return
     */
    @PostMapping("/api/updateDiskIOPSAndThroughput")
    public ResultMsg updateDiskIOPSAndThroughput(@Valid @RequestBody DiskPerformanceRequest request) {
        return adminApiService.updateDiskIOPSAndThroughput(request);
    }


}
