package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import com.sunbox.sdpcompose.service.ambari.blueprint.*;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigRecommendationStrategy;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import com.sunbox.sdpcompose.service.ambari.enums.ProvisionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 集群创建模板
 *
 * @author: wangda
 * @date: 2022/12/6
 */
public class ClusterTemplate {
    private Logger logger = LoggerFactory.getLogger(ClusterTemplate.class);

    /**
     * 基于Blueprint创建集群时的Blueprint名
     */
    @JsonProperty("blueprint")
    String blueprintName;

    /**
     * 配置策略
     */
    @JsonProperty("config_recommendation_strategy")
    String configRecommendationStrategy = ConfigRecommendationStrategy.NEVER_APPLY.name();

    /**
     *
     */
    @JsonProperty("provision_action")
    String provisionAction = ProvisionAction.INSTALL_ONLY.name();

    @JsonProperty("configurations")
    List<Map<String, BlueprintConfiguration>> configurations = new ArrayList<>();

    @JsonProperty("host_groups")
    List<ClusterTemplateHostGroup> hostGroups = new ArrayList<>();


    @JsonProperty("Clusters")
    ClusterInfo cluster = new ClusterInfo();

    @JsonIgnore
    Blueprint blueprint;

    public ClusterTemplate() {
    }

    public ClusterTemplate(String clusterName, Blueprint blueprint) {
        this.cluster.setClusterName(clusterName);
        this.blueprint = blueprint;
        this.blueprintName = blueprint.getBlueprints().getBlueprintName();
    }

    /**
     * 初始化主机组,2.1版本后不再使用此方法，因为此方法不支持多实例组
     *
     * @param hostGroups 主机组
     */
    @Deprecated
    public void initHostGroup(Map<HostGroupRole, List<String>> hostGroups) {
        Assert.notEmpty(hostGroups, "创建集群的主机列表不能为空");
        Assert.notNull(blueprint, "Blueprint不能为空");
        Map<HostGroupRole, List<HostGroup>> layout = blueprint.getHostGroups().stream().collect(Collectors.groupingBy(HostGroup::getHostGroupRole));

        // ambari 类型
        List<ClusterTemplateHostGroup> ambari = createClusterTemplateHostGroup(HostGroupRole.AMBARI, layout, hostGroups);
        if (!CollectionUtils.isEmpty(ambari)) {
            this.hostGroups.addAll(ambari);
        }

        // master,检查是高可用, 还是非高可用.
        List<ClusterTemplateHostGroup> master = createClusterTemplateHostGroup(HostGroupRole.MASTER, layout, hostGroups);
        if (!CollectionUtils.isEmpty(master)) {
            this.hostGroups.addAll(master);
        }

        // core
        List<ClusterTemplateHostGroup> core = createClusterTemplateHostGroup(HostGroupRole.CORE, layout, hostGroups);
        if (!CollectionUtils.isEmpty(core)) {
            this.hostGroups.addAll(core);
        }

        // task
        List<ClusterTemplateHostGroup> task = createClusterTemplateHostGroup(HostGroupRole.TASK, layout, hostGroups);
        if (!CollectionUtils.isEmpty(task)) {
            this.hostGroups.addAll(task);
        }

    }

    /**
     * 初始化主机组 和 主机组的配置
     *
     * @param hostGroups
     */
    public void initHostGroupsAndConfiguration(List<ClusterHostGroup> hostGroups) {
        Assert.notEmpty(hostGroups, "创建集群的主机列表不能为空");
        Assert.notNull(blueprint, "Blueprint不能为空");

        Map<HostGroupRole, List<HostGroup>> layout = blueprint.getHostGroups().stream().collect(Collectors.groupingBy(HostGroup::getHostGroupRole));

        // 特殊处理 ambari 类型
        List<ClusterTemplateHostGroup> ambari = createClusterTemplateHostGroup(HostGroupRole.AMBARI, layout, hostGroups);
        if (!CollectionUtils.isEmpty(ambari)) {
            this.hostGroups.addAll(ambari);
        }

        // 特殊处理master,检查是高可用, 还是非高可用.
        List<ClusterTemplateHostGroup> master = createClusterTemplateHostGroup(HostGroupRole.MASTER, layout, hostGroups);
        if (!CollectionUtils.isEmpty(master)) {
            this.hostGroups.addAll(master);
        }

        // Core和Task统一处理
        // core
//        List<ClusterTemplateHostGroup> core = createClusterTemplateHostGroup(HostGroupRole.CORE, layout, hostGroups);
        List<ClusterTemplateHostGroup> core = createClusterTemplateTaskHostGroup(layout.get(HostGroupRole.CORE), hostGroups);
        if (!CollectionUtils.isEmpty(core)) {
            this.hostGroups.addAll(core);
        }

        // task
        // Task实例组中的Hostgroup与Sku相关,一个Sku对应一个HostGroup
        List<ClusterTemplateHostGroup> task = createClusterTemplateTaskHostGroup(layout.get(HostGroupRole.TASK),
                hostGroups);
        if (!CollectionUtils.isEmpty(task)) {
            this.hostGroups.addAll(task);
        }
    }

