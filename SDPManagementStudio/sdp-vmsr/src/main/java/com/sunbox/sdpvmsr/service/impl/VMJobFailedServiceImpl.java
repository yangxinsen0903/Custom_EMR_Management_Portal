package com.sunbox.sdpvmsr.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sunbox.dao.mapper.InfoClusterVmDeleteMapper;
import com.sunbox.dao.mapper.InfoClusterVmReqJobFailedMapper;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmReqJobFailed;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.azure.JobQueryResponse;
import com.sunbox.domain.azure.VirtualMachineGroupResponse;
import com.sunbox.domain.azure.VirtualMachineResponse;
import com.sunbox.sdpvmsr.mapper.ConfClusterMapper;
import com.sunbox.sdpvmsr.service.IVMJobFailedService;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IVMDeleteService;
import com.sunbox.service.IVMService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author : [niyang]
 * @className : VMJobFailedServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/1 2:41 PM]
 */
@Service
public class VMJobFailedServiceImpl implements IVMJobFailedService, BaseCommonInterFace {

    @Autowired
    private IAzureService azureService;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private IVMDeleteService ivmDeleteService;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmReqJobFailedMapper vmReqJobFailedMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Override
    public boolean processVMReqJob(InfoClusterVmReqJobFailed failedJob) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(failedJob.getClusterId());
        if (confCluster.isDeleted()){
            failedJob.setStatus(InfoClusterVmReqJobFailed.STATUS_COMPLETED);
            vmReqJobFailedMapper.updateByPrimaryKeySelective(failedJob);
            getLogger().info("集群已经被删除, 不进行VM清理. clusterId={}, clusterName={}, state={}",
                    confCluster.getClusterId(), confCluster.getClusterName(), confCluster.getStateStr());
            // 集群如果已经被删除, 直接返回处理成功,因为不需要处理了.
            return true;
        }

        // 原来是通过/provisionDetail接口来获取成功和失败的VM, 新版Azure Fleet接口
        ResultMsg jobResult = azureService.getJobsStatusWithRequestTimeout(confCluster.getRegion(), failedJob.getJobId());
        if (Objects.isNull(jobResult.getData())) {
            getLogger().error("从Azure查询Job执行结果,返回结果为空,退出本次处理");
            return false;
        }

