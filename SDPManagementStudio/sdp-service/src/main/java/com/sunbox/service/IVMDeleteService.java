package com.sunbox.service;

import com.sunbox.domain.*;

import java.util.List;
import java.util.Map;

public interface IVMDeleteService {

    /**
     * 保存vm到异步数据删除
     * 历史的记录中若存在正在进行的同一VM记录 不允许重复添加
     * 若同一记录均为已删除的情况下，是允许重复删除的。
     * @param vms
     * @return 返回true 为保存成功 false 保存失败
     *
     */
    ResultMsg saveToAsyncDelete(String region, List<InfoClusterVm> vms, InfoClusterOperationPlan plan);

    /**
     *  获取需要删除的VM delete记录
     *
     * @return
     */
    List<InfoClusterVmDelete> getNeedDeleteVM();

    /**
     * 删除请求前更新状态
     * @param infoClusterVmDelete
     * @return
     */
    int updateInfoDeleteVMBeforeSendDeleteRequest(InfoClusterVmDelete infoClusterVmDelete);

    /**
     * 获取正在删除中的VM
     * @return
     */
    List<InfoClusterVmDelete> getDeletingVM();

    /**
     *  发送删除任务请求超时
     * @param timeoutSecond
     * @return
     */
    List<InfoClusterVmDelete> getSendRequestTimeOutVms(Integer timeoutSecond);

    /**
     * 删除中超时
     *
     * @param timeOutSecond
     * @return
     */
    List<InfoClusterVmDelete> getDeletingTimeOutVms(Integer timeOutSecond);

    /**
     * 获取需要释放的任务
     * @return
     */
    List<InfoClusterVmDelete> getNeedReleaseFreeze();

    /**
     *
     *
     * @param clusterVmReqJobFailed
     * @return
     */
    ResultMsg saveClusterVMJobFailed(InfoClusterVmReqJobFailed clusterVmReqJobFailed);

    List<Map> vmCleanSummary(String region);

}