    /**
     * 2.1版本后， 不再使用此方法，因为有此方法不支持多实例组
     *
     * @param role
     * @param layoutMap
     * @param hostsMap
     * @return
     */
    @Deprecated
    List<ClusterTemplateHostGroup> createClusterTemplateHostGroup(HostGroupRole role, Map<HostGroupRole, List<HostGroup>> layoutMap,
                                                                  Map<HostGroupRole, List<String>> hostsMap) {
        List<HostGroup> layoutHostGroup = layoutMap.get(role);
        if (CollectionUtils.isEmpty(layoutHostGroup)) {
            logger.info("Blueprint的主机布局中没有此类型的布局:{}", role.name());
            // 如果没有布局, 不为此角色的主机组设置主机
            return null;
        }

        List<String> hostList = hostsMap.get(role);
        if (CollectionUtils.isEmpty(hostList)) {
            logger.info("Blueprint的主机布局中有类型: {}, 但是主机列表中中未给此类型的布局设置主机", role.name());
            return null;
        }

        // AMBARI只能有一台主机, MASTER只能有2台主机, CORE和TASK随便
        if (Objects.equals(role, HostGroupRole.AMBARI)) {
            HostGroup hostGroup = layoutHostGroup.get(0);
            ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
            group.setName(hostGroup.getName());
            group.hosts.add(new ClusterTemplateHost(hostList.get(0)));
            return Lists.newArrayList(group);
        } else if (Objects.equals(role, HostGroupRole.MASTER)) {
            if (layoutHostGroup.size() != hostList.size()) {
                String errMsg = "Master布局数量与主机数据不匹配: 布局数据:" + layoutHostGroup.size() + ", 主机数量:" + hostList.size();
                throw new RuntimeException(errMsg);
            }
            ArrayList<ClusterTemplateHostGroup> resultList = Lists.newArrayList();
            for (int i = 0; i < layoutHostGroup.size(); i++) {
                HostGroup hostGroup = layoutHostGroup.get(i);
                ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
                group.setName(hostGroup.getName());
                group.hosts.add(new ClusterTemplateHost(hostList.get(i)));
                resultList.add(group);
            }
            return resultList;
        } else {
            if (layoutHostGroup.size() > 1) {
                String errMsg = role.name() + "的主机组只能有1个,现在数量是:" + layoutHostGroup.size();
                throw new RuntimeException(errMsg);
            }
            ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
            String groupName = layoutHostGroup.get(0).getName();
            group.setName(groupName);
            ArrayList<ClusterTemplateHostGroup> resultList = Lists.newArrayList();
            for (int i = 0; i < hostList.size(); i++) {
                group.hosts.add(new ClusterTemplateHost(hostList.get(i)));
            }
            resultList.add(group);
            return resultList;
        }
    }

