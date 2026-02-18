/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.TaskEvent;

/**
 * TaskEventService接口定义
 * @author wangda
 * @date 2023/7/23
 */
public interface ITaskEventService {

    int saveTaskEvent(TaskEvent taskEvent);
}
