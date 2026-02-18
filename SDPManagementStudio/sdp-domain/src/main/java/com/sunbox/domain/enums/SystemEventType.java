/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.enums;

/**
 * @author wangda
 * @date 2023/7/23
 */
public enum SystemEventType {
    /** 服务重启 */
    REBOOT("服务重启"),
    CLOSE("服务关闭");

    private String name;

    private SystemEventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
