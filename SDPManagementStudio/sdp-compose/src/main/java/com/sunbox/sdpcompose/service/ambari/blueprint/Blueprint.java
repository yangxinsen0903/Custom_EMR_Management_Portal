package com.sunbox.sdpcompose.service.ambari.blueprint;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.domain.ambari.AmbariConfigItemAttr;
import com.sunbox.domain.enums.DynamicType;
import com.sunbox.domain.enums.SceneType;
import com.sunbox.sdpcompose.mapper.AmbariConfigItemAttrMapper;
import com.sunbox.sdpcompose.mapper.AmbariConfigItemMapper;
import com.sunbox.sdpcompose.service.ambari.enums.*;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Blueprint对象
 * @author: wangda
 * @date: 2022/12/3
 */
public class Blueprint {

    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(Blueprint.class);

    /** Blueprint的 Blueprints节点*/
    @JsonProperty("Blueprints")
    BlueprintStack blueprints = new BlueprintStack();

    /** Blueprint的 host_groups节点 */
    @JsonProperty("host_groups")
    List<HostGroup> hostGroups = new ArrayList<>();

    /** Blueprint的 configurations 节点 */
    @JsonProperty("configurations")
    List<Map<String, BlueprintConfiguration>> configurations = new ArrayList<>();

    /**
     * 配置, 里面有两个配置对象: <br/>
     * 第一个: recovery_settings: [BlueprintRecoverySetting]<br/>
     * 第二个: service_settings: [BlueprintRecoverySetting]<br/>
     * <p/>
     * 这里面的配置由系统自动配置
     */
    @JsonProperty("settings")
    List<Map<String, List<BlueprintRecoverySetting>>> settings;

    @JsonIgnore
    private AmbariConfigItemMapper configItemMapper;

    @JsonIgnore
    private AmbariConfigItemAttrMapper configItemAttrMapper;

    /**
     * 为了方便查找配置文件生成的Map，Key=配置文件标识  Value=配置项
     */
    @JsonIgnore
    private Map<String, BlueprintConfiguration> configMap = new HashMap<>();

    /**
     * HA类型
     */
    @JsonIgnore
    private ConfigItemType ha;

    public BlueprintConfiguration findConfigByConfigType(String type) {
        if (configMap.size() == 0) {
            for (Map<String, BlueprintConfiguration> configuration : configurations) {
                Optional<Map.Entry<String, BlueprintConfiguration>> first = configuration.entrySet().stream().findFirst();
                first.ifPresent(entry -> {
                    configMap.put(entry.getKey(), entry.getValue());
                });
            }
        }
        return configMap.get(type);
    }

    /**
     * 删除一个配置
     * @param type 配置项类型，也就是配置文件名，可以从 <code>ConfigClassification</code>中取
     */
    public void removeConfigurationByConfigType(String type) {
        // 删除缓存
        configMap.remove(type);

        // 删除配置
        for (int i=0; i<configurations.size(); i++) {
            Map<String, BlueprintConfiguration> configuration = configurations.get(i);
            String blueprintConfigType = CollUtil.getFirst(configuration.keySet());
            if (Objects.equals(type, blueprintConfigType)) {
                configurations.remove(i);
                break;
            }
        }
    }

    /**
     * 从Spring容器中找到Mapper做初始化
     */
    public void init() {
        configItemMapper = SpringContextUtil.getBean(AmbariConfigItemMapper.class);
        configItemAttrMapper = SpringContextUtil.getBean(AmbariConfigItemAttrMapper.class);
    }

    /**
     * 设置服务的recoveryEnabled
     * @param name 服务名
     * @param recoveryEnabled 是否开启
     */
    public void putServiceSetting(String name, boolean recoveryEnabled) {
        if (Objects.isNull(settings)) {
            settings = new ArrayList<>();
        }

        List<BlueprintRecoverySetting> list = findRecoverySettingByName(settings, Constants.RecoverySettingsServiceKey);

        Optional<BDService> optionalService = BDService.parse(name);
        if (!optionalService.isPresent()) {
            logger.error("不支持的大数据服务：" + name);
            return;
        }

        BDService service = optionalService.get();
        if (Objects.equals(service, BDService.HDFS)) {
            list.add(BlueprintRecoverySetting.of(recoveryEnabled, true, optionalService.get().name()));
        } else {
            list.add(BlueprintRecoverySetting.of(recoveryEnabled, optionalService.get().name()));
        }

//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "MAPREDUCE2"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "SPARK3"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "HBASE"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "YARN"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "SQOOP"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "ZOOKEEPER"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "HDFS"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "TEZ"));
    }

