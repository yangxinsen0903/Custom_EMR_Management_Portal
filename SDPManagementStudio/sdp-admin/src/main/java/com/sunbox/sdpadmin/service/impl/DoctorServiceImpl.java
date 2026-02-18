/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.convert.Convert;
import com.sunbox.dao.mapper.AzureVmtraceInfoMapper;
import com.sunbox.dao.mapper.InfoThirdApiFailedLogMapper;
import com.sunbox.domain.InfoThirdApiFailedLog;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.azure.AzureVmtraceInfo;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;
import com.sunbox.sdpadmin.service.IDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangda
 * @date 2023/8/5
 */
@Component
public class DoctorServiceImpl implements IDoctorService {

    @Autowired
    InfoThirdApiFailedLogMapper infoThirdApiFailedLogMapper;

    @Autowired
    private AzureVmtraceInfoMapper azureVmtraceInfoMapper;

    @Override
    public List<InfoThirdApiFailedLog> getThridApiFailedLogList(Map params) {
        Integer pageIndex = Convert.toInt(params.get("pageIndex"), 1);
        Integer pageSize = Convert.toInt(params.get("pageSize"), 20);
        Integer pageStart = (pageIndex-1) * pageSize;
        params.put("pageStart", pageStart);
        return infoThirdApiFailedLogMapper.getListByParam(params);
    }

    @Override
    public Integer getThridApiFailedLogCount(Map params) {
        return infoThirdApiFailedLogMapper.getCountByParam(params);
    }

    @Override
    public InfoThirdApiFailedLog getThridApiFailedLogById(Long id) {
        return infoThirdApiFailedLogMapper.selectByPrimaryKey(id);
    }

    @Override
    public ResultMsg listAzureVm(AzureVmtraceInfoRequest request) {
        request.page();
        int total = azureVmtraceInfoMapper.selectTotal(request);
        ResultMsg result = new ResultMsg();
        result.setResult(true);
        if (total <= 0) {
            result.setData(new ArrayList<>());
            result.setTotal(total);
            return result;
        }
        List<AzureVmtraceInfo> azureVmtraceInfos = azureVmtraceInfoMapper.selectByPage(request);
        result.setData(azureVmtraceInfos);
        result.setTotal(total);
        return result;
    }
}
