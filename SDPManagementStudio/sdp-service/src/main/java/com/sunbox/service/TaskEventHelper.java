/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.enums.TaskEventType;

/**
 * 任务事件助手类,用于帮助处理任务事件相关的功能.
 * @author wangda
 * @date 2023/7/26
 */
public class TaskEventHelper {

    /** 超时 */
    public static int TIMEOUT = -1;

    /** 失败 */
    public static int FAIL = -2;

    /**
     * 根据任务名称 和 结果类型 解析出任务事件类型<br/>
     * 目前支持: 清理异常VM, 手动缩容, 弹性缩容, 竞价逐出<br/>
     * 更新信息,参见:PlanExecServiceImpl.getPlanName()方法
     * @param planName 计划中文名称
     * @param resultType 参见:TaskEventHelper.TIMEOUT, TaskEventHelper.FAIL
     * @return 参见TaskEventType, 如果没有匹配到, 则返回null
     */
    public static TaskEventType parseDestroyTaskType(String planName, int resultType) {
        if ("清理异常VM".equals(planName) && resultType == FAIL) {
            return TaskEventType.CLEAN_VM_FAIL;
        } else if ("清理异常VM".equals(planName) && resultType == TIMEOUT) {
            return TaskEventType.CLEAN_VM_TIMEOUT;
        } else if ("手动缩容".equals(planName) && resultType == FAIL) {
            return TaskEventType.MANUAL_SCALEIN_FAIL;
        } else if ("手动缩容".equals(planName) && resultType == TIMEOUT) {
            return TaskEventType.MANUAL_SCALEIN_TIMEOUT;
        } else if ("弹性缩容".equals(planName) && resultType == FAIL) {
            return TaskEventType.ELASTIC_SCALEIN_FAIL;
        } else if ("弹性缩容".equals(planName) && resultType == TIMEOUT) {
            return TaskEventType.ELASTIC_SCALEIN_TIMEOUT;
        } else if ("竞价逐出".equals(planName) && resultType == FAIL) {
            return TaskEventType.SPOT_SCALEIN_FAIL;
        } else if ("竞价逐出".equals(planName) && resultType == TIMEOUT) {
            return TaskEventType.SPOT_SCALEIN_TIMEOUT;
        }

        return null;
    }
}
