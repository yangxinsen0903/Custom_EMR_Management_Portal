package com.sunbox.sdpcompose.service.ambari.enums;

/**
 * 配置项类型，高可用场景配置 or  非高可用场景配置
 * @author: wangda
 * @date: 2022/12/11
 */
public enum ConfigItemType {
    /** 非高可用场景配置 */
    NON_HA(0),
    /** 高可用场景配置 */
    HA(1);

    public static ConfigItemType parse(Integer id) {
        for (ConfigItemType type : ConfigItemType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public boolean isHa() {
        return this == HA;
    }

    public Integer getId() {
        return this.id;
    }
    private final Integer id;

    ConfigItemType(Integer id) {
        this.id = id;
    }
}
