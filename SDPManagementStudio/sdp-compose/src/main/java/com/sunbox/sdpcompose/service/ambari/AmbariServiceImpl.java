package com.sunbox.sdpcompose.service.ambari;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.dao.mapper.InfoAmbariConfigGroupMapper;
import com.sunbox.dao.mapper.InfoClusterFinalBlueprintMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.domain.enums.DynamicType;
import com.sunbox.sdpcompose.enums.AmbariHostState;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.service.IAmbariService;
import com.sunbox.sdpcompose.service.IClusterService;
import com.sunbox.sdpcompose.service.ambari.blueprint.*;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplateHostGroup;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.CreateClusterTemplateCmd;
import com.sunbox.sdpcompose.service.ambari.configcleaner.AMLabelDisabledConfigCleaner;
import com.sunbox.sdpcompose.service.ambari.configcleaner.ConfigCleaner;
import com.sunbox.sdpcompose.service.ambari.configgeneerator.*;
import com.sunbox.sdpcompose.service.ambari.enums.*;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.util.L;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.util.Asserts;
import org.bouncycastle.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.ApiException;
import sunbox.sdp.ambari.client.api.BlueprintsApi;
import sunbox.sdp.ambari.client.api.ClusterServicesApi;
import sunbox.sdp.ambari.client.api.ClustersApi;
import sunbox.sdp.ambari.client.api.CustomActionApi;
import sunbox.sdp.ambari.client.model.BlueprintSwagger;
import sunbox.sdp.ambari.client.model.ClusterRequestSwagger;
import sunbox.sdp.ambari.client.model.ClusterServiceStateRequest;
import sunbox.sdp.ambari.client.model.ServiceOp;
import sunbox.sdp.ambari.client.model.createclusterprocess.*;
import sunbox.sdp.ambari.client.model.customaction.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ambari服务，通过此服务完成与Ambari之间的交互
 *
 * @author: wangda
 * @date: 2022/12/5
 */
@Service
public class AmbariServiceImpl implements IAmbariService {

    private static Logger logger = LoggerFactory.getLogger(AmbariServiceImpl.class);

    /**
     * 是否开启Ambari Api调用调试模式, 1:开启  0:不开启 <br/> 默认开启调试模式,便于查问题
     */
    @Value("${ambari.api.debug}")
    private String debug = "1";

    @Value("${yarn.api.timeout:30}")
    private Integer yarnApiTimeOut;

    /**
     * Ambari-Server服务是否设置自动启动
     */
    @Value("${ambari.settings.autostart:0}")
    private String autostart = "0";

    @Value("${ambari.querycomponentinhosts.retrytimes:3}")
    private String queryCompontRetrytimes;

    @Value("${hadoop.jmx.api.port:8088}")
    private int hadoopJmxApiPort;

    @Value("${hadoop.jmx.api.port:50070}")
    private int hadoopNameNodeJmxApiPort;

    @Value("${sdp.ambari.retry.count:5}")
    private Integer sdpAmbariRetryCount;

    @Value("${sdp.ambari.retry.duration:30}")
    private Long sdpAmbariRetryDuration;

    @Autowired
    InfoClusterVmMapper vmMapper;

    @Autowired
    ConfClusterMapper confClusterMapper;

    @Autowired
    AmbariConfigItemMapper itemMapper;

    @Autowired
    BaseSceneMapper sceneMapper;

    @Autowired
    BaseSceneAppsMapper sceneAppsMapper;

    @Autowired
    ConfClusterHostGroupAppsConfigMapper hostGroupConfigMapper;

    @Autowired
    ConfClusterAppsConfigMapper clusterConfigMapper;

    @Autowired
    InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    ConfClusterHostGroupMapper hostGroupMapper;

    @Autowired
    InfoAmbariConfigGroupMapper ambariConfigGroupMapper;

    @Autowired
    InfoClusterComponentLayoutMapper infoClusterComponentLayoutMapper;

    @Autowired
    AmbariComponentLayoutMapper ambariComponentLayoutMapper;

    @Autowired
    AmbariConfigItemMapper configItemMapper;

    @Autowired
    IClusterService clusterService;
    @Autowired
    private InfoClusterFinalBlueprintMapper infoClusterFinalBlueprintMapper;

    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;


    @Override
    public Blueprint createBlueprint(CreateBlueprintCmd cmd) {
        return createBlueprint(cmd, null);
    }

    @Override
    public Blueprint createBlueprint(CreateBlueprintCmd cmd, Blueprint originBlueprint) {
        // 校验参数
        cmd.validate();

        // 实例化Blueprint
        Blueprint blueprint = new Blueprint();

        // 设置HA的状态
        blueprint.setHa(cmd.isHa() ? ConfigItemType.HA : ConfigItemType.NON_HA);

        // 初始化Mapper
        blueprint.init();

        // 设置Stack  无论哪个版本的Stack, 传参数都是 SDP-1.0
//        blueprint.setStackInfo(cmd.getBlueprintName(), cmd.getStackName() + "-" + cmd.getStackVersion());
        blueprint.setStackInfo(cmd.getBlueprintName(), "SDP-1.0");

        // 设置默认配置
        blueprint.initDefaultConfig(cmd.getStackName() + "-" + cmd.getStackVersion(), cmd.getServices());

        // 如果存在覆盖默认配置，则进行覆盖
        overrideDefaultConfig(blueprint, originBlueprint);

        // 设置数据库配置
        blueprint.initDBConfig(cmd.getDbConfigs());

        // Ambari中设置自动重启。1：设置自动重启
        blueprint.initSettings(cmd.getServices());
//        if (Objects.equals("1", autostart)) {
//        }

        // 设置布局
        setDeployLayout(cmd, blueprint);

        // 设置实例组配置
        overrideHostGroupConfig(blueprint,originBlueprint);

        // 设置HBase多磁盘配置
        blueprint.setHbaseMultiDiskConfig(cmd.getFullName(), cmd.getScene(), cmd);

        // 设置ABFS配置
        blueprint.setMIABFSConfig(cmd.getFullName(), cmd.getMiTenantId(), cmd.getMiClientId());

//        logger.info("创建集群,判断mi是否配置:{}",blueprint.getConfigurations().stream().filter(x->x.containsKey("core-site")).findFirst().orElse(new HashMap<>()));
        // 处理Ganglia配置的服务器地址，将其替换为AmbariServer所在的主机名
        handleGangliaConfig(cmd, blueprint);

        // 处理HBase的配置
        handleTezConfig(cmd, blueprint);

        return blueprint;
    }

    /**
     * 处理HBase的配置
     *
     * @param cmd
     * @param blueprint
     */
    void handleTezConfig(CreateBlueprintCmd cmd, Blueprint blueprint) {
        if (Objects.isNull(cmd.getServices()) || !cmd.getServices().contains(BDService.TEZ.name())) {
            // 没选择Tez，yarn-site下不配置 org.apache.tez.dag.history.logging.ats.TimelineCachePluginImpl 参数
            BlueprintConfiguration yarnSiteConfig = blueprint.findConfigByConfigType(ConfigClassification.YARN_SITE.getClassification());
            yarnSiteConfig.getProperties().remove("yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes");
        }
    }

    /**
     * 生成实例组的资源配置
     *
     * @return
     */
    Map<String, List<BlueprintConfiguration>> generateResourceConfig(List<ClusterHostGroup> hostGroups) {
        if (CollectionUtil.isEmpty(hostGroups)) {
            logger.warn("创建集群时，动态计算资源参数出错：实例组为空");
            return null;
        }

        Map<String, List<BlueprintConfiguration>> result = new HashMap<>();
        // Task实例组的NodeManager资源动态计算生成
        EnumSet<HostGroupRole> ignoreSet = EnumSet.of(HostGroupRole.MASTER, HostGroupRole.AMBARI);
        for (ClusterHostGroup hostGroup : hostGroups) {
            if (ignoreSet.contains(hostGroup.getRole())) {
                continue;
            }

            CustomConfigGenerator generator = CustomConfigGeneratorFactory.tryCreate(hostGroup.getRole());
            if(generator == null){
                logger.warn("can not create CustomConfigGenerator instance for hostGroupRole:{}", hostGroup.getRole());
                continue;
            }

            List<HostInstance> hosts = hostGroup.getHosts();
            if (CollectionUtil.isEmpty(hosts)) {
                logger.error("创建集群时，实例组中没有配置主机，不能动态计算配置。实例组ID={}，实例组名称={}", hostGroup.getGroupId(), hostGroup.getGroupName());
                continue;
            }
            HostInstance instance = hosts.get(0);
            List<BlueprintConfiguration> configList = generator.generate(instance);

            result.put(hostGroup.getGroupName(), configList);
        }
        return result;
    }

    @Override
    public List<BlueprintConfiguration> generateCustomConfig(HostInstance hostInstance, HostGroupRole role) {
        Asserts.notNull(hostInstance, "生成自定义配置时，主机实例配置对象不对为空");
        Asserts.notNull(role, "生成自定义配置时，主机角色不能为空");

        CustomConfigGenerator generator = CustomConfigGeneratorFactory.tryCreate(role);
        if(generator == null){
            logger.warn("can not create CustomConfigGenerator instance for hostGroupRole:{}", role);
            return Lists.newArrayList();
        }

        return generator.generate(hostInstance);
    }

    private HostGroup getBlueprintHostGroupByGroupName(Blueprint blueprint, String groupName) {
        // 更新Blueprint中各个配置组的配置
        for (HostGroup group : blueprint.getHostGroups()) {
            if (Objects.equals(groupName, group.getName())) {
                return group;
            }
        }

        return null;
    }


    void handleGangliaConfig(CreateBlueprintCmd cmd, Blueprint blueprint) {
        if (cmd.getEnableGanglia() != 1) {
            BlueprintConfiguration gangliaConfig = blueprint.findConfigByConfigType(ConfigClassification.HADOOP_METRICS2.getClassification());
            if (Objects.nonNull(gangliaConfig)) {
                gangliaConfig.putProperties("content", "");
            }
//            blueprint.removeConfigurationByConfigType(ConfigClassification.HADOOP_METRICS2.getClassification());
            logger.info("未启用Ganglia, 删除Ganglia相关配置：{}", ConfigClassification.HADOOP_METRICS2.getClassification());
            return;
        }
        // 准备好Ambari-Server地址
        String gangliaConfigFile = "hadoop-metrics2.properties";
        String ambariServerName = null;
        if (cmd.isHa()) {
            // 找Ambari组
            ClusterHostGroup ambari = cmd.getClusterHostGroupByGroupName("ambari");
            ambariServerName = ambari.getHosts().get(0).getHostName();
        } else {
            // 找Master1组
            ClusterHostGroup ambari = cmd.getClusterHostGroupByGroupName("ambari");
            ambariServerName = ambari.getHosts().get(0).getHostName();
        }

        if (StrUtil.isEmpty(ambariServerName)) {
            logger.error("生成Ganglia配置时， 没有找到Ambari服务器地址");
            return;
        }

        // 找到Ganglia配置文件
        BlueprintConfiguration gangliaConfig = blueprint.findConfigByConfigType(gangliaConfigFile);
        if (Objects.isNull(gangliaConfig)) {
            logger.info("没找到Ganglia的配置文件：" + gangliaConfigFile);
            // 如果没找到Ganglia的配置，退出
            return;
        }

        // 找到需要替换的标识符
        String content = (String) gangliaConfig.getProperty("content");
        if (Objects.isNull(content)) {
            return;
        }
        String placeholder = "GangliaServer";
        String[] contentArray = content.split("\n");
        for (String s : contentArray) {
            if (s.indexOf("sink.ganglia.servers") >= 0) {
                // 是ganglia的配置，
                String[] gangliaSplit = s.split("=");
                if (gangliaSplit.length == 2 && StringUtils.isNotBlank(gangliaSplit[1])) {
                    placeholder = StringUtils.trim(gangliaSplit[1]);
                    break;
                }
            }
        }
        // 替换为Ambari-Server地址
        content = content.replaceAll(placeholder, ambariServerName);
        gangliaConfig.putProperties("content", content);
    }
    void overrideHostGroupConfig(Blueprint blueprint, Blueprint originBlueprint ) {
        if (originBlueprint == null || CollectionUtil.isEmpty(originBlueprint.getHostGroups())) {
            return;
        }
        logger.info("复制集群 源 hostgroups:{}",JacksonUtils.toJson(originBlueprint.getHostGroups()));
        logger.info("复制集群 目标 hostgroups:{}",JacksonUtils.toJson(blueprint.getHostGroups()));
        //能精确识别的
        HashMap<String, HostGroup> hostGroupHashMap = new HashMap<>();
        //未知的
        ArrayList<HostGroup> unknownTaskHostGroup = new ArrayList<>();
        int index = 0;
        for (HostGroup  originHostGroup : originBlueprint.getHostGroups()) {
//            logger.info("复制集群 源集群hostgroup:{}", JacksonUtils.toJson(originHostGroup));
            Set<String> components = originHostGroup.getComponents().stream().map(ComponentObj::getName).collect(Collectors.toSet());
            if (components.contains("AMBARI_SERVER")){
                hostGroupHashMap.put("AMBARI", originHostGroup);
                continue;
            }
            if (components.contains("RESOURCEMANAGER") && components.contains("APP_TIMELINE_SERVER")){
                hostGroupHashMap.put("MASTER1", originHostGroup);
                continue;
            }
            if (components.contains("RESOURCEMANAGER") && !components.contains("APP_TIMELINE_SERVER")){
                hostGroupHashMap.put("MASTER2", originHostGroup);
                continue;
            }
            if (components.contains("DATANODE")){
                hostGroupHashMap.put("CORE",originHostGroup);
                continue;
            }
            if (originHostGroup.getConfigurations() != null){
                boolean isUse = false;
                for (Map<String, Map<String,Object>> configuration : originHostGroup.getConfigurations()) {
                    if (configuration != null ){
                        Map<String,Object>  blueprintConfiguration = configuration.get(ConfigClassification.YARN_SITE.getClassification());
                        if (blueprintConfiguration != null ){
                            String property = (String) blueprintConfiguration.get("host.group");
                            if (property != null){
                                hostGroupHashMap.put(property,originHostGroup);
                                isUse = true;
                                break;
                            }
                        }
                    }
                }
                if (isUse){
                    continue;
                }
            }
            unknownTaskHostGroup.add(originHostGroup);
        }
        for (HostGroup hostGroup : blueprint.getHostGroups()) {
//            logger.info("复制集群 新集群hostgroup:{}", JacksonUtils.toJson(hostGroup));
            HostGroup originHostGroup = hostGroupHashMap.get(hostGroup.getName());
            //名称匹配
            if (originHostGroup != null && originHostGroup.getConfigurations() != null){

                overrideHostGroupConfig(hostGroup,originHostGroup);
            }else {
                //为匹配到 一般是task 按顺序选择一个
                if (unknownTaskHostGroup.size() > index){
                    overrideHostGroupConfig(hostGroup,unknownTaskHostGroup.get(index++));
                }
            }
        }
    }
    public static final void overrideHostGroupConfig(HostGroup hostGroup,HostGroup originHostGroup){
        logger.info("复制实例组配置 源：{} 目标:{}",JacksonUtils.toJson(originHostGroup),JacksonUtils.toJson(hostGroup));
        HashMap<String, Map<String,Object>> blueprintConfigurationHashMap = new HashMap<>();
        for (Map<String, Map<String,Object>> configuration : hostGroup.getConfigurations()) {
            blueprintConfigurationHashMap.putAll(configuration);
        }

        for (Map<String, Map<String,Object>> originConfiguration : originHostGroup.getConfigurations()) {
            for (Map.Entry<String, Map<String,Object>> originConfigurationEntry : originConfiguration.entrySet()) {
                String key = originConfigurationEntry.getKey();
                Map<String,Object> blueprintConfiguration = blueprintConfigurationHashMap.get(key);

                if (blueprintConfiguration == null){
                    blueprintConfiguration = new HashMap<>();
                    HashMap<String, Map<String, Object>> blueprintConfigurationMap = new HashMap<>();
                    blueprintConfigurationMap.put(key,blueprintConfiguration);
                    hostGroup.getConfigurations().add(blueprintConfigurationMap);
                }
                blueprintConfiguration.putAll(originConfigurationEntry.getValue());
            }
        }
    }

