package com.sunbox.service;

import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmClearLog;
import com.sunbox.domain.ResultMsg;

import java.util.List;


public interface IVMClearLogService {
    /**
     *  批量插入要清理的VM
     * @param clusterId
     * @param planId
     * @param vmNames
     * @param vmRole 
     * @return
     */
    ResultMsg insertClearHosts(String clusterId, String planId, List<String> vmNames,String vmRole);


    /**
     *  批量插入要清理的VM
     * @param clusterId
     * @param planId
     * @param vms
     * @return
     */
    ResultMsg insertClearVms(String clusterId, String planId, List<InfoClusterVm> vms);

    /**
     * 根据任务ID查询要清理的VMName 列表
     * @param planId
     * @return
     */
    List<String> queryVMListByPlanId(String planId);

    /**
     * 根据任务ID 获取清理的VMClearLogs对象列表
     * @param planId
     * @return
     */
    List<InfoClusterVmClearLog> queryVMClearLogsByPlanId(String planId);

    /**
     *  批量更新VM的Clear 状态
     * @param vmNames
     * @param planId
     * @param status
     * @return
     */
    ResultMsg batchUpdateVmsClearStatus(List<String> vmNames,String planId,Integer status);

}
