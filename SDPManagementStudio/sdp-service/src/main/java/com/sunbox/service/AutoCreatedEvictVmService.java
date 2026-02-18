/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.ResultMsg;

/**
 * @author wangda
 * @date 2024/7/4
 */
public interface AutoCreatedEvictVmService {

    /**
     * 处理驱逐后补足的事件
     * @param message 消息内容
     * @return
     */
    ResultMsg handleEvictVmEvent(String message);
}
