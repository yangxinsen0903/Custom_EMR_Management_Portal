package com.sunbox.sdpcompose.service.ambari.configgeneerator;

import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;

import java.util.List;

/**
 * 自定义配置生成器，定义一系列生成配置的抽象方法
 * @author: wangda
 * @date: 2023/2/14
 */
public interface CustomConfigGenerator {

    /**
     * 根据一个主机实例生成配置
     * @param instance 主机实例
     * @return Blueprint的配置，根据实际情况生成一个或多配置文件中的配置，如果没生成， 返回空列表。
     */
    List<BlueprintConfiguration> generate(HostInstance instance);
}
