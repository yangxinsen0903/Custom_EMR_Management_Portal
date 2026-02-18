package com.sunbox.sdpcompose.service.ambari.configgeneerator;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Task组的NodeManager资源配置，主要是CPU和内存
 * @author: wangda
 * @date: 2023/2/14
 */
public class TaskNodeManagerResourceConfigGenerator implements CustomConfigGenerator{
    Logger logger = LoggerFactory.getLogger(TaskNodeManagerResourceConfigGenerator.class);

    private static Map<Integer, Integer> cpuResourceSetting = new HashMap<>();

    private static Map<Integer, Integer> memoryResourceSetting = new HashMap<>();

    static {
        cpuResourceSetting.put(8, 8);
        cpuResourceSetting.put(16, 16);
        cpuResourceSetting.put(32, 32);
        cpuResourceSetting.put(64, 64);
        cpuResourceSetting.put(96, 96);

        memoryResourceSetting.put(16, 10);
        memoryResourceSetting.put(32, 25);
        // 空闲：11    53
        memoryResourceSetting.put(64, 52);
        // 空闲：30    98
        memoryResourceSetting.put(128, 95);
        // 空闲：16   240
        memoryResourceSetting.put(256, 235);
        // 空闲：8    376
        memoryResourceSetting.put(384, 365);
        // 空闲：8   504
        memoryResourceSetting.put(512, 490);
        // 空闲：20  652
        memoryResourceSetting.put(672, 640);
    }

    @Override
    public List<BlueprintConfiguration> generate(HostInstance instance) {
        logger.info("generate by NodeManagerResourceConfigGenerator, instance:{}", instance);
        if (Objects.isNull(instance)) {
            return new ArrayList<>();
        }

        try {
            BlueprintConfiguration config = new BlueprintConfiguration("yarn-site");

            // 计算CPU
            Integer vCpuCount = instance.getvCpu();
            Integer actualCpuCount = vCpuCount;
            config.putProperties("yarn.nodemanager.resource.cpu-vcores", String.valueOf(actualCpuCount));

            // 计算内存
            Integer memory = memoryResourceSetting.get(instance.getMemoryGB());
            if (Objects.isNull(memory)) {
                // 默认是80%，如果计算失败，取8G
                memory = Convert.toInt(Math.round(instance.getMemoryGB() * 0.8), 8);
            }
            config.putProperties("yarn.nodemanager.resource.memory-mb", String.valueOf(memory * 1024));

            logger.info("set instance:{} properties yarn.nodemanager.resource.cpu-vcores={}, yarn.nodemanager.resource.memory-mb={}",
                    instance.getHostName(),
                    actualCpuCount,
                    memory * 1024);

            return Lists.newArrayList(config);
        } catch (Exception ex) {
            logger.error("计算Yarn NodeManager的资源配置值出错：" + JSON.toJSONString(instance), ex);
            return new ArrayList<>();
        }
    }
}