    /**
     * 覆盖默认配置
     *
     * @param originBlueprint
     */

    void overrideDefaultConfig(Blueprint blueprint, Blueprint originBlueprint ) {
        if (originBlueprint == null || CollectionUtils.isEmpty(originBlueprint.getConfigurations())) {
            return;
        }

        // 用原集群的配置，覆盖新创建的Blueprint
        for (Map<String, BlueprintConfiguration> configuration : originBlueprint.getConfigurations()) {
            if (configuration.size() != 1) {
                logger.warn("配置项格式不正确，应该只包含一个配置项：{}", JacksonUtils.toJson(configuration));
            }

            String configType = null;
            BlueprintConfiguration originConfig = null;
            Optional<Map.Entry<String, BlueprintConfiguration>> first = configuration.entrySet().stream().findFirst();
            configType = first.get().getKey();
            originConfig = first.get().getValue();

            if (Objects.nonNull(configType) && Objects.nonNull(originConfig)) {
                BlueprintConfiguration config = blueprint.findConfigByConfigType(configType);
                if (Objects.nonNull(config)) {

                    if (Objects.equals(configType, ConfigClassification.HADOOP_METRICS2.getClassification())) {
                        logger.info(ConfigClassification.HADOOP_METRICS2.getClassification() + " 配置不进行覆盖：Ganglia配置自动生成或手动设置");
                        continue;
                    }
                    logger.info("原集群的配置文件在新集群中存在， 进行覆盖：{}", configType);

                    for (Map.Entry<String, Object> origin : originConfig.getProperties().entrySet()) {
                        String oldCfgValue = String.valueOf(config.getProperty(origin.getKey()));
                        // 包含 HOSTGROUP:: 的配置不能覆盖，因为这种配置Ambari会进行变量替换
                        if (oldCfgValue.contains("HOSTGROUP::")) {
                            logger.info("默认集群配置包含 HOSTGROUP::，不进行覆盖：{}， {}， {}", blueprint.getBlueprints().getBlueprintName(),
                                    origin.getKey(), oldCfgValue);
                            continue;
                        }
                        config.putProperties(origin.getKey(), origin.getValue());
                    }
                } else {
                    logger.info("原集群的配置文件在新集群中不存在， 不进行覆盖：{}", configType);
                }
            }
        }
    }


    /**
     * 设置部署布局
     *
     * @param cmd       创建命令
     * @param blueprint Blueprint
     */
    private void setDeployLayout(CreateBlueprintCmd cmd, Blueprint blueprint) {
        if (CollectionUtils.isNotEmpty(cmd.getHostGroups())) {
            DeployLayoutGenerator layoutGenerator = SpringContextUtil.getBean(DefaultDeployLayoutGenerator.class);
            List<HostGroup> hostGroups = layoutGenerator.generate(cmd.getStackName()+"-"+cmd.getStackVersion(), cmd.getHostGroups(), cmd.getServices(), cmd.isHa());
            // 设置HostGroup
            blueprint.setHostGroups(hostGroups);
        }
//        else {
//            // Host组
//            Map<HostGroupRole, Integer> hostGroupMap = new HashMap<>();
//            if (cmd.isHa()) {
//                hostGroupMap.put(HostGroupRole.AMBARI, cmd.getAmbariHosts().size());
//            }
//            hostGroupMap.put(HostGroupRole.MASTER, cmd.getMasterHosts().size());
//            hostGroupMap.put(HostGroupRole.CORE, cmd.getCoreHosts().size());
//            hostGroupMap.put(HostGroupRole.TASK, cmd.getTaskHosts().size());
//
//
//            DeployLayoutGenerator layoutGenerator = SpringContextUtil.getBean(DefaultDeployLayoutGenerator.class);
//            List<HostGroup> hostGroup = layoutGenerator.generate(cmd.getStackName(), hostGroupMap, cmd.getServices(), cmd.isHa());
//
//            // 设置HostGroup
//            blueprint.setHostGroups(hostGroup);
//        }
    }

    @Override
    public ClusterTemplate createCluterCreateTemplate(CreateClusterTemplateCmd cmd) {
        ClusterTemplate template = new ClusterTemplate(cmd.getClusterName(), cmd.getBlueprint());

        // 设置主机组
        template.initHostGroupsAndConfiguration(cmd.getHostGroups());

        // 计算配置
        template.computeConfiguration();

        // 设置自定义信息并覆盖配置
        template.initOverrideConfiguration(cmd.getConfigurations());

        return template;
    }

