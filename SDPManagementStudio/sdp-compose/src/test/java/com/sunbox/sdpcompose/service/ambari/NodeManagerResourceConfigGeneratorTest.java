package com.sunbox.sdpcompose.service.ambari;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;
import com.sunbox.sdpcompose.service.ambari.configgeneerator.CoreNodeManagerResourceConfigGenerator;
import com.sunbox.sdpcompose.service.ambari.configgeneerator.TaskNodeManagerResourceConfigGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/2/14
 */
public class NodeManagerResourceConfigGeneratorTest  {
    TaskNodeManagerResourceConfigGenerator generator = new TaskNodeManagerResourceConfigGenerator();
    CoreNodeManagerResourceConfigGenerator generator1 = new CoreNodeManagerResourceConfigGenerator();

    @Test
    public void testGenerate_标准机型() {

        HostInstance instance = new HostInstance();
        instance.setvCpu(8);
        instance.setMemoryGB(64);
        List<BlueprintConfiguration> configList = generator1.generate(instance);
        BlueprintConfiguration config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 8)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 53*1024);

        instance.setvCpu(16);
        instance.setMemoryGB(128);
        configList = generator.generate(instance);
        config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 16)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 98*1024);

        instance.setvCpu(32);
        instance.setMemoryGB(256);
        configList = generator.generate(instance);
        config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 32)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 240*1024);


        instance.setvCpu(48);
        instance.setMemoryGB(384);
        configList = generator.generate(instance);
        config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 48)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 376*1024);


        instance.setvCpu(64);
        instance.setMemoryGB(512);
        configList = generator.generate(instance);
        config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 64)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 504*1024);

        instance.setvCpu(96);
        instance.setMemoryGB(672);
        configList = generator.generate(instance);
        config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 96)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 652*1024);


    }

    @Test
    public void testGenerate_自定义机型() {

        HostInstance instance = new HostInstance();
        instance.setvCpu(9);
        instance.setMemoryGB(48);
        List<BlueprintConfiguration> configList = generator.generate(instance);
        BlueprintConfiguration config = configList.get(0);
        Assertions.assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("configItemName", "yarn-site");
        Assertions.assertThat(config.getProperties()).containsEntry("yarn.nodemanager.resource.cpu-vcores", 9)
                .containsEntry("yarn.nodemanager.resource.memory-mb", 38*1024);

    }
}