    /**
     * 设置服务的recoveryEnabled
     * @param name 服务名
     * @param recoveryEnabled 是否开启
     */
    public void putComponentSetting(String name, boolean recoveryEnabled) {
        if (Objects.isNull(settings)) {
            settings = new ArrayList<>();
        }

        List<BlueprintRecoverySetting> list = findRecoverySettingByName(settings, Constants.RecoverySettingsComponentKey);

        Optional<BDService> optionalService = BDService.parse(name);
        if (!optionalService.isPresent()) {
            logger.error("不支持的大数据服务：" + name);
            return;
        }

        BDService service = optionalService.get();
        List<BDComponent> components = service.getComponents(Objects.equals(ha, ConfigItemType.HA) ? HAScene.HA : HAScene.NON_HA);

        for (BDComponent component : components) {
            list.add(BlueprintRecoverySetting.of(recoveryEnabled, component.name()));

        }

//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "ZKFC"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "ZOOKEEPER_SERVER"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "HBASE_REGIONSERVER"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "SPARK3_THRIFTSERVER"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "SQOOP"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "MAPREDUCE2_CLIENT"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "YARN_CLIENT"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "HIVE_SERVER"));
//        list.add(BlueprintRecoverySetting.of(recoveryEnabled, "TEZ_CLIENT"));
    }

    /**
     * 设置全局的设置服务的recoveryEnabled
     * @param recoveryEnabled 是否开启
     */
    public void putRecoverySetting(boolean recoveryEnabled) {
        if (Objects.isNull(settings)) {
            settings = new ArrayList<>();
        }

        List<BlueprintRecoverySetting> list = findRecoverySettingByName(settings, Constants.RecoverySettingsGlobalKey);

        BlueprintRecoverySetting setting = new BlueprintRecoverySetting();
        setting.setRecoveryEnabled(String.valueOf(recoveryEnabled));
        list.add(setting);
    }

    /**
     * 根据服务名查找 RecoverySetting
     * @param settings 所有的配置
     * @param name 服务名
     * @return
     */
    private List<BlueprintRecoverySetting> findRecoverySettingByName(List<Map<String, List<BlueprintRecoverySetting>>> settings, String name) {
        for (Map<String, List<BlueprintRecoverySetting>> setting : this.settings) {
            List<BlueprintRecoverySetting> list = setting.get(name);
            if (Objects.nonNull(list)) {
                // 找到后直接返回,
                return list;
            }
        }
        // 没找到,创建一个新的.
        List<BlueprintRecoverySetting> list = new ArrayList<>();
        Map<String, List<BlueprintRecoverySetting>> map = new HashMap<>();
        map.put(name, list);
        settings.add(map);
        return list;
    }

    /**
     * 设置Blueprint的Stack信息
     * @param blueprintName Blueprint名，不能重复，生成创建模板时需要
     * @param stackFullName 全量
     */
    public void setStackInfo(String blueprintName, String stackFullName) {
        if (Objects.isNull(blueprints)) {
            blueprints = new BlueprintStack();
        }

        String[] names = stackFullName.split("-");
        if (Objects.isNull(names) || names.length != 2) {
            throw new RuntimeException("Stack发行版名称不正确，正确格式为：SDP-{版本号}，传进来的名称为：" + stackFullName);
        }
        blueprints.setBlueprintName(blueprintName);
        blueprints.setStackName(names[0]);
        blueprints.setStackVersion(names[1]);
    }


    /**
     * 初始化默认配置
     * @param services
     */
    public void initDefaultConfig(String stackCode, List<String> services) {
        if ( Objects.isNull(configurations)) {
            configurations = new ArrayList<>();
        }
        List<String> allService = Lists.newArrayList("STACK");
        allService.addAll(services);

        // 获取所有服务具体配置信息
        List<AmbariConfigItem> configItems = configItemMapper.queryByStackCodeAndServices(stackCode, allService, ha.name());

        // 将配置按配置类型分组
        Map<String, List<AmbariConfigItem>> configItemGroup = configItems.stream().collect(Collectors.groupingBy(AmbariConfigItem::getConfigTypeCode));

        // 设置初始配置
        configItemGroup.forEach((k, v) -> {
            Map<String, BlueprintConfiguration> map = new HashMap();
            Map<String, Object> properties = v.stream().collect(Collectors.toMap(AmbariConfigItem::getKey, AmbariConfigItem::getValue));
            BlueprintConfiguration config = new BlueprintConfiguration(k);
            config.putProperties(properties);
            map.put(k, config);
            // 全局搜索用的Map
            configMap.put(k, config);
            configurations.add(map);
        });

        // 获取 properties_attribute 的配置，此操作依赖上面的configMap。所以不能提前。
        initDefaultPropertiesAttr(stackCode, services);
    }

