/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service.impl;

import com.sunbox.dao.mapper.SystemEventMapper;
import com.sunbox.domain.SystemEvent;
import com.sunbox.domain.enums.SystemEventType;
import com.sunbox.service.ISystemEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author wangda
 * @date 2023/7/23
 */
@Service
public class SystemEventServiceImpl implements ISystemEventService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private SystemEventMapper systemEventMapper;

    @PostConstruct
    public void initCheckReboot() {
        SystemEvent systemEvent = SystemEvent.build()
                .setEventTriggerTime(new Date())
                .setEventType(SystemEventType.REBOOT.name())
                .setEventDesc("服务[" + applicationName + "]重启完成");
        systemEventMapper.insert(systemEvent);
    }

    @Override
    public void saveSystemEvent(SystemEvent systemEvent) {
        systemEventMapper.insert(systemEvent);
    }

    @Override
    public List<SystemEvent> querySystemEvent(int page, int size) {
        int limit = (page-1) * size;
        List<SystemEvent> systemEvents = systemEventMapper.selectByPage(limit, size);
        return systemEvents;
    }
}