        try {
            // 得到响应对象,准备后续操作
            JSONObject response = (JSONObject) jobResult.getData();
            JobQueryResponse jobResp = response.toJavaObject(JobQueryResponse.class);

            List<InfoClusterVm> deleteVms = new ArrayList<>();
            for (VirtualMachineResponse vm : jobResp.getAllVms()) {
                InfoClusterVm infoClusterVm = new InfoClusterVm();
                infoClusterVm.setClusterId(failedJob.getClusterId());
                infoClusterVm.setVmName(vm.getName());
                deleteVms.add(infoClusterVm);
            }

            getLogger().info("开始异步清理VM, 数量={}", deleteVms.size());
            // 因为异步删除设定超过20个就不能异步清理, 所以在此处一个VM一个VM的删
            for (InfoClusterVm deleteVm : deleteVms) {
                ivmDeleteService.saveToAsyncDelete(confCluster.getRegion(),
                        Arrays.asList(deleteVm),
                        null);
            }

            failedJob.setStatus(InfoClusterVmReqJobFailed.STATUS_COMPLETED);
            failedJob.setModifiedTime(new Date());
            vmReqJobFailedMapper.updateByPrimaryKeySelective(failedJob);
        }catch (Exception e){
            getLogger().error("处理processVMReqJob异常。",e);
        }
        return false;
    }

    /**
     * 备份原来的实现方式, 新修改的方法若出问题, 可以参照原来正确的方式查找问题<br/>
     * 新方法为:processVMReqJob(InfoClusterVmReqJobFailed failedJob)
     * @param failedJob
     * @return
     */
    private boolean processVMReqJobBak(InfoClusterVmReqJobFailed failedJob) {
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(failedJob.getClusterId());
        if (confCluster.getState().equals(ConfCluster.DELETED)
                || confCluster.getState().equals(ConfCluster.DELETING)){
            failedJob.setStatus(InfoClusterVmReqJobFailed.STATUS_COMPLETED);
            vmReqJobFailedMapper.updateByPrimaryKeySelective(failedJob);
        }

        JSONObject provisionDetail =
                ivmService.getProvisionDetail(failedJob.getJobId(),confCluster.getClusterName(),confCluster.getRegion());
        if (provisionDetail==null){
            getLogger().error("未获取到detail");
            return false;
        }
        JSONArray vmGroups = null;
        JSONArray failedVMs = null;

        if (provisionDetail.containsKey("provisionedVmGroups")){
            vmGroups = provisionDetail.getJSONArray("provisionedVmGroups");
        }

        if (provisionDetail.containsKey("failedVMs")){
            failedVMs = provisionDetail.getJSONArray("failedVMs");
        }

        try {
            if (vmGroups!=null) {
                List<VirtualMachineGroupResponse> vmdata = vmGroups.toJavaObject(new TypeReference<List<VirtualMachineGroupResponse>>() {
                });

                List<InfoClusterVm> suvms = new CopyOnWriteArrayList<>();
                vmdata.stream().forEach(d -> {
                    d.getVirtualMachines().stream().forEach(vm -> {
                        InfoClusterVm vm1 = new InfoClusterVm();
                        vm1.setClusterId(failedJob.getClusterId());
                        vm1.setVmName(vm.getName());
                        suvms.add(vm1);
                    });
                });
                getLogger().info("删除申请成功且未取回的机器:{}", suvms);
                if (suvms != null && suvms.size() > 0) {
                    ivmDeleteService.saveToAsyncDelete(confCluster.getRegion(), suvms, null);
                }
            }

            if (failedVMs !=null) {
                List<InfoClusterVm> failedVmList = new CopyOnWriteArrayList<>();

                for (int i = 0; i < failedVMs.size(); i++) {
                    InfoClusterVm vm1 = new InfoClusterVm();
                    vm1.setClusterId(failedJob.getClusterId());
                    vm1.setVmName(failedVMs.getString(i));
                    failedVmList.add(vm1);
                }
                getLogger().info("删除申请失败的机器:{}", failedVmList);
                if (failedVmList != null && failedVmList.size() > 0) {
                    ivmDeleteService.saveToAsyncDelete(confCluster.getRegion(), failedVmList, null);
                }

                failedJob.setStatus(InfoClusterVmReqJobFailed.STATUS_COMPLETED);
                failedJob.setModifiedTime(new Date());
            }

            vmReqJobFailedMapper.updateByPrimaryKeySelective(failedJob);

        }catch (Exception e){
            getLogger().error("处理processVMReqJob异常。",e);
        }
        return false;
    }


    @Scheduled(fixedDelay = 30000L)
    @Override
    public void batchProcessVMFailedJob() {
        String lockKey = "batchProcessVMFailedJob";
        boolean lock = redisLock.tryLock(lockKey, TimeUnit.SECONDS,1,600);
        try{
            if (!lock){
                getLogger().info("未获取到需要处理失败Job锁失败，退出，等待下一周期。");
                return;
            }
            List<InfoClusterVmReqJobFailed> vmReqJobFailedList =
                    vmReqJobFailedMapper.getAllByStatus(InfoClusterVmReqJobFailed.STATUS_INIT);
            getLogger().info("获取到需要处理的Job",vmReqJobFailedList.toString());
            for (InfoClusterVmReqJobFailed failedvm: vmReqJobFailedList){
                processVMReqJob(failedvm);
            }
        }catch (Exception e){
            getLogger().error("批量处理失败的getvmJob异常，",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }
    }
}
