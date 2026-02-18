/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import java.util.Date;
import java.util.UUID;

/**
 * SDP执行任务发生的事件，一般记录失败的事件
 * @author wangda
 * @date 2023/7/23
 */
public class TaskEvent {
    /** 任务事件ID，UUID，不自动生成 */
    private String taskEventId;

    /** 集群ID */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /** vm角色名称 */
    private String vmRole;

    /** 实例组名称 */
    private String groupName;

    /** 事件类型,见 TaskEventType 枚举 */
    private String eventType;

    /** 执行任务ID */
    private String planId;

    /** 执行任务名称 */
    private String planName;

    /** 执行任务的活动日志ID */
    private String planActivityLogId;

    /** 执行任务的活动日志名称 */
    private String planActivityLogName;

    /** 事件触发时间 */
    private Date eventTriggerTime;

    /** 事件说明 */
    private String eventDesc;

    public static TaskEvent build() {
        TaskEvent event = new TaskEvent();
        event.setTaskEventId(UUID.randomUUID().toString());
        return event;
    }

    public String getTaskEventId() {
        return taskEventId;
    }

    public TaskEvent setTaskEventId(String taskEventId) {
        this.taskEventId = taskEventId;
        return this;
    }

    public String getClusterId() {
        return clusterId;
    }

    public TaskEvent setClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public TaskEvent setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getVmRole() {
        return vmRole;
    }

    public TaskEvent setVmRole(String vmRole) {
        this.vmRole = vmRole;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public TaskEvent setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public TaskEvent setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getPlanId() {
        return planId;
    }

    public TaskEvent setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public String getPlanName() {
        return planName;
    }

    public TaskEvent setPlanName(String planName) {
        this.planName = planName;
        return this;
    }

    public String getPlanActivityLogId() {
        return planActivityLogId;
    }

    public TaskEvent setPlanActivityLogId(String planActivityLogId) {
        this.planActivityLogId = planActivityLogId;
        return this;
    }

    public String getPlanActivityLogName() {
        return planActivityLogName;
    }

    public TaskEvent setPlanActivityLogName(String planActivityLogName) {
        this.planActivityLogName = planActivityLogName;
        return this;
    }

    public Date getEventTriggerTime() {
        return eventTriggerTime;
    }

    public TaskEvent setEventTriggerTime(Date eventTriggerTime) {
        this.eventTriggerTime = eventTriggerTime;
        return this;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public TaskEvent setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
        return this;
    }
}