    /**
     * 创建集群
     *
     * @param cmd 创建集群的请求参数
     * @return 创建集群响应对象
     */
    @Override
    public InProgressResult createCluster(CreateClusterCmd cmd) {
        logger.info("开始创建集群，请求参数为：{}", JacksonUtils.toJson(cmd));
        InProgressResult result = new InProgressResult();
        try {
            // 替换Spark的配置为spark3
            handleSparkConfig(cmd.getServices(), cmd.getConfigurations());

            // 创建Blueprint
            Blueprint blueprint = buildBlueprintObject(cmd);
            logger.info("buildBlueprintObject-blueprint:{}", JSONUtil.toJsonStr(blueprint));

            // 创建CreateClusterTemplate
            ClusterTemplate clusterCreateTemplate = buildCreateClusterTemplateObject(cmd, blueprint);

            // 动态生成NodeManager的资源配置
            generateNodeManagerResourceConfig(cmd.getHostGroups(), clusterCreateTemplate);

            // 动态生成Core实例组AM标签配置
            generateCoreAmLabelConfig(blueprint, clusterCreateTemplate);

            // 动态生成Task实例组的多磁盘配置
            generateAllTaskGroupMultiDiskConfig(cmd.getStackVersion(), cmd.getHostGroups(), clusterCreateTemplate, blueprint.getHa());

            List<ClusterTemplateHostGroup> hostGroups = clusterCreateTemplate.getHostGroups();
            for (ClusterTemplateHostGroup hostGroup : hostGroups) {
                blueprint.addHostGroupConfiguration(hostGroup.getName(), hostGroup.getConfigurations());
            }

            // 保存布局快照
            logger.info("saveLayout-blueprint:{}", JSONUtil.toJsonStr(blueprint));
            saveLayout(cmd.getClusterId(), blueprint, clusterCreateTemplate);

            // 调用Ambari接口, 新建Blueprint
            logger.info("创建Blueprint：{}， 内容为：{}", cmd.getBlueprintName(), JacksonUtils.toJson(blueprint));
            createBlueprint(cmd, blueprint);
            // 保存 Blueprint内容

            // 调用Ambari接口, 创建集群
            logger.info("创建集群创建模板，集群名称：{}, 内容为：{}", cmd.getClusterName(), JacksonUtils.toJson(clusterCreateTemplate));
            CreateClusterResponseWrapper response = createClusterTemplate(cmd, clusterCreateTemplate);
            logger.info("创建集群创建模板完成，集群名称：{}, 内容为：{}", cmd.getClusterName(), JacksonUtils.toJson(response));
            // 保存集群创建模板

            // 查询ambari配置组信息更新配置组
//            clusterService.updateLocalClusterConfig(cmd.getClusterId());
            delayUpdateLocalClusterConfig(clusterService, cmd.getClusterId());

            result.setSuccess(true)
                    .setClusterName(cmd.getClusterName())
                    .setRequestId(new Long(response.getRequestId()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.setSuccess(false)
                    .setMessage(ex.getMessage())
                    .setClusterName(cmd.getClusterName());
        }

        return result;
    }

    private void generateAllTaskGroupMultiDiskConfig(String stackVersion, List<ClusterHostGroup> hostGroups,
                                                     ClusterTemplate clusterCreateTemplate,
                                                     ConfigItemType ha) {
        logger.info("开始计算Task实例组的多磁盘配置");
        for (ClusterHostGroup hostGroup : hostGroups) {
            if (hostGroup.getRole() == HostGroupRole.TASK) {
                List<HostInstance> hosts = hostGroup.getHosts();
                List<Map<String, BlueprintConfiguration>> multiDiskConfig = generateOneTaskGroupMultiDiskConfig(stackVersion, hosts, ha);
                for (Map<String, BlueprintConfiguration> config : multiDiskConfig) {
                    clusterCreateTemplate.overrideHostGroupConfiguration(hostGroup.getGroupName(), config);
                }
            }
        }
    }

    public List<Map<String, BlueprintConfiguration>> generateOneTaskGroupMultiDiskConfig(String stackVersion, List<HostInstance> hosts, ConfigItemType ha) {
        if (CollectionUtil.isEmpty(hosts)) {
            logger.error("设置Task实例组中多磁盘配置时，Task实例组主机数量为0，跳过配置多磁盘");
            return Lists.newArrayList();
        }

        HostInstance hostInstance = hosts.get(0);
        if (CollectionUtils.isEmpty(hostInstance.getDisks())) {
            logger.warn("主机未设置数据盘，无法根据数据盘数量计算数据盘配置参数, HostRole={}, HostName={}", hostInstance.getHostRole(), hostInstance.getHostName());
            return Lists.newArrayList();
        }

        // 生成配置
        // 从数据库加载HBase的多磁盘配置项
        List<AmbariConfigItem> items = configItemMapper.queryByDynamicTypeAndItemType(stackVersion, DynamicType.MULTI_DISK_TASK.name(), ha.name());
        Map<String, BlueprintConfiguration> allConfigFileConfig = new HashMap<>();
        int diskCount = hostInstance.getDisks().get(0).getCount();
        for (AmbariConfigItem item : items) {
            BlueprintConfiguration config = allConfigFileConfig.get(item.getConfigTypeCode());
            if (Objects.isNull(config)) {
                config = new BlueprintConfiguration(item.getConfigTypeCode());
                allConfigFileConfig.put(item.getConfigTypeCode(), config);
            }
            config.getProperties().put(item.getKey(), generateMultiDiskPath(item.getValue(), diskCount));
        }

        // 将配置转换为返回格式
        List<Map<String, BlueprintConfiguration>> multiDiskConfig = new ArrayList<>();
        for (Map.Entry<String, BlueprintConfiguration> entry : allConfigFileConfig.entrySet()) {
            Map<String, BlueprintConfiguration> config = new HashMap<>();
            config.put(entry.getKey(), entry.getValue());
            multiDiskConfig.add(config);
        }
        return multiDiskConfig;
    }

    private String generateMultiDiskPath(String path, int diskCount) {
        if (diskCount <= 1) {
            return path;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(path);
        for (int i=1; i<diskCount; i++) {
            String diskNPath = path.replace("disk0", "disk" + i);
            sb.append(",").append(diskNPath);
        }
        return sb.toString();
    }

    void generateNodeManagerResourceConfig(List<ClusterHostGroup> hostGroups, ClusterTemplate clusterCreateTemplate) {
        logger.info("=============开始计算NodeManager属性");
        // 动态计算配置  groupName -> groupConfig
        Map<String, List<BlueprintConfiguration>> computedHostGroupConfigs = generateResourceConfig(hostGroups);
        logger.info("=============动态计算配置为：{}", JSON.toJSONString(computedHostGroupConfigs));

        // 遍历动态计算
        for (ClusterTemplateHostGroup templateHostGroup : clusterCreateTemplate.getHostGroups()) {
            logger.info("============= 开始设置CreateTemplate的动态参数：{}", templateHostGroup.getName());
            // 获取某个实例组的动态配置，将这个动态配置覆盖现有的配置
            List<BlueprintConfiguration> computedConfigs = getFromMapIgnoreCase(computedHostGroupConfigs, templateHostGroup.getName());
            logger.info("============取到了动态参数值:{}", JSON.toJSONString(computedConfigs));
            templateHostGroup.addConfigurationIfNotExist(computedConfigs);
        }
    }

    /**
     * 生成Core实例组的标签配置,
     * 如果开启标签, Core配置组和默认配置组都需要修改配置<br/>
     * @param blueprint
     * @param clusterCreateTemplate
     */
    private void generateCoreAmLabelConfig(Blueprint blueprint, ClusterTemplate clusterCreateTemplate) {
        String coreAmLabelKey = "core.am-label.enabled";
        BlueprintConfiguration yarnConfigs = clusterCreateTemplate.findBlueprintConfiguration(ConfigClassification.YARN_SITE.getClassification());
        if (Objects.isNull(yarnConfigs)) {
            logger.info("集群创建时, 未配置yarn-site的配置, 所以未开启Core实例组am标签. core.am-label.enabled=false");
            CustomConfigGenerator generator = new AMLabelDisabledDefaultConfigGenerator();
            List<BlueprintConfiguration> defaultAmLabelConfig = generator.generate(null);
            // 开始覆盖Core实例组的配置
            overrideCoreHostGroupConfiguration(clusterCreateTemplate, null, defaultAmLabelConfig);

            // 清理非AM标签的配置
            ConfigCleaner configCleaner = new AMLabelDisabledConfigCleaner();
            configCleaner.clean(blueprint, clusterCreateTemplate);
            return;
        }

        String amLabelEnabled = Convert.toStr(yarnConfigs.getProperty(coreAmLabelKey));
        if (StrUtil.equalsIgnoreCase("true", StrUtil.trim(amLabelEnabled))) {
            logger.info("集群创建时,开启了Core实例组am标签. core.am-label.enabled={}", amLabelEnabled);
            CustomConfigGenerator yarnGenerator = new AMLabelEnabledYarnConfigGenerator();
            List<BlueprintConfiguration> coreAmLabelConfig = yarnGenerator.generate(null);

            CustomConfigGenerator schedulerGenerator = new AMLabelEnabledDefaultConfigGenerator();
            List<BlueprintConfiguration> schedulerConfig = schedulerGenerator.generate(null);

            // 开始覆盖Core实例组的配置
            overrideCoreHostGroupConfiguration(clusterCreateTemplate, coreAmLabelConfig, schedulerConfig);

            yarnConfigs.getProperties().remove(coreAmLabelKey);

        } else {
            logger.info("集群创建时,未开启Core实例组am标签. core.am-label.enabled={}", amLabelEnabled);
            CustomConfigGenerator generator = new AMLabelDisabledDefaultConfigGenerator();
            List<BlueprintConfiguration> coreAmLabelConfig = generator.generate(null);
            // 开始覆盖Core实例组的配置
            overrideCoreHostGroupConfiguration(clusterCreateTemplate, coreAmLabelConfig, null);

            // 清理非AM标签的配置
            ConfigCleaner configCleaner = new AMLabelDisabledConfigCleaner();
            configCleaner.clean(blueprint, clusterCreateTemplate);
        }
    }

    private void overrideCoreHostGroupConfiguration(ClusterTemplate clusterCreateTemplate,
                                                    List<BlueprintConfiguration> coreYarnConfig,
                                                    List<BlueprintConfiguration> defaultSchedulerConfig){
        // 覆写Core实例组的配置
        if (CollectionUtil.isNotEmpty(coreYarnConfig)) {
            for (ClusterTemplateHostGroup hostGroup : clusterCreateTemplate.getHostGroups()) {
                // 找到Core实例组, 然后覆盖配置
                if (StrUtil.containsIgnoreCase(hostGroup.getName(), "core")) {
                    hostGroup.addConfiguration(coreYarnConfig);
                }
            }
        }

        // 覆写默认Scheduler配置
        if (CollectionUtil.isNotEmpty(defaultSchedulerConfig)) {
            for (BlueprintConfiguration config : defaultSchedulerConfig) {
                Map<String, BlueprintConfiguration> configMap = MapUtil.newHashMap();
                configMap.put(config.getConfigItemName(), config);
                clusterCreateTemplate.overrideConfiguration(configMap);
            }
        }
    }

    List<BlueprintConfiguration> getFromMapIgnoreCase(Map<String, List<BlueprintConfiguration>> configs, String groupName) {
        groupName = Objects.isNull(groupName)? "": groupName;
        for (Map.Entry<String, List<BlueprintConfiguration>> entry : configs.entrySet()) {
            if (groupName.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * 创建配置组
     *
     * @param cmd
     */
    private void createConfigGroup(CreateClusterCmd cmd) {
        List<InstanceGroupConfiguration> groupConfigs = new ArrayList<>();
        // Ambari 和 Master ,CORE不用生成配置组，使用集群默认配置就可以了。
        EnumSet<HostGroupRole> skipSet = EnumSet.of(HostGroupRole.AMBARI, HostGroupRole.MASTER, HostGroupRole.CORE);
        for (ClusterHostGroup hostGroup : cmd.getHostGroups()) {
            if (skipSet.contains(hostGroup.getRole())) {
                continue;
            }
            InstanceGroupConfiguration groupConfig = new InstanceGroupConfiguration(hostGroup.getGroupId());
            Map<String, List<ConfClusterHostGroupAppsConfig>> groupedGroupConfigs = hostGroup.groupByConfigClassification();
            // 生成BlueprintConfiguration
            for (Map.Entry<String, List<ConfClusterHostGroupAppsConfig>> entry : groupedGroupConfigs.entrySet()) {
                Map<String, Object> hostGroupConfigMap = entry.getValue().stream()
                        .collect(Collectors.toMap(ConfClusterHostGroupAppsConfig::getConfigItem, ConfClusterHostGroupAppsConfig::getConfigVal));
                groupConfig.putConfig(entry.getKey(), hostGroupConfigMap);
            }

            groupConfigs.add(groupConfig);
        }

        // 创建配置组
        updateClusterConfig(cmd.getAmbarInfo(), cmd.getClusterId(), groupConfigs, null);
    }

    private void saveLayout(String clusterId, Blueprint blueprint, ClusterTemplate cluterCreateTemplate) {
        ClusterComponentLayouts layouts = new ClusterComponentLayouts();
        layouts.setClusterId(clusterId);
        for (HostGroup hostGroup : blueprint.getHostGroups()) {
            logger.info("saveLayout-hostgroup:{}", JSONUtil.toJsonStr(hostGroup));
            for (ComponentObj component : hostGroup.getComponents()) {
                InfoClusterComponentLayout layout = new InfoClusterComponentLayout();
                layout.setClusterId(clusterId);
                Optional<BDComponent> componentOptional = BDComponent.parse(component.getName());
                if (componentOptional.isPresent()) {
                    logger.info("saveLayout-hostgroup-component:{}", componentOptional.get());
                    layout.setServiceCode(componentOptional.get().getService().name());
                }
                layout.setHostGroup(hostGroup.getName());
                layout.setComponentCode(component.getName());
                layout.setIsHa(blueprint.getHa().getId());
                layout.setState("1");
                layout.setCreatedBy("system");
                layout.setCreatedTime(new Date());
                layout.setUpdatedBy("system");
                layout.setUpdatedTime(new Date());

                layouts.addComponentLayout(layout);
            }
        }
        layouts.saveAllLayout();
    }

    /**
     *  获取原集群的Blueprint
     * @return
     */
    @Override
    public Blueprint getBlueprint(AmbariInfo ambariInfo,String clusterName) {
        // 获取原集群的Blueprint
        Blueprint originBlueprint = null;
        try {
            ClustersApi clusterApi = new ClustersApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(clusterApi.getApiClient());
            String blueprintString = clusterApi.getBlueprintString(clusterName);
            logger.info("获取集群Blueprint:{}" , blueprintString);
            if (StrUtil.isNotEmpty(blueprintString)){
                originBlueprint = JacksonUtils.toObj(blueprintString, Blueprint.class);
            }
        } catch (Exception ex) {
            logger.error("获取源集群Blueprint失败：clusterName=" + clusterName, ex);
        }
        return originBlueprint;
    }

    @Override
    public InProgressResult duplicateCluster(DuplicateClusterCmd cmd) {
        logger.info("开始复制集群，复制集群:" + cmd.getOriginClusterName() + ", 请求参数为：{}", JacksonUtils.toJson(cmd));
        InProgressResult result = new InProgressResult();
        try {
            // 获取原集群的Blueprint
            Blueprint originBlueprint = this.getBlueprint(cmd.getOriginAmbariInfo(), cmd.getOriginClusterName());
            if (originBlueprint == null) {
                InfoClusterFinalBlueprint blueprint = infoClusterFinalBlueprintMapper.queryById(cmd.getSrcClusterId());
                if (blueprint != null) {
                    originBlueprint = JacksonUtils.toObj(blueprint.getBlueprintContent(), Blueprint.class);
                }
            }
            if (originBlueprint != null) {
                logger.info("复制集群 originClusterName:{},hostGroups:{}",cmd.getOriginClusterName(),JacksonUtils.toJson(originBlueprint.getHostGroups()));
            }

            // 加载所有的HOSTGROUP参数，
//            List<AmbariConfigItem> hostGroupConfigs = itemMapper.queryHostGroupConfigByItemType(cmd.isHa ? ConfigItemType.HA.name() : ConfigItemType.NON_HA.name());

            // 并覆盖原集群的Blueprint
//            for (AmbariConfigItem cfg : hostGroupConfigs) {
//                BlueprintConfiguration configByConfigType = originBlueprint.findConfigByConfigType(cfg.getConfigTypeCode());
//                if (Objects.nonNull(configByConfigType)) {
//                    configByConfigType.getProperties().put(cfg.getKey(), cfg.getValue());
//                }
//            }

            // /////////////////////////////////
            // 复制集群时，共有三套配置
            // 1. 创建集群时的[默认配置]
            // 2. 用于复制的[源集群配置]
            // 3. 用户[录入的配置]
            // 上面三套配置的覆盖关系为：
            // 1. 使用[默认配置]生成基础的Blueprint配置
            // 2. 使用[源集群配置] 覆盖一遍[默认配置]：有一些配置不能进行覆盖，包括：包含 %HOSTGROUP% 的配置
            // 3. 使用[录入的配置] 再覆盖上面的配置。由于[录入的配置]会在集群创建模板中设置，Ambari会自动进行覆盖，所以此处不进行覆盖操作。
            // //////////////////////////////////
            // 替换Spark的配置为spark3
            handleSparkConfig(cmd.getServices(), cmd.getConfigurations());

            // 创建Blueprint
            Blueprint blueprint = buildBlueprintObject(cmd, originBlueprint);

            // 覆写特殊参数
            overwriteSpecialBlueprintConfig(blueprint);

            // 创建CreateClusterTemplate
            ClusterTemplate cluterCreateTemplate = buildCreateClusterTemplateObject(cmd, blueprint);

            // 动态生成NodeManager的资源配置
            generateNodeManagerResourceConfig(cmd.getHostGroups(), cluterCreateTemplate);

            // 动态生成Core实例组AM标签配置
            generateCoreAmLabelConfig(blueprint, cluterCreateTemplate);

            // 动态生成Task实例组的多磁盘配置
            generateAllTaskGroupMultiDiskConfig(cmd.getStackVersion(), cmd.getHostGroups(), cluterCreateTemplate, blueprint.getHa());

            List<ClusterTemplateHostGroup> hostGroups = cluterCreateTemplate.getHostGroups();
            for (ClusterTemplateHostGroup hostGroup : hostGroups) {
                blueprint.addHostGroupConfiguration(hostGroup.getName(), hostGroup.getConfigurations());
            }

            // 保存布局快照
            logger.info("saveLayout-blueprint:{}", JSONUtil.toJsonStr(blueprint));
            saveLayout(cmd.getClusterId(), blueprint, cluterCreateTemplate);

            // 调用Ambari接口, 新建Blueprint
            logger.info("复制集群，创建Blueprint：{}， Blueprint：{}", cmd.getBlueprintName(), JacksonUtils.toJson(blueprint));
            createBlueprint(cmd, blueprint);

            // 调用Ambari接口, 创建集群
            logger.info("复制集群，创建集群创建模板，集群名称：{}, CreateTemplate：{}", cmd.getClusterName(), JacksonUtils.toJson(cluterCreateTemplate));
            CreateClusterResponseWrapper response = createClusterTemplate(cmd, cluterCreateTemplate);
            logger.info("复制集群，创建集群创建模板完成，集群名称：{}, 内容为：{}", cmd.getClusterName(), JacksonUtils.toJson(response));

            // 查询ambari配置组信息更新配置组
//            clusterService.updateLocalClusterConfig(cmd.getClusterId());
            delayUpdateLocalClusterConfig(clusterService, cmd.getClusterId());

            result.setSuccess(true)
                    .setClusterName(cmd.getClusterName())
                    .setRequestId(response.getRequestId());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.setSuccess(false)
                    .setMessage(ex.getMessage())
                    .setClusterName(cmd.getClusterName());
        }

        return result;
    }

    private void delayUpdateLocalClusterConfig(IClusterService clusterService, String clusterId) {
        new Thread(){
            public void run() {
                try {
                    logger.info("开始异步从Ambari同步配置组数据， clusterId={}", clusterId);
                    // 等30秒后开始查询
                    sleep(30000L);
                    clusterService.updateLocalClusterConfig(clusterId);
                    logger.info("从Ambari同步配置组数据完成, clusterId = ", clusterId);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }.start();
    }

    private void overwriteSpecialBlueprintConfig(Blueprint blueprint) {
        // 覆盖下面三个参数
        // core-site   fs.defaultFS                    hdfs://sunboxcluster
        // core-site   hadoop.proxyuser.hive.hosts     *
        // core-site   hadoop.proxyuser.root.hosts     *
        BlueprintConfiguration config = blueprint.findConfigByConfigType("core-site");
        if (Objects.isNull(config)) {
            return;
        }
        // 高可用时，替换为 sunboxcluster 。非高可用时，使用默认的HOSTGROUP::MASTER1即可。
        if (Objects.equals(ConfigItemType.HA, blueprint.getHa())) {
            config.getProperties().put("fs.defaultFS", "hdfs://sunboxcluster");
        }

        config.getProperties().put("hadoop.proxyuser.hive.hosts", "*");
        config.getProperties().put("hadoop.proxyuser.root.hosts", "*");
    }

    private CreateClusterResponseWrapper createClusterTemplate(CreateClusterCmd cmd, ClusterTemplate cluterCreateTemplate) throws ApiException {
        Gson gson = new Gson();
        ClustersApi clusterApi = new ClustersApi(cmd.getAmbarInfo().getAmbariApiClient());
        setAmbariDebugState(clusterApi.getApiClient());
        String clusterTemplateJson = JacksonUtils.toJson(cluterCreateTemplate);

        logger.info("创建集群:{} ClusterTemplate: {} ", cmd.getClusterName(), clusterTemplateJson);

        ClusterRequestSwagger body = gson.fromJson(clusterTemplateJson, ClusterRequestSwagger.class);
        CreateClusterResponseWrapper response = clusterApi.createCluster(cmd.getClusterName(), body);
        return response;
    }

    @NotNull
    private void createBlueprint(CreateClusterCmd cmd, Blueprint blueprint) throws ApiException {

        BlueprintsApi blueprintApi = new BlueprintsApi(cmd.getAmbarInfo().getAmbariApiClient());
        setAmbariDebugState(blueprintApi.getApiClient());

        String blueprintJson = JacksonUtils.toJson(blueprint);
        Gson gson = new Gson();
        BlueprintSwagger blueprintSwagger = gson.fromJson(blueprintJson, BlueprintSwagger.class);
        try {
            blueprintApi.blueprintServiceCreateBlueprint(cmd.getBlueprintName(), blueprintSwagger);
        } catch (ApiException ex) {
            logger.error("创建Blueprint失败：" + ex.getResponseBody(), ex);
            if (Objects.equals(ex.getCode(), 409)) {
                // Blueprint已经存在，流程继续进行。
                logger.warn("已经存在同名的Blueprint，当前操作可能是重试，不中断业务流程，继续执行。clusterName:" + cmd.getBlueprintName());
            } else {
                // 其它异常 ，中止创建流程
                throw ex;
            }
        }
    }

    private ClusterTemplate buildCreateClusterTemplateObject(CreateClusterCmd cmd, Blueprint blueprint) {
        CreateClusterTemplateCmd createClusterTemplateCmd = new CreateClusterTemplateCmd();
        createClusterTemplateCmd.setClusterName(cmd.getClusterName());
        createClusterTemplateCmd.setConfigurations(cmd.getConfigurations());
        createClusterTemplateCmd.setBlueprint(blueprint);
        Map<HostGroupRole, List<String>> hosts = new HashMap<>();
//        generateHosts(hosts, HostGroupRole.AMBARI, cmd.getAmbariHosts());
//        generateHosts(hosts, HostGroupRole.MASTER, cmd.getMasterHosts());
//        generateHosts(hosts, HostGroupRole.CORE, cmd.getCoreHosts());
//        generateHosts(hosts, HostGroupRole.TASK, cmd.getTaskHosts());
        createClusterTemplateCmd.setHosts(hosts);
        cmd.getHostGroups().removeIf(p -> CollUtil.isEmpty(p.getHosts()));
        createClusterTemplateCmd.setHostGroups(cmd.getHostGroups());
        ClusterTemplate cluterCreateTemplate = createCluterCreateTemplate(createClusterTemplateCmd);
        return cluterCreateTemplate;
    }

    private void generateHosts(Map<HostGroupRole, List<String>> hosts, HostGroupRole role, List<HostInstance> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return;
        }
        List<String> hostNameList = hostList.stream().map(HostInstance::getHostName).collect(Collectors.toList());
        hosts.put(role, hostNameList);
    }

    private Blueprint buildBlueprintObject(CreateClusterCmd cmd) {
        return buildBlueprintObject(cmd, null);
    }

    private Blueprint buildBlueprintObject(CreateClusterCmd cmd, Blueprint originBlueprint) {
        CreateBlueprintCmd createBlueprintCmd = new CreateBlueprintCmd();
        createBlueprintCmd.setHa(cmd.isHa);
        createBlueprintCmd.setBlueprintName(cmd.getBlueprintName());
        createBlueprintCmd.setStackName(cmd.getStackName());
        createBlueprintCmd.setStackVersion(cmd.getStackVersion());
        createBlueprintCmd.setServices(cmd.getServices());
        createBlueprintCmd.setEnableGanglia(cmd.getEnableGanglia());
//        createBlueprintCmd.setAmbariHosts(cmd.getAmbariHosts());
//        createBlueprintCmd.setMasterHosts(cmd.getMasterHosts());
//        createBlueprintCmd.setCoreHosts(cmd.getCoreHosts());
//        createBlueprintCmd.setTaskHosts(cmd.getTaskHosts());
        createBlueprintCmd.setDbConfigs(cmd.getDbConfigs());
        createBlueprintCmd.setMiTenantId(cmd.getMiTenantId());
        createBlueprintCmd.setMiClientId(cmd.getMiClientId());
        createBlueprintCmd.setHostGroups(cmd.getHostGroups());
        return createBlueprint(createBlueprintCmd, originBlueprint);
    }

    /**
     * 查询一个任务的执行进展, 由于要查询每个任务的执行情况, 所以此接口查询时间比较长
     *
     * @param cmd 查询请求
     * @return 进展详情
     */
    @Override
    public QueryProgressResult queryCreateClusterProgress(QueryProgressCmd cmd) {
        QueryProgressResult result = new QueryProgressResult();
        if (Objects.isNull(cmd)) {
            logger.warn("从Ambari查询任务结果时, requestId 为空, 认为任务处理成功");
            result.setProgressPercent(100.0);
            result.setTaskCount(100);
            result.setCompletedTaskCount(100);
            return result;
        }

        try {
            ClustersApi clusterApi = new ClustersApi(cmd.getAmbariInfo().getAmbariApiClient());
            setAmbariDebugState(clusterApi.getApiClient());

            CreateClusterProcess resp = clusterApi.getClusterCreateProcess(cmd.getClusterName(), cmd.getRequestId());

            result.setTaskCount(resp.getRequest().getTaskCount());
            result.setFailedTaskCount(resp.getRequest().getFailedTaskCount());
            result.setAbortedTaskCount(resp.getRequest().getAbortedTaskCount());
            result.setQueuedTaskCount(resp.getRequest().getQueuedTaskCount());
            result.setCompletedTaskCount(resp.getRequest().getCompletedTaskCount());
            result.setTimeoutTaskCount(resp.getRequest().getTimedOutTaskCount());
            result.setPendingHostRequestCount(resp.getRequest().getPendingHostRequestCount());
            result.setProgressPercent(resp.getRequest().getProgressPercent());

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            /*for (CreateClusterProcessTaskWrapper task : resp.getTasks()) {

                CreateClusterProcessTaskDetailWrapper taskResp = clusterApi.getClusterCreateProcessTask(cmd.getClusterName(),
                        cmd.getRequestId(), task.getTasks().getId());

                QueryProgressTask t = new QueryProgressTask();
                t.setCommand(taskResp.getTasks().getCommand());
                t.setCommandDetail(taskResp.getTasks().getCommandDetail());
                t.setHostName(taskResp.getTasks().getHostName());
                t.setStatus(taskResp.getTasks().getStatus());
                t.setRoleName(taskResp.getTasks().getRole());
                t.setTaskId(taskResp.getTasks().getId());
                if (Objects.nonNull(taskResp.getTasks().getStartTime())) {
                    t.setStartTime(sdf.format(new Date(taskResp.getTasks().getStartTime())));
                }
                if (Objects.nonNull(taskResp.getTasks().getEndTime())) {
                    t.setEndTime(sdf.format(new Date(taskResp.getTasks().getEndTime())));
                }

                result.addTask(t);
            }*/
            //logger.info("查询集群：{} 启动状态: {} ", cmd.getClusterName(), result.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return result;

    }

    @Override
    public QueryProgressResult queryCreateClusterProgressWithAllTask(QueryProgressCmd cmd) {
        QueryProgressResult result = new QueryProgressResult();
        result.setRequestId(cmd.getRequestId());
        try {
            ClustersApi clusterApi = new ClustersApi(cmd.getAmbariInfo().getAmbariApiClient());
            setAmbariDebugState(clusterApi.getApiClient());

            CreateClusterProcess resp = clusterApi.getClusterCreateProcess(cmd.getClusterName(), cmd.getRequestId());

            result.setTaskCount(resp.getRequest().getTaskCount());
            result.setFailedTaskCount(resp.getRequest().getFailedTaskCount());
            result.setAbortedTaskCount(resp.getRequest().getAbortedTaskCount());
            result.setQueuedTaskCount(resp.getRequest().getQueuedTaskCount());
            result.setCompletedTaskCount(resp.getRequest().getCompletedTaskCount());
            result.setTimeoutTaskCount(resp.getRequest().getTimedOutTaskCount());
            result.setPendingHostRequestCount(resp.getRequest().getPendingHostRequestCount());
            result.setProgressPercent(resp.getRequest().getProgressPercent());

            clusterApi.getApiClient().setDebugging(false);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (CreateClusterProcessTaskWrapper task : resp.getTasks()) {

                CreateClusterProcessTaskDetailWrapper taskResp = clusterApi.getClusterCreateProcessTask(cmd.getClusterName(),
                        cmd.getRequestId(), task.getTasks().getId());

                QueryProgressTask t = new QueryProgressTask();
                BeanUtil.copyProperties(taskResp.getTasks(), t, false);
                result.addTask(t);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return result;

    }

    /**
     * 启动集群里的服务
     *
     * @param clusterName 集群名称
     * @param services    要启动的服务列表
     */
    @Override
    public void startClusterService(AmbariInfo ambariInfo, String clusterName, List<String> services) {
        if (CollectionUtils.isEmpty(services)) {
            return;
        }
        ClusterServicesApi clusterServicesApi = new ClusterServicesApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(clusterServicesApi.getApiClient());

        for (String service : services) {
            logger.info("开始启动集群:{} 的服务: {} ", clusterName, service);
            try {
                ClusterServiceStateRequest yarnRequest = ClusterServiceStateRequest.buildSerivceRequest(clusterName, service, ServiceOp.START);
                CreateClusterResponseWrapper response = clusterServicesApi.serviceServiceUpdateService(service, clusterName, yarnRequest);
                logger.info("集群 {} 的服务 {} 启动完成, 响应报文: {}", clusterName, service, JacksonUtils.toJson(response));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 启动一个集群的全部服务，适用于非高可用下的首次启动和后续的集群服务启动。<br/>
     * 此接口不适用于高可用环境下的集群首次启动。高可用环境下的集群首次启动使用 <code>startAllClusterServicesHA</code>方法。
     *
     * @param ambariInfo
     * @param clusterName
     * @param clusterId
     * @return
     */
    @Override
    public InProgressResult startAllClusterServices(AmbariInfo ambariInfo, String clusterName, String clusterId) {
        ClusterServicesApi clusterServicesApi = new ClusterServicesApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(clusterServicesApi.getApiClient());

        InProgressResult result = new InProgressResult();
        logger.info("开始启动集群全部服务");
        try {
            ClusterServiceStateRequest startRequest = ClusterServiceStateRequest.buildStartAllSerivceRequest(clusterName, ServiceOp.START);
            CreateClusterResponseWrapper response = clusterServicesApi.serviceServiceUpdateService(clusterName, startRequest);
            logger.info("集群 {} 启动完成, 响应报文: {}", clusterName, JacksonUtils.toJson(response));

            result.setRequestId(Long.valueOf(response.getRequestId()));
            result.setSuccess(true);
            result.setClusterName(clusterName);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    /**
     * 启动HA的集群
     *
     * @param ambariInfo
     * @param clusterName
     * @return
     */
    @Override
    public InProgressResult startAllClusterServicesHA(AmbariInfo ambariInfo, String clusterName, String clusterId) {
        ClustersApi clusterApi = new ClustersApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(clusterApi.getApiClient());
        ClusterServicesApi clusterServicesApi = new ClusterServicesApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(clusterServicesApi.getApiClient());

        InProgressResult result = new InProgressResult();
        logger.info("开始启动HA集群全部服务");
        try {
            logger.info("查找集群部署的Master主机");

            //0---queryCreateClusterProgress(QueryProgressCmd cmd)
            List<InfoClusterVm> masterVms = vmMapper.selectByClusterIdAndRole(clusterId, "master");
            if (CollectionUtils.isEmpty(masterVms)) {
                throw new RuntimeException("master主机组中没有主机信息：clusterName = " + clusterName
                        + ", clusterId = " + clusterId);
            }
            if (masterVms.size() != 2) {
                throw new RuntimeException("master主机组中包含非2台主机：" + masterVms.toString());
            }

            String hostMaster1 = masterVms.get(0).getHostName();
            String hostMaster2 = masterVms.get(1).getHostName();
            logger.info("Master主机组的主机为：" + hostMaster1 + "   " + hostMaster2);

            //1、Start Zookeeper---startClusterService
            logger.info("1/7 开始启动ZooKeeper... ...");
            startClusterService(ambariInfo, clusterName, Lists.newArrayList("ZOOKEEPER"));
            logger.info("1/7 启动ZooKeeper完成 ");

            Thread.sleep(1000);

            //2、Start HDFS JounalNode----startClusterService
            logger.info("2/7 开始启动HDFS... ...");
            startClusterService(ambariInfo, clusterName, Lists.newArrayList("HDFS"));
            logger.info("2/7 启动HDFS完成 ");

            Thread.sleep(1000);

            //3、Format NameNode
            // NameNode1格式化
            logger.info("3/7 开始格式化NameNode1... ...");
            String serviceName = "HDFS";
            String componentName = "NAMENODE";
            ClusterRequest req = ClusterRequest.buildFormatRequest(serviceName, componentName, hostMaster1);
            clusterApi.clusterRequests(clusterName, req);
            logger.info("3/7 开始格式化NameNode1完成 ");
            Thread.sleep(1000);

            //4、Format ZKFC
            // 两台ZKFC全都格式化
            logger.info("4/7 开始格式化ZKFC... ...");
            serviceName = "HDFS";
            componentName = "ZKFC";
            req = ClusterRequest.buildFormatRequest(serviceName, componentName, hostMaster1);
            clusterApi.clusterRequests(clusterName, req);
            req = ClusterRequest.buildFormatRequest(serviceName, componentName, hostMaster2);
            clusterApi.clusterRequests(clusterName, req);
            logger.info("4/7 格式化ZKFC完成 ");
            Thread.sleep(1000);

            //5、Start HDFS----startClusterService
            logger.info("5/7 开始启动HDFS... ...");
            startClusterService(ambariInfo, clusterName, Lists.newArrayList("HDFS"));
            logger.info("5/7 启动HDFS完成 ");
            Thread.sleep(1000);

            //6、Bootstrap standby
            // NameNode2启动
            logger.info("6/7 开始Bootstrap StandBy - NameNode2... ...");
            serviceName = "HDFS";
            componentName = "NAMENODE";
            req = ClusterRequest.buildBootstrapStandByRequest(serviceName, componentName, hostMaster2);
            clusterApi.clusterRequests(clusterName, req);
            logger.info("6/7 启动Bootstrap StandBy完成 ");
            Thread.sleep(1000);

            //7、Start ALL----startAllClusterServices
            logger.info("7/7 开始启动全部服务... ...");
            InProgressResult createClusterResult = startAllClusterServices(ambariInfo, clusterName, clusterId);
            logger.info("7/7 启动全部服务完成 ");

            result.setRequestId(Long.valueOf(createClusterResult.getRequestId()));
            result.setSuccess(true);
            result.setClusterName(clusterName);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    @Override
    public void enableClusterAutoStart(AmbariInfo ambariInfo, String clusterName, List<String> services) {
        if (CollectionUtils.isEmpty(services)) {
            logger.warn("设置自动启动的大数据服务列表为空，系统不调用Ambari接口");
            return;
        }

        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());

        // 根据服务，拿到这些服务的大数据组件（不区分是否高可用，所有组件都统一处理）
        List<String> componentList = new ArrayList<>();
        for (String service : services) {
            Optional<BDService> serviceOptional = BDService.parse(service);
            serviceOptional.ifPresent(svr -> {
                svr.getComponents().forEach(c -> {
                    if (Objects.nonNull(c)) {
                        componentList.add(c.name());
                    } else {
                        logger.error("Service中的组件为Null, service:{}, Components:{}", svr.name(), svr.getComponents());
                    }
                });
            });
        }

        // 请求Ambari，选中需要自动重启的服务
        ComponentAutoStartRequest request = new ComponentAutoStartRequest();
        request.setEnableAutoStart(true);
        request.setComponents(componentList);
        api.componentAutoStart(clusterName, request);

        // 设置自动启动
        api.autoStartSetting(clusterName, true);
    }

    /**
     * 取消一个集群里的某些服务可以自动启动<br/>
     *
     * @param ambariInfo
     * @param clusterName
     * @param services
     */
    @Override
    public void disableClusterAutoStart(AmbariInfo ambariInfo, String clusterName, List<String> services) {
        if (CollectionUtils.isEmpty(services)) {
            logger.warn("设置自动启动的大数据服务列表为空，系统不调用Ambari接口");
            return;
        }

        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());
        api.autoStartSetting(clusterName, false);

        // 根据服务，拿到这些服务的大数据组件（不区分是否高可用，所有组件都统一处理）
//        List<String> componentList = new ArrayList<>();
//        for (String service : services) {
//            Optional<BDService> serviceOptional = BDService.parse(service);
//            serviceOptional.ifPresent(svr -> {
//                svr.getComponents().forEach(c -> {
//                    if (Objects.nonNull(c)) {
//                        componentList.add(c.name());
//                    } else {
//                        logger.error("Service中的组件为Null, service:{}, Components:{}", svr.name(), svr.getComponents());
//                    }
//                });
//            });
//        }
//
//        // 请求Ambari，开启服务自动启动
//        ComponentAutoStartRequest request = new ComponentAutoStartRequest();
//        request.setEnableAutoStart(false);
//        request.setComponents(componentList);
//        api.componentAutoStart(clusterName, request);
    }

    @Override
    public void addHosts(AmbariInfo ambariInfo, String clusterName, List<String> hosts) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            actionApi.addHost(clusterName, hosts);
        } catch (ApiException apiex) {
            //409为重复添加，实际是成功的
            if (!(apiex.getCode() == 409)) {
                throw apiex;
            } else {
                logger.info(clusterName + "addhost:" + hosts);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void configHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts, List<String> components) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            actionApi.configHostComponent(clusterName, hosts, components);
        } catch (ApiException apiException) {
            if (!(apiException.getCode() == 409)) {
                logger.error(clusterName + ": 配置新增机器组件,ex", apiException);
                throw apiException;
            }
        } catch (Exception ex) {
            logger.error(clusterName + ": 配置新增机器组件,ex", ex);
            throw ex;
        }
    }

    @Override
    public InProgressResult installHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts) {
        InProgressResult result = new InProgressResult();
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            InProgressResponse response = actionApi.installHostComponent(clusterName, hosts);
            if (Objects.isNull(response)) {
                L l = L.b().p("clusterName", clusterName).p("hosts", hosts);
                logger.error("第一次安装组件失败，返回结果为空，再重试一次：" + l.s());
                Thread.sleep(2000);
                response = actionApi.installHostComponent(clusterName, hosts);
            }
            if (Objects.isNull(response)) {
                L l = L.b().p("clusterName", clusterName).p("hosts", hosts);
                logger.error("第二次安装组件失败，返回结果为空，请登录Ambari确认是否有主机已经被逐出：" + l.s());
                result.setSuccess(false);
                result.setClusterName(clusterName);
                result.setMessage("安装组件失败，返回结果为空，请登录Ambari确认是否主机已经被逐出：" + l.s());
                return result;
            } else {
                result.setClusterName(clusterName);
                result.setSuccess(true);
                result.setRequestId(response.getRequestId());
                return result;
            }
        } catch (Exception ex) {
            L l = L.b().p("clusterName", clusterName).p("hosts", hosts);
            logger.error(ex.getMessage() + l.s(), ex);
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(l.s() + ExceptionUtils.getStackTrace(ex));
        }
        return result;
    }


    @Override
    public InProgressResult startHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts,
                                               List<String> components) {
        InProgressResult result = new InProgressResult();
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            InProgressResponse response = actionApi.startHostComponent(clusterName, hosts, components);
            result.setClusterName(clusterName);
            result.setSuccess(true);
            result.setRequestId(response.getRequestId());
            return result;
        } catch (Exception ex) {
            L l = L.b().
                    p("clusterName", clusterName).
                    p("hosts", hosts).
                    p("components", components);
            logger.error(ex.getMessage() + l.s(), ex);
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(l.s() + ExceptionUtils.getStackTrace(ex));
        }
        return result;
    }


    /**
     * core 节点数据平衡
     *
     * @param ambariInfo
     */
    @Override
    public void rebalanceDataForCore(AmbariInfo ambariInfo, String clusterName) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            QueryComponentInHostsResponse response = getComponmentInhostResp(clusterName, actionApi);

            actionApi.rebalanceHdfs(clusterName, response.getActiveNameNodeHostName(), 10);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * core 节点数据平衡By ClusterId
     *
     * @param ambariInfo
     * @param clusterName
     * @param clusterId
     */
    @Override
    public void rebalanceDataForCore(AmbariInfo ambariInfo, String clusterName, String clusterId) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            ResultMsg activeMsg = getActiveNameNode(clusterId);
            if (activeMsg.getResult() && activeMsg.getData()!=null && StringUtils.isNotEmpty(activeMsg.getData().toString())){
                actionApi.rebalanceHdfs(clusterName,activeMsg.getData().toString(), 10);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * 获取 commonmentInhost
     * 默认重试3次
     *
     * @param clusterName
     * @param actionApi
     * @return
     */
    private QueryComponentInHostsResponse getComponmentInhostResp(String clusterName, CustomActionApi actionApi) {
        QueryComponentInHostsResponse response = null;
        int i = 0;
        while (i <= Integer.parseInt(queryCompontRetrytimes)) {
            try {
                i++;
                response = actionApi.queryComponentInHosts(clusterName, Arrays.asList("NAMENODE"));
                logger.info("rebalanceDataForCore,response:" + JSON.toJSONString(response));
                if (response.getItems().get(0).getHostComponents().get(0).getMetrics() != null) {
                    return response;
                }
                Thread.sleep(3000L);
            } catch (Exception e) {
                logger.error("getComponmentInhostResp,异常：", e);
            }
        }
        return response;
    }

    /**
     * 查询一个组件所在的主机
     *
     * @param clusterId 集群ID
     * @param component 组件名
     * @return
     */
    @Override
    public QueryComponentInHostsResponse getComponentInHosts(String clusterId, String component) {
        AmbariInfo ambariInfo = clusterService.getAmbariInfo(clusterId);
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());
        QueryComponentInHostsResponse response = api.queryComponentInHosts(confCluster.getAmbariClusterName(), Arrays.asList(component));
        return response;
    }

    public InProgressResult decommissionComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts, String componentName) {
        try {
            // 调用接口完成decommission
            logger.info("decommission component request, clusterName:{}, hosts:{}, component: {}",
                    clusterName, org.apache.commons.lang3.StringUtils.join(hosts, ','), componentName);
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            InProgressResponse decommissionResp = actionApi.decommissionComponent(clusterName, hosts, componentName);
            Gson gson = new Gson();
            logger.info("decommission component response: {}", gson.toJson(decommissionResp));

            // 检查结果
            if (Objects.isNull(decommissionResp)) {
                throw new RuntimeException("decommission component 返回结果为空");
            }
            InProgressResult result = new InProgressResult();
            result.setSuccess(true);
            result.setClusterName(clusterName);
            result.setRequestId(decommissionResp.getRequestId().longValue());
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            InProgressResult result = new InProgressResult();
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
            if (ex instanceof ApiException) {
                result.setMessage(((ApiException) ex).getResponseBody());
            }
            return result;
        }
    }

    /**
     * Decommission单个主机上的组件<br/>
     * <b>此接口可以重复调用执行，每次调用返回来的进展requestId会发生变化</b>
     *
     * @param ambariInfo
     * @param clusterName   集群
     * @param hostName      主机名称
     * @param componentName 需要decommission的组件名称, 目前只支持 NODEMANAGER
     * @return Decommission进展
     */
    @Override
    public InProgressResult decommissionComponent(AmbariInfo ambariInfo, String clusterName, String hostName, String componentName) {
        try {
            // 调用接口完成decommission
            logger.info("decommission component request, clusterName:{}, hostName:{}, component: {}",
                    clusterName, hostName, componentName);
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            InProgressResponse decommissionResp = actionApi.decommissionComponent(clusterName, hostName, componentName);
            Gson gson = new Gson();
            logger.info("decommission component response: {}", gson.toJson(decommissionResp));

            // 检查结果
            if (Objects.isNull(decommissionResp)) {
                throw new RuntimeException("decommission component 返回结果为空");
            }
            InProgressResult result = new InProgressResult();
            result.setSuccess(true);
            result.setClusterName(clusterName);
            result.setRequestId(decommissionResp.getRequestId().longValue());
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            InProgressResult result = new InProgressResult();
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
            if (ex instanceof ApiException) {
                result.setMessage(((ApiException) ex).getResponseBody());
            }
            return result;
        }
    }

    public InProgressResult stopHostAllComponents(AmbariInfo ambariInfo, String clusterName, List<String> hosts) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());

            // 调用接口完成decommission
            final List<String>[] components = new List[]{new ArrayList<>()};
            Optional<String> hostName = hosts.stream().findFirst();
            hostName.ifPresent(host -> {
                logger.info("query host component request, clusterName:{}, hosts:{}",
                        clusterName, StringUtils.join(hosts, ','));
                QueryHostsComponentResponse queryHostsComponentResponse = actionApi.queryHostsComponents(clusterName, Arrays.asList(host));
                Gson gson = new Gson();
                logger.info("query host component response: {}", gson.toJson(queryHostsComponentResponse));
                components[0].addAll(queryHostsComponentResponse.getComponentNames(host));
            });

            if (CollectionUtils.isEmpty(components[0])) {
                throw new RuntimeException("关闭主机上的组件失败，没有获取到主机上的组件。hosts=" + StringUtils.join(hosts, ','));
            }

            // 关闭组件
            logger.info("stop all component request, clusterName:{}, hosts:{}",
                    clusterName, StringUtils.join(hosts, ','),
                    StringUtils.join(components[0], ','));
            InProgressResponse inProgressResponse = actionApi.stopHostComponent(clusterName, hosts, components[0]);
            Gson gson = new Gson();
            logger.info("stop all component request, clusterName:{}, hosts:{}", clusterName, gson.toJson(inProgressResponse));

            InProgressResult result = new InProgressResult();
            result.setSuccess(true);
            result.setClusterName(clusterName);
            result.setRequestId(inProgressResponse.getRequestId().longValue());
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            InProgressResult result = new InProgressResult();
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
            if (ex instanceof ApiException) {
                ApiException apiException = (ApiException)ex;
                result.setMessage(apiException.getMessage() + ":" + apiException.getCode() + " : " + apiException.getResponseBody());
            }
            return result;
        }
    }

    public DeleteHostResult deleteHosts(AmbariInfo ambariInfo, String clusterName, List<String> hosts) {
        CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(actionApi.getApiClient());

        DeleteHostResult result = new DeleteHostResult();
        result.setDeleteSuccessHosts(new ArrayList<>());
        result.setDeleteFailHosts(new ArrayList<>());
        boolean allDeletedFlag =true;

        for (String host:hosts){
            try {
                DeleteHostsResponse response = actionApi.deleteHost(clusterName,host);
                result.getDeleteSuccessHosts().add(host);
            }catch (Exception e){
                logger.error("从Ambari删除Host失败:" + e.getMessage(), e);
                allDeletedFlag = false;
                result.getDeleteFailHosts().add(host);
            }

            try {
                actionApi.deleteHost(host);
            }catch (Exception e){
                logger.error("补偿从Ambari删除Host失败:" + e.getMessage(), e);
            }
        }
        result.setAllDeleted(allDeletedFlag);
        return result;
    }

    /**
     * 根据hostname查询实例安装的组件
     *
     * @param ambariInfo  ambariInfo
     * @param clusterName 集群名称
     * @param hostname    机器名称
     * @return
     */
    @Override
    public List<String> getComponentsByHost(AmbariInfo ambariInfo, String clusterName, String hostname) {

        List<String> components = new ArrayList<>();

        try {
            List<String> hosts = new ArrayList<>();
            hosts.add(hostname);
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            components = actionApi.queryHostsComponents(clusterName, hosts).getComponentNames(hostname);
            return components;
        } catch (Exception e) {
            logger.error("根据host获取组件异常，", e);
            throw e;
        }

    }

    /**
     * 获取active的组件所在机器的hostname
     *
     * @param clusterId
     * @param componentName
     * @return
     */
    @Override
    public ResultMsg getActiveComponentHostName(String clusterId, String componentName) {
        ResultMsg resultMsg = new ResultMsg();
        Integer i = 0;
        while (true) {
            try {
                QueryComponentInHostsResponse componentInHosts = getComponentInHosts(clusterId, componentName);

                List<HostRole> hosts = componentInHosts.getHosts(componentName, true);
                if (CollectionUtils.isNotEmpty(hosts)) {
                    resultMsg.setResultSucces("Success");
                    resultMsg.setData(hosts.get(0).getHostName());
                } else if (CollectionUtils.isEmpty(hosts) && componentInHosts.getHostCount() == 1) {
                    HostRole firstHost = componentInHosts.getFirstHost();
                    resultMsg.setResultSucces("Success");
                    resultMsg.setData(firstHost.getHostName());
                } else {
                    resultMsg.setResultFail("没有找到主机");
                    resultMsg.setErrorMsg("没有找到主机");
                }
                return resultMsg;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                resultMsg.setResultFail(ex.getMessage());
                resultMsg.setErrorMsg(ex.getMessage());
            }
            i++;
            if (i > sdpAmbariRetryCount){
                return resultMsg;
            }
            ThreadUtil.sleep(1000 * sdpAmbariRetryDuration);

        }
    }

    /**
     * 返回集群所有机器
     *
     * @param ambariInfo
     * @param clusterName
     * @return
     */
    @Override
    public List<String> queryAllHosts(AmbariInfo ambariInfo, String clusterName) {

        List<String> hosts = new ArrayList<>();
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            hosts = actionApi.queryAllHostsName(clusterName);
            return hosts;
        } catch (Exception e) {
            logger.error("根据host获取组件异常，", e);
            throw e;
        }
    }

    public List<String> queryHosts(AmbariInfo ambariInfo, String clusterName, AmbariHostState state) {
        try {
            CustomActionApi actionApi = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(actionApi.getApiClient());
            List<Hosts> hosts;
            if (StringUtils.isNotEmpty(clusterName)){
                hosts = actionApi.queryAllHosts(clusterName);
            }else{
                hosts = actionApi.queryAllHosts();
            }

            List<String> selectedHosts = new ArrayList<>();
            for (Hosts host : hosts) {
                if (Objects.isNull(state)) {
                    selectedHosts.add(host.getHostName().trim());
                } else if (StrUtil.equalsIgnoreCase(host.getHostStatus(), state.name())) {
                    selectedHosts.add(host.getHostName().trim());
                }
            }
            return selectedHosts;
        } catch (Exception e) {
            logger.error("根据host获取组件异常，", e);
            throw e;
        }
    }

    @Override
    public List<String> verifyHostsFromAmbari(AmbariInfo ambariInfo, String clusterName, List<String> hosts) {
        List<String> hostsInAmbari = queryAllHosts(ambariInfo, clusterName);
        if (CollectionUtil.isEmpty(hostsInAmbari)) {
            logger.warn("从Ambari查询所有主机名时，返回空列表。[clusterName={}]", clusterName);
            return hosts;
        }

        List<String> validHosts = new ArrayList<>();
        Set<String> ambariHostSet = new HashSet<>(hostsInAmbari);
        for (String host : hosts) {
            if (ambariHostSet.contains(host)) {
                validHosts.add(host);
            }
        }
        return validHosts;
    }

    /**
     * 查询 DataNode 节点decommionsion进度
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public ResultMsg queryDataNodeDecommionsionProcess(String clusterId, List<String> hostNames) {

        ResultMsg resultMsg = new ResultMsg();

        try {
            String masterHostname = getMasterHostName(clusterId);
            logger.info("获取到masterhostname：{}", masterHostname);

            if (StringUtils.isEmpty(masterHostname)) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("为获取到master地址。");
                return resultMsg;
            }

            Map<String, String> hostdecommison = getDataNodeState(masterHostname, hadoopNameNodeJmxApiPort, hostNames);

            int totalcnt = hostNames.size();

            AtomicInteger decommissioning_cnt = new AtomicInteger();
            AtomicInteger decommissioned_cnt = new AtomicInteger();

            hostdecommison.entrySet().stream().forEach(x -> {
                if (hostNames.contains(x.getKey())) {
                    if (x.getValue().toString().toLowerCase().contains("decommissioned")) {
                        decommissioned_cnt.getAndIncrement();
                    } else {
                        decommissioning_cnt.getAndIncrement();
                    }
                }
            });

            if (totalcnt == decommissioned_cnt.get()) {
                resultMsg.setResult(true);
                resultMsg.setData(hostdecommison);
                return resultMsg;
            }
            resultMsg.setResult(false);
            resultMsg.setData(hostdecommison);
            return resultMsg;
        } catch (Exception e) {
            logger.error("获取datanode节点decommission异常，", e);
            return resultMsg;
        }
    }

    /**
     * 查询nodeManager 节点decommission 进度
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public ResultMsg queryNodeManagerDecommionsionProcess(String clusterId, List<String> hostNames) {
        ResultMsg resultMsg = new ResultMsg();

        try {
            ResultMsg getmsg = getActiveResourceManager(clusterId);

            if (!getmsg.getResult()){
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("未获取到ActiveResourceManager。");
                return resultMsg;
            }

            String masterHostname = getmsg.getData().toString();
            logger.info("获取到getActiveResourceManager：{}", masterHostname);

            if (StringUtils.isEmpty(masterHostname)) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("未获取到master地址。");
                return resultMsg;
            }

            Map<String, String> hostdecommison = getNodesFromYarn(
                    masterHostname,
                    hadoopJmxApiPort,
                    Arrays.asList("RUNNING"));

            //获取Node节点信息失败
            if (hostdecommison ==null || hostdecommison.size()==0){
                logger.error("获取Node节点信息失败.");
                resultMsg.setResult(false);
                return resultMsg;
            }

            AtomicInteger leftCount = new AtomicInteger();

            hostdecommison.entrySet().stream().forEach(x -> {
                if (hostNames.contains(x.getKey())) {
                    leftCount.getAndIncrement();
                }
            });

            // 状体为：RUNNING 和DECOMMISSIONING 节点中不包含要缩容的节点 = DECOMMISSION 完成
            if (leftCount.get()==0) {
                resultMsg.setResult(true);
                resultMsg.setData(hostdecommison);
                return resultMsg;
            }
            resultMsg.setResult(false);
            resultMsg.setData(hostdecommison);
            return resultMsg;
        } catch (Exception e) {
            logger.error("获取NodeManager节点decommission异常，", e);
            return resultMsg;
        }
    }

    /**
     * 查询NameNode JMX接口获取 DataNode节点状态
     * @param masterHostName active状态的NameNode节点IP
     * @param nameNodeUIPort NameNodeJMX节点接口
     * @param vmNames  节点的HostName（机器的FSDN/域名）
     * @return
     */
    private Map<String, String> getDataNodeState(String masterHostName, int nameNodeUIPort, List<String> vmNames) {
        Map<String, String> hosts_state = new HashMap<>();
        com.alibaba.fastjson.JSONObject jsondata = null;
        Integer i =0;
        while (true) {
            try {
                String jmxurl = String.format(
                        "http://%s:%s/jmx?qry=Hadoop:service=NameNode,name=NameNodeInfo&_"+System.currentTimeMillis(),
                        masterHostName,
                        nameNodeUIPort);
                logger.info("jmxurl:"+jmxurl);
                String responsebody = HttpUtil.get(jmxurl);
                logger.info("JMX接口返回结果：{}", responsebody);
                if (StringUtils.isNotEmpty(responsebody)) {
                    jsondata = com.alibaba.fastjson.JSON.parseObject(responsebody);
                    break;
                }
            }catch (Exception e){
                logger.error("JMX接口返回结果,异常",e);
            }
            i++;
            logger.warn("JMX接口,重试："+i);
            if (i > sdpAmbariRetryCount){
                return hosts_state;
            }
            ThreadUtil.sleep(1000 * sdpAmbariRetryDuration);
        }


        if (jsondata != null && null != jsondata.getJSONArray("beans")) {
            com.alibaba.fastjson.JSONObject node = jsondata.getJSONArray("beans").getJSONObject(0);
            com.alibaba.fastjson.JSONObject lnode = JSON.parseObject(node.getString("LiveNodes"));
            com.alibaba.fastjson.JSONObject dnode = JSON.parseObject(node.getString("DeadNodes"));

            vmNames.stream().forEach(x -> {
                String vmName = x.trim().toLowerCase();
                for (String lnodekey : lnode.keySet()) {
                    String vmFromLiveNodeKey = lnodekey;
                    String[] lnodeKeySegments = vmFromLiveNodeKey.split(":");
                    if (lnodeKeySegments.length > 1) {
                        vmFromLiveNodeKey = vmFromLiveNodeKey.substring(0, vmFromLiveNodeKey.length() - lnodeKeySegments[lnodeKeySegments.length - 1].length() - 1);
                    }
                    vmFromLiveNodeKey = vmFromLiveNodeKey.trim().toLowerCase();
                    if (StringUtils.equalsIgnoreCase(vmFromLiveNodeKey, vmName)) {
                        String adminstate = lnode.getJSONObject(lnodekey).getString("adminState");
                        switch (adminstate) {
                            case "In Service":
                                hosts_state.put(x, "InService");
                                break;
                            case "Decommission In Progress":
                                hosts_state.put(x, "Decommissioning");
                                break;
                            case "Decommissioned":
                                hosts_state.put(x, "Decommissioned");
                                break;
                            default:
                                hosts_state.put(x, adminstate);
                                break;
                        }
                        break;
                    }
                }

                for (String dnodeKey : dnode.keySet()) {
                    String[] dnodeKeySegments = dnodeKey.split(":");
                    if (dnodeKeySegments.length > 1) {
                        dnodeKey = dnodeKey.substring(0, dnodeKey.length() - dnodeKeySegments[dnodeKeySegments.length - 1].length() - 1);
                    }
                    dnodeKey = dnodeKey.trim().toLowerCase();
                    if (StringUtils.equalsIgnoreCase(dnodeKey, vmName)) {
                        hosts_state.put(x, "Decommissioned");
                        break;
                    }
                }
            });
            return hosts_state;
        }
        return hosts_state;
    }

    /**
     *
     * 查询ResoureManager webservice接口，获取各个Node的运行状态
     *  效率低，对于节点较多（包含历史节点）的集群不建议使用
     *
     * @param masterHostname
     * @param resourceManagerPort
     * @return 返回 node的HostName 和 node的运行状态
     */
    private Map<String, String> getNodeManagerState(String masterHostname, int resourceManagerPort) {
        Map<String, String> hosts_state = new HashMap<>();

        String yarnurl = String.format(
                "http://%s:%s/ws/v1/cluster/nodes?_=%s",
                masterHostname,
                resourceManagerPort,
                System.currentTimeMillis());

        String responsebody = HttpUtil.get(yarnurl);
        logger.info("JMX接口返回结果：{}", responsebody);


        com.alibaba.fastjson.JSONObject nodes = com.alibaba.fastjson.JSONObject.parseObject(responsebody);

        if (nodes != null
                && null != nodes.get("nodes")
                && null != nodes.getJSONObject("nodes").getJSONArray("node")) {
            com.alibaba.fastjson.JSONArray node = nodes.getJSONObject("nodes").getJSONArray("node");
            if (node != null && node.size() > 0) {
                for (int i = 0; i < node.size(); i++) {
                    com.alibaba.fastjson.JSONObject item = node.getJSONObject(i);
                    hosts_state.put(item.getString("nodeHostName"), item.getString("state"));
                }
            }
        }
        return hosts_state;
    }

    /**
     * 通过调用 Yarn webservice 接口获取各个Node的列表
     * @param masterHostname resourceManager地址
     * @param resourceManagerPort 商品
     * @param states Node的状态，目前支持：NEW, RUNNING, UNHEALTHY, DECOMMISSIONING, DECOMMISSIONED, LOST, REBOOTED, SHUTDOWN
     * @return
     */
    private Map<String, String> getNodesFromYarn(String masterHostname, int resourceManagerPort, List<String> states) {
        Map<String, String> hosts_state = new HashMap<>();

        String yarnurl = String.format(
                "http://%s:%s/ws/v1/cluster/nodes?_=%s",
                masterHostname,
                resourceManagerPort,
                System.currentTimeMillis());
        if (CollectionUtil.isNotEmpty(states)) {
            String statesParam = CollectionUtil.join(states, ",");
            yarnurl = yarnurl + "&states=" + statesParam;
        }

        try {
            logger.info("通过调用 Yarn webservice 接口获取各个Node的列表，url：{}", yarnurl);
            String responsebody = HttpUtil.get(yarnurl, yarnApiTimeOut * 1000);
            logger.info("通过调用 Yarn webservice 接口获取各个Node的列表：{}", responsebody);

            JSONObject nodes = JSONObject.parseObject(responsebody);
            if (nodes != null
                    && null != nodes.get("nodes")
                    && null != nodes.getJSONObject("nodes").getJSONArray("node")) {
                JSONArray node = nodes.getJSONObject("nodes").getJSONArray("node");
                if (node != null && node.size() > 0) {
                    for (int i = 0; i < node.size(); i++) {
                        JSONObject item = node.getJSONObject(i);
                        hosts_state.put(item.getString("nodeHostName"), item.getString("state"));
                    }
                }
            }
        }catch (Exception e){
            logger.error("调用Yarn webservice 异常，",e);
            return null;
        }
        return hosts_state;
    }


    /**
     * 调用jmx查询数据检查
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg hdfsCheck(String clusterId) {
        ResultMsg resultMsg = new ResultMsg();
        ResultMsg activeNameNodeMsg = getActiveComponentHostName(clusterId, "NameNode");
        if (!activeNameNodeMsg.getResult()) {
            logger.error("获取activeNamenode，失败。");
        }
        String activeNameNode = activeNameNodeMsg.getData().toString();
        String fskurl = String.format("http://{}:50070/fsck?ugi=hadoop&path=%2F", activeNameNode);
        //String responseBody=HttpUtil.get()

        //todo


        return null;
    }

    /**
     * 获取集群ActiveResourceManager的HostName
     *
     * @param clusterId 集群ID
     * @return
     */
    @Override
    public ResultMsg getActiveResourceManager(String clusterId) {
        ResultMsg msg=new ResultMsg();
        ConfCluster confCluster=confClusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster.getIsHa().equals(1)) {
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndRole(clusterId, "master");
            vms.stream().forEach(x->{
               boolean isconn = isHostConnectable(x.getHostName(),hadoopJmxApiPort);
               if (isconn && isActiveRM(x,hadoopJmxApiPort)){
                    msg.setResult(true);
                    msg.setData(x.getHostName());
                    return;
               }
            });
            if (!msg.getResult()){
                msg.setErrorMsg("获取ActiveRM异常。");
            }
            return msg;
        }else{
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndRole(clusterId,"ambari");
            if (vms!=null && vms.size()>0){
                msg.setResult(true);
                msg.setData(vms.get(0).getHostName());
                return msg;
            }else{
                msg.setResult(false);
                msg.setErrorMsg("获取ActiveRM异常。");
                return msg;
            }
        }
    }

    /**
     * 获取Active的ResourceManager with 重试
     *
     * @param clusterId
     * @return
     */
    private ResultMsg getActiveResourceManagerWithRetry(String clusterId){
        ResultMsg activeRM = new ResultMsg();
        Integer i = 0;
        while (true){
            activeRM = getActiveResourceManager(clusterId);
            if (activeRM.getResult()){
                return activeRM;
            }
            i++;
            logger.warn("getActiveResourceManager,重试:{}",i);
            if (i > sdpAmbariRetryCount){
                return activeRM;
            }
            ThreadUtil.sleep(sdpAmbariRetryDuration);
        }
    }

    private boolean isActiveRM(InfoClusterVm x,Integer port) {
        try {
            String url = String.format("http://%s:%s/ws/v1/cluster/info", x.getHostName(),port);
            String resstr = HttpUtil.get(url);
            logger.info("Response RM ws cluster info："+resstr);
            com.alibaba.fastjson.JSONObject jsonObject=JSON.parseObject(resstr);
            if (jsonObject!=null && jsonObject.containsKey("clusterInfo")){
                com.alibaba.fastjson.JSONObject clusterInfo=jsonObject.getJSONObject("clusterInfo");
                if (clusterInfo.containsKey("haState")
                        && clusterInfo.getString("haState").equalsIgnoreCase("active")){
                    return true;
                }
            }
        }catch (Exception e){
            logger.error("get isActiveRM Error:",e);
        }
        return false;
    }

    /**
     * 获取集群Active NameNode的HostName
     *
     * @param clusterId 集群ID
     * @return
     */
    @Override
    public ResultMsg getActiveNameNode(String clusterId) {
        ResultMsg msg=new ResultMsg();
        ConfCluster confCluster=confClusterMapper.selectByPrimaryKey(clusterId);
        if (confCluster.getIsHa().equals(1)) {
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndRole(clusterId, "master");
            Integer i = 0;
            while (true) {
                vms.stream().forEach(x -> {
                    boolean isconn = isHostConnectable(x.getHostName(), hadoopNameNodeJmxApiPort);
                    if (isconn && isActiveNameNode(x, hadoopNameNodeJmxApiPort)) {
                        msg.setResult(true);
                        msg.setData(x.getHostName());
                        return;
                    }
                });
                if (msg.getResult()){
                    break;
                }else{
                    i++;
                    logger.warn("获取集群Active NameNode的HostName,重试",i);
                    if (i > sdpAmbariRetryCount){
                        break;
                    }
                    ThreadUtil.sleep(1000 * sdpAmbariRetryDuration);
                }
            }
        }else{
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndRole(clusterId,"ambari");
            if (vms!=null && vms.size()>0){
                msg.setResult(true);
                msg.setData(vms.get(0).getHostName());
                return msg;
            }else{
                msg.setResult(false);
                return msg;
            }
        }
        return msg;
    }

    /**
     * 从Yarn中获取集群的运行中的主机
     * @param clusterId 集群ID
     * @return
     */
    public List<String> getRunningHostsFromYarn(String clusterId) {
        List<String> runningHosts = new ArrayList<>();
        try {
            ResultMsg  activeNameNodeMsg = getActiveResourceManagerWithRetry(clusterId);
            if (activeNameNodeMsg.getResult()){
                String activeNameNode = activeNameNodeMsg.getData().toString();
                Map<String,String> vmstates = getNodesFromYarn(activeNameNode,hadoopJmxApiPort, Arrays.asList("RUNNING"));
                for (Map.Entry<String, String> entry : vmstates.entrySet()) {
                    runningHosts.add(entry.getKey());
                }
            } else {
                logger.error("从Yarn获取运行中的节点时，没有找到ResourceManager节点。");
            }
        }catch (Exception e){
            logger.error("从Yarn获取运行中的节点失败：[clusterId=" + clusterId + "] " + e.getMessage(), e);
            throw new RuntimeException("从Yarn获取运行中的节点失败：[clusterId=" + clusterId + "] " + e.getMessage(), e);
        }
        return runningHosts;
    }

    /**
     * 判断是否Active NameNode
     *
     * @param x
     * @param port
     * @return
     */
    private boolean isActiveNameNode(InfoClusterVm x,Integer port){
        try {
            String url = String.format("http://%s:%s/jmx?qry=Hadoop:service=NameNode,name=FSNamesystem", x.getHostName(),port);
            String resstr = HttpUtil.get(url);
            logger.info("Response NN ws cluster info："+resstr);
            com.alibaba.fastjson.JSONObject jsonObject=JSON.parseObject(resstr);
            if (jsonObject!=null && jsonObject.containsKey("beans")){
                com.alibaba.fastjson.JSONArray nodes=jsonObject.getJSONArray("beans");
                if (nodes != null
                        && nodes.size()>0
                        && nodes.getJSONObject(0).containsKey("tag.HAState")
                        && nodes.getJSONObject(0).getString("tag.HAState").equalsIgnoreCase("active")){
                    return true;
                }
            }
        }catch (Exception e){
            logger.error("get isActiveNN Error:",e);
        }
        return false;
    }

    private boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 3000);
            return true;
        } catch (IOException e) {
            logger.error("连接："+host+",失败",e);
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
               logger.error("关闭连接异常",e);
            }
        }
    }

    /**
     * 获取master节点的hostname
     * HA模式 master 随机返回一台
     * 非HA模式 返回ambari
     *
     * @param clusterId
     * @return
     */
    private String getMasterHostName(String clusterId) {
        logger.info("clusterId,{}", clusterId);
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

        if (confCluster.getIsHa().equals(1)) {
            List<InfoClusterVm> masters = vmMapper.selectByClusterIdAndRole(clusterId, "master");
            if (masters != null && masters.size() > 0) {
                return masters.get(0).getHostName();
            } else {
                return null;
            }
        } else {
            List<InfoClusterVm> ambari = vmMapper.selectByClusterIdAndRole(clusterId, "ambari");
            if (ambari != null && ambari.size() > 0) {
                return ambari.get(0).getHostName();
            } else {
                return null;
            }
        }

    }

    void handleSparkConfig(List<String> services, Map<String, Map<String, Object>> configs) {
        String serviceName = "spark";
        Integer version = getServiceVersionFromServiceNames(serviceName, services);
        if (Objects.isNull(version)) {
            // 如果没找到Spark的版本号，默认是Spark3
            version = 3;
        }

        if (Objects.isNull(configs)) {
            return;
        }

        // 找到spark开头的配置
        List<String> configTypeList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : configs.entrySet()) {
            String configType = entry.getKey();
            if (configType.startsWith(serviceName) && !configType.startsWith(serviceName + version)) {
                configTypeList.add(configType);
            }
        }

        // 替换掉Spark开头的配置
        for (String configType : configTypeList) {
            Map<String, Object> config = configs.get(configType);
            configs.remove(configType);
            configType = configType.replaceAll(serviceName, serviceName + version);
            configs.put(configType, config);
        }

    }

    /**
     * 根据服务名，查找真实使用的带版本号的服务名
     *
     * @param servicePrefix 服务名前缀， 如：Spark
     * @param services      所有服务列表
     * @return 带版本号的服务名，如：Spark3
     */
    Integer getServiceVersionFromServiceNames(String servicePrefix, List<String> services) {
        servicePrefix = Strings.toUpperCase(servicePrefix);
        for (String service : services) {
            service = Strings.toUpperCase(service);
            if (service.startsWith(servicePrefix)) {
                String versionStr = service.replaceAll(servicePrefix, "");
                if (StringUtils.isEmpty(versionStr)) {
                    return null;
                } else {
                    try {
                        return Integer.parseInt(versionStr);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        return null;
                    }
                }
            }
        }

        return null;
    }

    Integer getServiceVersion(String serviceName, String releaserVer, String scene) {
        BaseScene baseScene = sceneMapper.queryByReleaseVerAndSceneName(releaserVer, scene);
        if (Objects.isNull(baseScene)) {
            return null;
        }

        String upperServiceName = Strings.toUpperCase(serviceName);
        List<BaseSceneApps> apps = sceneAppsMapper.queryBySceneId(baseScene.getSceneId());
        for (BaseSceneApps app : apps) {
            String appServiceName = Strings.toUpperCase(app.getAppName());
            if (Objects.equals(appServiceName, upperServiceName)) {
                double version = Double.parseDouble(app.getAppVersion());
                Double bigVersion = Math.floor(version);
                return bigVersion.intValue();
            }
        }
        return null;
    }


    private ClusterTemplateHostGroup findClusterTemplateHostGroupByName(ClusterTemplate clusterTemplate, String name) {
        for (ClusterTemplateHostGroup hostGroup : clusterTemplate.getHostGroups()) {
            if (Objects.equals(hostGroup.getName(), name)) {
                return hostGroup;
            }
        }
        return null;
    }

    private void setAmbariDebugState(ApiClient client) {
        if (Objects.isNull(client)) {
            return;
        }
        client.setDebugging(Objects.equals(this.debug, "1"));
    }


    public InProgressResult restartService(AmbariInfo ambariInfo, String clusterName, String serviceName) {
        // 检查参数
        Optional<BDService> service = BDService.parse(serviceName);
        Assert.isTrue(service.isPresent(), "重启服务时，服务不合法：" + serviceName);
        Assert.notNull(clusterName, "重启服务时，集群名不能为空");
        Assert.notNull(ambariInfo, "重启服务时，Ambari信息不能为空");

        try {
            // 根据Service获取对应的组件
            CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
            setAmbariDebugState(api.getApiClient());
            QueryServiceInHostsResponse queryServiceInHostsResponse = api.queryServiceInHosts(clusterName, serviceName);
            List<RequestResourceFilter> resourceFilters = extractResourceFilterFromHostsComponent(serviceName, queryServiceInHostsResponse.getItems());

            // 重启
            InProgressResponse inProgressResponse = api.restartService(clusterName, serviceName, resourceFilters);
            InProgressResult result = new InProgressResult();
            result.setSuccess(true);
            result.setRequestId(inProgressResponse.getRequestId().longValue());
            result.setClusterName(clusterName);
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            InProgressResult result = new InProgressResult();
            result.setSuccess(false);
            result.setClusterName(clusterName);
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    public InProgressResult restartHostsComponents(AmbariInfo ambariInfo, String clusterId, String serviceName, String groupName) {
        // 检查参数
        Optional<BDService> service = BDService.parse(serviceName);
        Assert.isTrue(service.isPresent(), "重启服务时，服务不合法：" + serviceName);
        Assert.notNull(clusterId, "重启服务时，集群Id不能为空");
        Assert.notNull(ambariInfo, "重启服务时，Ambari信息不能为空");

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

        // 根据Service获取对应的组件
        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());

        // 根据GroupName来重启时，对Ambari和Master作特殊处理
        // Ambari：高可用时， 直接查Ambari，非高可用时， 查MASTER1
        // MASTER：高可用时， 查MASTER1 和 MASTER2,非高可用时，查MASTER1
        // 其它：正常查

        String tmpGroupName = Strings.toUpperCase(groupName);
        String finalGroupName = "";
        if (Objects.equals(tmpGroupName, "AMBARI")) {
            if (confCluster.getIsHa() == 0) {
                finalGroupName = "master";
            } else {
                finalGroupName = "ambari";
            }
        } else if (Objects.equals(tmpGroupName, "MASTER")) {
            finalGroupName = "master";
        } else {
            finalGroupName = groupName;
        }

        try {
            // 查询主机
            List<InfoClusterVm> vms = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(clusterId, finalGroupName, InfoClusterVm.VM_RUNNING);
            if (Objects.equals(finalGroupName, "master")) {
                List<InfoClusterVm> ambariVm = infoClusterVmMapper.selectByClusterIdAndRoleAndState(clusterId, "ambari", InfoClusterVm.VM_RUNNING);
                vms.addAll(ambariVm);
            }
            List<String> hosts = vms.stream().map(InfoClusterVm::getHostName).collect(Collectors.toList());

            //

            String vmRole = null;
            if (CollectionUtil.isNotEmpty(vms)) {
                vmRole = Strings.toUpperCase(vms.get(0).getVmRole());
            }

            // 这个组件的格式为：主机名+ ":" + 组件名，如  sdp-wewt1kombqp-tsk-0009.dev.sdp.com:NODEMANAGER
            Set<String> components = null;

            // 从Ambari查询
            if (Objects.equals(finalGroupName, "ambari") || Objects.equals(finalGroupName, "master")) {
                components = queryHostComponentFromAmbari(api, confCluster.getAmbariClusterName(), hosts, serviceName, confCluster.getIsHa() == 1);
            } else {
                components = queryHostComponentFromAmbariByOneHost(api, confCluster.getAmbariClusterName(), hosts, serviceName, confCluster.getIsHa() == 1);
            }

            // 下面是查组件
            // 第一步，先查询这个实例组中某个主机的所有组件。CustomActionApi.queryHostsComponents() 先不使用这步了。
            // 第二步，如果主机中没有返回组件，则从快照中查询。info_cluster_component_layout
            // 第三步：如果快照中没有查询到，从布局表中查。ambari_component_layout
//            if (CollectionUtil.isEmpty(components)) {
//                if (Objects.equals(finalGroupName, "ambari") || Objects.equals(finalGroupName, "master")) {
//                    components = queryHostComponentFromAmbari(api, confCluster.getAmbariClusterName(), hosts, serviceName, confCluster.getIsHa() == 1);
//                } else {
//                    List<InfoClusterComponentLayout> componentLayouts = infoClusterComponentLayoutMapper.getComponentsByClusterIdAndHostGroup(clusterId, finalGroupName);
//                    for (InfoClusterComponentLayout componentLayout : componentLayouts) {
//
//                    }
//                    components = componentLayouts.stream().map(InfoClusterComponentLayout::getComponentCode).collect(Collectors.toList());
//                }
//            }

            // 最后从布局模板表中查询组件信息
//            if (CollectionUtil.isEmpty(components)) {
//                List<AmbariComponentLayout> ambariComponentLayouts = ambariComponentLayoutMapper.queryByHostGroupAndServiceCode(vmRole, Arrays.asList(serviceName), confCluster.getIsHa());
//                components = ambariComponentLayouts.stream().map(AmbariComponentLayout::getComponentCode).collect(Collectors.toList());
//            }

            // 重启
            InProgressResponse inProgressResponse = api.restartHostsComponents(confCluster.getAmbariClusterName(), serviceName, components);

            InProgressResult result = new InProgressResult();
            result.setSuccess(true);
            result.setRequestId(inProgressResponse.getRequestId().longValue());
            result.setClusterName(confCluster.getClusterName());
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            InProgressResult result = new InProgressResult();
            result.setSuccess(false);
            result.setClusterName(confCluster.getClusterName());
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    Set<String> queryHostComponentFromAmbari(CustomActionApi api, String clusterName, List<String> hosts, String serviceName, boolean isHa) {
        Set<String> result = new HashSet<>();
        Optional<BDService> service = BDService.parse(serviceName);
        if (!service.isPresent()) {
            logger.error("从Ambari查询主机的组件时，Service解析失败：serviceName={}", serviceName);
            return result;
        }
        List<BDComponent> components = service.get().getComponents(isHa ? HAScene.HA : HAScene.NON_HA);
        Set<String> componentSet = components.stream().map(BDComponent::name).collect(Collectors.toSet());

        QueryHostsComponentResponse response = api.queryHostsComponents(clusterName, hosts);
        if (Objects.isNull(response)) {
            logger.error("从Ambari查询主机的组件返回为空：clusterName={}, hosts={}", clusterName, hosts);
            return result;
        }

        for (String host : hosts) {
            List<String> componentNames = response.getComponentNames(host);
            for (String componentName : componentNames) {
                if (componentSet.contains(componentName)) {
                    result.add(host + ":" + componentName);
                }
            }
        }

        return result;
    }

    Set<String> queryHostComponentFromAmbariByOneHost(CustomActionApi api, String clusterName, List<String> hosts, String serviceName, boolean isHa) {
        Set<String> result = new HashSet<>();
        Optional<BDService> service = BDService.parse(serviceName);
        if (!service.isPresent()) {
            logger.error("从Ambari查询主机的组件时，Service解析失败：serviceName={}", serviceName);
            return result;
        }
        List<BDComponent> components = service.get().getComponents(isHa ? HAScene.HA : HAScene.NON_HA);
        Set<String> componentSet = components.stream().map(BDComponent::name).collect(Collectors.toSet());

        String host = hosts.get(0);
        QueryHostsComponentResponse response = api.queryHostsComponents(clusterName, Arrays.asList(host));
        if (Objects.isNull(response)) {
            logger.error("从Ambari查询主机的组件返回为空：clusterName={}, hosts={}", clusterName, host);
            return result;
        }

        List<String> componentNames = response.getComponentNames(host);
        for (String componentName : componentNames) {
            if (componentSet.contains(componentName)) {
                for (String s : hosts) {
                    result.add(s + ":" + componentName);
                }
            }
        }

        return result;
    }

    @Override
    public ResultMsg getNodesWithContainerRunning(String clusterId) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setRetcode(ResultMsg.DEFUALT);
        try {
            ResultMsg activeRMMsg = getActiveResourceManagerWithRetry(clusterId);
            if (!activeRMMsg.getResult()){
                logger.error("获取activeRM失败。");
                return activeRMMsg;
            }
            String activeRMHostName = activeRMMsg.getData().toString();
            logger.info("activeRMHostName:{}",activeRMHostName);

            String queryUrl = StrUtil.format(
                    "http://{}:{}/ws/v1/cluster/nodes?_={}&states=RUNNING,DECOMMISSIONING",
                    activeRMHostName,
                    hadoopJmxApiPort,
                    System.currentTimeMillis());

            com.alibaba.fastjson.JSONObject qryNodeInfos = queryNodeInfosFromRMJMXWithRetry(queryUrl);

            if (!qryNodeInfos.containsKey("nodes")){
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("获取节点信息失败。");
                return resultMsg;
            }

            com.alibaba.fastjson.JSONArray nodes = qryNodeInfos.getJSONObject("nodes").getJSONArray("node");
            List<String> nodesWithContainer = new ArrayList<>();

            for (int i=0;i<nodes.size();i++){
                com.alibaba.fastjson.JSONObject item = nodes.getJSONObject(i);
                if (item !=null && item.containsKey("nodeHostName") && item.containsKey("numContainers")){
                    if (item.getInteger("numContainers")>0){
                        nodesWithContainer.add(item.getString("nodeHostName").trim());
                    }
                }
            }
            resultMsg.setResult(true);
            resultMsg.setRows(nodesWithContainer);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            resultMsg.setResult(false);
            resultMsg.setRetcode(ResultMsg.ERROR);
        }
        return resultMsg;
    }

    /**
     * 查询RM JMX接口获取Nodes信息 with 重试
     *
     * @param queryUrl
     * @return
     * 返回格式 参考 ：
     * http://activeRM:8088/ws/v1/cluster/nodes?_=
     */
    private com.alibaba.fastjson.JSONObject queryNodeInfosFromRMJMXWithRetry(String queryUrl){
        logger.info("查询RMJMX接口获取Nodes信息,url:{}",queryUrl);
        Integer i = 0;
        while (true) {
            try {
                String strBeans = HttpUtil.get(queryUrl);
                if (StringUtils.isNotEmpty(strBeans)) {
                    com.alibaba.fastjson.JSONObject jsonBeans = JSON.parseObject(strBeans);
                    if (jsonBeans != null) {
                        return jsonBeans;
                    }
                }
                logger.error("查询RMJMX接口获取Nodes信息失败，strBeans:{}",strBeans);
            }catch (Exception e){
                logger.error("查询RMJMX接口获取Nodes信息异常，",e);
            }
            i++;
            logger.warn("查询RMJMX接口获取Nodes信息失败,重试：{}",i);
            if (i > sdpAmbariRetryCount){
                return null;
            }
            ThreadUtil.sleep(1000 * sdpAmbariRetryDuration);
        }
    }

    @Override
    public void updateClusterConfig(AmbariInfo ambariInfo, String clusterId, List<InstanceGroupConfiguration> groupConfigs,
                                    List<BlueprintConfiguration> clusterDefaultConfigs) {
        Assert.notNull(ambariInfo, "更新集群配置时，AmbariInfo不能为空");
        Assert.notNull(clusterId, "更新集群配置时，集群ID不能为空");

        CustomActionApi api = new CustomActionApi(ambariInfo.getAmbariApiClient());
        setAmbariDebugState(api.getApiClient());

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

        // 修改集群配置配置
        logger.info("updateClusterConfig-clusterId:{},clusterDefaultConfigs:{}", clusterId, JSONUtil.toJsonStr(clusterDefaultConfigs));
        if (CollectionUtil.isNotEmpty(clusterDefaultConfigs)) {
            handleUpdateClusterDefaultConfig(api, confCluster, clusterDefaultConfigs);
        }

        // 修改配置组配置
        logger.info("updateClusterConfig-clusterId:{},clusterDefaultConfigs:{}", clusterId, JSONUtil.toJsonStr(groupConfigs));
        if (CollectionUtil.isNotEmpty(groupConfigs)) {
            handleUpdateHostGroupConfig(api, confCluster, groupConfigs);
        }
    }

    /**
     * 处理实例组的配置变更
     *
     * @param api
     * @param confCluster
     * @param groupConfigs
     */
    private void handleUpdateHostGroupConfig(CustomActionApi api, ConfCluster confCluster, List<InstanceGroupConfiguration> groupConfigs) {
        logger.info("实例组信息:clusterId={},groupConfigs={}", confCluster.getClusterId(), JSONUtil.toJsonStr(groupConfigs));
        for (InstanceGroupConfiguration instanceGroupConfig : groupConfigs) {
            // 步骤一：查询出实例组的所有配置组信息：1.配置组列表  2.实例组所有的运行中的主机  3.配置信息

            // 查询出所有的配置组
            List<InfoAmbariConfigGroup> existInfoAmbariConfigGroups = ambariConfigGroupMapper.selectByGroupIdAndStates(instanceGroupConfig.getGroupId(),
                    Arrays.asList(InfoAmbariConfigGroup.STATE_RUNNING, InfoAmbariConfigGroup.STATE_SCALEIN, InfoAmbariConfigGroup.STATE_SCALEOUT));

            Map<BDService, List<BlueprintConfiguration>> createdConfigGroup = new HashMap<>();
            Map<BDService, List<BlueprintConfiguration>> uncreatedConfigGroup = new HashMap<>();

            // 将配置信息按是否已经在进行拆分，得到两个分组：已经存在， 未存在。为后续分别处理作准备
            splitConfigGroupByExisted(instanceGroupConfig, existInfoAmbariConfigGroups, createdConfigGroup, uncreatedConfigGroup);


            // 查询出集群所有的配置组
            ConfClusterHostGroup existHostGroup = hostGroupMapper.selectByPrimaryKey(instanceGroupConfig.getGroupId());

            // 实例组中的虚拟机
            List<InfoClusterVm> existVMs = infoClusterVmMapper.selectByClusterIdAndGroupNameAndState(confCluster.getClusterId(),
                    existHostGroup.getGroupName(), InfoClusterVm.VM_RUNNING);
            List<HostRole> existVmRoles = existVMs.stream().map(vm -> {
                HostRole role = new HostRole();
                role.setHostName(vm.getHostName());
                return role;
            }).collect(Collectors.toList());

            // 步骤三：更新已经存在的配置组，新增不存在的配置组
            // 处理未创建的配置组
            handleUncratedConfigGroup(api, confCluster, uncreatedConfigGroup, existHostGroup, existVmRoles);

            // 处理已创建的配置组
            handleCreatedConfigGroup(api, confCluster, existInfoAmbariConfigGroups, createdConfigGroup, existVmRoles);
        }

        // 保存配置
        saveClusterConfigGroupConfig(confCluster.getClusterId(), groupConfigs);
    }

    private void handleCreatedConfigGroup(CustomActionApi api, ConfCluster confCluster, List<InfoAmbariConfigGroup> existInfoAmbariConfigGroups,
                                          Map<BDService, List<BlueprintConfiguration>> createdConfigGroup, List<HostRole> existVmRoles) {
        for (Map.Entry<BDService, List<BlueprintConfiguration>> entry : createdConfigGroup.entrySet()) {
            // 更新
            // 生成配置组对象
            List<Map<String, Object>> desiredConfigs = new ArrayList<>();
            for (BlueprintConfiguration blueprintConfiguration : entry.getValue()) {
                Map<String, Object> desiredConfig = new HashMap<>();
                desiredConfig.put("type", blueprintConfiguration.getConfigItemName());
                desiredConfig.put("properties", blueprintConfiguration.getProperties());
                desiredConfigs.add(desiredConfig);
            }

            InfoAmbariConfigGroup configGroup = selectExistAmbariConfigGroup(existInfoAmbariConfigGroups, entry.getKey());
            ConfigGroup newConfigGroup = new ConfigGroup();
            newConfigGroup.setServiceName(configGroup.getAmbariServiceName());
            newConfigGroup.setTag(configGroup.getAmbariTag());
            newConfigGroup.setId(configGroup.getAmbariId());
            newConfigGroup.setGroupName(configGroup.getAmbariGroupName());
            newConfigGroup.setClusterName(configGroup.getAmbariClusterName());
            newConfigGroup.setHosts(existVmRoles);
            newConfigGroup.setDesiredConfigs(desiredConfigs);

            // 调用API更新配置
            api.updateConfigGroup(confCluster.getAmbariClusterName(), newConfigGroup);
        }
    }

    private void handleUncratedConfigGroup(CustomActionApi api, ConfCluster confCluster, Map<BDService, List<BlueprintConfiguration>> uncreatedConfigGroup,
                                           ConfClusterHostGroup existHostGroup, List<HostRole> existVmRoles) {
        for (Map.Entry<BDService, List<BlueprintConfiguration>> entry : uncreatedConfigGroup.entrySet()) {
            // 新增
            // 生成ConfigGroup
            List<Map<String, Object>> desiredConfigs = new ArrayList<>();
            for (BlueprintConfiguration blueprintConfiguration : entry.getValue()) {
                Map<String, Object> desiredConfig = new HashMap<>();
                desiredConfig.put("type", blueprintConfiguration.getConfigItemName());
                desiredConfig.put("properties", blueprintConfiguration.getProperties());
                desiredConfigs.add(desiredConfig);
            }

            ConfigGroup newConfigGroup = new ConfigGroup();
            String groupName = confCluster.getClusterName() + "_" + entry.getKey().name() + "_" + existHostGroup.getGroupName();
            newConfigGroup.setServiceName(entry.getKey().name());
            newConfigGroup.setTag(entry.getKey().name());
            newConfigGroup.setGroupName(groupName);
            newConfigGroup.setClusterName(confCluster.getAmbariClusterName());
            newConfigGroup.setHosts(existVmRoles);
            newConfigGroup.setDesiredConfigs(desiredConfigs);

            // 调用 API新增配置组
            CreateConfigGroupResponse createResponse = api.createConfigGroup(confCluster.getAmbariClusterName(), Arrays.asList(newConfigGroup));
            if (Objects.isNull(createResponse.getSingleId())) {
                throw new RuntimeException("新增配置组失败：" + JSON.toJSONString(newConfigGroup));
            }
            // 保存Ambari配置组信息至数据库表
            InfoAmbariConfigGroup ambariConfigGroup = new InfoAmbariConfigGroup();
            ambariConfigGroup.setClusterId(confCluster.getClusterId());
            ambariConfigGroup.setGroupId(existHostGroup.getGroupId());
            ambariConfigGroup.setAmbariId(createResponse.getSingleId().longValue());
            ambariConfigGroup.setAmbariServiceName(newConfigGroup.getServiceName());
            ambariConfigGroup.setAmbariGroupName(newConfigGroup.getGroupName());
            ambariConfigGroup.setAmbariTag(newConfigGroup.getTag());
            ambariConfigGroup.setAmbariClusterName(newConfigGroup.getClusterName());
            ambariConfigGroup.setAmbariDescription(newConfigGroup.getDescription());
            ambariConfigGroup.setState(InfoAmbariConfigGroup.STATE_RUNNING);
            ambariConfigGroup.setCreatedTime(new Date());
            ambariConfigGroup.setConfId(UUID.randomUUID().toString());
            ambariConfigGroupMapper.insert(ambariConfigGroup);
        }
    }

    private void splitConfigGroupByExisted(InstanceGroupConfiguration instanceGroupConfig, List<InfoAmbariConfigGroup> existInfoAmbariConfigGroups, Map<BDService, List<BlueprintConfiguration>> createdConfigGroup, Map<BDService, List<BlueprintConfiguration>> uncreatedConfigGroup) {
        // 查出来现在实例组的所有配置（根据配置组Id查）
        List<ConfClusterHostGroupAppsConfig> existConfigList = hostGroupConfigMapper.selectByGroupId(instanceGroupConfig.getGroupId());

        // 将之前已经配置好的配置项补齐到待修改配置中，一起进行修改
        for (ConfClusterHostGroupAppsConfig existConfig : existConfigList) {
            BlueprintConfiguration newConfig = instanceGroupConfig.getByConfigItem(existConfig.getAppConfigClassification());
            if (Objects.isNull(newConfig)) {
                continue;
            }
            Object newVal = newConfig.getProperty(existConfig.getConfigItem());
            if (Objects.isNull(newVal)) {
                newConfig.putProperties(existConfig.getConfigItem(), existConfig.getConfigVal());
            }
        }

        // 步骤二：将配置信息按配置组进行分组，要区分出已经存的配置组和不存在的配置组
        if (CollUtil.isNotEmpty(instanceGroupConfig.getGroupCfgs())) {
            Map<BDService, List<BlueprintConfiguration>> newGroupedConfigMap = instanceGroupConfig.getGroupCfgs().stream().collect(Collectors.groupingBy(cfg -> {
                return ConfigClassification.parseToService(cfg.getConfigItemName());
            }));


            for (Map.Entry<BDService, List<BlueprintConfiguration>> serviceEntry : newGroupedConfigMap.entrySet()) {
                BDService service = serviceEntry.getKey();
                for (InfoAmbariConfigGroup existAmbariConfigGroup : existInfoAmbariConfigGroups) {
                    Optional<BDService> existService = BDService.parse(existAmbariConfigGroup.getAmbariServiceName());
                    if (Objects.equals(service, existService.get())) {
                        createdConfigGroup.put(service, serviceEntry.getValue());
                    }
                }
                if (!createdConfigGroup.containsKey(service)) {
                    uncreatedConfigGroup.put(service, serviceEntry.getValue());
                }
            }
        }
    }

    /**
     * 处理集群默认配置的变更
     *
     * @param api
     * @param confCluster
     * @param clusterDefaultConfigs
     */
    private void handleUpdateClusterDefaultConfig(CustomActionApi api, ConfCluster confCluster, List<BlueprintConfiguration> clusterDefaultConfigs) {
        logger.info("处理集群默认配置的变更-开始:{}", JSONUtil.toJsonStr(clusterDefaultConfigs));
        String clusterName = confCluster.getAmbariClusterName();
        // 按服务进行分组
        Map<BDService, List<BlueprintConfiguration>> groupedClusterDefaultConfigs = clusterDefaultConfigs.stream().collect(Collectors.groupingBy(cfg -> {
            return ConfigClassification.parseToService(cfg.getConfigItemName());
        }));

        List<String> serviceList = groupedClusterDefaultConfigs.keySet().stream().map(BDService::name).collect(Collectors.toList());

        // 查询集群中现有的所有配置
        DefaultClusterConfigResponse resp = api.queryDefaultClusterConfig(clusterName, serviceList);

        // 遍历本次需要修改的配置进行替换
        List<DefaultClusterConfigWrapper> clusterServiceConfigs = new ArrayList<>();
        for (List<BlueprintConfiguration> configs : groupedClusterDefaultConfigs.values()) {
            DefaultClusterConfigWrapper newDefaultConfig = new DefaultClusterConfigWrapper();
            for (BlueprintConfiguration newConfig : configs) {
                DefaultConfigDesiredConfig oldConfig = resp.findConfigByConfigType(newConfig.getConfigItemName());
                if (Objects.isNull(oldConfig)) {
                    logger.info("更新集群默认配置时，在现有的集群中不存在配置类型的数据：{}", newConfig.getConfigItemName());
                    continue;
                }
                // 将旧配置中存在而新配置中不存在的配置项，移动到新配置中。
                for (Map.Entry<String, Object> newConfigItem : newConfig.getProperties().entrySet()) {
                    oldConfig.putProperty(newConfigItem.getKey(), String.valueOf(newConfigItem.getValue()));
                }
                newDefaultConfig.addConfig(oldConfig);
            }
            clusterServiceConfigs.add(newDefaultConfig);
        }

        // 对新配置按Service分组， 每次更新时只能更新一个Service的配置。
        UpdateClusterDefaultConfigResponse response = api.updateClusterDefaultConfig(clusterName, clusterServiceConfigs);

        // 保存配置
        saveClusterDefaultConfig(confCluster.getClusterId(), clusterDefaultConfigs);
        logger.info("处理集群默认配置的变更-完成:{}", JSONUtil.toJsonStr(clusterDefaultConfigs));
    }

    private void saveClusterDefaultConfig(String clusterId, List<BlueprintConfiguration> clusterDefaultConfigs) {
        // 根据 clusterId  configClassification  configItem 查询配置是否存在
        List<ConfClusterAppsConfig> configs = clusterConfigMapper.getAppConfigsByConfigId(clusterId);
        Map<String, ConfClusterAppsConfig> configsMap = configs.stream()
                .collect(Collectors.toMap(key -> {
                    return key.getAppConfigClassification() + key.getConfigItem();
                }, Function.identity()));
        for (BlueprintConfiguration newConfig : clusterDefaultConfigs) {
            String configClass = newConfig.getConfigItemName();
            for (Map.Entry<String, Object> entry : newConfig.getProperties().entrySet()) {
                String key = configClass + entry.getKey();
                ConfClusterAppsConfig config = configsMap.get(key);
                if (Objects.isNull(config)) {
                    // 没找到，新增
                    ConfClusterAppsConfig cfg = new ConfClusterAppsConfig();
                    cfg.setAppConfigItemId(UUID.randomUUID().toString());
                    cfg.setClusterId(clusterId);
                    BDService bdService = ConfigClassification.parseToService(configClass);
                    if (Objects.nonNull(bdService)) {
                        cfg.setAppName(bdService.name());
                    }
                    cfg.setAppConfigClassification(configClass);
                    cfg.setConfigItem(entry.getKey());
                    cfg.setConfigVal(String.valueOf(entry.getValue()));
                    cfg.setIsDelete(0);
                    cfg.setCreatedby("system");
                    cfg.setCreatedTime(new Date());
                    clusterConfigMapper.insert(cfg);
                } else {
                    // 找到，更新
                    config.setConfigVal(String.valueOf(entry.getValue()));
                    clusterConfigMapper.updateByPrimaryKeySelective(config);
                }
            }

        }
    }

    private void saveClusterConfigGroupConfig(String clusterId, List<InstanceGroupConfiguration> groupConfigs) {
        for (InstanceGroupConfiguration groupConfig : groupConfigs) {
            // 处理一个实例组的配置，一个实例组下面会有多个配置文件（配置类型）
            List<ConfClusterHostGroupAppsConfig> confClusterHostGroupAppsConfigs = hostGroupConfigMapper.selectByGroupId(groupConfig.getGroupId());
            Map<String, ConfClusterHostGroupAppsConfig> configsMap = confClusterHostGroupAppsConfigs.stream().collect(Collectors.toMap(cfg -> {
                return cfg.getAppConfigClassification() + cfg.getConfigItem();
            }, Function.identity()));

            if (CollUtil.isEmpty(groupConfig.getGroupCfgs())) {
                logger.info("无配置信息不生成ConfClusterHostGroupAppsConfig");
                return;
            }
            // 循环处理各个配置文件
            for (BlueprintConfiguration groupCfg : groupConfig.getGroupCfgs()) {

                // 循环处理配置文件下面的各个配置项
                Map<String, Object> properties = groupCfg.getProperties();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String key = groupCfg.getConfigItemName() + entry.getKey();
                    ConfClusterHostGroupAppsConfig cfg = configsMap.get(key);
                    if (Objects.isNull(cfg)) {
                        // 之前没配置过该配置项，新增
                        ConfClusterHostGroupAppsConfig newCfg = new ConfClusterHostGroupAppsConfig();
                        newCfg.setAppConfigItemId(UUID.randomUUID().toString());
                        newCfg.setClusterId(clusterId);
                        newCfg.setGroupId(groupConfig.getGroupId());
                        newCfg.setAppConfigClassification(groupCfg.getConfigItemName());
                        BDService bdService = ConfigClassification.parseToService(groupCfg.getConfigItemName());
                        if (Objects.nonNull(bdService)) {
                            newCfg.setAppName(bdService.name());
                        }

                        newCfg.setConfigItem(entry.getKey());
                        newCfg.setConfigVal(String.valueOf(entry.getValue()));
                        newCfg.setIsDelete(0);
                        newCfg.setCreatedTime(new Date());
                        newCfg.setCreatedby("system");

                        hostGroupConfigMapper.insert(newCfg);
                    } else {
                        // 配置过，更新
                        cfg.setConfigVal(String.valueOf(entry.getValue()));
                        hostGroupConfigMapper.updateByPrimaryKeySelective(cfg);
                    }
                }
            }
        }
    }

    private InfoAmbariConfigGroup selectExistAmbariConfigGroup(List<InfoAmbariConfigGroup> existInfoAmbariConfigGroups, BDService service) {
        for (InfoAmbariConfigGroup configGroup : existInfoAmbariConfigGroups) {
            Optional<BDService> tmpService = BDService.parse(configGroup.getAmbariServiceName());
            if (tmpService.isPresent()) {
                if (Objects.equals(service, tmpService.get())) {
                    return configGroup;
                }
            }
            ;
        }
        return null;
    }

    private List<RequestResourceFilter> extractResourceFilterFromHostsComponent(String serviceName, List<ServiceInHost> hostList) {
        Assert.notNull(hostList, "主机组件清单不能为空");
        Map<String, RequestResourceFilter> filterMap = new HashMap<>();

        // 循环检查所有的主机
        for (ServiceInHost host : hostList) {
            String hostName = host.getHost().getHostName();


            // 循环检查一个主机中的所有组件
            for (HostComponent hostComponent : host.getHostComponents()) {
                HostRole hostRole = hostComponent.getHostRole();
                if (Objects.isNull(hostRole)) {
                    continue;
                }
                RequestResourceFilter filter = filterMap.get(hostRole.getComponentName());
                if (Objects.isNull(filter)) {
                    filter = new RequestResourceFilter();
                    filter.setHosts(hostName);
                    filter.setServiceName(serviceName);
                    filter.setComponentName(hostRole.getComponentName());
                    filterMap.put(hostRole.getComponentName(), filter);
                } else {
                    filter.setHosts(filter.getHosts() + "," + hostName);
                }
            }
        }

        List<RequestResourceFilter> filterList = new ArrayList<>();
        for (Map.Entry<String, RequestResourceFilter> entry : filterMap.entrySet()) {
            filterList.add(entry.getValue());
        }

        return filterList;
    }

    /**
     * 查询NameNode JMX接口查询指定VM且DataNode状态为InService的VM列表
     * 非InService的datanode，decommission请求ambari不被允许
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public List<String> getHostsWithInServiceDataNode(String clusterId, List<String> hostNames) {
        List<String> inServiceHostNames = new ArrayList<>();
        try {
            ResultMsg  activeNameNodeMsg = getActiveNameNode(clusterId);
            if (activeNameNodeMsg.getResult()){
                String activeNameNode = activeNameNodeMsg.getData().toString();
                Map<String,String> vmstates = getDataNodeState(activeNameNode,hadoopNameNodeJmxApiPort,hostNames);
                for (Map.Entry<String, String> entry : vmstates.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("InService")){
                        inServiceHostNames.add(entry.getKey());
                    }
                }
            }
            if (inServiceHostNames.size()>0){
                logger.info("inServiceHostNames:"+inServiceHostNames);
                return inServiceHostNames;
            }
        }catch (Exception e){
            logger.error("获取InService状态的DataNode，Hosts，异常：",e);
        }
        return hostNames;
    }

    /**
     * 查询ResourceManager WebService Restful Api
     * 获取Running状态的HostName
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public List<String> getHostsWithRunningNodeManager(String clusterId, List<String> hostNames) {
        List<String> runningNodeManagers = new ArrayList<>();
        try {
            ResultMsg activeRMMsg=getActiveResourceManagerWithRetry(clusterId);
            if (activeRMMsg.getResult()){
                Map<String, String> hosts_state = getNodesFromYarn(
                        activeRMMsg.getData().toString(),
                        hadoopJmxApiPort,
                        Arrays.asList("RUNNING"));

                //未获取到host状态
                if (hosts_state == null || hosts_state.size() == 0){
                    return hostNames;
                }
                for (Map.Entry<String, String> entry : hosts_state.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase("RUNNING") && hostNames.contains(entry.getKey().toLowerCase())){
                        runningNodeManagers.add(entry.getKey());
                    }
                }
                logger.info("runningNodeManagers:"+runningNodeManagers);
                return runningNodeManagers;
            }
        }catch (Exception e){
            logger.error("获取running状态的NodeManager异常，",e);
        }
        return hostNames;
    }
}
