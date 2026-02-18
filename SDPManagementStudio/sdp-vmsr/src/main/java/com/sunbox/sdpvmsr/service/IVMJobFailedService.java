package com.sunbox.sdpvmsr.service;

import com.sunbox.domain.InfoClusterOperationPlanActivityLog;
import com.sunbox.domain.InfoClusterVmReqJobFailed;

public interface IVMJobFailedService {

    /**
     * 在向Azure提出VM资源申请后,会有失败的情况, 此方法会删除调用Azure接口失败后, 成功被开出来的VM
     * @param failedJob 申请失败的job
     * @return
     */
    boolean processVMReqJob(InfoClusterVmReqJobFailed failedJob);

    void batchProcessVMFailedJob();
}
