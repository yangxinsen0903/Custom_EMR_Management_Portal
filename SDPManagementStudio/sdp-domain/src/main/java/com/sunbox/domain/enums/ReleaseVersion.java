/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.enums;

import cn.hutool.core.util.StrUtil;

/**
 * Stack的版本信息
 * @author wangda
 * @date 2024/6/1
 */
public enum ReleaseVersion {
    SDP_1_0("SDP-1.0"),
    SDP_2_0("SDP-2.0");

    String versionValue;

    ReleaseVersion(String versionValue) {
        this.versionValue = versionValue;
    }

    /**
     * 校验Stack版本号
     * @param version
     */
    public static void validate(String version) {
        for (ReleaseVersion releaseVersion : ReleaseVersion.values()) {
            if (StrUtil.equals(version, releaseVersion.versionValue)) {
                return;
            }
        }
        throw new RuntimeException("校验Stack版本号失败，Stack版本号不正确：" + version);
    }

    public String getVersionValue() {
        return versionValue;
    }

    public void setVersionValue(String versionValue) {
        this.versionValue = versionValue;
    }
}
