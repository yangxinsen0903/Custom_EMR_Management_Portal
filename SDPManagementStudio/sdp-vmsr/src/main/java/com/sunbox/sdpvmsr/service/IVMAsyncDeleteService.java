package com.sunbox.sdpvmsr.service;

public interface IVMAsyncDeleteService {

    /**
     *  发送删除申请
     */
    void sendDeleteRequest();

    /**
     *  查询删除申请结果
     */
    void getDeleteJobResult();

    /**
     *  超时任务
     *  1.发送删除申请的任务
     *  2.查询删除结果的任务
     */
    void timeOutScan();

    /**
     * 释放冻结的任务
     */
    void releaseFreeze();

}
