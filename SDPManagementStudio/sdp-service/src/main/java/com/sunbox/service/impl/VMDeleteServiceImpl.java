package com.sunbox.service.impl;

import com.sunbox.dao.mapper.InfoClusterVmDeleteMapper;
import com.sunbox.dao.mapper.InfoClusterVmReqJobFailedMapper;
import com.sunbox.domain.*;
import com.sunbox.service.IVMDeleteService;
import com.sunbox.util.DateUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author : [niyang]
 * @className : VMDeleteServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/30 8:15 PM]
 */
@Service
public class VMDeleteServiceImpl implements IVMDeleteService, BaseCommonInterFace {

    @Value("${sdp.vm.delete.async.max:20}")
    private Integer sdpVMDeleteAsyncMax;

    @Value("${sdp.vm.senddelete.each.count:50}")
    private Integer sdpSendDeleteEachCount;


    @Autowired
    private InfoClusterVmDeleteMapper infoClusterVmDeleteMapper;

    @Autowired
    private InfoClusterVmReqJobFailedMapper vmReqJobFailedMapper;

    /**
     * 保存vm到异步数据删除
     *
     * @param vms
     * @return 返回true 为保存成功 false 保存失败
     */
    @Override
    public ResultMsg saveToAsyncDelete(String region, List<InfoClusterVm> vms, InfoClusterOperationPlan plan) {
        ResultMsg msg = new ResultMsg();
        try {
            getLogger().info("需要删除的VM：{}",vms);
            // 判断是否可以异步删除, 按需类型大于阈值 直接使用原有的逻辑
            if (vms!=null && vms.size()>sdpVMDeleteAsyncMax && vms.get(0).getPurchaseType()!=null
                    && vms.get(0).getPurchaseType().equals("1")){
                msg.setResult(false);
                msg.setErrorMsg("删除的按需VM数量大于单次删除的上限:"+sdpVMDeleteAsyncMax);
                return msg;
            }

            // 主动缩容类任务 手动缩容/弹性缩容 ，数量大于阈值 使用原有的批量删除逻辑
            if (vms!=null && vms.size()>sdpVMDeleteAsyncMax && plan!=null
            && (plan.getPlanName().contains("缩容")||plan.getPlanName().contains("删除"))){
                msg.setResult(false);
                msg.setErrorMsg("缩容类的VM数量大于单次删除的上限:"+sdpVMDeleteAsyncMax);
                return msg;
            }
            for (InfoClusterVm vm:vms){

                List<InfoClusterVmDelete> vmDeletes =
                        infoClusterVmDeleteMapper.getByClusterIdAndVmName(vm.getClusterId(),vm.getVmName());

                Optional<InfoClusterVmDelete> vmDeleteop = vmDeletes.stream().filter(x->{
                   return !x.getStatus().equals(InfoClusterVmDelete.STATUS_DELETE_SUCCESS);
                }).findFirst();

                if (vmDeleteop.isPresent()){
                    getLogger().info("ClusterId:{},VmName:{}已存在跳过。",vm.getClusterId(),vm.getVmName());
                    continue;
                }

                InfoClusterVmDelete infoClusterVmDelete = new InfoClusterVmDelete();
                infoClusterVmDelete.setClusterId(vm.getClusterId());
                infoClusterVmDelete.setVmName(vm.getVmName());
                if (plan !=null) {
                    infoClusterVmDelete.setPlanId(plan.getPlanId());
                }
                infoClusterVmDelete.setCreatedTime(new Date());
                infoClusterVmDelete.setStatus(InfoClusterVmDelete.STATUS_INIT);
                infoClusterVmDelete.setPriority(getDeletePriority(vm,plan));
                infoClusterVmDelete.setPurchaseType(vm.getPurchaseType());
                infoClusterVmDelete.setVmRole(vm.getVmRole()==null?vm.getVmRole():"task");
                infoClusterVmDelete.setRegion(region);
                infoClusterVmDeleteMapper.insert(infoClusterVmDelete);
            }
            msg.setResult(true);
        }catch (Exception e){
            getLogger().error("保存deletevm异常，",e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }

    /**
     * 获取需要删除的VM delete记录
     *
     * @return
     */
    @Override
    public List<InfoClusterVmDelete> getNeedDeleteVM() {
        List<InfoClusterVmDelete> infoClusterVmDeleteList =
                infoClusterVmDeleteMapper.getNeedDeleteVMDeletes(sdpSendDeleteEachCount);
        return infoClusterVmDeleteList;
    }

    /**
     * 删除请求前更新状态
     *
     * @param infoClusterVmDelete
     * @return
     */
    @Override
    public int updateInfoDeleteVMBeforeSendDeleteRequest(InfoClusterVmDelete infoClusterVmDelete) {
        infoClusterVmDelete.setModifiedTime(new Date());
        infoClusterVmDelete.setBegSendRequestTime(new Date());
        infoClusterVmDelete.setStatus(InfoClusterVmDelete.STATUS_DELETE_REQUEST_SENDING);
        return infoClusterVmDeleteMapper.updateByPrimaryKeySelective(infoClusterVmDelete);
    }

    /**
     * 获取正在删除中的VM
     *
     * @return
     */
    @Override
    public List<InfoClusterVmDelete> getDeletingVM() {
        List<InfoClusterVmDelete>  deleteing = infoClusterVmDeleteMapper.getDeletingVMs();
        return deleteing;
    }

    /**
     * 发送删除任务请求超时
     *
     * @param timeoutSecond
     * @return
     */
    @Override
    public List<InfoClusterVmDelete> getSendRequestTimeOutVms(Integer timeoutSecond) {
        Date date = DateUtil.addSeconds(new Date(),-timeoutSecond);
        List<InfoClusterVmDelete> vmDeletes = infoClusterVmDeleteMapper.getSendRequestTimeOutVms(date);
        return vmDeletes;
    }

    /**
     * 删除中超时
     *
     * @param timeOutSecond
     * @return
     */
    @Override
    public List<InfoClusterVmDelete> getDeletingTimeOutVms(Integer timeOutSecond) {
        Date date = DateUtil.addSeconds(new Date(),-timeOutSecond);
        List<InfoClusterVmDelete> vmDeletes = infoClusterVmDeleteMapper.getDeletingTimeOutVms(date);
        return vmDeletes;
    }

    /**
     * 获取需要释放的任务
     *
     * @return
     */
    @Override
    public List<InfoClusterVmDelete> getNeedReleaseFreeze() {
        List<InfoClusterVmDelete> infoClusterVmDeleteList =
                infoClusterVmDeleteMapper.getNeedReleaseFreeze(new Date());
        return infoClusterVmDeleteList;
    }

    /**
     * @param clusterVmReqJobFailed
     * @return
     */
    @Override
    public ResultMsg saveClusterVMJobFailed(InfoClusterVmReqJobFailed clusterVmReqJobFailed) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            List<InfoClusterVmReqJobFailed> failedJobs =
                    vmReqJobFailedMapper.getByClusterIdAndPlanIdAndJobId(clusterVmReqJobFailed.getClusterId(),
                            clusterVmReqJobFailed.getPlanId(), clusterVmReqJobFailed.getJobId());
            if (failedJobs == null || failedJobs.size() == 0) {
                vmReqJobFailedMapper.insert(clusterVmReqJobFailed);
            }
          resultMsg.setResult(true);
        }catch (Exception e){
            getLogger().error("保存VMJobFailed异常，",e);
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    @Override
    public List<Map> vmCleanSummary(String region) {
        return infoClusterVmDeleteMapper.vmCleanSummary(region);
    }

    private Integer getDeletePriority(InfoClusterVm vm, InfoClusterOperationPlan plan){

        if (plan==null){
            return InfoClusterVmDelete.PRIORITY_FAILED;
        }
        //按需
        if (vm.getPurchaseType().equals(ConfClusterVm.PURCHASETYPE_ONDEMOND)){
            return InfoClusterVmDelete.PRIORITY_OD;
        }
        // 主动缩容 （弹性缩容/人工缩容）
        if (plan.getPlanName().contains("缩容")){
            return InfoClusterVmDelete.PRIORITY_SCALEIN;
        }
        //竞价逐出
        if (plan.getPlanName().contains("逐出")){
            return InfoClusterVmDelete.PRIORITY_EVICTION;
        }

        if (plan.getPlanName().contains("清理")){
            return InfoClusterVmDelete.PRIORITY_CLEAR;
        }

        return InfoClusterVmDelete.PRIORITY_DEFAULT;
    }
}