    private void initDefaultPropertiesAttr(String stackCode, List<String> services) {
        // 1. 从数据库中查询出需要加到Blueprint里的配置
        List<AmbariConfigItemAttr> attrs = configItemAttrMapper.queryByStackCodeAndServiceCode(stackCode, services);
        if (CollectionUtils.isEmpty(attrs)) {
            return;
        }

        // 2. 按配置标识（配置文件名称）分组
        Map<String, List<AmbariConfigItemAttr>> groupedAttr = attrs.stream().collect(Collectors.groupingBy(AmbariConfigItemAttr::getConfigTypeCode));

        // 3. 遍历循环找到各个配置文件的配置项，设置里面的配置值
        for (Map.Entry<String, List<AmbariConfigItemAttr>> entry : groupedAttr.entrySet()) {
            // 找到配置标识（配置文件）的配置对象，要把数据库里的配置加到这个配置对象里面
            BlueprintConfiguration existConfig = configMap.get(entry.getKey());
            if (Objects.isNull(existConfig)) {
                continue; // 如果没找到配置项，则不设置配置属性。因为Ambari会报错。
//                existConfig = new BlueprintConfiguration(entry.getKey());
//                configMap.put(entry.getKey(), existConfig);
//                Map<String , BlueprintConfiguration> existConfigMap = new HashMap<>();
//                existConfigMap.put(entry.getKey(), existConfig);
//                configurations.add(existConfigMap);
            }

            // 按  配置文件(core-site)  -  TagName(final) -  配置Map(配置名->配置值)  的样子分组，生成配置项Map
            List<AmbariConfigItemAttr> configList = entry.getValue();
            if (CollectionUtils.isEmpty(configList)) {
                continue;
            }
            Map<String, List<AmbariConfigItemAttr>> configGroupByTag = configList.stream().collect(Collectors.groupingBy(AmbariConfigItemAttr::getTagName));

            for (Map.Entry<String, List<AmbariConfigItemAttr>> configByTag : configGroupByTag.entrySet()) {
                String tagName = configByTag.getKey();
                Map<String, Object> configAttr = configByTag.getValue().stream().collect(Collectors.toMap(AmbariConfigItemAttr::getKey, AmbariConfigItemAttr::getValue));
                existConfig.putPropertiesAttributes(tagName, configAttr);
            }
        }
    }

    /**
     * 初始化数据库连接信息,将数据库的配置保存到配置中
     * @param dbConfigs
     */
    public void initDBConfig(List<DBConnectInfo> dbConfigs) {
        if (CollectionUtils.isEmpty(dbConfigs)) {
            return ;
        }

        for (DBConnectInfo dbConfig : dbConfigs) {
            String type = dbConfig.getConfigType();
            BlueprintConfiguration blueprintConfiguration = configMap.get(type);
            if (Objects.isNull(blueprintConfiguration)) {
                blueprintConfiguration = new BlueprintConfiguration(type);
                configMap.put(type, blueprintConfiguration);
                configurations.add(blueprintConfiguration.toMap());
            }
            blueprintConfiguration.putProperties(dbConfig.toConfigMap());
        }
    }

    /**
     * 设置HBase的多磁盘配置
     * @param scene 场景
     * @param cmd 创建Blueprint的参数，要从参数中获取Core节点的主机信息安装HBase存储用的主机列表, 用于计算磁盘数量
     */
    public void setHbaseMultiDiskConfig(String stackVersion, SceneType scene,CreateBlueprintCmd cmd) {
        List<HostInstance> hosts = new ArrayList<>();
        for (ClusterHostGroup hostGroup : cmd.getHostGroups()) {
            if (hostGroup.getRole() == HostGroupRole.CORE) {
                hosts = hostGroup.getHosts();
                setInstanceGroupMultiDiskConfig(stackVersion, scene, hosts, DynamicType.MULTI_DISK);
                setInstanceGroupMultiDiskConfig(stackVersion, scene, hosts, DynamicType.MULTI_DISK_TASK);
            }
        }

    }

    private void setInstanceGroupMultiDiskConfig(String stackVersion, SceneType scene, List<HostInstance> hosts, DynamicType dynamicType) {
        if (CollectionUtil.isEmpty(hosts)) {
            logger.error("设置Core实例组中多磁盘配置时，Core实例组主机数量为0，跳过配置多磁盘");
            return;
        }

        // 判断是否是HBase,如果是HBase才需要修改这个配置
        if (!Objects.equals(scene, SceneType.HBASE)) {
            logger.info("设置HBase多磁盘配置时，传入的场景不是HBASE，如果磁盘数量超过1块，也会进行多磁盘配置。");
        }

        // 从数据库加载HBase的多磁盘配置项
        List<AmbariConfigItem> items = configItemMapper.queryByDynamicTypeAndItemType(stackVersion, dynamicType.name(), ha.name());

        // 磁盘
        if (CollectionUtils.isEmpty(hosts)) {
            logger.warn("要判断主机磁盘数量时,传递过来的主机列表为空，计算中止，退出计算多磁盘配置");
            return;
        }

        HostInstance hostInstance = hosts.get(0);
        if (CollectionUtils.isEmpty(hostInstance.getDisks())) {
            logger.warn("主机未设置数据盘，无法根据数据盘数量计算数据盘配置参数, HostRole={}, HostName={}", hostInstance.getHostRole(), hostInstance.getHostName());
            return;
        }

        int diskCount = hostInstance.getDisks().get(0).getCount();
        for (AmbariConfigItem item : items) {
            BlueprintConfiguration config = configMap.get(item.getConfigTypeCode());
            if (Objects.isNull(config)) {
                logger.warn("没找到多磁盘配置文件：{}", item.getConfigTypeCode());
                continue;
            }
            config.getProperties().put(item.getKey(), generateHbaseMultiDiskPath(item.getValue(), diskCount));
        }
    }

