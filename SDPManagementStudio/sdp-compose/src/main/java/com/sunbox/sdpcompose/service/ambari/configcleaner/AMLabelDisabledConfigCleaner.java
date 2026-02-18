/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.service.ambari.configcleaner;

import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 禁用AmLabel时, 清理无用的配置
 * @author wangda
 * @date 2025/2/24
 */
public class AMLabelDisabledConfigCleaner implements ConfigCleaner{
    Logger logger = LoggerFactory.getLogger(AMLabelDisabledConfigCleaner.class);

    @Override
    public void clean(Blueprint blueprint, ClusterTemplate clusterTemplate) {
        cleanBlueprintConfig(blueprint);

        cleanClusterTemplateConfig(clusterTemplate);
    }

    private void cleanBlueprintConfig(Blueprint blueprint) {
        BlueprintConfiguration config = blueprint.findConfigByConfigType(ConfigClassification.YARN_SITE.getClassification());
        if (Objects.nonNull(config)) {
            config.getProperties().remove("yarn.node-labels.enabled");
            config.getProperties().remove("yarn.node-labels.configuration-type");
            config.getProperties().remove("yarn.resourcemanager.node-labels.am.default-node-label-expression");
            config.getProperties().remove("yarn.nodemanager.node-labels.provider");
            config.getProperties().remove("yarn.nodemanager.node-labels.provider.configured-node-partition");
            logger.info("清理blueprint yarn-site默认配置: yarn.node-labels.enabled \n"
                    + "yarn.node-labels.configuration-type \n"
                    + "yarn.resourcemanager.node-labels.am.default-node-label-expression \n"
                    + "yarn.nodemanager.node-labels.provider \n"
                    + "yarn.nodemanager.node-labels.provider.configured-node-partition");
        }

        config = blueprint.findConfigByConfigType(ConfigClassification.CAPACITY_SCHEDULER.getClassification());
        if (Objects.nonNull(config)) {
            config.getProperties().remove("yarn.scheduler.capacity.root.accessible-node-labels.am.capacity");
            config.getProperties().remove("yarn.scheduler.capacity.root.default.accessible-node-labels.am.capacity");
            logger.info("清理blueprint capacity-scheduler默认配置: yarn.scheduler.capacity.root.accessible-node-labels.am.capacity \n"
                            + "yarn.scheduler.capacity.root.default.accessible-node-labels.am.capacity");
        }
    }

    private void cleanClusterTemplateConfig(ClusterTemplate clusterTemplate) {
        BlueprintConfiguration config = clusterTemplate.findBlueprintConfiguration(ConfigClassification.YARN_SITE.getClassification());
        if (Objects.nonNull(config)) {
            config.getProperties().remove("yarn.nodemanager.node-labels.provider.configured-node-partition");
            config.getProperties().remove("core.am-label.enabled");

            logger.info("清理ClusterTemplate yarn-site配置: yarn.nodemanager.node-labels.provider.configured-node-partition \n"
                + "core.am-label.enabled");
        }
    }
}