    /**
     * 根据role,创建ClusterTemplate的HostGroup
     * @param role
     * @param layoutMap
     * @param allHostGroups
     * @return
     */
    List<ClusterTemplateHostGroup> createClusterTemplateHostGroup(HostGroupRole role,
                                                                  Map<HostGroupRole, List<HostGroup>> layoutMap,
                                                                  List<ClusterHostGroup> allHostGroups) {
        List<HostGroup> layoutHostGroup = layoutMap.get(role);
        if (CollectionUtils.isEmpty(layoutHostGroup)) {
            logger.info("Blueprint的主机布局中没有此类型的布局:{}", role.name());
            // 如果没有布局, 不为此角色的主机组设置主机
            return null;
        }

        Map<HostGroupRole, List<ClusterHostGroup>> grouped = allHostGroups.stream()
                .collect(Collectors.groupingBy(ClusterHostGroup::getRole));

        List<ClusterHostGroup> roleHostGroups = grouped.get(role);

        if (CollUtil.isEmpty(roleHostGroups)) {
            logger.info("Blueprint的主机布局中没有此类型的布局:{}", role.name());
            // 如果没有布局, 不为此角色的主机组设置主机
            return null;
        }

        // AMBARI只能有一台主机, MASTER只能有2台主机, CORE和TASK随便
        if (Objects.equals(role, HostGroupRole.AMBARI)) {
            // Blueprint中的AMBARI布局的主机组
            HostGroup hostGroup = layoutHostGroup.get(0);

            // 创建集群时，传进来的主机列表
            ClusterHostGroup ambariHostGroup = roleHostGroups.get(0);
            HostInstance instance = ambariHostGroup.getHosts().get(0);

            ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
            group.setName(hostGroup.getName());
            group.hosts.add(new ClusterTemplateHost(instance.getHostName()));
            // group.setConfigurations(convertToBlueprintConfigurations(ambariHostGroup.getGroupConfgs()));
            return Lists.newArrayList(group);

        } else if (Objects.equals(role, HostGroupRole.MASTER)) {
            // Master实例组，只有1个组配置，里面有2台主机。
            ClusterHostGroup clusterHostGroup = roleHostGroups.get(0);
            if (layoutHostGroup.size() != clusterHostGroup.getHosts().size()) {
                String errMsg = "Master布局数量与主机数据不匹配: Blueprint布局数据:" + layoutHostGroup.size() + ", 传参布局数量:" + roleHostGroups.size();
                throw new RuntimeException(errMsg);
            }

            ArrayList<ClusterTemplateHostGroup> resultList = Lists.newArrayList();
            for (int i = 0; i < clusterHostGroup.getHosts().size(); i++) {
//                ClusterHostGroup paramHostGroup = roleHostGroups.get(i);
                HostInstance host = clusterHostGroup.getHosts().get(i);
                HostGroup hostGroup = layoutHostGroup.get(i);
                ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
                group.setName(hostGroup.getName());
                group.hosts.add(new ClusterTemplateHost(host.getHostName()));
                // group.setConfigurations(convertToBlueprintConfigurations(clusterHostGroup.getGroupConfgs()));
                resultList.add(group);
            }
            return resultList;
        } else {
            // Core和Task
            logger.info("createClusterTemplateHostGroup-roleHostGroups:{}", JSONUtil.toJsonStr(roleHostGroups));
            ArrayList<ClusterTemplateHostGroup> resultList = Lists.newArrayList();
            for (int i = 0; i < layoutHostGroup.size(); i++) {
                if (i > roleHostGroups.size() - 1) {
                    continue;
                }
                String groupName = layoutHostGroup.get(i).getName();
                ClusterHostGroup clusterHostGroup = roleHostGroups.get(i);
                ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
                group.setName(groupName);
                for (HostInstance host : clusterHostGroup.getHosts()) {
                    group.hosts.add(new ClusterTemplateHost(host.getHostName()));
                }
                group.setConfigurations(convertToBlueprintConfigurations(clusterHostGroup.getGroupConfgs()));
                resultList.add(group);
            }
            return resultList;
        }
    }

    /**
     * 给ClusterTemplate的Task实例组创建HostGroup,<br/>
     * 因为Task实例组已经在Blueprint上根据VmSku进行分组创建了,所以此处直接使用Blueprint的HostGroup即可<br/>
     * <ol>
     *     <li>遍历Blueprint的HostGroup(因为Blueprint的HostGroup中只有大数据组件的布局,没有Host,所以需要单独为其取Host)</li>
     *     <li>从创建命令中的HostGroup中,找到对应的HostGroup</li>
     *     <li>取出Host,生成ClusterTemplate的HostGroup</li>
     *     <li>再补充上配置值</li>
     * </ol>
     * @param bpHostGroups Blueprint里的HostGroup
     * @param allHostGroups 创建集群命令中的HostGroup,这里面是按VmSku拆分过的HostGroup,拆分位置在创建CreateClusterCmd时
     * @return
     */
    List<ClusterTemplateHostGroup> createClusterTemplateTaskHostGroup(List<HostGroup> bpHostGroups,
                                                                  List<ClusterHostGroup> allHostGroups) {
        if (Objects.isNull(bpHostGroups)) {
            return null;
        }

        Map<String, ClusterHostGroup> cmdHostGroupsMap = new HashMap<>();
        for (ClusterHostGroup g : allHostGroups) {
            cmdHostGroupsMap.put(g.getGroupName(), g);
        }

        // Task
        ArrayList<ClusterTemplateHostGroup> resultList = Lists.newArrayList();
        for (int i = 0; i < bpHostGroups.size(); i++) {
            HostGroup hostGroup = bpHostGroups.get(i);
            String groupName = hostGroup.getName();
            ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
            group.setName(groupName);

            ClusterHostGroup clusterHostGroup = cmdHostGroupsMap.get(groupName);
            if (Objects.isNull(clusterHostGroup)) {
                logger.warn("为ClusterTemplate生成HostGroup时, 未从构建Cmd对象中找到HostGroup. groupName={}", groupName);
                continue;
            }
            for (HostInstance host : clusterHostGroup.getHosts()) {
                group.hosts.add(new ClusterTemplateHost(host.getHostName()));
            }
            group.setConfigurations(convertToBlueprintConfigurations(clusterHostGroup.getGroupConfgs()));
            resultList.add(group);
        }
        return resultList;
    }

