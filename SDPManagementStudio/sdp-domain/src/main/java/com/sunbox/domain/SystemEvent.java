/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import com.sunbox.domain.enums.SystemEventType;

import java.util.Date;
import java.util.UUID;

/**
 * SDP系统发生的各种事件记录，如：服务重启
 * @author wangda
 * @date 2023/7/23
 */
public class SystemEvent {
    /** 系统事件ID，UUID，不自动生成 */
    private String systemEventId;

    /** 事件触发时间 */
    private Date eventTriggerTime;

    /** 事件类型, 使用 SystemEventType 枚举中的值 */
    private String eventType;

    /** 事件说明 */
    private String eventDesc;

    public static SystemEvent build() {
        SystemEvent event = new SystemEvent();
        event.setSystemEventId(UUID.randomUUID().toString());
        return event;
    }

    public static String toCSVHeader() {
        return "Id,事件触发时间,事件类型,事件描述";
    }

    public String toCSVLine() {
        return systemEventId + "," + eventTriggerTime + "," + eventType + "," + eventDesc;
    }

    public String getSystemEventId() {
        return systemEventId;
    }

    public SystemEvent setSystemEventId(String systemEventId) {
        this.systemEventId = systemEventId;
        return this;
    }

    public Date getEventTriggerTime() {
        return eventTriggerTime;
    }

    public SystemEvent setEventTriggerTime(Date eventTriggerTime) {
        this.eventTriggerTime = eventTriggerTime;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public SystemEvent setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public SystemEvent setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
        return this;
    }
}
