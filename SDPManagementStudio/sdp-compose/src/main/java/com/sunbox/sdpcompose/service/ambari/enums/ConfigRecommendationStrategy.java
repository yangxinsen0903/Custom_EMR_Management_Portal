package com.sunbox.sdpcompose.service.ambari.enums;

/**
 *
 * @author: wangda
 * @date: 2022/12/7
 */
public enum ConfigRecommendationStrategy {

    NEVER_APPLY,
    ONLY_STACK_DEFAULTS_APPLY,
    ALWAYS_APPLY,
    ALWAYS_APPLY_DONT_OVERRIDE_CUSTOM_VALUES;
}
