package com.sunbox.sdpcompose.service.ambari.configgeneerator;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Core组的NodeManager资源配置，主要是CPU和内存
 */
public class CoreNodeManagerResourceConfigGenerator implements CustomConfigGenerator {
    Logger logger = LoggerFactory.getLogger(CoreNodeManagerResourceConfigGenerator.class);

    @Override
    public List<BlueprintConfiguration> generate(HostInstance instance) {
        logger.info("generate by CoreNodeManagerResourceConfigGenerator, instance:{}", instance);
        if (Objects.isNull(instance)) {
            return new ArrayList<>();
        }

        List<BlueprintConfiguration> blueprintConfigurations = new ArrayList<>();

        BlueprintConfiguration config = tryGenerateForYarnSite(instance);
        if (config != null) {
            blueprintConfigurations.add(config);
        }

        config = tryGenerateForYarnEnv(instance);
        if (config != null) {
            blueprintConfigurations.add(config);
        }

        return blueprintConfigurations;
    }

    private BlueprintConfiguration tryGenerateForYarnSite(HostInstance instance) {
        logger.info("tryGenerateForYarnSite instance:{}", instance);
        try {
            BlueprintConfiguration config = new BlueprintConfiguration("yarn-site");

            //region 计算CPU
            // yarn.nodemanager.resource.cpu-vcores
            //   = CPU - Operation System(1C) - HDFS DataNode(1C) - YARN NodeManager(1C)
            int cpuVcores = 0;
            if (instance.getvCpu() < 4) {
                cpuVcores = instance.getvCpu();
            } else {
                cpuVcores = instance.getvCpu() - 3;
            }
            config.putProperties("yarn.nodemanager.resource.cpu-vcores", String.valueOf(cpuVcores));
            //endregion

            //region 计算内存
            // yarn.nodemanager.resource.memory-mb
            //   = RAM - Operation System(4G~8G) - HDFS DataNode(6G) - YARN NodeManager(6G)
            int memoryMB = 0;

            if (instance.getMemoryGB() < 8) {
                memoryMB = (instance.getMemoryGB() - 1 - 1 - 1) * 1024;
            } else if (instance.getMemoryGB() < 16) {
                memoryMB = (instance.getMemoryGB() - 2 - 2 - 2) * 1024;
            } else if (instance.getMemoryGB() < 32) {
                memoryMB = (instance.getMemoryGB() - 4 - 4 - 4) * 1024;
            } else if (instance.getMemoryGB() < 512) {
                memoryMB = (instance.getMemoryGB() - 8 - 6 - 6) * 1024;
            } else {
                memoryMB = ((int) (instance.getMemoryGB() * 0.75)) * 1024;
            }
            config.putProperties("yarn.nodemanager.resource.memory-mb", String.valueOf(memoryMB));

            // yarn.scheduler.maximum-allocation-mb
            //   = yarn.nodemanager.resource.memory-mb
            config.putProperties("yarn.scheduler.maximum-allocation-mb", String.valueOf(memoryMB));
            //endregion

            logger.info("set instance:{} yarn-site\n" +
                            "yarn.nodemanager.resource.cpu-vcores={},\n" +
                            "yarn.nodemanager.resource.memory-mb={},\n" +
                            "yarn.scheduler.maximum-allocation-mb={}",
                    instance.getHostName(),
                    cpuVcores,
                    memoryMB,
                    memoryMB);
            return config;
        } catch (Exception ex) {
            logger.error("tryGenerateForYarnSite error, instance:" + instance, ex);
            return null;
        }
    }

    private BlueprintConfiguration tryGenerateForYarnEnv(HostInstance instance) {
        logger.info("tryGenerateForYarnEnv instance:{}", instance);
        try {
            BlueprintConfiguration config = new BlueprintConfiguration("yarn-env");
            int nodeManagerHeapSize = 0;
            int resourceManagerHeapSize = 0;
            if (instance.getMemoryGB() < 8) {
                nodeManagerHeapSize = 1;
                resourceManagerHeapSize = 1;
            } else if (instance.getMemoryGB() < 16) {
                nodeManagerHeapSize = 2;
                resourceManagerHeapSize = 2;
            } else if (instance.getMemoryGB() < 32) {
                nodeManagerHeapSize = 4;
                resourceManagerHeapSize = 4;
            } else if (instance.getMemoryGB() < 512) {
                nodeManagerHeapSize = 6;
                resourceManagerHeapSize = 6;
            } else {
                nodeManagerHeapSize = (int) (instance.getMemoryGB() * 0.75 * 0.33);
                resourceManagerHeapSize = nodeManagerHeapSize;
            }

            // 计算CPU
            // nodemanager_heapsize
            config.putProperties("nodemanager_heapsize", String.valueOf(nodeManagerHeapSize * 1024));

            // 计算内存
            // resourcemanager_heapsize
            config.putProperties("resourcemanager_heapsize", String.valueOf(resourceManagerHeapSize * 1024));

            logger.info("set instance:{} properties nodemanager_heapsize={}, resourcemanager_heapsize={}",
                    instance.getHostName(),
                    nodeManagerHeapSize,
                    resourceManagerHeapSize);
            return config;
        } catch (Exception ex) {
            logger.error("tryGenerateForYarnEnv error, instance:" + instance, ex);
            return null;
        }
    }
}