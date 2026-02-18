/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service.impl;

import com.sunbox.dao.mapper.TaskEventMapper;
import com.sunbox.domain.TaskEvent;
import com.sunbox.service.ITaskEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangda
 * @date 2023/7/23
 */
@Service
public class TaskEventServiceImpl implements ITaskEventService {

    @Autowired
    private TaskEventMapper taskEventMapper;

    @Override
    public int saveTaskEvent(TaskEvent taskEvent) {
        return taskEventMapper.insert(taskEvent);
    }
}
