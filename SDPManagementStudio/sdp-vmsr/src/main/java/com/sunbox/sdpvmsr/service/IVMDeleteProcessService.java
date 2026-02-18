package com.sunbox.sdpvmsr.service;

import com.sunbox.domain.InfoClusterVmDelete;

public interface IVMDeleteProcessService {

    /**
     *  发送删除请求
     * @param vmDelete
     */
    void deleteVMSendReq(InfoClusterVmDelete vmDelete);

    /**
     *  查询删除结果
     * @param vmDelete
     */
    void deleteJobQuery(InfoClusterVmDelete vmDelete);

    void deleteVMFailed(InfoClusterVmDelete vmDelete);

}
