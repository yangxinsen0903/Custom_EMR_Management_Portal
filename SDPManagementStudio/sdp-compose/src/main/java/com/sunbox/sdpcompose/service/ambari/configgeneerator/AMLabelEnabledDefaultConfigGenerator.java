/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.service.ambari.configgeneerator;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 在创建集群时, 如果在yarn-site中配置了 <code>core.am-labels.enabled = true</code>，则会使用本类来生成开启AM标签相关的一堆配置<br/>
 * <b>注意:此配置加到Default配置中,可不能加错了</b>
 * @author wangda
 * @date 2025/2/20
 */
public class AMLabelEnabledDefaultConfigGenerator implements CustomConfigGenerator{
    Logger logger = LoggerFactory.getLogger(AMLabelEnabledDefaultConfigGenerator.class);

    @Override
    public List<BlueprintConfiguration> generate(HostInstance instance) {
        List<BlueprintConfiguration> configurations = new ArrayList<>();
        BlueprintConfiguration capacityConfig = generateCapacitySchedulerConfig();
        if (Objects.nonNull(capacityConfig)) {
            configurations.add(capacityConfig);
        }

        BlueprintConfiguration yarnConfig = generateYarnSiteConfig();
        if (Objects.nonNull(yarnConfig)) {
            configurations.add(yarnConfig);
        }

        return configurations;
    }

    private BlueprintConfiguration generateCapacitySchedulerConfig() {
        logger.info("CoreAMLabelEnabled-生成Default capacity-scheduler配置...");
        try {
            BlueprintConfiguration config = new BlueprintConfiguration(ConfigClassification.CAPACITY_SCHEDULER.getClassification());

            config.putProperties("yarn.scheduler.capacity.root.accessible-node-labels.am.capacity", "100");
            config.putProperties("yarn.scheduler.capacity.root.default.accessible-node-labels.am.capacity", "100");

            logger.info("CoreAMLabelEnabled-生成Default capacity-scheduler配置完成, 生成内容为:{}", config.getProperties());
            return config;
        } catch (Exception ex) {
            logger.error("CoreAMLabelEnabled-生成capacity-scheduler配置出错", ex);
            return null;
        }
    }

    private BlueprintConfiguration generateYarnSiteConfig() {
        logger.info("CoreAMLabelEnabled-生成Default yarn-site配置...");
        try {
            BlueprintConfiguration config = new BlueprintConfiguration(ConfigClassification.YARN_SITE.getClassification());

            config.putProperties("yarn.node-labels.enabled", "true");
            config.putProperties("yarn.node-labels.configuration-type", "distributed");
            config.putProperties("yarn.resourcemanager.node-labels.am.default-node-label-expression", "am");
            config.putProperties("yarn.nodemanager.node-labels.provider", "config");

            logger.info("CoreAMLabelEnabled-生成Default yarn-site配置完成, 生成内容为:{}", config.getProperties());
            return config;
        } catch (Exception ex) {
            logger.error("CoreAMLabelEnabled-生成Default yarn-site配置出错", ex);
            return null;
        }
    }
}
