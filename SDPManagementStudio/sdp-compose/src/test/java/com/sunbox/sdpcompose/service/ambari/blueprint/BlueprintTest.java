package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/28
 */
public class BlueprintTest {

    @Test
    public void addHostGroupConfiguration() {

        Blueprint blueprint = new Blueprint();
        Map<String,Object> config = new HashMap<>();
        config.put("a", "a");
        config.put("b", "b");
        Map<String, Map<String,Object>> configMap = new HashMap<>();
        configMap.put("core-site", config);

        HostGroup hostGroup = new HostGroup();
        hostGroup.setHostGroupRole(HostGroupRole.TASK);
        hostGroup.setName("task");
        blueprint.getHostGroups().add(hostGroup);

        blueprint.addHostGroupConfiguration("task", Arrays.asList(configMap));

    }

    @Test
    public void duplicateBlueprintConfiguration() {
        Blueprint blueprint = new Blueprint();

        BlueprintConfiguration config = new BlueprintConfiguration();
        config.setConfigItemName("core-site");
        config.putProperties("a", "a");
        config.putProperties("b", "b");
        Map<String, BlueprintConfiguration> configMap = new HashMap<>();
        configMap.put("core-site", config);

        Map<String, BlueprintConfiguration> dup = blueprint.duplicateBlueprintConfiguration(configMap);
        Assertions.assertThat(dup).containsKey("core-site");
        Assertions.assertThat(dup.get("core-site")).hasFieldOrPropertyWithValue("configItemName", "core-site");
    }

    @Test
    public void removeConfigurationByConfigType() {
        BlueprintConfiguration config = new BlueprintConfiguration();
        config.setConfigItemName("core-site");
        config.putProperties("a", "a");
        config.putProperties("b", "b");
        Map<String, BlueprintConfiguration> configMap = new HashMap<>();
        configMap.put("core-site", config);
        Blueprint blueprint = new Blueprint();
        blueprint.getConfigurations().add(configMap);

        blueprint.removeConfigurationByConfigType("core-site");
        Assertions.assertThat(blueprint.getConfigurations()).isEmpty();
    }
}