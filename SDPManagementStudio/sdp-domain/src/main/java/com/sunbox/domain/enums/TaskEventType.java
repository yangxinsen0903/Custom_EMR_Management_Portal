/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.enums;

import java.util.EnumSet;

/**
 * SDP执行任务发生的事件类型
 * @author wangda
 * @date 2023/7/23
 */
public enum TaskEventType {
    /** 创建集群失败 */
    CREATE_CLUSTER_FAIL("创建集群失败"),
    /** 创建集群超时 */
    CREATE_CLUSTER_TIMEOUT("创建集群超时"),

    /** 竞价逐出失败 */
    SPOT_SCALEIN_FAIL("竞价逐出失败"),

    /** 竞价逐出超时 */
    SPOT_SCALEIN_TIMEOUT("竞价逐出超时"),

    /** 弹性缩容失败 */
    ELASTIC_SCALEIN_FAIL("弹性缩容失败"),

    /** 弹性缩容超时 */
    ELASTIC_SCALEIN_TIMEOUT("弹性缩容超时"),

    /** 手动缩容失败 */
    MANUAL_SCALEIN_FAIL("手动缩容失败"),

    /** 手动缩容超时 */
    MANUAL_SCALEIN_TIMEOUT("手动缩容超时"),
    /** 清理VM失败 */
    CLEAN_VM_FAIL("清理异常VM失败"),
    /** 清理VM超时 */
    CLEAN_VM_TIMEOUT("清理异常VM超时"),

    /** 竞价买入失败 */
    SPOT_SCALEOUT_FAIL("竞价买入失败"),

    /** 竞价买入超时 */
    SPOT_SCALEOUT_TIMEOUT("竞价买入超时"),

    /** 弹性扩容失败 */
    ELASTIC_SCALEOUT_FAIL("弹性扩容失败"),

    /** 弹性扩容超时 */
    ELASTIC_SCALEOUT_TIMEOUT("弹性扩容超时"),

    /** 手动扩容失败 */
    MANUAL_SCALEOUT_FAIL("手动扩容失败"),

    /** 手动扩容超时 */
    MANUAL_SCALEOUT_TIMEOUT("手动扩容超时")
    ;

    public static EnumSet<TaskEventType> SCALEIN_TASK_SET = EnumSet.of(SPOT_SCALEIN_FAIL, SPOT_SCALEIN_TIMEOUT,
            ELASTIC_SCALEIN_FAIL, ELASTIC_SCALEIN_TIMEOUT,
            MANUAL_SCALEIN_FAIL, MANUAL_SCALEIN_TIMEOUT);

    public static EnumSet<TaskEventType> CLEAN_TASK_SET = EnumSet.of(CLEAN_VM_FAIL, CLEAN_VM_TIMEOUT);

    private String desc;


    private TaskEventType(String desc) {
        this.desc = desc;
    }

    /**
     * 获取事件类型名称
     * @return
     */
    public String getDesc() {
        return desc;
    }
}
