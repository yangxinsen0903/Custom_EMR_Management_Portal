package com.sunbox.sdpcompose.service.ambari;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: wangda
 * @date: 2023/2/16
 */
public class InstanceGroupConfiguration {

    /**
     * 实例组ID，必填
     */
    private String groupId;

    /**
     * 配置
     */
    private List<BlueprintConfiguration> groupCfgs;

    private Map<String, BlueprintConfiguration> cfgsMap = new HashMap<>();


    public BlueprintConfiguration getByConfigItem(String configItem) {
        return this.cfgsMap.get(configItem);
    }

    /**
     * 增加一个配置
     * @param configType 配置分类，如：core-site, hdfs-site 等
     * @param configMap 该配置分类下的的所有配置
     */
    public void putConfig(String configType, Map configMap) {
        if (Objects.isNull(groupCfgs)) {
            this.groupCfgs = new ArrayList<>();
        }

        BlueprintConfiguration config = new BlueprintConfiguration();
        config.setConfigItemName(configType);
        config.putProperties(configMap);
        this.groupCfgs.add(config);

        this.cfgsMap.put(configType, config);
    }

    public List<BlueprintConfiguration> getGroupCfgs() {
        return groupCfgs;
    }

    public void setGroupCfgs(List<BlueprintConfiguration> groupCfgs) {
        this.groupCfgs = groupCfgs;
        this.cfgsMap = this.groupCfgs.stream().collect(Collectors.toMap(BlueprintConfiguration::getConfigItemName, Function.identity()));
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public InstanceGroupConfiguration(String groupId) {
        this.groupId = groupId;
    }
}
