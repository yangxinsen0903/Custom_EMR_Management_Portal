package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.metaData.AvailabilityZone;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.domain.metaData.VMSkuObj;
import com.sunbox.sdpcompose.manager.AzureMetaDataManager;
import com.sunbox.sdpcompose.mapper.ConfClusterTagMapper;
import com.sunbox.sdpcompose.mapper.ConfClusterVmMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmMapper;
import com.sunbox.sdpcompose.model.azure.fleet.request.*;
import com.sunbox.sdpcompose.model.azure.fleet.response.AzureFleetInfo;
import com.sunbox.sdpcompose.model.azure.fleet.response.InstancePrice;
import com.sunbox.domain.azure.AzureDeleteVMsRequest;
import com.sunbox.sdpcompose.model.azure.request.AzureSpotProfile;
import com.sunbox.sdpcompose.service.IAzureFleetService;
import com.sunbox.service.IAzurePriceService;
import com.sunbox.service.IConfHostGroupVmSkuService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.HttpClientUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AzureFleetServiceImpl implements IAzureFleetService, BaseCommonInterFace {

    @Value("${azure.request.url}")
    private String azureFleetUrl;

    @Value("${vm.username}")
    private String vmusername;

    @Value("${vm.hostnamesuffix}")
    private String hostnamesuffix;

    @Value("${sdp.api.azure.deletevms.timeout:600}")
    private Integer sdpApiAzureDeleteVmTimeOut;

    @Value("${sdp.api.azure.delete1vm.timeout:60}")
    private Integer sdpApiAzureDelete1VmTimeOut;

    @Autowired
    private AzureMetaDataManager azureMetaDataManager;

    @Autowired
    private IMetaDataItemService azureMetaDataService;

    @Autowired
    private ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Autowired
    private ConfClusterVmMapper confClusterVmMapper;

    @Autowired
    private ConfClusterTagMapper tagMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;
    @Qualifier("infoClusterVmMapper")
    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    @Autowired
    private IConfHostGroupVmSkuService confHostGroupVmSkuService;

    @Autowired
    private IAzurePriceService azurePriceService;

    //region 请求Azure接口

    /**
     * 调用Azure fleet 创建VM接口
     *
     * @param azureVmsRequest
     * @return
     */
    @Override
    public ResultMsg createVms(AzureFleetVmsRequest azureVmsRequest,String subscriptionId) {
        getLogger().info("createAzureFleetVms:"+ JSON.toJSONString(azureVmsRequest));
        ResultMsg msg = new ResultMsg();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);

        Long start = System.currentTimeMillis();
        try {
            String jsonStr = HttpClientUtil.doPost(azureFleetUrl + "/api/v1/vms",
                    JSON.toJSONString(azureVmsRequest),
                    headerMap);
            getLogger().info("创建虚拟机, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);

            if (StringUtils.isNotEmpty(jsonStr)) {
                msg.setData(JSONObject.parseObject(jsonStr));
                msg.setResult(true);
            } else {
                msg.setResult(false);
            }
        }catch (Exception e){
            getLogger().error("创建虚拟机异常，",e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 查询AzureFleet资源管理任务的状态
     *
     * @param jobId
     * @param subscriptionId
     * @return
     */
    @Override
    public ResultMsg getJobsStatusWithRequestTimeout(String jobId, String subscriptionId) {

        ResultMsg msg = new ResultMsg();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        Long start = System.currentTimeMillis();
        try {
            String jsonStr = HttpClientUtil.doGetWithRequestTimeOut(azureFleetUrl + "/api/v1/jobs/"+jobId,null, headerMap,60);
            getLogger().info("查询AzureFleetJob结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);

            if (StringUtils.isNotEmpty(jsonStr)) {
                msg.setData(JSONObject.parseObject(jsonStr));
                msg.setResult(true);
            } else {
                msg.setResult(false);
            }
        }catch (Exception e){
            getLogger().error("查询AzureFleetJob结果异常，",e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 查询资源申请的详细信息 <p/>
     * Azure Fleet的ProvisionDetail接口不返回数据, 复用 jobs/{jobId} 接口
     * @param jobId
     * @param subscriptionId
     * @return
     */
    @Override
    public ResultMsg provisionDetail(String jobId, String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        Long start = System.currentTimeMillis();
        try {
//            String jsonStr = HttpClientUtil.doGetWithRequestTimeOut(azureFleetUrl + "/api/v1/jobs/"+jobId+"/provisionDetail",null, headerMap,60);
            String jsonStr = HttpClientUtil.doGetWithRequestTimeOut(azureFleetUrl + "/api/v1/jobs/"+jobId,null, headerMap,60);
            getLogger().info("查询AzureFleetJob结果, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);

            if (StringUtils.isNotEmpty(jsonStr)) {
                msg.setData(JSONObject.parseObject(jsonStr));
                msg.setResult(true);
            } else {
                msg.setResult(false);
            }
        }catch (Exception e){
            getLogger().error("查询AzureFleetJob结果异常，",e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }


    /**
     * 批量删除VM
     * @param deleteVMsRequest
     * @param subscriptionId
     * @return
     */
    public ResultMsg deleteVirtualMachines(AzureDeleteVMsRequest deleteVMsRequest,String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        String reqStr = JSON.toJSONString(deleteVMsRequest, SerializerFeature.DisableCircularReferenceDetect);

        long start = System.currentTimeMillis();
        getLogger().info("AzureServiceImpl.deleteVirtualMachines, reqStr: {}", reqStr);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        HttpClientUtil.HttpResult result = HttpClientUtil.httpPutWithRequestTimeOut(
                azureFleetUrl + "/api/v1/vms/deleteVirtualMachines",
                reqStr,headerMap,
                sdpApiAzureDeleteVmTimeOut);
        getLogger().info("AzureServiceImpl.deleteVirtualMachines, elapse:{}ms, respStr: {}",
                (System.currentTimeMillis() - start), result.getResponseBody());

        if (result.getStatusCode() != 200) {
            msg.setResult(false);
            msg.setErrorMsg("deleteVirtualMachines error.");
            msg.setActimes(60);
            return msg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result.getResponseBody());
            msg.setResult(true);
        } catch (Exception e) {
            msg.setResult(false);
            getLogger().info("AzureServiceImpl.deleteVirtualMachines.JSONObject.parseObject error, respStr: {}, e: {}",
                    result.getResponseBody(), e);
        }
        msg.setData(jsonObject);
        return msg;
    }

    /**
     * 删除集群
     *
     * @param clusterName
     * @param subscriptionId
     * @return
     */
    @Override
    public ResultMsg deleteCluster(String clusterName, String subscriptionId) {
        return null;
    }

    /**
     * 查询vmSku价格
     * {
     * "vmSkuName": "Standard_D4s_v5",
     * "spotUnitPricePerHourUSD": 0.019429,
     * "onDemandUnitPricePerHourUSD": 0.192
     * }
     *
     * @param skuNames
     * @param region
     */
    @Override
    public ResultMsg getInstancePrice(List<String> skuNames, String region,String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        Map<String, String> headerMap = new HashMap<>();
        Map<String,Object> reqJsonMap= new HashMap<>();
        reqJsonMap.put("region",region);
        reqJsonMap.put("vmSkuNames",skuNames);
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        Long start = System.currentTimeMillis();
        try {
            String jsonStr = HttpClientUtil.doPost(azureFleetUrl + "/api/v1/price/spotInstance",
                    JSON.toJSONString(reqJsonMap),
                    headerMap);
            getLogger().info("查询spot单价, elapse:{}ms, resp: {}", (System.currentTimeMillis() - start), jsonStr);

            if (StringUtils.isNotEmpty(jsonStr)) {
                List<InstancePrice> instancePriceList = JSON.parseArray(jsonStr, InstancePrice.class);
                msg.setData(instancePriceList);
                msg.setResult(true);
            } else {
                msg.setResult(false);
            }
        }catch (Exception e){
            getLogger().error("查询spot单价异常，",e);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 查询单个VmSku价格
     *
     * @param skuName
     * @param region
     * @param subscriptionId
     * @return
     */
    @Override
    public ResultMsg getInstancePrice(String skuName, String region, String subscriptionId) {
        return getInstancePrice(Collections.singletonList(skuName),region,subscriptionId);
    }

    /**
     * 扩容创建虚拟机
     *
     * @param azureVmsRequest
     * @param subscriptionId
     */
    @Override
    public ResultMsg createAppendVms(AzureFleetAppendVMsRequest azureVmsRequest, String subscriptionId) {
        ResultMsg msg = new ResultMsg();
        String reqStr = JSON.toJSONString(azureVmsRequest, SerializerFeature.DisableCircularReferenceDetect);

        long start = System.currentTimeMillis();
        getLogger().info("AzureFleetServiceImpl.appendVirtualMachines, reqStr: {}", reqStr);
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID,subscriptionId);
        HttpClientUtil.HttpResult result = HttpClientUtil.httpPutWithRequestTimeOut(
                azureFleetUrl + "/api/v1/vms/appendVirtualMachines",
                reqStr,headerMap,
                sdpApiAzureDeleteVmTimeOut);
        getLogger().info("AzureFleetServiceImpl.appendVirtualMachines, elapse:{}ms, respStr: {}",
                (System.currentTimeMillis() - start), result.getResponseBody());

        if (result.getStatusCode() != 200) {
            msg.setResult(false);
            msg.setErrorMsg("appendVirtualMachines error.");
            msg.setActimes(60);
            return msg;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result.getResponseBody());
            msg.setResult(true);
        } catch (Exception e) {
            msg.setResult(false);
            getLogger().info("AzureFleetServiceImpl.appendVirtualMachines.JSONObject.parseObject error, respStr: {}, e: {}",
                    result.getResponseBody(), e);
        }
        msg.setData(jsonObject);
        return msg;
    }


    @Override
    public ResultMsg<AzureFleetInfo> getAzureFleetInfo(ConfCluster confCluster, String groupName) {
        ResultMsg<AzureFleetInfo> msg = new ResultMsg<>();
        // /api/v1/fleet/{cluster}/{group}
        String url = "/api/v1/fleet/" + confCluster.getClusterName() + "/" + groupName;
        long start = System.currentTimeMillis();
        getLogger().info("AzureFleetServiceImpl.getAzureFleetInfo, url: {}, subscriptionId={}",
                azureFleetUrl + url, confCluster.getSubscriptionId());
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID, confCluster.getSubscriptionId());


        String result = HttpClientUtil.doGet(azureFleetUrl + url, headerMap);
        getLogger().info("AzureFleetServiceImpl.getAzureFleetInfo, elapse:{}ms, respStr: {}",
                (System.currentTimeMillis() - start), result);

        AzureFleetInfo azureFleetInfo = null;
        try {
            JSONObject jsonResult = JSONObject.parseObject(result);
            if (jsonResult.containsKey("status") && jsonResult.getInteger("status") > 201) {
                // 返回不正确, 比如返回404
                msg.setResult(false);
                msg.setMsg(result);
                msg.setErrorMsg(result);
                msg.setData(null);
            } else {
                azureFleetInfo = JSONObject.parseObject(result, AzureFleetInfo.class);
                msg.setResult(true);
                msg.setData(azureFleetInfo);
            }
        } catch (Exception e) {
            msg.setResult(false);
            msg.setErrorMsg(e.getMessage());
            getLogger().info("AzureFleetServiceImpl.getAzureFleetInfo.JSONObject.parseObject error, respStr: {}, e: {}",
                    result, e);
        }
        return msg;
    }

    @Override
    public ResultMsg deleteAzureFleet(ConfCluster confCluster, String groupName) {
        ResultMsg msg = new ResultMsg();
        // /api/v1/fleet/{cluster}/{group}
        String url = "/api/v1/fleet/" + confCluster.getClusterName() + "/" + groupName;

        long start = System.currentTimeMillis();
        getLogger().info("AzureFleetServiceImpl.deleteAzureFleet, url: {}, subscriptionId={}",
                azureFleetUrl + url, confCluster.getSubscriptionId());
        Map<String, String> headerMap = new HashMap<>();
        //订阅id
        headerMap.put(MetaDataConstants.SUBSCRIPTION_ID, confCluster.getSubscriptionId());
        String result = "";
        try {
            result = HttpClientUtil.doDeleteWithRequestTimeOut(
                    azureFleetUrl + url, headerMap, sdpApiAzureDeleteVmTimeOut);
            getLogger().info("AzureFleetServiceImpl.deleteAzureFleet, elapse:{}ms, respStr: {}",
                    (System.currentTimeMillis() - start), result);
        } catch (Exception ex) {
            throw new RuntimeException("调用Azure接口删除AzureFleet失败: url=" + url, ex);
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result);
            msg.setResult(true);
        } catch (Exception e) {
            String message = StrUtil.format("AzureFleetServiceImpl.deleteAzureFleet.JSONObject.parseObject error, respStr: {}",
                    result);
            msg.setResult(false);
            getLogger().error(message, e);
        }
        msg.setData(jsonObject);
        return msg;
    }


    //endregion


    //region 构造请求参数

    /**
     * 构造spot profile, 不会根据AzureFleet的实际值调整. 用于集群新创建的时候使用
     *
     * @param confClusterVm
     * @return
     */
    @Override
    public ResultMsg buildAzureSpotProfile(ConfCluster confCluster,ConfClusterVm confClusterVm) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        // 判断是否是spot实例组
        if (Objects.equals(confClusterVm.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
            SpotProfile spotProfile = new SpotProfile();
            spotProfile.setMaintain(true);
            if (StrUtil.isNotEmpty(confClusterVm.getSpotAllocationStrategy())){
                spotProfile.setAllocationStrategy(confClusterVm.getSpotAllocationStrategy());
            }else{
                spotProfile.setAllocationStrategy(SpotProfile.SpotAllocationStrategyEnum.PriceCapacityOptimized.getCode());
            }
            spotProfile.setCapacity(confClusterVm.getCount());
            BigDecimal bidPrice;
            //先获取info_cluster_vm的购买价格,保持创建和扩容价格一致
            List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectByClusterIdAndGroupName(confCluster.getClusterId(), confClusterVm.getGroupName());
            if (CollUtil.isNotEmpty(infoClusterVms)){
                bidPrice = infoClusterVms.get(0).getSpotPrice();
            }else {
                bidPrice=azurePriceService.computeSpotPrice(confCluster.getRegion(), confClusterVm);
            }
            if (bidPrice==null){
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("获取标的价格失败");
                return resultMsg;
            }
            spotProfile.setMaxPricePerVM(bidPrice.toString());
            spotProfile.setEvictionPolicy(AzureSpotProfile.EvictionPolicy.Delete.toString());
            resultMsg.setData(spotProfile);
            resultMsg.setResult(true);
        } else {
            resultMsg.setErrorMsg("实例组不是竞价实例, 所以不生成SpotProfile.");
        }
        return resultMsg;
    }

    /**
     * 构造 base Profile
     *
     * @param confCluster
     * @param item
     * @return
     */
    @Override
    public ResultMsg buildAzureBaseProfile(ConfCluster confCluster, ConfClusterVm item) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            BaseProfile baseProfile = new BaseProfile();

            //region 1.构造MI
            Set<String> miSet = new HashSet<>();
            if (!StringUtils.isEmpty(confCluster.getLogMI())) {
                miSet.add(confCluster.getLogMI());
            }
            if (!StringUtils.isEmpty(confCluster.getVmMI())) {
                miSet.add(confCluster.getVmMI());
            }

            List<String> miList = new ArrayList<>();
            for (String s : miSet) {
                miList.add(s);
            }
            //endregion 构造MI

            baseProfile.setUserAssignedIdentityResourceIds(miList);
            baseProfile.setOsImageType(item.getOsImageType());
            if (item.getOsImageType().equalsIgnoreCase("CustomImage")) {
                baseProfile.setCustomOSImageId(item.getOsImageid());
            }
            if (item.getOsImageType().equalsIgnoreCase("MarketplaceImage")) {
                baseProfile.setMarketplaceOSImageName(item.getOsImageid());
            }

            baseProfile.setHostNameSuffix(hostnamesuffix);
            baseProfile.setStartupScriptBlobUrl(item.getInitScriptPath());
            baseProfile.setUserName(vmusername);
            baseProfile.setZone(confCluster.getZone());
            baseProfile.setSecondaryZone("");

            // ssh key
            baseProfile.setSshPublicKeySecretName(confCluster.getKeypairId());
            Map<String, String> meta = azureMetaDataManager.findSshKeyByName(confCluster.getRegion(), confCluster.getKeypairId());
            baseProfile.setSshKeyVaultId(meta.get("keyVaultResourceId"));
            baseProfile.setSshPublicKeyType("KeyVaultSecret");
            baseProfile.setSshPublicKeySecretName(confCluster.getKeypairId());
            baseProfile.setSshPublicKey("");

            // 子网
            baseProfile.setSubnetResourceId(confCluster.getSubnet());

            // 安全组
            if (item.getGroupName().equalsIgnoreCase("ambari")
                    || item.getGroupName().equalsIgnoreCase("master")) {
                baseProfile.setNsgResourceId(confCluster.getMasterSecurityGroup());
            } else {
                baseProfile.setNsgResourceId(confCluster.getSlaveSecurityGroup());
            }

            // 系统盘
            baseProfile.setOsDiskSku(item.getOsVolumeType());
            baseProfile.setOsDiskSizeGB(item.getOsVolumeSize());

            // TODO: 对于L系列的主机，数据盘不进行设置，默认大小和数量都为0即可。 此处需根据磁盘类弄判断， 现在暂时使用主机名是否包含L来区分。
            // 多机型判断多个sku中是否存在L系列主机,存在L系列主机时,不挂载数据盘
            List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByVmConfId(item.getVmConfId());
            long isLCount = confHostGroupVmSkus.stream().filter(x -> x.getSku().contains("_L")).count();
            if (CollectionUtils.isNotEmpty(item.getVmDataVolumes()) && isLCount<=0) {
                baseProfile.setDataDiskSku(item.getVmDataVolumes().get(0).getDataVolumeType());
                baseProfile.setDataDiskSizeGB(item.getVmDataVolumes().get(0).getDataVolumeSize());
                baseProfile.setDataDiskCount(item.getVmDataVolumes().get(0).getCount());
            }


            resultMsg.setData(baseProfile);
            resultMsg.setResult(true);
        }catch (Exception e){
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("构造BaseProfile异常,"+ExceptionUtils.getStackTrace(e));
            getLogger().error("构造BaseProfile异常，",e);
        }
        return resultMsg;
    }

    /**
     * 构造 regular Profile
     *
     * @param confCluster
     * @param confClusterVm
     * @return
     */
    @Override
    public ResultMsg buildAzureRegularProfile(ConfCluster confCluster, ConfClusterVm confClusterVm) {
        ResultMsg resultMsg = new ResultMsg();
        try{
            if(Objects.equals(confClusterVm.getPurchaseType(), ConfClusterVm.PURCHASETYPE_ONDEMOND)){
                RegularProfile regularProfile = new RegularProfile();
                regularProfile.setCapacity(confClusterVm.getCount());
                regularProfile.setAllocationStrategy(RegularProfile.RegularAllocationStrategyEnum.Prioritized.getCode());
//                regularProfile.setMinCapacity(confClusterVm.getCount());
                resultMsg.setData(regularProfile);
                resultMsg.setResult(true);
            } else {
                resultMsg.setResult(true);
                resultMsg.setErrorMsg("实例组不是按需实例组,所以不生成按需Profile");
            }
        }catch (Exception e){
            getLogger().error("构造regularProfile异常，",e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("构造regularProfile异常,"+ExceptionUtils.getStackTrace(e));
            return resultMsg;
        }
        return resultMsg;
    }

    /**
     * 构建vmSizesProfile
     *
     * @param confCluster
     * @param confClusterVm
     * @return
     */
    @Override
    public List<VMSizesProfile> buildVMSizesProfile(ConfCluster confCluster, ConfClusterVm confClusterVm, boolean useMultiSku) {
        List<VMSizesProfile> vmSizesProfiles = new ArrayList<>();

        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByVmConfId(confClusterVm.getVmConfId());
        for (int i = 0; i < confHostGroupVmSkus.size(); i++) {
            VMSizesProfile vmSizesProfiletmp = new VMSizesProfile();
            vmSizesProfiletmp.setName(confHostGroupVmSkus.get(i).getSku());
            vmSizesProfiles.add(vmSizesProfiletmp);
        }
        // 竞价实例下,如果不足三个时,则补齐三个, 保证至少有3个sku
        if (useMultiSku && vmSizesProfiles.size()<3) {
            int patchCount = 3-vmSizesProfiles.size();
            String baseSku = confClusterVm.getSku();
            if (StrUtil.isBlank(baseSku)) {
                baseSku = confHostGroupVmSkus.get(0).getSku();
            }
            List<VMSku> vmSkus = getSameSpecVmSku(baseSku, confCluster.getRegion(), patchCount);
            if (vmSkus.size() < patchCount) {
                String skus = vmSkus.stream().map(VMSku::getName).collect(Collectors.joining());
                String msg = "竞价实例组需要至少3个VmSku,自动补全时未补够3台. sku=" + baseSku + " 补齐Sku=" + skus;
                getLogger().error(msg);
                throw new RuntimeException(msg);
            }
            if (CollectionUtils.isNotEmpty(vmSkus)) {
                for (int i = 0; i < vmSkus.size(); i++) {
                    VMSizesProfile vmSizesProfiletmp = new VMSizesProfile();
                    vmSizesProfiletmp.setName(vmSkus.get(i).getName());
                    vmSizesProfiles.add(vmSizesProfiletmp);
                }
                //补齐 confHostGroupVmSku
                getLogger().info("buildVMSizesProfile单机型补齐confHostGroupVmSku数据:{}",vmSkus);
                confHostGroupVmSkuService.addConfHostGroupVmSku(confClusterVm,vmSkus);
            }
        } else {
            getLogger().info("因为未开启自动sku补齐, 所以不补齐3个Vm Sku: groupName={}, purchaseType={}",
                    confClusterVm.getGroupName(), confClusterVm.getPurchaseType()==2?"Spot": "OnDemand");
        }
        return vmSizesProfiles;
    }

    /**
     * 构建扩容请求报文
     *
     * @param task
     * @param confCluster
     * @param beginIndex
     * @param infoClusterVmJob
     * @param confScalingTaskVms
     * @return
     */
    @Override
    public AzureFleetAppendVMsRequest buildAzureFleetAppendVMsRequest(ConfScalingTask task,
                                                                      ConfCluster confCluster,
                                                                      Integer beginIndex,
                                                                      InfoClusterVmJob infoClusterVmJob,
                                                                      List<ConfScalingTaskVm> confScalingTaskVms) {
        AzureFleetAppendVMsRequest azureFleetAppendVMsRequest = new AzureFleetAppendVMsRequest();
        azureFleetAppendVMsRequest.setApiVersion("V1");
        azureFleetAppendVMsRequest.setRegion(confCluster.getRegion());
        azureFleetAppendVMsRequest.setTransactionId(infoClusterVmJob.getTransactionId());
        azureFleetAppendVMsRequest.setClusterName(confCluster.getClusterName());

        List<AzureVMGroupRequest> virtualMachineGroups = new ArrayList<>();

        // 4.获取集群标签
        List<ConfClusterTag> clusterTags =
                tagMapper.getTagsbyClusterId(confCluster.getClusterId());
        HashMap<String, String> tagmap = new HashMap<>();

        if (null != clusterTags && clusterTags.size() > 0) {
            clusterTags.stream().forEach(x -> {
                tagmap.put(x.getTagGroup(), x.getTagVal());
            });
        }
        AvailabilityZone zone = metaDataItemService.getAZ(confCluster.getRegion(), confCluster.getZone());
        confScalingTaskVms.stream().forEach(taskVm -> {

            ConfClusterVm confClusterVm = getConfClusterVm(confCluster,taskVm,task);
            AzureVMGroupRequest azureVMGroupRequest = new AzureVMGroupRequest();
            azureVMGroupRequest.setBeginIndex(beginIndex);
            azureVMGroupRequest.setGroupName(confClusterVm.getGroupName());
            azureVMGroupRequest.setVmRole(confClusterVm.getVmRole());
            azureVMGroupRequest.setProvisionType("Azure_Fleet");

            //region virtualMachineSpec
            VirtualMachineSpec vmSpec = new VirtualMachineSpec();

            confClusterVm.setCount(task.getAfterScalingCount());

            if (confClusterVm==null){
                throw new RuntimeException("查询实例组信息异常");
            }
            //region base
            ResultMsg baseProfileMsg = buildAzureBaseProfile(confCluster,confClusterVm);
            if (baseProfileMsg.getResult() && baseProfileMsg.getData()!=null){
                vmSpec.setBaseProfile((BaseProfile) baseProfileMsg.getData());
            }else{
                throw new RuntimeException("构造扩容报文异常");
            }

            //endregion
            ResultMsg<AzureFleetInfo> fleetInfo = getAzureFleetInfo(confCluster, task.getGroupName());

            SpotProfile azureSpotProfile = buildAzureSpotProfile(task, confCluster, confClusterVm, (AzureFleetInfo)fleetInfo.getData());
            if (Objects.nonNull(azureSpotProfile)) {
                vmSpec.setSpotProfile(azureSpotProfile);
            } else {
                getLogger().info("未生成SpotProfile");
            }

            //region regular
            RegularProfile regularProfile = buildAzureOndemandProfile(task, confCluster, confClusterVm, (AzureFleetInfo)fleetInfo.getData());
            if (Objects.nonNull(regularProfile)) {
                vmSpec.setRegularProfile(regularProfile);
            } else {
                getLogger().info("未生成RegularProfile");
            }
            //endregion regular

            // 如果是竞价实例组, 则需要Sku中设置多个(3个)VmSKU,这个是AzureFleet要求的. 如果是按需, 只需要一个vmsku即可.
//            boolean userMultiSku = Objects.nonNull(vmSpec.getSpotProfile());
            boolean userMultiSku = false;
            vmSpec.setVmSizesProfile(buildVMSizesProfile(confCluster,confClusterVm, userMultiSku));
            //构建虚拟机tags
            Map vmtagmap = (Map) tagmap.clone();
            this.buildVmsTags(vmtagmap,confCluster,confClusterVm,zone,azureSpotProfile);
            vmSpec.setVirtualMachineTags(vmtagmap);

            azureVMGroupRequest.setVirtualMachineSpec(vmSpec);
            virtualMachineGroups.add(azureVMGroupRequest);
            //endregion
        });
        azureFleetAppendVMsRequest.setVirtualMachineGroups(virtualMachineGroups);

        return azureFleetAppendVMsRequest;
    }

    /**
     * 构建OndemandProfile对象<br/>
     *
     * @param task
     * @param confCluster
     * @param confClusterVm
     * @param azureFleetInfo
     * @return
     */
    private RegularProfile buildAzureOndemandProfile(ConfScalingTask task, ConfCluster confCluster,
                                                     ConfClusterVm confClusterVm,
                                                     AzureFleetInfo azureFleetInfo) {
        ResultMsg regularMsg = buildAzureRegularProfile(confCluster, confClusterVm);
        getLogger().info("生成按需实例组Profile为, 下面接着检查是否需要调整Capacity: {}", JSON.toJSONString(regularMsg));
        if (!regularMsg.isResult() || Objects.isNull(regularMsg.getData())) {
            getLogger().error("构建RegularProile失败:" + regularMsg.getErrorMsg());
            return null;
        }

        RegularProfile regularProfile = (RegularProfile) regularMsg.getData();
        if (Objects.nonNull(azureFleetInfo) && azureFleetInfo.hasRegularProfile()) {
            // Azure已经有此AzureFleet的话, 需要调整参数
            regularProfile.setCapacity(task.getScalingCount() + azureFleetInfo.getRegularVmCapacity());
            getLogger().info("按需实例组Proifle为: {}", regularProfile);
            return regularProfile;
        } else {
            getLogger().info("从Azure获取AzureFleet信息时为空或没有RegularProfile, 使用默认的RegularProfile");
            return regularProfile;
        }
    }

    /**
     * 构造一个AzureSpot的Profile, 会根据Task中的ScaleCount重新计算扩容数量.
     * 用于扩容操作中
     * @param task
     * @param confCluster
     * @param confClusterVm
     * @return
     */
    private SpotProfile buildAzureSpotProfile(ConfScalingTask task,
                                            ConfCluster confCluster,
                                            ConfClusterVm confClusterVm,
                                            AzureFleetInfo azureFleetInfo) {
        ResultMsg spotProfileMsg = buildAzureSpotProfile(confCluster, confClusterVm);

        SpotProfile azureSpotProfile =null;
        if (!spotProfileMsg.isResult() || Objects.isNull(spotProfileMsg.getData())) {
            return azureSpotProfile;
        }

        // 从Azure获取AzureFleet的信息, 因为扩容时, AzureFleet中Spot实例会被驱逐,驱逐后又会被补齐,
        // 所以AzureFleet的实际容量不与SDP容量一致,而是在实例的容量基础上增加扩容数量
        Integer finalScalingCount = 0;
        if (Objects.nonNull(azureFleetInfo) && azureFleetInfo.hasSpotProfile()) {
            finalScalingCount = azureFleetInfo.getSpotVmCapacity() + task.getScalingCount();
            getLogger().info("向Azure请求竞价实例扩容,容量调整: 原Capacity={}, 新Capacity={}",
                    azureFleetInfo.getVmCapacity(), finalScalingCount);
        } else {
            // 如果没找到AzureFleet,说明是新建实例组,直接使用扩容后数量
            getLogger().info("获取AzureFleet信息时失败, 扩容数量直接使用扩容任务的:扩容后数量={}.  {}",
                    task.getAfterScalingCount(),
                    "未从Azure获取到AzureFleet的信息");
            finalScalingCount = task.getAfterScalingCount();
        }

        azureSpotProfile = (SpotProfile) spotProfileMsg.getData();
        azureSpotProfile.setCapacity(finalScalingCount);
        getLogger().info("set AzureSpotProfile:{}", azureSpotProfile);

        return azureSpotProfile;
    }

    /**
     * 构建虚拟机tags
     * @param vmtagmap
     * @param confCluster
     * @param confClusterVm
     * @param zone
     * @param azureSpotProfile
     */
    public void buildVmsTags(Map<String, String> vmtagmap,ConfCluster confCluster,ConfClusterVm confClusterVm, AvailabilityZone zone,SpotProfile azureSpotProfile){
        vmtagmap.put("sdp-groupId", confClusterVm.getGroupId());
        vmtagmap.put("sdp-purchaseType", Convert.toStr(confClusterVm.getPurchaseType()));
        vmtagmap.put("sdp-sku", confClusterVm.getSku());
        vmtagmap.put("sdp-clusterId", confCluster.getClusterId());
        vmtagmap.put("sdp-clusterName",confCluster.getClusterName());
        vmtagmap.put("sdp-groupName",confClusterVm.getGroupName());
        vmtagmap.put("sdp-role",confClusterVm.getVmRole());
        //新增 Logical zone,  Physicalzone,opscloud zone
        vmtagmap.put("sdp-opsCloudZone",confCluster.getRegion());
        if (zone!=null){
            vmtagmap.put("sdp-logicalZone",zone.getLogicalZone());
            vmtagmap.put("sdp-physicalZone",zone.getPhysicalZone());

        }else {
            getLogger().error("zone is null");
        }
        if (azureSpotProfile != null) {
            vmtagmap.put("sdp-spot-bid-price", String.valueOf(azureSpotProfile.getMaxPricePerVM()));
        }
        if (StrUtil.isNotEmpty(confClusterVm.getSku())){
            AzurePriceHistory azurePriceHistory = azurePriceHistoryMapper.selectLatestPrice(confCluster.getRegion(), confClusterVm.getSku());
            if (azurePriceHistory!=null){
                vmtagmap.put("sdp-spot-demand-price", String.valueOf(azurePriceHistory.getOndemandUnitPrice()));
            }
        }
    }

    private ConfClusterVm getConfClusterVm(ConfCluster confCluster,ConfScalingTaskVm taskVm,ConfScalingTask task){
        ConfClusterVm confClusterVm;
        List<ConfClusterVm> confClusterVms =
                confClusterVmMapper.getVmConfsByGroupName(task.getGroupName(),task.getClusterId());
        if (CollectionUtils.isNotEmpty(confClusterVms)){
            confClusterVm = confClusterVms.get(0);
        }else {
            getLogger().error("获取实例组信息异常");
            return null;
        }

        List<ConfClusterVmDataVolume> dataDisks = new ArrayList<>();
        taskVm.getVmDataVolumes().stream().forEach(datadisk->{
            ConfClusterVmDataVolume dataVolume = new ConfClusterVmDataVolume();
            dataVolume.setCount(datadisk.getCount());
            dataVolume.setDataVolumeSize(datadisk.getDataVolumeSize());
            dataVolume.setDataVolumeType(datadisk.getDataVolumeType());
            dataDisks.add(dataVolume);
        });
        confClusterVm.setVmDataVolumes(dataDisks);
        return confClusterVm;
    }
    /**
     * 按需单价（标准单价）
     * @return
     */
    @Override
    public BigDecimal getOndemondPrice(String skuName,String region,String subscriptionId){
        ResultMsg msg = getInstancePrice(skuName,region,subscriptionId);
        if (msg.getResult() && msg.getData()!=null) {
            List<InstancePrice> instancePriceList = (List<InstancePrice>) msg.getData();
            if (CollectionUtils.isEmpty(instancePriceList)) {
                getLogger().error("按需单价,未获取到实时价格");
                return null;
            }
            InstancePrice instancePrice = instancePriceList.get(0);
            return instancePrice.getOnDemandUnitPricePerHourUSD();
        }
        return null;
    }


    //endregion

    /**
     * 获取相同规格的Sku名称，筛选顺序:<p/>
     * <ol>
     * <li>系列必须相同</li>
     * <li>配置相同</li>
     * <li>配置相同的凑不够时, 找相同系列的Intel机型, 配置稍高的Sku</li>
     * </ol>
     * 如果实在凑不够, 抛异常
     * @param skuName 基准Sku名称
     * @param region Region
     * @param count 返回数量
     * @return
     */
    public List<VMSku> getSameSpecVmSku(String skuName, String region, int count) {
        List<VMSku> allSkus = azureMetaDataService.getVMSKU(region);
        return getSameSpecVmSku(skuName, allSkus, count);
    }

    public List<VMSku> getSameSpecVmSku(String skuName, List<VMSku> allSkus, int count) {
        // 过滤掉基准sku
        List<VMSkuObj> skuObjs = allSkus.stream().filter(item -> {
            return !StrUtil.equals(item.getName(), skuName);
        }).map(item -> {
            VMSkuObj skuObj = new VMSkuObj();
            skuObj.parse(item.getName());
            skuObj.setVmSku(item);
            return skuObj;
        }).collect(Collectors.toList());

        VMSkuObj targetSkuObj = new VMSkuObj();
        targetSkuObj.parse(skuName);

        List<VMSku> targetVmSkus = new ArrayList<>();
        // 1. 找到同系列, 同配置(CPU,内存)的Sku
        for (VMSkuObj skuObj : skuObjs) {
            if (skuObj.isSameFamily(targetSkuObj.getFamily())  // Family相同
                    && StrUtil.equalsIgnoreCase(skuObj.getVersion(), targetSkuObj.getVersion())  // 版本相同
                    && skuObj.getVCpu() == targetSkuObj.getVCpu()   // CPU数量相同
            ) {
                if (targetVmSkus.size() >= count) {
                    break;
                }
                targetVmSkus.add(skuObj.getVmSku());
            }
        }

        // 2. 找到同系列CPU略大的非AMD机型SKU
        // 找到所有同系列,非AMD, CPU大于目标机型CPU的所有SKU, 然后从小到大顺序取
        List<VMSkuObj> sortedSkus = skuObjs.stream().filter(skuObj -> {
            return skuObj.isSameFamily(targetSkuObj.getFamily())  // Family相同
                    && !skuObj.isAMDCpu()  // 非AMD CPU
                    && skuObj.getVCpu() > targetSkuObj.getVCpu()   // CPU数量大于目标机型CPU
                    && StrUtil.equals(skuObj.getVersion(), targetSkuObj.getVersion())
                    ;
        }).sorted(Comparator.comparingInt(o -> o.getVCpu())).collect(Collectors.toList());

        for (VMSkuObj skuObj : sortedSkus) {
            if (targetVmSkus.size() >= count) {
                break;
            }
            targetVmSkus.add(skuObj.getVmSku());
        }

        return targetVmSkus;
    }

    public static void main(String[] args) {
        AzureFleetServiceImpl svc = new AzureFleetServiceImpl();
        svc.azureFleetUrl = "http://172.179.89.77:8080";
        svc.sdpApiAzureDeleteVmTimeOut = 10;
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterName("sdp-wd-test-44");
        confCluster.setSubscriptionId("bba32ad2-4ac4-4bc3-8c34-ad8b2475d857");
        ResultMsg result = svc.getAzureFleetInfo(confCluster, "task-2");
        svc.deleteAzureFleet(confCluster, "task-2");
        System.out.println(result.getData());

    }

}
