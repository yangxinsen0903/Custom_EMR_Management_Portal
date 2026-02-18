package com.sunbox.sdpcompose.service.ambari.enums;

import java.util.EnumSet;

/**
 * HA场景
 * @author: wangda
 * @date: 2022/12/31
 */
public enum HAScene {
    /** HA场景 */
    HA,
    /** 非HA场景 */
    NON_HA,
    /** 所有场景,包括HA和非HA */
    ALL;

    /** HA场景 */
    public static EnumSet HA_SET = EnumSet.of(HAScene.HA, HAScene.ALL);

    /** 非HA场景 */
    public static EnumSet NON_HA_SET = EnumSet.of(HAScene.NON_HA, HAScene.ALL);

}
