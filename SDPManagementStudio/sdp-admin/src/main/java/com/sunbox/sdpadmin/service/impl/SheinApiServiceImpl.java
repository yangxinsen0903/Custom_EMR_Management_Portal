package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.ConfClusterScript;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.DiskPerformanceRequest;
import com.sunbox.domain.cluster.WorkOrderApprovalRequest;
import com.sunbox.domain.cluster.WorkorderCallbackRequest;
import com.sunbox.domain.enums.PurchaseType;
import com.sunbox.domain.metaData.AvailabilityZone;
import com.sunbox.domain.metaData.ManagedIdentity;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.domain.metaData.keyVault;
import com.sunbox.sdpadmin.filter.RegionMappingUtil;
import com.sunbox.sdpadmin.mapper.*;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpadmin.model.admin.request.*;
import com.sunbox.sdpadmin.model.shein.request.*;
import com.sunbox.sdpadmin.model.shein.response.*;
import com.sunbox.sdpadmin.service.*;
import com.sunbox.sdpadmin.strategy.ClusterCreationStrategy;
import com.sunbox.sdpadmin.strategy.ClusterCreationStrategyFactory;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdpservice.service.ScaleService;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.util.*;

import com.sunbox.web.BaseCommonInterFace;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sunbox.service.consts.SheinParamConstant.*;

@Service
public class SheinApiServiceImpl implements SheinApiService, BaseCommonInterFace {
    @Value("${sdp.dns.ttl:3600}")
    private Integer sdp_dns_ttl;

    @Value("${sdp.scalein.waiting.min:60}")
    private Integer scaleinWaitingMin;

    @Value("${sdp.scalein.waiting.max:1800}")
    private Integer scaleinWaitingMax;

    @Resource
    private ConfClusterMapper confClusterMapper;

    @Resource
    private ConfClusterAppMapper confClusterAppMapper;

    @Resource
    private ConfClusterAppsConfigMapper confClusterAppsConfigMapper;

    @Resource
    private ConfClusterVmNeoMapper confClusterVmNeoMapper;

    @Resource
    private ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Resource
    private InfoClusterVmMapper infoClusterVmMapper;

    @Resource
    private BaseReleaseAppsMapper baseReleaseAppsMapper;

    @Resource
    private ConfClusterTagMapper confClusterTagMapper;

    @Resource
    private BaseReleaseAppsConfigMapper baseReleaseAppsConfigMapper;

    @Resource
    private InfoClusterOperationPlanMapper infoClusterOperationPlanMapper;

    @Autowired
    private ConfClusterOpTaskMapper confClusterOpTaskMapper;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private AdminApiService adminApiService;

    @Resource
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private BaseReleaseVmImgMapper baseReleaseVmImgMapper;

    @Autowired
    private ConfClusterScriptMapper confClusterScriptMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    private ICheckParam checkParam;

    @Autowired
    private BaseSceneAppsMapper baseSceneAppsMapper;

    @Autowired
    private BaseSceneMapper baseSceneMapper;

    @Resource
    private BaseScriptMapper baseScriptMapper;

    @Autowired
    private ConfGroupElasticScalingMapper confGroupElasticScalingMapper;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    private ConfGroupElasticScalingRuleMapper confGroupElasticScalingRuleMapper;

    @Autowired
    private ConfClusterHostGroupAppsConfigMapper confClusterHostGroupAppsConfigMapper;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    ClusterCreationStrategyFactory clusterCreationStrategyFactory;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    private AuthKeyMapper authKeyMapper;

    @Autowired
    private WorkOrderApprovalRequestMapper workOrderApprovalRequestMapper;

//    @Autowired
//    private CreateClusterRateLimiter createClusterRateLimiter;

    @Value("${admin.message.clientname}")
    private String clientname;

    /**
     * Ambari数据库名是否手动设置，默认为false
     */
    @Value("${ambari.dbname.manual:false}")
    private String ambariDbNameManual;

    @Value("${cluster.creater.mock:0}")
    private String clusterMock;

    @Value("${core.scalein.max.count:3}")
    private Integer scaleInMaxCount;

    private final static String SCRIPT_JOBNAME_REGULAR = "^[\\u4E00-\\u9FA5-_A-Za-z0-9]{6,36}$";

    private final static String ELASTIC_RULE_NAME_REGULAR = "^[\\u4E00-\\u9FA5-_A-Za-z0-9]{1,100}$";

    private final static String ELASTIC_RULE_ID_REGULAR = "^[-A-Za-z0-9]{36}$";

    private final static String INSTANCE_GROUP_NAME_REGULAR = "^[\\u4E00-\\u9FA5-_A-Za-z0-9]{1,30}$";

    private final static String CLUSTER_CONFIG_PROPERTIES_CONFS_KEY = "^[\\u4E00-\\u9FA5-_A-Za-z0-9]{1,300}$";

    private final static String CLUSTER_CONFIG_PROPERTIES_CONFS_VALUE = "^[\\u4E00-\\u9FA5-_A-Za-z0-9]{1,300}$";

    private final static String[] ES_AGGREGATE_TYPE_ARR = {"max", "min", "avg"};

    private final static String[] ES_OPERATOR_ARR = {">=", ">", "<=", "<"};

    private final static String[] ES_LOAD_METRIC_ARR = {"MemoryAvailablePrecentage", "ContainerPendingRatio", "VCoreAvailablePrecentage","AppsPending"};
    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    public void addConfCluster(String clusterId, SheinRequestModel sheinRequestModel) {
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId(clusterId);
        confCluster.setClusterName(sheinRequestModel.getClusterName());
        confCluster.setClusterReleaseVer(sheinRequestModel.getClusterReleaseVer());
        confCluster.setRegion(sheinRequestModel.getDc());
        confCluster.setVnet(sheinRequestModel.getvNet());
        confCluster.setLogMI(sheinRequestModel.getLogMI());
        confCluster.setVmMI(sheinRequestModel.getVmMI());
        confCluster.setVmMIClientId(sheinRequestModel.getVmMIClientId());
        confCluster.setVmMITenantId(sheinRequestModel.getVmMITenantId());
        confCluster.setSubnet(sheinRequestModel.getSubnet());
        confCluster.setMasterSecurityGroup(sheinRequestModel.getMasterSecurityGroup());
        confCluster.setSlaveSecurityGroup(sheinRequestModel.getSlaveSecurityGroup());
        confCluster.setLogPath(sheinRequestModel.getS3LogLocation());
        confCluster.setKeypairId(sheinRequestModel.getInstanceKeyPair());
        confCluster.setScene(sheinRequestModel.getScene());
        confCluster.setZone(sheinRequestModel.getAz());
        confCluster.setZoneName(getZoneNameByZoneNumber(sheinRequestModel.getAz(), sheinRequestModel.getDc()));
        confCluster.setEnableGanglia(sheinRequestModel.getEnableGanglia());
        confCluster.setDeleteProtected(StringUtils.isEmpty(sheinRequestModel.getDeleteProtected())
                ? "0" : sheinRequestModel.getDeleteProtected());

        if (null != sheinRequestModel.getInstanceGroupNewConfigs() && sheinRequestModel.getInstanceGroupNewConfigs().size() > 0) {
            confCluster.setInstanceCollectionType("1");
        }
        if (null != sheinRequestModel.getInstanceFleetNewConfigs() && sheinRequestModel.getInstanceFleetNewConfigs().size() > 0) {
            confCluster.setInstanceCollectionType("2");
        }
        if (sheinRequestModel.getInternalIpOnly() == null || sheinRequestModel.getInternalIpOnly()) {
            confCluster.setPublicipAvailable("0");
        } else {
            confCluster.setPublicipAvailable("1");
        }
        confCluster.setIsEmbedAmbariDb(sheinRequestModel.getIsEmbedAmbariDb());
        if (!Objects.equals(sheinRequestModel.getIsEmbedAmbariDb(),1)) {
            //使用外部数据库
            if (sheinRequestModel.getAmbariDbCfgs() != null) {
                confCluster.setAmbariDatabase(sheinRequestModel.getAmbariDbCfgs().getDatabase());
                confCluster.setAmbariDburl(sheinRequestModel.getAmbariDbCfgs().getUrl());
                if (sheinRequestModel.getAmbariDbCfgs().getPort() == null) {
                    // 默认3306
                    confCluster.setAmbariPort("3306");
                } else {
                    confCluster.setAmbariPort(sheinRequestModel.getAmbariDbCfgs().getPort() + "");
                }
            }

        }else {
            // 使用内置数据库
            // do nothing
        }

        if (sheinRequestModel.getHiveMetadataDbCfgs() != null) {
            confCluster.setHiveMetadataDatabase(sheinRequestModel.getHiveMetadataDbCfgs().getDatabase());
            confCluster.setHiveMetadataDburl(sheinRequestModel.getHiveMetadataDbCfgs().getUrl());
            if (sheinRequestModel.getHiveMetadataDbCfgs().getPort() == null) {
                // 默认3306
                confCluster.setHiveMetadataPort("3306");
            } else {
                confCluster.setHiveMetadataPort(sheinRequestModel.getHiveMetadataDbCfgs().getPort() + "");
            }
        }

        // hivemetadata数据库账号密码 写入 keyvault
        //  ambari数据库账号密码 写入 keyvault
        saveKeyVault(sheinRequestModel);

        confCluster.setAmbariDbAutocreate(ambariDbNameManual.equalsIgnoreCase("false") ? 1 : 0);

        confCluster.setIsHa(sheinRequestModel.getStartHa());
        confCluster.setState(ConfCluster.CREATING);
        confCluster.setCreatedby(sheinRequestModel.getCreateUser());
        confCluster.setCreatedTime(new Date());
        confCluster.setLogMIClientId(sheinRequestModel.getLogMIClientId());
        confCluster.setLogMITenantId(sheinRequestModel.getLogMITenantId());
        confClusterMapper.insertSelective(confCluster);
    }

    public String getZoneNameByZoneNumber(String zoneNumber,String region) {
        AvailabilityZone zone = metaDataItemService.getAZ(region, zoneNumber);
        if (zone!=null){
            return zone.getAvailabilityZone();
        }
        return null;
    }

    private void fillMIExtInfo(SheinRequestModel req) {
        if (StringUtils.isNotEmpty(req.getVmMIClientId()) && StringUtils.isNotEmpty(req.getVmMITenantId())) {
            return;
        }
        String region=req.getDc();
        List<ManagedIdentity> list = metaDataItemService.getMIList(region);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (ManagedIdentity identity : list) {
            if (Objects.equals(identity.getResourceId(), req.getVmMI())) {
                req.setVmMIClientId(identity.getClientId());
                req.setVmMITenantId(identity.getTenantId());
                break;
            }
        }

        for (ManagedIdentity identity : list) {
            if (Objects.equals(identity.getResourceId(), req.getLogMI())) {
                req.setLogMIClientId(identity.getClientId());
                req.setLogMITenantId(identity.getTenantId());
                break;
            }
        }
    }

    public void addClusterApps(String clusterId, SheinRequestModel sheinRequestModel){
        List<String> clusterApps = sheinRequestModel.getClusterApps();

        if(StringUtils.isNotEmpty(sheinRequestModel.getScene())){

            BaseScene baseScene=baseSceneMapper.queryByReleaseVerAndSceneName(sheinRequestModel.getClusterReleaseVer(),
                    sheinRequestModel.getScene());
            if (baseScene==null){
                return;
            }
            List<BaseSceneApps> baseSceneApps=baseSceneAppsMapper
                    .queryBySceneId(baseScene.getSceneId());
            getLogger().info("获取到场景下:"+sheinRequestModel.getScene()+",Apps:"+baseSceneApps.toString());

            List<BaseSceneApps> requiredApps=baseSceneApps.stream().filter(x->{
                return x.getRequired().equals(1);
            }).collect(Collectors.toList());

            if (clusterApps==null){
                clusterApps=new CopyOnWriteArrayList<>();
            }
            for (BaseSceneApps app:requiredApps){
                Optional<String> clusterapp=clusterApps.stream().filter(x->{
                    return x.equalsIgnoreCase(app.getAppName());
                }).findFirst();
                if (!clusterapp.isPresent()){
                    clusterApps.add(app.getAppName());
                }
            }
        }


        if(null!=clusterApps && clusterApps.size()>0){
            for (String ca : clusterApps){
                ConfClusterApp confClusterApp = new ConfClusterApp();
                confClusterApp.setAppName(ca);
                BaseReleaseApps baseReleaseApps
                        = baseReleaseAppsMapper.selectByPrimaryKey(sheinRequestModel.getClusterReleaseVer(),ca);
                if(null!=baseReleaseApps){
                    confClusterApp.setAppVersion(baseReleaseApps.getAppVerison());
                }
                confClusterApp.setClusterId(clusterId);
                confClusterApp.setCreatedby(sheinRequestModel.getCreateUser());
                confClusterApp.setCreatedTime(new Date());
                confClusterAppMapper.insertSelective(confClusterApp);
            }
        }
    }

    public void addConfClusterAppsConfig(String clusterId, SheinRequestModel sheinRequestModel){

        Map<String,String> param=new HashMap<>();
        param.put("releaseVersion",sheinRequestModel.getClusterReleaseVer());

        List<BaseReleaseAppsConfig> baseReleaseAppsConfigList=
                baseReleaseAppsConfigMapper.selectByObject(param);

        List<Empty> clusterCfgs = sheinRequestModel.getClusterCfgs();
        List<ConfClusterAppsConfig> configs=new ArrayList<>();

        if(null!=clusterCfgs && clusterCfgs.size()>0){
            for (Empty empty : clusterCfgs){
                for(String key : empty.getcfg().keySet()){
                    ConfClusterAppsConfig confClusterAppsConfig = new ConfClusterAppsConfig();
                    confClusterAppsConfig.setConfigItem(key);
                    confClusterAppsConfig.setConfigVal(empty.getcfg().get(key).toString());
                    confClusterAppsConfig.setClusterId(clusterId);
                    confClusterAppsConfig.setAppConfigItemId(UUID.randomUUID().toString());

                    Optional<BaseReleaseAppsConfig> appsConfig=baseReleaseAppsConfigList.stream().filter(x->{
                        return x.getAppConfigClassification().equalsIgnoreCase(empty.getClassification());
                    }).findFirst();

                    if (appsConfig.isPresent()) {
                        confClusterAppsConfig.setAppName(appsConfig.get().getAppName());
                    }else{
                        getLogger().warn("没有找到对应到Appname："+empty.getClassification());
                    }

                    confClusterAppsConfig.setAppConfigClassification(empty.getClassification());
                    confClusterAppsConfig.setCreatedby(sheinRequestModel.getCreateUser());
                    confClusterAppsConfig.setCreatedTime(new Date());
                    confClusterAppsConfig.setIsDelete(0);
                    configs.add(confClusterAppsConfig);
                }
            }
        }
        if(null!=configs&&configs.size()>1){
            getLogger().info("App configs :"+configs.toString());
            confClusterAppsConfigMapper.batchInsert(configs);
        }
    }

