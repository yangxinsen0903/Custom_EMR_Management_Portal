package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/3/6
 */
class ClusterTemplateHostGroupTest {

    @Test
    void addConfigurationIfNotExist() {
        BlueprintConfiguration configItem = new BlueprintConfiguration();
        configItem.setConfigItemName("yarn-site");
        configItem.putProperties("configKey", "existConfigValue");

        ClusterTemplateHostGroup hostGroup = new ClusterTemplateHostGroup();
        hostGroup.addConfiguration(Arrays.asList(configItem));

        List<BlueprintConfiguration> newConfigs = new ArrayList<>();
        BlueprintConfiguration configItem2 =  new BlueprintConfiguration();
        configItem2.setConfigItemName("yarn-site");
        configItem2.putProperties("configKey", "configValue");
        configItem2.putProperties("newConfigKey2", "newConfigValue");
        newConfigs.add(configItem2);
        hostGroup.addConfigurationIfNotExist(newConfigs);
        Map<String, Object> blueprintConfiguration = hostGroup.getConfigurations().get(0).get("yarn-site");
        Assertions.assertThat(blueprintConfiguration.get("configKey")).isEqualTo("existConfigValue");
        Assertions.assertThat(blueprintConfiguration.get("newConfigKey2")).isEqualTo("newConfigValue");
    }
}