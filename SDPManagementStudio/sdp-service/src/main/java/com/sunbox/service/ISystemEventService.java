/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.SystemEvent;

import java.util.List;

/**
 * @author wangda
 * @date 2023/7/23
 */
public interface ISystemEventService {


    void saveSystemEvent(SystemEvent systemEvent);

    List<SystemEvent> querySystemEvent(int page, int size);
}
