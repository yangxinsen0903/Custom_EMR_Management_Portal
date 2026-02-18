package com.sunbox.sdpadmin.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.BaseReleaseVmImgMapper;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.dao.mapper.ConfClusterVmNeoMapper;
import com.sunbox.dao.mapper.ConfGroupElasticScalingMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.domain.metaData.Region;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.sdpadmin.mapper.*;
import com.sunbox.sdpadmin.model.admin.request.*;
import com.sunbox.service.IConfHostGroupVmSkuService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.misc.VM;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ClusterCreationManager implements BaseCommonInterFace {
    @Value("${ambari.dbname.manual:false}")
    private String ambariDbNameManual;

    @Autowired
    ConfClusterMapper confClusterMapper;

    @Autowired
    ConfClusterTagMapper confClusterTagMapper;

    @Autowired
    ConfTagKeysMapper confTagKeysMapper;

    @Autowired
    ConfClusterAppMapper confClusterAppMapper;

    @Autowired
    BaseReleaseAppsConfigMapper baseReleaseAppsConfigMapper;

    @Autowired
    ConfClusterAppsConfigMapper confClusterAppsConfigMapper;

    @Autowired
    ConfClusterVmNeoMapper confClusterVmNeoMapper;

    @Autowired
    BaseReleaseVmImgMapper baseReleaseVmImgMapper;

    @Autowired
    ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Autowired
    ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    ConfClusterHostGroupAppsConfigMapper confClusterHostGroupAppsConfigMapper;

    @Autowired
    ConfClusterScriptMapper confClusterScriptMapper;

    @Autowired
    ConfGroupElasticScalingRuleMapper confGroupElasticScalingRuleMapper;

    @Autowired
    ConfGroupElasticScalingMapper confGroupElasticScalingMapper;

    @Autowired
    InfoClusterMapper infoClusterMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private IConfHostGroupVmSkuService confHostGroupVmSkuService;

    public void addConfCluster(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId(clusterId);
        confCluster.setClusterName(adminSaveClusterRequest.getClusterName());
        confCluster.setRegion(adminSaveClusterRequest.getRegion());
        confCluster.setCreatedTime(new Date());
        confCluster.setCreatedby(adminSaveClusterRequest.getUserName());
        confCluster.setIsHa(adminSaveClusterRequest.getIsHa());
        confCluster.setDeleteProtected(adminSaveClusterRequest.getDeleteProtected());
        confCluster.setCreationMode(adminSaveClusterRequest.getCreationMode());

        confCluster.setHiveMetadataDburl(adminSaveClusterRequest.getHiveMetadataDbCfgs().geturl());
        confCluster.setHiveMetadataPort(adminSaveClusterRequest.getHiveMetadataDbCfgs().getPort());
        confCluster.setHiveMetadataDatabase(adminSaveClusterRequest.getHiveMetadataDbCfgs().getDatabase());

        confCluster.setLogPath(adminSaveClusterRequest.getLogPath());
        confCluster.setKeypairId(adminSaveClusterRequest.getKeypairId());
        confCluster.setMasterSecurityGroup(adminSaveClusterRequest.getMasterSecurityGroup());
        confCluster.setSlaveSecurityGroup(adminSaveClusterRequest.getSlaveSecurityGroup());
        confCluster.setSubnet(adminSaveClusterRequest.getSubNet());
        confCluster.setVnet(adminSaveClusterRequest.getVNet());
        confCluster.setPublicipAvailable("0");
        if (adminSaveClusterRequest.getWorkOrderCreate() !=null && 1==adminSaveClusterRequest.getWorkOrderCreate()) {
            confCluster.setState(ConfCluster.CREATE_AUDITING);
        } else {
            confCluster.setState(ConfCluster.CREATING);
        }
        confCluster.setVmMI(adminSaveClusterRequest.getVmMI());
        confCluster.setLogMI(adminSaveClusterRequest.getLogMI());
        confCluster.setVmMIClientId(adminSaveClusterRequest.getVmMIClientId());
        confCluster.setVmMITenantId(adminSaveClusterRequest.getVmMITenantId());
        confCluster.setLogMIClientId(adminSaveClusterRequest.getLogMIClientId());
        confCluster.setLogMITenantId(adminSaveClusterRequest.getLogMITenantId());
        confCluster.setClusterReleaseVer(adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer());
        confCluster.setScene(adminSaveClusterRequest.getScene());
        confCluster.setZone(adminSaveClusterRequest.getZone());
        confCluster.setZoneName(adminSaveClusterRequest.getZoneName());
        confCluster.setSrcClusterId(adminSaveClusterRequest.getSrcClusterId());
        confCluster.setEnableGanglia(adminSaveClusterRequest.getEnableGanglia());

        //region 判断是否内嵌数据库
        if (!Objects.equals(adminSaveClusterRequest.getIsEmbedAmbariDb(), 1)) {
            // 使用外部数据库作为ambariserverdb
            confCluster.setIsEmbedAmbariDb(0);

            confCluster.setAmbariDburl(adminSaveClusterRequest.getAmbariDbCfgs().geturl());
            confCluster.setAmbariPort(adminSaveClusterRequest.getAmbariDbCfgs().getPort());
            confCluster.setAmbariDatabase(adminSaveClusterRequest.getAmbariDbCfgs().getDatabase());
            confCluster.setAmbariAcount(adminSaveClusterRequest.getAmbariUsername());
            confCluster.setAmbariDbAutocreate(ambariDbNameManual.equalsIgnoreCase("false") ? 1 : 0);
        } else {
            confCluster.setAmbariDbAutocreate(1);
            confCluster.setIsEmbedAmbariDb(1);
        }

        //endregion

        // 根据region获取订阅ID
        Region regionInfo = metaDataItemService.getRegion(adminSaveClusterRequest.getRegion());

        Assert.notNull(regionInfo, "从元数据中没找到Region。region={}", adminSaveClusterRequest.getRegion());
        confCluster.setSubscriptionId(regionInfo.getSubscriptionId());

        //页面没有传值，先默认为1
        confCluster.setInstanceCollectionType("1");
        Integer iswhiteAddr = adminSaveClusterRequest.getIswhiteAddr();
        confCluster.setIsWhiteAddr(iswhiteAddr);
        confClusterMapper.insertSelective(confCluster);
    }

    public void addConfClusterTag(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        JSONObject jsonObject = adminSaveClusterRequest.getTagMap();
        if (null != jsonObject && jsonObject.size() > 0) {
            for (String key : jsonObject.keySet()) {
                ConfClusterTag confClusterTag = new ConfClusterTag();
                ConfTagKeys confTagKeys = new ConfTagKeys();
                confClusterTag.setClusterId(clusterId);
                confClusterTag.setTagGroup(key);
                confClusterTag.setTagVal(jsonObject.getString(key));
                confClusterTagMapper.insertSelective(confClusterTag);
                confTagKeys.setTagKey(key);
                confTagKeys.setCreatedby("sysadmin");
                confTagKeys.setCreatedTime(new Date());
                confTagKeysMapper.insertSelective(confTagKeys);
            }
        }
    }

    public void addConfClusterApp(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        List<ClusterApp> clusterApps = adminSaveClusterRequest.getInstanceGroupVersion().getClusterApps();
        for (ClusterApp clusterApp : clusterApps) {
            ConfClusterApp confClusterApp = new ConfClusterApp();
            confClusterApp.setClusterId(clusterId);
            confClusterApp.setAppName(clusterApp.getAppName());
            confClusterApp.setAppVersion(clusterApp.getAppVersion());
            confClusterApp.setCreatedTime(new Date());
            confClusterApp.setCreatedby("sysadmin");
            confClusterAppMapper.insertSelective(confClusterApp);
        }
    }

    public void addConfClusterAppsConfig(String clusterId, List<ClusterCfg> clusterCfgs, String releaseVersion) {
        Map<String, String> param = new HashMap<>();
        param.put("releaseVersion", releaseVersion);

        List<BaseReleaseAppsConfig> baseReleaseAppsConfigList =
                baseReleaseAppsConfigMapper.selectByObject(param);

        List<ConfClusterAppsConfig> configs = new ArrayList<>();

        for (ClusterCfg clusterCfg : clusterCfgs) {
            JSONObject jsonObject = clusterCfg.getcfg();
            if (null != jsonObject && jsonObject.size() > 0) {
                for (String key : jsonObject.keySet()) {
                    ConfClusterAppsConfig confClusterAppsConfig = new ConfClusterAppsConfig();
                    confClusterAppsConfig.setAppConfigItemId(UUID.randomUUID().toString());
                    confClusterAppsConfig.setClusterId(clusterId);
                    confClusterAppsConfig.setAppConfigClassification(StrUtil.trim(clusterCfg.getClassification()));
                    confClusterAppsConfig.setConfigItem(StrUtil.trim(key));
                    confClusterAppsConfig.setConfigVal(StrUtil.trim(jsonObject.getString(key)));
                    confClusterAppsConfig.setIsDelete(0);
                    confClusterAppsConfig.setCreatedby("sysadmin");
                    confClusterAppsConfig.setCreatedTime(new Date());

                    Optional<BaseReleaseAppsConfig> appsConfig = baseReleaseAppsConfigList.stream().filter(x -> {
                        return x.getAppConfigClassification().equalsIgnoreCase(clusterCfg.getClassification());
                    }).findFirst();

                    if (appsConfig.isPresent()) {
                        confClusterAppsConfig.setAppName(appsConfig.get().getAppName());
                    } else {
                        getLogger().warn("没有找到对应到Appname：" + clusterCfg.getClassification());
                    }
                    configs.add(confClusterAppsConfig);
                    //confClusterAppsConfigMapper.insertSelective(confClusterAppsConfig);
                }
            }
        }

        if (null != configs && configs.size() > 0) {
            getLogger().info("App configs :" + configs.toString());
            confClusterAppsConfigMapper.batchInsert(configs);
        }

    }

    public void addConfClusterVmAndDataVolume(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = adminSaveClusterRequest.getInstanceGroupSkuCfgs();
        String clusterReleaseVer = adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer();
        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            String vmRole = StringUtils.isEmpty(instanceGroupSkuCfg.getVmRole()) ? "" : instanceGroupSkuCfg.getVmRole().toLowerCase();
            if (adminSaveClusterRequest.getIsHa() == 0 && "master".equals(vmRole)) {
                continue;
            }

            String vmConfId = UUID.randomUUID().toString();
            ConfClusterVm confClusterVm = new ConfClusterVm();
            confClusterVm.setClusterId(clusterId);
            confClusterVm.setCreatedTime(new Date());
            confClusterVm.setCreatedby("sysadmin");
            confClusterVm.setVmConfId(vmConfId);
            confClusterVm.setVmRole(instanceGroupSkuCfg.getVmRole().toLowerCase());

            if (StringUtils.isEmpty(instanceGroupSkuCfg.getGroupName())) {
                confClusterVm.setGroupName(instanceGroupSkuCfg.getVmRole().toLowerCase());
            } else {
                confClusterVm.setGroupName(instanceGroupSkuCfg.getGroupName().toLowerCase());
            }
            confClusterVm.setOsVolumeSize(instanceGroupSkuCfg.getOsVolumeSize());
            confClusterVm.setOsVolumeType(instanceGroupSkuCfg.getOsVolumeType());
            confClusterVm.setState(0);
            confClusterVm.setCount(instanceGroupSkuCfg.getCnt());

            // 竞价相关
            confClusterVm.setPriceStrategy(instanceGroupSkuCfg.getPriceStrategy());
            confClusterVm.setMaxPrice(instanceGroupSkuCfg.getMaxPrice());
            confClusterVm.setPurchasePriority(instanceGroupSkuCfg.getPurchasePriority());

            boolean provisionTypeFlag = false;
            if (adminSaveClusterRequest.getIsHa() == 1) {  // 高可用
                if (instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("Master") ||
                        instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("Core")) {
                    provisionTypeFlag = true;
                }
            } else if (adminSaveClusterRequest.getIsHa() == 0) {  // 非高可用
                if (instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("Core")) {
                    provisionTypeFlag = true;
                }
            }

            if (provisionTypeFlag) {
                String provisionType = instanceGroupSkuCfg.getProvisionType();
                if (StringUtils.isNotBlank(provisionType)) {
                    if (provisionType.equalsIgnoreCase("VM_Standalone")) {
                        confClusterVm.setProvisionType(ConfClusterVm.PROVISION_TYPE_VM_Standalone);
                    } else if (provisionType.equalsIgnoreCase("VMSS_Flexible")) {
                        confClusterVm.setProvisionType(ConfClusterVm.PROVISION_TYPE_VMSS_Flexible);
                    }
                } else {
                    confClusterVm.setProvisionType(ConfClusterVm.PROVISION_TYPE_VM_Standalone);
                }
            }

            if (StringUtils.isNotEmpty(clusterReleaseVer)) {
                BaseReleaseVmImg baseReleaseVmImg = baseReleaseVmImgMapper.selectByPrimaryKey(clusterReleaseVer, instanceGroupSkuCfg.getVmRole().toLowerCase());
                if (null != baseReleaseVmImg) {
                    confClusterVm.setOsImageid(baseReleaseVmImg.getOsImageid());
                    confClusterVm.setOsVersion(baseReleaseVmImg.getOsVersion());
                    confClusterVm.setOsImageType(baseReleaseVmImg.getOsImageType());
                    confClusterVm.setImgId(baseReleaseVmImg.getImgId());
                }
            }
            confClusterVm.setGroupId(instanceGroupSkuCfg.getGroupId());
            confClusterVm.setPurchaseType(instanceGroupSkuCfg.getPurchaseType());

            List<String> skuNameList = instanceGroupSkuCfg.getSkuNames();
            if (PurchaseType.Standard.equalValue(instanceGroupSkuCfg.getPurchaseType())){
                //标准只有一个sku
                confClusterVm.setSku(skuNameList.get(0));
                //优先使用元数据接口
                VMSku vmSku = metaDataItemService.getVMSKU(adminSaveClusterRequest.getRegion(), confClusterVm.getSku());
                if (vmSku != null) {
                    confClusterVm.setVcpus(vmSku.getVCoreCount());
                    confClusterVm.setMemory(vmSku.getMemoryGB());
                    confClusterVm.setCpuType(vmSku.getCpuType());
                } else {
                    confClusterVm.setVcpus(String.valueOf(instanceGroupSkuCfg.getVCPUs()));
                    confClusterVm.setMemory(String.valueOf(instanceGroupSkuCfg.getMemoryGB()));
                    confClusterVm.setCpuType(instanceGroupSkuCfg.getCpuType());
                }
            }
            //分配策略
            confClusterVm.setSpotAllocationStrategy(instanceGroupSkuCfg.getSpotAllocationStrategy());
            confClusterVm.setRegularAllocationStrategy(instanceGroupSkuCfg.getRegularAllocationStrategy());
            // endregion
            confClusterVmNeoMapper.insertSelective(confClusterVm);

            //初始化 ConfHostGroupVmSku
            confHostGroupVmSkuService.addConfHostGroupVmSku(adminSaveClusterRequest.getRegion(),confClusterVm,skuNameList);

            ConfClusterVmDataVolume confClusterVmDataVolume = new ConfClusterVmDataVolume();
            confClusterVmDataVolume.setVmConfId(vmConfId);
            confClusterVmDataVolume.setCount(Integer.parseInt(instanceGroupSkuCfg.getDataVolumeCount()));
            confClusterVmDataVolume.setVolumeConfId(UUID.randomUUID().toString());
            confClusterVmDataVolume.setDataVolumeType(instanceGroupSkuCfg.getDataVolumeType());
            confClusterVmDataVolume.setDataVolumeSize(instanceGroupSkuCfg.getDataVolumeSize());
            confClusterVmDataVolume.setIops(confClusterVmDataVolume.getBaseIOPS());
            confClusterVmDataVolume.setThroughput(confClusterVmDataVolume.getBaseThroughput());
            confClusterVmDataVolumeMapper.insertSelective(confClusterVmDataVolume);
        }
    }

    public void addConfClusterHostGroupAppsConfig(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        Map<String, String> param = new HashMap<>();
        param.put("releaseVersion", adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer());

        List<BaseReleaseAppsConfig> baseReleaseAppsConfigList = baseReleaseAppsConfigMapper.selectByObject(param);

        for (InstanceGroupSkuCfg instanceGroupSkuCfg : adminSaveClusterRequest.getInstanceGroupSkuCfgs()) {
            if (Objects.equals(adminSaveClusterRequest.getIsHa(), 0) && StrUtil.equalsIgnoreCase("master", instanceGroupSkuCfg.getVmRole())) {
                continue;
            }
            List<ConfClusterHostGroupAppsConfig> configs = new ArrayList<>();

            ConfClusterHostGroup hostGroup = new ConfClusterHostGroup();
            hostGroup.setGroupName(instanceGroupSkuCfg.getGroupName().toLowerCase());
            hostGroup.setGroupId(UUID.randomUUID().toString());
            hostGroup.setInsCount(instanceGroupSkuCfg.getCnt());
            hostGroup.setClusterId(clusterId);
            hostGroup.setVmRole(instanceGroupSkuCfg.getVmRole().toLowerCase());
            //todo 这里是否需要修改为创建审核中?
            hostGroup.setState(ConfClusterHostGroup.STATE_CREATING);
            hostGroup.setPurchaseType(instanceGroupSkuCfg.getPurchaseType());
            hostGroup.setCreatedTime(cn.hutool.core.date.DateUtil.date());
            hostGroup.setEnableAfterstartScript(instanceGroupSkuCfg.getEnableAfterstartScript());
            hostGroup.setEnableBeforestartScript(instanceGroupSkuCfg.getEnableBeforestartScript());
            //竞价实例组时设置期望购买数量
            if (PurchaseType.Spot.equalValue(hostGroup.getPurchaseType())) {
                hostGroup.setExpectCount(instanceGroupSkuCfg.getCnt());
            }
            confClusterHostGroupMapper.insertSelective(hostGroup);
            instanceGroupSkuCfg.setGroupId(hostGroup.getGroupId());

            List<ClusterCfg> clusterCfgs = new ArrayList<>();
            if (CollUtil.isNotEmpty(instanceGroupSkuCfg.getGroupCfgs())) {
                clusterCfgs = instanceGroupSkuCfg.getGroupCfgs();
            }

            for (ClusterCfg clusterCfg : clusterCfgs) {
                JSONObject jsonObject = clusterCfg.getcfg();
                if (null != jsonObject && jsonObject.size() > 0) {
                    for (String key : jsonObject.keySet()) {
                        ConfClusterHostGroupAppsConfig hostGroupAppsConfig = new ConfClusterHostGroupAppsConfig();
                        hostGroupAppsConfig.setAppConfigItemId(UUID.randomUUID().toString());
                        hostGroupAppsConfig.setClusterId(clusterId);
                        hostGroupAppsConfig.setAppConfigClassification(clusterCfg.getClassification());
                        hostGroupAppsConfig.setConfigItem(key);
                        hostGroupAppsConfig.setGroupId(hostGroup.getGroupId());
                        hostGroupAppsConfig.setConfigVal(jsonObject.getString(key));
                        hostGroupAppsConfig.setIsDelete(ConfClusterHostGroupAppsConfig.DELETE_NO);
                        hostGroupAppsConfig.setCreatedby("sysadmin");
                        hostGroupAppsConfig.setCreatedTime(new Date());

                        Optional<BaseReleaseAppsConfig> appsConfig = baseReleaseAppsConfigList.stream().filter(x -> {
                            return x.getAppConfigClassification().equalsIgnoreCase(clusterCfg.getClassification());
                        }).findFirst();

                        if (appsConfig.isPresent()) {
                            hostGroupAppsConfig.setAppName(appsConfig.get().getAppName());
                        } else {
                            getLogger().warn("没有找到对应到Appname：" + clusterCfg.getClassification());
                        }
                        configs.add(hostGroupAppsConfig);
                    }
                }
            }

            if (CollUtil.isNotEmpty(configs)) {
                getLogger().info("Host Group App configs :clusterId={},groupId={},size={}", clusterId, hostGroup.getGroupId(), configs.toString());
                confClusterHostGroupAppsConfigMapper.batchInsert(configs);
            }
        }
    }

    public void addConfClusterScript(String clusterId, List<com.sunbox.sdpadmin.model.admin.request.ConfClusterScript> confClusterScripts) {
        for (com.sunbox.sdpadmin.model.admin.request.ConfClusterScript ccs : confClusterScripts) {
            com.sunbox.domain.ConfClusterScript confClusterScript = new com.sunbox.domain.ConfClusterScript();
            confClusterScript.setCreatedTime(new Date());
            confClusterScript.setCreatedby("sysadmin");
            confClusterScript.setClusterId(clusterId);
            confClusterScript.setConfScriptId(UUID.randomUUID().toString());
            if (ccs.getRunTiming().equalsIgnoreCase("aftervminit")) {
                confClusterScript.setRunTiming("aftervminit");
            }
            if (ccs.getRunTiming().equalsIgnoreCase("beforestart")) {
                confClusterScript.setRunTiming("beforestart");
            }
            if (ccs.getRunTiming().equalsIgnoreCase("afterstart")) {
                confClusterScript.setRunTiming("afterstart");
            }
            confClusterScript.setScriptName(ccs.getScriptName());
            confClusterScript.setScriptPath(ccs.getScriptPath());
            confClusterScript.setScriptParam(ccs.getScriptParam());
            confClusterScript.setSortNo(ccs.getSortNo());
            confClusterScriptMapper.insertSelective(confClusterScript);
        }
    }

    public void addElasticScaleRules(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest, List<InstanceGroupSkuCfg> instanceGroupSkuCfgs) {
        if (CollUtil.isEmpty(instanceGroupSkuCfgs)) {
            getLogger().info("实例组信息为空,clusterId={},clusterName={},groupName={}", clusterId, adminSaveClusterRequest.getClusterName(), adminSaveClusterRequest.getGroupName());
            return;
        }
        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            if (Objects.isNull(instanceGroupSkuCfg.getConfGroupElasticScalingData())
                    || CollUtil.isEmpty(instanceGroupSkuCfg.getConfGroupElasticScalingData().getScalingRules())) {
                getLogger().info("未配置弹性规则信息:clusterId={},clusterName={},groupName={}", clusterId, adminSaveClusterRequest.getClusterName(), instanceGroupSkuCfg.getGroupName());
                continue;
            }
            ConfGroupElasticScalingData confGroupElasticScalingData = instanceGroupSkuCfg.getConfGroupElasticScalingData();
            List<ConfGroupElasticScalingRuleData> scalingRules = instanceGroupSkuCfg.getConfGroupElasticScalingData().getScalingRules();
            //添加弹性规则配置
            ConfGroupElasticScaling elasticScaling = new ConfGroupElasticScaling();
            elasticScaling.setGroupEsId(UUID.randomUUID().toString());
            elasticScaling.setClusterId(clusterId);
            elasticScaling.setGroupName(confGroupElasticScalingData.getGroupName());
            elasticScaling.setVmRole(StringUtils.lowerCase(confGroupElasticScalingData.getVmRole()));
            elasticScaling.setMaxCount(confGroupElasticScalingData.getMaxCount());
            elasticScaling.setMinCount(confGroupElasticScalingData.getMinCount());
            elasticScaling.setCreatedby("system");
            elasticScaling.setCreatedTime(new Date());
            elasticScaling.setIsValid(ConfGroupElasticScaling.ISVALID_YES);
            confGroupElasticScalingMapper.insertSelective(elasticScaling);

            for (ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData : scalingRules) {
                ConfGroupElasticScalingRule confGroupElasticScalingRule = new ConfGroupElasticScalingRule();
                try {
                    BeanUtils.copyProperties(confGroupElasticScalingRule, confGroupElasticScalingRuleData);
                } catch (Exception e) {
                    getLogger().error("AdminApiServiceImpl.postElasticScalingRule copyProperties error. confGroupElasticScalingRuleData: {}, e: {}", JSON.toJSONString(confGroupElasticScalingRuleData), e);
                }
                confGroupElasticScalingRule.setEsRuleId(UUID.randomUUID().toString());
                confGroupElasticScalingRule.setGroupEsId(elasticScaling.getGroupEsId());
                confGroupElasticScalingRule.setClusterId(clusterId);
                confGroupElasticScalingRule.setGroupName(confGroupElasticScalingData.getGroupName());
                confGroupElasticScalingRule.setIsValid(ConfGroupElasticScalingRule.ISVALID_YES);
                confGroupElasticScalingRule.setCreatedby("system");
                confGroupElasticScalingRule.setCreatedTime(new Date());
                confGroupElasticScalingRuleMapper.insertSelective(confGroupElasticScalingRule);
            }
        }
    }

    public void addInfoCluster(String clusterId) {
        InfoCluster infoCluster = new InfoCluster();
        infoCluster.setClusterId(clusterId);
        ConfClusterApp confClusterApp = new ConfClusterApp();
        confClusterApp.setClusterId(clusterId);
        List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(confClusterApp);
        infoCluster.setAppsCount(null == confClusterApps ? 0 : confClusterApps.size());
        infoClusterMapper.insertSelective(infoCluster);
    }
}
