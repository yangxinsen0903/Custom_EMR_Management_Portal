package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 集群创建模板中的主机组，用于配置在Blueprint中主机组的实际的主机信息
 * @author: wangda
 * @date: 2022/12/7
 */
public class ClusterTemplateHostGroup {
    private static Logger logger = LoggerFactory.getLogger(ClusterTemplateHostGroup.class);
    /** 主机组名称，如 master, core, task 等 */
    @JsonProperty("name")
    String name;

    /**
     * 该主机组下面的所有主机列表
     */
    @JsonProperty("hosts")
    List<ClusterTemplateHost> hosts = new ArrayList<>();

    @JsonProperty("configurations")
    List<Map<String, Map<String,Object>>> configurations = new ArrayList<>();

//    @JsonProperty("host_count")
//    Integer hostCount;
//
//    @JsonProperty("host_predicate")
//    String hostPredicate;

    public ClusterTemplateHostGroup() {

    }

    /**
     * 给一个实例组增加配置
     * @param configs
     */
    public void addConfiguration(Collection<BlueprintConfiguration> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return;
        }

        for (BlueprintConfiguration newConfig : configs) {
            // 从现有的配置中找到对应的配置文件，如果没有，则新增
            Map<String,Object> existConfig = getConfigByItemName(newConfig.getConfigItemName());
            if (Objects.isNull(existConfig)) {
                logger.info("=========配置不存在,新增配置:{}", JSON.toJSONString(newConfig));
                // 不存在这个配置，新增
                Map<String, Map<String,Object>> newConfigMap = new HashMap<>();
                newConfigMap.put(newConfig.getConfigItemName(), newConfig.getProperties());
                configurations.add(newConfigMap);
            } else {
                logger.info("==========配置已经存在,进行覆盖:{}", JSON.toJSONString(existConfig));
                // 覆盖
                existConfig.putAll(newConfig.getProperties());
            }
        }

    }

    /**
     * 填加配置，如果已经存在了， 就不覆盖了。
     * @param configs
     */
    public void addConfigurationIfNotExist(List<BlueprintConfiguration> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return;
        }

        for (BlueprintConfiguration newConfig : configs) {
            // 从现有的配置中找到对应的配置文件，如果没有，则新增
            Map<String,Object> existConfig = getConfigByItemName(newConfig.getConfigItemName());
            if (Objects.isNull(existConfig)) {
                logger.info("=========配置不存在,新增配置:{}", JSON.toJSONString(newConfig));
                // 不存在这个配置，新增
                Map<String,  Map<String,Object>> newConfigMap = new HashMap<>();
                newConfigMap.put(newConfig.getConfigItemName(), new HashMap<>(newConfig.getProperties()));
                configurations.add(newConfigMap);
            } else {
                logger.info("==========配置已经存在,进行覆盖:{}", JSON.toJSONString(existConfig));
                // 不进行覆盖。遍历新配置，检查每一个新配置项在Blueprint是否存在，如果存在，就跳过。否则，新增加
                for (Map.Entry<String, Object> entry : newConfig.getProperties().entrySet()) {
                    Object property = existConfig.get(entry.getKey());
                    if (Objects.isNull(property)) {
                        existConfig.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    private Map<String,Object> getConfigByItemName(String itemName) {
        for (Map<String, Map<String,Object>> configuration : configurations) {
            Map<String,Object> blueprintConfiguration = configuration.get(itemName);
            if (Objects.nonNull(blueprintConfiguration)) {
                return blueprintConfiguration;
            }
        }
        return null;
    }

    public List<Map<String, Map<String, Object>>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Map<String, Map<String, Object>>> configurations) {
        this.configurations = configurations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClusterTemplateHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<ClusterTemplateHost> hosts) {
        this.hosts = hosts;
    }
}
