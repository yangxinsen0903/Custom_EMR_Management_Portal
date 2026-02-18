package com.sunbox.sdpvmsr.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.InfoClusterVmDeleteMapper;
import com.sunbox.dao.mapper.InfoClusterVmNeoMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpvmsr.mapper.ConfClusterMapper;
import com.sunbox.sdpvmsr.mapper.InfoClusterVmMapper;
import com.sunbox.sdpvmsr.service.IVMDeleteProcessService;
import com.sunbox.service.IAzureService;
import com.sunbox.util.DateUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author : [niyang]
 * @className : VMDeleteProcessServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/30 11:22 PM]
 */
@Service
public class VMDeleteProcessServiceImpl  implements IVMDeleteProcessService, BaseCommonInterFace {

    @Autowired
    private IAzureService azureService;

    @Autowired
    private InfoClusterVmDeleteMapper infoClusterVmDeleteMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmNeoMapper;

    /**
     * 发送删除请求
     *
     * @param vmDelete
     */
    @Override
    public void deleteVMSendReq(InfoClusterVmDelete vmDelete) {
        try {
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(vmDelete.getClusterId());

            if (confCluster.getState().equals(ConfCluster.DELETING)){
                // 删除中 跳过
                getLogger().info("{}集群删除中,跳过",vmDelete.getClusterId());
               return;
            }
            if (confCluster.getState().equals(ConfCluster.DELETED)){
                vmDelete.setStatus(InfoClusterVmDelete.STATUS_DELETE_SUCCESS);
                vmDelete.setModifiedTime(new Date());
                infoClusterVmDeleteMapper.updateByPrimaryKeySelective(vmDelete);
                getLogger().info("{}集群已删除,更新状态已删除。",vmDelete.getClusterId());
                return;
            }

            InfoClusterVm infoClusterVm = infoClusterVmNeoMapper.selectByPrimaryKey(confCluster.getClusterId(), vmDelete.getVmName());
            ResultMsg msg = azureService.deleteVMInstance(confCluster.getClusterName(), vmDelete.getVmName(), infoClusterVm.getHostName(), vmDelete.getRegion());
            if (msg.getResult() && msg.getData()!=null){
                JSONObject response = (JSONObject) msg.getData();
                String jobId = response.getString("id");
                vmDelete.setStatus(InfoClusterVmDelete.STATUS_DELETING);
                vmDelete.setJobId(jobId);
                vmDelete.setModifiedTime(new Date());
                vmDelete.setGetDeleteJobidTime(new Date());
                infoClusterVmDeleteMapper.updateByPrimaryKeySelective(vmDelete);
            }else{
                getLogger().error("发送单个删除VM请求异常，vmName:{},异常：{}",vmDelete.getVmName(),msg);
                deleteVMFailed(vmDelete);
            }
        }catch (Exception e){
            getLogger().error("发送删除请求异常，",e);
        }
    }

    /**
     * 删除失败处理逻辑
     *
     * @param vmDelete
     */
    @Override
    public void deleteVMFailed(InfoClusterVmDelete vmDelete){
         if (vmDelete.getRetryCount() == null || vmDelete.getRetryCount()< 20){
             //重试逻辑
             if (vmDelete.getRetryCount()==null){
                 vmDelete.setRetryCount(0);
             }
             vmDelete.setRetryCount(vmDelete.getRetryCount()+1);
             vmDelete.setStatus(InfoClusterVmDelete.STATUS_INIT);
             infoClusterVmDeleteMapper.updateByPrimaryKeySelective(vmDelete);
         }else{
             //冻结逻辑
             vmDelete.setStatus(InfoClusterVmDelete.STATUS_FREEZE);
             vmDelete.setReleaseFreezeTime(DateUtil.dateAddOrSubHour(new Date(),1));
             vmDelete.setFreezeCount((vmDelete.getFreezeCount()==null?0:vmDelete.getFreezeCount())+1);
             infoClusterVmDeleteMapper.updateByPrimaryKeySelective(vmDelete);
         }
    }

    /**
     * 查询删除结果
     *
     * @param vmDelete
     */
    @Override
    public void deleteJobQuery(InfoClusterVmDelete vmDelete) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(vmDelete.getClusterId());
        ResultMsg msg = azureService.getJobsStatusWithRequestTimeout(confCluster.getRegion(), vmDelete.getJobId());
        if (msg.getData() != null) {
            JSONObject response = (JSONObject) msg.getData();
            if (response.containsKey("status")) {
                if (response.getString("status").equalsIgnoreCase("Completed")) {
                   vmDelete.setStatus(InfoClusterVmDelete.STATUS_DELETE_SUCCESS);
                   vmDelete.setModifiedTime(new Date());
                   infoClusterVmDeleteMapper.updateByPrimaryKeySelective(vmDelete);
                   // 更新InfoclusterVM表
                   if (StringUtils.isNotEmpty(vmDelete.getClusterId())
                           && StringUtils.isNotEmpty(vmDelete.getVmName())){
                       infoClusterVmMapper.updateVMStateByClusterIdAndVMName(vmDelete.getClusterId(),vmDelete.getVmName(),InfoClusterVm.VM_DELETED);
                   }
                }

                if (response.getString("status").equalsIgnoreCase("Failed")) {
                    deleteVMFailed(vmDelete);
                }
            }
        }
    }
}
