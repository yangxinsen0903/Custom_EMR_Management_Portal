package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.sunbox.constant.BizConfigConstants;
import com.sunbox.dao.mapper.*;
import com.sunbox.dao.mapper.ConfClusterVmDataVolumeMapper;
import com.sunbox.dao.mapper.ConfGroupElasticScalingMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.DiskPerformanceRequest;
import com.sunbox.domain.enums.*;
import com.sunbox.domain.metaData.ManagedIdentity;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.domain.metaData.keyVault;
import com.sunbox.sdpadmin.core.util.CookieUtil;
import com.sunbox.sdpadmin.core.util.JacksonUtil;
import com.sunbox.sdpadmin.enums.ConfigClassification;
import com.sunbox.sdpadmin.mapper.*;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupAppsConfigMapper;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpadmin.model.ParamKey;
import com.sunbox.sdpadmin.model.admin.request.ConfClusterScript;
import com.sunbox.sdpadmin.model.admin.request.*;
import com.sunbox.sdpadmin.model.admin.response.*;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.ICheckParam;
import com.sunbox.sdpadmin.strategy.ClusterCreationStrategy;
import com.sunbox.sdpadmin.strategy.ClusterCreationStrategyFactory;
import com.sunbox.sdpadmin.util.OperationPlanUtils;
import com.sunbox.sdpservice.data.compose_cloud.ScaleInForDeleteTaskVmReq;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdpservice.service.ScaleService;
import com.sunbox.service.*;
import com.sunbox.service.BizConfigService;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IConfHostGroupVmSkuService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.consts.CommonConstant;
import com.sunbox.service.consts.ComposeConstant;
import com.sunbox.service.consts.DestroyStatusConstant;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.util.*;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class AdminApiServiceImpl implements AdminApiService, BaseCommonInterFace {

    public static final int LOG_FLAG_ONE_DAY_CHANG = 3;
    /**
     * Ambari数据库名是否手动设置，默认为false
     */
    @Value("${ambari.dbname.manual:false}")
    private String ambariDbNameManual;

    @Value("${sdp.region}")
    private String sdpRegion;

    @Value("${sdp.dns.ttl:3600}")
    private Integer sdp_dns_ttl;

    @Value("${sdp.spot.release.time:2023-03-27}")
    private String sdp_spot_release_time;

    @Value("${sdp.scalein.waiting.min:60}")
    private Integer scaleinWaitingMin;

    @Value("${sdp.scalein.waiting.max:1800}")
    private Integer scaleinWaitingMax;

//    @Autowired
//    private CreateClusterRateLimiter createClusterRateLimiter;

    public static final int INSTANCE_GROUP_CORE_TOTAL_COUNT = 1000;

    public static final int INSTANCE_GROUP_TASK_TOTAL_COUNT = 1999;

    private List<String> mustExistTags = Arrays.asList("svcid", "svc", "service", "for");

    @Value("${hive.db.check:false}")
    private String hivecheck;

    @Resource
    private BaseReleaseVersionMapper baseReleaseVersionMapper;

    @Resource
    private BaseReleaseAppsMapper baseReleaseAppsMapper;

    @Resource
    private BaseReleaseAppsConfigMapper baseReleaseAppsConfigMapper;

    @Resource
    private InfoClusterMapper infoClusterMapper;

    @Resource
    private ConfClusterTagMapper confClusterTagMapper;

    @Resource
    private BaseDictionaryMapper baseDictionaryMapper;

    @Resource
    private ConfClusterAppsConfigMapper confClusterAppsConfigMapper;

    @Resource
    private ConfClusterHostGroupAppsConfigMapper confClusterHostGroupAppsConfigMapper;

    @Resource
    private ConfClusterMapper confClusterMapper;

    @Resource
    private ConfClusterScriptMapper confClusterScriptMapper;

    @Resource
    private ConfClusterVmNeoMapper confClusterVmNeoMapper;

    @Resource
    private ConfClusterVmDataVolumeMapper confClusterVmDataVolumeMapper;

    @Resource
    private ConfKeypairMapper confKeypairMapper;

    @Resource
    private ConfClusterAppMapper confClusterAppMapper;

    @Resource
    private BaseReleaseVmImgMapper baseReleaseVmImgMapper;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private ScaleService scaleService;

    @Resource
    private ConfTagKeysMapper confTagKeysMapper;

    @Resource
    private InfoClusterOperationPlanMapper infoClusterOperationPlanMapper;

    @Resource
    private InfoClusterOperationPlanActivityLogMapper infoClusterOperationPlanActivityLogMapper;

    @Resource
    private BaseSceneMapper baseSceneMapper;

    @Resource
    private BaseSceneAppsMapper baseSceneAppsMapper;

    @Resource
    private BaseScriptMapper baseScriptMapper;

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    private DistributedRedisLock redisLock;


    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    ConfGroupElasticScalingMapper confGroupElasticScalingMapper;

    @Autowired
    ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    ConfScalingVmMapper confScalingVmMapper;

    @Autowired
    private ConfGroupElasticScalingRuleMapper confGroupElasticScalingRuleMapper;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private ICheckParam checkParam;

    @Autowired
    private ConfScalingTaskNeoMapper scalingTaskMapper;

    @Autowired
    InfoGroupElasticScalingRuleLogMapper infoGroupElasticScalingRuleLogMapper;

    @Value("${admin.message.clientname}")
    private String clientname;

    @Value("${core.scalein.max.count:3}")
    private Integer scaleInMaxCount;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    ClusterCreationStrategyFactory clusterCreationStrategyFactory;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private InfoThirdApiFailedLogMapper thirdApiFailedLogMapper;

    @Autowired
    private OsImageMapper osImageMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    @Autowired
    private ClusterDestroyTaskMapper clusterDestroyTaskMapper;

    @Autowired
    private BizConfigService bizConfigService;

    @Autowired
    private IUserInfoService userInfoService;

    @Autowired
    private IClusterInfoService clusterInfoService;

    @Autowired
    private IConfHostGroupVmSkuService confHostGroupVmSkuService;

    @Autowired
    private WorkOrderApprovalRequestMapper workOrderApprovalRequestMapper;

    @Autowired
    private HttpServletRequest HttpRequest;

    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    @Autowired
    private IAzurePriceService azurePriceService;

    @Autowired
    private IConfScalingTaskService confScalingTaskService;

    /**
     * 集群概览
     */
    @Override
    public ResultMsg clusterOverview() {
        ResultMsg resultMsg = new ResultMsg();
//        List<Map> clusterStateList = confClusterMapper.selectStateGroupByState();

        Map<String, String> params = new HashMap<>();
        List<Map> infoClusterMaps = infoClusterMapper.selectByObject(params);

        Map<String, Integer> stateMap = new HashMap<>();
        stateMap.put("0", 0);
        stateMap.put("1", 0);
        stateMap.put("2", 0);
        stateMap.put("-1", 0);
        stateMap.put("-2", 0);
        stateMap.put("-9", 0);
        stateMap.put("-3", 0);
        stateMap.put("3", 0);
        stateMap.put("-5", 0);
        stateMap.put("4", 0);
        //stateMap.put("-6", 0);

        if (null != infoClusterMaps && infoClusterMaps.size() > 0) {
            for (Map map : infoClusterMaps) {
                if (map.containsKey("state")) {
                    String state = map.get("state").toString();
                    switch (state) {
                        case "0":
                            stateMap.put("0", stateMap.get("0") + 1);
                            break;
                        case "1":
                            stateMap.put("1", stateMap.get("1") + 1);
                            break;
                        case "2":
                            stateMap.put("2", stateMap.get("2") + 1);
                            break;
                        case "-1":
                            stateMap.put("-1", stateMap.get("-1") + 1);
                            break;
                        case "-2":
                            stateMap.put("-2", stateMap.get("-2") + 1);
                            break;
                        case "-9":
                            stateMap.put("-9", stateMap.get("-9") + 1);
                            break;
                        case "-3":
                            stateMap.put("-3", stateMap.get("-3") + 1);
                            break;
                        case "-4":
                            stateMap.put("-4", stateMap.get("-4") + 1);
                            break;
                        case "3":
                            stateMap.put("3", stateMap.get("3") + 1);
                            break;
                        case "-5":
                            stateMap.put("-5", stateMap.get("-5") + 1);
                            break;
                        case "4":
                            stateMap.put("4", stateMap.get("4") + 1);
                            break;
//                        case "-6":
//                            stateMap.put("-6", stateMap.get("-6") + 1);
//                            break;
                    }
                }
            }
        }

        // 数据转换
        List<Map<String, Integer>> clusterStateList = new ArrayList<>();
        Set<String> stateKeySet = stateMap.keySet();
        for (String stateKey : stateKeySet) {
            Map<String, Integer> clusterStateMap = new HashMap<>();
            clusterStateMap.put("state", Integer.valueOf(stateKey));
            clusterStateMap.put("state_count", stateMap.get(stateKey));
            clusterStateList.add(clusterStateMap);
        }

        if (!CollectionUtils.isEmpty(clusterStateList)) {
            resultMsg.setResult(true);
            resultMsg.setData(clusterStateList);
        } else {
            resultMsg.setResult(false);
            resultMsg.setMsg("集群概览数据获取失败.");
        }
        return resultMsg;
    }

    @Override
    public ResultMsg queryClusterList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        Map params = new HashMap();
        params.put("clusterId", jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : null);
        params.put("clusterName", jsonObject.containsKey("clusterName") ? jsonObject.getString("clusterName") : null);
        params.put("region", jsonObject.getString("region"));
        // params.put("state",jsonObject.containsKey("state") ? jsonObject.getInteger("state") : null);

        // state支持多选查询
        Set<Integer> stateSet = new HashSet<>();
        if (jsonObject.containsKey("state") && StringUtils.isNotBlank(jsonObject.get("state").toString())) {
            JSONArray states = jsonObject.getJSONArray("state");
            for (Object state : states) {
                stateSet.add(Integer.valueOf(state.toString()));
            }
        }
        if (!CollectionUtils.isEmpty(stateSet)) {
            params.put("states", stateSet);
        }

        params.put("clusterTag", jsonObject.containsKey("clusterTag") ? jsonObject.getString("clusterTag") : null);
        Integer pageIndex = jsonObject.containsKey("pageIndex") ? jsonObject.getInteger("pageIndex") : 1;
        Integer pageSize = jsonObject.containsKey("pageSize") ? jsonObject.getInteger("pageSize") : 10;
        params.put("pageSize", pageSize);
        params.put("pageIndex", (pageIndex - 1) * (pageSize));
        String tagValue = jsonObject.containsKey("tagValue") ? jsonObject.getString("tagValue") : null;
        List<String> clusterIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(tagValue)) {
            ConfClusterTag confClusterTag = new ConfClusterTag();
            confClusterTag.setTagVal(tagValue);
            List<ConfClusterTag> confClusterTagList = confClusterTagMapper.selectByObject(confClusterTag);
            if (null != confClusterTagList && confClusterTagList.size() > 0) {
                for (ConfClusterTag cct : confClusterTagList) {
                    clusterIds.add(cct.getClusterId());
                }
                params.put("clusterIds", clusterIds);
            } else {
                return this.returnResultMsg(true, "[]", "success", 0, null);
            }
        }
        List<Map> infoClusterMaps = infoClusterMapper.selectByObject(params);
        Integer count = infoClusterMapper.countByObject(params);
        List<Map> infoClusterData = new ArrayList<>();
        if (null != infoClusterMaps && infoClusterMaps.size() > 0) {
            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            for (Map map : infoClusterMaps) {
                Map param = new HashMap();
                param.put("clusterId", map.containsKey("cluster_id") ? map.get("cluster_id") : "-");
                param.put("clusterName", map.containsKey("cluster_name") ? map.get("cluster_name") : "-");
                Integer state2 = map.containsKey("state") ? Integer.valueOf(map.get("state").toString()) : -3;
                if (state2 > -3) {
                    //0 待创建 1 创建中 2 已创建 -1释放中 -2 已释放
                    switch (state2) {
                        case 0:
                            param.put("stateName", "待创建");
                            break;
                        case 1:
                            param.put("stateName", "创建中");
                            break;
                        case 2:
                            param.put("stateName", "已创建");
                            break;
                        case -1:
                            param.put("stateName", "销毁中");
                            break;
                        case -2:
                            param.put("stateName", "已销毁");
                            break;
                    }
                }
                param.put("state", map.containsKey("state") ? map.get("state") : "-");
                param.put("clusterMonitor", map.containsKey("clusterMonitor") ? map.get("clusterMonitor") : "-");
                ConfClusterTag confClusterTag = new ConfClusterTag();
                confClusterTag.setClusterId(param.get("clusterId").toString());
                List<ConfClusterTag> confClusterTags = confClusterTagMapper.selectByObject(confClusterTag);
                param.put("clusterTag", confClusterTags);
                param.put("deleteProtected", map.containsKey("delete_protected") ? map.get("delete_protected") : "0");
                Integer isHa = map.containsKey("is_ha") ? Integer.valueOf(map.get("is_ha").toString()) : -1;
                param.put("ambariIp", map.containsKey("ambari_host") ? map.get("ambari_host") : "-");
                param.put("masterIps", map.containsKey("master_ips") ? map.get("master_ips") : "-");
                param.put("masterNodeNum", map.containsKey("master_vms_count") ? map.get("master_vms_count") : "-");
                param.put("ambariNodeNum", map.containsKey("ambari_count") ? map.get("ambari_count") : "-");
                param.put("coreNodeNum", map.containsKey("core_vms_count") ? map.get("core_vms_count") : "-");
                param.put("taskNodeNum", map.containsKey("task_vms_count") ? map.get("task_vms_count") : "-");
//                param.put("ambariNodeNum",map.containsKey("ambari_count") ? map.get("ambari_count") : "-");
                param.put("serviceNum", map.containsKey("apps_count") ? map.get("apps_count") : "-");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                param.put("createdTime", map.containsKey("created_time") ? sdf.format(map.get("created_time")) : "-");
                param.put("creationSubState", map.get("creation_sub_state"));
                //数据中心
                param.put("region", map.get("region"));
                param.put("regionName",regionMap.get(map.get("region")));
                infoClusterData.add(param);
            }
        }
        return this.returnResultMsg(true, infoClusterData, "success", count, null);
    }

    @Override
    public ResultMsg getReleases() {
        List<BaseReleaseVersion> baseReleaseVersions = baseReleaseVersionMapper.selectAll();
        return this.returnResultMsg(true, baseReleaseVersions, "success", null, null);
    }

    @Override
    public ResultMsg getReleaseApps(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String releaseVersion = jsonObject.containsKey("releaseVersion") ? jsonObject.getString("releaseVersion") : null;

        Map params = new HashMap<>();
        params.put("releaseLabelPrefix", releaseVersion);
        List<BaseReleaseApps> baseReleaseApps = baseReleaseAppsMapper.selectByObject(params);
        return this.returnResultMsg(true, baseReleaseApps, "success", null, null);
    }

    @Override
    public ResultMsg getSceneApps(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String releaseVersion = jsonObject.containsKey("releaseVersion") ? jsonObject.getString("releaseVersion") : null;
        String scene = jsonObject.getString("scene");
        if (StringUtils.isBlank(scene)) {
            scene = "DEFAULT";
        }
        scene = scene.toUpperCase();

        BaseScene baseScene = baseSceneMapper.queryByReleaseVerAndSceneName(releaseVersion, scene);
        // 没查到场景数据，返回空列表
        if (Objects.isNull(baseScene)) {
            getLogger().info("没查询到场景对象。releaseVer={}, scene={}", releaseVersion, scene);
            this.returnResultMsg(true, new ArrayList<>(), "success", null, null);
        }

        List<BaseSceneApps> baseSceneApps = baseSceneAppsMapper.queryBySceneId(baseScene.getSceneId());

        return this.returnResultMsg(true, baseSceneApps, "success", null, null);
    }

    @Override
    public ResultMsg getReleaseConfigList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String releaseVersion = jsonObject.containsKey("releaseVersion") ? jsonObject.getString("releaseVersion") : null;
        Map params = new HashMap<>();
        params.put("releaseVersion", releaseVersion);
        List<BaseReleaseAppsConfig> baseReleaseAppsConfigs = baseReleaseAppsConfigMapper.selectByObject(params);
        return this.returnResultMsg(true, baseReleaseAppsConfigs, "success", null, null);
    }

    private List<BaseDictionary> getBaseDictionaryByAliasName(String aliasName) {
        BaseDictionary baseDictionary = new BaseDictionary();
        baseDictionary.setAliasName(aliasName);
        List<BaseDictionary> baseDictionaries = baseDictionaryMapper.selectBaseDictionary(baseDictionary);
        return baseDictionaries;
    }

    @Override
    public ResultMsg getAzureDiskSkuList(String region) {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        ResultMsg resultMsg = azureService.getDiskSku(region);
        getLogger().info("getKeyPairList getAzureDiskSkuList" + JSON.toJSONString(resultMsg));
        if (null != resultMsg && resultMsg.getResult()) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
            if (null != jsonArray && jsonArray.size() > 0) {
                for (Object obj : jsonArray) {
                    BaseDictionary baseDictionary = new BaseDictionary();
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
                    baseDictionary.setDictName(name);
                    baseDictionary.setDictValue(name);
                    baseDictionaries.add(baseDictionary);
                }
            }
        }
        return this.returnResultMsg(true, baseDictionaries, "success", baseDictionaries.size(), null);
    }

    @Override
    public ResultMsg getClassificationList(String releaseVersion) {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        // 此处修改为从表中查询,便于后续的维护

        // 从枚举中遍历
//        for (ConfigClassification classification : ConfigClassification.values()) {
//            BaseDictionary dict = new BaseDictionary();
//            dict.setDictName(classification.getClassification());
//            dict.setDictValue(classification.getClassification());
//            baseDictionaries.add(dict);
//        }
        releaseVersion = StrUtil.isBlank(releaseVersion)? ReleaseVersion.SDP_1_0.getVersionValue() : releaseVersion;
        List<BaseReleaseAppsConfig> baseReleaseAppsConfigs = baseReleaseAppsConfigMapper.selectAll(releaseVersion);
        if (null != baseReleaseAppsConfigs && baseReleaseAppsConfigs.size() > 0) {
            for (BaseReleaseAppsConfig brac : baseReleaseAppsConfigs) {
                BaseDictionary baseDictionary = new BaseDictionary();
                baseDictionary.setDictName(brac.getAppConfigClassification());
                baseDictionary.setDictValue(brac.getAppConfigClassification());
                baseDictionaries.add(baseDictionary);
            }
        }
        return this.returnResultMsg(true, baseDictionaries, "success", null, null);
    }

    @Override
    public ResultMsg getNetWorkList() {
        String aliasName = "NetWork";
        List<BaseDictionary> baseDictionaries = this.getBaseDictionaryByAliasName(aliasName);
        return this.returnResultMsg(true, baseDictionaries, "success", null, null);
    }

    @Override
    public ResultMsg getPrimarySecurityGroupList(String region) {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        ResultMsg resultMsg = azureService.getNSGSku(region);
        getLogger().info("getKeyPairList getPrimarySecurityGroupList" + JSON.toJSONString(resultMsg));
        if (null != resultMsg && resultMsg.getResult()) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
            if (null != jsonArray && jsonArray.size() > 0) {
                for (Object obj : jsonArray) {
                    BaseDictionary baseDictionary = new BaseDictionary();
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
                    String keyVaultResourceId = jsonObject.containsKey("resourceId") ? jsonObject.getString("resourceId") : "";
                    baseDictionary.setDictName(name);
                    baseDictionary.setDictValue(keyVaultResourceId);
                    baseDictionaries.add(baseDictionary);
                }
            }
        }
        return this.returnResultMsg(true, baseDictionaries, "success", baseDictionaries.size(), null);
    }

    @Override
    public ResultMsg getSubSecurityGroupList(String region) {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        ResultMsg resultMsg = azureService.getNSGSku(region);
        getLogger().info("getKeyPairList getSubSecurityGroupList" + JSON.toJSONString(resultMsg));
        if (null != resultMsg && resultMsg.getResult()) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
            if (null != jsonArray && jsonArray.size() > 0) {
                for (Object obj : jsonArray) {
                    BaseDictionary baseDictionary = new BaseDictionary();
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
                    String keyVaultResourceId = jsonObject.containsKey("resourceId") ? jsonObject.getString("resourceId") : "";
                    baseDictionary.setDictName(name);
                    baseDictionary.setDictValue(keyVaultResourceId);
                    baseDictionaries.add(baseDictionary);
                }
            }
        }
        return this.returnResultMsg(true, baseDictionaries, "success", baseDictionaries.size(), null);
    }

    @Override
    public ResultMsg getKeyPairList(String region) {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        ResultMsg resultMsg = azureService.getSSHKeyPair(region);
        getLogger().info("getKeyPairList return" + JSON.toJSONString(resultMsg));
        if (null != resultMsg && resultMsg.getResult()) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
            if (null != jsonArray && jsonArray.size() > 0) {
                for (Object obj : jsonArray) {
                    BaseDictionary baseDictionary = new BaseDictionary();
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
                    String publicKeySecretName = jsonObject.containsKey("publicKeySecretName") ? jsonObject.getString("publicKeySecretName") : "";
                    baseDictionary.setDictName(name);
                    baseDictionary.setDictValue(publicKeySecretName);
                    baseDictionaries.add(baseDictionary);
                }
            }
        }
        return this.returnResultMsg(true, baseDictionaries, "success", baseDictionaries.size(), null);
    }

    @Override
    public ResultMsg getTagKeyList() {
        List<BaseDictionary> baseDictionaries = new ArrayList<>();
        List<ConfTagKeys> confTagKeysList = confTagKeysMapper.selectAll();
        for (ConfTagKeys confTagKeys : confTagKeysList) {
            BaseDictionary baseDictionary = new BaseDictionary();
            baseDictionary.setDictName(confTagKeys.getTagKey());
            baseDictionary.setDictValue(confTagKeys.getTagKey());
            baseDictionaries.add(baseDictionary);
        }
        return this.returnResultMsg(true, baseDictionaries, "success", null, null);
    }

    @Override
    public ResultMsg checkConnect(String jsonStr, HttpServletResponse httpServletResponse) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        try {
            JSONObject requestParams = JSON.parseObject(jsonStr);
            if (requestParams != null && requestParams.size() > 0) {
                String paramKey = requestParams.containsKey("paramKey") ? requestParams.getString("paramKey") : "";
                if (StringUtils.isNotEmpty(paramKey)) {
                    return ParamKey.resolveParamKey(sqlSessionFactory, applicationContext, paramKey, paramKey, httpServletResponse);
                }

                String url = requestParams.containsKey("url") ? requestParams.getString("url") : "";
                String port = requestParams.containsKey("port") ? requestParams.getString("port") : "3306";
                String database = requestParams.containsKey("database") ? requestParams.getString("database") : "";
                String account = requestParams.containsKey("account") ? requestParams.getString("account") : "";
                String password = requestParams.containsKey("password") ? requestParams.getString("password") : "";
                Integer type = requestParams.containsKey("type") ? requestParams.getInteger("type") : -1;

                //自动建立ambari数据库
                if (type == 0 && ambariDbNameManual.equalsIgnoreCase("false")) {
                    return checkParam.checkAmbariDb(url, Integer.parseInt(port), database, account, password, true);
                }
                // 手动建立ambari数据库
                if (type == 0 && !ambariDbNameManual.equalsIgnoreCase("false")) {
                    return checkParam.checkAmbariDb(url, Integer.parseInt(port), database, account, password, false);
                }

                // 不检查hive数据库
                if (type == 1 && hivecheck.equalsIgnoreCase("false")) {
                    ResultMsg msg = new ResultMsg();
                    msg.setResult(true);
                    return msg;
                }

                // 检查hive数据库
                if (type == 1) {
                    return checkParam.checkHiveMetaDb(url, Integer.parseInt(port), database, account, password);
                }

            }
        } catch (Exception e) {
            getLogger().info("数据库测试异常" + e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    @Override
    public ResultMsg checkClusterName(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        try {
            JSONObject requestParams = JSON.parseObject(jsonStr);
            if (requestParams != null && requestParams.size() > 0) {
                String clusterName = requestParams.containsKey("clusterName") ? requestParams.getString("clusterName") : "";
                if (StringUtils.isEmpty(clusterName)) {
                    return this.returnResultMsg(false, null, null, null, "request cluster name value is empty");
                }
                String regex = "[ _`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(clusterName);
                if (matcher.find()) {
                    return this.returnResultMsg(false, null, null, null, "request cluster name value contains illegal characters");
                }
                // 检查一个集群名是否存在: clusterName = ${集群名} && state in (0,1,2,-1,-9)
                Map params = new HashMap();
                params.put("clusterName", clusterName);
                params.put("emrStatus", Arrays.asList(ConfCluster.CREATING, ConfCluster.CREATED, ConfCluster.DELETING, ConfCluster.FAILED,
                        ConfCluster.CREATE_AUDITING, ConfCluster.CREATE_AUDIT_REJECT, ConfCluster.DELETE_AUDITING,
                        ConfCluster.WAIT_DELETE, ConfCluster.DELETE_FAILED));
                List<Map> maps = confClusterMapper.selectByObject(params);
                if (maps.size() > 0) {
                    return this.returnResultMsg(false, null, null, null, "request cluster name already exists");
                }

                if (!checkClusterNameUsable(clusterName)) {
                    getLogger().error("创建集群失败：已经存在的集群名称[" + clusterName + "],删除时间不足3600秒");
                    ResultMsg msg = new ResultMsg();
                    msg.setResult(false);
                    msg.setErrorMsg("集群名称还不能使用");
                    return msg;
                }
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    @Override
    public ResultMsg geVmSkus(String region) {
        List<Map> vmSkus = new ArrayList<>();
        try {
            ResultMsg resultMsg = azureService.getVmSkus(region);
            getLogger().info("geVmSkus return" + JSON.toJSONString(resultMsg));
            if (null != resultMsg && resultMsg.getResult()) {
                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
                if (null != jsonArray && jsonArray.size() > 0) {
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                        Map params = new HashMap();
                        params.put("skuName", jsonObject.containsKey("name") ? jsonObject.getString("name") : "");
                        params.put("memoryGB", jsonObject.containsKey("memoryGB") ? jsonObject.getInteger("memoryGB") : "");
                        params.put("vCPUs", jsonObject.containsKey("vCoreCount") ? jsonObject.getInteger("vCoreCount") : "");
                        params.put("maxDataDiskCount", jsonObject.containsKey("maxDataDisksCount") ? jsonObject.getInteger("maxDataDisksCount") : 0);
                        params.put("hasNVMeDisk", jsonObject.containsKey("tempNVMeDisksCount") ? jsonObject.getInteger("tempNVMeDisksCount") > 0 : false);
                        params.put("nvmeDiskSizeGB", jsonObject.containsKey("tempNVMeDiskSizeGB") ? jsonObject.getInteger("tempNVMeDiskSizeGB") : 0);
                        params.put("nvmeDiskCount", jsonObject.containsKey("tempNVMeDisksCount") ? jsonObject.getInteger("tempNVMeDisksCount") : 0);
                        params.put("nvmeStorageGB", jsonObject.containsKey("tempNVMeStorageGB") ? jsonObject.getInteger("tempNVMeStorageGB") : 0);
                        params.put("referenceLink", jsonObject.containsKey("referenceLink") ? jsonObject.getString("referenceLink") : "");
                        vmSkus.add(params);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, vmSkus, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, vmSkus, "success", null, null);
    }


    @Override
    public ResultMsg getMIList(String region,String subscriptionId) {
        List<Map> miList = new ArrayList<>();
        try {
            ResultMsg resultMsg = azureService.getMIList( region, subscriptionId);
            getLogger().info("getMIList return: " + JSON.toJSONString(resultMsg));
            if (null != resultMsg && resultMsg.getResult()) {
                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
                if (null != jsonArray && jsonArray.size() > 0) {
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                        Map params = new HashMap();
                        params.put("name", jsonObject.containsKey("name") ? jsonObject.getString("name") : "");
                        params.put("applicationId", jsonObject.containsKey("applicationId") ? jsonObject.getString("applicationId") : "");
                        params.put("resourceId", jsonObject.containsKey("resourceId") ? jsonObject.getString("resourceId") : "");
                        params.put("tenantId", jsonObject.containsKey("tenantId") ? jsonObject.getString("tenantId") : "");
                        params.put("clientId", jsonObject.containsKey("clientId") ? jsonObject.getString("clientId") : "");
                        miList.add(params);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, miList, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, miList, "success", miList.size(), null);
    }

    @Override
    public ResultMsg getTagValueList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                String tagName = jsonObject.containsKey("tagKey") ? jsonObject.getString("tagKey") : "";
                ConfClusterTag confClusterTag = new ConfClusterTag();
                confClusterTag.setTagGroup(tagName);
                List<ConfClusterTag> confClusterTags = confClusterTagMapper.selectDistinctValueByTagGroup(confClusterTag);
                return this.returnResultMsg(true, confClusterTags, "success", null, null);
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, "[]", "success", null, null);
    }

    @Override
    public ResultMsg getServicelist(HttpServletRequest request) {
        // 获取查询入参
        String inputParam = request.getParameter("param");
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        if (StrUtil.isNotBlank(inputParam)) {
            params.put("uid", inputParam);
        }
        // 获取Cmdb信息:URL和Token
        String cmdbUrl = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_CMDB, BizConfigConstants.SHEINCMDBURL, String.class);
        String cmdbToken = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_CMDB, BizConfigConstants.SHEINCMDBXTOKEN, String.class);
        String requestUrl = cmdbUrl + SheinParamConstant.SHEIN_CMDB_SERVICE_URL;
        // 请求的Header中方token
        Map header = new HashMap();
        header.put(SheinParamConstant.SHEIN_XTOKEN_NAME, cmdbToken);

        ResultMsg result = new ResultMsg();
        result.setResult(true);
        try {
            // 请求CMDB
            long start = System.currentTimeMillis();
            String respStr = HttpClientUtil.doGet(requestUrl, params, header);
            getLogger().info("调用Cmdb-Service接口耗时：{} ms", (System.currentTimeMillis()-start));
            SheinCmdbResponse sheinResponseModel = JacksonUtil.readValue(respStr, SheinCmdbResponse.class);
            List svcList = sheinResponseModel.getAllResults().stream().map(item -> {
                Map<String, String> mapData = new HashMap<>();
                mapData.put("svcid", item.getUid());
                mapData.put("svc", item.getName());
                return mapData;
            }).collect(Collectors.toList());
            result.setData(svcList);
        } catch (Exception ex) {
            getLogger().error("请求Shein CMDB获取Service列表失败", ex);
            result.setErrorMsg(ex.getMessage());
            return result;
        }
        return result;
    }

    @Override
    public ResultMsg getSystemlist(HttpServletRequest request) {
        // 获取查询入参
        String inputParam = request.getParameter("param");
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        if (StrUtil.isNotBlank(inputParam)) {
            params.put("uid", inputParam);
        }
        // 获取Cmdb信息:URL和Token
        String cmdbUrl = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_CMDB, BizConfigConstants.SHEINCMDBURL, String.class);
        String cmdbToken = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_CMDB, BizConfigConstants.SHEINCMDBXTOKEN, String.class);
        String requestUrl = cmdbUrl + SheinParamConstant.SHEIN_CMDB_SYSTEM_URL;
        // 请求的Header中方token
        Map header = new HashMap();
        header.put(SheinParamConstant.SHEIN_XTOKEN_NAME, cmdbToken);

        ResultMsg result = new ResultMsg();
        result.setResult(true);
        try {
            // 请求CMDB
            long start = System.currentTimeMillis();
            String respStr = HttpClientUtil.doGet(requestUrl, params, header);
            getLogger().info("调用Cmdb-System接口耗时：{} ms", (System.currentTimeMillis()-start));
            SheinCmdbResponse sheinResponseModel = JacksonUtil.readValue(respStr, SheinCmdbResponse.class);
            List svcList = sheinResponseModel.getAllResults().stream().map(item -> {
                Map<String, String> mapData = new HashMap<>();
                mapData.put("svcid", item.getUid());
                mapData.put("svc", item.getName());
                return mapData;
            }).collect(Collectors.toList());
            result.setData(svcList);
        } catch (Exception ex) {
            getLogger().error("请求Shein CMDB获取Service列表失败", ex);
            result.setErrorMsg(ex.getMessage());
            return result;
        }
        return result;
    }

    @Override
    public ResultMsg getJobDetail(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                String planId = jsonObject.containsKey("planId") ? jsonObject.getString("planId") : "";
                String taskId = jsonObject.containsKey("taskId") ? jsonObject.getString("taskId") : "";
                String clusterId = "";
                if (StringUtils.isEmpty(planId) && StringUtils.isEmpty(taskId)) {
                    return this.returnResultMsg(false, null, null, null, "param value is empty");
                }
                Map params = new HashMap();
                Map resultMap = new HashMap();
                StringBuilder sb = new StringBuilder();
                List<Map> icopalMaps = new ArrayList<>();
                InfoClusterOperationPlan infoClusterOperationPlan = new InfoClusterOperationPlan();
                infoClusterOperationPlan.setPlanId(planId);
                infoClusterOperationPlan.setScalingTaskId(taskId);
                InfoClusterOperationPlan iCop = null;
                List<InfoClusterOperationPlan> infoClusterOperationPlans = infoClusterOperationPlanMapper.selectByObject(infoClusterOperationPlan);
                if (null != infoClusterOperationPlans && infoClusterOperationPlans.size() > 0) {
                    iCop = infoClusterOperationPlans.get(0);
                    clusterId = iCop.getClusterId();
                    planId = iCop.getPlanId();
                }
                ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);


                params.put("clusterId", clusterId);
                List<Map> maps = infoClusterMapper.selectByObject(params);
                if (null != maps && maps.size() > 0) {
                    Map map = maps.get(0);

                    ConfClusterApp confClusterApp = new ConfClusterApp();
                    confClusterApp.setClusterId(clusterId);
                    List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(confClusterApp);
                    if (null != confClusterApps && confClusterApps.size() > 0) {
                        for (ConfClusterApp cca : confClusterApps) {
                            sb.append(cca.getAppName());
                            sb.append(cca.getAppVersion());
                            sb.append(",");
                        }
                    }

                    List<InfoClusterOperationPlanActivityLogWithBLOBs> infoClusterOperationPlanActivityLogWithBLOBs
                            = infoClusterOperationPlanActivityLogMapper.getAllActivity(planId);


                    List<String> retryList = getRetryActivityLogIds(infoClusterOperationPlanActivityLogWithBLOBs);
                    if (null != infoClusterOperationPlanActivityLogWithBLOBs && infoClusterOperationPlanActivityLogWithBLOBs.size() > 0) {
                        for (InfoClusterOperationPlanActivityLogWithBLOBs icopal : infoClusterOperationPlanActivityLogWithBLOBs) {
                            Map map2 = new HashMap();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            map2.put("sortNo", icopal.getSortNo());
                            map2.put("activityName", StringUtils.isEmpty(icopal.getActivityCnname()) ? "-" : icopal.getActivityCnname());
                            map2.put("begTime", null == icopal.getBegtime() ? "-" : sdf.format(icopal.getBegtime()));
                            map2.put("endTime", null == icopal.getEndtime() ? "-" : sdf.format(icopal.getEndtime()));

                            //region 重试按钮控制
                            if (retryList != null && retryList.size() > 0
                                    && retryList.contains(icopal.getActivityLogId())) {
                                map2.put("activityLogId", icopal.getActivityLogId());

                                //region 重试按钮展示控制,销毁中和已销毁的不展示重试
                                if (!confCluster.getState().equals(ConfCluster.DELETING)
                                        && !confCluster.getState().equals(ConfCluster.DELETED)) {
                                    map2.put("retry", 1);
                                }
                                //endregion 重试按钮展示控制,销毁中和已销毁的不展示重试

                                //region 销毁集群的任务失败允许重试
                                if (iCop.getOperationType().equalsIgnoreCase(InfoClusterOperationPlan.Plan_OP_Delete)) {
                                    map2.put("retry", 1);
                                }
                                //endregion
                            }
                            if (StrUtil.isNotBlank(icopal.getLogs())) {
                                map2.put("showlog", 1);
                                map2.put("log", icopal.getLogs());
                            }
                            //endregion

                            //0 未执行 1 执行中 2 执行完成  -1 执行超时  -2 执行失败
                            switch (icopal.getState()) {
                                case 0:
                                    map2.put("state", "未执行");
                                    break;
                                case 1:
                                    map2.put("state", "执行中");
                                    break;
                                case 2:
                                    map2.put("state", "完成");
                                    break;
                                case -1:
                                    map2.put("state", "执行超时");
                                    break;
                                case -2:
                                    map2.put("state", "执行失败");
                                    break;
                            }
                            icopalMaps.add(map2);
                        }
                    }

                    Integer ambariNum = map.containsKey("ambari_count") ? Integer.valueOf(map.get("ambari_count").toString()) : 0;
                    Integer masterNum = map.containsKey("master_vms_count") ? Integer.valueOf(map.get("master_vms_count").toString()) : 0;
                    Integer coreNum = map.containsKey("core_vms_count") ? Integer.valueOf(map.get("core_vms_count").toString()) : 0;
                    Integer taskNum = map.containsKey("task_vms_count") ? Integer.valueOf(map.get("task_vms_count").toString()) : 0;
                    Integer isHa = map.containsKey("is_ha") ? Integer.valueOf(map.get("is_ha").toString()) : 0;
                    String clusterName = map.containsKey("cluster_name") ? map.get("cluster_name").toString() : "-";
                    resultMap.put("ambariNum", ambariNum);
                    resultMap.put("masterNum", masterNum);
                    resultMap.put("coreNum", coreNum);
                    resultMap.put("taskNum", taskNum);
                    resultMap.put("clusterName", clusterName);
                    resultMap.put("isHa", isHa == 0 ? false : true);
                    resultMap.put("serviceInfos", sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString() : "-");
                    resultMap.put("activityInfos", icopalMaps);
                    return this.returnResultMsg(true, resultMap, "success", null, null);
                }
            }
        } catch (Exception e) {
            getLogger().error("getjobdetail,exception,", e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    private List<String> getRetryActivityLogIds(List<InfoClusterOperationPlanActivityLogWithBLOBs>
                                                        infoClusterOperationPlanActivityLogWithBLOBs) {

        List<String> retrylist = new ArrayList<>();
        Optional<InfoClusterOperationPlanActivityLogWithBLOBs> failActivityLog =
                infoClusterOperationPlanActivityLogWithBLOBs.stream().filter(x -> {
                    return (x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_TIMEOUT)
                            || x.getState().equals(InfoClusterOperationPlanActivityLog.ACTION_FAILED));
                }).findFirst();

        if (failActivityLog.isPresent() && null != failActivityLog.get()) {
            List<InfoClusterOperationPlanActivityLogWithBLOBs> sortedlist =
                    infoClusterOperationPlanActivityLogWithBLOBs.stream().sorted((x1, x2) -> {
                        return x2.getSortNo().compareTo(x1.getSortNo());
                    }).collect(Collectors.toList());
            AtomicBoolean flag = new AtomicBoolean(false);
            sortedlist.stream().forEachOrdered(item -> {
                if (item.getActivityLogId().equals(failActivityLog.get().getActivityLogId())) {
                    retrylist.add(item.getActivityLogId());
                    flag.set(true);
                }
                if (item.getSortNo() < failActivityLog.get().getSortNo() && flag.get()) {
                    retrylist.add(item.getActivityLogId());
                    flag.set(false);
                }
            });
        }
        return retrylist;
    }

    @Override
    public ResultMsg getJobList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        List<Map> resultMap = new ArrayList<>();
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                List<String> jobNameCodes = getJobNameCodes(jsonObject);
//                String jobName = jsonObject.containsKey("jobName") ? jsonObject.getString("jobName") : null;
                Integer state = null;
                Object jobState = jsonObject.get("jobState");
                if (jobState != null) {
                    if (!StringUtils.isEmpty(jobState.toString())) {
                        state = Integer.parseInt(jobState.toString());
                    }
                }

                String clusterId = jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : "";
                String clusterName = jsonObject.containsKey("clusterName") ? jsonObject.getString("clusterName") : "";
                String begTime = jsonObject.containsKey("begTime") ? jsonObject.getString("begTime") : "";
                String endTime = jsonObject.containsKey("endTime") ? jsonObject.getString("endTime") : "";
                Integer pageIndex = jsonObject.containsKey("pageIndex") ? jsonObject.getInteger("pageIndex") : 1;
                Integer pageSize = jsonObject.containsKey("pageSize") ? jsonObject.getInteger("pageSize") : 10;
                Integer startPercent = jsonObject.containsKey("startPercent") ? jsonObject.getInteger("startPercent") : null;
                Integer endPercent = jsonObject.containsKey("endPercent") ? jsonObject.getInteger("endPercent") : null;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                InfoClusterOperationPlan infoClusterOperationPlan = new InfoClusterOperationPlan();
//                infoClusterOperationPlan.setPlanName(OperationPlanUtils.getPlanName(jobName));
                infoClusterOperationPlan.setClusterId(clusterId);
                infoClusterOperationPlan.setPageIndex((pageIndex - 1) * (pageSize));
                infoClusterOperationPlan.setPageSize(pageSize);
                infoClusterOperationPlan.setClusterName(clusterName);
                infoClusterOperationPlan.setStartPercent(startPercent);
                infoClusterOperationPlan.setEndPercent(endPercent);
                if (StringUtils.isNotEmpty(begTime) && StringUtils.isNotEmpty(endTime)) {
                    infoClusterOperationPlan.setBegTime(sdf.parse(begTime + " 00:00:01"));
                    infoClusterOperationPlan.setEndTime(sdf.parse(endTime + " 23:59:59"));
                }
                infoClusterOperationPlan.setState(state);

                List<String> jobNames = getJobNameParmas(infoClusterOperationPlan, jobNameCodes);

//                List<InfoClusterOperationPlan> infoClusterOperationPlans = infoClusterOperationPlanMapper.selectByObject(infoClusterOperationPlan);
//                Integer total = infoClusterOperationPlanMapper.countByObject(infoClusterOperationPlan);
                List<InfoClusterOperationPlan> infoClusterOperationPlans = infoClusterOperationPlanMapper.selectJobList(infoClusterOperationPlan, jobNames);
                Integer total = infoClusterOperationPlanMapper.countJobList(infoClusterOperationPlan, jobNames);
                if (null != infoClusterOperationPlans && infoClusterOperationPlans.size() > 0) {
                    for (InfoClusterOperationPlan iCop : infoClusterOperationPlans) {
                        String cName = confClusterMapper.selectClusterNameByPrimaryKey(iCop.getClusterId());
                        Map map = new HashMap();
                        map.put("jobName", iCop.getPlanName());
                        map.put("begTime", null == iCop.getBegTime() ? "-" : sdf.format(iCop.getBegTime()));
                        map.put("endTime", null == iCop.getEndTime() ? "-" : sdf.format(iCop.getEndTime()));
                        map.put("clusterName", cName);
                        map.put("clusterId", iCop.getClusterId());
                        map.put("planId", iCop.getPlanId());
                        map.put("state", OperationPlanUtils.getStateText(iCop.getState(), iCop.getPercent()));
                        map.put("scalingTaskId", iCop.getScalingTaskId());
                        map.put("opTaskId", iCop.getOpTaskId());
                        map.put("operationType", iCop.getOperationType());
                        resultMap.add(map);
                    }
                    return this.returnResultMsg(true, resultMap, "success", total, null);
                }
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    private static List<String> getJobNameCodes(JSONObject jsonObject) {
        List<String> jobNameCodes = new ArrayList<>();
        Object jobNameObj = jsonObject.get("jobName");
        if (Objects.nonNull(jobNameObj)) {
            if (jobNameObj instanceof String && StrUtil.isNotEmpty(jobNameObj.toString())) {
                jobNameCodes.add(jobNameObj.toString());
            } else if (jobNameObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jobNameObj;
                for (int i = 0; i < jsonArray.size(); i++) {
                    jobNameCodes.add(jsonArray.getString(i));
                }
            }
        }

        return jobNameCodes;
    }

    private List<String> getJobNameParmas(InfoClusterOperationPlan infoClusterOperationPlan, List<String> jobNameCodes) {
        List<String> jobNames = new ArrayList<>();
        if (CollectionUtil.isEmpty(jobNameCodes)) {
            return jobNames;
        }
        jobNameCodes.forEach(jobNameCode -> {
            jobNames.add(OperationPlanUtils.getPlanName(jobNameCode));
        });

        return jobNames;
    }

    @Override
    public ResultMsg getJobListNew(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        List<Map<String, Object>> resultMap = new ArrayList<>();
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                String jobName = jsonObject.containsKey("jobName") ? jsonObject.getString("jobName") : "";
                String clusterId = jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : "";
                String clusterName = jsonObject.containsKey("clusterName") ? jsonObject.getString("clusterName") : "";
                String begTime = jsonObject.containsKey("begTime") ? jsonObject.getString("begTime") : "";
                String endTime = jsonObject.containsKey("endTime") ? jsonObject.getString("endTime") : "";
                Integer pageIndex = jsonObject.containsKey("pageIndex") ? jsonObject.getInteger("pageIndex") : 1;
                Integer pageSize = jsonObject.containsKey("pageSize") ? jsonObject.getInteger("pageSize") : 10;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                InfoClusterOperationPlan infoClusterOperationPlan = new InfoClusterOperationPlan();
                infoClusterOperationPlan.setOperationType(getJobOperationType(jobName));
                infoClusterOperationPlan.setClusterId(clusterId);
                infoClusterOperationPlan.setPageIndex((pageIndex - 1) * (pageSize));
                infoClusterOperationPlan.setPageSize(pageSize);
                infoClusterOperationPlan.setClusterName(clusterName);
                if (StringUtils.isNotEmpty(begTime) && StringUtils.isNotEmpty(endTime)) {
                    infoClusterOperationPlan.setBegTime(sdf.parse(begTime + " 00:00:01"));
                    infoClusterOperationPlan.setEndTime(sdf.parse(endTime + " 23:59:59"));
                }

                Integer total = infoClusterOperationPlanMapper.countByObject(infoClusterOperationPlan);
                List<SelectJoinCCAndActivityLogResult> selectJoinCCAndActivityLogResults = infoClusterOperationPlanMapper.selectJoinCCAndActivityLog(infoClusterOperationPlan);
                // 根据planId进行分类
                Map<String, List<SelectJoinCCAndActivityLogResult>> groupByPlanIdMap = new LinkedHashMap<>();
                if (!CollectionUtils.isEmpty(selectJoinCCAndActivityLogResults)) {
                    for (SelectJoinCCAndActivityLogResult logResult : selectJoinCCAndActivityLogResults) {
                        String planId = logResult.getPlanId();
                        List<SelectJoinCCAndActivityLogResult> logResultList = new ArrayList<>();
                        if (!groupByPlanIdMap.containsKey(planId)) {
                            logResultList.add(logResult);
                            groupByPlanIdMap.put(planId, logResultList);
                        } else {
                            groupByPlanIdMap.get(planId).add(logResult);
                        }
                    }
                }

                // 封装响应数据
                if (groupByPlanIdMap.size() > 0) {
                    Set<String> planIdSet = groupByPlanIdMap.keySet();
                    for (String planId : planIdSet) {
                        Map<String, Object> respData = new HashMap<>();
                        List<SelectJoinCCAndActivityLogResult> logResultList = groupByPlanIdMap.get(planId);
                        SelectJoinCCAndActivityLogResult logResult = logResultList.get(0);
                        InfoClusterOperationPlan iCop = new InfoClusterOperationPlan();
                        iCop.setOperationType(logResult.getOperationType());
                        iCop.setScalingTaskId(logResult.getScalingTaskId());
                        respData.put("jobName", getJobName(iCop, respData));
                        respData.put("begTime", null == logResult.getBegTime() ? "-" : sdf.format(logResult.getBegTime()));
                        respData.put("endTime", null == logResult.getEndTime() ? "-" : sdf.format(logResult.getEndTime()));
                        respData.put("clusterName", logResult.getClusterName());
                        respData.put("clusterId", logResult.getClusterId());
                        respData.put("planId", logResult.getPlanId());
                        respData.put("createdTime", logResult.getCreatedTime());

                        /*
                         * 计算state值（百分比）
                         * 计算状态逻辑：优先检查运行失败及超时的任务，如果有失败，马上退出检查，生成失败结果。如果没有失败， 再正常检查完成的数量占全部数量的百分比
                         * 响应的状态：完成100%， 运行中N%， 运行失败 N%, 超时 N%
                         */
                        if (!CollectionUtils.isEmpty(logResultList)) {
                            Integer size = logResultList.size();
                            int failIndex = -1;
                            int runningIndex = -1;
                            int timeoutIndex = -1;
                            int completeIndex = -1;
                            int completeCount = 0;
                            int waitRunCount = 0;
                            for (int i = 1; i <= logResultList.size(); i++) {
                                Integer activityState = logResultList.get(i - 1).getState();
                                if (activityState != null) {
                                    // State: 0 未执行 1 执行中 2 执行完成  -1 执行超时  -2 执行失败
                                    switch (activityState) {
                                        case 0:
                                            waitRunCount++;
                                            break;
                                        case 1:
                                            runningIndex = i;
                                            break;
                                        case 2:
                                            completeCount++;
                                            completeIndex = i;
                                            break;
                                        case -1:
                                            timeoutIndex = i;
                                            break;
                                        case -2:
                                            failIndex = i;
                                            break;
                                    }
                                }
                            }
                            if (timeoutIndex > 0) {
                                respData.put("state", "执行超时" + computePercentage(timeoutIndex, size));
                            } else if (failIndex > 0) {
                                respData.put("state", "执行失败" + computePercentage(failIndex, size));
                            } else if (Objects.equals(completeCount, size)) {
                                respData.put("state", "完成100%");
                            } else if (runningIndex > 0) {
                                respData.put("state", "运行中" + computePercentage(runningIndex, size));
                            } else if (Objects.equals(waitRunCount, size)) {
                                respData.put("state", "未执行");
                            } else if (completeIndex > 0) {
                                respData.put("state", "运行中止" + computePercentage(completeIndex, size));
                            } else {
                                respData.put("state", "运行中止");
                            }
                        }
                        resultMap.add(respData);
                    }

                    // 对结果进行排序
                    resultMap.sort(new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            return Double.valueOf(((Date) o2.get("createdTime")).getTime() - ((Date) o1.get("createdTime")).getTime()).intValue();
                        }
                    });
                    return this.returnResultMsg(true, resultMap, "success", total, null);
                }
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    private String getJobName(InfoClusterOperationPlan iCop, Map map) {
        if (InfoClusterOperationPlan.Plan_OP_Create.equals(iCop.getOperationType())) {
            return "创建集群";
        }
        if (InfoClusterOperationPlan.Plan_OP_Delete.equals(iCop.getOperationType())) {
            return "删除集群";
        }
        if (InfoClusterOperationPlan.Plan_OP_RunUserScript.equalsIgnoreCase(iCop.getOperationType())) {
            return "执行用户脚本任务";
        }

        ConfScalingTask scalingTask = confScalingTaskNeoMapper.selectByPrimaryKey(iCop.getScalingTaskId());
        if (InfoClusterOperationPlan.Plan_OP_ScaleIn.equalsIgnoreCase(iCop.getOperationType())) {
            if (scalingTask != null) {
                if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_delete_group)) {
                    return "删除实例组";
                } else if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_delete_Task_Vm)) {
                    return "删除扩容任务实例";
                } else if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_spot)) {
                    return "[竞价实例]缩容集群";
                }
            }
            return "缩容集群";
        }
        if (InfoClusterOperationPlan.Plan_OP_ScaleOut.equalsIgnoreCase(iCop.getOperationType())) {
            if (scalingTask != null) {
                if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_create_group)) {
                    return "新增实例组";
                } else if (Objects.equals(scalingTask.getOperatiionType(), ConfScalingTask.Operation_type_spot)) {
                    return "[竞价实例]扩容集群";
                }
            }
            return "扩容集群";
        }
        if (InfoClusterOperationPlan.Plan_OP_CollectLogs.equalsIgnoreCase(iCop.getOperationType())) {
            return "日志收集";
        }
        if (InfoClusterOperationPlan.Plan_OP_ClusterService_Restart.equalsIgnoreCase(iCop.getOperationType())) {
            return "服务重启";
        }
        if (InfoClusterOperationPlan.Plan_OP_Part_ScaleOut.equalsIgnoreCase(iCop.getOperationType())) {
            return "磁盘扩容";
        }

        if (InfoClusterOperationPlan.Plan_OP_ClearVMs.equalsIgnoreCase(iCop.getOperationType())) {
            return "清理VM";
        }
        return "";
    }

    private String getJobOperationType(String jobName) {
        if (StringUtils.equals("创建集群", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_Create;
        } else if (StringUtils.equals("删除集群", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_Delete;
        } else if (StringUtils.equals("执行用户脚本任务", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_RunUserScript;
        } else if (StringUtils.equals("缩容集群", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_ScaleIn;
        } else if (StringUtils.equals("扩容集群", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_ScaleOut;
        } else if (StringUtils.equals("日志收集", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_CollectLogs;
        } else if (StringUtils.equals("服务重启", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_ClusterService_Restart;
        } else if (StringUtils.equals("磁盘扩容", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_Part_ScaleOut;
        } else if (StringUtils.equals("新增实例组", jobName)) {
            //todo: 新增实例组后续会添加
            return "";
        } else if (StringUtils.equals("清理VM", jobName)) {
            return InfoClusterOperationPlan.Plan_OP_ClearVMs;
        }
        return "";
    }

    private String computePercentage(Integer index, Integer total) {
        BigDecimal totalNum = new BigDecimal(total);
        BigDecimal indexNum = new BigDecimal(index);
        BigDecimal percent = indexNum.divide(totalNum, 2, BigDecimal.ROUND_HALF_UP).scaleByPowerOfTen(2);
        if (NumberUtil.equals(percent, new BigDecimal("100"))) {
            // 100时，返回99%，避免发生100%失败的情况。
            return "99%";
        } else {
            return percent + "%";
        }
    }


    @Override
    public ResultMsg deleteCluster(String jsonStr, String userName) {
        ResultMsg msg = new ResultMsg();

        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        getLogger().info("ClusterDaemonTask ,deleteCluster,jsonObject:{},userName:{}", jsonObject,userName);
        String clusterId = jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : "";
        if (StringUtils.isEmpty(clusterId)) {
            return this.returnResultMsg(false, null, null, null, "clusterId is empty");
        }
        // 判断是否强制删除
        String fDelStr = jsonObject.containsKey("fDel") ? jsonObject.getString("fDel") : null;
        boolean fDel = fDelStr != null && fDelStr.equalsIgnoreCase("true");

        String lockKey = "delete_" + clusterId;
        boolean lock = redisLock.tryLock(lockKey);
        try {
            if (!lock) {
                msg.setErrorMsg("请勿重复提交请求");
                msg.setResult(false);
                return msg;
            }
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                return this.returnResultMsg(false, null, null, null, "can't find this cluster:" + clusterId);
            }
            // 判断是否来自定时任务:ClusterDaemonTask
            String fromTask = jsonObject.getString("fromTask");
            if(!StringUtils.isEmpty(fromTask) && "1".equals(fromTask)){
                if ( confCluster.getState() == -2) {
                    msg.setMsg("该集群已销毁");
                    msg.setResult(true);
                    return msg;
                }
                msg = this.clusterOperation(clusterId, "delete", confCluster.getClusterReleaseVer());
                if (msg.getResult()) {
                    // 更新集群状态为销毁中
                    confCluster.setState(ConfCluster.DELETING);
                    confCluster.setModifiedby(userName);
                    confClusterHostGroupMapper.updateByClusterId(confCluster.getClusterId(), ConfClusterHostGroup.STATE_RELEASING);
                    confClusterMapper.updateByPrimaryKey(confCluster);
                    return msg;
                }else {
                    return msg;
                }

            }
            if (!fDel) {
                // 非强制删除集群，需要判断此时的集群状态
                if (confCluster.getState() == -1 || confCluster.getState() == -2) {
                    msg.setErrorMsg("该集群正在销毁中或已销毁");
                    msg.setResult(false);
                    return msg;
                }
            }

            //判断是否在白名单
            Integer isWhiteAddr = confCluster.getIsWhiteAddr();
            if (isWhiteAddr != null && isWhiteAddr == 0 ) {
                //不在白名单, 而且是创建成功的, 才加入定时任务来销毁
                //先走工单进行审批, 审批回调后再加入定时任务来销毁. 如果是管理员, 也加入定时任务直接销毁.
                String callBackPass = jsonObject.getString("callBackPass");
                String oriUserName="";
                List<ClusterDestroyTask> clusterDestroyTasks = clusterDestroyTaskMapper.selectByClusterId(clusterId);
                if (CollectionUtils.isEmpty(clusterDestroyTasks)) {
                    oriUserName=userName;
                }else {
                    ClusterDestroyTask clusterDestroyTask = clusterDestroyTasks.stream().filter(clu -> clu.getClusterId().equalsIgnoreCase(clusterId)).findFirst().get();
                    oriUserName = clusterDestroyTask.getCreatedby();
                }
                String roleCode = userInfoService.getUserRoleStrByUserName(oriUserName);
                if("1".equals(callBackPass) || CommonConstant.ADMINISTRATOR.equalsIgnoreCase(roleCode)){
                    getLogger().info("AdminApiServiceImpl delete cluster by task,confCluster  {}", confCluster);
                    clusterOperationBytask(confCluster, "delete", fDel,oriUserName);
                    updateClusterState(oriUserName, confCluster.getClusterId(),ConfCluster.WAIT_DELETE);
                    msg.setResult(true);
                    msg.setMsg("销毁流程转入后台任务中，请稍后再查看");
                    return msg;
                }
                //其他角色的走工单来销毁
                 destroyByOrder(oriUserName, msg, confCluster);
                return msg;
            }

            msg = this.clusterOperation(clusterId, "delete", confCluster.getClusterReleaseVer());
            if (msg.getResult()) {
                // 更新集群状态为销毁中
                confCluster.setState(ConfCluster.DELETING);
                confCluster.setModifiedby(userName);
                confClusterHostGroupMapper.updateByClusterId(confCluster.getClusterId(), ConfClusterHostGroup.STATE_RELEASING);
                confClusterMapper.updateByPrimaryKey(confCluster);
            }
        } catch (Exception e) {
            getLogger().error("delete cluster exception，", e);
            msg.setErrorMsg(e.getMessage());
        } finally {
            try {
                if (redisLock.isLocked(lockKey)) {
                    redisLock.unlock(lockKey);
                }
            } catch (Exception e) {
                getLogger().error("锁释放异常，", e);
            }

        }
        return msg;

    }

    /**
     * 发送工单销毁请求
     * @param userName
     * @param msg
     * @param confCluster
     * @return
     */
    private ResultMsg destroyByOrder(String userName, ResultMsg msg, ConfCluster confCluster) {
      //审核通过(回调接口)后, callBackPass=1, ,销毁时, 应该走正常的限流流程.
            ResultMsg resultMsg = clusterInfoService.destroyClusterByWorkOrderTicket("", confCluster.getClusterId(),userName);
            getLogger().info("AdminApiServiceImpl,deleteCluster, destroyClusterByWorkOrderTicket,resultMsg:{}", resultMsg);
            if (resultMsg.getResult()) {
                updateClusterState(userName, confCluster.getClusterId(),ConfCluster.DELETE_AUDITING);
                msg.setResult(true);
                msg.setMsg("工单销毁已经发送请求,请稍后查看");
                return msg;
            } else {
                msg.setResult(false);
                msg.setMsg("工单销毁异常");
                return msg;
            }
    }

    private void updateClusterState(String userName, String clusterId,Integer state) {
        //更新集群状态
        ConfCluster confCluster1 =new ConfCluster();
        confCluster1.setClusterId(clusterId);
        confCluster1.setState(state);
        confCluster1.setModifiedby(userName);
        confCluster1.setModifiedTime(new Date());
        //todo 状态
        // confClusterHostGroupMapper.updateByClusterId(confCluster.getClusterId(), ConfClusterHostGroup.DELETE_AUDITING);
        confClusterMapper.updateByPrimaryKeySelective(confCluster1);
    }


//    public void updateCluster(String userName, ConfCluster confCluster,Integer state) {
//        // 更新集群状态为销毁中
//        confCluster.setState(ConfCluster.WAIT_DELETE);
//        confCluster.setModifiedby(userName);
////        confClusterHostGroupMapper.updateByClusterId(confCluster.getClusterId(), ConfClusterHostGroup.STATE_RELEASING);
//        confClusterMapper.updateByPrimaryKey(confCluster);
//    }

    /**
     * 存入销毁任务表中
     * @param confCluster
     * @param operation
     * @param fDel
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void clusterOperationBytask(ConfCluster confCluster, String operation, boolean fDel,String userName) throws InvocationTargetException, IllegalAccessException {
        ClusterDestroyTask clusterDestroyTask = new ClusterDestroyTask();
        BeanUtils.copyProperties( clusterDestroyTask,confCluster);
        clusterDestroyTask.setDestroyTaskId(cn.hutool.core.lang.UUID.fastUUID().toString(true));
        clusterDestroyTask.setDestroyStatus(DestroyStatusConstant.DESTROY_STATUS_WAITING);
        int fDelInt = fDel ? 1 : 0;
        clusterDestroyTask.setfDel(fDelInt);
        clusterDestroyTask.setCreatedby(userName);
        clusterDestroyTask.setCreatedTime(new Date());
        clusterDestroyTaskMapper.insertSelective(clusterDestroyTask);
    }

    @Override
    public ResultMsg createCluster(String jsonStr, String userName) {
        getLogger().info("begin createCluster, jsonStr: {}", jsonStr);

        // 限流
//        boolean acquired = createClusterRateLimiter.tryAcquire();
//        if (!acquired) {
//            return this.returnResultMsg(false, null, "创建集群请求过于频繁，请稍后再试", null, "创建集群请求过于频繁，请稍后再试");
//        }

        String clusterId = UUID.randomUUID().toString();
        // 判断请求参数内容是否为空
        if (StringUtils.isEmpty(jsonStr)) {
            getLogger().error("创建集群失败：请求内容为空");
            return this.returnResultMsg(false, null, null, null, "error: the request data is empty.");
        }
        AdminSaveClusterRequest adminSaveClusterRequest = JSON.parseObject(jsonStr, AdminSaveClusterRequest.class);
        adminSaveClusterRequest.setUserName(userName);

        if (Objects.isNull(adminSaveClusterRequest.getIsEmbedAmbariDb())) {
            adminSaveClusterRequest.setIsEmbedAmbariDb(0);
        }

        if (adminSaveClusterRequest.getClusterName().contains("-amb-")
                || adminSaveClusterRequest.getClusterName().contains("-cor-")
                || adminSaveClusterRequest.getClusterName().contains("-mst-")) {
            getLogger().error("create cluster fail, cluster name has key words: -amb-,-cor-,-mst-");
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setErrorMsg("集群名称含有不可用的关键字[clusterName=" + adminSaveClusterRequest.getClusterName() + ", 限制关键字：-amb-,-cor-,-mst-]");
            return msg;
        }

        // 参数校验, 目前只校验了数据盘大小
        adminSaveClusterRequest.validate();
        //校验竞价相关信息,1,价格,2,sku数量3-15个
        List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = adminSaveClusterRequest.getInstanceGroupSkuCfgs();
        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            this.checkSpot(adminSaveClusterRequest.getRegion(),instanceGroupSkuCfg);
        }

        fillMIExtInfo(adminSaveClusterRequest);

        // ganglia关联配置合法性校验
        ResultMsg resultMsg = checkGangliaAssociatedConf(adminSaveClusterRequest);
        if (!resultMsg.getResult()) {
            return resultMsg;
        }

        String lockKey = "create_lockKey_" + adminSaveClusterRequest.getClusterName();
        boolean lock = redisLock.tryLock(lockKey);
        if (!lock) {
            getLogger().error("error createCluster lock failure, lockKey: {},clusterName: {}",
                    lockKey, adminSaveClusterRequest.getClusterName());
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setErrorMsg("请勿重复提交数据。");
            return msg;
        }

        try {
            // 查询cluster是否已存在，不允许存在
            if (checkIsExistByClusterId(adminSaveClusterRequest.getClusterName())) {
                getLogger().error("创建集群失败：已经存在的集群名称: {}", adminSaveClusterRequest.getClusterName());
                ResultMsg msg = new ResultMsg();
                msg.setResult(false);
                msg.setErrorMsg("已经存在的集群名称。");
                return msg;
            }
            //检查集群名称是否可用
            if (!checkClusterNameUsable(adminSaveClusterRequest.getClusterName())) {
                getLogger().error("创建集群失败：已经存在的集群名称[" + adminSaveClusterRequest.getClusterName() + "],删除时间不足3600秒");
                ResultMsg msg = new ResultMsg();
                msg.setResult(false);
                msg.setErrorMsg("集群名称还不能使用");
                return msg;
            }
            //检查实例名称是否可用
            ResultMsg checkGroupNameUsable = this.checkGroupNameUsable(instanceGroupSkuCfgs);
            if (!checkGroupNameUsable.isSuccess()){
                return checkGroupNameUsable;
            }

            // 检查HBase场景下的参数配置
            adminSaveClusterRequest.validateHBaseScene();

            String roleCode = userInfoService.getUserRoleStrByUserName(userName);
            if (CommonConstant.STAFF.equalsIgnoreCase(roleCode)) {
                 //普通人员,调用shein接口创建工单
                ResultMsg resultMsgTicket = clusterInfoService.createClusterByWorkOrderTicket(jsonStr,clusterId,userName);
                getLogger().info("AdminApiServiceImpl.createCluster createClusterByWorkOrderTicket: {}",  JSON.toJSONString(resultMsgTicket));
                if (!resultMsgTicket.getResult()) {
                    getLogger().error("AdminApiServiceImpl.createCluster createClusterByWorkOrderTicket: {}",  JSON.toJSONString(resultMsgTicket));
                    return this.returnResultMsg(false, null, null, null, "工单创建集群异常。");
                }
                adminSaveClusterRequest.setWorkOrderCreate(1);
            }else {
                adminSaveClusterRequest.setWorkOrderCreate(0);
            }
            // 创建数据库
            ResultMsg crtdb = createAmbariDb(adminSaveClusterRequest);
            if (!crtdb.getResult()) {
                return this.returnResultMsg(false, null, null, null, "创建ambari数据库异常。");
            }

            /** 开始保存KeyVault **/
            this.saveKeyVault(adminSaveClusterRequest);
            /** 结束保存KeyVault **/

            getLogger().info("AdminApiServiceImpl.createCluster clusterId: {}, adminSaveClusterRequest: {}", clusterId, JSON.toJSONString(adminSaveClusterRequest));
            ConfCluster.CreationMode creationMode = adminSaveClusterRequest.getCreationMode(ConfCluster.CreationMode.DIRECTLY);
            adminSaveClusterRequest.setCreationMode(creationMode.getValue());
            ClusterCreationStrategy clusterCreationStrategy = clusterCreationStrategyFactory.create(creationMode);
            return clusterCreationStrategy.createCluster(clusterId, adminSaveClusterRequest);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, e.getMessage());
        } finally {
            redisLock.tryUnlock(lockKey);
        }
    }

    /**
     * 创建数据库
     *
     * @param adminSaveClusterRequest
     * @return
     */
    private ResultMsg createAmbariDb(AdminSaveClusterRequest adminSaveClusterRequest) {
        ResultMsg msg = new ResultMsg();
        if (adminSaveClusterRequest.getIsEmbedAmbariDb().equals(0)) {
            return checkParam.checkAmbariDb(adminSaveClusterRequest.getAmbariDbCfgs().geturl(),
                    Integer.parseInt(adminSaveClusterRequest.getAmbariDbCfgs().getPort()),
                    adminSaveClusterRequest.getAmbariDbCfgs().getDatabase(),
                    adminSaveClusterRequest.getAmbariDbCfgs().getAccount(),
                    adminSaveClusterRequest.getAmbariDbCfgs().getPassword(),
                    true);
        } else {
            getLogger().info("内置数据库跳过。");
            msg.setResult(true);
        }
        return msg;
    }


    private void fillMIExtInfo(AdminSaveClusterRequest req) {
        String region = req.getRegion();
        if (StringUtils.isEmpty(req.getVmMIClientId()) || StringUtils.isEmpty(req.getVmMITenantId())) {
            getLogger().info("vmMI的ClientId或TenantId为为空，从Azure处查询MiList：vmClientId={},vmTenantId={},resourceId={}",
                    req.getVmMIClientId(), req.getVmMITenantId(),req.getVmMI());
            String vmMI = req.getVmMI();
            ManagedIdentity mi = metaDataItemService.getMI(region, vmMI);
            if (mi == null) {
                getLogger().error("保存集群信息时， 从元数据增强获取getMI为空");
                return;
            }
            req.setVmMIClientId(mi.getClientId());
            req.setVmMITenantId(mi.getTenantId());
        }

        if (StringUtils.isEmpty(req.getLogMIClientId()) || StringUtils.isEmpty(req.getLogMITenantId())) {
            getLogger().info("logMI的ClientId或TenantId为为空，从Azure处查询MiList：logClientId={},logTenantId={},resourceId={}",
                    req.getLogMIClientId(), req.getLogMITenantId(),req.getLogMI());
            String logMI = req.getLogMI();
            ManagedIdentity mi = metaDataItemService.getMI(region, logMI);
            if (mi == null) {
                getLogger().error("保存集群信息时， 从元数据增强获取getLogsBlobContainer为空");
                return;
            }
            req.setLogMIClientId(mi.getClientId());
            req.setLogMITenantId(mi.getTenantId());
        }

        Assert.notEmpty(req.getVmMIClientId(), "虚拟机MI的ClientId不能为空");
        Assert.notEmpty(req.getVmMITenantId(), "虚拟机MI的TenantId不能为空");
        Assert.notEmpty(req.getLogMIClientId(), "日志桶MI的ClientId不能为空");
        Assert.notEmpty(req.getLogMITenantId(), "日志桶MI的TenantId不能为空");
    }

    private ResultMsg checkGangliaAssociatedConf(AdminSaveClusterRequest adminSaveClusterRequest) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        Integer enableGanglia = adminSaveClusterRequest.getEnableGanglia();
        Integer isEmbedAmbariDb = adminSaveClusterRequest.getIsEmbedAmbariDb();
        List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = adminSaveClusterRequest.getInstanceGroupSkuCfgs();

        int totalVmCount = 0;
        Integer ambariDataVolumeSize = null;
        Integer ambariOsVolumeSize = null;
        String skuName = null;
        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            if (instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("Ambari")) {
                ambariDataVolumeSize = instanceGroupSkuCfg.getDataVolumeSize();
                ambariOsVolumeSize = instanceGroupSkuCfg.getOsVolumeSize();
                skuName = instanceGroupSkuCfg.getSkuNames().get(0);
            }
            Integer cnt = instanceGroupSkuCfg.getCnt();
            totalVmCount += cnt;
        }

        // 启用Ganglia
        if (enableGanglia != null && enableGanglia == 1) {
            // 总实例数不能大于200
            if (totalVmCount > 200) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后总实例数不能大于200");
                return resultMsg;
            }

            // Ambari实例的数据盘容量不能小于2T
            if (ambariDataVolumeSize != null && ambariDataVolumeSize < 2000) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari数据盘容量不能小于2T");
                return resultMsg;
            }

            // Ambari实例的系统盘容量必须为200G
            if (ambariOsVolumeSize != null && ambariOsVolumeSize != 200) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari系统盘容量必须为200G");
                return resultMsg;
            }

            // Ambari实例组规格最低为16c64G
            if (StringUtils.isBlank(skuName) || !checkEnableGangliaVmSku(skuName,adminSaveClusterRequest.getRegion())) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("启用Ganglia后Ambari规格最低为16c64G");
                return resultMsg;
            }
        }
        return resultMsg;
    }

    private boolean checkEnableGangliaVmSku(String skuName,String region) {
        boolean checkFlag = false;
        VMSku vmsku = metaDataItemService.getVMSKU(skuName, region);
        if (vmsku != null) {
            String targetSkuName = vmsku.getName();
            int vCPUs = Integer.parseInt(vmsku.getVCoreCount());
            int memoryGB = Integer.parseInt(vmsku.getMemoryGB());
            if (targetSkuName.equals(skuName)) {
                if (vCPUs >= 16 && memoryGB >= 64) {
                    checkFlag = true;
                }
            }
        }
        return checkFlag;
    }

    @Override
    public ResultMsg getCluster(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String clusterId = jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : "";
        if (StringUtils.isEmpty(clusterId)) {
            return this.returnResultMsg(false, null, null, null, "clusterId is empty");
        }
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster == null) {
            return this.returnResultMsg(false, null, null, null, "can't find this cluster:" + clusterId);
        }
        return this.returnResultMsg(true, confCluster, "success", null, null);
    }

    private void saveKeyVault(AdminSaveClusterRequest adminSaveClusterRequest) {
        String clusterName = StringUtils.isEmpty(adminSaveClusterRequest.getClusterName()) ? "" : adminSaveClusterRequest.getClusterName();
        String region = adminSaveClusterRequest.getRegion();
        //region ambari
        if (adminSaveClusterRequest.getIsEmbedAmbariDb() == null || adminSaveClusterRequest.getIsEmbedAmbariDb().equals(0)) {
            String ambariPassword = StringUtils.isEmpty(adminSaveClusterRequest.getAmbariPassword()) ? "" : adminSaveClusterRequest.getAmbariPassword();
            String ambariDbAccount = StringUtils.isEmpty(adminSaveClusterRequest.getAmbariDbCfgs().getAccount()) ? "" : adminSaveClusterRequest.getAmbariDbCfgs().getAccount();
            String ambariDbPassword = StringUtils.isEmpty(adminSaveClusterRequest.getAmbariDbCfgs().getPassword()) ? "" : adminSaveClusterRequest.getAmbariDbCfgs().getPassword();
            if (StringUtils.isNotEmpty(ambariPassword)) {
                // 先删除，后增加
                this.delete2KeyVault("ambari-pwd-" + clusterName,region);
                this.save2KeyVault("ambari-pwd-" + clusterName, ambariPassword,region);
            }
            if (StringUtils.isNotEmpty(ambariDbAccount) && StringUtils.isNotEmpty(ambariDbPassword)) {
                // 先删除，后增加
                this.delete2KeyVault("ambari-db-user-" + clusterName,region);
                this.delete2KeyVault("ambari-db-pwd-" + clusterName,region);
                this.save2KeyVault("ambari-db-user-" + clusterName, ambariDbAccount,region);
                this.save2KeyVault("ambari-db-pwd-" + clusterName, ambariDbPassword,region);
            }
        }

        //endregion

        //region hive
        String hiveDbAccount = adminSaveClusterRequest.getHiveMetadataDbCfgs().getAccount();
        String hiveDbPassword = adminSaveClusterRequest.getHiveMetadataDbCfgs().getPassword();

        if (StringUtils.isNotEmpty(hiveDbAccount) && StringUtils.isNotEmpty(hiveDbPassword)) {
            // 先删除，后增加
            this.delete2KeyVault("hivemetadata-db-user-" + clusterName,region);
            this.delete2KeyVault("hivemetadata-db-pwd-" + clusterName,region);
            this.save2KeyVault("hivemetadata-db-user-" + clusterName, hiveDbAccount,region);
            this.save2KeyVault("hivemetadata-db-pwd-" + clusterName, hiveDbPassword,region);
        }
        //endregion
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
     *
     * @param clusterName
     * @return
     */
    private boolean checkClusterNameUsable(String clusterName) {
        Map params = new HashMap();
        params.put("clusterName", clusterName);
        params.put("emrStatus", Arrays.asList(ConfCluster.DELETED));
        ConfCluster confCluster = confClusterMapper.findTop1ByObject(params);
        if (confCluster == null || confCluster.getModifiedTime() == null) {
            return true;
        }

        if (System.currentTimeMillis() - confCluster.getModifiedTime().getTime() < sdp_dns_ttl * 1000) {
            return false;
        }
        return true;
    }
    /**
     * 检查实例组名称是否可用
     *
     * @param instanceGroupSkuCfgs
     * @return
     */
    private ResultMsg checkGroupNameUsable(List<InstanceGroupSkuCfg> instanceGroupSkuCfgs) {
        Integer count = 0;
        int groupNameCount = 0;
        Set<String> groupNameSet = new HashSet<>();
        for (InstanceGroupSkuCfg igsc : instanceGroupSkuCfgs) {
            String vmRole = igsc.getVmRole().toLowerCase();
            if ("ambari".equals(vmRole)) {
                count++;
            }

            if (StringUtils.isNotBlank(igsc.getGroupName())) {
                groupNameSet.add(igsc.getGroupName());
                groupNameCount++;
            }
        }
        if (count > 1) {
            return ResultMsg.FAILURE("exist ambari number of nodes more than one");
        }
        if (groupNameSet.size() != groupNameCount) {
            return ResultMsg.FAILURE("实例组名称存在重复。");
        }
        return ResultMsg.SUCCESS();
    }

    private void addConfClusterVmAndDataVolumeWhenAddGroup(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        getLogger().info("添加vm配置信息,clusterId={},config={}", clusterId, adminSaveClusterRequest.getInstanceGroupSkuCfgs());
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
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
            confClusterVm.setPriceStrategy(instanceGroupSkuCfg.getPriceStrategy());
            confClusterVm.setMaxPrice(instanceGroupSkuCfg.getMaxPrice());
            confClusterVm.setPurchasePriority(instanceGroupSkuCfg.getPurchasePriority());

            //从core的vm中获取镜像信息
            List<ConfClusterVm> clusterVms = confClusterVmNeoMapper.selectByClusterIdAndVmRole(clusterId, VmRoleType.CORE.getVmRole());
            ConfClusterVm coreConfClusterVm = clusterVms.get(0);
            if (StrUtil.isEmpty(instanceGroupSkuCfg.getImgId())) {
                confClusterVm.setImgId(coreConfClusterVm.getImgId());
            } else {
                confClusterVm.setImgId(instanceGroupSkuCfg.getImgId());
            }

            if (StrUtil.isEmpty(instanceGroupSkuCfg.getOsImageId())) {
                confClusterVm.setOsImageid(coreConfClusterVm.getOsImageid());
            } else {
                confClusterVm.setOsImageid(instanceGroupSkuCfg.getOsImageId());
            }
            confClusterVm.setOsVersion(coreConfClusterVm.getOsVersion());
            confClusterVm.setOsImageType(coreConfClusterVm.getOsImageType());
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

    private void addConfClusterScript(String clusterId, List<ConfClusterScript> confClusterScripts) {
        for (ConfClusterScript ccs : confClusterScripts) {
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

    private void addConfKeypair(String keypairId) {
        ConfKeypair confKeypair = new ConfKeypair();
        confKeypair.setKeypairId(keypairId);
        confKeypair.setCreatedby("sysadmin");
        confKeypair.setCreatedTime(new Date());
        confKeypairMapper.insertSelective(confKeypair);
    }

    private void addInfoCluster(String clusterId) {
        InfoCluster infoCluster = new InfoCluster();
        infoCluster.setClusterId(clusterId);
        ConfClusterApp confClusterApp = new ConfClusterApp();
        confClusterApp.setClusterId(clusterId);
        List<ConfClusterApp> confClusterApps = confClusterAppMapper.selectByObject(confClusterApp);
        infoCluster.setAppsCount(null == confClusterApps ? 0 : confClusterApps.size());
        infoClusterMapper.insertSelective(infoCluster);
    }

    private ResultMsg returnResultMsg(Boolean result, Object data, String msg, Integer total, String errorMsg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(result);
        resultMsg.setData(data);
        resultMsg.setMsg(msg);
        resultMsg.setErrorMsg(errorMsg);
        resultMsg.setTotal(null == total ? 0 : total);
        return resultMsg;
    }

    private Boolean save2KeyVault(String key, String val,String region) {
        keyVault keyVault = metaDataItemService.getkeyVault(region);
        return keyVaultUtil.setSecret(key, val,keyVault.getEndpoint());
    }

    private Boolean delete2KeyVault(String key,String region) {
        keyVault keyVault = metaDataItemService.getkeyVault(region);
        return keyVaultUtil.delSecret(key,keyVault.getEndpoint());
    }

    private String getVault(String key,String region) {
        keyVault keyVault = metaDataItemService.getkeyVault(region);
        String secretVal = keyVaultUtil.getSecretVal(key,keyVault.getEndpoint());
        return secretVal;
    }

    private String getAmbariDBUsername(String clusterName,String region) {
        String keyPrefix = "ambari-db-user-";
        String key = keyPrefix + clusterName;
        return getVault(key,region);
    }

    private String getAmbariDBPassword(String clusterName,String region) {
        String keyPrefix = "ambari-db-pwd-";
        String key = keyPrefix + clusterName;
        return getVault(key,region);
    }

    private String getHiveMetaDataDBUsername(String clusterName,String region) {
        String keyPrefix = "hivemetadata-db-user-";
        String key = keyPrefix + clusterName;
        return getVault(key,region);
    }

    private String getHiveMetaDataDBPassword(String clusterName,String region) {
        String keyPrefix = "hivemetadata-db-pwd-";
        String key = keyPrefix + clusterName;
        return getVault(key,region);
    }

    private ResultMsg clusterOperation(String clusterId, String operation, String releaseVer) {
        getLogger().info("clusterOperation clusterId:{},operation:{},releaseVer:{}",
                clusterId,
                operation,
                releaseVer);

        ResultMsg createPlanResult = composeService.createPlan(clusterId, operation, releaseVer);
        if (null == createPlanResult || !createPlanResult.getResult()) {
            return createPlanResult;
        }
        Map<String, String> datamap = new HashMap<>();
        datamap.put("clusterId", clusterId);
        createPlanResult.setData(datamap);
        return createPlanResult;

    }

    @Override
    public ResultMsg checkCustomScriptUri(String customScriptUri) {
        ResultMsg composeFeignResult = composeService.checkCustomScriptUri(customScriptUri);
        if (null == composeFeignResult || !composeFeignResult.getResult()) {
            return this.returnResultMsg(false, null, null, null, "error: check custom script uri failed");
        }
        return this.returnResultMsg(true, composeFeignResult.getData(), "success", null,
                (Boolean) composeFeignResult.getData() ? null : composeFeignResult.getErrorMsg());
    }

    @Override
    public ResultMsg getAmbariStatus(String activityLogId) {
        ResultMsg msg = composeService.getAmbariStatus(activityLogId);
        if (null == msg || !msg.getResult()) {
            return this.returnResultMsg(false, null, null, null, "error: get Ambari Status failed");
        }
        return this.returnResultMsg(true, msg.getData(), "success", null, "");
    }

    @Override
    public ResultMsg getAnsibleStatus(String activityLogId) {
        ResultMsg msg = composeService.getAnsibleStatus(activityLogId);
        if (null == msg || !msg.getResult()) {
            return this.returnResultMsg(false, null, null, null, "error: get ansible Status failed");
        }
        return this.returnResultMsg(true, msg.getData(), "success", null, "");
    }

    @Override
    public ResultMsg queryClusterInfo(String clusterId) {
        return queryClusterInfo(clusterId, true);
    }

    @Override
    public ResultMsg queryClusterInfo(String clusterId, boolean fetchScalingRules) {
        // 目标：组织出 ClusterQueryResponse
        ResultMsg resultMsg = new ResultMsg();
        // 校验集群是否真实存在
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (null == confCluster) {
            getLogger().error("查询集群数据为空 {}", clusterId);
            resultMsg.setResult(true);
            resultMsg.setData(null);
            resultMsg.setErrorMsg("查询集群数据为空");
            return resultMsg;
        }
        String clusterName = confCluster.getClusterName();
        String region = confCluster.getRegion();

        // 封装基础信息
        ClusterQueryResponse response = new ClusterQueryResponse();
        response.setMasterSecurityGroup(confCluster.getMasterSecurityGroup());
        response.setSlaveSecurityGroup(confCluster.getSlaveSecurityGroup());
        response.setSubNet(confCluster.getSubnet());
        response.setVNet(confCluster.getVnet());
        response.setKeypairId(confCluster.getKeypairId());
        response.setLogPath(confCluster.getLogPath());
        response.setIsHa(confCluster.getIsHa());
        response.setClusterName(clusterName);
        response.setDeleteProtected(confCluster.getDeleteProtected());
        response.setScene(confCluster.getScene());
        response.setVmMI(confCluster.getVmMI());
        response.setLogMI(confCluster.getLogMI());
        response.setState(confCluster.getState());
        response.setZone(confCluster.getZone());
        response.setZoneName(confCluster.getZoneName());
        response.setEnableGanglia(confCluster.getEnableGanglia());
        response.setRegion(confCluster.getRegion());
        response.setRegionName(getRegionName(confCluster.getRegion()));
        if (Objects.isNull(confCluster.getIsEmbedAmbariDb())) {
            response.setIsEmbedAmbariDb(0);
        } else {
            response.setIsEmbedAmbariDb(confCluster.getIsEmbedAmbariDb());
        }
        //是否加入直接销毁白名单,是:空或1,否:0
        response.setIsParallelScale(confCluster.getIsParallelScale());
        if (Objects.isNull(confCluster.getIsWhiteAddr())){
            response.setIsWhiteAddr(1);
        }else {
            response.setIsWhiteAddr(confCluster.getIsWhiteAddr());
        }

        // 封装ambari
        AmbariDbCfg ambariDbCfg = new AmbariDbCfg();
        if (response.getIsEmbedAmbariDb() == 1) {
            // 内置MySQL时, 使用默认值
            ambariDbCfg.setDatabase("ambaridb");
            ambariDbCfg.setAccount("root");
            ambariDbCfg.setPassword("");
            ambariDbCfg.setPort("3306");
            ambariDbCfg.seturl("localhost");
            response.setAmbariDbCfgs(ambariDbCfg);
            response.setAmbariUsername("root");
            response.setAmbariPassword("");
        } else {
            ambariDbCfg.setDatabase(confCluster.getAmbariDatabase());
            ambariDbCfg.setAccount(getAmbariDBUsername(clusterName,region));
            ambariDbCfg.setPassword(getAmbariDBPassword(clusterName,region));
            ambariDbCfg.setPort(confCluster.getAmbariPort());
            ambariDbCfg.seturl(confCluster.getAmbariDburl());
            response.setAmbariDbCfgs(ambariDbCfg);
            response.setAmbariUsername(confCluster.getAmbariAcount());
            response.setAmbariPassword(ambariDbCfg.getPassword());
        }

        // 封装hive
        HiveMetadataDbCfg hiveMetadataDbCfg = new HiveMetadataDbCfg();
        hiveMetadataDbCfg.setDatabase(confCluster.getHiveMetadataDatabase());
        hiveMetadataDbCfg.setAccount(getHiveMetaDataDBUsername(clusterName,region));
        hiveMetadataDbCfg.setPassword(getHiveMetaDataDBPassword(clusterName,region));
        hiveMetadataDbCfg.setPort(confCluster.getHiveMetadataPort());
        hiveMetadataDbCfg.seturl(confCluster.getHiveMetadataDburl());
        response.setHiveMetadataDbCfgs(hiveMetadataDbCfg);

        // 封装 InstanceGroupVersion
        InstanceGroupVersion instanceGroupVersion = new InstanceGroupVersion();
        instanceGroupVersion.setClusterReleaseVer(confCluster.getClusterReleaseVer());
        // List<ClusterApp> clusterApps 封装
        ConfClusterApp toSelectClusterApp = new ConfClusterApp();
        toSelectClusterApp.setClusterId(clusterId);
        List<ConfClusterApp> confClusterAppList = confClusterAppMapper.selectByObject(toSelectClusterApp);
        if (!CollectionUtils.isEmpty(confClusterAppList)) {
            List<ClusterApp> clusterAppList = confClusterAppList.stream()
                    .map(confClusterApp -> {
                        ClusterApp clusterApp = new ClusterApp();
                        clusterApp.setAppName(confClusterApp.getAppName());
                        clusterApp.setAppVersion(confClusterApp.getAppVersion());
                        return clusterApp;
                    })
                    .collect(Collectors.toList());
            instanceGroupVersion.setClusterApps(clusterAppList);
        }
        response.setInstanceGroupVersion(instanceGroupVersion);

        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByClusterId(clusterId);

        // 封装 InstanceGroupSkuCfg list
        List<ConfClusterVm> confClusterVmList = confClusterVmNeoMapper.selectByClusterId(clusterId);
        Set<String> groupSet = confClusterHostGroupMapper.selectByValidClusterId(clusterId).stream()
                .map(hostgroup -> hostgroup.getGroupId()).collect(Collectors.toSet());
        boolean clusterIsRuning = confCluster.getState() == 2;
        getLogger().info("confClusterVmList:{}",confClusterVmList.toString());
        AtomicReference<String> masterProvisionType = new AtomicReference<>("VM_Standalone");
        if (!CollectionUtils.isEmpty(confClusterVmList)) {
            List<InstanceGroupSkuCfg> instanceGroupSkuCfgList = confClusterVmList.stream()
                    .filter(confClusterVm -> !clusterIsRuning || groupSet.contains(confClusterVm.getGroupId()))
                    .map(confClusterVm -> {
                        InstanceGroupSkuCfg instanceGroupSkuCfg = new InstanceGroupSkuCfg();
                        instanceGroupSkuCfg.setCnt(confClusterVm.getCount());
                        instanceGroupSkuCfg.setVmRole(confClusterVm.getVmRole());
                        instanceGroupSkuCfg.setGroupName(confClusterVm.getGroupName());
                        instanceGroupSkuCfg.setMemoryGB(Convert.toBigDecimal(confClusterVm.getMemory(), null));
                        instanceGroupSkuCfg.setOsVolumeSize(confClusterVm.getOsVolumeSize());
                        instanceGroupSkuCfg.setOsVolumeType(confClusterVm.getOsVolumeType());
                        List<String> skuNameList = confHostGroupVmSkus.stream()
                                .filter(confHostGroupVmSku -> confHostGroupVmSku.getVmConfId().equals(confClusterVm.getVmConfId()))
                                .map(ConfHostGroupVmSku::getSku)
                                .collect(Collectors.toList());
                        //竞价多个sku
                        instanceGroupSkuCfg.setSkuNames(skuNameList);
                        //按需只有一个
                        instanceGroupSkuCfg.setSkuName(confClusterVm.getSku());
                        instanceGroupSkuCfg.setPurchaseType(confClusterVm.getPurchaseType());
                        instanceGroupSkuCfg.setPurchasePriority(confClusterVm.getPurchasePriority());
                        instanceGroupSkuCfg.setMaxPrice(confClusterVm.getMaxPrice());
                        instanceGroupSkuCfg.setPriceStrategy(confClusterVm.getPriceStrategy());
                        instanceGroupSkuCfg.setVCPUs(Convert.toInt(confClusterVm.getVcpus(), null));
                        instanceGroupSkuCfg.setRegularAllocationStrategy(confClusterVm.getRegularAllocationStrategy());
                        instanceGroupSkuCfg.setSpotAllocationStrategy(confClusterVm.getSpotAllocationStrategy());
                        Integer provisionType = confClusterVm.getProvisionType();
                        if (provisionType != null && provisionType == 1) {
                            instanceGroupSkuCfg.setProvisionType("VM_Standalone");
                        } else if (provisionType != null && provisionType == 2) {
                            instanceGroupSkuCfg.setProvisionType("VMSS_Flexible");
                        }
                        if (provisionType == null) {
                            instanceGroupSkuCfg.setProvisionType("VM_Standalone");
                        }
                        if (StrUtil.equalsIgnoreCase(confClusterVm.getVmRole(), "master")) {
                            masterProvisionType.set(instanceGroupSkuCfg.getProvisionType());
                        }

                        String vmConfId = confClusterVm.getVmConfId();
                        getLogger().info(vmConfId);
                        ConfClusterVmDataVolume dataVolume = confClusterVmDataVolumeMapper.selectByVmConfId(vmConfId).get(0);
                        instanceGroupSkuCfg.setDataVolumeSize(dataVolume.getDataVolumeSize());
                        instanceGroupSkuCfg.setDataVolumeType(dataVolume.getDataVolumeType());
                        instanceGroupSkuCfg.setDataVolumeCount(dataVolume.getCount() + "");

                        doFetchScalingRules(clusterId, confClusterVm, instanceGroupSkuCfg, fetchScalingRules);

                        //获取配置
                        List<ConfClusterHostGroupAppsConfig> confClusterHostGroupAppsConfigs = confClusterHostGroupAppsConfigMapper.listByClusterIdAndGroupId(confClusterVm.getClusterId(), confClusterVm.getGroupId());
                        if (confClusterHostGroupAppsConfigs != null) {
                            instanceGroupSkuCfg.setGroupCfgs(new ArrayList<>());
                            for (ConfigClassification classification : ConfigClassification.values()) {
                                ClusterCfg clusterCfg = null;
                                for (ConfClusterHostGroupAppsConfig confClusterHostGroupAppsConfig : confClusterHostGroupAppsConfigs) {
                                    if (StringUtils.equals(confClusterHostGroupAppsConfig.getAppConfigClassification(), classification.getClassification())) {
                                        if (clusterCfg == null) {
                                            clusterCfg = new ClusterCfg();
                                            clusterCfg.setClassification(classification.getClassification());
                                            clusterCfg.setcfg(new JSONObject());
                                        }
                                        clusterCfg.getcfg().put(confClusterHostGroupAppsConfig.getConfigItem(), confClusterHostGroupAppsConfig.getConfigVal());
                                    }
                                }
                                if (clusterCfg != null) {
                                    instanceGroupSkuCfg.getGroupCfgs().add(clusterCfg);
                                }
                            }
                        }
                        return instanceGroupSkuCfg;
                    })
                    .collect(Collectors.toList());

            // 处理Ambari配置，在高可用场景下，先找到Ambari配置，将其设置为Master的配置
            if (Objects.equals(response.getIsHa(), 1)) {
                for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgList) {
                    if (StrUtil.equalsIgnoreCase(instanceGroupSkuCfg.getVmRole(), "ambari")) {
                        instanceGroupSkuCfg.setProvisionType(masterProvisionType.get());
                        getLogger().info("集群是高可用集群，将Ambari的反物理亲和性设置为与Master一致：[clusterId={}, isHa={}, masterProvisionType={}, ambariProvisionType={}]",
                                clusterId, response.getIsHa(), masterProvisionType.get(), instanceGroupSkuCfg.getProvisionType());
                    }
                }
            } else {
                getLogger().info("集群不是高可用集群，不需要将Ambari的反物理亲和性设置为与Master一致：[clusterId={}, isHa={}, masterProvisionType={}]",
                        clusterId, response.getIsHa(), masterProvisionType.get());
            }

            response.setInstanceGroupSkuCfgs(instanceGroupSkuCfgList);
        }

        // 如果集群在使用中，使用正在运行的主机数据
        if (confCluster.getState() == 2) {
            // 正在运行的主机数量
            List<HashMap> runningVMCountList = infoClusterVmMapper.getGroupNameCount(confCluster.getClusterId());

            // 已经统计的集群申请时的主机数量
            List<InstanceGroupSkuCfg> instanceGroupSkuCfgList = response.getInstanceGroupSkuCfgs();

            for (HashMap runningVM : runningVMCountList) {
                for (InstanceGroupSkuCfg groupSku : instanceGroupSkuCfgList) {
                    String groupName = groupSku.getGroupName();
                    groupName = StrUtil.isEmpty(groupName) ? groupSku.getVmRole() : groupName;

                    String runningGroupName = String.valueOf(runningVM.get("group_name"));

                    if (Objects.equals(runningGroupName, groupName)) {
                        Integer cnt = Convert.toInt(runningVM.get("cnt"), null);
                        if (Objects.nonNull(cnt)) {
                            groupSku.setCnt(cnt);
                            break;
                        }
                    }
                }
            }
        }

        // ClusterCfg 封装
        List<ConfClusterAppsConfig> confClusterAppsConfigList = confClusterAppsConfigMapper.selectByClusterId(clusterId);
        if (!CollectionUtils.isEmpty(confClusterAppsConfigList)) {
            Map<String, List<ConfClusterAppsConfig>> fileGroup = confClusterAppsConfigList.stream().collect(Collectors.groupingBy(ConfClusterAppsConfig::getAppConfigClassification));
            List<ClusterCfg> clusterCfgList = new ArrayList<>();

            for (Map.Entry<String, List<ConfClusterAppsConfig>> entry : fileGroup.entrySet()) {
                ClusterCfg cfg = new ClusterCfg();
                cfg.setClassification(entry.getKey());
                JSONObject cfgItems = new JSONObject();
                cfg.setcfg(cfgItems);

                entry.getValue().stream().forEach(appConfig -> {
                    cfgItems.put(appConfig.getConfigItem(), appConfig.getConfigVal());
                });
                clusterCfgList.add(cfg);
            }

            response.setClusterCfgs(clusterCfgList);
        }

        // confClusterScript封装
        List<com.sunbox.domain.ConfClusterScript> confClusterScriptList =
                confClusterScriptMapper.selectByClusterIdForCp(clusterId);
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
            response.setConfClusterScript(confClusterScripts);
        }

        // 封装 tagMap
        JSONObject tagMapJson = getClusterTags(clusterId);
        response.setTagMap(tagMapJson);
        resultMsg.setResult(true);
        resultMsg.setData(response);
        resultMsg.setMsg("success");
        return resultMsg;
    }

    private void doFetchScalingRules(String clusterId, ConfClusterVm confClusterVm, InstanceGroupSkuCfg instanceGroupSkuCfg, boolean fetchScalingRules) {
        if (!fetchScalingRules) {
            // 不需要查询扩缩容规则时，直接返回
            return;
        }
        List<ConfGroupElasticScaling> confGroupElasticScalings = confGroupElasticScalingMapper.listByClusterIdAndGroupNameAndValid(confClusterVm.getClusterId(), confClusterVm.getGroupName());
        if (!confGroupElasticScalings.isEmpty()) {
            ConfGroupElasticScaling elasticScaling = confGroupElasticScalings.get(0);
            if (confGroupElasticScalings.size() > 1) {
                String lockKey = "repair-ConfGroupElasticScaling:" + clusterId + ":" + confClusterVm.getGroupName();
                if (redisLock.tryLock(lockKey)) {
                    try {
                        for (int index = 1; index < confGroupElasticScalings.size(); index++) {
                            ConfGroupElasticScaling repairRecord = new ConfGroupElasticScaling();
                            repairRecord.setGroupEsId(confGroupElasticScalings.get(index).getGroupEsId());
                            repairRecord.setIsValid(ConfGroupElasticScaling.ISVALID_NO);
                            repairRecord.setModifiedby("maintainer");
                            repairRecord.setModifiedTime(new Date());
                            confGroupElasticScalingMapper.updateByPrimaryKey(repairRecord);
                        }
                    } finally {
                        redisLock.tryUnlock(lockKey);
                    }
                }
            }

            ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
            confGroupElasticScalingData.setClusterId(confClusterVm.getClusterId());
            confGroupElasticScalingData.setGroupName(confClusterVm.getGroupName());
            confGroupElasticScalingData.setVmRole(confClusterVm.getVmRole());
            confGroupElasticScalingData.setMaxCount(elasticScaling.getMaxCount());
            confGroupElasticScalingData.setMinCount(elasticScaling.getMinCount());
            confGroupElasticScalingData.setIsValid(ConfGroupElasticScaling.ISVALID_YES);
            confGroupElasticScalingData.setScalingRules(Lists.newArrayList());

            List<ConfGroupElasticScalingRule> scalingRuleList = confGroupElasticScalingRuleMapper.selectByClusterIdAndGroupNameAndValid(confClusterVm.getClusterId(), confClusterVm.getGroupName());
            for (ConfGroupElasticScalingRule scalingRule : scalingRuleList) {
                ConfGroupElasticScalingRuleData scalingRuleData = new ConfGroupElasticScalingRuleData();
                BeanUtil.copyProperties(scalingRule, scalingRuleData);
                confGroupElasticScalingData.getScalingRules().add(scalingRuleData);
            }
            instanceGroupSkuCfg.setConfGroupElasticScalingData(confGroupElasticScalingData);
        }
    }



    @NotNull
    private JSONObject getClusterTags(String clusterId) {
        ConfClusterTag toSelectTag = new ConfClusterTag();
        toSelectTag.setClusterId(clusterId);
        List<ConfClusterTag> confClusterTagList = confClusterTagMapper.selectByObject(toSelectTag);
        JSONObject tagMapJson = new JSONObject();
        confClusterTagList.forEach(confClusterTag -> {
            tagMapJson.put(confClusterTag.getTagGroup(), confClusterTag.getTagVal());
        });
        // 检查集群的Tag是否缺少必填的4个Tag   svcid   svc   service    for
        for (String mustExistTag : mustExistTags) {
            Object val = tagMapJson.get(mustExistTag);
            if (Objects.isNull(val)) {
                tagMapJson.put(mustExistTag, "");
            }
        }
        return tagMapJson;
    }

    private String getRegionName(String code) {
        try {
            JSONArray regionArray = JSON.parseArray(sdpRegion);
            String name = "";
            for (Object region : regionArray) {
                Map<String, String> regionMap = (Map<String, String>) region;
                if (Objects.equals(regionMap.get("code"), code)) {
                    name = regionMap.get("name");
                    break;
                }
            }
            return name;
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            return "";
        }
    }

    @Override
    public boolean queryAmbariDbNameManual() {
        return Objects.equals(this.ambariDbNameManual, "true");
    }

    @Override
    public ResultMsg queryBaseScript(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }

        ResultMsg result = new ResultMsg();
        result.setResult(true);
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                String scriptName = jsonObject.getString("scriptName");
                String state = jsonObject.getString("state");
                Integer pageIndex = jsonObject.getInteger("pageIndex");
                Integer pageSize = jsonObject.getInteger("pageSize");
                state = Objects.isNull(state) ? "VALID" : state;
                pageIndex = Objects.isNull(pageIndex) ? 0 : pageIndex - 1;
                pageSize = Objects.isNull(pageSize) ? 20 : pageSize;

                BaseScript script = new BaseScript();
                script.setScriptName(scriptName);
                script.setState(state);
                Long total = baseScriptMapper.count(script);
                result.setTotal(total);
                if (total == 0) {
                    return result;
                }

                List<BaseScript> baseScripts = baseScriptMapper.queryAllByLimit(script, PageRequest.of(pageIndex, pageSize));
                return this.returnResultMsg(true, baseScripts, "success", total.intValue(), null);
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    @Override
    public ResultMsg getBlobContent(String filePath) {
        ResultMsg result = new ResultMsg();
        try {
            Assert.notEmpty(filePath, "要获取的文件不能为空");
            byte[] content = HttpsUtil.doGet(filePath);
            String contentStr = new String(content, "UTF-8");
            result.setResult(true);
            result.setData(contentStr);
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            result.setResult(false);
            result.setErrorMsg(ex.getMessage());
        }
        return result;
    }

    @Override
    public ResultMsg createBaseScript(BaseScript script, MultipartFile file) {
        // 检查文件名是否重复
        List<BaseScript> baseScriptList = baseScriptMapper.queryByScriptName(script.getScriptName());
        if (!CollectionUtils.isEmpty(baseScriptList)) {
            getLogger().error("上传脚本文件名称已存在, 不接受此脚本文件: " + script.getScriptName());
            return this.returnResultMsg(false, null, null, null, "脚本文件已存在:" + script.getScriptName());
        }
        try {
            byte[] bytes = file.getBytes();
            String fileContent = new String(bytes, "UTF-8");
            fileContent = replaceWindowsR(fileContent);
            //todo  上传脚本暂时不区分region
            String region="";
            ResultMsg result = composeService.uploadFileToBlob(script.getScriptName(), fileContent,region);

            if (result.getResult()) {
                // 准备数据
                script.setScriptId(UUID.randomUUID().toString());
                script.setState("VALID");
                script.setUploadTime(new Date());
                script.setBlobPath(String.valueOf(result.getData()));

                // 保存数据库
                baseScriptMapper.insert(script);

                // 生成返回结果
                return this.returnResultMsg(true, script, "success", null, "");
            } else {
                return result;
            }
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            ResultMsg msg = new ResultMsg();
            msg.setData(ex.getMessage());
            return msg;
        }
    }

    private String replaceWindowsR(String s) {
        if (Objects.isNull(s)) {
            return "";
        }
        return s.replaceAll("\\r", "");
    }

    @Override
    public String getClusterBlueprint(String clusterId) {
        ResultMsg clusterBlueprint = composeService.getClusterBlueprint(clusterId);
        if (Objects.nonNull(clusterBlueprint)) {
            return (String) clusterBlueprint.getData();
        }
        return null;
    }

    /**
     * 创建资源组标签
     */
    @Override
    public ResultMsg createResourceGroup(String azureResourceGroupTagsRequest) {
        return composeService.createResourceGroup(azureResourceGroupTagsRequest);
    }

    /**
     * 查看资源组标签
     */
    @Override
    public ResultMsg getResourceGroup(String clusterId) {
        return composeService.getResourceGroup(clusterId);
    }

    /**
     * 删除资源组
     */
    @Override
    public ResultMsg deleteResourceGroup(String clusterId) {
        return composeService.deleteResourceGroup(clusterId);
    }

    /**
     * 更新资源组标签-全量
     */
    @Override
    public ResultMsg updateResourceGroupTags(String azureResourceGroupTagsRequest) {
        return composeService.updateResourceGroupTags(azureResourceGroupTagsRequest);
    }

    /**
     * 更新资源组标签-增量
     */
    @Override
    public ResultMsg addResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        return composeService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
    }

    /**
     * 删除资源组标签
     */
    @Override
    public ResultMsg deleteResourceGroupTags(String azureResourceGroupAddTagsRequest) {
        return composeService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
    }

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     */
    @Override
    public ResultMsg supportedVMSkuList(String region) {
        return azureService.supportedVMSkuList(region);
    }

    /**
     * 集群实例扩容
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg clusterScaleOut(ClusterScaleOutOrScaleInRequest request) {
        getLogger().info("begin clusterScaleOut:{}", request);
        ResultMsg msg = new ResultMsg();
        // region 参数校验（注释）
        if (StrUtil.isEmpty(request.getClusterId())) {
            getLogger().error("clusterScaleOut error,clusterId is null");
            msg.setResult(false);
            msg.setErrorMsg("集群参数未找到");
            return msg;
        }

        if (StrUtil.isEmpty(request.getGroupName())) {
            getLogger().error("clusterScaleOut error,groupName is null");
            msg.setResult(false);
            msg.setErrorMsg("实例组参数未找到");
            return msg;
        }

        if (request.getExpectCount() == null) {
            getLogger().error("clusterScaleOut error,expect count is null,clusterId:{},groupName:{}", request.getClusterId(), request.getGroupName());
            msg.setResult(false);
            msg.setErrorMsg("参数错误期望数量为空");
            return msg;
        }

        // region 参数校验
        if (request.getScaleOutCount() == null) {
            getLogger().error("clusterScaleOut error,scale out count is null,clusterId:{},groupName:{}", request.getClusterId(), request.getGroupName());
            msg.setResult(false);
            msg.setErrorMsg("参数错误");
            return msg;
        }

        if ((request.getVmRole().equalsIgnoreCase("core") && request.getScaleOutCount() > INSTANCE_GROUP_CORE_TOTAL_COUNT) ||
                (request.getVmRole().equalsIgnoreCase("task") && request.getScaleOutCount() > INSTANCE_GROUP_TASK_TOTAL_COUNT)) {
            msg.setResult(false);
            msg.setErrorMsg("该类型实例扩容总数超上限");
            return msg;
        }

        String lockKey = "request-scale:" + request.getClusterId() + ":" + request.getGroupName();
        boolean lockResult = this.redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
        if (!lockResult) {
            msg.setResult(false);
            msg.setErrorMsg("执行冲突，请重新提交");
            return msg;
        }

        try {
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(request.getClusterId());
            if (confCluster == null) {
                msg.setResult(false);
                msg.setErrorMsg("集群不存在");
                return msg;
            }

            if (request.getScaleOutCount() <= 0) {
                SdpExceptionUtil.wrapRuntimeAndThrow("扩容数量不正确,count={}", request.getScaleOutCount());
            }

            // Core实例组扩容时，检查扩容上限
            if ("CORE".equalsIgnoreCase(request.getVmRole())) {
                // HBase集群，检查一次最多只能扩容5台
                if (isHbaseCluster(confCluster) && request.getScaleOutCount() > 5) {
                    getLogger().error("HBase集群 Core实例组一次最多可以扩容5台，当前扩容数量={}", request.getScaleOutCount());
                    SdpExceptionUtil.wrapRuntimeAndThrow("HBase集群, Core实例组一次最多可以扩容5台，当前扩容数量={}", request.getScaleOutCount());
                }
            }
            // endregion

            // region 获取当前vm实例信息
            ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(request.getClusterId(), request.getVmRole(), request.getGroupName());
            if (confClusterVm == null) {
                msg.setResult(false);
                msg.setErrorMsg("无该实例信息");
                return msg;
            }
            //endregion

            //region 竞价实例扩容
            //如果是竞价实例的扩容交给竞价服务处理,此处只更新申请实例数量
            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(request.getClusterId(), request.getGroupName());
            if (hostGroup == null) {
                SdpExceptionUtil.wrapRuntimeAndThrow("实例组信息错误,clusterId={},groupName={}", request.getClusterId(), request.getGroupName());
            }
            if (PurchaseType.Spot.equalValue(hostGroup.getPurchaseType())) {
                hostGroup.setExpectCount(NumberUtil.add(hostGroup.getExpectCount(), request.getScaleOutCount()).intValue());
                confClusterHostGroupMapper.updateByPrimaryKeySelective(hostGroup);
                return ResultMsg.SUCCESS();
            }
            //endregion

            // region 构造扩容任务
            ConfScalingTask confScalingTask = new ConfScalingTask();
            try {
                BeanUtils.copyProperties(confScalingTask, request);
            } catch (Exception e) {
                getLogger().error("AdminApiServiceImpl.clusterScaleOut, request to confScalingTask error. request: {}, e: {}", JSON.toJSONString(request), e);
                msg.setResult(false);
                msg.setMsg("AdminApiServiceImpl.clusterScaleOut error.");
            }
            //endregion

            //region 查询当前集群信息
            List<InfoClusterVm> infoClusterVms =
                    infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                            request.getClusterId(),
                            request.getGroupName(),
                            InfoClusterVm.VM_RUNNING);
            //endregion

            int afterScaleCount = infoClusterVms.size() + request.getScaleOutCount();
            if (afterScaleCount > request.getExpectCount()) {
                getLogger().error("can not start scale out, after scale count:{} greater than request expect count:{}, clusterId:{}, groupName:{}",
                        afterScaleCount,
                        request.getExpectCount(),
                        request.getClusterId(),
                        request.getGroupName());
                msg.setResult(false);
                msg.setErrorMsg("无法扩容，实际结果将超出扩容期望数量" + request.getExpectCount() + ",请关闭页面刷新再重试");
                return msg;
            } else if (afterScaleCount != request.getExpectCount()) {
                getLogger().error("can not start scale out, after scale count:{} not equals request expect count:{}, clusterId:{}, groupName:{}",
                        afterScaleCount,
                        request.getExpectCount(),
                        request.getClusterId(),
                        request.getGroupName());
                msg.setResult(false);
                msg.setErrorMsg("无法扩容，数量验证不通过,请关闭页面刷新再重试");
                return msg;
            }

            //region 数据补充
            confScalingTask.setTaskId(UUID.randomUUID().toString());
            confScalingTask.setScalingType(ConfScalingTask.ScaleType_OUT);
            confScalingTask.setEsRuleId(null);
            confScalingTask.setEsRuleName(null);
            confScalingTask.setBeforeScalingCount(infoClusterVms.size());
            confScalingTask.setAfterScalingCount(afterScaleCount);
            confScalingTask.setScalingCount(request.getScaleOutCount());
            confScalingTask.setExpectCount(request.getExpectCount());
            confScalingTask.setIsGracefulScalein(null);
            confScalingTask.setScaleinWaitingtime(null);
            confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
            if (Objects.nonNull(request.getScaleOperationType())) {
                confScalingTask.setOperatiionType(request.getScaleOperationType());
            }
            confScalingTask.setEnableAfterstartScript(request.getEnableAfterstartScript());
            confScalingTask.setEnableBeforestartScript(request.getEnableBeforestartScript());
            confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingTask.setBegTime(new Date());
            confScalingTask.setVmRole(confScalingTask.getVmRole().toLowerCase());
            confScalingTask.setGroupName(request.getGroupName());
            confScalingTask.setCreatedBy(request.getCreatedBy());
            confScalingTask.setCreateTime(new Date());
            //endregion

            //region 扩容任务
            return composeService.createScaleOutTask(confScalingTask);
            //endregion
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        } finally {
            this.redisLock.unlock(lockKey);
        }
    }

    private boolean isHbaseCluster(ConfCluster confCluster) {
        boolean isHbaseCluster = false;
        ConfClusterApp appArg = new ConfClusterApp();
        appArg.setClusterId(confCluster.getClusterId());
        List<ConfClusterApp> apps = confClusterAppMapper.selectByObject(appArg);
        for (ConfClusterApp app : apps) {
            if ("HBASE".equalsIgnoreCase(app.getAppName())) {
                isHbaseCluster = true;
                break;
            }
        }
        return isHbaseCluster;
    }

    /**
     * 集群实例缩容
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg clusterScaleIn(ClusterScaleOutOrScaleInRequest request) {
        getLogger().info("clusterScaleIn:{}", request);

        ResultMsg msg = new ResultMsg();
        // region 参数校验（注释）
        if (StrUtil.isEmpty(request.getClusterId())) {
            getLogger().error("clusterScaleOut error,clusterId is null");
            msg.setResult(false);
            msg.setErrorMsg("集群参数未找到");
            return msg;
        }

        if (StrUtil.isEmpty(request.getGroupName())) {
            getLogger().error("clusterScaleOut error,groupName is null");
            msg.setResult(false);
            msg.setErrorMsg("实例组参数未找到");
            return msg;
        }

        if (request.getExpectCount() == null) {
            msg.setResult(false);
            msg.setErrorMsg("参数错误期望数量为空");
            return msg;
        }

        // region 参数校验（注释）
        if (request.getScaleInCount() == null || request.getIsGracefulScalein() == null) {
            msg.setResult(false);
            msg.setErrorMsg("参数错误");
            return msg;
        }

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(request.getClusterId());
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("集群不存在");
            return msg;
        }

        if (Objects.isNull(request.getForceScaleinDataNode())) {
            request.setForceScaleinDataNode(ConfScalingTask.FORCE_SCALEIN_NO);
        }

        if (request.getScaleInCount() <= 0) {
            msg.setResult(false);
            msg.setErrorMsg("缩容数量不正确");
            return msg;
        }

        String lockKey = "request-scale:" + request.getClusterId() + ":" + request.getGroupName();
        boolean lockResult = this.redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 300);
        if (!lockResult) {
            msg.setResult(false);
            msg.setErrorMsg("执行冲突，请重新提交");
            return msg;
        }

        try {
            int createOrRunningCount = this.confScalingTaskNeoMapper.countByScalingTypeAndState(request.getClusterId(),
                    request.getGroupName(),
                    ConfScalingTask.ScaleType_OUT,
                    ConfScalingTask.ScaleType_IN,
                    ConfScalingTask.SCALINGTASK_Create,
                    ConfScalingTask.SCALINGTASK_Running);
            if (createOrRunningCount > 0) {
                msg.setResult(false);
                msg.setErrorMsg("当前实例组存在正在执行的扩缩容任务，请稍后提交");
                return msg;
            }

            // region 获取当前vm实例信息
            request.setVmRole(request.getVmRole().toLowerCase());
            ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(request.getClusterId(), request.getVmRole(), request.getGroupName());
            if (confClusterVm == null) {
                msg.setResult(false);
                msg.setErrorMsg("无该实例信息");
                return msg;
            }
            //endregion

            //region 竞价实例扩容
            //如果是竞价实例的扩容交给竞价服务处理,此处只更新申请实例数量
            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(request.getClusterId(), request.getGroupName());
            if (hostGroup == null) {
                SdpExceptionUtil.wrapRuntimeAndThrow("实例组信息错误,clusterId={},groupName={}", request.getClusterId(), request.getGroupName());
            }
            //endregion

            //region 查询当前集群信息
            List<InfoClusterVm> infoClusterVms =
                    infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(
                            request.getClusterId(),
                            request.getGroupName(),
                            InfoClusterVm.VM_RUNNING);
            // endregion

            if ("CORE".equalsIgnoreCase(request.getVmRole())) {
                if (infoClusterVms.size() != request.getExpectCount() + request.getScaleInCount()) {
                    msg.setResult(false);
                    msg.setErrorMsg("缩容数量错误,请刷新页面重新提交");
                    return msg;
                }
            } else if ("TASK".equalsIgnoreCase(request.getVmRole())) {
                if (infoClusterVms.size() != request.getExpectCount() + request.getScaleInCount()) {
                    if (Objects.equals(hostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                        int originalScaleInCount = request.getScaleInCount();
                        request.setScaleInCount(infoClusterVms.size() - request.getExpectCount());
                        if (request.getScaleInCount() < 0) {
                            request.setScaleInCount(0);
                        }
                        getLogger().info("spot host group scale in,update scale in count from:{} to:{},clusterId:{},groupName:{}",
                                originalScaleInCount,
                                request.getScaleInCount(),
                                request.getClusterId(),
                                request.getGroupName());
                    } else {
                        msg.setResult(false);
                        msg.setErrorMsg("缩容数量错误,请刷新页面重新提交");
                        return msg;
                    }
                }
            }

            // region 数据核实
            if (infoClusterVms.size() < request.getExpectCount()) {
                if (Objects.equals(hostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                    getLogger().info("spot host group scale in,update expect count from:{} to:{}",
                            hostGroup.getExpectCount(),
                            request.getExpectCount());
                    hostGroup.setExpectCount(request.getExpectCount());
                    hostGroup.setModifiedTime(new Date());
                    hostGroup.setModifiedby(request.getUser());
                    confClusterHostGroupMapper.updateByPrimaryKeySelective(hostGroup);
                    msg.setResult(true);
                    return msg;
                } else {
                    msg.setResult(false);
                    msg.setErrorMsg("缩容数量错误。");
                    return msg;
                }
            }
            // endregion

            int afterScaleCount = infoClusterVms.size() - request.getScaleInCount();
            if (VmRoleType.CORE.equalValue(request.getVmRole())) {
                if (afterScaleCount < scaleInMaxCount) {
                    msg.setResult(false);
                    msg.setErrorMsg("至少保存3台core实例，不能再缩容");
                    return msg;
                }
            }

            // region 构造缩容任务
            ConfScalingTask confScalingTask = new ConfScalingTask();
            try {
                BeanUtils.copyProperties(confScalingTask, request);
            } catch (Exception e) {
                getLogger().error("AdminApiServiceImpl.clusterScaleOut, request to confScalingTask error. request: {}, e: {}", JSON.toJSONString(request), e);
                msg.setResult(false);
                msg.setMsg("AdminApiServiceImpl.clusterScaleOut error.");
            }

            if (hostGroup.getExpectCount() == null) {
                getLogger().warn("expect count lost,fix by infoClusterVms size,clusterId:{},groupId:{},groupName:{}",
                        hostGroup.getClusterId(),
                        hostGroup.getGroupId(),
                        hostGroup.getGroupName());
                hostGroup.setExpectCount(infoClusterVms.size());
            }

            if (Objects.equals(hostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                if (hostGroup.getExpectCount().compareTo(request.getExpectCount()) <= 0) {
                    getLogger().error("can't start scale in, expect count:{} less eq than request expect count:{}, clusterId:{}, groupName:{}",
                            afterScaleCount,
                            request.getExpectCount(),
                            request.getClusterId(),
                            request.getGroupName());
                    msg.setResult(false);
                    msg.setErrorMsg("无法缩容,无法从当前期望" + hostGroup.getExpectCount() + "缩容到目标期望数量" + request.getExpectCount());
                    return msg;
                }
            } else {
                if (afterScaleCount < request.getExpectCount()) {
                    getLogger().error("can't start scale in, after scale count:{} less than request expect count:{}, clusterId:{}, groupName:{}",
                            afterScaleCount,
                            request.getExpectCount(),
                            request.getClusterId(),
                            request.getGroupName());
                    msg.setResult(false);
                    msg.setErrorMsg("无法缩容,无法缩容至期望数量" + request.getExpectCount() + ",请关闭页面刷新再重试");
                    return msg;
                } else if (afterScaleCount != request.getExpectCount()) {
                    getLogger().error("can't start scale in, after scale count:{} not equals request expect count:{}, clusterId:{}, groupName:{}",
                            afterScaleCount,
                            request.getExpectCount(),
                            request.getClusterId(),
                            request.getGroupName());
                    msg.setResult(false);
                    msg.setErrorMsg("无法缩容,数量验证不通过,请关闭页面刷新再重试");
                    return msg;
                }
            }

            // 数据补充
            confScalingTask.setTaskId(UUID.randomUUID().toString());
            //（1扩容，2缩容）
            confScalingTask.setScalingType(2);
            confScalingTask.setEsRuleId(null);
            confScalingTask.setEsRuleName(null);
            confScalingTask.setBeforeScalingCount(infoClusterVms.size());
            confScalingTask.setAfterScalingCount(afterScaleCount);
            confScalingTask.setScalingCount(request.getScaleInCount());
            confScalingTask.setExpectCount(request.getExpectCount());
            confScalingTask.setIsGracefulScalein(request.getIsGracefulScalein());
            confScalingTask.setScaleinWaitingtime(request.getScaleinWaitingtime());
            confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
            if (Objects.nonNull(request.getScaleOperationType())) {
                confScalingTask.setOperatiionType(request.getScaleOperationType());
            }
            confScalingTask.setVmRole(confScalingTask.getVmRole().toLowerCase());
            confScalingTask.setGroupName(request.getGroupName());
            confScalingTask.setBegTime(new Date());
            confScalingTask.setCreateTime(new Date());
            confScalingTask.setScaleoutTaskId(request.getScaleByTaskId());
            confScalingTask.setDeleteGroup(request.getDeleteGroup());
            confScalingTask.setForceScaleinDataNode(request.getForceScaleinDataNode());
            confScalingTask.setCreatedBy(request.getCreatedBy());
            //endregion

            getLogger().info("composeService.createScaleInTask begin confScalingTask:{}", confScalingTask);
            //region 缩容任务
            return composeService.createScaleInTask(confScalingTask);
            //endregion
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        } finally {
            this.redisLock.unlock(lockKey);
        }
    }

    @Override
    public ResultMsg clusterCancelScalingTask(ClusterCancelScalingTaskRequest request) {
        getLogger().info("clusterCancelScalingTask:{}", request);

        ResultMsg msg = new ResultMsg();
        // region 参数校验（注释）
        if (StrUtil.isEmpty(request.getClusterId())) {
            getLogger().error("clusterCancelScalingTask error,clusterId is null");
            msg.setResult(false);
            msg.setErrorMsg("集群参数未找到");
            return msg;
        }

        if (StrUtil.isEmpty(request.getGroupName())) {
            getLogger().error("clusterCancelScalingTask error,groupName is null");
            msg.setResult(false);
            msg.setErrorMsg("实例组参数未找到");
            return msg;
        }

        if (StrUtil.isEmpty(request.getTaskId())) {
            getLogger().error("clusterCancelScalingTask error,taskId is null");
            msg.setResult(false);
            msg.setErrorMsg("任务参数未找到");
            return msg;
        }

        String lockKeyForTask = "start_task_plan_lock:" + request.getTaskId();
        if (redisLock.tryLock(lockKeyForTask)) {
            try {
                ConfScalingTask confScalingTask = confScalingTaskNeoMapper.selectByPrimaryKey(request.getTaskId());
                if (confScalingTask == null) {
                    getLogger().error("clusterCancelScalingTask error,task not found,taskId:{}", request.getTaskId());
                    msg.setResult(false);
                    msg.setErrorMsg("任务数据未找到");
                    return msg;
                }

                if (!confScalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Create)) {
                    getLogger().error("clusterCancelScalingTask error,task state is not CREATED,task:{}", confScalingTask);
                    msg.setResult(false);
                    msg.setErrorMsg("当前任务状态无法执行删除操作");
                    return msg;
                }

                if (Objects.equals(confScalingTask.getScalingType(), ConfScalingTask.ScaleType_Part_OUT)) {
                    if (StringUtils.startsWith(confScalingTask.getEsRuleName(), "磁盘扩容拆分")) {
                        getLogger().error("clusterCancelScalingTask error,task scalingType is ScaleType_Part_OUT and is split scale task,task:{}", confScalingTask);
                        msg.setResult(false);
                        msg.setErrorMsg("磁盘扩容拆分任务无法执行删除操作");
                        return msg;
                    }
                }

                confScalingTask.setBegTime(new Date());
                confScalingTask.setEndTime(new Date());
                confScalingTask.setState(ConfScalingTask.SCALINGTASK_Failed);
                confScalingTask.setInQueue(ConfScalingTask.NOT_IN_TAKS_WAIT_QUEUE);
                confScalingTask.setRemark(String.format("被%s(%s)手动删除", request.getUserRealName(), request.getUser()));
                confScalingTaskNeoMapper.updateByPrimaryKeySelective(confScalingTask);
                getLogger().info("clusterCancelScalingTask update:{}", confScalingTask);
                if (Objects.equals(confScalingTask.getScalingType(), ConfScalingTask.ScaleType_IN)) {
                    this.infoClusterVmMapper.cleanScaleinTaskId(confScalingTask.getClusterId(), confScalingTask.getTaskId());
                    getLogger().info("clusterCancelScalingTask cleanScaleinTaskId clusterId:{},taskId:{}",
                            confScalingTask.getClusterId(),
                            confScalingTask.getTaskId());
                }
                msg.setResult(true);
                return msg;
            } finally {
                redisLock.tryUnlock(lockKeyForTask);
            }
        } else {
            getLogger().error("clusterCancelScalingTask error,task is locked by other action,taskId:{}", request.getTaskId());
            msg.setResult(false);
            msg.setErrorMsg("当前任务已被其它操作锁定，无法进行删除操作");
            return msg;
        }
    }

    /**
     * 弹性伸缩记录
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg clusterScalingLog(ClusterScalingLogData request) {
        ResultMsg msg = new ResultMsg();
        List<ClusterScalingLogData> clusterScalingLogDataList = new ArrayList<>();

        // 组装查询参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("clusterId", request.getClusterId());
        paramMap.put("vmRole", request.getVmRole());
        paramMap.put("groupName", request.getGroupName());
        paramMap.put("state", request.getState());
        paramMap.put("esRuleName", request.getEsRuleName());
        paramMap.put("begTime", request.getBegTime());
        paramMap.put("endTime", request.getEndTime());

        if (StrUtil.isNotEmpty(request.getOperatiionType())) {
            // 只展示弹性伸缩数据
            paramMap.put("operatiionType", request.getOperatiionType());
        }
        if (Objects.nonNull(request.getScalingType())) {
            // 伸缩类型（1扩容，2缩容）
            paramMap.put("scalingType", request.getScalingType());
        }
        paramMap.put("scalingTypes", request.getScalingTypes());
        // 查队列中的任务
        paramMap.put("inQueue", request.getInQueue());
        paramMap.put("logFlag", request.getLogFlag());
        getLogger().info("查询任务日志参数:{}", paramMap);
        int total = confScalingTaskNeoMapper.selectCountByParams(paramMap);

        List<ConfScalingTask> confScalingTaskList = null;
        if (!Objects.equals(request.getLogFlag(), LOG_FLAG_ONE_DAY_CHANG)) {
            if (request.getPageIndex() == null || request.getPageSize() == null) {
                request.setPageIndex(1);
                request.setPageSize(20);
            }
            paramMap.put("page", (request.getPageIndex() - 1) * (request.getPageSize()));
            paramMap.put("size", request.getPageSize());
            if (request.getInQueue() == 1) {
                confScalingTaskList = confScalingTaskNeoMapper.selectByParamsOrderByCreateTimeAsc(paramMap);
            } else {
                confScalingTaskList = confScalingTaskNeoMapper.selectByParamsOrderByBeginTimeDesc(paramMap);
            }
        } else {
            confScalingTaskList = confScalingTaskNeoMapper.selectByParamsOrderByEndTimeAsc(paramMap);
        }

        if (!CollectionUtils.isEmpty(confScalingTaskList)) {
            for (ConfScalingTask confScalingTask : confScalingTaskList) {
                try {
                    ClusterScalingLogData clusterScalingLogData = new ClusterScalingLogData();
                    BeanUtils.copyProperties(clusterScalingLogData, confScalingTask);
                    clusterScalingLogDataList.add(clusterScalingLogData);
                } catch (Exception e) {
                    getLogger().error("AdminApiServiceImpl.clusterScalingLog, confScalingTask to clusterScalingLogData error. confScalingTask: {}, e: {}", JSON.toJSONString(confScalingTask), e);
                }
            }
        }

        msg.setResult(true);
        if (Objects.nonNull(request.getLogFlag())) {
            clusterScalingLogDataList.sort(new Comparator<ClusterScalingLogData>() {
                @Override
                public int compare(ClusterScalingLogData o1, ClusterScalingLogData o2) {
                    return Double.valueOf(o1.getEndTime().getTime() - o2.getEndTime().getTime()).intValue();
                }
            });
        }
        msg.setData(clusterScalingLogDataList);
        msg.setTotal(total);
        return msg;
    }

    /**
     * 获取弹性伸缩规则
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg getElasticScalingRule(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();

        // 参数校验
        String clusterId = request.getClusterId();
        String groupName = request.getGroupName();
        String vmRole = request.getVmRole();
        if (StringUtils.isBlank(clusterId) || StringUtils.isBlank(groupName) || StringUtils.isBlank(vmRole)) {
            msg.setResult(false);
            msg.setErrorMsg("参数缺失");
            return msg;
        }

        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(clusterId, groupName);
        if (confClusterHostGroup == null) {
            msg.setResult(false);
            msg.setErrorMsg("实例组信息未找到");
            return msg;
        }

        if (Objects.equals(confClusterHostGroup.getState(), ConfClusterHostGroup.STATE_CREATING)) {
            msg.setResult(false);
            msg.setErrorMsg("实例组正在创建中");
            return msg;
        }
        //判断竞价实例组节点数量是否达到期望值
        // if (Objects.equals(confClusterHostGroup.getPurchaseType(), PurchaseType.Spot.getPurchaseType())
        //         && !Objects.equals(confClusterHostGroup.getInsCount(), confClusterHostGroup.getExpectCount())) {
        //     msg.setResult(false);
        //     msg.setErrorMsg("节点实际数量不等于期望数量，请等待实例组扩缩容完成!");
        //     return msg;
        // }


        // 每个实例组都有弹性扩缩容数据, 如果没有, 则生成一个默认的.
        ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
        List<ConfGroupElasticScaling> confGroupElasticScalingList = confGroupElasticScalingMapper.listByClusterIdAndGroupNameAndValid(clusterId, groupName);
        ConfGroupElasticScaling confGroupElasticScaling = null;
        String uuid = UUID.randomUUID().toString();
        if (confGroupElasticScalingList.isEmpty()) {
            // 如果不存在，进行初始化（仅限于：task）
            if (vmRole.equalsIgnoreCase("task")) {
                ConfGroupElasticScaling initElasticScaling = new ConfGroupElasticScaling();
                initElasticScaling.setGroupEsId(uuid);
                initElasticScaling.setClusterId(clusterId);
                initElasticScaling.setGroupName(groupName);
                initElasticScaling.setVmRole(StringUtils.lowerCase(request.getVmRole()));
                initElasticScaling.setMaxCount(200);
                initElasticScaling.setMinCount(0);
                initElasticScaling.setIsValid(ConfGroupElasticScaling.ISVALID_YES);
                initElasticScaling.setCreatedby("sdpadmin");
                initElasticScaling.setCreatedTime(new Date());
                initElasticScaling.setIsFullCustody(request.getIsFullCustody()==null?0:request.getIsFullCustody());
                confGroupElasticScalingMapper.insert(initElasticScaling);
                confGroupElasticScaling = initElasticScaling;
            } else {
                getLogger().warn("getElasticScalingRule failure, invalid vmRole:{}", vmRole);
                msg.setResult(false);
                msg.setMsg("只有task节点才能配置弹性伸缩规则");
                return msg;
            }
        } else {
            confGroupElasticScaling = confGroupElasticScalingList.get(0);
            getLogger().info("获取弹性伸缩配置: {}",JsonMapper.nonEmptyMapper().toJson(confGroupElasticScaling));
            String lockKey = "repair-ConfGroupElasticScaling:" + clusterId + ":" + groupName;
            if (redisLock.tryLock(lockKey)) {
                try {
                    for (int index = 1; index < confGroupElasticScalingList.size(); index++) {
                        ConfGroupElasticScaling repairRecord = new ConfGroupElasticScaling();
                        repairRecord.setGroupEsId(confGroupElasticScalingList.get(index).getGroupEsId());
                        repairRecord.setIsValid(ConfGroupElasticScaling.ISVALID_NO);
                        repairRecord.setModifiedby("maintainer");
                        repairRecord.setModifiedTime(new Date());
                        confGroupElasticScalingMapper.updateByPrimaryKey(repairRecord);
                    }
                } finally {
                    redisLock.tryUnlock(lockKey);
                }
            }
        }

        queryFullCustodyParam(confGroupElasticScaling, confGroupElasticScalingData);

        String groupEsId = confGroupElasticScaling.getGroupEsId();
        List<ConfGroupElasticScalingRule> confGroupElasticScalingRuleList = confGroupElasticScalingRuleMapper.selectAllByGroupEsIdAndValid(groupEsId);

        try {
            copyPropertiesToData(confGroupElasticScaling, confGroupElasticScalingData);
//            BeanUtils.copyProperties(confGroupElasticScalingData, confGroupElasticScaling);
        } catch (Exception e) {
            getLogger().error("AdminApiServiceImpl.getElasticScalingRule copyProperties error. confGroupElasticScaling: {}, e: {}", JSON.toJSONString(confGroupElasticScaling), e);
        }
        getLogger().info("获取弹性伸缩配置 to model: {}",JsonMapper.nonEmptyMapper().toJson(confGroupElasticScalingData));
        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(confGroupElasticScalingRuleList)) {
            for (ConfGroupElasticScalingRule confGroupElasticScalingRule : confGroupElasticScalingRuleList) {
                ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData = new ConfGroupElasticScalingRuleData();
                try {
                    BeanUtils.copyProperties(confGroupElasticScalingRuleData, confGroupElasticScalingRule);
                    confGroupElasticScalingRuleData.setLastExecuteTime(getScalingRuleLastExecuteTime(confGroupElasticScaling.getClusterId(), confGroupElasticScalingRule.getEsRuleId()));
                } catch (Exception e) {
                    getLogger().error("AdminApiServiceImpl.getElasticScalingRule copyProperties error. confGroupElasticScalingRule: {}, e: {}", JSON.toJSONString(confGroupElasticScalingRule), e);
                }

                InfoGroupElasticScalingRuleLog infoGroupElasticScalingRuleLog = infoGroupElasticScalingRuleLogMapper.findTop1OrderByDesc(confGroupElasticScalingRule.getClusterId(),
                        confGroupElasticScalingRule.getEsRuleId());

                if (infoGroupElasticScalingRuleLog != null) {
                    confGroupElasticScalingRuleData.setLastComputedTime(infoGroupElasticScalingRuleLog.getCreatedTime());
                }
                confGroupElasticScalingRuleDataList.add(confGroupElasticScalingRuleData);
            }
        }

        // 检查集群是否扩缩容
        boolean isScaling = checkClusterIsScaling(clusterId, groupName);
        if (isScaling) {
            confGroupElasticScalingData.setFreezingEndTime("集群扩缩容中");
        } else {
            // 检查最新的扩缩容结束时间
            confGroupElasticScalingData.setFreezingEndTime(getFreezingEndTime(confGroupElasticScalingRuleDataList));
        }

        confGroupElasticScalingData.setScalingRules(confGroupElasticScalingRuleDataList);
        msg.setResult(true);
        msg.setData(confGroupElasticScalingData);
        return msg;
    }

    private void copyPropertiesToData(ConfGroupElasticScaling src, ConfGroupElasticScalingData dest) {
        // 实例组弹性配置ID
        dest.setGroupEsId(src.getGroupEsId());

        // 集群ID
        dest.setClusterId(src.getClusterId());

        // 实例组名称
        dest.setGroupName(src.getGroupName());

        // 实例角色
        dest.setVmRole(src.getVmRole());

        // 实例组最大实例数
        dest.setMaxCount(src.getMaxCount());

        // 实例组最小实例数
        dest.setMinCount(src.getMinCount());
        /**
         * 是否优雅缩容
         */
        dest.setIsGracefulScalein(src.getIsGracefulScalein());
        /**
         * 优雅缩容等待时间单位：分钟
         */
        dest.setScaleinWaitingTime(src.getScaleinWaitingTime());
        /**
         * 扩容是否执行启动前脚本
         */
        dest.setEnableBeforestartScript(src.getEnableBeforestartScript());
        /**
         * 扩容是否执行启动后脚本
         */
        dest.setEnableAfterstartScript(src.getEnableAfterstartScript());
        /**
         * 是否开启全托管弹性伸缩
         */
        dest.setIsFullCustody(src.getIsFullCustody());

        /**
         * 开启全托管伸缩后的自定义参数
         */
        dest.setFullCustodyParam(src.getFullCustodyParamObject());


        // 弹性伸缩时间限制
        dest.setScalingLimitTime(src.getScalingLimitTime());

        // 是否有效
        dest.setIsValid(src.getIsValid());
    }

    /**
     * 查全托管数据
     * @param confGroupElasticScaling
     * @param confGroupElasticScalingData
     */
    private void queryFullCustodyParam(ConfGroupElasticScaling confGroupElasticScaling, ConfGroupElasticScalingData confGroupElasticScalingData) {
        FullCustodyParam custodyParam = FullCustodyParam.parse(confGroupElasticScaling.getFullCustodyParam());
        confGroupElasticScalingData.setFullCustodyParam(custodyParam);

        String scaleoutMetric = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_SCALE_HOSTING,
                FullCustodyParam.SCALE_HOSTING_SCALEOUT_METRIC, String.class);
        Integer memoryThreshold = bizConfigService.getConfigValue(BizConfigConstants.CATEGORY_SCALE_HOSTING,
                FullCustodyParam.SCALE_HOSTING_SCALEIN_MEMORY_THRESHOLD, Integer.class);
        FullCustodyParam defParam = new FullCustodyParam();
        defParam.setScaleoutMetric(scaleoutMetric);
        defParam.setScaleinMemoryThreshold(memoryThreshold);
        confGroupElasticScalingData.setDefaultFullCustodyParam(defParam);
    }

    private boolean checkClusterIsScaling(String clusterId, String groupName) {
        Map<String, Object> params = new HashMap<>();
        params.put("clusterId", clusterId);
        params.put("groupName", groupName);
        params.put("state", "1");
        List<ConfScalingTask> confScalingTasks = confScalingTaskNeoMapper.selectByParams(params);
        return !CollectionUtils.isEmpty(confScalingTasks);
    }

    private String getFreezingEndTime(List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList) {
        if (CollectionUtil.isEmpty(confGroupElasticScalingRuleDataList)) {
            return DateUtil.getCurrentSimpleDate();
        }

        // 遍历找到
        ConfGroupElasticScalingRuleData lastExecuteRule = null;
        for (ConfGroupElasticScalingRuleData rule : confGroupElasticScalingRuleDataList) {
            if (Objects.isNull(rule.getLastExecuteTime())) {
                // 没有最后执行时间时，检查下一条
                continue;
            }

            // 找到最近一条执行扩缩容的规则
            if (Objects.isNull(lastExecuteRule)) {
                lastExecuteRule = rule;
            } else {
                if (rule.getLastExecuteTime().after(lastExecuteRule.getLastExecuteTime())) {
                    lastExecuteRule = rule;
                }
            }
        }

        if (Objects.isNull(lastExecuteRule)) {
            return DateUtil.getCurrentSimpleDate();
        }

        Date freezingTime = DateUtils.addSeconds(lastExecuteRule.getLastExecuteTime(), lastExecuteRule.getFreezingTime());
        return cn.hutool.core.date.DateUtil.format(freezingTime, "yyyy-MM-dd HH:mm:ss");
    }

    // 获取对应弹性伸缩规则的最后一次执行时间
    private Date getScalingRuleLastExecuteTime(String clusterId, String esRuleId) {
        Date endTime = null;
        Map<String, Object> paramMap = new HashMap<>();

        // 查询参数
        paramMap.put("clusterId", clusterId);
        paramMap.put("esRuleId", esRuleId);
        paramMap.put("page", 0);
        paramMap.put("size", 1);

        List<ConfScalingTask> confScalingTaskList = confScalingTaskNeoMapper.selectAllByEsRuleId(paramMap);
        if (!CollectionUtils.isEmpty(confScalingTaskList)) {
            ConfScalingTask confScalingTask = confScalingTaskList.get(0);
            endTime = confScalingTask.getBegTime();
        }
        return endTime;
    }

    /**
     * 修改实例组弹性伸缩配置
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg updateGroupElasticScaling(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();

        // 参数校验
        if (StringUtils.isBlank(request.getClusterId()) || StringUtils.isBlank(request.getGroupEsId())) {
            msg.setResult(false);
            msg.setErrorMsg("参数错误");
            return msg;
        }

        if (request.getMaxCount()<=request.getMinCount()){
            msg.setResult(false);
            msg.setErrorMsg("最大、最小参数异常。");
            return msg;
        }

        if (request.getMaxCount()<=0){
            msg.setResult(false);
            msg.setErrorMsg("最大参数异常。");
            return msg;
        }
        if (request.getMinCount()<0){
            msg.setResult(false);
            msg.setErrorMsg("最小参数异常。");
            return msg;
        }

        // 集群校验
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(request.getClusterId());
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("集群不存在");
            return msg;
        }

        // 实例组弹性配置ID 有效性校验
        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByPrimaryKey(request.getGroupEsId());
        if (confGroupElasticScaling == null) {
            msg.setResult(false);
            msg.setErrorMsg("未查到有效数据");
            return msg;
        }

        // 更新数据
        if (request.getMaxCount() != null) {
            confGroupElasticScaling.setMaxCount(request.getMaxCount());
        }

        if (request.getMinCount() != null) {
            confGroupElasticScaling.setMinCount(request.getMinCount());
        }
        if (!Objects.isNull(request.getIsFullCustody())) {
            confGroupElasticScaling.setIsFullCustody(request.getIsFullCustody());
        }
        if (!Objects.isNull(request.getIsGracefulScalein())){
            confGroupElasticScaling.setIsGracefulScalein(request.getIsGracefulScalein());
        }
        if (!Objects.isNull(request.getScaleinWaitingTime())){
            confGroupElasticScaling.setScaleinWaitingTime(request.getScaleinWaitingTime());
        }
        if (!Objects.isNull(request.getEnableAfterstartScript())){
            confGroupElasticScaling.setEnableAfterstartScript(request.getEnableAfterstartScript());
        }
        if (!Objects.isNull(request.getEnableBeforestartScript())){
            confGroupElasticScaling.setEnableBeforestartScript(request.getEnableBeforestartScript());
        }


        if (Objects.equals(1,request.getIsFullCustody())){
            confGroupElasticScalingRuleMapper.deleteByGroupEsId(request.getGroupEsId());
            getLogger().info("开启全托管，删除弹性伸缩规则clusterId:{} groupName:{}",confGroupElasticScaling.getClusterId(),confGroupElasticScaling.getGroupName());
        }

        confGroupElasticScaling.setModifiedby(request.getUserName());
        confGroupElasticScaling.setModifiedTime(new Date());
        confGroupElasticScalingMapper.updateByPrimaryKeySelective(confGroupElasticScaling);
        msg.setResult(true);
        return msg;
    }

    @Override
    public ResultMsg updateGroupESFullCustodyParam(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();
        Assert.notNull(request, "请求参数为空");
        Assert.notBlank(request.getClusterId(), "集群ID不能为空");
        Assert.notBlank(request.getGroupEsId(), "实例组弹性扩缩容ID不能为空");
        Assert.notNull(request.getFullCustodyParam(), "全托管弹性扩缩容参数不能为空");

        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByPrimaryKey(request.getGroupEsId());
        if (confGroupElasticScaling == null) {
            msg.setResult(false);
            msg.setErrorMsg("未查到有效数据, groupESId=" + request.getGroupEsId());
            return msg;
        }

        confGroupElasticScaling.setFullCustodyParam(request.getFullCustodyParam().toJsonString());
        confGroupElasticScaling.setModifiedTime(new Date());
        confGroupElasticScalingMapper.updateByPrimaryKey(confGroupElasticScaling);
        msg.setResult(true);
        return msg;
    }

    /**
     * 添加弹性伸缩规则
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg postElasticScalingRule(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();
        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = request.getScalingRules();

        // 参数校验
        if (StringUtils.isBlank(request.getVmRole()) || CollectionUtils.isEmpty(confGroupElasticScalingRuleDataList)) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误.");
            return msg;
        }

        // 集群校验
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(request.getClusterId());
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("集群不存在");
            return msg;
        }



        String vmRole = request.getVmRole().toLowerCase();
        if (!vmRole.equals("task")) {
            msg.setResult(false);
            msg.setErrorMsg("当前仅允许task角色的实例组设置弹性规则.");
            return msg;
        }

        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(request.getClusterId(),request.getGroupName());
        if(confClusterHostGroup ==null){
            msg.setResult(false);
            msg.setErrorMsg("实例组不存在。");
            return msg;
        }

        if (confClusterHostGroup.getState().equals(ConfClusterHostGroup.STATE_DELETED)){
            msg.setResult(false);
            msg.setErrorMsg("实例组已删除。");
            return msg;
        }
        if (Objects.equals(request.getIsFullCustody(),1)){
            ConfGroupElasticScaling elasticScaling = confGroupElasticScalingMapper.selectByClusterIdAndFullCustodyAndValid(request.getClusterId());
            if (elasticScaling != null){
                msg.setResult(false);
                msg.setErrorMsg("已存在全托管弹性伸缩配置，请先删除后再添加。");
                return msg;
            }
        }

        ConfGroupElasticScaling confGroupElasticScaling =
                confGroupElasticScalingMapper.selectByClusterIdAndGroupNameAndValid(request.getClusterId(), request.getGroupName());
        if (confGroupElasticScaling == null) {
            String uuid = UUID.randomUUID().toString();
            confGroupElasticScaling = new ConfGroupElasticScaling();
            confGroupElasticScaling.setGroupEsId(uuid);
            confGroupElasticScaling.setClusterId(request.getClusterId());
            confGroupElasticScaling.setGroupName(request.getGroupName());
            confGroupElasticScaling.setVmRole(StringUtils.lowerCase(request.getVmRole()));
            confGroupElasticScaling.setMaxCount(200);
            confGroupElasticScaling.setMinCount(0);
            confGroupElasticScaling.setIsValid(ConfGroupElasticScaling.ISVALID_YES);
            confGroupElasticScaling.setCreatedby("sdpadmin");
            confGroupElasticScaling.setCreatedTime(new Date());
            confGroupElasticScaling.setIsFullCustody(request.getIsFullCustody()==null?0:request.getIsFullCustody());
            confGroupElasticScalingMapper.insert(confGroupElasticScaling);
        }

        // 规则名称能不能重复，要不要校验

        for (ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData : confGroupElasticScalingRuleDataList) {
            ConfGroupElasticScalingRule confGroupElasticScalingRule = new ConfGroupElasticScalingRule();
            try {
                BeanUtils.copyProperties(confGroupElasticScalingRule, confGroupElasticScalingRuleData);
            } catch (Exception e) {
                getLogger().error("AdminApiServiceImpl.postElasticScalingRule copyProperties error. confGroupElasticScalingRuleData: {}, e: {}", JSON.toJSONString(confGroupElasticScalingRuleData), e);
            }
            if (StringUtils.isEmpty(confGroupElasticScalingRule.getEsRuleId())){
                confGroupElasticScalingRule.setEsRuleId(UUID.randomUUID().toString());
            }
            confGroupElasticScalingRule.setGroupEsId(confGroupElasticScaling.getGroupEsId());
            confGroupElasticScalingRule.setClusterId(request.getClusterId());
            confGroupElasticScalingRule.setGroupName(request.getGroupName());
            // isValid给予默认值
            if (confGroupElasticScalingRule.getIsValid() == null) {
                confGroupElasticScalingRule.setIsValid(1);
            }
            confGroupElasticScalingRule.setCreatedby(request.getUserName());
            confGroupElasticScalingRule.setCreatedTime(new Date());
            getLogger().info("AdminApiServiceImpl,保存esrule的数据："+JSON.toJSONString(confGroupElasticScalingRule));
            confGroupElasticScalingRuleMapper.insert(confGroupElasticScalingRule);
        }
        elasticScalingRuleChangeNotice();
        msg.setResult(true);
        return msg;
    }

    /**
     * 修改弹性伸缩规则（全量参数）
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg updateElasticScalingRule(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();
        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = request.getScalingRules();

        // 参数校验
        if (CollectionUtils.isEmpty(confGroupElasticScalingRuleDataList)) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误.");
            return msg;
        }

        ConfGroupElasticScaling confGroupElasticScaling =
                confGroupElasticScalingMapper.selectByClusterIdAndGroupNameAndValid(request.getClusterId(), request.getGroupName());
        if (confGroupElasticScaling == null) {
            msg.setResult(false);
            msg.setErrorMsg("弹性伸缩配置不存在.");
            return msg;
        }

        List<ConfGroupElasticScalingRule> confGroupElasticScalingRuleList =
                confGroupElasticScalingRuleMapper.selectAllByGroupEsId(confGroupElasticScaling.getGroupEsId());
        if (CollectionUtils.isEmpty(confGroupElasticScalingRuleList)) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误.");
            return msg;
        }
        Map<String, Object> elasticScalingRuleMap = new HashMap<>();
        for (ConfGroupElasticScalingRule confGroupElasticScalingRule : confGroupElasticScalingRuleList) {
            elasticScalingRuleMap.put(confGroupElasticScalingRule.getEsRuleId(), confGroupElasticScalingRule);
        }

        for (ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData : confGroupElasticScalingRuleDataList) {
            if (StringUtils.isNotBlank(confGroupElasticScalingRuleData.getEsRuleId())) {
                ConfGroupElasticScalingRule oldElasticScalingRule = (ConfGroupElasticScalingRule) elasticScalingRuleMap.get(confGroupElasticScalingRuleData.getEsRuleId());
                try {
                    if(!Objects.equals(oldElasticScalingRule.getScalingType(),confGroupElasticScalingRuleData.getScalingType())){
                        msg.setErrorMsg("弹性规则的伸缩类型不能改变。");
                        msg.setResult(false);
                        return msg;
                    }
                    BeanUtils.copyProperties(oldElasticScalingRule, confGroupElasticScalingRuleData);
                } catch (Exception e) {
                    getLogger().error("AdminApiServiceImpl.updateElasticScalingRule copyProperties error. confGroupElasticScalingRuleData: {}, e: {}", JSON.toJSONString(confGroupElasticScalingRuleData), e);
                }
                oldElasticScalingRule.setModifiedby(request.getUserName());
                oldElasticScalingRule.setModifiedTime(new Date());
                confGroupElasticScalingRuleMapper.updateByPrimaryKey(oldElasticScalingRule);
            }
        }
        elasticScalingRuleChangeNotice();
        msg.setResult(true);
        return msg;
    }

    /**
     * 启停用弹性伸缩规则
     */
    @Override
    public ResultMsg updateElasticScalingRuleValid(String esRuleId, Integer isValid) {
        ResultMsg resultMsg = new ResultMsg();
        if (isValid == null || !(isValid == 0 || isValid == 1)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("弹性伸缩规则启停状态值错误");
            return resultMsg;
        }

        ConfGroupElasticScalingRule confGroupElasticScalingRule = confGroupElasticScalingRuleMapper.selectByPrimaryKey(esRuleId);
        if (confGroupElasticScalingRule == null) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("未查到有效数据");
            return resultMsg;
        }

        if (!isValid.equals(confGroupElasticScalingRule.getIsValid())) {
            confGroupElasticScalingRule.setIsValid(isValid);
            confGroupElasticScalingRule.setModifiedby("sdpadmin");
            confGroupElasticScalingRule.setModifiedTime(new Date());
            confGroupElasticScalingRuleMapper.updateByPrimaryKey(confGroupElasticScalingRule);
            elasticScalingRuleChangeNotice();
        }
        resultMsg.setResult(true);
        resultMsg.setMsg("success");
        return resultMsg;
    }

    /**
     * 删除弹性伸缩规则
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg deleteElasticScalingRule(ConfGroupElasticScalingData request) {
        ResultMsg msg = new ResultMsg();
        List<ConfGroupElasticScalingRuleData> confGroupElasticScalingRuleDataList = request.getScalingRules();

        // 参数校验
        if (StringUtils.isBlank(request.getGroupEsId()) || CollectionUtils.isEmpty(confGroupElasticScalingRuleDataList)) {
            msg.setResult(false);
            msg.setErrorMsg("参数有误.");
            return msg;
        }

        ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalingMapper.selectByPrimaryKey(request.getGroupEsId());
        if (confGroupElasticScaling == null) {
            msg.setResult(false);
            msg.setErrorMsg("弹性伸缩配置不存在.");
            return msg;
        }

        for (ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData : confGroupElasticScalingRuleDataList) {
            confGroupElasticScalingRuleMapper.deleteByPrimaryKey(confGroupElasticScalingRuleData.getEsRuleId());
        }
        elasticScalingRuleChangeNotice();
        msg.setResult(true);
        return msg;
    }

    /**
     * 弹性伸缩规则变更通知
     */
    private void elasticScalingRuleChangeNotice() {
        try {
            scaleService.metricChange();
        } catch (Exception e) {
            getLogger().error("AdminApiServiceImpl.elasticScalingRuleChangeNotice error. e: ", e);
        }
    }

    /**
     * 单个创建VM实例
     */
    @Override
    public ResultMsg createVMInstance(String azureVMInstanceRequest) {
        return composeService.createVMInstance(azureVMInstanceRequest);
    }

    /**
     * 单个删除VM实例
     */
    @Override
    public ResultMsg deleteVMInstance(String vmName) {
        return composeService.deleteVMInstance(vmName);
    }

    /**
     * 批量/单个VM扩容磁盘
     */
    @Override
    public ResultMsg updateVirtualMachinesDiskSize(String azureUpdateVirtualMachinesDiskSizeRequest) {
        return composeService.updateVirtualMachinesDiskSize(azureUpdateVirtualMachinesDiskSizeRequest);
    }

    /**
     * 获取AZ列表
     */
    @Override
    public ResultMsg getAzList(String region) {
        return azureService.getAzList(region);
    }

    /**
     * 获取实例组
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg getVMGroupsByClusterId(String clusterId) {
        ResultMsg msg = new ResultMsg();
        try {
            List<ConfClusterHostGroup> hostGroups = confClusterHostGroupMapper.selectByValidClusterId(clusterId);
            CopyOnWriteArrayList<Map> vms = new CopyOnWriteArrayList<>();
            hostGroups.stream().forEach(x -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("groupName", StringUtils.isNotEmpty(x.getGroupName()) ? x.getGroupName() : x.getVmRole());
                map.put("vmRole", x.getVmRole());
                map.put("count", x.getInsCount());
                vms.add(map);
            });
            msg.setResult(true);
            msg.setData(vms);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            msg.setResult(false);
            msg.setErrorMsg(e.getMessage());
        }
        return msg;
    }

    /**
     * 节点信息概览
     *
     * @param param
     * @return
     */
    public ResultMsg getVmOverview(Map param) {
        ResultMsg msg = new ResultMsg();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(param.get("clusterId").toString());
        if (confCluster == null) {
            msg.setResult(false);
            msg.setErrorMsg("集群不存在, 集群ID=" + param.get("clusterId").toString());
            return msg;
        }

        List<ConfClusterHostGroup> confClusterHostGroups = confClusterHostGroupMapper.selectByClusterId(confCluster.getClusterId());
        List<ConfClusterVm> clusterVms = confClusterVmNeoMapper.selectByClusterId(confCluster.getClusterId());

        if (confClusterHostGroups == null || CollectionUtils.isEmpty(confClusterHostGroups)) {
            msg.setResult(false);
            msg.setErrorMsg("未查到实例组数据, 集群ID=" + confCluster.getClusterId() + " 集群名=" + confCluster.getClusterName());
            return msg;
        }

        // 注意: 此处虽然使用的是InfoClusterVm,但是查询出来的各实例组中VM的数量, 使用的字段是表中不存在的:cnt字段.
        List<InfoClusterVm> runingGroupVms = infoClusterVmMapper.getVMCountByState(confCluster.getClusterId(), InfoClusterVm.VM_RUNNING);
        String vnet = confCluster.getVnet();
        String subnet = confCluster.getSubnet();

        Map<String, Integer> groupNoMap = new HashMap<>();
        groupNoMap.put("Ambari", 0);
        groupNoMap.put("Master", 1);
        groupNoMap.put("Core", 2);
        groupNoMap.put("Task", 3);
        List<VmOverviewResponse> vmOverviewResponseList = new ArrayList<>();
        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.selectByClusterId(confCluster.getClusterId());

        for (ConfClusterHostGroup hostGroup : confClusterHostGroups) {
            String vmRole = StrUtil.upperFirst(hostGroup.getVmRole());
            if (!groupNoMap.containsKey(vmRole)) continue;
            VmOverviewResponse vmOverview = new VmOverviewResponse();
            vmOverview.setGroupName(hostGroup.getGroupName());
            vmOverview.setVmRole(vmRole);
            vmOverview.setSpotState(hostGroup.getSpotState());
            Optional<InfoClusterVm> optVm = runingGroupVms.stream().filter(p -> Objects.equals(p.getGroupName(), hostGroup.getGroupName())).findFirst();
            int count = 0;
            if (optVm.isPresent()) {
                count = optVm.get().getCnt();
            }
            vmOverview.setState(hostGroup.getState());
            Optional<ConfClusterVm> first = clusterVms.stream().filter(p -> Objects.equals(p.getGroupId(), hostGroup.getGroupId())).findFirst();
            if (!first.isPresent()) {
                getLogger().error("ConfClusterVm-不存在:clusterId={},groupId={}", hostGroup.getClusterId(), hostGroup.getGroupId());
                continue;
            }
            ConfClusterVm clusterVm = first.get();
            List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(first.get().getVmConfId());
            if (CollUtil.isNotEmpty(confClusterVmDataVolumes)) {
                ConfClusterVmDataVolume confClusterVmDataVolume = confClusterVmDataVolumes.get(0);
                vmOverview.setDataVolumeSize(confClusterVmDataVolume.getDataVolumeSize());
                vmOverview.setDataVolumeCount(confClusterVmDataVolume.getCount());
                vmOverview.setDataVolumeType(confClusterVmDataVolume.getDataVolumeType());
                vmOverview.setIops(confClusterVmDataVolume.getIops());
                vmOverview.setThroughput(confClusterVmDataVolume.getThroughput());
            }
            vmOverview.setVmCountByRole(count);
            List<String> skuNameList = confHostGroupVmSkus.stream()
                    .filter(confHostGroupVmSku -> confHostGroupVmSku.getVmConfId().equals(clusterVm.getVmConfId()))
                    .map(ConfHostGroupVmSku::getSku)
                    .collect(Collectors.toList());
            //竞价多个sku
            vmOverview.setSkuNames(skuNameList);
            vmOverview.setSku(clusterVm.getSku());
            vmOverview.setVmConfId(clusterVm.getVmConfId());
            vmOverview.setVnet(vnet);
            vmOverview.setSubnet(subnet);
            vmOverview.setPurchaseType(hostGroup.getPurchaseType());
            vmOverview.setExpectCount(hostGroup.getExpectCount());
            vmOverview.setSpotAllocationStrategy(clusterVm.getSpotAllocationStrategy());
            vmOverview.setRegularAllocationStrategy(clusterVm.getRegularAllocationStrategy());
            vmOverview.setVcpus(clusterVm.getVcpus());

            if (vmRole.equalsIgnoreCase("TASK")) {
                vmOverview.setScaleState(VmOverviewResponse.ALLOWED_SET);
                //查询弹性规则
                List<ConfGroupElasticScalingRule> scalingRules = confGroupElasticScalingRuleMapper.selectByClusterIdAndGroupName(hostGroup.getClusterId(), hostGroup.getGroupName());
                List<ConfGroupElasticScalingRule> collect = scalingRules.stream().filter(p -> Objects.equals(p.getIsValid(), ConfGroupElasticScalingRule.ISVALID_YES)).collect(Collectors.toList());
                vmOverview.setScalingRules(collect);
                if (Objects.equals(hostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                    //region 竞价实例
                    ConfClusterVm confClusterVm = this.confClusterVmNeoMapper.selectByAny(hostGroup.getClusterId(), hostGroup.getVmRole(), hostGroup.getGroupName());
                    if (clusterVm != null) {
                        vmOverview.setPriceStrategy(confClusterVm.getPriceStrategy());
                        vmOverview.setMaxPrice(confClusterVm.getMaxPrice());
                    }
                    //新增返回spot实例组Spot买入和逐出任务开关，为NULL 默认 openall
                    vmOverview.setSpotState(hostGroup.getSpotState()==null?ConfClusterHostGroup.SPOTSTATE_OPENALL:hostGroup.getSpotState());
                    //endregion
                }
                //全托管信息
                List<ConfGroupElasticScaling> confGroupElasticScalings = confGroupElasticScalingMapper.listByClusterIdAndGroupName(hostGroup.getClusterId(), hostGroup.getGroupName());
                if (CollUtil.isNotEmpty(confGroupElasticScalings)){
                    ConfGroupElasticScaling confGroupElasticScaling = confGroupElasticScalings.get(0);
                    vmOverview.setIsFullCustody(confGroupElasticScaling.getIsFullCustody());
                }
            } else {
                vmOverview.setScaleState(VmOverviewResponse.NOT_ALLOWD_SET);
            }
            vmOverviewResponseList.add(vmOverview);
        }
        vmOverviewResponseList.sort(Comparator.comparingInt(o -> groupNoMap.get(o.getVmRole())));

        msg.setResult(true);
        msg.setTotal(vmOverviewResponseList.size());
        msg.setData(vmOverviewResponseList);
        return msg;
    }

    public Integer str2Integer(String param) {
        Integer integer = null;
        if (StringUtils.isBlank(param)) {
            return null;
        }
        try {
            integer = Integer.valueOf(param);
        } catch (NumberFormatException e) {
            getLogger().error("AdminApiServiceImpl.str2Integer error. param: {}, e: {}", param, e);
        }
        return integer;
    }

    /**
     * 获取实例列表-带参数 分页
     */
    @Override
    public ResultMsg getVMlistByClusterId(Map param) {
        ResultMsg msg = new ResultMsg();
        // state 前端传值，字符串转Integer
        if (param.get("state") != null) {
            String stateStr = param.get("state").toString();
            Integer state = str2Integer(stateStr);
            param.put("state", state);
        }

        try {
            int totalcount = infoClusterVmMapper.getVMlistCountByParam(param);
            int pageindex = Integer.parseInt(param.get("pageIndex").toString()) - 1;
            int pagesize = Integer.parseInt(param.get("pageSize").toString());
            param.put("pageIndex", pageindex * pagesize);
            List<HashMap<String, Object>> vmList = infoClusterVmMapper.getVMlistByParam(param);
            vmList.forEach(x -> {
                List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(x.get("vm_conf_id").toString());
                if (confClusterVmDataVolumes != null && !confClusterVmDataVolumes.isEmpty()) {
                    x.put("data_volume_size", confClusterVmDataVolumes.get(0).getDataVolumeSize());
                    x.put("data_volume_count", confClusterVmDataVolumes.get(0).getCount());
                }
                //竞价模式,按市场价的比例
                String purchaseType = (String)x.get("purchase_type");
                Integer priceStrategy = (Integer)x.get("price_strategy");
                if(PurchaseType.Spot.getPurchaseType().toString().equals(purchaseType)
                    && SpotPriceStrategy.MARKET.equalValue(priceStrategy)){
                    BigDecimal maxPrice = (BigDecimal)x.get("max_price");
                    BigDecimal ondemondPrice = (BigDecimal)x.get("ondemond_price");
                    BigDecimal spotPrice = (BigDecimal)x.get("spot_price");
                    //出价:出价金额
                    x.put("bidAmount",maxPrice.multiply(ondemondPrice).divide(new BigDecimal("100"),4, RoundingMode.HALF_UP));
                    //成交价:标准价百分比
                    x.put("closingPriceScale",spotPrice.divide(ondemondPrice,4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
                }
            });

            msg.setResult(true);
            msg.setData(vmList);
            msg.setTotal(totalcount);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            msg.setResult(false);
            msg.setErrorMsg(e.getMessage());
            getLogger().error("getvmlist,exception", e);
        }
        return msg;
    }

    @Override
    public ResultMsg queryScriptJobList(String scriptJobListRequest) {
        // 获取参数
        if (StringUtils.isEmpty(scriptJobListRequest)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }

        ResultMsg result = new ResultMsg();
        result.setResult(true);
        try {
            JSONObject jsonObject = JSON.parseObject(scriptJobListRequest);
            if (null != jsonObject && jsonObject.size() > 0) {
                String clusterId = jsonObject.getString("clusterId");
                String jobId = jsonObject.getString("jobId");
                String jobName = jsonObject.getString("jobName");
                String startTime = jsonObject.getString("startTime");
                String endTime = jsonObject.getString("endTime");

                Integer pageIndex = jsonObject.getInteger("pageIndex");
                Integer pageSize = jsonObject.getInteger("pageSize");

                ConfClusterScriptJob scriptJob = new ConfClusterScriptJob();
                scriptJob.setClusterId(clusterId);
                scriptJob.setConfScriptId(jobId);
                scriptJob.setScriptName(jobName);
                scriptJob.setBegTime(StringUtils.isEmpty(startTime) ? null : DateUtil.string2Date(startTime));
                scriptJob.setEndTime(StringUtils.isEmpty(endTime) ? null : DateUtil.string2Date(endTime));
                // 查询数量
                Long count = confClusterScriptMapper.count(scriptJob);
                result.setTotal(count);
                if (count == 0) {
                    return result;
                }

                pageIndex = Objects.isNull(pageIndex) ? 0 : pageIndex - 1;
                pageSize = Objects.isNull(pageSize) ? 20 : pageSize;

                List<ConfClusterScriptJob> scriptJobList = confClusterScriptMapper.queryScriptJob(scriptJob, PageRequest.of(pageIndex, pageSize));

                return this.returnResultMsg(true, convertConfClusterScriptJobToMapList(scriptJobList), "success", count.intValue(), null);
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    private List<Map> convertConfClusterScriptJobToMapList(List<ConfClusterScriptJob> scriptJobList) {
        List<Map> resultList = new ArrayList<>();
        for (ConfClusterScriptJob job : scriptJobList) {
            try {
                Map<String, String> objMap = BeanUtils.describe(job);
                objMap.remove("class");

                // createTime
                String createTimeStr = DateUtil.dateToStr(job.getCreatedTime(), "yyyy-MM-dd HH:mm:ss");
                objMap.put("createTime", createTimeStr);

                // begTime
                String begTimeStr = DateUtil.dateToStr(job.getBegTime(), "yyyy-MM-dd HH:mm:ss");
                objMap.put("begTime", begTimeStr);

                // endtime
                String endTimeStr = DateUtil.dateToStr(job.getEndTime(), "yyyy-MM-dd HH:mm:ss");
                objMap.put("endTime", endTimeStr);

                resultList.add(objMap);

            } catch (Exception ex) {
                getLogger().error(ex.getMessage(), ex);
            }
        }

        return resultList;
    }

    @Override
    public ResultMsg getLogsBlobContainerList() {
        List<Map> resultList = new ArrayList<>();
        //todo region
        ResultMsg resultMsg = null;//azureService.getLogsBlobContainerList();
        getLogger().info("getLogsBlobContainerList " + JSON.toJSONString(resultMsg));
        if (null != resultMsg && resultMsg.getResult()) {
            String dataJsonStr = JSON.toJSONString(resultMsg.getData());
            JSONObject dataJson = JSONObject.parseObject(dataJsonStr);
            if (Objects.nonNull(dataJson)) {
                JSONArray jsonArray = dataJson.getJSONArray("data");
                if (null != jsonArray && jsonArray.size() > 0) {
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                        Map map = new HashMap();
                        map.put("name", jsonObject.getString("name"));
                        map.put("blobContainerUrl", jsonObject.getString("blobContainerUrl"));
                        resultList.add(map);
                    }
                }
            }
        }
        return this.returnResultMsg(true, resultList, "success", null, null);
    }

    @Override
    public ResultMsg deleteGroup(String clusterId, String groupName, String vmRole) {
        getLogger().info("删除实例组开始:clusterId={},groupName={}", clusterId, groupName);
        try {
            ConfScalingTask scalingTask = new ConfScalingTask();
            scalingTask.setClusterId(clusterId);
            scalingTask.setGroupName(groupName);
            scalingTask.setVmRole(VmRoleType.TASK.getVmRole());
            int runningTaskCount = scalingTaskMapper.queryRunningTaskCount(scalingTask);
            if (runningTaskCount > 0) {
                SdpExceptionUtil.wrapRuntimeAndThrow("存在正在扩缩容的任务不允许删除实例组");
            }
            ClusterScaleOutOrScaleInRequest request = new ClusterScaleOutOrScaleInRequest();
            request.setClusterId(clusterId);
            request.setGroupName(groupName);
            List<InfoClusterVm> workvms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, groupName, InfoClusterVm.VM_RUNNING);
            ConfClusterHostGroup hostGroup = new ConfClusterHostGroup();
            hostGroup.setGroupName(groupName);
            hostGroup.setClusterId(clusterId);
            if (workvms.size() > 0) {
                request.setScaleInCount(workvms.size());
                request.setIsGracefulScalein(ConfScalingTask.SCALINGTASK_NOT_GRACEFULSCALEIN);
                request.setDeleteGroup(ConfScalingTask.SCALINGTASK_DELETE_GROUP);
                request.setScaleOperationType(ConfScalingTask.Operation_type_delete_group);
                request.setVmRole(workvms.get(0).getVmRole());
                request.setScaleInCount(workvms.size());
                request.setExpectCount(0);

                ResultMsg clusterScaleInResult = clusterScaleIn(request);
                getLogger().info("删除实例组创建缩容任务返回:{},{}", clusterScaleInResult, clusterScaleInResult.getResult());
                if (!clusterScaleInResult.getResult()) {
                    throw new RuntimeException(StrUtil.format("实例组删除失败,clusterId={},groupName={}", clusterId, groupName));
                } else {
                    hostGroup.setState(ConfClusterHostGroup.STATE_RELEASING);
                }
            } else {
                hostGroup.setState(ConfClusterHostGroup.STATE_DELETED);
                deleteAzureFleet(clusterId, groupName);
            }
            updateElasticScaleValid(hostGroup, ConfGroupElasticScalingRule.ISVALID_NO);
            getLogger().info("删除实例组任务创建成功，更新实例组为删除中的状态，clusterId={},groupName={},vmRole={},size={}", clusterId, groupName, vmRole, workvms.size());

            hostGroup.setVmRole(vmRole);
            confClusterHostGroupMapper.updateByClusterIdAndGroupNameAndVmRole(hostGroup);
            getLogger().info("删除实例组完成:clusterId={},groupName={}", clusterId, groupName);
            return ResultMsg.SUCCESS();
        } catch (Exception ex) {
            getLogger().error("实例组删除失败", ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    /**
     * 如果是当前实例组数量是0时, 也需要把AzureFleet给删掉
     * @param clusterId
     * @param groupName
     */
    private void deleteAzureFleet(String clusterId, String groupName) {
        ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(clusterId, groupName);
        Integer insCount = confClusterHostGroup.getInsCount();
        if (0 == insCount) {
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster != null) {
                getLogger().info("deleteAzureFleet,before deleteAzureFleet clusterId={},groupName={}", clusterId, groupName);
                ResultMsg resultMsg = composeService.deleteAzureFleet(confCluster, groupName);
                getLogger().info("deleteAzureFleet, after deleteAzureFleet clusterId={},groupName={},resultMsg={}", clusterId, groupName, resultMsg);
                if (!resultMsg.getResult()) {
                    throw new RuntimeException(StrUtil.format("实例组删除失败,clusterId={},groupName={}", clusterId, groupName));
                }
            }
        }
    }

    private void updateElasticScaleValid(ConfClusterHostGroup hostGroup, Integer isValid) {
        try {
            getLogger().info("实例组扩缩容完成时更新规则为无效,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid);
            ConfGroupElasticScaling scaling = new ConfGroupElasticScaling();
            scaling.setClusterId(hostGroup.getClusterId());
            scaling.setGroupName(hostGroup.getGroupName());
            scaling.setIsValid(isValid);
            scaling.setIsFullCustody(isValid);
            confGroupElasticScalingMapper.updateValid(scaling);

            ConfGroupElasticScalingRule scalingRule = new ConfGroupElasticScalingRule();
            scalingRule.setClusterId(hostGroup.getClusterId());
            scalingRule.setGroupName(hostGroup.getGroupName());
            scalingRule.setIsValid(isValid);
            confGroupElasticScalingRuleMapper.updateValid(scalingRule);

            scaleService.metricChange();
            getLogger().error("实例组扩缩容完成时更新规则为完成,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid);
        } catch (Exception ex) {
            getLogger().error("实例组扩缩容完成时更新规则为异常,clusterId={},groupName={},isValid={}", hostGroup.getClusterId(), hostGroup.getGroupName(), isValid, ex);
        }
    }

    @Override
    public ResultMsg addGroup(String jsonStr) {
        getLogger().info("begin addGroup:{}", jsonStr);
        AdminSaveClusterRequest adminSaveClusterRequest = JSON.parseObject(jsonStr, AdminSaveClusterRequest.class);
        InstanceGroupSkuCfg instanceGroupSkuCfg = adminSaveClusterRequest.getInstanceGroupSkuCfgs().get(0);
        if (CollUtil.isEmpty(instanceGroupSkuCfg.getGroupCfgs())) {
            instanceGroupSkuCfg.setGroupCfgs(adminSaveClusterRequest.getClusterCfgs());
        }
        String clusterId = adminSaveClusterRequest.getSrcClusterId();
        String groupName = adminSaveClusterRequest.getGroupName();

        String lockKey="addGroup:"+clusterId+":"+groupName;
        boolean lock = redisLock.tryLock(lockKey);
        try {
            if (!lock) {
                return ResultMsg.FAILURE("请勿重复提交请求");
            }
            ConfClusterVm clusterVm = confClusterVmNeoMapper.selectByAny(clusterId, null, groupName);
            if (clusterVm != null) {
                throw new RuntimeException("实例组名重复");
            }

            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                throw new RuntimeException("集群不存在");
            }
            // 检查OS的磁盘是否是Pv2，磁盘OS不能是PV2类型磁盘
            if (instanceGroupSkuCfg.isOSUsePv2DataVolume()) {
                throw new RuntimeException("系统盘不能使用PremiumV2_LRS类型的磁盘");
            }
            //增加添加实例组对集群创建时间的判断
            if (confCluster.getCreatedTime() != null) {
                Date sdpSpotReleaseTime = DateUtils.parseDate(sdp_spot_release_time, "yyyy-MM-dd");
                long deltaTicks = confCluster.getCreatedTime().getTime() - sdpSpotReleaseTime.getTime();
                if (deltaTicks < 0) {
                    getLogger().error("can not create spot group on cluster:{},deltaTicks:{},sdpSpotReleaseTime:{}", confCluster, deltaTicks, sdpSpotReleaseTime);
                    return ResultMsg.FAILURE("当前实例组不支持创建竞价实例组");
                } else {
                    getLogger().info("can create spot group on cluster:{},deltaTicks:{},sdpSpotReleaseTime:{}", confCluster, deltaTicks, sdpSpotReleaseTime);
                }
            }
            adminSaveClusterRequest.setRegion(confCluster.getRegion());
            //添加实例组时,校验竞价相关信息
            this.checkSpot(confCluster.getRegion(), instanceGroupSkuCfg);

            ConfClusterApp clusterApp = new ConfClusterApp();
            clusterApp.setClusterId(clusterId);
            List<ConfClusterApp> clusterApps = confClusterAppMapper.selectByObject(clusterApp);
            InstanceGroupVersion instanceGroupVersion = new InstanceGroupVersion();
            instanceGroupVersion.setClusterReleaseVer(confCluster.getClusterReleaseVer());
            List<ClusterApp> reqClusterApps = new ArrayList<>();
            for (ConfClusterApp app : clusterApps) {
                ClusterApp clusterAppItem = new ClusterApp();
                clusterAppItem.setAppName(app.getAppName());
                clusterAppItem.setAppVersion(app.getAppVersion());
                reqClusterApps.add(clusterAppItem);
            }
            instanceGroupVersion.setClusterApps(reqClusterApps);
            adminSaveClusterRequest.setInstanceGroupVersion(instanceGroupVersion);
            if (CollUtil.isEmpty(instanceGroupSkuCfg.getGroupCfgs())) {
                instanceGroupSkuCfg.setGroupCfgs(adminSaveClusterRequest.getClusterCfgs());
            }
            instanceGroupSkuCfg.setGroupName(instanceGroupSkuCfg.getGroupName().toLowerCase());
            List<ConfClusterVm> core = confClusterVmNeoMapper.selectByClusterIdAndVmRole(confCluster.getClusterId(), "core");
            instanceGroupSkuCfg.setOsVolumeSize(core.get(0).getOsVolumeSize());
            instanceGroupSkuCfg.setOsVolumeType(core.get(0).getOsVolumeType());
            addConfClusterHostGroupAppsConfig(clusterId, adminSaveClusterRequest);
            String groupId = instanceGroupSkuCfg.getGroupId();
            adminSaveClusterRequest.setGroupId(instanceGroupSkuCfg.getGroupId());
            adminSaveClusterRequest.setIsHa(confCluster.getIsHa());
            adminSaveClusterRequest.setInstanceGroupVersion(instanceGroupVersion);
            getLogger().info("新增实例组开始-添加主机配置信息,clusterId={},groupName={}", clusterId, instanceGroupSkuCfg.getGroupName());
            addConfClusterVmAndDataVolumeWhenAddGroup(adminSaveClusterRequest.getSrcClusterId(), adminSaveClusterRequest);

            //添加弹性规则
            addElasticScaleRules(adminSaveClusterRequest, instanceGroupSkuCfg);

            //创建Ambari配置组, 配置组后面统一创建. 此处不再创建.
//            getLogger().info("invoke composeService createConfigGroup,clusterId:{},groupName:{},groupId:{},vmRole:{}",
//                    clusterId,
//                    adminSaveClusterRequest.getGroupName(),
//                    groupId,
//                    instanceGroupSkuCfg.getVmRole());
//            composeService.createConfigGroup(clusterId, adminSaveClusterRequest.getGroupName(), groupId, instanceGroupSkuCfg.getVmRole());
            //扩容
            if (Objects.nonNull(instanceGroupSkuCfg.getCnt()) && instanceGroupSkuCfg.getCnt() > 0 && PurchaseType.Standard.equalValue(instanceGroupSkuCfg.getPurchaseType())) {
                ClusterScaleOutOrScaleInRequest clusterScaleOutOrScaleInRequest = new ClusterScaleOutOrScaleInRequest();
                clusterScaleOutOrScaleInRequest.setScaleOutCount(instanceGroupSkuCfg.getCnt());
                clusterScaleOutOrScaleInRequest.setExpectCount(instanceGroupSkuCfg.getCnt());
                clusterScaleOutOrScaleInRequest.setGroupName(instanceGroupSkuCfg.getGroupName());
                clusterScaleOutOrScaleInRequest.setClusterId(confCluster.getClusterId());
                clusterScaleOutOrScaleInRequest.setVmRole(adminSaveClusterRequest.getVmRole());
                clusterScaleOutOrScaleInRequest.setEnableBeforestartScript(instanceGroupSkuCfg.getEnableBeforestartScript());
                clusterScaleOutOrScaleInRequest.setEnableAfterstartScript(instanceGroupSkuCfg.getEnableAfterstartScript());
                clusterScaleOutOrScaleInRequest.setScaleOperationType(ConfScalingTask.Operation_type_create_group);
                ResultMsg clusterScaleOutResult = clusterScaleOut(clusterScaleOutOrScaleInRequest);
                if (!clusterScaleOutResult.isResult()) {
                    getLogger().error("invoke clusterScaleOut error,request:{},result:{}", clusterScaleOutOrScaleInRequest, clusterScaleOutResult);
                    ConfClusterHostGroup updateHostGroupState = new ConfClusterHostGroup();
                    updateHostGroupState.setGroupId(groupId);
                    updateHostGroupState.setState(ConfClusterHostGroup.STATE_DELETED);
                    confClusterHostGroupMapper.updateByPrimaryKeySelective(updateHostGroupState);
                }
            } else {
                ConfClusterHostGroup updateHostGroupState = new ConfClusterHostGroup();
                updateHostGroupState.setGroupId(groupId);
                updateHostGroupState.setState(ConfClusterHostGroup.STATE_RUNNING);
                //竞价实例数量更新为0，由竞价Spot任务产生
                if (Objects.equals(instanceGroupSkuCfg.getPurchaseType(), PurchaseType.Spot.getPurchaseType())) {
                    updateHostGroupState.setInsCount(0);
                    updateHostGroupState.setExpectCount(instanceGroupSkuCfg.getCnt());
                }
                confClusterHostGroupMapper.updateByPrimaryKeySelective(updateHostGroupState);
            }
            getLogger().info("addGroup success:{}", jsonStr);
            return ResultMsg.SUCCESS(instanceGroupSkuCfg);
        } catch (Exception ex) {
            getLogger().error("addGroup error:{}", jsonStr, ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }finally {
            redisLock.tryUnlock(lockKey);
        }
    }

    private void addElasticScaleRules(AdminSaveClusterRequest adminSaveClusterRequest, InstanceGroupSkuCfg instanceGroupSkuCfg) {
        ConfGroupElasticScalingData confGroupElasticScalingData = adminSaveClusterRequest.getConfGroupElasticScalingData();
        if (Objects.nonNull(confGroupElasticScalingData)) {
            //添加弹性规则配置
            ConfGroupElasticScaling elasticScaling = new ConfGroupElasticScaling();
            elasticScaling.setGroupEsId(UUID.randomUUID().toString());
            elasticScaling.setClusterId(confGroupElasticScalingData.getClusterId());
            elasticScaling.setGroupName(confGroupElasticScalingData.getGroupName());
            elasticScaling.setVmRole(StringUtils.lowerCase(confGroupElasticScalingData.getVmRole()));
            elasticScaling.setMaxCount(confGroupElasticScalingData.getMaxCount());
            elasticScaling.setMinCount(confGroupElasticScalingData.getMinCount());
            elasticScaling.setCreatedby("system");
            elasticScaling.setCreatedTime(new Date());
            if (!Objects.isNull(confGroupElasticScalingData.getIsFullCustody())) {
                elasticScaling.setIsFullCustody(confGroupElasticScalingData.getIsFullCustody());
            }
            if (!Objects.isNull(confGroupElasticScalingData.getIsGracefulScalein())) {
                elasticScaling.setIsGracefulScalein(confGroupElasticScalingData.getIsGracefulScalein());
            }
            if (!Objects.isNull(confGroupElasticScalingData.getScaleinWaitingTime())) {
                elasticScaling.setScaleinWaitingTime(confGroupElasticScalingData.getScaleinWaitingTime());
            }
            if (!Objects.isNull(confGroupElasticScalingData.getEnableAfterstartScript())) {
                elasticScaling.setEnableAfterstartScript(confGroupElasticScalingData.getEnableAfterstartScript());
            }
            if (!Objects.isNull(confGroupElasticScalingData.getEnableBeforestartScript())) {
                elasticScaling.setEnableBeforestartScript(confGroupElasticScalingData.getEnableBeforestartScript());
            }
            if (instanceGroupSkuCfg.getCnt() > 0) {
                elasticScaling.setIsValid(ConfGroupElasticScaling.ISVALID_NO);
            } else {
                elasticScaling.setIsValid(ConfGroupElasticScaling.ISVALID_YES);
            }
            confGroupElasticScalingMapper.insertSelective(elasticScaling);
            List<ConfGroupElasticScalingRuleData> scalingRules = adminSaveClusterRequest.getConfGroupElasticScalingData().getScalingRules();
            if (CollUtil.isNotEmpty(scalingRules)) {
                for (ConfGroupElasticScalingRuleData confGroupElasticScalingRuleData : scalingRules) {
                    ConfGroupElasticScalingRule confGroupElasticScalingRule = new ConfGroupElasticScalingRule();
                    try {
                        BeanUtils.copyProperties(confGroupElasticScalingRule, confGroupElasticScalingRuleData);
                    } catch (Exception e) {
                        getLogger().error("AdminApiServiceImpl.postElasticScalingRule copyProperties error. confGroupElasticScalingRuleData: {}, e: {}", JSON.toJSONString(confGroupElasticScalingRuleData), e);
                    }
                    confGroupElasticScalingRule.setEsRuleId(UUID.randomUUID().toString());
                    confGroupElasticScalingRule.setGroupEsId(elasticScaling.getGroupEsId());
                    confGroupElasticScalingRule.setClusterId(confGroupElasticScalingData.getClusterId());
                    confGroupElasticScalingRule.setGroupName(confGroupElasticScalingData.getGroupName());
                    if (instanceGroupSkuCfg.getCnt() > 0) {
                        confGroupElasticScalingRule.setIsValid(ConfGroupElasticScalingRule.ISVALID_NO);
                    } else {
                        confGroupElasticScalingRule.setIsValid(ConfGroupElasticScalingRule.ISVALID_YES);
                    }
                    confGroupElasticScalingRule.setCreatedby("sdpadmin");
                    confGroupElasticScalingRule.setCreatedTime(new Date());
                    confGroupElasticScalingRuleMapper.insertSelective(confGroupElasticScalingRule);
                }
            }
        }
    }

    @Override
    public ResultMsg growPart(String jsonStr) {
        getLogger().info("磁盘扩容任务创建开始,{}", jsonStr);
        try {
            AdminSaveClusterRequest clusterRequest = JSONUtil.toBean(jsonStr, AdminSaveClusterRequest.class);
            List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = clusterRequest.getInstanceGroupSkuCfgs();
            if (CollUtil.isEmpty(instanceGroupSkuCfgs)) {
                throw new RuntimeException("扩容配置信息为空");
            }
            InstanceGroupSkuCfg instanceGroupSkuCfg = instanceGroupSkuCfgs.get(0);
            if (Objects.isNull(instanceGroupSkuCfg.getDataVolumeSize())) {
                throw new RuntimeException("扩容磁盘容量为空");
            }
            ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(instanceGroupSkuCfg.getClusterId(), null, instanceGroupSkuCfg.getGroupName());
            List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(confClusterVm.getVmConfId());
            if (CollUtil.isEmpty(confClusterVmDataVolumes)) {
                SdpExceptionUtil.wrapRuntimeAndThrow("磁盘扩容任务创建异常，data volume信息不存在,clusterId={}", confClusterVm.getClusterId());
            }
            ConfClusterVmDataVolume dataVolume = confClusterVmDataVolumes.get(0);
            ConfScalingTask confScalingTask = new ConfScalingTask();
            confScalingTask.setTaskId(UUID.randomUUID().toString());
            confScalingTask.setClusterId(instanceGroupSkuCfg.getClusterId());
            confScalingTask.setScalingType(ConfScalingTask.ScaleType_Part_OUT);
            confScalingTask.setBeforeScalingCount(dataVolume.getDataVolumeSize());
            confScalingTask.setAfterScalingCount(instanceGroupSkuCfg.getDataVolumeSize());
            confScalingTask.setScalingCount(instanceGroupSkuCfg.getDataVolumeSize());
            confScalingTask.setExpectCount(instanceGroupSkuCfg.getDataVolumeSize());
            confScalingTask.setOperatiionType(ConfScalingTask.Operation_type_UserManual);
            confScalingTask.setState(ConfScalingTask.SCALINGTASK_Create);
            confScalingTask.setBegTime(new Date());
            confScalingTask.setVmRole(instanceGroupSkuCfg.getVmRole().toLowerCase());
            confScalingTask.setGroupName(instanceGroupSkuCfg.getGroupName());
            confScalingTask.setCreateTime(new Date());

            ResultMsg scaleInTask = composeService.createScalePartOutTask(confScalingTask);
            getLogger().info("磁盘扩容任务创建完成,result:{}", scaleInTask);
            return scaleInTask;
        } catch (Exception ex) {
            getLogger().error("磁盘扩容任务创建异常", ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    @Override
    public ResultMsg sdpVersionInfo() {
        List<BaseReleaseVmImg> vmImgs = baseReleaseVmImgMapper.selectAllStackVersion();

        ResultMsg result = new ResultMsg();
        result.setResultSucces("");
        List<Map> allRelease = new ArrayList<>();
        for (BaseReleaseVmImg vmImg : vmImgs) {
            Map data = new HashMap();
            data.put("sdpVersion", vmImg.getReleaseVersion());
            data.put("imageVersion", vmImg.getOsImageid());
            allRelease.add(data);
        }

        result.setData(allRelease);
        return result;
    }

    @Override
    public ResultMsg getTaskInfo(String taskId) {
        try {
            getLogger().info("获取任务信息开始:{}", taskId);
            Map<String, Object> result = new HashMap<>();
            Assert.notEmpty(taskId, "任务Id为空");
            ConfScalingTask scalingTask = confScalingTaskNeoMapper.selectByPrimaryKey(taskId);
            result.put("task", scalingTask);

            List<InfoClusterVm> infoClusterVms = null;
            if (Objects.equals(scalingTask.getScalingType(), ConfScalingTask.ScaleType_Part_OUT)) {
                List<ConfScalingVm> confScalingVms = confScalingVmMapper.selectByTaskId(scalingTask.getClusterId(), taskId);
                infoClusterVms = new ArrayList<>();
                for (ConfScalingVm confScalingVm : confScalingVms) {
                    InfoClusterVm infoClusterVm = new InfoClusterVm();
                    infoClusterVm.setClusterId(confScalingVm.getClusterId());
                    infoClusterVm.setPurchaseType(confScalingVm.getPurchaseType());
                    infoClusterVm.setGroupId(confScalingVm.getGroupId());
                    infoClusterVm.setGroupName(confScalingVm.getGroupName());
                    infoClusterVm.setHostName(confScalingVm.getHostName());
                    infoClusterVm.setVmRole(confScalingVm.getVmRole());
                    infoClusterVm.setVmName(confScalingVm.getVmName());
                    infoClusterVm.setVmConfId(confScalingVm.getVmConfId());
                    infoClusterVm.setInternalip(confScalingVm.getInternalIp());
                    infoClusterVm.setSkuName(confScalingVm.getSkuName());
                    infoClusterVms.add(infoClusterVm);
                }
            } else {
                infoClusterVms = infoClusterVmMapper.selectVmsByTaskId(scalingTask.getClusterId(), taskId);
            }

            for (InfoClusterVm infoClusterVm : infoClusterVms) {
                List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(infoClusterVm.getVmConfId());
                if (CollUtil.isEmpty(confClusterVmDataVolumes)) {
                    getLogger().error("not found data volume info,clusterId={},vmRole={},groupName={}", scalingTask.getClusterId(), scalingTask.getVmRole(), scalingTask.getGroupName());
                    continue;
                }
                ConfClusterVmDataVolume dataVolume = confClusterVmDataVolumes.get(0);
                infoClusterVm.setDataVolumeSize(dataVolume.getDataVolumeSize());
                infoClusterVm.setDataVolumeCount(dataVolume.getCount());

                ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(scalingTask.getClusterId(), scalingTask.getVmRole(), scalingTask.getGroupName());
                infoClusterVm.setOsVolumeSize(confClusterVm.getOsVolumeSize());
                infoClusterVm.setOsVolumeCount(1);
            }
            result.put("vms", infoClusterVms);
            getLogger().info("获取任务信息完成:{}", taskId);
            return ResultMsg.SUCCESS(result);
        } catch (Exception ex) {
            getLogger().error("获取任务信息异常,ex-message={}", ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    @Override
    public ResultMsg getVmListByPlanId(String planId) {
        // 根据planId, 找到plan
        InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);
        if (Objects.isNull(plan)) {
            L l = L.b("未找到对应的任务信息").p("planId", planId);
            return ResultMsg.SUCCESS(l.toString());
        }

        String opTaskId = "";
        // operation_type = 'clearvms'
        if (StrUtil.equalsIgnoreCase(plan.getOperationType(), "clearvms")) {
            opTaskId = plan.getOpTaskId();
            plan = planMapper.selectByPrimaryKey(opTaskId);
        }
        if (Objects.isNull(plan)) {
            L l = L.b("未找到对应的任务信息").p("planId", planId).p("opTaskId", opTaskId);
            return ResultMsg.SUCCESS(l.toString());
        }

        String scalingTaskId = plan.getScalingTaskId();
        if (StrUtil.isEmpty(scalingTaskId)) {
            L l = L.b("未找到任务对应的扩缩容任务信息").p("planId", planId).p("scalingTaskId", scalingTaskId)
                    .p("planName", plan.getPlanName());
            return ResultMsg.SUCCESS(l.s());
        }
        return getTaskInfo(scalingTaskId);
    }

    @Override
    public ResultMsg deleteScaleOutTaskVms(String taskId) {
        try {
            getLogger().info("删除任务实例开始:{}", taskId);
            ConfScalingTask scalingTask = confScalingTaskNeoMapper.selectByPrimaryKey(taskId);
            if (scalingTask.getState().equals(ConfScalingTask.SCALINGTASK_Failed)
                    || scalingTask.getScalingType().equals(ConfScalingTask.ScaleType_OUT)) {
                List<InfoClusterVm> infoClusterVms = infoClusterVmMapper.selectVmsByTaskId(scalingTask.getClusterId(), taskId);
                if (CollUtil.isEmpty(infoClusterVms)) {
                    throw new RuntimeException("无实例信息，不需要删除");
                }
                getLogger().info("创建删除任务计划:{}", taskId);
                ConfClusterHostGroup confClusterHostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(scalingTask.getClusterId(), scalingTask.getGroupName());
                if (confClusterHostGroup == null) {
                    getLogger().info("not found confClusterHostGroup,task:{}", scalingTask);
                    return ResultMsg.FAILURE("配置组信息未找到");
                }

                List<String> vms = new ArrayList<>();
                infoClusterVms.forEach(infoClusterVm -> {
                    vms.add(infoClusterVm.getVmName());
                });

                ScaleInForDeleteTaskVmReq scaleInForDeleteTaskVmReq = new ScaleInForDeleteTaskVmReq(
                        scalingTask.getClusterId(),
                        confClusterHostGroup.getGroupId(),
                        scalingTask.getTaskId(),
                        vms,
                        new Date()
                );
                ResultMsg resultMsg = composeService.createScaleInForDeleteTaskVm(scaleInForDeleteTaskVmReq);
                if (resultMsg.isSuccess()) {
                    getLogger().info("删除任务实例完成:{}", taskId);
                    return ResultMsg.SUCCESS("正在删除实例...");
                } else {
                    return resultMsg;
                }
            } else {
                return ResultMsg.FAILURE("无法删除此实例组的实例，只能删除扩容失败的任务实例");
            }
        } catch (Exception ex) {
            getLogger().error("删除任务实例异常,ex-message={}", ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    @Deprecated
    private void addConfClusterHostGroupAppsConfig(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
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

    @Override
    public ResultMsg getScaleCountInQueue(ConfScalingTask scalingTask) {
        try {
            Assert.notEmpty(scalingTask.getClusterId(), "集群clusterId不能为空");
            Assert.notEmpty(scalingTask.getGroupName(), "实例组名groupName不能为空");
            Assert.notEmpty(scalingTask.getVmRole(), "实例类型vmRole不能为空");
            Assert.notNull(scalingTask.getScalingType(), "扩缩容类型scalingType不能为空");
            ConfClusterHostGroup hostGroup = confClusterHostGroupMapper.selectByClusterIdAndGroupName(scalingTask.getClusterId(), scalingTask.getGroupName());
            Map<String, Integer> res = new HashMap<>();
            if (Objects.equals(hostGroup.getPurchaseType(), ConfClusterVm.PURCHASETYPE_SPOT)) {
                res.put("insCount", hostGroup.getInsCount());
                if (hostGroup.getExpectCount() == null) {
                    res.put("expectCount", hostGroup.getInsCount());
                } else {
                    res.put("expectCount", hostGroup.getExpectCount());
                }
            } else {
                int runningCount = infoClusterVmMapper.countByClusterIdAndGroupIdAndState(hostGroup.getClusterId(), hostGroup.getGroupId(), InfoClusterVm.VM_RUNNING);
                res.put("insCount", runningCount);
                res.put("expectCount", runningCount);
            }
            return ResultMsg.SUCCESS(res);
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            return ResultMsg.FAILURE(ex.getMessage());
        }
    }

    /**
     * 竞价实例历史价格
     */
    public ResultMsg spotPriceHistory(Map<String, Object> param) {
        Gson gson = new Gson();
        List<String> skuNames = (List<String>)param.get("skuNames");
        ResultMsg resultMsg = azureService.spotPriceHistory(skuNames,param.get("region").toString());
        if (!resultMsg.getResult() || resultMsg.getData() == null) {
            return resultMsg;
        }

        try {
            List<SpotPriceHistory> spotPriceHistoryList;
            SpotPriceHistory[] spotPriceHistoryArr = gson.fromJson(gson.toJson(resultMsg.getData()), SpotPriceHistory[].class);
            spotPriceHistoryList = Arrays.asList(spotPriceHistoryArr);

            if (!CollectionUtils.isEmpty(spotPriceHistoryList)) {
                for (SpotPriceHistory spotPriceHistory : spotPriceHistoryList) {
                    List<PriceItem> priceItems = spotPriceHistory.getPriceItems();
                    priceItems.sort(new Comparator<PriceItem>() {
                        @Override
                        public int compare(PriceItem o1, PriceItem o2) {
                            return Double.valueOf(o1.getEffectiveDate().getTime() - o2.getEffectiveDate().getTime()).intValue();
                        }
                    });
                }
            }
            resultMsg.setResult(true);
            resultMsg.setData(spotPriceHistoryList);
        } catch (Exception e) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("数据解析错误");
            getLogger().error("AdminApiServiceImpl.spotPriceHistory json parse error. resultData: {}, e: {}", gson.toJson(resultMsg.getData()), e);
        }
        return resultMsg;
    }

    @Override
    public List<AzurePriceHistory> spotPriceAndEvictionRateHistory(String region, String skuName,
                                                                   Integer periodDays) {
        Assert.notEmpty(region, "查询Spot历史价格和逐出率时，Region不能为空");
        Assert.notEmpty(skuName, "查询Spot历史价格和逐出率时，skuName不能为空");
        if (Objects.isNull(periodDays)) {
            periodDays = 7;
        }

        Date endDate = new Date();
        Date startDate = cn.hutool.core.date.DateUtil.offsetDay(endDate, (periodDays-1)*-1);
        return azurePriceHistoryMapper.selectDayLastByRegionAndDateRange(region, skuName, startDate, endDate);
    }

    /**
     * 获取执行计划信息
     *
     * @param planId
     * @return
     */
    @Override
    public ResultMsg getPlanInfoByPlanId(String planId) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoClusterOperationPlan plan = planMapper.selectByPrimaryKey(planId);
            msg.setResult(true);
            msg.setData(plan);
        } catch (Exception e) {
            getLogger().error("get plan exception:", e);
            msg.setResult(false);
        }
        return msg;

    }

    /**
     * 获取扩缩容任务信息
     *
     * @param taskId
     * @return
     */
    @Override
    public ResultMsg getScaleTaskInfoByTaskId(String taskId) {
        ResultMsg msg = new ResultMsg();
        try {
            ConfScalingTask task = confScalingTaskNeoMapper.selectByPrimaryKey(taskId);
            msg.setResult(true);
            msg.setData(task);
        } catch (Exception e) {
            getLogger().error("get task exception:", e);
            msg.setResult(false);
        }
        return msg;
    }

    @Override
    public ResultMsg getJobQueryParamDict() {
        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        GetJobQueryParamDictOutput output = OperationPlanUtils.getJobQueryParamDict();
        msg.setData(output);
        return msg;
    }

    /**
     * 处理历史没有任务名称的计划
     *
     * @return
     */
    @Override
    public ResultMsg processWithOutPlanName() {
        ResultMsg msg = new ResultMsg();
        List<InfoClusterOperationPlan> planList = planMapper.selectAllWithOutPlanName();
        planList.parallelStream().forEach(x -> {
            composeService.updatePlanName(x.getPlanId());
            composeService.updatePlanStateAndPercent(x.getPlanId());
        });
        return msg;
    }

    /**
     * 处理存量VM的VMID
     *
     * @return
     */
    @Override
    public ResultMsg processWithOutVmId( String region) {
        ResultMsg msg = new ResultMsg();
        try {
            List<Map<String,String>> vmNodes = new ArrayList<>();

            //region 1.获取 Azure 所有VM数据
            ResultMsg resultMsg = this.azureService.getVmList(region);
            Object data = resultMsg.getData();
            if (data == null) {
                getLogger().error("查询vmlist失败，未获取到vmlist。");
                msg.setErrorMsg("查询vmlist失败，未获取到vmlist。");
                return msg;
            }

            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object vmGroupListData = dataMap.get("data");
            if (vmGroupListData == null) {
                msg.setErrorMsg("查询vmlist失败，未获取到vmlist,data为空");
                return msg;
            }

            List<Map<String, Object>> vmGroupList = (List<Map<String, Object>>) vmGroupListData;
            for (Map<String, Object> vmGroup : vmGroupList) {
                Object virtualMachines = vmGroup.get("virtualMachines");
                if (virtualMachines == null) {
                    continue;
                }

                List<Map<String, Object>> vmList = (List<Map<String, Object>>) virtualMachines;
                for (Map<String, Object> vmNode : vmList) {
                    Map<String,String> node = new HashMap<>();
                    node.put("vmName",vmNode.get("name").toString());
                    node.put("vmid",vmNode.get("uniqueId").toString());
                    vmNodes.add(node);
                }
            }
            //endregion

            //region  2.获取 running 且 vmid is null
            List<InfoClusterVm> runningWithoutvmIdVms = infoClusterVmMapper.selectRunningVMsWithOutVmId();
            //endregion

            //region 3.取Azure中包含的记录
            List<Map<String,String>> updateItems = new ArrayList<>();
            vmNodes.stream().forEach(node->{
                runningWithoutvmIdVms.stream().forEach(vm->{
                    if (node.get("vmName").toLowerCase().equals(vm.getVmName())){
                        Map<String,String> updateItem = new HashMap<>();
                        updateItem.put("clusterId",vm.getClusterId());
                        updateItem.put("vmName",vm.getVmName());
                        updateItem.put("vmid",node.get("vmid"));

                        updateItems.add(updateItem);
                    }
                });
            });
            getLogger().info("需要更新的数量："+updateItems.size());
            //endregion

            //region 4.更新info_cluster_vm
            updateItems.forEach(item->{
                infoClusterVmMapper.updateVMId(item);
            });
            //endregion
            msg.setResult(true);
        }catch (Exception e){
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            msg.setResult(false);
        }

        return msg;
    }

    @Override
    public ResultMsg getAvailableImage(String clusterId) {
        try {
            // 获取当前集群的镜像
            List<ConfClusterVm> vmList = confClusterVmNeoMapper.selectByClusterIdAndVmRole(clusterId, "ambari");
            if (CollectionUtil.isEmpty(vmList)) {
                vmList = confClusterVmNeoMapper.selectByClusterIdAndVmRole(clusterId, "master");
            }
            Assert.notEmpty(vmList,"获取不到当前集群的镜像");
            // 根据镜像id获取当前版本下的镜像信息(镜像版本需按Stack区分)
            List<BaseReleaseVmImg> baseReleaseVmImgs = baseReleaseVmImgMapper.selectOneByImgId(vmList.get(0).getImgId());
            List<ClusterAvalibaleImageDto> imageDtoList = baseReleaseVmImgs.stream().map(baseReleaseVmImg -> {
                ClusterAvalibaleImageDto dto = new ClusterAvalibaleImageDto();
                dto.setImgId(baseReleaseVmImg.getImgId());
                dto.setOsImageId(baseReleaseVmImg.getOsImageid());
                return dto;
            }).collect(Collectors.toList());
            ResultMsg msg = ResultMsg.SUCCESS("查询镜像数据成功");
            msg.setData(imageDtoList);
            return msg;
        } catch (Exception ex) {
            getLogger().error("查询镜像数据失败", ex);
            return ResultMsg.FAILURE("查询镜像数据失败");
        }
    }

    /**
     * 查询失败日志By条件
     *
     * @param param
     * @return
     */
    @Override
    public ResultMsg getFailedLogsByParam(Map param) {
        ResultMsg msg = new ResultMsg();
        try {
            List<InfoThirdApiFailedLog> logs =
                    thirdApiFailedLogMapper.getListByParam(param);
            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            logs.forEach(log-> log.setRegionName(regionMap.get(log.getRegion())));
            Integer logsCount = thirdApiFailedLogMapper.getCountByParam(param);
            msg.setData(logs);
            msg.setTotal(logsCount);
            msg.setResult(true);
        }catch (Exception e){
            getLogger().error("查询失败日志By条件,异常",e);
            msg.setErrorMsg("查询失败日志By条件,异常");
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 查询失败日志详情
     *
     * @param Id
     * @return
     */
    @Override
    public ResultMsg getFailedLogById(Long Id) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoThirdApiFailedLogWithBLOBs log =
                    thirdApiFailedLogMapper.selectByPrimaryKey(Id);
            msg.setData(log);
            msg.setResult(true);
        }catch (Exception e){
            getLogger().error("查询失败日志By条件,异常",e);
            msg.setErrorMsg("查询失败日志异常");
            msg.setResult(false);
        }
        return msg;
    }

    /**
     * 运维功能-删除锁
     *
     * @param keyName
     * @return
     */
    @Override
    public ResultMsg opsDelLockKey(String keyName) {
        ResultMsg msg= new ResultMsg();
        try {
            redisLock.delete(keyName);
            msg.setResult(true);
        }catch (Exception e){
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * 运维功能-补发第一条消息
     *
     * @param planId
     * @return
     */
    @Override
    public ResultMsg opsSendFirstMessage(String planId) {
        return composeService.startPlan(planId);
    }

    @Override
    public ResultMsg getStackVersions() {
        Map<String, String> result = new HashMap<>();
        List<Map<String, String>> versions = osImageMapper.queryStackVersionImage();
        return ResultMsg.SUCCESS(versions);
    }

    @Override
    public int updateClusterParallel(String clusterId, Integer isParallelScale) {
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId(clusterId);
        confCluster.setIsParallelScale(isParallelScale);
        return confClusterMapper.updateByPrimaryKeySelective(confCluster);
    }

    @Override
    public int updateDestroyStatus(String clusterId, Integer isWhiteAddr) {
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId(clusterId);
        confCluster.setIsWhiteAddr(isWhiteAddr);
        return confClusterMapper.updateByPrimaryKeySelective(confCluster);
    }

    /**
     * 校验竞价相关信息
     * 1,价格
     * 2,sku数量
     * @param region
     * @param instanceGroupSkuCfg
     */
    private void checkSpot(String region, InstanceGroupSkuCfg instanceGroupSkuCfg) {
        if (PurchaseType.Spot.equalValue(instanceGroupSkuCfg.getPurchaseType())) {
            if (Objects.isNull(instanceGroupSkuCfg.getMaxPrice())) {
                SdpExceptionUtil.wrapRuntimeAndThrow("直接出价时请输入竞价价格");
            }
            List<String> skuNameList = instanceGroupSkuCfg.getSkuNames();
            List<VMSku> vmSkus = metaDataItemService.listVmSkuDistinct(region, skuNameList);
            if (vmSkus==null || vmSkus.size() < 1 || vmSkus.size() > 15) {
                SdpExceptionUtil.wrapRuntimeAndThrow("多机型资源池sku数量小于1个或大于15个。");
            }
            AzurePriceHistory instancePrice = azurePriceService.getMaxOndemondPrice(region, skuNameList);
            if (instancePrice == null) {
                SdpExceptionUtil.wrapRuntimeAndThrow("获取价格失败");
            }
            BigDecimal spotPrice = azurePriceService.computeSpotPrice(
                    instanceGroupSkuCfg.getPriceStrategy(),
                    instancePrice.getOndemandUnitPrice(),
                    instanceGroupSkuCfg.getMaxPrice());
            if (CompareUtil.compare(spotPrice, instancePrice.getSpotUnitPrice()) < 0) {
                SdpExceptionUtil.wrapRuntimeAndThrow("购买价格不能低于规格价格,sku={},市场价={}",
                        instanceGroupSkuCfg.getSkuNames(),
                        instancePrice.getSpotUnitPrice());
            }
        }
    }

    /**
     * 更新PV2数据盘IOPS和MBPS
     *
     * @param request
     * @return
     */
    @Override
    public ResultMsg updateDiskIOPSAndThroughput(DiskPerformanceRequest request) {
        ResultMsg resultMsg = new ResultMsg<>();
        String clusterId = request.getClusterId();
        String vmConfId = request.getVmConfId();
        Integer newDataDiskIOPSReadWrite = request.getNewDataDiskIOPSReadWrite();
        Integer newDataDiskMBpsReadWrite = request.getNewDataDiskMBpsReadWrite();

        String lockKey = "pv2DiskPerformanceAdjust:" + clusterId;
        boolean lock = redisLock.tryLock(lockKey);
        if (!lock) {
            resultMsg.setErrorMsg("请勿重复提交请求");
            resultMsg.setResult(false);
            return resultMsg;
        }
        try {
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                return ResultMsg.FAILURE("集群不存在!");
            }
            if (!confCluster.getState().equals(ConfCluster.CREATED)) {
                return ResultMsg.FAILURE("集群状态不正常!");
            }
            ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByPrimaryKey(vmConfId);
            if (confClusterVm == null) {
                return ResultMsg.FAILURE("集群实例配置信息不存在!");
            }
            List<ConfClusterVmDataVolume> confClusterVmDataVolumes = confClusterVmDataVolumeMapper.selectByVmConfId(vmConfId);
            if (CollUtil.isEmpty(confClusterVmDataVolumes)) {
                return ResultMsg.FAILURE("集群实例数据盘配置信息不存在!");
            }
            ConfClusterVmDataVolume dataVolume = confClusterVmDataVolumes.get(0);
            //检查数据盘类型和数据盘最大IOPS和最大吞吐量
            resultMsg = this.checkDiskIOPSAndThroughput(dataVolume, newDataDiskIOPSReadWrite, newDataDiskMBpsReadWrite);
            if (!resultMsg.isSuccess()) {
                return resultMsg;
            }
            // 组装查询参数,磁盘创建之后的前24小时只能调整3次
            Date createdTime = confClusterVm.getCreatedTime();
            if (createdTime != null && new Date().getTime() - createdTime.getTime() < 24 * 60 * 60 * 1000){
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("clusterId", request.getClusterId());
                paramMap.put("vmRole", confClusterVm.getVmRole());
                paramMap.put("groupName", confClusterVm.getGroupName());
                paramMap.put("stateList",Arrays.asList(
                        ConfScalingTask.SCALINGTASK_Create,
                        ConfScalingTask.SCALINGTASK_Running,
                        ConfScalingTask.SCALINGTASK_Complete));
                paramMap.put("scalingType", ConfScalingTask.scaleType_diskThroughput);
                paramMap.put("scaleOutTaskIdIsNull", true);
                int total = confScalingTaskNeoMapper.selectCountByParams(paramMap);
                if (total>=3){
                    return ResultMsg.FAILURE("磁盘创建之后的前24小时只能调整3次，现已超过3次！");
                }
            }
            confScalingTaskService.savePv2DiskScalingTask(clusterId,
                    confClusterVm.getGroupName(),
                    confClusterVm.getVmRole(),
                    newDataDiskIOPSReadWrite,
                    newDataDiskMBpsReadWrite,
                    null);
            return ResultMsg.SUCCESS();
        }catch (Exception e) {
            getLogger().error("PV2数据盘性能调整:{}", e.getMessage(), e);
            return ResultMsg.FAILURE(e.getMessage());
        } finally {
            redisLock.tryUnlock(lockKey);
        }
    }

    /**
     * 检查数据盘类型和数据盘最大IOPS和最大MBPS
     *
     * @param confClusterVmDataVolume
     * @param newDataDiskIOPSReadWrite
     * @param newDataDiskMBpsReadWrite
     * @return
     */
    private ResultMsg checkDiskIOPSAndThroughput(ConfClusterVmDataVolume confClusterVmDataVolume,
                                              Integer newDataDiskIOPSReadWrite,
                                              Integer newDataDiskMBpsReadWrite) {
        ResultMsg resultMsg = new ResultMsg<>();
        resultMsg.setResult(true);
        if (!DataVolumeType.PremiumV2_LRS.name().equals(confClusterVmDataVolume.getDataVolumeType())) {
            resultMsg = ResultMsg.FAILURE("数据盘类型不允许调整,请检查数据盘类型(PremiumV2_LRS)!");
        }
        Integer dataVolumeSize = confClusterVmDataVolume.getDataVolumeSize() * confClusterVmDataVolume.getCount();
        if (newDataDiskIOPSReadWrite<3000){
            resultMsg = ResultMsg.FAILURE("所允许的最小IOPS:3000");
        }
        if (newDataDiskMBpsReadWrite<125){
            resultMsg = ResultMsg.FAILURE("所允许的最小吞吐量:125");
        }

        Integer maxDataDiskIOPS = this.getMaxDataDiskIOPS(dataVolumeSize);
        if (maxDataDiskIOPS.compareTo(newDataDiskIOPSReadWrite) < 0) {
            resultMsg = ResultMsg.FAILURE("超过所允许的最大IOPS,max:" + maxDataDiskIOPS);
        }
        Integer maxDataDiskMBps = this.getMaxDataDiskMBps(maxDataDiskIOPS);
        if (maxDataDiskMBps.compareTo(newDataDiskMBpsReadWrite) < 0) {
            resultMsg = ResultMsg.FAILURE("超过所允许的最大MBPS,max:" + maxDataDiskMBps);
        }
        return resultMsg;
    }

    /**
     *  IOPS默认是3000，当磁盘超过6GB之后，每提升1GB，可以提高500 IOPS，80000封顶
     *
     * @param dataVolumeSize
     * @return
     */
    private Integer getMaxDataDiskIOPS(Integer dataVolumeSize) {
        if (dataVolumeSize <= 6) {
            return 3000;
        }
        if (dataVolumeSize <= 160){
            return 3000 + (dataVolumeSize - 6) * 500;
        }
        return 80000;
    }

    /**
     * MBPS默认是125MB/s，当磁盘超过6GB之后，MBPS最高可设置为IOPS*0.25MB/s，1250MB/s封顶
     *
     * @param dataDiskIOPS
     * @return
     */
    private Integer getMaxDataDiskMBps(Integer dataDiskIOPS) {
        if (dataDiskIOPS>=4800){
            return 1200;
        }
        return dataDiskIOPS * 25 / 100;
    }
}