    public void addConfClusterVmAndDataVolume(String clusterId, SheinRequestModel sheinRequestModel) {
        Map<String, VMSku> vmSkuMap = metaDataItemService.getVmSkuMap(sheinRequestModel.getDc());
        List<InstanceGroupNewConfigElement> instanceGroupNewConfigElements = sheinRequestModel.getInstanceGroupNewConfigs();
        if (null != instanceGroupNewConfigElements && instanceGroupNewConfigElements.size() > 0) {
            for (InstanceGroupNewConfigElement instanceGroupNewConfigElement : instanceGroupNewConfigElements) {
                InstanceGroupAddConfig instanceGroupAddConfig = instanceGroupNewConfigElement.getInstanceGroupAddConfig();
                ConfClusterVm confClusterVm = new ConfClusterVm();
                String vmConfId = UUID.randomUUID().toString();
                confClusterVm.setVmConfId(vmConfId);
                confClusterVm.setClusterId(clusterId);
                confClusterVm.setVmRole(instanceGroupAddConfig.getInsGpRole().toLowerCase());
                if (StringUtils.isEmpty(instanceGroupAddConfig.getInsGpName())) {
                    confClusterVm.setGroupName(instanceGroupAddConfig.getInsGpRole().toLowerCase());
                } else {
                    confClusterVm.setGroupName(instanceGroupAddConfig.getInsGpName().toLowerCase());
                }
                confClusterVm.setGroupId(instanceGroupAddConfig.getInsGroupId());
                confClusterVm.setSku(instanceGroupAddConfig.getInsType());
                confClusterVm.setOsVolumeSize(sheinRequestModel.getRootVolSize());
                confClusterVm.setOsVolumeType(sheinRequestModel.getRootVolType());
                confClusterVm.setCount(instanceGroupAddConfig.getInsGpCnt());

                //region 优先使用元数据的信息
                VMSku vmSku = vmSkuMap.get(instanceGroupAddConfig.getInsType());
                if (vmSku!=null
                        && StrUtil.isNotEmpty(vmSku.getVCoreCount())
                        && StrUtil.isNotEmpty(vmSku.getMemoryGB())
                        && StrUtil.isNotEmpty(vmSku.getCpuType())){
                    confClusterVm.setVcpus(vmSku.getVCoreCount());
                    confClusterVm.setMemory(vmSku.getMemoryGB());
                    confClusterVm.setCpuType(vmSku.getCpuType());
                }else{
                    confClusterVm.setVcpus(instanceGroupAddConfig.getvCpus());
                    confClusterVm.setMemory(instanceGroupAddConfig.getMemory());
                    confClusterVm.setCpuType(vmSku.getCpuType());
                }
                //endregion 优先使用元数据的信息

                if ("ondemond".equals(instanceGroupAddConfig.getInsMktType())) {
                    // 按需
                    confClusterVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_ONDEMOND);
                }
                if ("spot".equals(instanceGroupAddConfig.getInsMktType())) {
                    // 竞价
                    confClusterVm.setPurchaseType(ConfClusterVm.PURCHASETYPE_SPOT);
                }

                if (StringUtils.isNotEmpty(sheinRequestModel.getClusterReleaseVer())) {
                    BaseReleaseVmImg baseReleaseVmImg =
                            baseReleaseVmImgMapper.selectByPrimaryKey(
                                    sheinRequestModel.getClusterReleaseVer(),
                                    instanceGroupAddConfig.getInsGpRole().toLowerCase());

                    if (null != baseReleaseVmImg) {
                        confClusterVm.setOsImageid(baseReleaseVmImg.getOsImageid());
                        confClusterVm.setOsVersion(baseReleaseVmImg.getOsVersion());
                        confClusterVm.setOsImageType(baseReleaseVmImg.getOsImageType());
                        confClusterVm.setImgId(baseReleaseVmImg.getImgId());
                    }
                }

                confClusterVm.setPriceStrategy(instanceGroupAddConfig.getPriceStrategy());
                confClusterVm.setMaxPrice(instanceGroupAddConfig.getPriceStrategyValue());
                confClusterVm.setPurchasePriority(instanceGroupAddConfig.getPurchasePriority());

                confClusterVm.setCreatedby(sheinRequestModel.getCreateUser());
                confClusterVm.setCreatedTime(new Date());
                confClusterVmNeoMapper.insertSelective(confClusterVm);
                this.addVmDataVolume(vmConfId, instanceGroupAddConfig);
            }
        }
    }

    public void addConfClusterTag(String clusterId, SheinRequestModel sheinRequestModel){
        JSONObject tagMapJsonObj = sheinRequestModel.getTagMap();
        if(null!=tagMapJsonObj){
            for(String key : tagMapJsonObj.keySet()){
                ConfClusterTag confClusterTag = new ConfClusterTag();
                confClusterTag.setClusterId(clusterId);
                confClusterTag.setTagGroup(key);
                confClusterTag.setTagVal(tagMapJsonObj.getString(key));
                confClusterTagMapper.insertSelective(confClusterTag);
            }
        }
    }

    public void addVmDataVolume(String vmConfId,InstanceGroupAddConfig addConfig){
        ConfClusterVmDataVolume confClusterVmDataVolume = new ConfClusterVmDataVolume();
        confClusterVmDataVolume.setVmConfId(vmConfId);
        confClusterVmDataVolume.setVolumeConfId(UUID.randomUUID().toString());
        confClusterVmDataVolume.setDataVolumeSize(addConfig.getVolumeSizeInGB());
        confClusterVmDataVolume.setDataVolumeType(addConfig.getVolumeType());
        if(null==addConfig.getDataDiskCnt()){
            confClusterVmDataVolume.setCount(1);
        }else {
            confClusterVmDataVolume.setCount(addConfig.getDataDiskCnt());
        }
        confClusterVmDataVolume.setIops(confClusterVmDataVolume.getBaseIOPS());
        confClusterVmDataVolume.setThroughput(confClusterVmDataVolume.getBaseThroughput());
        confClusterVmDataVolumeMapper.insertSelective(confClusterVmDataVolume);
    }

    private void addConfClusterHostGroupAppsConfig(String clusterId, SheinRequestModel sheinRequestModel) {
        Map<String, String> param = new HashMap<>();
        param.put("releaseVersion", sheinRequestModel.getClusterReleaseVer());

        List<BaseReleaseAppsConfig> baseReleaseAppsConfigList =
                baseReleaseAppsConfigMapper.selectByObject(param);

        for (InstanceGroupNewConfigElement instanceGroupSkuCfg : sheinRequestModel.getInstanceGroupNewConfigs()) {
            List<ConfClusterHostGroupAppsConfig> configs = new ArrayList<>();

            ConfClusterHostGroup hostGroup = new ConfClusterHostGroup();

            if (StringUtils.isEmpty(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpName())) {
                hostGroup.setGroupName(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpRole().toLowerCase());
            } else {
                hostGroup.setGroupName(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpName().toLowerCase());
            }

            hostGroup.setGroupId(UUID.randomUUID().toString());
            hostGroup.setInsCount(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpCnt());
            hostGroup.setClusterId(clusterId);
            hostGroup.setVmRole(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpRole().toLowerCase());
            hostGroup.setState(ConfClusterHostGroup.STATE_CREATING);

            if (instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsMktType().equalsIgnoreCase("spot")) {
                hostGroup.setPurchaseType(2);
            } else {
                hostGroup.setPurchaseType(1);
            }

            // 竞价实例组时设置期望购买数量
            if (PurchaseType.Spot.equalValue(hostGroup.getPurchaseType())) {
                hostGroup.setExpectCount(instanceGroupSkuCfg.getInstanceGroupAddConfig().getInsGpCnt());
            }

            hostGroup.setCreatedTime(cn.hutool.core.date.DateUtil.date());
            confClusterHostGroupMapper.insertSelective(hostGroup);
            instanceGroupSkuCfg.getInstanceGroupAddConfig().setInsGroupId(hostGroup.getGroupId());

            List<InstanceGroupCfg> clusterCfgs = new ArrayList<>();
            if (CollUtil.isNotEmpty(instanceGroupSkuCfg.getInstanceGroupCfgs())) {
                clusterCfgs = instanceGroupSkuCfg.getInstanceGroupCfgs();
            }

            for (InstanceGroupCfg clusterCfg : clusterCfgs) {
                Map<String, Object> jsonObject = clusterCfg.getcfg();
                if (null != jsonObject && jsonObject.size() > 0) {
                    for (String key : jsonObject.keySet()) {
                        ConfClusterHostGroupAppsConfig hostGroupAppsConfig = new ConfClusterHostGroupAppsConfig();
                        hostGroupAppsConfig.setAppConfigItemId(UUID.randomUUID().toString());
                        hostGroupAppsConfig.setClusterId(clusterId);
                        hostGroupAppsConfig.setAppConfigClassification(clusterCfg.getClassification());
                        hostGroupAppsConfig.setConfigItem(key);
                        hostGroupAppsConfig.setGroupId(hostGroup.getGroupId());
                        hostGroupAppsConfig.setConfigVal(jsonObject.get(key).toString());
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

    private void addInfoCluster(String clusterId){
        InfoCluster infoCluster = new InfoCluster();
        infoCluster.setClusterId(clusterId);
        ConfClusterApp confClusterApp = new ConfClusterApp();
        confClusterApp.setClusterId(clusterId);
        List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(confClusterApp);
        infoCluster.setAppsCount(null==confClusterApps ? 0 : confClusterApps.size());
        infoClusterMapper.insertSelective(infoCluster);
    }

    @Override
    public SheinResponseModel descClusterReleaseLabel(String id, String releaseLabel) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        ConfCluster confCluster=null;
        ConfClusterApp confClusterApp = new ConfClusterApp();

        if (StringUtils.isEmpty(id)&&StringUtils.isEmpty(releaseLabel)){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少查询条件。");
            return sheinResponseModel;
        }

        if (StringUtils.isNotEmpty(id)) {
            confClusterApp.setClusterId(id);
            confCluster = confClusterMapper.selectByPrimaryKey(id);
            if (confCluster == null) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("集群不存在。");
                return sheinResponseModel;
            }
            if (!confCluster.getClusterReleaseVer().equalsIgnoreCase(releaseLabel) && StringUtils.isNotEmpty(releaseLabel)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("查询条件错误。");
                return sheinResponseModel;
            }
            try{
                List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(confClusterApp);
                if(null!=confClusterApps && confClusterApps.size()>0){
                    Map<String,Object> dataMap = new HashMap<>();
                    Map<String,String> mapList = new HashMap<>();
                    for (ConfClusterApp cca : confClusterApps){
                        mapList.put(cca.getAppName(),cca.getAppVersion());
                    }
                    dataMap.put("clusterApps",mapList);
                    dataMap.put("ReleaseLabel",confCluster.getClusterReleaseVer());
                    sheinResponseModel.setInfo(dataMap);
                }
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
                sheinResponseModel.setMsg("success");
            }catch (Exception e){
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                sheinResponseModel.setMsg("error:"+ ExceptionUtils.getStackTrace(e));
                getLogger().error("descClusterReleaseLabel,error:",e);
            }
        }else{
            Map<String,String> paramap=new HashMap<>();
            paramap.put("releaseLabelPrefix",releaseLabel);
            List<BaseReleaseApps> baseReleaseAppsList=baseReleaseAppsMapper.selectByObject(paramap);
            Map<String,Object> dataMap = new HashMap<>();
            Map<String,String> mapList = new HashMap<>();
            baseReleaseAppsList.stream().forEach(x->{
                mapList.put(x.getAppName(),x.getAppVerison());
            });
            if (mapList.isEmpty()){
                sheinResponseModel.setInfo(dataMap);
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无效参数。");
                return sheinResponseModel;
            }
            dataMap.put("App",mapList);
            dataMap.put("ReleaseLabel",releaseLabel);
            sheinResponseModel.setInfo(dataMap);
            sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            sheinResponseModel.setMsg("success");
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel listAvailableClusters(String dc) {
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);

        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        Map params = new HashMap();
        params.put("region", dc);
        params.put("emrStatus", Lists.newArrayList(ConfCluster.CREATING, ConfCluster.CREATED));
        params.put("ambariHost", "1");
        try {
            List<Map> confClusters = confClusterMapper.selectByObject(params);
            if (null != confClusters && confClusters.size() > 0) {
                List<Map<String, Object>> mapList = new ArrayList<>();
                for (Map map : confClusters) {
                    Map<String, Object> listMap = new HashMap<>();
                    listMap.put("clusterId", map.get("cluster_id"));
                    listMap.put("clusterName", map.get("cluster_name"));
                    listMap.put("clusterReleaseVer", map.get("cluster_release_ver"));
                    listMap.put("state", map.get("state"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (map.containsKey("cluster_create_begtime")) {
                        listMap.put("createTime", sdf.format(map.get("cluster_create_begtime")));
                    } else {
                        listMap.put("createTime", "");
                    }
                    if (map.containsKey("cluster_create_endtime")) {
                        listMap.put("readyTime", sdf.format(map.get("cluster_create_endtime")));
                    } else {
                        listMap.put("readyTime", "");
                    }
                    mapList.add(listMap);
                }
                sheinResponseModel.setInfo(mapList);
                sheinResponseModel.setCode("200");
                sheinResponseModel.setMsg("success");
            } else {
                sheinResponseModel.setCode("404");
                sheinResponseModel.setMsg("未查到有效数据");
            }
        } catch (Exception e) {
            sheinResponseModel.setCode("-1");
            sheinResponseModel.setMsg("error:" + e.getMessage());
            getLogger().error("listAvailableClusters,error",e);
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel listAvailableClustersNew(String dc) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        Map<String, Map<String, Object>> responseData = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("region", dc);
        params.put("emrStatus", Lists.newArrayList(ConfCluster.CREATING, ConfCluster.CREATED));

        try {
            List<Map<String, Object>> confClusters = confClusterMapper.selectByObjectInnerICV(params);
            if (!CollectionUtils.isEmpty(confClusters)) {
                List<String> clusterIds = new ArrayList<>();
                for (Map<String, Object> confCluster : confClusters) {
                    clusterIds.add(confCluster.get("cluster_id").toString());

                    Map<String, Object> responseDataMap = new HashMap<>();
                    responseDataMap.put("clusterId", confCluster.get("cluster_id"));
                    responseDataMap.put("clusterName", confCluster.get("cluster_name"));
                    responseDataMap.put("clusterReleaseVer", confCluster.get("cluster_release_ver"));
                    responseDataMap.put("state", confCluster.get("state"));
                    responseDataMap.put("dc", RegionMappingUtil.mappingOut((String)confCluster.get("region")));
                    responseDataMap.put("createTime", "");
                    responseDataMap.put("readyTime", "");
                    responseData.put(confCluster.get("cluster_id").toString(), responseDataMap);
                }

                List<Map<String, Object>> infoClusters = infoClusterMapper.selectByClusterIds(clusterIds);
                if (!CollectionUtils.isEmpty(infoClusters)) {
                    for (Map<String, Object> infoCluster : infoClusters) {
                        String clusterId = infoCluster.get("cluster_id").toString();
                        Object createBegTime = infoCluster.get("cluster_create_begtime");
                        Object createEndTime = infoCluster.get("cluster_create_endtime");

                        Map<String, Object> responseDataMap = responseData.get(clusterId);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (createBegTime != null) {
                            responseDataMap.put("createTime", sdf.format(createBegTime));
                        }
                        if (createEndTime != null) {
                            responseDataMap.put("readyTime", sdf.format(createEndTime));
                        }
                    }
                }
                sheinResponseModel.setInfo(responseData.values());
                sheinResponseModel.setCode("200");
                sheinResponseModel.setMsg("success");
            } else {
                sheinResponseModel.setCode("404");
                sheinResponseModel.setMsg("未查到有效数据");
            }
        } catch (Exception e) {
            sheinResponseModel.setCode("-1");
            sheinResponseModel.setMsg("error:" + e.getMessage());
            getLogger().error("listAvailableClustersNew,error",e);
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel listClusters(String stateList, String begtime, String endtime, String dc) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 参数不可空
        if (StringUtils.isEmpty(stateList) && StringUtils.isEmpty(begtime)
                && StringUtils.isEmpty(endtime) && StringUtils.isEmpty(dc)){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少查询条件。");
            return sheinResponseModel;
        }
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);

        //校验时间格式
        ResultMsg cb=checkParam.checkDateTimeFormat(begtime,"yyyy-MM-dd HH:mm:ss");
        if(!cb.getResult()){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg(cb.getErrorMsg());
            return sheinResponseModel;
        }

        ResultMsg ce=checkParam.checkDateTimeFormat(endtime,"yyyy-MM-dd HH:mm:ss");
        if (!ce.getResult()){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg(ce.getErrorMsg());
            return sheinResponseModel;
        }

        // 校验时间合法性
        ResultMsg be=checkParam.checkBEDateTimeValid(begtime,endtime,"yyyy-MM-dd HH:mm:ss");
        if (!be.getResult()){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg(be.getErrorMsg());
            return sheinResponseModel;
        }


        Map params = new HashMap();
        params.put("region",dc);
        try{
            List<Integer> states = new ArrayList<>();
            try {

                if (StringUtils.isNotEmpty(stateList)) {
                    if (!stateList.contains(",")) {
                        states.add(Integer.parseInt(stateList));
                    } else {
                        String[] state = stateList.split(",");
                        List<String> stateStrList = Arrays.asList(state);
                        stateStrList.forEach(stateStr -> {
                            states.add(Integer.parseInt(stateStr));
                        });
                    }
                }
            }catch (Exception e){
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("参数异常，参数范围：0，1，2，-1，-2");
                return sheinResponseModel;
            }

            params.put("emrStatus",states.size()>0 ? states : null);
            params.put("atfer",StringUtils.isNotEmpty(begtime) ? begtime : null);
            params.put("before",StringUtils.isNotEmpty(endtime) ? endtime : null);
            List<Map> confClusters = confClusterMapper.selectByObject(params);
            if(null!=confClusters && confClusters.size()>0){
                List<Map<String,Object>> mapList = new ArrayList<>();
                for (Map map : confClusters){
                    Map<String,Object> listMap = new HashMap<>();
                    listMap.put("clusterId",map.get("cluster_id"));
                    listMap.put("clusterName",map.get("cluster_name"));
                    listMap.put("clusterReleaseVer",map.get("cluster_release_ver"));
                    listMap.put("state",map.get("state"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if(map.containsKey("cluster_create_begtime")){
                        listMap.put("createTime",sdf.format(map.get("cluster_create_begtime")));
                    }else {
                        listMap.put("createTime","");
                    }
                    if(map.containsKey("cluster_create_endtime")){
                        listMap.put("readyTime",sdf.format(map.get("cluster_create_endtime")));
                    }else {
                        listMap.put("readyTime","");
                    }
                    if((map.get("state").toString().equalsIgnoreCase("2")
                            ||map.get("state").toString().equalsIgnoreCase("-2"))
                            && map.get("cluster_create_begtime")!=null
                            && StringUtils.isNotEmpty(map.get("cluster_create_begtime").toString())
                            && map.get("cluster_create_endtime")!=null
                            && StringUtils.isNotEmpty(map.get("cluster_create_endtime").toString())){
                        Long dr=(sdf.parse(map.get("cluster_create_endtime").toString()).getTime()
                                -sdf.parse(map.get("cluster_create_begtime").toString()).getTime())/1000;
                        listMap.put("create_Duration",dr);
                    }

                    // 集群删除 新增删除结束事件
                    if(map.get("state").toString().equalsIgnoreCase("-2")){
                        InfoClusterOperationPlan infoClusterOperationPlan=new InfoClusterOperationPlan();
                        infoClusterOperationPlan.setClusterId(map.get("cluster_id").toString());
                        infoClusterOperationPlan.setOperationType("delete");
                       List<InfoClusterOperationPlan> plan=infoClusterOperationPlanMapper
                               .selectByObject(infoClusterOperationPlan);
                       if (plan==null || plan.size()==0 ||plan.get(0).getBegTime()==null || plan.get(0).getEndTime()==null){
                           // donothing
                       }else {
                           listMap.put("deleteBegTime", sdf.format(plan.get(0).getBegTime()));
                           listMap.put("deleteEndTime", sdf.format(plan.get(0).getEndTime()));
                       }
                    }
                    mapList.add(listMap);
                }
                sheinResponseModel.setInfo(mapList);
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
                sheinResponseModel.setMsg("success");
            } else {
                sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
                sheinResponseModel.setMsg("未查到有效数据");
            }
        }catch (Exception e){
            getLogger().error("listClusters,error",e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("查询异常。"+e.getMessage());
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel listClusterReleaseLabels(String releaseLabelPrefix, String appVersion, String appName) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        try {
            List<BaseReleaseApps> baseReleaseApps = baseReleaseAppsMapper.selectClusterReleaseLabels(releaseLabelPrefix,
                    appVersion, appName);
            Map<String,List<BaseReleaseApps>> map=baseReleaseApps.stream().collect(Collectors.groupingBy(item->{
                return item.getReleaseVersion();
            }));
            List<String> infos=new ArrayList<>();
            map.entrySet().stream().forEach(item->{
                infos.add(item.getKey());
            });
            if (CollectionUtils.isEmpty(infos)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
                sheinResponseModel.setMsg("未查到有效数据");
            } else {
                sheinResponseModel.setInfo(infos);
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
                sheinResponseModel.setMsg("success");
            }
        }catch (Exception e){
            sheinResponseModel.setCode("-1");
            sheinResponseModel.setMsg("error:"+e.getMessage());
            getLogger().error("listClusterReleaseLabels,error:",e);
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel listClusterGroupInstances(String id, String insGpId, String insGpTypes, String insStatus, String dc) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (StringUtils.isBlank(id) || StringUtils.isBlank(dc)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少必要参数：id、dc");
            return sheinResponseModel;
        }

        if (StringUtils.isEmpty(insGpId) && StringUtils.isEmpty(insGpTypes)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少必要参数：insGpID或insGpTypes");
            return sheinResponseModel;
        }

        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", id);
        clusterParams.put("region", dc);
        List<Map> confClusters = confClusterMapper.selectByObject(clusterParams);
        if (confClusters == null || confClusters.size() == 0) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
            return sheinResponseModel;
        }

        Integer state = null;
        if (StringUtils.isNotBlank(insStatus)) {
            try {
                state = Integer.valueOf(insStatus);
            } catch (NumberFormatException e) {
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                sheinResponseModel.setMsg("insStatus格式不正确");
                return sheinResponseModel;
            }
        }

        // insGpTypes 支持多选
        Set<String> vmRoleSet = new HashSet<>();
        if (StringUtils.isNotBlank(insGpTypes)) {
            String[] vmRoleArr = insGpTypes.split(",");
            for (String vmRole : vmRoleArr) {
                vmRoleSet.add(vmRole.toLowerCase());
            }
        }
        if (vmRoleSet.contains("master")) {
            vmRoleSet.add("ambari");
        }

        try {
            // 查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("state", state);
            params.put("vmRoles", vmRoleSet);
            params.put("groupId", insGpId);
            List<Map> mapsNotScaleout = infoClusterVmMapper.selectByObjectNotScaleoutTask(params);

            params.put("scalingTaskState", 2);
            List<Map> mapsHaveScaleout = infoClusterVmMapper.selectByObjectInnerScalingTask(params);

            List<Map> maps = new ArrayList<>();
            maps.addAll(mapsNotScaleout);
            maps.addAll(mapsHaveScaleout);

            List<Map> infoClusterVms = new ArrayList<>();
            if (null != maps && maps.size() > 0) {
                for (Map icm : maps) {
                    List<Map> confClusterVmList = confClusterVmNeoMapper.selectByObject(params);
                    String vmConfId = "";
                    Map param = new HashMap();
                    param.put("insGpId", icm.containsKey("group_id") ? icm.get("group_id") : "");
                    param.put("vmName", icm.containsKey("vm_name") ? icm.get("vm_name") : "");
                    param.put("hostName", icm.containsKey("host_name") ? icm.get("host_name") : "");
                    param.put("internalIp", icm.containsKey("internalIp") ? icm.get("internalIp") : "");
                    param.put("defaultUsername", icm.containsKey("default_username") ? icm.get("default_username") : "");
                    if (null != confClusterVmList && confClusterVmList.size() > 0) {
                        Map confClusterVmMap = confClusterVmList.get(0);
                        vmConfId = confClusterVmMap.containsKey("vm_conf_id") ? confClusterVmMap.get("vm_conf_id").toString() : "";
                        param.put("osVersion", confClusterVmMap.containsKey("os_version") ? confClusterVmMap.get("os_version") : "");
                        param.put("osVolumeType", confClusterVmMap.containsKey("os_volume_type") ? confClusterVmMap.get("os_volume_type") : "");
                        // param.put("osVolumeSizeGB",confClusterVmMap.containsKey("os_volume_size") ? confClusterVmMap.get("os_volume_size") : "");
                    }
                    param.put("skuName", icm.containsKey("sku_name") ? icm.get("sku_name") : "");
                    param.put("purchaseType", 1);
                    List<Map> confClusterVmDataVolumes = new ArrayList<>();
                    if (StringUtils.isNotEmpty(vmConfId)) {
                        List<ConfClusterVmDataVolume> confClusterVmDataVolumeList = confClusterVmDataVolumeMapper.selectByVmConfId(vmConfId);
                        if (CollUtil.isNotEmpty(confClusterVmDataVolumeList)) {
                            for (ConfClusterVmDataVolume dataVolume : confClusterVmDataVolumeList) {
                                Map ccvdvMap = new HashMap();
                                ccvdvMap.put("volumeType", dataVolume.getDataVolumeType());
                                ccvdvMap.put("volumeSizeGB", dataVolume.getDataVolumeSize());
                                confClusterVmDataVolumes.add(ccvdvMap);
                            }
                        }
                    }
                    // param.put("dataVols",confClusterVmDataVolumes);
                    String clusterId = icm.get("cluster_id").toString();
                    String vmName = icm.get("vm_name").toString();
                    param.put("vmId",icm.get("vmid").toString());
                    param.put("vmRole", icm.containsKey("vm_role") ? icm.get("vm_role") : "");
                    param.put("state", icm.containsKey("state") ? icm.get("state") : "");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Object createBegTime = icm.get("create_begtime");
                    Object createEndTime = icm.get("create_endtime");
                    try {
                        Date begTime = createBegTime instanceof Date? (Date)createBegTime: null;
                        if (createBegTime == null || !(createBegTime instanceof Date)) {
                            getLogger().warn("createBegTime日期格式不正确: {}", createBegTime);
                        }
                        Date endTime = createEndTime instanceof Date? (Date)createEndTime: null;
                        if (createEndTime == null || !(createEndTime instanceof Date)) {
                            getLogger().warn("createEndTime日期格式不正确: {}", createEndTime);
                        }
                        param.put("createTime", Objects.nonNull(begTime) ? sdf.format(begTime) : "");
                        param.put("readyTime", Objects.nonNull(endTime) ? sdf.format(endTime) : "");
                    } catch (Exception ex) {
                        getLogger().error(ex.getMessage(), ex);
                    }
                    infoClusterVms.add(param);
                }
                sheinResponseModel.setInfo(infoClusterVms);
                sheinResponseModel.setMsg("success");
                sheinResponseModel.setCode("200");
            } else {
                sheinResponseModel.setInfo(infoClusterVms);
                sheinResponseModel.setMsg("未查到有效数据。");
                sheinResponseModel.setCode("404");
            }
        } catch (Exception e) {
            sheinResponseModel.setCode("-1");
            sheinResponseModel.setMsg("error:" + e.getMessage());
            getLogger().error("listClusterGroupInstances,error:",e);
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel terminateCluster(String dc, String id) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(id);
        if(null==confCluster){
            sheinResponseModel.setMsg("the query result is empty, clusterId:"+id);
            sheinResponseModel.setCode("-1");
            return sheinResponseModel;
        }
        ResultMsg resultMsg = this.clusterOperation(confCluster.getClusterId(),"delete",confCluster.getClusterReleaseVer());
        if(resultMsg.getResult()){
            sheinResponseModel.setMsg("success");
            sheinResponseModel.setCode("200");
        }else {
            sheinResponseModel.setMsg(resultMsg.getErrorMsg());
            sheinResponseModel.setCode("-1");
        }
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel createCluster(String jsonStr) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        // 集群UUID
        String clusterId = UUID.randomUUID().toString();
        // 判断请求参数内容是否为空
        if (StringUtils.isEmpty(jsonStr)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("error:the request data is empty.");
            return sheinResponseModel;
        }
        getLogger().info("SheinApiServiceImpl.createCluster, jsonStr: " + jsonStr);

        // 限流
//        boolean acquired = createClusterRateLimiter.tryAcquire();
//        if (!acquired) {
//            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
//            sheinResponseModel.setMsg("创建集群请求过于频繁，请稍后再试");
//            return sheinResponseModel;
//        }

        String lockkey = "";
        try {
            // 解析并反射参数到对象
            SheinRequestModel sheinRequestModel = null;
            try {
                sheinRequestModel = JSON.parseObject(jsonStr, SheinRequestModel.class, Feature.SupportAutoType);
            } catch (Exception e) {
                getLogger().error("提交参数格式异常：", e);
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("提交参数格式异常");
                return sheinResponseModel;
            }

            Long begtime = System.currentTimeMillis();
            String clusterName = sheinRequestModel.getClusterName();
            logProcessTime(clusterName, "begin", begtime);

            lockkey = "create_lockKey_" + sheinRequestModel.getClusterName();
            boolean lock = redisLock.tryLock(lockkey);
            if (!lock) {
                sheinResponseModel.setMsg("请勿重复提交数据");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }
            if (Objects.isNull(sheinRequestModel.getIsEmbedAmbariDb())) {
                // 默认不使用内置数据库
                sheinRequestModel.setIsEmbedAmbariDb(0);
            }

            logProcessTime(clusterName, "lock", begtime);
            ResultMsg ckmsg = checkParam.checkSheinCreateParam(sheinRequestModel);

            // sheinMaster与Ambari概念及参数转换（待开启测试）
            // sheinCreateClusterMaster2Ambari(sheinRequestModel);

            logProcessTime(clusterName, "checkparam", begtime);
            if (!ckmsg.getResult()) {
                sheinResponseModel.setMsg(ckmsg.getErrorMsg());
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                return sheinResponseModel;
            }

            // 查询cluster是否已存在，不允许存在
            if (checkIsExistByClusterId(sheinRequestModel.getClusterName())) {
                sheinResponseModel.setMsg("已经存在的集群名称");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }

            if (!checkClusterNameUsable(clusterName)) {
                getLogger().error("创建集群失败：已经存在的集群名称[" + clusterName + "],删除时间不足3600秒");
                sheinResponseModel.setMsg("集群名称还不能使用");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }

            logProcessTime(clusterName, "uniquecheck", begtime);
            fillMIExtInfo(sheinRequestModel);
            logProcessTime(clusterName, "fillMIExtInfo", begtime);

            this.addConfCluster(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addConfCluster", begtime);

            this.addClusterApps(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addClusterApps", begtime);

            this.addConfClusterAppsConfig(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addConfClusterAppsConfig", begtime);

            this.addConfClusterHostGroupAppsConfig(clusterId, sheinRequestModel);

            this.addConfClusterVmAndDataVolume(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addConfClusterVmAndDataVolume", begtime);

            this.addConfClusterTag(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addConfClusterTag", begtime);

            this.addConfClusterScript(clusterId, sheinRequestModel);
            logProcessTime(clusterName, "addConfClusterScript", begtime);

            this.addInfoCluster(clusterId);
            logProcessTime(clusterName, "addInfoCluster", begtime);

            ResultMsg resultMsg = this.clusterOperation(clusterId, "create",
                    sheinRequestModel.getClusterReleaseVer());
            logProcessTime(clusterName, "clusterOperation", begtime);

            if (resultMsg.getResult()) {
                Map<String, Object> result = new HashMap<>();
                result.put("clusterId", clusterId);
                sheinResponseModel.setInfo(result);
                sheinResponseModel.setMsg("success");
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            } else {
                sheinResponseModel.setMsg(resultMsg.getErrorMsg());
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            }
            return sheinResponseModel;
        } catch (Exception e) {
            sheinResponseModel.setMsg("error:" + e.getMessage());
            getLogger().error("create_error:", e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
        } finally {
            try {
                if (redisLock.isLocked(lockkey)) {
                    redisLock.unlock(lockkey);
                }
            } catch (Exception e) {
                getLogger().error("锁释放异常，", e);
            }
        }

        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel createClusterNew(String jsonStr) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (StringUtils.isEmpty(jsonStr)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("error: the request data is empty.");
            return sheinResponseModel;
        }
        getLogger().info("SheinApiServiceImpl.createCluster, jsonStr: " + jsonStr);

        // 限流
//        boolean acquired = createClusterRateLimiter.tryAcquire();
//        if (!acquired) {
//            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
//            sheinResponseModel.setMsg("创建集群请求过于频繁，请稍后再试");
//            return sheinResponseModel;
//        }

        String clusterId = UUID.randomUUID().toString();
        String lockkey = "";
        try {
            SheinRequestModel sheinRequestModel = null;
            try {
                sheinRequestModel = JSON.parseObject(jsonStr, SheinRequestModel.class, Feature.SupportAutoType);
                //入参映射
                sheinRequestModel.setDc(RegionMappingUtil.mappingIn(sheinRequestModel.getDc()));

                String logicalZone = mappingAzToPhysicalZone(sheinRequestModel.getDc(), sheinRequestModel.getAz());
                getLogger().info("将接口请求的物理Zone转换为逻辑Zone: reqAz={}, logicalZoneNo={}", sheinRequestModel.getAz(), logicalZone);
                sheinRequestModel.setAz(logicalZone);
            } catch (Exception e) {
                getLogger().error("SheinApiServiceImpl.createCluster 参数格式异常: ", e);
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("提交参数格式异常");
                return sheinResponseModel;
            }

            Long begTime = System.currentTimeMillis();
            String clusterName = sheinRequestModel.getClusterName();
            logProcessTime(clusterName, "begin", begTime);

            lockkey = "create_lockKey_" + sheinRequestModel.getClusterName();
            boolean lock = redisLock.tryLock(lockkey);
            if (!lock) {
                sheinResponseModel.setMsg("请勿重复提交数据");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }

            // 默认不使用内置数据库
            if (Objects.isNull(sheinRequestModel.getIsEmbedAmbariDb())) {
                sheinRequestModel.setIsEmbedAmbariDb(0);
            }

            logProcessTime(clusterName, "lock", begTime);
            ResultMsg ckMsg = checkParam.checkSheinCreateParam(sheinRequestModel);
            logProcessTime(clusterName, "checkParam", begTime);
            if (!ckMsg.getResult()) {
                sheinResponseModel.setMsg(ckMsg.getErrorMsg());
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                return sheinResponseModel;
            }

            // sheinMaster与Ambari概念及参数转换（待开启测试）
            // sheinCreateClusterMaster2Ambari(sheinRequestModel);

            // 查询cluster是否已存在，不允许存在
            if (checkIsExistByClusterId(sheinRequestModel.getClusterName())) {
                sheinResponseModel.setMsg("已经存在的集群名称");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }

            if (!checkClusterNameUsable(clusterName)) {
                getLogger().error("创建集群失败：已经存在的集群名称[" + clusterName + "],删除时间不足3600秒");
                sheinResponseModel.setMsg("集群名称还不能使用");
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                return sheinResponseModel;
            }

            logProcessTime(clusterName, "uniqueCheck", begTime);

            fillMIExtInfo(sheinRequestModel);
            logProcessTime(clusterName, "fillMIExtInfo", begTime);

            AdminSaveClusterRequest saveClusterRequest = persistenceDataConversion(sheinRequestModel);
            getLogger().info("SheinApiServiceImpl.createCluster persistenceDataConversion saveClusterRequest: " + JSON.toJSONString(saveClusterRequest));
            ConfCluster.CreationMode creationMode = saveClusterRequest.getCreationMode(ConfCluster.CreationMode.DIRECTLY);
            saveClusterRequest.setCreationMode(creationMode.getValue());
            ClusterCreationStrategy clusterCreationStrategy = clusterCreationStrategyFactory.create(creationMode);
            ResultMsg resultMsg = clusterCreationStrategy.createCluster(clusterId, saveClusterRequest);

            logProcessTime(clusterName, "clusterOperation", begTime);
            if (resultMsg.getResult()) {
                Map<String, Object> result = new HashMap<>();
                result.put("clusterId", clusterId);
                sheinResponseModel.setInfo(result);
                sheinResponseModel.setMsg("success");
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            } else {
                sheinResponseModel.setMsg(resultMsg.getErrorMsg());
                sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            }
            return sheinResponseModel;
        } catch (Exception e) {
            getLogger().error("SheinApiServiceImpl.createCluster create_error: ", e);
            sheinResponseModel.setMsg("error: " + e.getMessage());
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
        } finally {
            try {
                if (redisLock.isLocked(lockkey)) {
                    redisLock.unlock(lockkey);
                }
            } catch (Exception e) {
                getLogger().error("SheinApiServiceImpl.createCluster 锁释放异常: ", e);
            }
        }
        return sheinResponseModel;
    }

    private String mappingAzToPhysicalZone(String region, String azNo) {
        AvailabilityZone az = metaDataItemService.getAZByPhysicalZone(region, azNo);
        if (Objects.isNull(az)) {
            throw new RuntimeException("将提交的AZ转换为物理Zone时,未在元数据中找到物理Zone: region=" + region
            + ", physicalAzNo=" + azNo);
        }
        return az.getLogicalZone();
    }

    /**
     * 创建集群参数持久化前转换
     */
    private AdminSaveClusterRequest persistenceDataConversion(SheinRequestModel sheinRequestModel) {
        AdminSaveClusterRequest saveClusterRequest = new AdminSaveClusterRequest();
        dataConversionAddConfCluster(sheinRequestModel, saveClusterRequest);
        dataConversionAddConfClusterTag(sheinRequestModel, saveClusterRequest);
        dataConversionAddConfClusterApp(sheinRequestModel, saveClusterRequest);
        dataConversionAddConfClusterAppsConfig(sheinRequestModel, saveClusterRequest);
        dataConversionAddConfClusterHostGroupAppsConfig(sheinRequestModel, saveClusterRequest);
        dataConversionAddConfClusterScript(sheinRequestModel, saveClusterRequest);
        return saveClusterRequest;
    }

    private void dataConversionAddConfCluster(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        saveClusterRequest.setClusterName(sheinRequestModel.getClusterName());
        saveClusterRequest.setRegion(sheinRequestModel.getDc());
        saveClusterRequest.setUserName(sheinRequestModel.getCreateUser());
        saveClusterRequest.setIsHa(sheinRequestModel.getStartHa());
        saveClusterRequest.setDeleteProtected(StringUtils.isNotBlank(sheinRequestModel.getDeleteProtected()) ? sheinRequestModel.getDeleteProtected() : "0");

        if (sheinRequestModel.getCreationMode() != null && sheinRequestModel.getCreationMode() == 2) {
            saveClusterRequest.setCreationMode(ConfCluster.CreationMode.SPLIT.getValue());
        } else {
            saveClusterRequest.setCreationMode(ConfCluster.CreationMode.DIRECTLY.getValue());
        }

        HiveMetadataDbCfg hiveMetadataDbCfg = new HiveMetadataDbCfg();
        if (sheinRequestModel.getHiveMetadataDbCfgs() != null) {
            hiveMetadataDbCfg.seturl(sheinRequestModel.getHiveMetadataDbCfgs().getUrl());
            hiveMetadataDbCfg.setDatabase(sheinRequestModel.getHiveMetadataDbCfgs().getDatabase());
            if (sheinRequestModel.getHiveMetadataDbCfgs().getPort() == null) {
                hiveMetadataDbCfg.setPort("3306");
            } else {
                hiveMetadataDbCfg.setPort(sheinRequestModel.getHiveMetadataDbCfgs().getPort().toString());
            }
        }
        saveClusterRequest.setHiveMetadataDbCfgs(hiveMetadataDbCfg);

        saveClusterRequest.setLogPath(sheinRequestModel.getS3LogLocation());
        saveClusterRequest.setKeypairId(sheinRequestModel.getInstanceKeyPair());
        saveClusterRequest.setMasterSecurityGroup(sheinRequestModel.getMasterSecurityGroup());
        saveClusterRequest.setSlaveSecurityGroup(sheinRequestModel.getSlaveSecurityGroup());
        saveClusterRequest.setSubNet(sheinRequestModel.getSubnet());
        saveClusterRequest.setVNet(sheinRequestModel.getvNet());
        saveClusterRequest.setVmMI(sheinRequestModel.getVmMI());
        saveClusterRequest.setLogMI(sheinRequestModel.getLogMI());
        saveClusterRequest.setVmMIClientId(sheinRequestModel.getVmMIClientId());
        saveClusterRequest.setVmMITenantId(sheinRequestModel.getVmMITenantId());
        saveClusterRequest.setLogMIClientId(sheinRequestModel.getLogMIClientId());
        saveClusterRequest.setLogMITenantId(sheinRequestModel.getLogMITenantId());

        InstanceGroupVersion instanceGroupVersion = new InstanceGroupVersion();
        instanceGroupVersion.setClusterReleaseVer(sheinRequestModel.getClusterReleaseVer());
        saveClusterRequest.setInstanceGroupVersion(instanceGroupVersion);

        saveClusterRequest.setScene(sheinRequestModel.getScene());
        saveClusterRequest.setZone(sheinRequestModel.getAz());
        saveClusterRequest.setZoneName(getZoneNameByZoneNumber(sheinRequestModel.getAz(),sheinRequestModel.getDc()));
        saveClusterRequest.setEnableGanglia(sheinRequestModel.getEnableGanglia());
        saveClusterRequest.setIsEmbedAmbariDb(sheinRequestModel.getIsEmbedAmbariDb());
        saveClusterRequest.setIswhiteAddr(sheinRequestModel.getInWhiteList());

        // 判断是否使用内嵌数据库
        if (!Objects.equals(sheinRequestModel.getIsEmbedAmbariDb(), 1)) {
            if (sheinRequestModel.getAmbariDbCfgs() != null) {
                AmbariDbCfg ambariDbCfg = new AmbariDbCfg();
                ambariDbCfg.seturl(sheinRequestModel.getAmbariDbCfgs().getUrl());
                ambariDbCfg.setDatabase(sheinRequestModel.getAmbariDbCfgs().getDatabase());
                if (sheinRequestModel.getAmbariDbCfgs().getPort() == null) {
                    ambariDbCfg.setPort("3306");
                } else {
                    ambariDbCfg.setPort(sheinRequestModel.getAmbariDbCfgs().getPort().toString());
                }
                saveClusterRequest.setAmbariDbCfgs(ambariDbCfg);
            }
        }

        // hivemetadata数据库账号密码 写入 keyvault
        // ambari数据库账号密码 写入 keyvault
        saveKeyVault(sheinRequestModel);
    }

    private void dataConversionAddConfClusterTag(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        saveClusterRequest.setTagMap(sheinRequestModel.getTagMap());
    }

    private void dataConversionAddConfClusterApp(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        List<String> clusterApps = sheinRequestModel.getClusterApps();
        // 去重操作，保证唯一性
        Set<String> appNameSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(clusterApps)) {
            appNameSet.addAll(clusterApps);
        }

        // 查询必选appName
        Set<String> appNameNVSet = new HashSet<>();
        if (StringUtils.isNotEmpty(sheinRequestModel.getScene())) {
            BaseScene baseScene = baseSceneMapper.queryByReleaseVerAndSceneName(sheinRequestModel.getClusterReleaseVer(), sheinRequestModel.getScene());
            if (baseScene == null) {
                return;
            }
            List<BaseSceneApps> baseSceneApps = baseSceneAppsMapper.queryBySceneId(baseScene.getSceneId());
            // 筛选必选项
            List<BaseSceneApps> requiredApps = baseSceneApps.stream().filter(x -> x.getRequired().equals(1)).collect(Collectors.toList());
            for (BaseSceneApps requiredApp : requiredApps) {
                appNameNVSet.add(requiredApp.getAppName());
            }
        }

        // 补充必选appName
        Set<String> appNameCapitalSet = new HashSet<>();
        for (String appName : appNameSet) {
            appNameCapitalSet.add(appName.toUpperCase());
        }
        Set<String> appNameNVCapitalSet = new HashSet<>();
        for (String appName : appNameNVSet) {
            appNameNVCapitalSet.add(appName.toUpperCase());
        }
        appNameCapitalSet.addAll(appNameNVCapitalSet);

        // 数据过滤-与baseReleaseApps保持一致
        List<ClusterApp> clusterAppList = new ArrayList<>();
        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("releaseLabelPrefix", sheinRequestModel.getClusterReleaseVer());
        List<BaseReleaseApps> baseReleaseApps = baseReleaseAppsMapper.selectByObject(selectParams);
        for (BaseReleaseApps baseReleaseApp : baseReleaseApps) {
            String appName = baseReleaseApp.getAppName();
            if (appNameCapitalSet.contains(appName.toUpperCase())) {
                ClusterApp clusterApp = new ClusterApp();
                clusterApp.setAppName(appName);
                clusterApp.setAppVersion(baseReleaseApp.getAppVerison());
                clusterAppList.add(clusterApp);
            }
        }

        InstanceGroupVersion instanceGroupVersion = new InstanceGroupVersion();
        instanceGroupVersion.setClusterReleaseVer(sheinRequestModel.getClusterReleaseVer());
        instanceGroupVersion.setClusterApps(clusterAppList);
        saveClusterRequest.setInstanceGroupVersion(instanceGroupVersion);
    }

    private void dataConversionAddConfClusterAppsConfig(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        List<ClusterCfg> clusterCfgList = new ArrayList<>();
        List<Empty> sheinClusterCfgs = sheinRequestModel.getClusterCfgs();
        if (!CollectionUtils.isEmpty(sheinClusterCfgs)) {
            for (Empty sheinClusterCfg : sheinClusterCfgs) {
                ClusterCfg clusterCfg = new ClusterCfg();
                clusterCfg.setClassification(sheinClusterCfg.getClassification());
                clusterCfg.setcfg(JSON.parseObject(JSON.toJSONString(sheinClusterCfg.getcfg())));
                clusterCfgList.add(clusterCfg);
            }
        }
        saveClusterRequest.setClusterCfgs(clusterCfgList);
    }

    private void dataConversionAddConfClusterHostGroupAppsConfig(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        List<InstanceGroupSkuCfg> instanceGroupSkuCfgList = new ArrayList<>();
        List<InstanceGroupNewConfigElement> instanceGroupNewConfigs = sheinRequestModel.getInstanceGroupNewConfigs();

        for (InstanceGroupNewConfigElement instanceGroupNewConfig : instanceGroupNewConfigs) {
            // ConfClusterHostGroup
            InstanceGroupAddConfig instanceGroupAddConfig = instanceGroupNewConfig.getInstanceGroupAddConfig();
            InstanceGroupSkuCfg instanceGroupSkuCfg = new InstanceGroupSkuCfg();
            instanceGroupSkuCfg.setVmRole(instanceGroupAddConfig.getInsGpRole());
            instanceGroupSkuCfg.setGroupName(instanceGroupAddConfig.getInsGpName());
            instanceGroupSkuCfg.setCnt(instanceGroupAddConfig.getInsGpCnt());
            if (instanceGroupAddConfig.getInsMktType().equalsIgnoreCase("spot")) {
                instanceGroupSkuCfg.setPurchaseType(2);
            } else {
                instanceGroupSkuCfg.setPurchaseType(1);
            }
            instanceGroupSkuCfg.setEnableBeforestartScript(1);
            instanceGroupSkuCfg.setEnableAfterstartScript(1);

            // dataConversionAddConfClusterVmAndDataVolume
            dataConversionAddConfClusterVmAndDataVolume(sheinRequestModel, instanceGroupAddConfig, instanceGroupSkuCfg);

            // dataConversionAddElasticScaleRules
            dataConversionAddElasticScaleRules(instanceGroupAddConfig.getScalingRules(), instanceGroupSkuCfg);

            // ConfClusterHostGroupAppsConfig
            List<InstanceGroupCfg> instanceGroupCfgs = instanceGroupNewConfig.getInstanceGroupCfgs();
            if (!CollectionUtils.isEmpty(instanceGroupCfgs)) {
                List<ClusterCfg> clusterCfgList = new ArrayList<>();
                for (InstanceGroupCfg instanceGroupCfg : instanceGroupCfgs) {
                    ClusterCfg clusterCfg = new ClusterCfg();
                    String classification = instanceGroupCfg.getClassification();
                    Map<String, Object> sheinGpCfg = instanceGroupCfg.getcfg();
                    clusterCfg.setClassification(classification);
                    clusterCfg.setcfg(JSON.parseObject(JSON.toJSONString(sheinGpCfg)));
                    clusterCfgList.add(clusterCfg);
                }
                instanceGroupSkuCfg.setGroupCfgs(clusterCfgList);
            }
            instanceGroupSkuCfgList.add(instanceGroupSkuCfg);
        }
        saveClusterRequest.setInstanceGroupSkuCfgs(instanceGroupSkuCfgList);
    }

    private void dataConversionAddConfClusterVmAndDataVolume(SheinRequestModel sheinRequestModel, InstanceGroupAddConfig instanceGroupAddConfig, InstanceGroupSkuCfg instanceGroupSkuCfg) {
        instanceGroupSkuCfg.setOsVolumeSize(sheinRequestModel.getRootVolSize());
        instanceGroupSkuCfg.setOsVolumeType(sheinRequestModel.getRootVolType());
        instanceGroupSkuCfg.setSkuNames(this.splitSukName(instanceGroupAddConfig.getInsType()));
        instanceGroupSkuCfg.setDataVolumeSize(instanceGroupAddConfig.getVolumeSizeInGB());
        instanceGroupSkuCfg.setDataVolumeType(instanceGroupAddConfig.getVolumeType());

        instanceGroupSkuCfg.setPriceStrategy(instanceGroupAddConfig.getPriceStrategy());
        instanceGroupSkuCfg.setMaxPrice(instanceGroupAddConfig.getPriceStrategyValue());
        instanceGroupSkuCfg.setPurchasePriority(instanceGroupAddConfig.getPurchasePriority());

        Integer provisionType = instanceGroupAddConfig.getProvisionType();
        if (provisionType == 1) {
            instanceGroupSkuCfg.setProvisionType("VM_Standalone");
        } else if (provisionType == 2) {
            instanceGroupSkuCfg.setProvisionType("VMSS_Flexible");
        }

        if (instanceGroupAddConfig.getDataDiskCnt() == null) {
            instanceGroupSkuCfg.setDataVolumeCount("1");
        } else {
            instanceGroupSkuCfg.setDataVolumeCount(instanceGroupAddConfig.getDataDiskCnt().toString());
        }
        if (StrUtil.isNotEmpty(instanceGroupAddConfig.getvCpus())){
            instanceGroupSkuCfg.setVCPUs(Integer.parseInt(instanceGroupAddConfig.getvCpus()));
        }
        if (StrUtil.isNotEmpty(instanceGroupAddConfig.getMemory())){
            instanceGroupSkuCfg.setMemoryGB(new BigDecimal(instanceGroupAddConfig.getMemory()));
        }
        //分配策略
        instanceGroupSkuCfg.setSpotAllocationStrategy(instanceGroupAddConfig.getSpotAllocationStrategy());
        instanceGroupSkuCfg.setRegularAllocationStrategy(instanceGroupAddConfig.getRegularAllocationStrategy());
    }

    private void dataConversionAddConfClusterScript(SheinRequestModel sheinRequestModel, AdminSaveClusterRequest saveClusterRequest) {
        List<com.sunbox.sdpadmin.model.admin.request.ConfClusterScript> confClusterScriptList = new ArrayList<>();
        List<ConfClusterScript> confClusterScript = sheinRequestModel.getConfClusterScript();
        if (!CollectionUtils.isEmpty(confClusterScript)) {
            for (ConfClusterScript clusterScript : confClusterScript) {
                com.sunbox.sdpadmin.model.admin.request.ConfClusterScript adminClusterScript = new com.sunbox.sdpadmin.model.admin.request.ConfClusterScript();
                adminClusterScript.setRunTiming(clusterScript.getRunTiming());
                adminClusterScript.setScriptName(clusterScript.getScriptName());
                adminClusterScript.setScriptPath(clusterScript.getScriptPath());
                adminClusterScript.setScriptParam(clusterScript.getScriptParam());
                adminClusterScript.setSortNo(clusterScript.getSortNo());
                confClusterScriptList.add(adminClusterScript);
            }
        }
        saveClusterRequest.setConfClusterScript(confClusterScriptList);
    }

    private void dataConversionAddElasticScaleRules(List<SheinElasticScalingRuleData> scalingRules, InstanceGroupSkuCfg instanceGroupSkuCfg) {
        if (!CollectionUtils.isEmpty(scalingRules)) {

        }
    }

    // sheinMaster与Ambari概念及参数转换
    private void sheinCreateClusterMaster2Ambari(SheinRequestModel sheinRequestModel) {
        if (sheinRequestModel.getStartHa() == 1) {
            // 高可用场景
            InstanceGroupAddConfig ambariAddInsGpConf = new InstanceGroupAddConfig();
            List<InstanceGroupCfg> ambariInsGpConfList = new ArrayList<>();
            InstanceGroupNewConfigElement ambariInsGpNewConfigElement = new InstanceGroupNewConfigElement();
            ambariInsGpNewConfigElement.setInstanceGroupAddConfig(ambariAddInsGpConf);
            ambariInsGpNewConfigElement.setInstanceGroupCfgs(ambariInsGpConfList);

            List<InstanceGroupNewConfigElement> instanceGroupNewConfigs = sheinRequestModel.getInstanceGroupNewConfigs();
            for (InstanceGroupNewConfigElement instanceGroupNewConfig : instanceGroupNewConfigs) {
                InstanceGroupAddConfig instanceGroupAddConfig = instanceGroupNewConfig.getInstanceGroupAddConfig();
                List<InstanceGroupCfg> instanceGroupCfgs = instanceGroupNewConfig.getInstanceGroupCfgs();
                if (instanceGroupAddConfig.getInsGpRole().equalsIgnoreCase("master")) {
                    try {
                        BeanUtils.copyProperties(ambariAddInsGpConf, instanceGroupAddConfig);
                        BeanUtils.copyProperties(ambariInsGpConfList, instanceGroupCfgs);
                    } catch (Exception e) {
                        getLogger().error("SheinApiServiceImpl.sheinCreateClusterMaster2Ambari bean properties copy error. instanceGroupAddConfig: {}, instanceGroupCfgs: {}, e: {}", instanceGroupAddConfig, instanceGroupCfgs, e);
                        return;
                    }
                    instanceGroupAddConfig.setInsGpCnt(2);
                    break;
                }
            }
            ambariAddInsGpConf.setInsGpRole("ambari");
            ambariAddInsGpConf.setInsGpCnt(1);
            instanceGroupNewConfigs.add(ambariInsGpNewConfigElement);
        } else if (sheinRequestModel.getStartHa() == 0) {
            // 非高可用场景
            List<InstanceGroupNewConfigElement> instanceGroupNewConfigs = sheinRequestModel.getInstanceGroupNewConfigs();
            for (InstanceGroupNewConfigElement instanceGroupNewConfig : instanceGroupNewConfigs) {
                InstanceGroupAddConfig instanceGroupAddConfig = instanceGroupNewConfig.getInstanceGroupAddConfig();
                if (instanceGroupAddConfig.getInsGpRole().equalsIgnoreCase("master")) {
                    instanceGroupAddConfig.setInsGpRole("ambari");
                }
            }
        }
    }

    private void logProcessTime(String clusterName, String action, Long begTime) {
        long duration = System.currentTimeMillis() - begTime;
        getLogger().info("cluster_create_" + action + ":" + clusterName + ",duration:" + duration);
    }

    private Boolean save2KeyVault(String key, String val,String region) {
        keyVault keyVault = metaDataItemService.getkeyVault(region);
        return keyVaultUtil.setSecret(key, val,keyVault.getEndpoint());
    }

    private void saveKeyVault(SheinRequestModel adminSaveClusterRequest) {
        String clusterName = StringUtils.isEmpty(adminSaveClusterRequest.getClusterName()) ? "" : adminSaveClusterRequest.getClusterName();

        String region = adminSaveClusterRequest.getDc();
        if (adminSaveClusterRequest.getHiveMetadataDbCfgs() != null
                && StringUtils.isNotEmpty(adminSaveClusterRequest.getHiveMetadataDbCfgs().getAccount())
                && StringUtils.isNotEmpty(adminSaveClusterRequest.getHiveMetadataDbCfgs().getPassword())) {
            String hiveDbPassword = adminSaveClusterRequest.getHiveMetadataDbCfgs().getPassword();
            String hiveDbAccount = adminSaveClusterRequest.getHiveMetadataDbCfgs().getAccount();
            if (StringUtils.isNotEmpty(hiveDbAccount) && StringUtils.isNotEmpty(hiveDbPassword)) {
                this.save2KeyVault("hivemetadata-db-user-" + clusterName, hiveDbAccount,region);
                this.save2KeyVault("hivemetadata-db-pwd-" + clusterName, hiveDbPassword,region);
            }
        }

        if (adminSaveClusterRequest.getIsEmbedAmbariDb() == null || adminSaveClusterRequest.getIsEmbedAmbariDb().equals(0)) {
            String ambariDbAccount = StringUtils.isEmpty(adminSaveClusterRequest.getAmbariDbCfgs().getAccount()) ? "" : adminSaveClusterRequest.getAmbariDbCfgs().getAccount();
            String ambariDbPassword = StringUtils.isEmpty(adminSaveClusterRequest.getAmbariDbCfgs().getPassword()) ? "" : adminSaveClusterRequest.getAmbariDbCfgs().getPassword();
            this.save2KeyVault("ambari-db-user-" + clusterName, ambariDbAccount,region);
            this.save2KeyVault("ambari-db-pwd-" + clusterName, ambariDbPassword,region);
        }
    }

    private boolean checkIsExistByClusterId(String clusterName) {
        Map params = new HashMap();
        params.put("clusterName", clusterName);
        params.put("emrStatus", Arrays.asList(ConfCluster.CREATING, ConfCluster.CREATED, ConfCluster.DELETING, ConfCluster.FAILED));
        List<Map> maps = confClusterMapper.selectByObject(params);
        if (maps.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * true:可用，false:不可用
     * @param clusterName
     * @return
     */
    private boolean checkClusterNameUsable(String clusterName) {
        Map params = new HashMap();
        params.put("clusterName", clusterName);
        params.put("emrStatus", Arrays.asList(ConfCluster.DELETED));
        ConfCluster confCluster = confClusterMapper.findTop1ByObject(params);
        if(confCluster == null || confCluster.getModifiedTime() == null) {
            return true;
        }

        if(System.currentTimeMillis() -  confCluster.getModifiedTime().getTime() < sdp_dns_ttl * 1000){
            return false;
        }
        return true;
    }

    private void addConfClusterScript(String clusterId, SheinRequestModel sheinRequestModel){
        if (sheinRequestModel.getConfClusterScript()==null){
            return;
        }
        for (ConfClusterScript ccs : sheinRequestModel.getConfClusterScript()){
            com.sunbox.domain.ConfClusterScript confClusterScript = new ConfClusterScript();
            confClusterScript.setCreatedTime(new Date());
            confClusterScript.setCreatedby(StringUtils.isEmpty(sheinRequestModel.getCreateUser())?sheinRequestModel.getCreateUser() : "shinAPI");
            confClusterScript.setClusterId(clusterId);
            confClusterScript.setConfScriptId(UUID.randomUUID().toString());
            if(ccs.getRunTiming().equalsIgnoreCase("aftervminit")){
                confClusterScript.setRunTiming("aftervminit");
            }
            if(ccs.getRunTiming().equalsIgnoreCase("beforestart")){
                confClusterScript.setRunTiming("beforestart");
            }
            if(ccs.getRunTiming().equalsIgnoreCase("afterstart")){
                confClusterScript.setRunTiming("afterstart");
            }
            confClusterScript.setScriptName(ccs.getScriptName());
            confClusterScript.setScriptPath(ccs.getScriptPath());
            confClusterScript.setScriptParam(ccs.getScriptParam());
            confClusterScript.setSortNo(ccs.getSortNo());
            confClusterScriptMapper.insertSelective(confClusterScript);
        }
    }

    @Override
    public SheinResponseModel getVmSkuList(String region) {
        ResultMsg resultMsg = azureService.getVmSkus(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    @Override
    public SheinResponseModel getSubnetList(String region) {
        ResultMsg resultMsg = azureService.getSubnet(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    @Override
    public SheinResponseModel getOsDiskTypeList(String region) {
        ResultMsg resultMsg = azureService.getDiskSku(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    @Override
    public SheinResponseModel getKeypairList(String region) {
        ResultMsg resultMsg = azureService.getSSHKeyPair(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    @Override
    public SheinResponseModel getPrimarySecurityGroupList(String region) {
        ResultMsg resultMsg = azureService.getNSGSku(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    @Override
    public SheinResponseModel getSubSecurityGroupList(String region) {
        ResultMsg resultMsg = azureService.getNSGSku(region);
        SheinResponseModel responseModel = new SheinResponseModel();
        if(null!=resultMsg && resultMsg.getResult()){
            responseModel.setInfo(resultMsg.getData());
            responseModel.setMsg("success");
            responseModel.setCode("200");
        }else {
            responseModel.setMsg(resultMsg.getErrorMsg());
            responseModel.setCode("-1");
        }
        return responseModel;
    }

    private ResultMsg clusterOperation(String clusterId, String operation, String releaseVer) {
        ResultMsg resultMsg = new ResultMsg();
        ResultMsg createPlanResult = composeService.createPlan(clusterId, operation, releaseVer);
        if (null == createPlanResult || !createPlanResult.getResult()) {
            return createPlanResult;
        }
        resultMsg.setResult(true);
        resultMsg.setMsg("success");
        return resultMsg;
    }

    @Override
    public SheinResponseModel descCluster(String clusterId, String dc) {
        EMRClusterInfoRespModel emrRespModel = new EMRClusterInfoRespModel();
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 设置emr基础信息
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (null == confCluster) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("集群不存在。");
            return sheinResponseModel;
        }

        if (StringUtils.isNotEmpty(dc)) {
            //入参映射
            dc = RegionMappingUtil.mappingIn(dc);
            if (!confCluster.getRegion().equalsIgnoreCase(dc)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("参数dc输入错误。");
                return sheinResponseModel;
            }
        }

        emrRespModel.setClusterId(clusterId);
        emrRespModel.setClusterName(confCluster.getClusterName());
        emrRespModel.setClusterReleaseVer(confCluster.getClusterReleaseVer());
        //出参映射
        emrRespModel.setDc(RegionMappingUtil.mappingOut(confCluster.getRegion()));
        emrRespModel.setSubnet(confCluster.getSubnet());
        emrRespModel.setAz(confCluster.getZone() != null ? Integer.valueOf(confCluster.getZone()) : null);
        emrRespModel.setMasterSecurityGroup(confCluster.getMasterSecurityGroup());
        emrRespModel.setSlaveSecurityGroup(confCluster.getSlaveSecurityGroup());
        emrRespModel.setS3LogLocation(confCluster.getLogPath());
        emrRespModel.setInstanceKeyPair(confCluster.getKeypairId());
        emrRespModel.setTerminationProtected("1".equals(confCluster.getDeleteProtected()));
        emrRespModel.setInternalIpOnly("0".equals(confCluster.getPublicipAvailable()));
        emrRespModel.setStartHa(confCluster.getIsHa());
        emrRespModel.setCreateUser(confCluster.getCreatedby());
        emrRespModel.setVmMI(confCluster.getVmMI());
        emrRespModel.setVmMIClientId(confCluster.getVmMIClientId());
        emrRespModel.setVmMITenantId(confCluster.getVmMITenantId());
        emrRespModel.setState(confCluster.getState());
        emrRespModel.setMasterHostName(getMasterHostName(clusterId));
        emrRespModel.setLogMI(confCluster.getLogMI());
        emrRespModel.setIsEmbedAmbariDb(confCluster.getIsEmbedAmbariDb());
        emrRespModel.setEnableGanglia(confCluster.getEnableGanglia());
        emrRespModel.setInWhiteList(confCluster.getIsWhiteAddr());
        emrRespModel.setIsParallelScale(confCluster.getIsParallelScale());
        sheinResponseModel.setInfo(emrRespModel);

        // 设置 集群安装的大数据组件 ClusterApps
        ConfClusterApp toQuery = new ConfClusterApp();
        toQuery.setClusterId(clusterId);
        List<ConfClusterApp> clusterApps = confClusterAppMapper.selectByObject(toQuery);
        if (!CollectionUtils.isEmpty(clusterApps)) {
            Map<String, String> clusterAppMap = new HashMap<>();
            for (ConfClusterApp clusterApp : clusterApps) {
                clusterAppMap.put(clusterApp.getAppName(), clusterApp.getAppVersion());
            }
            emrRespModel.setClusterApps(clusterAppMap);
        }

        // 设置 集群配置，各大数据组件的配置信息
        List<ConfClusterAppsConfig> confClusterAppsConfigList = confClusterAppsConfigMapper.selectByClusterId(clusterId);
        if (!CollectionUtils.isEmpty(confClusterAppsConfigList)) {
            Map<String, List<ConfClusterAppsConfig>> clusterCfgs = confClusterAppsConfigList.stream()
                    .collect(Collectors.groupingBy(ConfClusterAppsConfig::getAppConfigClassification));
            List<Empty> emptyList = new ArrayList<>();
            for (String key : clusterCfgs.keySet()) {
                Empty empty = new Empty();
                empty.setClassification(key);
                List<ConfClusterAppsConfig> configList = clusterCfgs.get(key);
                Map<String, Object> cfg = new HashMap<>();
                configList.forEach(config -> {
                    cfg.put(config.getConfigItem(), config.getConfigVal());
                });
                empty.setcfg(cfg);
                emptyList.add(empty);
            }
            emrRespModel.setClusterCfgs(emptyList);
        }

        emrRespModel.setInstanceCollectionType(Integer.parseInt(confCluster.getInstanceCollectionType()));

        // 设置 EMR 集群实例组
        String initScriptPath = "";
        if ("1".equals(confCluster.getInstanceCollectionType())) {
            List<ConfClusterVm> confClusterVmList = confClusterVmNeoMapper.selectByValidClusterId(clusterId);
            if (!CollectionUtils.isEmpty(confClusterVmList)) {
                initScriptPath = confClusterVmList.get(0).getInitScriptPath();
                List<InstanceGroupNewConfigElement> instanceGroupNewConfigs = confClusterVmList.stream()
                        .map(confClusterVm -> {
                            InstanceGroupNewConfigElement element = new InstanceGroupNewConfigElement();
                            InstanceGroupAddConfig value = new InstanceGroupAddConfig();
                            ScalingConstraint scalingConstraint = new ScalingConstraint();
                            List<SheinElasticScalingRuleData> scalingRules = new ArrayList<>();

                            value.setInsGpRole(confClusterVm.getVmRole());
                            value.setInsGpName(confClusterVm.getGroupName());
                            value.setInsGroupId(confClusterVm.getGroupId());

                            List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByVmConfId(confClusterVm.getVmConfId());
                            if (CollUtil.isNotEmpty(confHostGroupVmSkus)){
                                String skuNames = confHostGroupVmSkus.stream().map(ConfHostGroupVmSku::getSku).collect(Collectors.joining(","));
                                value.setInsType(skuNames);
                            }else{
                                value.setInsType(confClusterVm.getSku());
                            }

                            value.setInsGpCnt(confClusterVm.getCount());
                            value.setRootVolSize(confClusterVm.getOsVolumeSize());
                            value.setRootVolType(confClusterVm.getOsVolumeType());
                            if (confClusterVm.getPurchaseType() == null
                                    || 1 == confClusterVm.getPurchaseType()) {
                                value.setInsMktType("1");
                            } else {
                                value.setInsMktType("2");
                            }
                            value.setvCpus(confClusterVm.getVcpus());
                            value.setMemory(confClusterVm.getMemory());
                            value.setSpotAllocationStrategy(confClusterVm.getSpotAllocationStrategy());
                            value.setRegularAllocationStrategy(confClusterVm.getRegularAllocationStrategy());
                            value.setPriceStrategy(confClusterVm.getPriceStrategy());
                            value.setPriceStrategyValue(confClusterVm.getMaxPrice());
                            value.setPurchasePriority(confClusterVm.getPurchasePriority());
                            value.setProvisionType(confClusterVm.getProvisionType());

                            String vmConfId = confClusterVm.getVmConfId();
                            List<ConfClusterVmDataVolume> confClusterVmDataVolumeList =
                                    confClusterVmDataVolumeMapper.selectByVmConfId(vmConfId);
                            if (!CollectionUtils.isEmpty(confClusterVmDataVolumeList)) {
                                value.setVolumeType(confClusterVmDataVolumeList.get(0).getDataVolumeType());
                                value.setVolumeSizeInGB(confClusterVmDataVolumeList.get(0).getDataVolumeSize());
                                String datacnt = ((Integer) confClusterVmDataVolumeList.stream().mapToInt(ConfClusterVmDataVolume::getCount).sum()).toString();
                                value.setDataDiskCnt(Integer.parseInt(datacnt));
                            } else {
                                value.setDataDiskCnt(0);
                            }

                            // 封装弹性伸缩规则数据
                            if (confClusterVm.getVmRole().equalsIgnoreCase("task")) {
                                String groupEsId = checkScalingInstanceLimit(clusterId, confClusterVm.getGroupName(), confClusterVm.getVmRole(), scalingConstraint);
                                List<ConfGroupElasticScalingRule> confGroupElasticScalingRules = confGroupElasticScalingRuleMapper.selectAllByGroupEsId(groupEsId);
                                if (!CollectionUtils.isEmpty(confGroupElasticScalingRules)) {
                                    scalingRules =getScalingRules(confGroupElasticScalingRules);
                                }
                            }
                            value.setScalingRules(scalingRules);
                            value.setScalingConstraint(scalingConstraint);
                            element.setInstanceGroupAddConfig(value);

                            // 封装实例组配置
                            List<InstanceGroupCfg> instanceGroupCfgList = new ArrayList<>();
                            element.setInstanceGroupCfgs(instanceGroupCfgList);
                            return element;
                        })
                        .collect(Collectors.toList());
                emrRespModel.setInstanceGroupNewConfigs(instanceGroupNewConfigs);
            }
        }

        // confClusterScript封装
        List<ConfClusterScript> confClusterScriptList = confClusterScriptMapper.selectByClusterIdForCp(clusterId);
        if (!CollectionUtils.isEmpty(confClusterScriptList)) {
            List<ConfClusterScript> confClusterScripts = confClusterScriptList.stream()
                    .map(confClusterScript -> {
                        ConfClusterScript script = new ConfClusterScript();
                        script.setRunTiming(confClusterScript.getRunTiming());
                        script.setScriptName(confClusterScript.getScriptName());
                        script.setScriptParam(confClusterScript.getScriptParam());
                        script.setScriptPath(confClusterScript.getScriptPath());
                        script.setSortNo(confClusterScript.getSortNo());
                        return script;
                    })
                    .collect(Collectors.toList());
            emrRespModel.setConfClusterScript(confClusterScripts);
        }

        // 设置 标签
        ConfClusterTag toQueryTag = new ConfClusterTag();
        toQueryTag.setClusterId(clusterId);
        List<ConfClusterTag> confClusterTagList = confClusterTagMapper.selectByObject(toQueryTag);
        if (!CollectionUtils.isEmpty(confClusterTagList)) {
            JSONObject tagMapJsonObj = new JSONObject();
            confClusterTagList.forEach(tag -> {
                tagMapJsonObj.put(tag.getTagGroup(), tag.getTagVal());
            });
            emrRespModel.setTagMap(tagMapJsonObj);
        }

        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg("success");
        return sheinResponseModel;
    }

    /**
     *  scalingRule 输出结果格式转换
     *
     * @param confGroupElasticScalingRules
     * @return
     */
    private List<SheinElasticScalingRuleData> getScalingRules( List<ConfGroupElasticScalingRule> confGroupElasticScalingRules) {
        List<SheinElasticScalingRuleData> scalingRules = new CopyOnWriteArrayList<>();
        for (ConfGroupElasticScalingRule scalingRule : confGroupElasticScalingRules) {
            SheinElasticScalingRuleData scalingRuleData = new SheinElasticScalingRuleData();
            scalingRuleData.setEsRuleId(scalingRule.getEsRuleId());
            scalingRuleData.setEsRuleName(scalingRule.getEsRuleName());
            scalingRuleData.setScalingType(scalingRule.getScalingType());
            scalingRuleData.setPerScalingCnt(scalingRule.getPerSalingCout());

            for (int i = 0; i < ES_LOAD_METRIC_ARR.length; i++) {
                String loadMetric = ES_LOAD_METRIC_ARR[i];
                if (loadMetric.equals(scalingRule.getLoadMetric())) {
                    scalingRuleData.setLoadMetric(i + 1);
                }
            }

            scalingRuleData.setWindowSize(scalingRule.getWindowSize());

            for (int i = 0; i < ES_AGGREGATE_TYPE_ARR.length; i++) {
                String aggregateType = ES_AGGREGATE_TYPE_ARR[i];
                if (aggregateType.equals(scalingRule.getAggregateType())) {
                    scalingRuleData.setAggregateType(i + 1);
                }
            }

            for (int i = 0; i < ES_OPERATOR_ARR.length; i++) {
                String operator = ES_OPERATOR_ARR[i];
                if (operator.equals(scalingRule.getOperator())) {
                    scalingRuleData.setOperator(i + 1);
                }
            }

            scalingRuleData.setThreshold(scalingRule.getThreshold());
            scalingRuleData.setRepeatCnt(scalingRule.getRepeatCount());
            scalingRuleData.setFreezingTime(scalingRule.getFreezingTime());
            scalingRuleData.setIsValid(scalingRule.getIsValid());
            scalingRuleData.setEnableBeforeStartScript(scalingRule.getEnableBeforestartScript());
            scalingRuleData.setEnableAfterStartScript(scalingRule.getEnableAfterstartScript());
            scalingRuleData.setIsGracefulScaleIn(scalingRule.getIsGracefulScalein());
            scalingRuleData.setScaleInWaitingTime(scalingRule.getScaleinWaitingtime());
            scalingRules.add(scalingRuleData);
        }
        return scalingRules;
    }

    /**
     * check并初始化弹性伸缩实例数限制
     * 需要过滤：只有task类型实例组才能进入该方法
     */
    private String checkScalingInstanceLimit(String clusterId, String groupName, String vmRole, ScalingConstraint scalingConstraint) {
        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByClusterIdAndGroupNameAndValid(clusterId, groupName);
        if (confGroupElasticScaling == null) {
            ConfGroupElasticScaling initElasticScaling = new ConfGroupElasticScaling();
            initElasticScaling.setGroupEsId(UUID.randomUUID().toString());
            initElasticScaling.setClusterId(clusterId);
            initElasticScaling.setGroupName(groupName);
            initElasticScaling.setVmRole(StringUtils.lowerCase(vmRole));
            initElasticScaling.setMaxCount(200);
            initElasticScaling.setMinCount(0);
            initElasticScaling.setIsValid(1);
            initElasticScaling.setCreatedby("sysadmin");
            initElasticScaling.setCreatedTime(new Date());
            confGroupElasticScalingMapper.insert(initElasticScaling);
            scalingConstraint.setMaxCnt(200);
            scalingConstraint.setMinCnt(0);
            return initElasticScaling.getGroupEsId();
        }
        scalingConstraint.setMaxCnt(confGroupElasticScaling.getMaxCount());
        scalingConstraint.setMinCnt(confGroupElasticScaling.getMinCount());
        scalingConstraint.setIsFullCustody(confGroupElasticScaling.getIsFullCustody());
        scalingConstraint.setIsGracefulScalein(confGroupElasticScaling.getIsGracefulScalein());
        scalingConstraint.setScaleinWaitingTime(confGroupElasticScaling.getScaleinWaitingTime());
        scalingConstraint.setEnableBeforestartScript(confGroupElasticScaling.getEnableBeforestartScript());
        scalingConstraint.setEnableAfterstartScript(confGroupElasticScaling.getEnableAfterstartScript());
        return confGroupElasticScaling.getGroupEsId();
    }

    private String getMasterHostName(String clusterId) {
        getLogger().info("SheinApiServiceImpl.getMasterHostName, clusterId: " + clusterId);
        String masterHostName = null;
        Integer state = 1;
        List<InfoClusterVm> hAInfoClusterVms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, "master", state);
        if (!CollectionUtils.isEmpty(hAInfoClusterVms) && hAInfoClusterVms.size() > 1) {
            masterHostName = hAInfoClusterVms.get(0).getHostName();
        } else {
            List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, "ambari", state);
            if (!CollectionUtils.isEmpty(infoClusterVms)) {
                masterHostName = infoClusterVms.get(0).getHostName();
            }
        }
        return masterHostName;
    }

    /**
     * 更新资源组标签-全量
     */
    @Override
    public SheinResponseModel updateResourceGroupTags(String azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = composeService.updateResourceGroupTags(azureResourceGroupTagsRequest);
        return resultMsg2SheinResponseModel(resultMsg);
    }

    /**
     * 更新资源组标签-增量
     */
    @Override
    public SheinResponseModel addResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = composeService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg2SheinResponseModel(resultMsg);
    }

    /**
     * 删除资源组标签
     */
    @Override
    public SheinResponseModel deleteResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = composeService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg2SheinResponseModel(resultMsg);
    }

    @Override
    public SheinResponseModel resultMsg2SheinResponseModel(ResultMsg resultMsg) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 有错误信息
        if (StringUtils.isNotBlank(resultMsg.getErrorMsg())) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg(resultMsg.getErrorMsg());
            return sheinResponseModel;
        }

        // 无数据返回
        if (null == resultMsg.getData()) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
            return sheinResponseModel;
        }

        // 正常返回
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg("success");
        sheinResponseModel.setInfo(resultMsg.getData());
        return sheinResponseModel;
    }

    /**
     * 查询实例组信息
     *
     * @param clusterId
     * @param vmRole
     * @param dc
     * @return
     */
    @Override
    public SheinResponseModel listInstanceGroups(String clusterId, String vmRole, String dc) {
        SheinResponseModel model = new SheinResponseModel();
        Map<String, Object> paramap = new HashMap<>();
        paramap.put("id", clusterId);
        paramap.put("dc", dc);
        paramap.put("clusterId", clusterId);
        paramap.put("region", dc);

        List<String> vmRoles = new ArrayList<>();
        vmRoles.add("master");
        vmRoles.add("ambari");
        if (StringUtils.isNotEmpty(vmRole) && vmRole.equalsIgnoreCase("master")) {
            paramap.put("vmRoles", vmRoles);
        } else if (StringUtils.isNotEmpty(vmRole)) {
            paramap.put("vmRole", vmRole.toLowerCase());
        }

        if (StringUtils.isEmpty(clusterId)) {
            model.setMsg("缺少clusterId");
            model.setCode("-1");
            return model;
        }

        List<Map> confcluster = confClusterMapper.selectByObject(paramap);
        if (confcluster == null || confcluster.size() == 0) {
            model.setCode(SheinResponseModel.Request_NoData);
            model.setMsg("未查到有效数据");
            return model;
        }

        List<Map> infos = new ArrayList<>();

        List<Map> vms = confClusterVmNeoMapper.selectByObject(paramap);
        for (Map item : vms) {
            Map<String, Object> infoitem = new HashMap<>();
            String vmconfid = item.get("vm_conf_id").toString();
            if (item.get("purchase_type")==null
                    || item.get("purchase_type").toString().equals(ConfClusterVm.PURCHASETYPE_ONDEMOND)){
                infoitem.put("insMktType", "ondemond");
            }else{
                infoitem.put("insMktType", "spot");
            }
            infoitem.put("insType", item.get("sku").toString());
            infoitem.put("insGpRole", item.get("vm_role").toString());
            infoitem.put("insGpCnt", item.get("count").toString());
            infoitem.put("rootVolSize", item.get("os_volume_size"));
            infoitem.put("insGpId", item.get("vm_conf_id").toString());
            infoitem.put("insGpName", item.get("group_name").toString());
            infoitem.put("state", item.containsKey("state") ? Integer.valueOf(item.get("state").toString()) : "");
            List<Map<String, Object>> disks = new ArrayList<>();
            List<ConfClusterVmDataVolume> dataVolumes =
                    confClusterVmDataVolumeMapper.selectByVmConfId(vmconfid);
            dataVolumes.stream().forEach(x -> {
                Map<String, Object> disk = new HashMap<>();
                disk.put("volumeSizeInGB", x.getDataVolumeSize());
                disk.put("volumeType", x.getDataVolumeType());
                disks.add(disk);
            });
            infoitem.put("disks", disks);
            infos.add(infoitem);
        }

        if (StringUtils.isNotEmpty(vmRole) && vmRole.equalsIgnoreCase("master")) {
            if (infos != null && infos.size() == 2) {
                Optional<Map> master = infos.stream().filter(x -> {
                    return x.get("insGpRole").toString().equalsIgnoreCase("master");
                }).findFirst();
                if (master.isPresent()) {
                    infos.clear();
                    Map nma = master.get();
                    nma.put("insGpCnt", "3");
                    infos.add(nma);
                }
            }

            if (infos != null && infos.size() == 1) {
                Map nma = infos.get(0);
                nma.put("insGpRole", "master");
            }
        }

        if (infos.size() == 0) {
            model.setCode("404");
            model.setInfo(infos);
            model.setMsg("未查到有效数据");
        } else {
            model.setCode("200");
            model.setInfo(infos);
            model.setMsg("success");
        }
        return model;
    }

    /**
     * 查询实例组信息
     */
    @Override
    public SheinResponseModel listInstanceGroups(String clusterId, String vmRole, String dc, String groupId) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (StringUtils.isBlank(clusterId)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("缺少clusterId");
            return sheinResponseModel;
        }
        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", clusterId);
        clusterParams.put("region", dc);
        List<Map> confClusters = confClusterMapper.selectByObject(clusterParams);
        if (confClusters == null || confClusters.size() == 0) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
            return sheinResponseModel;
        }

        List<ConfClusterVm> confClusterVmList = confClusterVmNeoMapper.selectByClusterId(clusterId);
        if (CollectionUtils.isEmpty(confClusterVmList)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
            return sheinResponseModel;
        }
        Map<String, Object> groupNameAndConfClusterVmMap = new HashMap<>();
        for (ConfClusterVm confClusterVm : confClusterVmList) {
            groupNameAndConfClusterVmMap.put(confClusterVm.getGroupName(), confClusterVm);
        }

        // 高可用下，查询master，需要将ambari数据一并返回
        List<String> vmRoles = new ArrayList<>();
        vmRoles.add("master");
        vmRoles.add("ambari");

        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        params.put("groupId", groupId);
        if (StringUtils.isNotBlank(vmRole) && vmRole.equalsIgnoreCase("master")) {
            params.put("vmRoles", vmRoles);
        } else {
            params.put("vmRole", vmRole);
        }

        List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectAllByObject(params);
        if (CollectionUtils.isEmpty(confClusterHostGroups)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
            return sheinResponseModel;
        }

        List<InstanceGroupResponse> instanceGroupResponseList = new ArrayList<>();
        for (ConfClusterHostGroup confClusterHostGroup : confClusterHostGroups) {
            InstanceGroupResponse instanceGroupResponse = new InstanceGroupResponse();
            instanceGroupResponse.setClusterId(clusterId);
            instanceGroupResponse.setInsGpId(confClusterHostGroup.getGroupId());
            instanceGroupResponse.setInsGpName(confClusterHostGroup.getGroupName());
            instanceGroupResponse.setInsGpRole(confClusterHostGroup.getVmRole());
            instanceGroupResponse.setInsGpCnt(confClusterHostGroup.getInsCount());
            instanceGroupResponse.setState(confClusterHostGroup.getState());
            instanceGroupResponse.setExpectCount(confClusterHostGroup.getExpectCount());

            ConfClusterVm confClusterVm = (ConfClusterVm) groupNameAndConfClusterVmMap.get(confClusterHostGroup.getGroupName());
            List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByVmConfId(confClusterVm.getVmConfId());
            if (CollUtil.isNotEmpty(confHostGroupVmSkus)){
                String skuNames = confHostGroupVmSkus.stream().map(ConfHostGroupVmSku::getSku).collect(Collectors.joining(","));
                instanceGroupResponse.setInsType(skuNames);
            }else{
                instanceGroupResponse.setInsType(confClusterVm.getSku());
            }

            if (confClusterVm.getPurchaseType() == null
                    || confClusterVm.getPurchaseType().equals(ConfClusterVm.PURCHASETYPE_ONDEMOND)) {
                instanceGroupResponse.setInsMktType("ondemond");
            } else {
                instanceGroupResponse.setInsMktType("spot");
            }
            if (!StrUtil.equals("spot", instanceGroupResponse.getInsMktType())) {
                // 不是竞价实例时，期望值与实际值相同
                instanceGroupResponse.setExpectCount(instanceGroupResponse.getInsGpCnt());
            }

            instanceGroupResponse.setRootVolSize(confClusterVm.getOsVolumeSize());

            List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(confClusterVm.getVmConfId());
            List<Map<String, Object>> disks = new ArrayList<>();
            if (!CollectionUtils.isEmpty(confClusterVmDataVolumes)) {
                for (ConfClusterVmDataVolume confClusterVmDataVolume : confClusterVmDataVolumes) {
                    Map<String, Object> disk = new HashMap<>();
                    disk.put("volumeType", confClusterVmDataVolume.getDataVolumeType());
                    disk.put("volumeSizeInGB", confClusterVmDataVolume.getDataVolumeSize());
                    disks.add(disk);
                }
            }
            instanceGroupResponse.setDisks(disks);

            //region 查询hostgroup的弹性规则

            List<ConfGroupElasticScaling> elasticScalings =
                    confGroupElasticScalingMapper.listByClusterIdAndGroupNameAndValid(clusterId,confClusterHostGroup.getGroupName());

            if (elasticScalings!=null && elasticScalings.size()>0){
                ConfGroupElasticScaling scaling = elasticScalings.get(0);
                instanceGroupResponse.setMaxCnt(scaling.getMaxCount());
                instanceGroupResponse.setMinCnt(scaling.getMinCount());
                //全托管字段
                instanceGroupResponse.setEnableAfterstartScript(scaling.getEnableAfterstartScript());
                instanceGroupResponse.setEnableBeforestartScript(scaling.getEnableBeforestartScript());
                instanceGroupResponse.setIsFullCustody(scaling.getIsFullCustody());
                instanceGroupResponse.setIsGracefulScalein(scaling.getIsGracefulScalein());
                instanceGroupResponse.setScaleinWaitingTime(scaling.getScaleinWaitingTime());
                List<ConfGroupElasticScalingRule> confGroupElasticScalingRules
                        =confGroupElasticScalingRuleMapper.selectAllByGroupEsIdAndValid(scaling.getGroupEsId());
                if (confGroupElasticScalingRules !=null && confGroupElasticScalingRules.size()>0) {
                    List<SheinElasticScalingRuleData> scalingRuleDataList=getScalingRules(confGroupElasticScalingRules);
                    instanceGroupResponse.setScalingRules(scalingRuleDataList);
                }

            }

            //endregion 查询hostgroup的弹性规则


            instanceGroupResponseList.add(instanceGroupResponse);
        }

        if (CollectionUtils.isEmpty(instanceGroupResponseList)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setInfo(instanceGroupResponseList);
            return sheinResponseModel;
        }
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setInfo(instanceGroupResponseList);

        // HA下Master与Ambari合并处理
        if (StringUtils.isNotBlank(vmRole) && vmRole.equalsIgnoreCase("master")) {
            if (instanceGroupResponseList.size() == 2) {
                List<InstanceGroupResponse> masterInstanceGroupResponse = instanceGroupResponseList.stream().filter(item ->
                        item.getInsGpRole().equalsIgnoreCase("master")).collect(Collectors.toList());
                masterInstanceGroupResponse.get(0).setInsGpCnt(3);
                sheinResponseModel.setInfo(masterInstanceGroupResponse);
            }

            if (instanceGroupResponseList.size() == 1) {
                instanceGroupResponseList.get(0).setInsGpRole("master");
            }
        }

        return sheinResponseModel;
    }

    private SheinResponseModel checkParamGracefulScalein(Map<String, Object> param) {
        SheinResponseModel responseModel = new SheinResponseModel();
        if (param.containsKey("isGracefulScalein")) {
            int isGraceful = Double.valueOf(param.get("isGracefulScalein").toString()).intValue();
            param.put("isGracefulScalein", isGraceful);
            if (!(isGraceful == 0 || isGraceful == 1)) {
                responseModel.setCode(SheinResponseModel.Request_ConditionError);
                responseModel.setMsg("参数isGracefulScalein值只允许为0或1");
                return responseModel;
            }
        }

        if (param.containsKey("scaleinWaitingtime")) {
            int waitingTime = Double.valueOf(param.get("scaleinWaitingtime").toString()).intValue();
            param.put("scaleinWaitingtime", waitingTime);
            if (waitingTime < 60 || waitingTime > 1800) {
                responseModel.setCode(SheinResponseModel.Request_ConditionError);
                responseModel.setMsg("参数scaleinWaitingtime允许范围在：60-1800。");
                return responseModel;
            }

            if (!param.containsKey("isGracefulScalein")) {
                param.put("isGracefulScalein", 1);  // 优雅缩容
            } else {
                if (Double.valueOf(param.get("isGracefulScalein").toString()).intValue() != 1) {
                    responseModel.setCode(SheinResponseModel.Request_ConditionError);
                    responseModel.setMsg("参数scaleinWaitingtime存在下需要开启优雅缩容。");
                    return responseModel;
                }
            }
        } else {
            if (param.containsKey("isGracefulScalein") && Double.valueOf(param.get("isGracefulScalein").toString()).intValue() != 0) {
                responseModel.setCode(SheinResponseModel.Request_ConditionError);
                responseModel.setMsg("参数scaleinWaitingtime不能为空。");
                return responseModel;
            }
        }
        responseModel.setCode(SheinResponseModel.Request_Success);
        return responseModel;
    }

    /**
     * 手动扩缩容
     *
     * @param param
     * @return
     */
    @Override
    public SheinResponseModel modifyClusterInstanceGroup(Map<String, Object> param, boolean checkInsCnt) {
        SheinResponseModel responseModel = new SheinResponseModel();
        ResultMsg msg = new ResultMsg();
        Gson gson = new Gson();

        if (!param.containsKey("clusterId") || StringUtils.isEmpty(param.get("clusterId").toString())) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("缺失clusterId。");
            return responseModel;
        }

        if (!param.containsKey("insGpId") || StringUtils.isEmpty(param.get("insGpId").toString())) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("缺失insGpId。");
            return responseModel;
        }

        if (!param.containsKey("insCnt") || StringUtils.isEmpty(param.get("insCnt").toString())) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("insCnt字段缺失。");
            return responseModel;
        }

        SheinResponseModel checkGracefulScaleinModel = checkParamGracefulScalein(param);
        if (checkGracefulScaleinModel.getCode().equals(SheinResponseModel.Request_ConditionError)) {
            return checkGracefulScaleinModel;
        }

        // 集群有效性校验
        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", param.get("clusterId").toString());
        clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
        List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
        if (CollectionUtils.isEmpty(clusters)) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("无有效集群或该集群状态不支持扩缩容。");
            return responseModel;
        }
        ConfCluster confCluster = new ConfCluster();
        confCluster = gson.fromJson(gson.toJson(clusters.get(0)), confCluster.getClass());

        Integer insCnt = null;
        try {
            insCnt = Integer.valueOf(param.get("insCnt").toString());
        } catch (NumberFormatException e) {
            getLogger().error("SheinApiServiceImpl.resizeCluster.insCnt2int error. insCnt: {}, e: {}", param.get("insCnt").toString(), e);
            msg.setResult(false);
            msg.setErrorMsg("insCnt参数值错误");
        }

        // 校验实例组有效性和实例数量合法性
        if (checkInsCnt) {
            SheinResponseModel checkModel = checkInsGpValidityAndInsCntRange(confCluster, param.get("insGpId").toString(), insCnt);
            if (!checkModel.getCode().equals(SheinResponseModel.Request_Success)) {
                return checkModel;
            }
        }

        // 获取实例组相关信息
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(param.get("insGpId").toString());
        if (confClusterHostGroup == null) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("无该实例组信息");
            return responseModel;
        }
        param.put("igName", confClusterHostGroup.getGroupName());

        if (insCnt != null) {
            try {
                msg = composeService.resizeCluster(param);
            } catch (Exception e) {
                msg.setResult(false);
                msg.setErrorMsg("手动扩缩容失败");
                getLogger().error("SheinApiServiceImpl.resizeCluster.composeService.resizeCluster error. param: {}, e: {}", JSON.toJSONString(param), e);
            }
        }

        if (msg.getResult()) {
            responseModel.setCode(SheinResponseModel.Request_Success);
            responseModel.setInfo(msg.getData());
        } else {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg(msg.getErrorMsg());
        }

        return responseModel;
    }

    /**
     * 多实例组手动扩缩容
     */
    @Override
    public SheinResponseModel modifyClusterInstanceGroups(Map<String, Object> param) {
        SheinResponseModel responseModel = new SheinResponseModel();
        Gson gson = new Gson();
        List<Map<String, Object>> instanceGroupsResizeParam = new ArrayList<>();
        List<Map<String, Object>> respDatas = new ArrayList<>();

        String clusterId = param.containsKey("clusterId") ? param.get("clusterId").toString() : null;
        String insGpConfsJson = param.containsKey("insGpConfs") ? param.get("insGpConfs").toString() : null;
        if (StringUtils.isBlank(clusterId) || StringUtils.isBlank(insGpConfsJson)) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺失，必传参数：clusterId、insGpConfsJson");
            return responseModel;
        }

        List<Map<String, Object>> insGpConfsList = new ArrayList<>();
        try {
            insGpConfsList = gson.fromJson(insGpConfsJson, insGpConfsList.getClass());
        } catch (JsonSyntaxException e) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数：insGpConfs格式错误");
            return responseModel;
        }

        // 集群有效性校验
        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", clusterId);
        clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
        List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
        if (CollectionUtils.isEmpty(clusters)) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("无有效集群或该集群状态不支持扩缩容。");
            return responseModel;
        }
        ConfCluster confCluster = new ConfCluster();
        confCluster = gson.fromJson(gson.toJson(clusters.get(0)), confCluster.getClass());

        if (!CollectionUtils.isEmpty(insGpConfsList)) {
            for (Map<String, Object> insGpConfMap : insGpConfsList) {
                String insGpId = insGpConfMap.containsKey("insGpId") ? insGpConfMap.get("insGpId").toString() : null;
                String insCntStr = insGpConfMap.containsKey("insCnt") ? insGpConfMap.get("insCnt").toString() : null;

                if (StringUtils.isBlank(insGpId) || StringUtils.isBlank(insCntStr)) {
                    responseModel.setCode(SheinResponseModel.Request_ConditionError);
                    responseModel.setMsg("参数缺失：insGpId、insCnt");
                    return responseModel;
                }

                SheinResponseModel checkGracefulScaleinModel = checkParamGracefulScalein(insGpConfMap);
                if (checkGracefulScaleinModel.getCode().equals(SheinResponseModel.Request_ConditionError)) {
                    return checkGracefulScaleinModel;
                }

                Integer insCnt = null;
                try {
                    insCnt = Double.valueOf(insCntStr).intValue();
                } catch (Exception e) {
                    getLogger().error("SheinApiServiceImpl.modifyClusterInstanceGroups.insCntStr2int error. insCntStr: {}, e: {}", insCntStr, e);
                }

                if (insCnt == null) {
                    responseModel.setCode(SheinResponseModel.Request_ConditionError);
                    responseModel.setMsg("参数：insCnt格式错误");
                    return responseModel;
                }

                // 校验实例组有效性和实例数量合法性
                SheinResponseModel checkModel = checkInsGpValidityAndInsCntRange(confCluster, insGpId, insCnt);
                if (!checkModel.getCode().equals(SheinResponseModel.Request_Success)) {
                    return checkModel;
                }

                Map<String, Object> instanceGroupResizeParam = new HashMap<>();
                instanceGroupResizeParam.put("clusterId", clusterId);
                instanceGroupResizeParam.put("insGpId", insGpId);
                instanceGroupResizeParam.put("insCnt", insCnt);
                if (insGpConfMap.containsKey("isGracefulScalein")) {
                    instanceGroupResizeParam.put("isGracefulScalein", insGpConfMap.get("isGracefulScalein"));
                }
                if (insGpConfMap.containsKey("scaleinWaitingtime")) {
                    instanceGroupResizeParam.put("scaleinWaitingtime", insGpConfMap.get("scaleinWaitingtime"));
                }
                instanceGroupsResizeParam.add(instanceGroupResizeParam);
            }
        } else {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺失：insGpConfsJson");
            return responseModel;
        }

        for (Map<String, Object> instanceGroupResizeParam : instanceGroupsResizeParam) {
            Map<String, Object> respData = new HashMap<>();
            SheinResponseModel sheinResponseModel = modifyClusterInstanceGroup(instanceGroupResizeParam, false);
            if (SheinResponseModel.Request_Success.equals(sheinResponseModel.getCode())) {
                respData.put("insGpId", instanceGroupResizeParam.get("insGpId"));
                respData.put("taskId", sheinResponseModel.getInfo());
            } else {
                respData.put("insGpId", instanceGroupResizeParam.get("insGpId"));
                respData.put("msg", sheinResponseModel.getMsg());
            }
            respDatas.add(respData);
        }

        responseModel.setCode(SheinResponseModel.Request_Success);
        responseModel.setInfo(respDatas);
        return responseModel;
    }

    // 校验实例组有效性和实例数量合法性
    private SheinResponseModel checkInsGpValidityAndInsCntRange(ConfCluster confCluster, String insGpId, Integer insCnt) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg("success");
        Integer isHa = confCluster.getIsHa();

        // 获取实例组相关信息
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
        if (confClusterHostGroup == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("无该实例组信息");
            return sheinResponseModel;
        }

        // 实例组类型为task，支持实例数量为：0-1999
        if (confClusterHostGroup.getVmRole().equalsIgnoreCase("task")) {
            if (insCnt < 0 || insCnt > 1999) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("task类型的实例组实例数量范围为：0-1999");
                return sheinResponseModel;
            }
        }

        // 实例组类型为core，支持实例数量为：3-1000（HA），2-1000（!HA）
        if (confClusterHostGroup.getVmRole().equalsIgnoreCase("core")) {
            if (insCnt < scaleInMaxCount || insCnt > 1000) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("core类型的实例组实例数量范围为：" + scaleInMaxCount + "-1000");
                return sheinResponseModel;
            }
        }
        return sheinResponseModel;
    }

    /**
     * 重启大数据服务
     */
    @Override
    public SheinResponseModel restartClusterService(Map<String, Object> param) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 数据校验
        if (param == null || param.size() == 0) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数缺失");
            return sheinResponseModel;
        }

        if (!param.containsKey("clusterId") || StringUtils.isBlank(param.get("clusterId").toString())) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("clusterId缺失");
            return sheinResponseModel;
        }

        if (!param.containsKey("serviceName") || StringUtils.isBlank(param.get("serviceName").toString())) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("serviceName缺失");
            return sheinResponseModel;
        }

        // 集群合法性校验
        String clusterId = param.get("clusterId").toString();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (null == confCluster || !confCluster.getState().equals(ConfCluster.CREATED)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("集群不存在或集群状态不支持组件重启。");
            return sheinResponseModel;
        }

        // 实例组有效性校验
        if (param.containsKey("insGpId") && StringUtils.isNotBlank(param.get("insGpId").toString())) {
            String insGpId = param.get("insGpId").toString();
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
            if (confClusterHostGroup == null || !confClusterHostGroup.getClusterId().equals(clusterId)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无该实例组信息");
                return sheinResponseModel;
            }
            param.put("groupId", insGpId);
        }

        // serviceName 合法性校验
        if (param.get("serviceName").toString().equalsIgnoreCase("TEZ")
                || param.get("serviceName").toString().equalsIgnoreCase("SQOOP")) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("组件：TEZ、SQOOP无法重启");
            return sheinResponseModel;
        }
        ConfClusterApp selectParam = new ConfClusterApp();
        selectParam.setClusterId(clusterId);
        List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(selectParam);
        if (CollectionUtils.isEmpty(confClusterApps)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("未查到有效组件信息。");
            return sheinResponseModel;
        }
        Set<String> serviceNameSet = new HashSet<>();
        for (ConfClusterApp confClusterApp : confClusterApps) {
            serviceNameSet.add(confClusterApp.getAppName().toLowerCase());
        }
        if (!serviceNameSet.contains(param.get("serviceName").toString().toLowerCase())) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("serviceName不合法");
            return sheinResponseModel;
        }

        param.put("releaseVersion", confCluster.getClusterReleaseVer());
        ResultMsg resultMsg = composeService.restartClusterService(param);
        sheinResponseModel = resultMsg2SheinResponseModel(resultMsg);
        return sheinResponseModel;
    }

    /**
     * 查询重启任务结果
     */
    @Override
    public SheinResponseModel getRestartTaskResult(String clusterId, String taskId) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 参数校验
        if (StringUtils.isBlank(clusterId) || StringUtils.isBlank(taskId)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数缺失，必传参数：clusterId、taskId");
            return sheinResponseModel;
        }

        ConfClusterOpTask confClusterOpTask = confClusterOpTaskMapper.selectByTaskIdAndClusterId(taskId, clusterId);
        if (confClusterOpTask == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_NoData);
            sheinResponseModel.setMsg("未查到有效数据");
        } else {
            sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            sheinResponseModel.setInfo(confClusterOpTask);
        }
        return sheinResponseModel;
    }

    /**
     * 更新集群配置
     */
    @Override
    public SheinResponseModel updateClusterConfig(UpdateClusterConfigData requestData) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        Gson gson = new Gson();

        String clusterId = requestData.getClusterId();
        String insGpId = requestData.getInsGpId();
        List<ConfigProperties> clusterConfigs = requestData.getClusterConfigs();

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

        // 请求数据非空校验
        if (StringUtils.isBlank(clusterId) || CollectionUtils.isEmpty(clusterConfigs)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数缺失，必传参数：clusterId、clusterConfigs");
            return sheinResponseModel;
        }

        // 配置合法性校验
        SheinResponseModel checkModel = checkClusterConfigs(clusterConfigs, confCluster);
        if (!checkModel.getCode().equals(SheinResponseModel.Request_Success)) {
            return checkModel;
        }

        // 集群有效性校验
        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", clusterId);
        clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
        List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
        if (CollectionUtils.isEmpty(clusters)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("无有效集群或该集群状态不支持修改配置。");
            return sheinResponseModel;
        }

        // 获取实例组相关信息
        if (StringUtils.isNotBlank(insGpId)) {
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
            if (confClusterHostGroup == null) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无该实例组信息");
                return sheinResponseModel;
            }
            if (!confClusterHostGroup.getVmRole().equalsIgnoreCase("task")) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("只允许task类型的实例组更新配置");
                return sheinResponseModel;
            }
            requestData.setGroupId(requestData.getInsGpId());
            requestData.setGroupName(confClusterHostGroup.getGroupName());
        }

        ResultMsg resultMsg = composeService.updateClusterConfig(gson.toJson(requestData));
        sheinResponseModel = resultMsg2SheinResponseModel(resultMsg);
        return sheinResponseModel;
    }

    private SheinResponseModel checkClusterConfigs(List<ConfigProperties> clusterConfigs, ConfCluster confCluster) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg("success");

        Set<String> AppConfigClassifications = new HashSet<>();
        List<BaseReleaseAppsConfig> baseReleaseAppsConfigs = baseReleaseAppsConfigMapper.selectAll(confCluster.getClusterReleaseVer());
        if (!CollectionUtils.isEmpty(baseReleaseAppsConfigs)) {
            for (BaseReleaseAppsConfig baseReleaseAppsConfig : baseReleaseAppsConfigs) {
                AppConfigClassifications.add(baseReleaseAppsConfig.getAppConfigClassification());
            }
        }

        for (ConfigProperties clusterConfig : clusterConfigs) {
            String confItemName = clusterConfig.getConfItemName();
            if (StringUtils.isBlank(confItemName) || !AppConfigClassifications.contains(confItemName)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("参数：clusterConfigs.confItemName不合法");
                return sheinResponseModel;
            }

            Map<String, Object> confs = clusterConfig.getConfs();
            if (confs == null || confs.size() == 0) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("参数：clusterConfigs.confs不能为空");
                return sheinResponseModel;
            }

            Set<String> confKeys = confs.keySet();
            for (String confKey : confKeys) {
                String value = confs.get(confKey).toString();
                if (StringUtils.isBlank(confKey) || StringUtils.isBlank(value) || confKey.length() > 300 || value.length() > 300) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                    sheinResponseModel.setMsg("参数：clusterConfigs.confs, key 和 value 不能为空，且长度不能超过300");
                    return sheinResponseModel;
                }
            }
        }
        return sheinResponseModel;
    }

    /**
     * 执行脚本
     */
    @Override
    public SheinResponseModel saveAndExecuteScript(SaveAndExecuteScriptData scriptRequest) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        // 参数非空校验
        if (StringUtils.isBlank(scriptRequest.getClusterId()) || StringUtils.isBlank(scriptRequest.getJobName())
                || StringUtils.isBlank(scriptRequest.getScriptPath())) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数缺失，必传参数：clusterId、jobName、scriptPath");
            return sheinResponseModel;
        }

        // 集群有效性校验
        Map<String, Object> clusterParams = new HashMap<>();
        clusterParams.put("clusterId", scriptRequest.getClusterId());
        clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
        List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
        if (CollectionUtils.isEmpty(clusters)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("无有效集群或该集群状态不支持执行脚本。");
            return sheinResponseModel;
        }

        // jobName校验
        if (!scriptRequest.getJobName().matches(SCRIPT_JOBNAME_REGULAR)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数: jobName格式不合法，校验规则: " + SCRIPT_JOBNAME_REGULAR);
            return sheinResponseModel;
        }

        // scriptParam校验
        if (null != scriptRequest.getScriptParam() && scriptRequest.getScriptParam().length() > 1000) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("参数总字符数不能超过1000");
            return sheinResponseModel;
        }

        // 脚本url校验
        ResultMsg checkScriptUriResult = composeService.checkCustomScriptUri(scriptRequest.getScriptPath());
        if (null == checkScriptUriResult || !checkScriptUriResult.getData().toString().equalsIgnoreCase("true")) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("脚本url错误");
            return sheinResponseModel;
        }

        List<String> vmRoles = scriptRequest.getVmRoles();
        List<String> groupIds = scriptRequest.getInsGpIds();
        Set<String> groupNames = new HashSet<>();
        // 优先处理 insGpId，无 insGpId 再处理 vmRole
        if (!CollectionUtils.isEmpty(groupIds)) {
            for (String groupId : groupIds) {
                ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(groupId);
                if (confClusterHostGroup == null) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                    sheinResponseModel.setMsg("存在无效实例组id: " + groupId);
                    return sheinResponseModel;
                }
                groupNames.add(confClusterHostGroup.getGroupName());
            }
        } else if (!CollectionUtils.isEmpty(vmRoles)) {
            Set<String> vmRoleSet = new HashSet<>();
            for (String vmRole : vmRoles) {
                vmRoleSet.add(vmRole.toLowerCase());
            }

            Map<String, Object> selectParams = new HashMap<>();
            selectParams.put("clusterId", scriptRequest.getClusterId());
            selectParams.put("vmRoles", vmRoleSet);
            List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectAllByObject(selectParams);

            if (CollectionUtils.isEmpty(confClusterHostGroups)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("该实例组类型下无有效信息");
                return sheinResponseModel;
            } else {
                for (ConfClusterHostGroup confClusterHostGroup : confClusterHostGroups) {
                    groupNames.add(confClusterHostGroup.getGroupName());
                }
            }
        } else {
            // 取该集群下的所有实例组
            Map<String, Object> selectParams = new HashMap<>();
            selectParams.put("clusterId", scriptRequest.getClusterId());
            List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectAllByObject(selectParams);
            if (CollectionUtils.isEmpty(confClusterHostGroups)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无有效实例组数据");
                return sheinResponseModel;
            }
            for (ConfClusterHostGroup confClusterHostGroup : confClusterHostGroups) {
                groupNames.add(confClusterHostGroup.getGroupName());
            }
        }

        String groupNamesStr = StringUtils.join(groupNames.toArray(), ",");
        Map<String, Object> saveUserScriptParam = new HashMap<>();
        saveUserScriptParam.put("clusterId", scriptRequest.getClusterId());
        saveUserScriptParam.put("groupName", groupNamesStr);
        saveUserScriptParam.put("jobName", scriptRequest.getJobName());
        saveUserScriptParam.put("scriptPath", scriptRequest.getScriptPath());
        saveUserScriptParam.put("scriptParam", scriptRequest.getScriptParam());
        ResultMsg resultMsg = composeService.saveuserscript(JSON.toJSONString(saveUserScriptParam));

        sheinResponseModel = resultMsg2SheinResponseModel(resultMsg);
        return sheinResponseModel;
    }

    /**
     * 弹性伸缩规则变更通知
     */
    private void elasticScalingRuleChangeNotice() {
        try {
            scaleService.metricChange();
        } catch (Exception e) {
            getLogger().error("SheinApiServiceImpl.elasticScalingRuleChangeNotice error. e: ", e);
        }
    }

    /**
     * 集群弹性伸缩规则-附加
     */
    @Override
    public SheinResponseModel saveElasticScalingRule(SheinElasticScalingData request) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        try {
            String clusterId = request.getClusterId();
            String insGpId = request.getInsGpId();
            Integer maxCount = request.getMaxCnt();
            Integer minCount = request.getMinCnt();
            List<SheinElasticScalingRuleData> ruleDataList = request.getScalingRules();
            if (StringUtils.isBlank(clusterId) || StringUtils.isBlank(insGpId) || maxCount == null ||
                    minCount == null ) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("缺少参数，必传参数：clusterId、insGpId、maxCount、minCount");
                return sheinResponseModel;
            }
            if ( maxCount < 1 || maxCount > 1999 || minCount < 1 || minCount > 1999 || minCount > maxCount) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("实例数限制参数错误。");
                return sheinResponseModel;
            }
            //弹性扩缩容和规则二选一
            if (Objects.equals(1,request.getIsFullCustody())){
                if (request.getIsGracefulScalein() == null || request.getEnableBeforestartScript() == null ||
                        request.getEnableAfterstartScript() == null) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                    sheinResponseModel.setMsg("开启全托管缺少参数，必传参数：isGracefulScalein、enableBeforestartScript、enableAfterstartScript");
                    return sheinResponseModel;
                }
                // 优雅缩容
                if (request.getIsGracefulScalein()==1){
                    if (request.getScaleinWaitingTime() == null || request.getScaleinWaitingTime() < 0) {
                        sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                        sheinResponseModel.setMsg("开启全托管优雅缩容缺少参数，必传参数：scaleinWaitingTime");
                        return sheinResponseModel;
                    }
                }
            }else{
                if (CollectionUtils.isEmpty(ruleDataList)) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                    sheinResponseModel.setMsg("缺少参数，必传参数：ScalingRules");
                    return sheinResponseModel;
                }
                // 弹性伸缩规则参数校验
                SheinResponseModel checkSRModel = checkScalingRulesParam(ruleDataList);
                if (!checkSRModel.getCode().equals(SheinResponseModel.Request_Success)) {
                    return checkSRModel;
                }
            }

            Map<String, Object> clusterParams = new HashMap<>();
            clusterParams.put("clusterId", clusterId);
            clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
            List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
            if (CollectionUtils.isEmpty(clusters)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无有效集群或该集群状态不支持添加弹性伸缩规则。");
                return sheinResponseModel;
            }

            // 获取实例组相关信息
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
            if (confClusterHostGroup == null) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无该实例组信息");
                return sheinResponseModel;
            }

            // 持久化实例组弹性伸缩配置
            String groupEsId = saveConfGroupElasticScaling(clusterId, confClusterHostGroup,request );

            //开启全托管时,删除规则
            if (Objects.equals(1,request.getIsFullCustody())){
                confGroupElasticScalingRuleMapper.deleteByGroupEsId(groupEsId);
                getLogger().info("开启全托管，删除弹性伸缩规则clusterId:{} groupName:{}",clusterId,confClusterHostGroup.getGroupName());
            }else{
                // 持久化实例组弹性伸缩规则
                List<Map<String, Object>> respDataList = saveConfGroupElasticScalingRule(clusterId, confClusterHostGroup.getGroupName(), groupEsId, ruleDataList);
                if (CollectionUtils.isEmpty(respDataList)) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
                    sheinResponseModel.setMsg("error");
                    return sheinResponseModel;
                }
                sheinResponseModel.setInfo(respDataList);
            }

            // 弹性伸缩规则变更通知
            elasticScalingRuleChangeNotice();
            sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            sheinResponseModel.setMsg("success");
        } catch (Exception e) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("处理过程发生异常。");
            getLogger().error("处理过程发生异常",e);
        }
        return sheinResponseModel;
    }

    // 持久化实例组弹性伸缩配置
    private String saveConfGroupElasticScaling(String clusterId, ConfClusterHostGroup confClusterHostGroup, SheinElasticScalingData scalingData) {
        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByClusterIdAndGroupNameAndValid(clusterId, confClusterHostGroup.getGroupName());
        String uuid = UUID.randomUUID().toString();
        if (confGroupElasticScaling == null) {
            // 新增 confGroupElasticScaling
            ConfGroupElasticScaling initElasticScaling = new ConfGroupElasticScaling();
            initElasticScaling.setGroupEsId(uuid);
            initElasticScaling.setClusterId(clusterId);
            initElasticScaling.setGroupName(confClusterHostGroup.getGroupName());
            initElasticScaling.setVmRole(confClusterHostGroup.getVmRole());
            initElasticScaling.setMaxCount(scalingData.getMaxCnt());
            initElasticScaling.setMinCount(scalingData.getMinCnt());
            initElasticScaling.setIsValid(1);
            initElasticScaling.setCreatedby("sheinAdmin");
            initElasticScaling.setCreatedTime(new Date());
            //全托管
            initElasticScaling.setIsFullCustody(scalingData.getIsFullCustody());
            initElasticScaling.setIsGracefulScalein(scalingData.getIsGracefulScalein());
            initElasticScaling.setScaleinWaitingTime(scalingData.getScaleinWaitingTime());
            initElasticScaling.setEnableAfterstartScript(scalingData.getEnableAfterstartScript());
            initElasticScaling.setEnableBeforestartScript(scalingData.getEnableBeforestartScript());
            confGroupElasticScalingMapper.insert(initElasticScaling);
            confGroupElasticScaling = initElasticScaling;
        } else {
            // 修改 confGroupElasticScaling
            confGroupElasticScaling.setMaxCount(scalingData.getMaxCnt());
            confGroupElasticScaling.setMinCount(scalingData.getMinCnt());
            confGroupElasticScaling.setModifiedby("sheinAdmin");
            confGroupElasticScaling.setModifiedTime(new Date());
            //全托管
            confGroupElasticScaling.setIsFullCustody(scalingData.getIsFullCustody());
            confGroupElasticScaling.setIsGracefulScalein(scalingData.getIsGracefulScalein());
            confGroupElasticScaling.setScaleinWaitingTime(scalingData.getScaleinWaitingTime());
            confGroupElasticScaling.setEnableAfterstartScript(scalingData.getEnableAfterstartScript());
            confGroupElasticScaling.setEnableBeforestartScript(scalingData.getEnableBeforestartScript());

            confGroupElasticScalingMapper.updateByPrimaryKeySelective(confGroupElasticScaling);
        }
        String groupEsId = confGroupElasticScaling.getGroupEsId();
        return groupEsId;
    }

    // 持久化实例组弹性伸缩规则
    private List<Map<String, Object>> saveConfGroupElasticScalingRule(String clusterId, String groupName, String groupEsId, List<SheinElasticScalingRuleData> ruleDataList) {
        List<Map<String, Object>> respDataList = new ArrayList<>();

        // 数据封装
        List<ConfGroupElasticScalingRule> elasticScalingRuleList = new ArrayList<>();
        for (SheinElasticScalingRuleData ruleData : ruleDataList) {
            ConfGroupElasticScalingRule confGroupElasticScalingRule = new ConfGroupElasticScalingRule();
            try {
                BeanUtils.copyProperties(confGroupElasticScalingRule, ruleData);
                // 对一些不符合规范字段单独赋值
                confGroupElasticScalingRule.setPerSalingCout(ruleData.getPerScalingCnt());
                confGroupElasticScalingRule.setRepeatCount(ruleData.getRepeatCnt());
                confGroupElasticScalingRule.setEnableBeforestartScript(ruleData.getEnableBeforeStartScript());
                confGroupElasticScalingRule.setEnableAfterstartScript(ruleData.getEnableAfterStartScript());
                confGroupElasticScalingRule.setIsGracefulScalein(ruleData.getIsGracefulScaleIn());
                confGroupElasticScalingRule.setScaleinWaitingtime(ruleData.getScaleInWaitingTime());
            } catch (Exception e) {
                getLogger().error("SheinApiServiceImpl.saveConfGroupElasticScalingRule, ruleData to confGroupElasticScalingRule error. ruleData: {}, e: {}", JSON.toJSONString(ruleData), e);
                return respDataList;
            }

            if (StringUtils.isBlank(confGroupElasticScalingRule.getEsRuleId())) {
                confGroupElasticScalingRule.setEsRuleId(UUID.randomUUID().toString());
            }
            confGroupElasticScalingRule.setGroupEsId(groupEsId);
            confGroupElasticScalingRule.setClusterId(clusterId);
            confGroupElasticScalingRule.setGroupName(groupName);
            confGroupElasticScalingRule.setIsValid(1);

            confGroupElasticScalingRule.setLoadMetric(ES_LOAD_METRIC_ARR[ruleData.getLoadMetric() - 1]);
            confGroupElasticScalingRule.setAggregateType(ES_AGGREGATE_TYPE_ARR[ruleData.getAggregateType() - 1]);
            confGroupElasticScalingRule.setOperator(ES_OPERATOR_ARR[ruleData.getOperator() - 1]);

            if (ruleData.getScalingType() == 1) {
                confGroupElasticScalingRule.setEnableBeforestartScript(ruleData.getEnableBeforeStartScript() != null ? ruleData.getEnableBeforeStartScript() : 0);
                confGroupElasticScalingRule.setEnableAfterstartScript(ruleData.getEnableAfterStartScript() != null ? ruleData.getEnableAfterStartScript() : 0);
            }
            confGroupElasticScalingRule.setCreatedby("sheinAdmin");
            confGroupElasticScalingRule.setCreatedTime(new Date());
            elasticScalingRuleList.add(confGroupElasticScalingRule);
        }

        List<ConfGroupElasticScalingRule> confGroupElasticScalingRules = confGroupElasticScalingRuleMapper.selectAllByGroupEsId(groupEsId);
        if (!CollectionUtils.isEmpty(confGroupElasticScalingRules)) {
            // 规则合并
            for (ConfGroupElasticScalingRule confGroupElasticScalingRule : elasticScalingRuleList) {
                Integer scalingType = confGroupElasticScalingRule.getScalingType();
                String loadMetric = confGroupElasticScalingRule.getLoadMetric();
                boolean overwriteFlag = false;

                // 规则覆盖（同一 scalingType + loadMetric 的规则只能允许存在一条）
                for (ConfGroupElasticScalingRule scalingRule : confGroupElasticScalingRules) {
                    if (scalingType.equals(scalingRule.getScalingType()) && loadMetric.equals(scalingRule.getLoadMetric())) {
                        confGroupElasticScalingRule.setEsRuleId(scalingRule.getEsRuleId());
                        confGroupElasticScalingRule.setCreatedTime(scalingRule.getCreatedTime());
                        confGroupElasticScalingRule.setModifiedTime(new Date());
                        confGroupElasticScalingRule.setRuleSorted(scalingRule.getRuleSorted());
                        overwriteFlag = true;
                        break;
                    }
                }

                if (overwriteFlag) {
                    confGroupElasticScalingRuleMapper.updateByPrimaryKey(confGroupElasticScalingRule);
                } else {
                    confGroupElasticScalingRuleMapper.insert(confGroupElasticScalingRule);
                }
                Map<String, Object> respData = new HashMap<>();
                respData.put("esRuleId", confGroupElasticScalingRule.getEsRuleId());
                respData.put("esRuleName", confGroupElasticScalingRule.getEsRuleName());
                respDataList.add(respData);
            }
        } else {
            // 规则添加
            for (ConfGroupElasticScalingRule confGroupElasticScalingRule : elasticScalingRuleList) {
                confGroupElasticScalingRuleMapper.insert(confGroupElasticScalingRule);
                Map<String, Object> respData = new HashMap<>();
                respData.put("esRuleId", confGroupElasticScalingRule.getEsRuleId());
                respData.put("esRuleName", confGroupElasticScalingRule.getEsRuleName());
                respDataList.add(respData);
            }
        }
        return respDataList;
    }

    private SheinResponseModel checkScalingRulesParam(List<SheinElasticScalingRuleData> ruleDataList) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);

        Set<String> scalingTypeAndLoadMetricSet = new HashSet<>();
        int scalingTypeAndLoadMetricCount = 0;

        for (SheinElasticScalingRuleData ruleData : ruleDataList) {
            String esRuleId = ruleData.getEsRuleId();
            String esRuleName = ruleData.getEsRuleName();
            Integer scalingType = ruleData.getScalingType();
            Integer perScalingCount = ruleData.getPerScalingCnt();
            Integer loadMetric = ruleData.getLoadMetric();
            Integer windowSize = ruleData.getWindowSize();
            Integer aggregateType = ruleData.getAggregateType();
            Integer operator = ruleData.getOperator();
            Double threshold = ruleData.getThreshold();
            Integer repeatCount = ruleData.getRepeatCnt();
            Integer freezingTime = ruleData.getFreezingTime();
            Integer enableAfterStartScript = ruleData.getEnableAfterStartScript();
            Integer enableBeforeStartScript = ruleData.getEnableBeforeStartScript();
            Integer isGracefulScalein = ruleData.getIsGracefulScaleIn();
            Integer scaleInWaitingTime = ruleData.getScaleInWaitingTime();

            StringBuilder sb = new StringBuilder();
            if (esRuleId != null && !esRuleId.matches(ELASTIC_RULE_ID_REGULAR)) {
                sb.append("规则id不合法: " + ELASTIC_RULE_ID_REGULAR + "; ");
            }
            if (StringUtils.isBlank(esRuleName) || !esRuleName.matches(ELASTIC_RULE_NAME_REGULAR)) {
                sb.append("规则名称不合法: " + ELASTIC_RULE_NAME_REGULAR + "; ");
            }
            if (scalingType == null || !(scalingType == 1 || scalingType == 0)) {
                sb.append("伸缩类型填写错误; ");
            }
            if (perScalingCount == null || perScalingCount < 1 || perScalingCount > 1999) {
                sb.append("伸缩数量不正确; ");
            }
            // 负载指标，枚举值判断
            if (loadMetric == null || !(loadMetric == 1 || loadMetric == 2 || loadMetric == 3 || loadMetric == 4)) {
                sb.append("负载指标不正确; ");
            }
            if (windowSize == null || windowSize < 1) {
                sb.append("统计周期不正确; ");
            }
            if (aggregateType == null || !(aggregateType == 1 || aggregateType == 2 || aggregateType == 3)) {
                sb.append("聚合类型不正确; ");
            }
            if (operator == null || !(operator == 1 || operator == 2 || operator == 3 || operator == 4)) {
                sb.append("运算符不正确; ");
            }
            if (threshold == null || threshold < 1 || threshold > 100) {
                sb.append("阈值不正确; ");
            }
            if (repeatCount == null || repeatCount < 1) {
                sb.append("统计周期不正确; ");
            }
            if (freezingTime == null || freezingTime < 0) {
                sb.append("冷却时间不正确; ");
            }
            if (enableAfterStartScript != null) {
                if (!(enableAfterStartScript == 0 || enableAfterStartScript == 1)) {
                    sb.append("参数enableAfterStartScript填写错误; ");
                }
            } else {
                ruleData.setEnableAfterStartScript(1);
            }
            if (enableBeforeStartScript != null) {
                if (!(enableBeforeStartScript == 0 || enableBeforeStartScript == 1)) {
                    sb.append("参数enableBeforeStartScript填写错误; ");
                }
            } else {
                ruleData.setEnableBeforeStartScript(1);
            }

            // 缩容场景
            if (scalingType != null && scalingType == 0) {
                if (isGracefulScalein == null) {
                    ruleData.setIsGracefulScaleIn(1);
                    isGracefulScalein = 1;
                }

                if (!(isGracefulScalein == 0 || isGracefulScalein == 1)) {
                    sb.append("请填写是否优雅扩缩容; ");
                } else if (isGracefulScalein == 1) {
                    if (scaleInWaitingTime != null) {
                        if (scaleInWaitingTime < 60 || scaleInWaitingTime > 1800) {
                            sb.append("优雅缩容等待时间错误; ");
                        }
                    } else {
                        ruleData.setScaleInWaitingTime(1800);
                    }
                }
            }

            if (sb.length() > 0) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg(sb.toString());
                break;
            }

            scalingTypeAndLoadMetricSet.add(scalingType.toString() + "-" + loadMetric.toString());
            scalingTypeAndLoadMetricCount++;
        }

        if (scalingTypeAndLoadMetricSet.size() != scalingTypeAndLoadMetricCount) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("弹性扩容或缩容，同一负载指标只允许存在一条规则");
        }
        return sheinResponseModel;
    }

    /**
     * 集群弹性伸缩规则-剥离
     */
    @Override
    public SheinResponseModel terminateElasticScalingRule(SheinElasticScalingData request) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        String clusterId = request.getClusterId();
        String insGpId = request.getInsGpId();
        List<String> esRuleIds = request.getEsRuleIds();

        if (StringUtils.isBlank(clusterId) || StringUtils.isBlank(insGpId)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("缺少参数，必传参数：clusterId、insGpId");
            return sheinResponseModel;
        }

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("集群不存在");
            return sheinResponseModel;
        }

        // 获取实例组相关信息
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
        if (confClusterHostGroup == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("无该实例组信息");
            return sheinResponseModel;
        }

        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByClusterIdAndGroupNameAndValid(clusterId, confClusterHostGroup.getGroupName());
        if (confGroupElasticScaling == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("该实例组下无伸缩配置");
            return sheinResponseModel;
        }

        if (CollectionUtils.isEmpty(esRuleIds)) {
            // 删除该实例组下的全部弹性规则
            confGroupElasticScalingRuleMapper.deleteByGroupEsId(confGroupElasticScaling.getGroupEsId());
        } else {
            // 根据规则id删除
            for (String esRuleId : esRuleIds) {
                confGroupElasticScalingRuleMapper.deleteByPrimaryKey(esRuleId);
            }
        }
        // 弹性伸缩规则变更通知
        elasticScalingRuleChangeNotice();
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg("success");
        return sheinResponseModel;
    }

    /**
     * 集群新增实例组
     */
    @Override
    public SheinResponseModel addInstanceGroup(SheinInstanceGroupData instanceGroupData) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        Gson gson = new Gson();
        try {

            String clusterId = instanceGroupData.getSrcClusterId();
            SheinInstanceGroupSkuCfg instanceGroupSkuCfg = instanceGroupData.getInstanceGroupSkuCfg();

            // 参数非空校验
            if (StringUtils.isBlank(clusterId) || instanceGroupSkuCfg == null) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("缺少参数，必传参数：clusterId、instanceGroupSkuCfg");
                return sheinResponseModel;
            }

            // 参数合法性校验
            SheinResponseModel checkSRModel = checkSheinAddInstanceGroupParams(instanceGroupData);
            if (!checkSRModel.getCode().equals(SheinResponseModel.Request_Success)) {
                return checkSRModel;
            }

            // 集群有效性校验
            Map<String, Object> clusterParams = new HashMap<>();
            clusterParams.put("clusterId", clusterId);
            clusterParams.put("emrStatus", Arrays.asList(ConfCluster.CREATED));
            List<Map> clusters = confClusterMapper.selectByObject(clusterParams);
            if (CollectionUtils.isEmpty(clusters)) {
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("无有效集群或该集群状态不支持添加实例组。");
                return sheinResponseModel;
            }

            // 调用admin新增实例组逻辑
            AdminSaveClusterRequest adminSaveClusterRequest = new AdminSaveClusterRequest();
            List<InstanceGroupSkuCfg> addGroupCfgs = new ArrayList<>();
            InstanceGroupSkuCfg addGroupCfg = new InstanceGroupSkuCfg();
            try {
                BeanUtils.copyProperties(adminSaveClusterRequest, instanceGroupData);
                BeanUtils.copyProperties(addGroupCfg, instanceGroupData.getInstanceGroupSkuCfg());
            } catch (Exception e) {
                getLogger().error("SheinApiServiceImpl.addInstanceGroup, instanceGroupData to adminSaveClusterRequest error. instanceGroupData: {}, e: {}", JSON.toJSONString(instanceGroupData), e);
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("addInstanceGroup error.");
                return sheinResponseModel;
            }
            addGroupCfgs.add(addGroupCfg);
            adminSaveClusterRequest.setInstanceGroupSkuCfgs(addGroupCfgs);
            ResultMsg resultMsg = adminApiService.addGroup(gson.toJson(adminSaveClusterRequest));
            getLogger().info("SheinApiServiceImpl.addInstanceGroup to adminApiService.addGroup response: {}", gson.toJson(resultMsg));

            Map<String, Object> respData = new HashMap<>();
            if (resultMsg.getResult()) {
                InstanceGroupSkuCfg addGroupResult = (InstanceGroupSkuCfg) resultMsg.getData();
                respData.put("insGpName", addGroupResult.getGroupName());
                respData.put("insGpId", addGroupResult.getGroupId());
            }
            resultMsg.setData(respData);
            sheinResponseModel = resultMsg2SheinResponseModel(resultMsg);
        }catch (Exception e){
            getLogger().error("处理过程发生异常,",e);
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("处理过程发生异常。");
        }
        return sheinResponseModel;
    }

    private SheinResponseModel checkSheinAddInstanceGroupParams(SheinInstanceGroupData instanceGroupData) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        StringBuilder sb = new StringBuilder();

        String clusterId = instanceGroupData.getSrcClusterId();
        String vmRole = instanceGroupData.getVmRole();
        String groupName = instanceGroupData.getGroupName();
        SheinInstanceGroupSkuCfg instanceGroupSkuCfg = instanceGroupData.getInstanceGroupSkuCfg();
        List<SheinClusterCfg> clusterCfgs = instanceGroupData.getClusterCfgs();

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (cluster == null){
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("集群信息不存在");
            return sheinResponseModel;
        }
        String region=cluster.getRegion();

        // 只允许新增task类型的实例组
        if (StringUtils.isBlank(vmRole)) {
            instanceGroupData.setVmRole("task");
        } else if (!vmRole.equalsIgnoreCase("task")) {
            sb.append("实例组类型错误; ");
        }

        // 未传实例组名称自动补充
        if (StringUtils.isBlank(groupName)) {
            int groupCount = confClusterHostGroupMapper.selectInsCountByVmRole(clusterId, vmRole);
            String insGpNamePrefix = "task-";
            String insGpName = insGpNamePrefix + (groupCount + 1);
            instanceGroupData.setGroupName(insGpName);
            groupName = insGpName;
        }

        // 实例组名称合法性校验
        if (!groupName.matches(INSTANCE_GROUP_NAME_REGULAR)) {
            sb.append("实例组名称不合法; ");
        } else {
            ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(clusterId, groupName);
            if (confClusterHostGroup != null) {
                sb.append("该实例组已存在; ");
            }
        }

        // 校验 instanceGroupSkuCfg 数据合法性
        Integer cnt = instanceGroupSkuCfg.getCnt();
        String skuName = instanceGroupSkuCfg.getSkuName();
        String dataVolumeType = instanceGroupSkuCfg.getDataVolumeType();
        Integer dataVolumeSize = instanceGroupSkuCfg.getDataVolumeSize();
        Integer dataVolumeCount = instanceGroupSkuCfg.getDataVolumeCount();
        Integer insMktType = instanceGroupSkuCfg.getPurchaseType();
        Integer priceStrategy = instanceGroupSkuCfg.getPriceStrategy();
        BigDecimal priceStrategyValue = instanceGroupSkuCfg.getMaxPrice();
        Integer enableBeforestartScript = instanceGroupSkuCfg.getEnableBeforestartScript();
        Integer enableAfterstartScript = instanceGroupSkuCfg.getEnableAfterstartScript();

        // 必填验证
        if (cnt == null  || StringUtils.isBlank(dataVolumeType) || StrUtil.isEmpty(skuName)
                || dataVolumeSize == null || dataVolumeCount == null) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg("实例组配置缺失参数。");
            return sheinResponseModel;
        }
        List<String> skuNameList = this.splitSukName(instanceGroupSkuCfg.getSkuName());
        // 购买方式校验，默认给1
        if (insMktType == null) {
            instanceGroupSkuCfg.setPurchaseType(1);
        } else if (!(insMktType == 1 || insMktType == 2)) {
            sb.append("参数purchaseType不合法; ");
        }

        if (insMktType == 2) {
            if (priceStrategy == null || !(priceStrategy == 1 || priceStrategy == 2)) {
                sb.append("参数: priceStrategy传值有误; ");
            }
            if (priceStrategy == 1) {  // 按市场价百分比
                if (priceStrategyValue == null || priceStrategyValue.doubleValue() < 0 || priceStrategyValue.doubleValue() > 100) {
                    sb.append("参数: priceStrategyValue传值有误, [0,100]; ");
                }
            } else if (priceStrategy == 2) {  // 固定价
                if (priceStrategyValue == null || priceStrategyValue.doubleValue() < 0) {
                    sb.append("参数: priceStrategyValue传值有误, 只允许为正数; ");
                }
            }
        }

        // 默认1
        if (enableBeforestartScript == null) {
            instanceGroupSkuCfg.setEnableBeforestartScript(1);
        } else if (!(enableBeforestartScript == 0 || enableBeforestartScript == 1)) {
            sb.append("参数senableBeforestartScript不合法; ");
        }

        // 默认1
        if (enableAfterstartScript == null) {
            instanceGroupSkuCfg.setEnableAfterstartScript(1);
        } else if (!(enableAfterstartScript == 0 || enableAfterstartScript == 1)) {
            sb.append("参数senableBeforestartScript不合法; ");
        }

        // 实例数量校验
        if (cnt < 1 || cnt > 1999) {
            sb.append("实例数超出合法范围; ");
        }
        // sku校验
        for (String sku : skuNameList){
            ResultMsg msgSku = checkParam.checkVMSku(sku,region);
            if (!msgSku.getResult()) {
                sb.append(msgSku.getErrorMsg());
                sb.append("; ");
            }
            // 数据盘数量校验，默认为1，可空
            ResultMsg msgDiskCnt = checkParam.checkDataDiskCnt(region,sku,dataVolumeCount);
            if (!msgDiskCnt.getResult()) {
                sb.append(msgDiskCnt.getErrorMsg());
                sb.append("; ");
            }
        }

        //todo 暂时不校验sku的数量, 原因:为了兼容Azure其他系统只传一个sku的情况
        // if ("spot".equalsIgnoreCase(config.getInsMktType())){
        //     //校验一下数量
        //     List<VMSku> vmSkus = metaDataItemService.listVmSkuDistinct(region, splitSukName);
        //     if (vmSkus.size() < 3 || vmSkus.size() > 15) {
        //         msg.setResult(false);
        //         msg.setErrorMsg("多机型资源池sku数量小于3个或大于15个。");
        //         return msg;
        //     }
        // }
        // endregion

        // 数据盘类型校验
        ResultMsg msgDiskSku = checkParam.checkDiskSku(dataVolumeType,region);
        if (!msgDiskSku.getResult()) {
            sb.append(msgDiskSku.getErrorMsg());
            sb.append("; ");
        }

        // 数据盘大小数据校验
        ResultMsg msgDiskSize = checkParam.checkDataDiskSize(dataVolumeSize);
        if (!msgDiskSize.getResult()) {
            sb.append(msgDiskSize.getErrorMsg());
            sb.append("; ");
        }

        instanceGroupSkuCfg.setVmRole(vmRole);
        instanceGroupSkuCfg.setGroupName(groupName);

        // 为调用 adminApiService.addGroup() 参数补充 - confGroupElasticScalingData
        ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
        confGroupElasticScalingData.setClusterId(clusterId);
        confGroupElasticScalingData.setVmRole(vmRole);
        confGroupElasticScalingData.setGroupName(groupName);
        confGroupElasticScalingData.setMaxCount(100);
        confGroupElasticScalingData.setMinCount(0);
        confGroupElasticScalingData.setScalingRules(new ArrayList<>());
        instanceGroupData.setConfGroupElasticScalingData(confGroupElasticScalingData);

        // clusterCfgs 校验
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        Set<String> AppConfigClassifications = new HashSet<>();
        List<BaseReleaseAppsConfig> baseReleaseAppsConfigs = baseReleaseAppsConfigMapper.selectAll(confCluster.getClusterReleaseVer());
        if (!CollectionUtils.isEmpty(baseReleaseAppsConfigs)) {
            for (BaseReleaseAppsConfig baseReleaseAppsConfig : baseReleaseAppsConfigs) {
                AppConfigClassifications.add(baseReleaseAppsConfig.getAppConfigClassification());
            }
        }

        if (!CollectionUtils.isEmpty(clusterCfgs)) {
            for (SheinClusterCfg clusterCfg : clusterCfgs) {
                String confItemName = clusterCfg.getClassification();
                Map<String, Object> cfg = clusterCfg.getCfg();

                if (StringUtils.isBlank(confItemName) || !AppConfigClassifications.contains(confItemName)) {
                    sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                    sheinResponseModel.setMsg("参数：clusterConfigs.confItemName不合法");
                    return sheinResponseModel;
                }

                Set<String> confKeys = cfg.keySet();
                for (String confKey : confKeys) {
                    String value = cfg.get(confKey).toString();
                    if (StringUtils.isBlank(confKey) || StringUtils.isBlank(value) || confKey.length() > 300 || value.length() > 300) {
                        sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                        sheinResponseModel.setMsg("参数：clusterConfigs.confs, key 和 value 不能为空，且长度不能超过300");
                        return sheinResponseModel;
                    }
                }
            }
        }

        if (sb.length() > 0) {
            sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
            sheinResponseModel.setMsg(sb.toString());
        }
        return sheinResponseModel;
    }

    private Map<String, Object> getVmSkuData(String skuName,String region) {
        Map<String, Object> vmSkuData = new HashMap<>();
        ResultMsg resultMsg = checkParam.checkVMSku(skuName,region);
        if (resultMsg.isResult() && resultMsg.getData() != null) {
            try {
                Gson gson = new Gson();
                List<Map<String, Object>> vmSkuList = new ArrayList<>();
                vmSkuList = gson.fromJson(gson.toJson(resultMsg.getData()), vmSkuList.getClass());
                Double dVcpus = (double) vmSkuList.get(0).get("vCoreCount");
                Double dMemory = (double) vmSkuList.get(0).get("memoryGB");
                vmSkuData.put("vCPUs", dVcpus.intValue());
                vmSkuData.put("memoryGB", dMemory.intValue());
            } catch (Exception e) {
                getLogger().error("SheinApiServiceImpl.getVmSkuData checkVMSku vcpus memory error. resultMsg.data: {}, e: {}",
                        JSONObject.toJSONString(resultMsg.getData()), e);
            }
        } else {
            getLogger().error("SheinApiServiceImpl.getVmSkuData checkVMSku error. skuName: {}", skuName);
        }
        return vmSkuData;
    }

    /**
     * 查询spot买入逐出统计
     *
     * @param clusterId
     * @param skuName
     * @param endTime   yyyy-MM-dd HH:mm:ss
     * @param dc
     * @return
     */
    @Override
    public SheinResponseModel getSpotStatic(String clusterId, String skuName, String endTime, String dc) {
        //入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        getLogger().info("查询spot买入逐出统计参数：clusterId:{},skuName:{},endTime:{},dc:{}",clusterId,skuName,endTime,dc);
        SheinResponseModel sheinResponseModel = new SheinResponseModel();

        //region 构造参数
        Map<String,Object> spotSaleParam = new HashMap<>();
        Map<String,Object> spotEvictionParam = new HashMap<>();

        if (StringUtils.isNotEmpty(clusterId)){
            spotSaleParam.put("clusterId",clusterId.trim());
            spotEvictionParam.put("clusterId",clusterId.trim());
        }

        if (StringUtils.isNotEmpty(skuName)){
            ResultMsg skuck =checkParam.checkVMSku(skuName,dc);
            if (skuck.getResult()){
                spotSaleParam.put("skuName",skuName.trim());
                spotEvictionParam.put("skuName",skuName.trim());
            }else{
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg(skuck.getErrorMsg());
                return sheinResponseModel;
            }
        }

        if (StringUtils.isNotEmpty(endTime)){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date end_time = sdf.parse(endTime.trim());
                spotSaleParam.put("endTime",end_time);
                spotEvictionParam.put("endTime",end_time);
            }catch (Exception e){
                sheinResponseModel.setCode(SheinResponseModel.Request_ConditionError);
                sheinResponseModel.setMsg("endTime格式不正确,请使用：yyyy-MM-dd HH:mm:ss");
                return sheinResponseModel;
            }
        }

        if (StringUtils.isNotEmpty(dc)){
            spotSaleParam.put("dc",dc.trim());
            spotEvictionParam.put("dc",dc.trim());
        }

        //endregion

        //region 查询
        try {
            //region 查询买入
            List<Map> saleCount = infoClusterMapper.getSpotSaleCountByParam(spotSaleParam);
            getLogger().info("查询spot买入明细：" + saleCount);

            AtomicInteger saletotal= new AtomicInteger();
            saleCount.stream().forEach(item->{
                if (item.containsKey("count")){
                    Integer count = Integer.parseInt(item.get("count").toString());
                    if (count!=null) {
                        saletotal.addAndGet(count);
                    }
                }
            });
            //endregion

            //region 查询逐出
            List<Map> evictionCount = infoClusterMapper.getSpotEvictionCountByParam(spotSaleParam);
            getLogger().info("查询spot逐出明细：" + evictionCount);

            AtomicInteger evictiontotal= new AtomicInteger();
            evictionCount.stream().forEach(item->{
                if (item.containsKey("count")){
                    Integer count = Integer.parseInt(item.get("count").toString());
                    if (count!=null) {
                        evictiontotal.addAndGet(count);
                    }
                }
            });
            //endregion

            Map<String,Object> info= new HashMap<>();
            info.put("saleDetail",saleCount);
            info.put("evictionDetail",evictionCount);
            info.put("saleTotal",saletotal.get());
            info.put("evictionTotal", evictiontotal.get());
            sheinResponseModel.setInfo(info);
            sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            sheinResponseModel.setMsg("Success");
        }catch (Exception e){
            getLogger().error("查询spot买入逐出统计查询异常:",e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("查询异常，traceId:");
        }
        //endregion
        return sheinResponseModel;
    }

    /**
     * 更新弹性规则的实例数量范围
     *
     * @param elasticScalingData
     * @return
     */
    @Override
    public SheinResponseModel updateScaleVmScope(SheinElasticScalingData elasticScalingData) {
        SheinResponseModel responseModel=new SheinResponseModel();

        //region 检查数据合法性、有效性
        if (StringUtils.isEmpty(elasticScalingData.getClusterId()) ||
                StringUtils.isEmpty(elasticScalingData.getInsGpId()) ||
                elasticScalingData.getMaxCnt()==null ||
                elasticScalingData.getMinCnt() ==null
        ){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺少，请参考接口文档，补全参数。");
            return responseModel;
        }

        ConfClusterHostGroup group = confClusterHostGroupMapper.selectByPrimaryKey(elasticScalingData.getInsGpId());
        if (group==null){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("未查询到可用的Group，请核实insGpId是否正确。");
            return responseModel;
        }

        if (elasticScalingData.getMaxCnt()<= elasticScalingData.getMinCnt()){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("最大，最小范围数据异常，请核实。");
            return responseModel;
        }
        //endregion

        //region 查询对应的规则
        List<ConfGroupElasticScaling> elasticScalings =
                confGroupElasticScalingMapper.listByClusterIdAndGroupNameAndValid(elasticScalingData.getClusterId(),
                        group.getGroupName());
        if (elasticScalings == null || elasticScalings.size() ==0){
            responseModel.setCode(SheinResponseModel.Request_Failed);
            responseModel.setMsg("未找到有效的规则，请核实。");
            return responseModel;
        }
        //endregion

        ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
        confGroupElasticScalingData.setGroupName(group.getGroupName());
        confGroupElasticScalingData.setClusterId(elasticScalingData.getClusterId());
        confGroupElasticScalingData.setGroupEsId(elasticScalings.get(0).getGroupEsId());
        confGroupElasticScalingData.setMaxCount(elasticScalingData.getMaxCnt());
        confGroupElasticScalingData.setMinCount(elasticScalingData.getMinCnt());

        confGroupElasticScalingData.setIsGracefulScalein(elasticScalingData.getIsGracefulScalein());
        confGroupElasticScalingData.setScaleinWaitingTime(elasticScalingData.getScaleinWaitingTime());
        confGroupElasticScalingData.setEnableBeforestartScript(elasticScalingData.getEnableBeforestartScript());
        confGroupElasticScalingData.setEnableAfterstartScript(elasticScalingData.getEnableAfterstartScript());
        confGroupElasticScalingData.setIsFullCustody(elasticScalingData.getIsFullCustody());

        ResultMsg updateMsg = adminApiService.updateGroupElasticScaling(confGroupElasticScalingData);

        if (updateMsg.getResult()){
            responseModel.setCode(SheinResponseModel.Request_Success);
            responseModel.setMsg("Success");
        }else{
            responseModel.setCode(SheinResponseModel.Request_Failed);
            responseModel.setMsg(updateMsg.getErrorMsg());
        }
        return responseModel;
    }

    /**
     * 更新已经存在的弹性规则
     *
     * @param elasticScalingData
     * @return
     */
    @Override
    public SheinResponseModel updateEsRule(SheinElasticScalingRuleData elasticScalingData) {
        SheinResponseModel responseModel =new SheinResponseModel();

        //region 检查数据合法性有效性
        if(StringUtils.isEmpty(elasticScalingData.getEsRuleId()) || StringUtils.isEmpty(elasticScalingData.getEsRuleName()) ||
                elasticScalingData.getEnableAfterStartScript() ==null || elasticScalingData.getEnableBeforeStartScript() == null ||
                elasticScalingData.getPerScalingCnt() ==null || elasticScalingData.getAggregateType()==null||
                elasticScalingData.getScalingType() ==null || elasticScalingData.getLoadMetric()==null || elasticScalingData.getOperator()==null ||
                elasticScalingData.getRepeatCnt()==null ||  elasticScalingData.getFreezingTime()==null ||
                elasticScalingData.getThreshold() ==null || elasticScalingData.getWindowSize() ==null
        ){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺少，请参考接口文档，补全参数");
            return responseModel;
        }
        List<SheinElasticScalingRuleData>  ruleDataList = new ArrayList<>();
        ruleDataList.add(elasticScalingData);
        SheinResponseModel checkSRModel = checkScalingRulesParam(ruleDataList);
        if (!checkSRModel.getCode().equals(SheinResponseModel.Request_Success)) {
            return checkSRModel;
        }
        //endregion

        //region 构造保存的数据结构
        ConfGroupElasticScalingRuleData ruleData = new ConfGroupElasticScalingRuleData();
        ruleData.setEsRuleId(elasticScalingData.getEsRuleId());
        ruleData.setScalingType(elasticScalingData.getScalingType());
        ruleData.setEsRuleName(elasticScalingData.getEsRuleName());
        ruleData.setPerSalingCout(elasticScalingData.getPerScalingCnt());
        ruleData.setWindowSize(elasticScalingData.getWindowSize());
        ruleData.setThreshold(elasticScalingData.getThreshold());
        ruleData.setRepeatCount(elasticScalingData.getRepeatCnt());
        ruleData.setFreezingTime(elasticScalingData.getFreezingTime());
        ruleData.setEnableAfterstartScript(elasticScalingData.getEnableAfterStartScript());
        ruleData.setEnableBeforestartScript(elasticScalingData.getEnableBeforeStartScript());
        ruleData.setIsGracefulScalein(elasticScalingData.getIsGracefulScaleIn());
        ruleData.setIsValid(1);
        ruleData.setScaleinWaitingtime(elasticScalingData.getScaleInWaitingTime());


        ruleData.setLoadMetric(ES_LOAD_METRIC_ARR[elasticScalingData.getLoadMetric() - 1]);
        ruleData.setAggregateType(ES_AGGREGATE_TYPE_ARR[elasticScalingData.getAggregateType() - 1]);
        ruleData.setOperator(ES_OPERATOR_ARR[elasticScalingData.getOperator() - 1]);

        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = new ArrayList<>();
        confGroupElasticScalingRuleDataList.add(ruleData);
        //endregion

        //region 调用adminapisevice更新
        ConfGroupElasticScalingRule elasticScalingRule = confGroupElasticScalingRuleMapper.selectByPrimaryKey(elasticScalingData.getEsRuleId());
        if (elasticScalingRule!=null){
            ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
            confGroupElasticScalingData.setUserName("sheinAPI");
            confGroupElasticScalingData.setClusterId(elasticScalingRule.getClusterId());
            confGroupElasticScalingData.setGroupName(elasticScalingRule.getGroupName());
            confGroupElasticScalingData.setScalingRules(confGroupElasticScalingRuleDataList);

            ResultMsg resultMsg = adminApiService.updateElasticScalingRule(confGroupElasticScalingData);

            if (resultMsg.getResult()){
                responseModel.setCode(SheinResponseModel.Request_Success);
                responseModel.setMsg("Success");
            }else{
                responseModel.setCode(SheinResponseModel.Request_Failed);
                responseModel.setMsg(resultMsg.getErrorMsg());
            }
        }else{
            responseModel.setCode(SheinResponseModel.Request_Failed);
            responseModel.setMsg("规则ID有误，未查询到相关信息，请核实。");
        }

        return responseModel;
    }

    /**
     * 新增弹性规则
     *
     * @param addRuleData
     * @return
     */
    @Override
    public SheinResponseModel addEsRule(SheinElasticScalingAddRuleData addRuleData) {
        SheinResponseModel responseModel =new SheinResponseModel();

        //region 检查数据合法性有效性
        if (StringUtils.isEmpty(addRuleData.getClusterId()) || StringUtils.isEmpty(addRuleData.getGroupName()) ||
                StringUtils.isEmpty(addRuleData.getVmRole()) || addRuleData.getRule()==null){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺少，请参考接口文档，补全参数。请求参数："+JSON.toJSONString(addRuleData));
            return responseModel;
        }
        SheinElasticScalingRuleData elasticScalingData = addRuleData.getRule();
        if(StringUtils.isEmpty(elasticScalingData.getEsRuleName()) ||
                elasticScalingData.getEnableAfterStartScript() ==null || elasticScalingData.getEnableBeforeStartScript() == null ||
                elasticScalingData.getPerScalingCnt() ==null || elasticScalingData.getAggregateType()==null||
                elasticScalingData.getScalingType() ==null || elasticScalingData.getLoadMetric()==null || elasticScalingData.getOperator()==null ||
                elasticScalingData.getRepeatCnt()==null ||  elasticScalingData.getFreezingTime()==null ||
                elasticScalingData.getThreshold() ==null || elasticScalingData.getWindowSize() ==null
        ){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺少，请参考接口文档，补全参数，请求参数："+JSON.toJSONString(elasticScalingData));
            return responseModel;
        }

        if(elasticScalingData.getScalingType().equals(0)){
            if(elasticScalingData.getScaleInWaitingTime() ==null || elasticScalingData.getIsGracefulScaleIn() ==null)
            {
                responseModel.setCode(SheinResponseModel.Request_ConditionError);
                responseModel.setMsg("参数缺少，请参考接口文档，补全参数，请求参数："+JSON.toJSONString(elasticScalingData));
                return responseModel;
            }

            if (elasticScalingData.getScaleInWaitingTime().compareTo(scaleinWaitingMin)<0 ||
                    elasticScalingData.getScaleInWaitingTime().compareTo(scaleinWaitingMax)>0){
                responseModel.setCode(SheinResponseModel.Request_ConditionError);
                responseModel.setMsg("ScaleInWaitingTime,参数范围：["+scaleinWaitingMin+","+scaleinWaitingMax+"]");
                return responseModel;
            }
        }
        List<SheinElasticScalingRuleData>  ruleDataList = new ArrayList<>();
        ruleDataList.add(elasticScalingData);
        SheinResponseModel checkSRModel = checkScalingRulesParam(ruleDataList);
        if (!checkSRModel.getCode().equals(SheinResponseModel.Request_Success)) {
            return checkSRModel;
        }
        //endregion

        //region 构造保存的数据结构
        ConfGroupElasticScalingRuleData ruleData = new ConfGroupElasticScalingRuleData();
        String esRuleId= UUID.randomUUID().toString();
        ruleData.setEsRuleId(esRuleId);
        ruleData.setScalingType(elasticScalingData.getScalingType());
        ruleData.setEsRuleName(elasticScalingData.getEsRuleName());
        ruleData.setPerSalingCout(elasticScalingData.getPerScalingCnt());
        ruleData.setWindowSize(elasticScalingData.getWindowSize());
        ruleData.setThreshold(elasticScalingData.getThreshold());
        ruleData.setRepeatCount(elasticScalingData.getRepeatCnt());
        ruleData.setFreezingTime(elasticScalingData.getFreezingTime());
        ruleData.setEnableAfterstartScript(elasticScalingData.getEnableAfterStartScript());
        ruleData.setEnableBeforestartScript(elasticScalingData.getEnableBeforeStartScript());
        ruleData.setIsGracefulScalein(elasticScalingData.getIsGracefulScaleIn());
        ruleData.setIsValid(1);
        ruleData.setScaleinWaitingtime(elasticScalingData.getScaleInWaitingTime());


        ruleData.setLoadMetric(ES_LOAD_METRIC_ARR[elasticScalingData.getLoadMetric() - 1]);
        ruleData.setAggregateType(ES_AGGREGATE_TYPE_ARR[elasticScalingData.getAggregateType() - 1]);
        ruleData.setOperator(ES_OPERATOR_ARR[elasticScalingData.getOperator() - 1]);

        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = new ArrayList<>();
        confGroupElasticScalingRuleDataList.add(ruleData);

        ConfGroupElasticScalingData scalingData = new ConfGroupElasticScalingData();
        scalingData.setClusterId(addRuleData.getClusterId());
        scalingData.setGroupName(addRuleData.getGroupName());
        scalingData.setVmRole(addRuleData.getVmRole());
        scalingData.setScalingRules(confGroupElasticScalingRuleDataList);
        //endregion

        ResultMsg msg = adminApiService.postElasticScalingRule(scalingData);

        if (msg.getResult()){
            responseModel.setCode(SheinResponseModel.Request_Success);
            HashMap<String,String> map= new HashMap<>();
            map.put("esRuleId",esRuleId);
            responseModel.setInfo(map);
            return responseModel;
        }else{
            responseModel.setCode(SheinResponseModel.Request_Failed);
            responseModel.setMsg(msg.getErrorMsg());
            return responseModel;
        }
    }

    /**
     * 获取指定实例组正在运行和队列中的任务
     *
     * @param clusterId
     * @param groupName
     * @return
     */
    @Override
    public SheinResponseModel getPendingSaleTask(String clusterId, String groupName) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        List<Map> res = new CopyOnWriteArrayList<>();

        try {
            List<ConfScalingTask> taskList =
                    confScalingTaskNeoMapper.selectRunningOrInqueueTasksByClusterAndGroupName(clusterId, groupName);
            if (taskList!=null && taskList.size()>0){
                taskList.stream().forEach(x->{
                    Map<String,Object> taskMap= new HashMap<>();
                    taskMap.put("clusterId",x.getClusterId());
                    taskMap.put("taskId",x.getTaskId());
                    if (x.getInQueue().equals(1)){
                        taskMap.put("state","inqueue");
                    }else{
                        taskMap.put("state","running");
                    }
                    res.add(taskMap);
                });
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            }else{
                sheinResponseModel.setCode(SheinResponseModel.Request_Success);
            }
        }catch (Exception e){
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("查询数据异常。");
            getLogger().error("查询pending的task异常，",e);
        }
        sheinResponseModel.setInfo(res);
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel getToken(String time, String ak, String sign) {
        getLogger().info("getToken,{},{},{}",time,ak,sign);
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (StringUtils.isEmpty(time) || StringUtils.isEmpty(ak) || StringUtils.isEmpty(sign)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("参数不能为空。");
            return sheinResponseModel;
        }

        List<Map> res = new CopyOnWriteArrayList<>();
        Boolean timeCheck = DateUtil.check(time);
        if (!timeCheck) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Timeout);
            sheinResponseModel.setMsg("时间校验失败。");
            return sheinResponseModel;
        }
        Boolean aBoolean = checkAk(time, ak, sign);
        if (!aBoolean) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("sign校验失败。");
            return sheinResponseModel;
        }
        String tokenNew = generateToken(ak);
        String fTime = DateUtil.generateFutureTime();
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("accessToken", tokenNew);
        stringObjectHashMap.put("expiresIn", fTime);
        res.add(stringObjectHashMap);
        sheinResponseModel.setInfo(res);
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setMsg(SheinResponseModel.MSG_Success);
        return sheinResponseModel;
    }

    @Override
    public SheinResponseModel workOrderCallback(WorkorderCallbackRequest workorderCallbackRequest) {
        getLogger().info("workOrderCallback,workorderCallbackRequest:{}", workorderCallbackRequest);
        SheinResponseModel responseModel = new SheinResponseModel();
        responseModel = checkInParam(workorderCallbackRequest);
        if (!SheinResponseModel.Request_Success.equalsIgnoreCase(responseModel.getCode())) {
            return responseModel;
        }
        List<WorkOrderApprovalRequest> workOrderApprovalList = queryTicket(Collections.singletonList(workorderCallbackRequest.getTicket_id()));
        if (CollectionUtils.isEmpty(workOrderApprovalList)) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("ticketid错误。");
            return responseModel;
        }

        //查询表,获得clusterId, 以及当前是哪个动作,
        WorkOrderApprovalRequest currActionDb = getCurrAction(workOrderApprovalList);
        getLogger().info("workOrderCallback,workorderCallbackRequest:{},workOrderApprovalList:{},currActionDb:{}",
                workorderCallbackRequest, workOrderApprovalList, currActionDb);
        if (currActionDb == null || StringUtils.isEmpty(currActionDb.getClusterId())) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("此动作无效, 没有找到下一个动作。");
            return responseModel;
        }
        ConfCluster clu = queryCluster(currActionDb.getClusterId());
        Boolean res = checkCluster(clu);
        if (!res) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("集群未在审核中。");
            return responseModel;
        }
        String actionState = workorderCallbackRequest.getAction_state();
        if (APPROVAL_STATE_AGREE.equalsIgnoreCase(actionState)) {
            SheinResponseModel sheinResponseModel = dealAgree(workorderCallbackRequest, responseModel, currActionDb, clu);
            return sheinResponseModel;
        } else if (APPROVAL_STATE_REFUSE.equalsIgnoreCase(actionState)) {
            SheinResponseModel responseModel1 = dealRefuse(workorderCallbackRequest, responseModel, currActionDb);
            return responseModel1;
        }
        responseModel.setCode(SheinResponseModel.Request_ConditionError);
        responseModel.setMsg("请求异常,缺少状态。");
        return responseModel;
    }

    /**
     * 判断集群状态, 如果是创建审核中,/ 删除审核中的,   ,才可以进行下一步
     * @param clu
     * @return
     */
    private Boolean checkCluster(ConfCluster clu) {
         if(ConfCluster.CREATE_AUDITING==clu.getState() || ConfCluster.DELETE_AUDITING==clu.getState()|| ConfCluster.CREATE_AUDIT_REJECT==clu.getState()){
             return true;
         }
        return false;
    }

    private SheinResponseModel dealRefuse(WorkorderCallbackRequest workorderCallbackRequest, SheinResponseModel responseModel, WorkOrderApprovalRequest nextAction) {
        String clusterId = nextAction.getClusterId();
        Long id = nextAction.getId();
        String action = nextAction.getRequestType();
        //拒绝
        if (REQUEST_TYPE_CREATE.equalsIgnoreCase(action)) {
            // 如果是创建 拒绝 的审批后的回调
            updateClusterState(clusterId, ConfCluster.CREATE_AUDIT_REJECT);
            //更新集群 为  拒绝..
        } else if (REQUEST_TYPE_DESTORY.equalsIgnoreCase(action)) {
            //如果是销毁  拒绝 审批后的回调
            //更新集群 为  已创建..
            updateClusterState(clusterId, ConfCluster.CREATED);
        }
        // 不同意 拒绝...
        updateWorkerOrder(workorderCallbackRequest, id);
        responseModel.setCode(SheinResponseModel.Request_Success);
        responseModel.setMsg("成功。");
        return responseModel;
    }

    private SheinResponseModel dealAgree(WorkorderCallbackRequest workorderCallbackRequest, SheinResponseModel responseModel,
                                         WorkOrderApprovalRequest currActionDb, ConfCluster clu) {
        ResultMsg resultMsg = new ResultMsg();
        String clusterId = currActionDb.getClusterId();
        Long id = currActionDb.getId();
        String action = currActionDb.getRequestType();
        if (REQUEST_TYPE_CREATE.equalsIgnoreCase(action)) {
            // 更新集群状态
            updateClusterState(clusterId, ConfCluster.CREATING);
            // 如果是创建的审批后的回调,启动创建流程
            resultMsg = startCreateCluser(clu);
        } else if (REQUEST_TYPE_DESTORY.equalsIgnoreCase(action)) {
            //如果是销毁审批后的回调, 直接调用销毁方法
            resultMsg = startDestroyCluser(clu,currActionDb.getCreatedby());
        }
        if (resultMsg.getResult()) {
            // 创建/销毁成功
            updateWorkerOrder(workorderCallbackRequest, id);
            responseModel.setCode(SheinResponseModel.Request_Success);
            responseModel.setMsg("成功。");
            return responseModel;
        } else {
            // 创建/销毁失败
            getLogger().error("workOrderCallback, error,workorderCallbackRequest:{},workOrderApprovalList:{}", workorderCallbackRequest, currActionDb);
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("请求异常。");
            return responseModel;
        }
    }

    private SheinResponseModel checkInParam(WorkorderCallbackRequest workorderCallbackRequest) {
        SheinResponseModel responseModel = new SheinResponseModel();
        if (workorderCallbackRequest == null || workorderCallbackRequest.getTicket_id() == null || StringUtils.isEmpty(workorderCallbackRequest.getAction_state())) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数异常,缺少ticketId或actionState。");
            return responseModel;
        }
        responseModel.setCode(SheinResponseModel.Request_Success);
        return responseModel;
    }

    private void updateWorkerOrder(WorkorderCallbackRequest workorderCallbackRequest, Long id) {
        getLogger().info("updateWorkerOrder,workorderCallbackRequest:{},Id:{}",workorderCallbackRequest,id);
        WorkOrderApprovalRequest workOrderApprovalRequest = new WorkOrderApprovalRequest();
        workOrderApprovalRequest.setTicketId(String.valueOf(workorderCallbackRequest.getTicket_id()));
        workOrderApprovalRequest.setApprovalState(workorderCallbackRequest.getAction_state().toLowerCase());
        workOrderApprovalRequest.setModifiedTime(new Date());
        workOrderApprovalRequest.setId(id);
        workOrderApprovalRequest.setApprovalResult(workorderCallbackRequest.toString());
         workOrderApprovalRequestMapper.updateByPrimaryKeySelective(workOrderApprovalRequest);
    }


    /**
     * 获取当前动作,和当前要处理的数据,用来判断是要进行真正的创建还是真正的销毁
     * @param workOrderApprovalList 来自数据库
     * @return
     */
    private WorkOrderApprovalRequest getCurrAction(List<WorkOrderApprovalRequest> workOrderApprovalList) {
        // 有创建 && 没有销毁 动作, 应该销毁.       重试的场景, 打回... 会存入多条数据.每次的都要记录
        if (CollectionUtils.isEmpty(workOrderApprovalList)) {
            return null;
        }
        Set<String> createIds = new HashSet<>();
        Set<String> destroyIds = new HashSet<>();
        for (WorkOrderApprovalRequest request : workOrderApprovalList) {
            if (REQUEST_TYPE_CREATE.equalsIgnoreCase(request.getRequestType()) &&
                    (APPROVAL_STATE_INIT.equalsIgnoreCase(request.getApprovalState()) || APPROVAL_STATE_REFUSE.equalsIgnoreCase(request.getApprovalState()))) {
                createIds.add(String.valueOf(request.getId()));
            }
            if (REQUEST_TYPE_DESTORY.equalsIgnoreCase(request.getRequestType()) &&
                    (APPROVAL_STATE_INIT.equalsIgnoreCase(request.getApprovalState()) || APPROVAL_STATE_REFUSE.equalsIgnoreCase(request.getApprovalState()))) {
                destroyIds.add(String.valueOf(request.getId()));
            }
        }
        if (!CollectionUtils.isEmpty(createIds)) {
            //有创建
            ArrayList<String> list = new ArrayList<>(createIds);
            WorkOrderApprovalRequest workOrderApprovalRequest = workOrderApprovalList.stream()
                    .filter(vo -> String.valueOf(vo.getId()).equalsIgnoreCase(list.get(0)))
                    .collect(Collectors.toList()).get(0);
            return workOrderApprovalRequest;
        }
        if (!CollectionUtils.isEmpty(destroyIds)) {
            //有销毁
            ArrayList<String> list = new ArrayList<>(destroyIds);
            WorkOrderApprovalRequest workOrderApprovalRequest = workOrderApprovalList.stream()
                    .filter(vo -> String.valueOf(vo.getId()).equalsIgnoreCase(list.get(0)))
                    .collect(Collectors.toList()).get(0);
            return workOrderApprovalRequest;
        }
        return null;
    }

    private ConfCluster queryCluster(String clusterId) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        return confCluster;
    }

    private List<WorkOrderApprovalRequest> queryTicket(List<Long> ticketIdList) {
        if (CollectionUtils.isEmpty(ticketIdList)) {
            return new ArrayList<>();
        }
       // 把ticketIdList转成List<String>类型
        List<String> ticketIdListStr = new ArrayList<>();
        ticketIdList.forEach(x-> ticketIdListStr.add(x.toString()));
        List<WorkOrderApprovalRequest> req = workOrderApprovalRequestMapper.selectByTicketIds(ticketIdListStr);
        return req;
    }

    private ResultMsg startDestroyCluser(ConfCluster clu,String userName) {
        Map<String, String> map = new HashMap<>();
        map.put("clusterId", clu.getClusterId());
        map.put("callBackPass", "1");
        ResultMsg resultMsg = adminApiService.deleteCluster(JSON.toJSONString(map), userName);
        return resultMsg;
    }

    private ResultMsg startCreateCluser( ConfCluster clu) {
        // 查询cluster , 直接创建 模式
        AdminSaveClusterRequest adminSaveClusterRequest= new AdminSaveClusterRequest();
        adminSaveClusterRequest.setCreationMode( clu.getCreationMode());
        ConfCluster.CreationMode creationMode = adminSaveClusterRequest.getCreationMode(ConfCluster.CreationMode.DIRECTLY);
        ClusterCreationStrategy clusterCreationStrategy = clusterCreationStrategyFactory.create(creationMode);
        ResultMsg StartPlanRes = clusterCreationStrategy.createAndStartPlan(clu.getClusterId(), clu.getClusterReleaseVer(), creationMode);

        return StartPlanRes;
    }

    public String generateToken(String ak) {
        String s = "t_" + UUID.randomUUID().toString();
        String token = s.replaceAll("-", "");
        redisLock.save(token, ak, SheinParamConstant.TIME_24H);
        return token;
    }

    /**
     * 有效true. 签名字符串 = time+ak+sk
     * @param time
     * @param ak
     * @param sign
     * @return
     */
    public Boolean checkAk(String time, String ak, String sign) {
        List<ApiAuthKey> apiAuthKeys = authKeyMapper.selectAllByAk(ak);
//        检査该 ak 和 sk 对应的记录是否有效(检査 State)
//        2.4 校验签名是否有效
        if (CollectionUtils.isEmpty(apiAuthKeys)) {
            getLogger().info("apiAuthKeys，{}",apiAuthKeys);
            return false;
        }
        ApiAuthKey apiAuthKey = apiAuthKeys.get(0);
        String status = apiAuthKey.getStatus();
        if (!SheinParamConstant.VALID.equalsIgnoreCase(status)) {
            return false;
        }
        // 2.5 校验签名是否有效
        String s = time + ak + apiAuthKey.getSecretKey();
        String sha256StrJava = SHAUtil.getSHA256StrJava(s);
        if (!sign.equalsIgnoreCase(sha256StrJava)) {
            getLogger().info("sign:{},sha256StrJava:{}",sign,sha256StrJava);
            return false;
        }
        return true;
    }

    /**
     * 更新集群状态
     * @param clusterId
     * @param state
     */
    public void updateClusterState(String clusterId, Integer state) {
        getLogger().info("updateClusterState,clusterId:{},state:{}", clusterId, state);
        ConfCluster confCluster1 = new ConfCluster();
        confCluster1.setClusterId(clusterId);
        confCluster1.setState(state);
        confCluster1.setModifiedTime(new Date());
        confClusterMapper.updateByPrimaryKeySelective(confCluster1);
    }

    /**
     * 拆分 skuName
     * @return
     */
    public List<String> splitSukName(String skuName){
        List<String> stringList = StrUtil.splitTrim(skuName, ",");
        if (stringList.size()==1){
            stringList=StrUtil.splitTrim(skuName, "|");
        }
        return stringList;
    }

    /**
     * pv2磁盘调整
     * @param diskPerformance
     * @return
     */
    @Override
    public SheinResponseModel pv2DiskInfo(SheinDiskPerformance diskPerformance) {
        SheinResponseModel responseModel =new SheinResponseModel();
        String clusterId = diskPerformance.getClusterId();
        String insGpId = diskPerformance.getInsGpId();
        Integer iops = diskPerformance.getIops();
        Integer throughput = diskPerformance.getThroughput();
        //检查数据合法性有效性
        if (StrUtil.isEmpty(clusterId)
                || StrUtil.isEmpty(insGpId)
                || iops==null
                || throughput==null){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("参数缺少，请参考接口文档，补全参数。请求参数："+JSON.toJSONString(diskPerformance));
            return responseModel;
        }
        // 获取实例组相关信息
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByPrimaryKey(insGpId);
        if (confClusterHostGroup == null) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("无该实例组信息");
            return responseModel;
        }
        ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(clusterId, confClusterHostGroup.getVmRole(), confClusterHostGroup.getGroupName());
        if (confClusterVm == null) {
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg("无该实例组信息");
            return responseModel;
        }
        DiskPerformanceRequest request =new DiskPerformanceRequest();
        request.setClusterId(clusterId);
        request.setVmConfId(confClusterVm.getVmConfId());
        request.setNewDataDiskIOPSReadWrite(iops);
        request.setNewDataDiskMBpsReadWrite(throughput);
        ResultMsg msg = adminApiService.updateDiskIOPSAndThroughput(request);
        if (!msg.isSuccess()){
            responseModel.setCode(SheinResponseModel.Request_ConditionError);
            responseModel.setMsg(msg.getMsg());
            return responseModel;
        }
        responseModel.setCode(SheinResponseModel.Request_Success);
        return responseModel;

    }
    /**
     * 获取VM实例详情信息
     * @param vmInstanceRequest
     * @return
     */
    @Override
    public SheinResponseModel vmInstanceDetail(VmInstanceDetailRequest vmInstanceRequest) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        String dc = vmInstanceRequest.getDc();
        String clusterId = vmInstanceRequest.getClusterId();
        //数据中心入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        String ip = vmInstanceRequest.getIp();
        String vmName = vmInstanceRequest.getVmName();
        String hostName = vmInstanceRequest.getHostName();
        if (StrUtil.isEmpty(ip) && StrUtil.isEmpty(vmName) && StrUtil.isEmpty(hostName)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("缺少参数,vmName,hostName,ip三选一");
            return sheinResponseModel;
        }
        List<VmInstanceDetailResponse> vmInstanceDetailResponses = selectVmInstanceDetail(dc, clusterId, vmName, hostName, ip,null,null);
        if (CollUtil.isNotEmpty(vmInstanceDetailResponses)) {
            if (StrUtil.isNotEmpty(ip)) {
                //如果是按IP查，只返回运行中的一条，如果没有运行中的，就返回空
                Optional<VmInstanceDetailResponse> instanceDetailResponse = vmInstanceDetailResponses
                        .stream()
                        .filter(vmInstanceDetailResponse -> vmInstanceDetailResponse.getState() == InfoClusterVm.VM_RUNNING)
                        .findFirst();
                instanceDetailResponse.ifPresent(sheinResponseModel::setInfo);
            } else {
                sheinResponseModel.setInfo(vmInstanceDetailResponses.get(0));
            }
        }
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        return sheinResponseModel;
    }

    /**
     * 获取集群中VM实例列表
     *
     * @param vmInstanceRequest
     * @return
     */
    @Override
    public SheinResponseModel vmInstancesByClusterId(VmInstanceDetailRequest vmInstanceRequest) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        String dc = vmInstanceRequest.getDc();
        String clusterId = vmInstanceRequest.getClusterId();
        Integer state = vmInstanceRequest.getState();
        String groupName = vmInstanceRequest.getGroupName();
        if (StrUtil.isEmpty(clusterId)) {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("缺少clusterId");
            return sheinResponseModel;
        }
        //数据中心入参映射
        dc = RegionMappingUtil.mappingIn(dc);
        //拆分groupNames
        List<String> groupNameList = StrUtil.split(groupName, ",",true,true);
        List<VmInstanceDetailResponse> vmInstanceDetailResponses = selectVmInstanceDetail(dc, clusterId, null, null, null, state, groupNameList);
        sheinResponseModel.setCode(SheinResponseModel.Request_Success);
        sheinResponseModel.setInfo(vmInstanceDetailResponses);
        return sheinResponseModel;
    }

    private List<VmInstanceDetailResponse> selectVmInstanceDetail(String dc, String clusterId, String vmName, String hostName, String ip,Integer state ,List<String> groupNameList) {
        List<HashMap<String, Object>> mapList = infoClusterVmMapper.selectVmInstanceDetail(dc, clusterId, vmName, hostName, ip,state,groupNameList);
        Set<String> vmConfIds = mapList.stream().map(vm -> Convert.toStr(vm.get("vm_conf_id"))).collect(Collectors.toSet());
        Map<String, ConfClusterVmDataVolume> dataValumeMap = getDataValumeMap(vmConfIds);
        return mapList.stream().map(vm -> {
            VmInstanceDetailResponse response = new VmInstanceDetailResponse();
            response.setRegion(RegionMappingUtil.mappingOut(Convert.toStr(vm.get("region"))));
            response.setClusterId(Convert.toStr(vm.get("cluster_id")));
            response.setClusterName(Convert.toStr(vm.get("cluster_name")));
            response.setVmName(Convert.toStr(vm.get("vm_name")));
            response.setHostName(Convert.toStr(vm.get("host_name")));
            response.setIp(Convert.toStr(vm.get("internalIp")));
            response.setVmRole(Convert.toStr(vm.get("vm_role")));
            response.setGroupName(Convert.toStr(vm.get("group_name")));
            response.setSkuName(Convert.toStr(vm.get("sku_name")));
            response.setPurchaseType(Convert.toInt(vm.get("purchase_type")));
            response.setState(Convert.toInt(vm.get("state")));
            response.setCreateTime(Convert.toDate(vm.get("create_begtime")));
            //磁盘信息,默认取第一个
            ConfClusterVmDataVolume confClusterVmDataVolume = dataValumeMap.get(Convert.toStr(vm.get("vm_conf_id")));
            if (confClusterVmDataVolume!=null) {
                response.setDiskType(confClusterVmDataVolume.getDataVolumeType());
                response.setDiskSize(confClusterVmDataVolume.getDataVolumeSize());
            }
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 获取实例组中的磁盘信息,转换成map,key=VmConfId
     * @param vmConfIds
     * @return
     */
    private Map<String, ConfClusterVmDataVolume> getDataValumeMap(Set<String> vmConfIds) {
        List<ConfClusterVmDataVolume> clusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfIds(vmConfIds);
        return clusterVmDataVolumes.stream().collect(Collectors.toMap(ConfClusterVmDataVolume::getVmConfId, Function.identity()));
    }

    public SheinResponseModel fullCustodyControl(FullCustodyRequest request){
        SheinResponseModel response = new SheinResponseModel();
        response.setCode(SheinResponseModel.Request_Success);
        response.setMsg("成功");

        ConfGroupElasticScaling groupElasticScaling = null;
        try {
            // 校验参数
            checkFullcustodyControllArguments(request);

            //
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(request.getClusterId());
            Assert.notNull(confCluster, "集群不存在: clusterId={}", request.getClusterId());

            groupElasticScaling = confGroupElasticScalingMapper.selectByClusterIdAndGroupName(request.getClusterId(), request.getGroupName());
            Assert.notNull(groupElasticScaling, "实例组弹性扩缩容配置不存在: clusterId={}, groupName={}",
                    request.getClusterId(), request.getGroupName());
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            response.setCode(SheinResponseModel.Request_Failed);
            response.setMsg(ex.getMessage());
            return response;
        }

        // 更新配置值
        if (StrUtil.equals(request.getFullCustodyState(), "ENABLED")) {
            groupElasticScaling.setIsFullCustody(1);
            groupElasticScaling.setEnableAfterstartScript(request.getEnableAfterstartScript());
            groupElasticScaling.setEnableBeforestartScript(request.getEnableBeforestartScript());
            groupElasticScaling.setIsGracefulScalein(request.getIsGracefulScalein());
            groupElasticScaling.setScaleinWaitingTime(request.getScaleinWaitingTime());
            if (Objects.nonNull(request.getFullCustodyParam())) {
                groupElasticScaling.setFullCustodyParam(JSON.toJSONString(request.getFullCustodyParam()));
            } else {
                groupElasticScaling.setFullCustodyParam("");
            }
            confGroupElasticScalingMapper.updateByPrimaryKey(groupElasticScaling);
        } else if (StrUtil.equals(request.getFullCustodyState(), "DISABLED")) {
            groupElasticScaling.setIsFullCustody(0);
            groupElasticScaling.setModifiedTime(new Date());
            confGroupElasticScalingMapper.updateByPrimaryKey(groupElasticScaling);
        }
        return response;
    }

    private static void checkFullcustodyControllArguments(FullCustodyRequest request) {
        Assert.notBlank(request.getClusterId(), "集群ID(clusterId)不能为空");
        Assert.notBlank(request.getGroupName(), "实例组名称(groupName)不能为空");
        Assert.notBlank(request.getFullCustodyState(), "托管状态(fullCustodyState)不能为空");
        if (StrUtil.equals(request.getFullCustodyState(), "DISABLED")) {
            return;
        }
        Assert.notNull(request.getEnableBeforestartScript(), "是否执行启动前脚本参数(enableBeforestartScript)不能为空");
        Assert.notNull(request.getEnableAfterstartScript(), "是否执行启动后脚本参数(enableAfterstartScript)不能为空");
        if (Objects.nonNull(request.getIsGracefulScalein())) {
            Assert.notNull(request.getScaleinWaitingTime(), "开启优雅缩容后,优雅缩容等待时间(scaleinWaitingTime)不能为空");
        }
        if (Objects.nonNull(request.getFullCustodyParam())) {
            String metric = request.getFullCustodyParam().getScaleoutMetric();
            Assert.isTrue(StrUtil.isBlank(metric) || StrUtil.equals(metric, "App") || StrUtil.equals(metric, "Container"),
                    "扩容监控指标(fullCustodyParam.scaleoutMetric)只能是:空,App, Container中的一个，请求值为：" + metric);
            if (Objects.nonNull(request.getFullCustodyParam().getScaleinMemoryThreshold())) {
                Assert.checkBetween(request.getFullCustodyParam().getScaleinMemoryThreshold(), 1, 100,
                        "缩容内存百分比阈值(fullCustodyParam.scaleinMemoryThreshold)取值范围只能是1-100, 请求值为:"
                                + request.getFullCustodyParam().getScaleinMemoryThreshold());
            }
        }
    }
}
