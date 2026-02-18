package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ResultMsg;

/**
 * @author : [niyang]
 * @className : MQProducerService
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 3:28 PM]
 */
public interface IMQProducerService {



    /**
     * 正常消息发送
     * 消费代码在 PlanExecServiceImpl.composeExecute
     * @param clientname
     * @param messagebody
     * @return
     */
    ResultMsg sendMessage(String clientname,String messagebody);

    /**
     * 延时消息发送
     * @param clientName 客户端名称
     * @param messagebody 消息体
     * @param offsetSeconds 延时秒数
     * @return
     */
    ResultMsg sendScheduleMessage(String clientName,String messagebody,Long offsetSeconds);


}
