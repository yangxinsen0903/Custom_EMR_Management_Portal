/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.enums;

/**
 * 虚拟机差异类型
 * @author wangda
 * @date 2023/7/25
 */
public enum VmDiffType {
    /** SDP与YARN的数据比对 */
    SDP_YARN("SDP与YARN比对"),
    /** AZURE与SDP的数据比对 */
    AZURE_SDP("Azure与SDP比对"),
    /** AZURE与YARN的数据比对 */
    AZURE_YARN("Azure与YARN比对");

    private String desc;

    private VmDiffType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
