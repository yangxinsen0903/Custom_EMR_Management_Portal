package com.sunbox.service.impl;

import com.sunbox.dao.mapper.InfoClusterVmClearLogMapper;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmClearLog;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.IVMClearLogService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author : [niyang]
 * @className : VMClearLogServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/23 3:41 PM]
 */
@Service
public class VMClearLogServiceImpl implements IVMClearLogService, BaseCommonInterFace {

    @Autowired
    private InfoClusterVmClearLogMapper infoClusterVmClearLogMapper;


    /**
     * 批量插入要清理的VM
     *
     * @param clusterId
     * @param planId
     * @param vmNames
     * @return
     */
    @Override
    public ResultMsg insertClearHosts(String clusterId, String planId, List<String> vmNames,String vmRole) {
        ResultMsg msg = new ResultMsg();

        List<String> tmpvmNames = queryVMListByPlanId(planId);
        if (tmpvmNames!=null && tmpvmNames.size() > 0){
            msg.setResult(true);
            return msg;
        }

        List<InfoClusterVmClearLog> clusterVmClearLogList = new CopyOnWriteArrayList<>();
        try {
            vmNames.stream().forEach(x -> {
                InfoClusterVmClearLog vmClearLog = new InfoClusterVmClearLog();
                vmClearLog.setClusterId(clusterId);
                vmClearLog.setStatus(InfoClusterVmClearLog.VM_Clear_Status_INIT);
                vmClearLog.setCreatedTime(new Date());
                vmClearLog.setPlanId(planId);
                vmClearLog.setVmName(x);
                vmClearLog.setVmRole(vmRole);
                clusterVmClearLogList.add(vmClearLog);
            });
            infoClusterVmClearLogMapper.insertBatchVMClearLog(clusterVmClearLogList);
            msg.setResultSucces("success");
        }catch (Exception e){
            getLogger().error("保存清理VMlog，异常，",e);
            msg.setResultFail("保存清理VMlog，异常:"+ExceptionUtils.getFullStackTrace(e));
            return msg;
        }
        return msg;
    }

    /**
     * 批量插入要清理的VM
     *
     * @param clusterId
     * @param planId
     * @param vms
     * @return
     */
    @Override
    public ResultMsg insertClearVms(String clusterId, String planId, List<InfoClusterVm> vms) {
        ResultMsg msg = new ResultMsg();
        List<String> vmNames = queryVMListByPlanId(planId);
        if (vmNames!=null && vmNames.size() > 0){
            msg.setResult(true);
            return msg;
        }
        List<InfoClusterVmClearLog> clusterVmClearLogList = new CopyOnWriteArrayList<>();
        try {
            vms.stream().forEach(x -> {
                InfoClusterVmClearLog vmClearLog = new InfoClusterVmClearLog();
                vmClearLog.setClusterId(clusterId);
                vmClearLog.setStatus(InfoClusterVmClearLog.VM_Clear_Status_INIT);
                vmClearLog.setCreatedTime(new Date());
                vmClearLog.setPlanId(planId);
                vmClearLog.setVmName(x.getVmName());
                vmClearLog.setVmRole(x.getVmRole());
                clusterVmClearLogList.add(vmClearLog);
            });
            infoClusterVmClearLogMapper.insertBatchVMClearLog(clusterVmClearLogList);
            msg.setResultSucces("success");
        }catch (Exception e){
            getLogger().error("保存清理VMlog，异常，",e);
            msg.setResultFail("保存清理VMlog，异常:"+ExceptionUtils.getFullStackTrace(e));
            return msg;
        }
        return msg;
    }

    /**
     * 根据任务ID查询要清理的VMName 列表
     *
     * @param planId
     * @return
     */
    @Override
    public List<String> queryVMListByPlanId(String planId) {
        List<String> vmNames = new CopyOnWriteArrayList<>();
        List<InfoClusterVmClearLog> vmClearLogs = infoClusterVmClearLogMapper.selectByPlanId(planId);
        vmClearLogs.stream().forEach(x->{
            vmNames.add(x.getVmName());
        });
        return vmNames;
    }


    /**
     * 根据任务ID 获取清理的VMClearLogs对象列表
     *
     * @param planId
     * @return
     */
    @Override
    public List<InfoClusterVmClearLog> queryVMClearLogsByPlanId(String planId) {
        List<InfoClusterVmClearLog> vmClearLogs = infoClusterVmClearLogMapper.selectByPlanId(planId);
        return vmClearLogs;
    }

    /**
     * 批量更新VM的Clear 状态
     *
     * @param vmNames
     * @param planId
     * @param status
     * @return
     */
    @Override
    public ResultMsg batchUpdateVmsClearStatus(List<String> vmNames, String planId, Integer status) {
        return null;
    }
}
