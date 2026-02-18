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
 * 在创建集群时, 如果在yarn-site中配置了 <code>core.am-labels.enabled = false</code>，或没有设置,则会使用本类来生成关闭AM标签相关的一堆配置
 * @author wangda
 * @date 2025/2/20
 */
public class AMLabelDisabledDefaultConfigGenerator implements CustomConfigGenerator{
    Logger logger = LoggerFactory.getLogger(AMLabelDisabledDefaultConfigGenerator.class);

    @Override
    public List<BlueprintConfiguration> generate(HostInstance instance) {
        List<BlueprintConfiguration> configurations = new ArrayList<>();

        BlueprintConfiguration yarnConfig = generateYarnSiteConfig();
        if (Objects.nonNull(yarnConfig)) {
            configurations.add(yarnConfig);
        }
        return configurations;
    }

    private BlueprintConfiguration generateYarnSiteConfig() {
        logger.info("CoreAMLabelDisabled-生成Default yarn-site配置...");
        try {
            BlueprintConfiguration config = new BlueprintConfiguration(ConfigClassification.YARN_SITE.getClassification());

            config.putProperties("yarn.node-labels.enabled", "false");

            logger.info("CoreAMLabelDisabled-生成Default yarn-site配置完成, 生成内容为:{}", config.getProperties());
            return config;
        } catch (Exception ex) {
            logger.error("CoreAMLabelDisabled-生成Default yarn-site配置出错", ex);
            return null;
        }
    }

}
