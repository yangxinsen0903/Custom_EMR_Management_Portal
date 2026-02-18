package com.sunbox.sdpcompose.service.ambari.blueprint;

/**
 * Bluesprint中的一些常量值
 * @author: wangda
 * @date: 2022/12/5
 */
public class Constants {
    /** Blueprint文件中, setting节点下的全局RecoveryEnabled配置的Key */
    public static final String RecoverySettingsGlobalKey = "recovery_settings";

    /** Blueprint文件中, setting节点下的服务RecoveryEnabled配置的Key */
    public static final String RecoverySettingsServiceKey = "service_settings";

    /** Blueprint文件中, setting节点下的大数据组件RecoveryEnabled配置的Key */
    public static final String RecoverySettingsComponentKey = "component_settings";
}
