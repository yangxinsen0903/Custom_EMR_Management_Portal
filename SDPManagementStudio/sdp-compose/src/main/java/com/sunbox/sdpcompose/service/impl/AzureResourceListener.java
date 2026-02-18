package com.sunbox.sdpcompose.service.impl;

import com.sunbox.sdpcompose.service.IKafkaListener;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author : [niyang]
 * @className : AzureResourceListener
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/2 6:07 PM]
 */
@Qualifier("AzureResourceListener")
@Service
public class AzureResourceListener implements IKafkaListener, BaseCommonInterFace {

    /**
     * 处理消息
     *
     * @param consumerRecord
     */
    @Override
    public void listen(ConsumerRecord<String, String> consumerRecord) {

    }
}