    private List<Map<String, Map<String,Object>>> convertToBlueprintConfigurations(List<ConfClusterHostGroupAppsConfig> groupConfgs) {
        if (Objects.isNull(groupConfgs)) {
            return null;
        }

        List<Map<String, Map<String,Object>>> resultList = Lists.newArrayList();
        Map<String, List<ConfClusterHostGroupAppsConfig>> groupedByClass = groupConfgs.stream().collect(Collectors.groupingBy(ConfClusterHostGroupAppsConfig::getAppConfigClassification));

        for (Map.Entry<String, List<ConfClusterHostGroupAppsConfig>> entry : groupedByClass.entrySet()) {
            Map configMap = entry.getValue().stream().collect(Collectors.toMap(ConfClusterHostGroupAppsConfig::getConfigItem, ConfClusterHostGroupAppsConfig::getConfigVal));
            Map<String, Map<String,Object>> map = new HashMap<>();
            map.put(entry.getKey(), new HashMap<>(configMap));
            resultList.add(map);
        }
        return resultList;
    }


    public void initOverrideConfiguration(Map<String, Map<String, Object>> configMap) {
        // 循环处理配置
        if (CollectionUtils.isEmpty(configMap)) {
            return;
        }

        configMap.entrySet().forEach(entry -> {
            Map<String, BlueprintConfiguration> config = new HashMap<>();
            String name = entry.getKey();
            Map<String, Object> values = entry.getValue();

            BlueprintConfiguration blueprintConfiguration = new BlueprintConfiguration(name);
            blueprintConfiguration.putProperties(values);

            config.put(name, blueprintConfiguration);
            configurations.add(config);
        });
    }

    public void overrideConfiguration(Map<String, BlueprintConfiguration> configMap) {
        // 循环处理配置
        if (CollectionUtils.isEmpty(configMap)) {
            return;
        }

        configMap.entrySet().forEach(entry -> {
            // 需要覆盖用的配置项名称
            String name = entry.getKey();
            // 需要覆盖用的具体配置
            BlueprintConfiguration value = entry.getValue();

            // 找到配置，如果找到， 则覆盖里面的配置项， 如果没找到， 则新增一个
            BlueprintConfiguration existConfig = findBlueprintConfiguration(name);
            if (Objects.isNull(existConfig)) {
                BlueprintConfiguration blueprintConfiguration = new BlueprintConfiguration(name);
                blueprintConfiguration.putProperties(value.getProperties());
                Map<String, BlueprintConfiguration> config = new HashMap<>();
                config.put(name, blueprintConfiguration);
                configurations.add(config);
            } else {
                existConfig.putProperties(value.getProperties());
            }
        });
    }

    public void overrideHostGroupConfiguration(String hostGroupName, Map<String, BlueprintConfiguration> configMap) {
        // 循环处理配置
        if (CollectionUtils.isEmpty(configMap)) {
            return;
        }

        // 找到对应的hostGroup
        ClusterTemplateHostGroup hostGroup = findHostGroup(hostGroupName);
        if (Objects.isNull(hostGroup)) {
            logger.warn("找不到对应的hostGroup [hostGroupName={}]", hostGroupName);
            return;
        }

        hostGroup.addConfiguration(configMap.values());
    }

    private ClusterTemplateHostGroup findHostGroup(String hostGroupName) {
        for (ClusterTemplateHostGroup hostGroup : hostGroups) {
            if (StrUtil.equalsIgnoreCase(hostGroup.getName(), hostGroupName)) {
                return hostGroup;
            }
        }
        return null;
    }

    public BlueprintConfiguration findBlueprintConfiguration(String configItemType) {
        for (Map<String, BlueprintConfiguration> config : configurations) {
            if (config.containsKey(configItemType)) {
                return config.get(configItemType);
            }
        }
        return null;
    }

    /**
     * 计算那些需要进行计算的配置项:如堆大小, Zookeeper的位置
     */
    public void computeConfiguration() {

    }

    public String getProvisionAction() {
        return provisionAction;
    }

    public void setProvisionAction(String provisionAction) {
        this.provisionAction = provisionAction;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getConfigRecommendationStrategy() {
        return configRecommendationStrategy;
    }

    public void setConfigRecommendationStrategy(String configRecommendationStrategy) {
        this.configRecommendationStrategy = configRecommendationStrategy;
    }

    public List<Map<String, BlueprintConfiguration>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Map<String, BlueprintConfiguration>> configurations) {
        this.configurations = configurations;
    }

    public List<ClusterTemplateHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<ClusterTemplateHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public ClusterInfo getCluster() {
        return cluster;
    }

    public void setCluster(ClusterInfo cluster) {
        this.cluster = cluster;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }
}
