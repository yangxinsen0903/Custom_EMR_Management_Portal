/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.service;

import com.sunbox.domain.DiffVm;

import java.util.List;

/**
 * 运维相关的服务
 * @author wangda
 * @date 2023/7/25
 */
public interface OpsService {

    /**
     * 生成SDP与YARN的差异虚拟机
     * @return
     */
    List<DiffVm> generateSdpYarnDiffVms();
}