    /**
     * 设置MI ABFS的配置
     */
    public void setMIABFSConfig(String stackVersion, String miTenantId, String miClientId) {
//        logger.info("添加mi配置,stackVersion:{} miTenantId:{} miClientId:{}",stackVersion,miClientId,miClientId);
        String tenantIdKey = "fs.azure.account.oauth2.msi.tenant";

        String clientIdKey = "fs.azure.account.oauth2.client.id";

        List<AmbariConfigItem> items = configItemMapper.queryByDynamicTypeAndItemType(stackVersion, DynamicType.MI_ABFS.name(), ha.name());

        for (AmbariConfigItem item : items) {
            logger.info("setMIABFSConfig key:{}",item.getKey());
            // fs.azure.account.oauth2.msi.tenant  TenantId
            if (Objects.equals(item.getKey(), tenantIdKey)) {
                BlueprintConfiguration config = findConfigByConfigType(item.getConfigTypeCode());
                config.getProperties().put(item.getKey(), miTenantId);
            }

            // fs.azure.account.oauth2.client.id    ClientId
            if (Objects.equals(item.getKey(), clientIdKey)) {
                BlueprintConfiguration config = findConfigByConfigType(item.getConfigTypeCode());
                config.getProperties().put(item.getKey(), miClientId);
            }
        }
    }

    private String generateHbaseMultiDiskPath(String path, int diskCount) {
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

    /**
     * 初始化服务的Setting
     * @param services 服务列表
     */
    public void initSettings(List<String> services) {
        // 全局配置
        putRecoverySetting(true);

        // 每个服务配置 组件和Service都不设置了。
//        for (String service : services) {
//            putServiceSetting(service, true);
//            putComponentSetting(service, true);
//        }
    }

    public void addHostGroupConfiguration(String hostGroupName, List<Map<String, Map<String,Object>>> configuration) {
        for (HostGroup hostGroup : hostGroups) {
            if (Objects.equals(hostGroup.getName(), hostGroupName)) {
                for (Map<String, Map<String,Object>> configMap : configuration) {
//                    Map<String, BlueprintConfiguration> dupConfigMap = duplicateBlueprintConfiguration(configMap);
                    HashMap<String, Map<String, Object>> dupConfigMap = new HashMap<>();
                    for (Map.Entry<String, Map<String, Object>> mapEntry : configMap.entrySet()) {
                        dupConfigMap.put(mapEntry.getKey(),new HashMap<>(mapEntry.getValue()));
                    }
                    hostGroup.getConfigurations().add(dupConfigMap);
                }
            }
        }
    }

    Map<String, BlueprintConfiguration> duplicateBlueprintConfiguration(Map<String, BlueprintConfiguration> configuration) {
        Optional<Map.Entry<String, BlueprintConfiguration>> configEntry = configuration.entrySet().stream().findFirst();
        String classification = configEntry.get().getKey();
        BlueprintConfiguration blueprintConfiguration = configEntry.get().getValue();
        BlueprintConfiguration dupOne = new BlueprintConfiguration(classification);
        for (Map.Entry<String, Object> entry : blueprintConfiguration.getProperties().entrySet()) {
            dupOne.putProperties(entry.getKey(), entry.getValue());
        }

        Map<String, BlueprintConfiguration> dupConfig = new HashMap<>();
        dupConfig.put(dupOne.getConfigItemName(), dupOne);
        return dupConfig;
    }

    public BlueprintStack getBlueprints() {
        return blueprints;
    }

    public void setBlueprints(BlueprintStack blueprints) {
        this.blueprints = blueprints;
    }

    public List<HostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<HostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public List<Map<String, BlueprintConfiguration>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Map<String, BlueprintConfiguration>> configurations) {
        this.configurations = configurations;
    }

    public ConfigItemType getHa() {
        return ha;
    }

    public void setHa(ConfigItemType ha) {
        this.ha = ha;
    }

}
