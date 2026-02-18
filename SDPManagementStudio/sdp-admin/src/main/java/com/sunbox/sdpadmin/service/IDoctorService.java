/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.service;

import com.sunbox.domain.InfoThirdApiFailedLog;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;

import java.util.List;
import java.util.Map;

/**
 * 运维监控服务接口
 * @author wangda
 * @date 2023/8/5
 */
public interface IDoctorService {

    /**
     * 获取第三方接口调用失败日志列表
     * @param params 查询参数
     * @return
     */
    List<InfoThirdApiFailedLog> getThridApiFailedLogList(Map params);

    /**
     * 获取第三方接口调用失败日志总数
     * @param params
     * @return
     */
    Integer getThridApiFailedLogCount(Map params);
    /**
     * 根据id获取第三方接口调用失败日志
     * @param id 日志ID
     * @return
     */
    InfoThirdApiFailedLog getThridApiFailedLogById(Long id);
    /**
     * 查询Azure端机器清理任务,Azure端僵尸机清理任务
     * @param request
     * @return
     */
    ResultMsg listAzureVm(AzureVmtraceInfoRequest request);
}
