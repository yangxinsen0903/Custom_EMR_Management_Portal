package com.sunbox.sdpcompose.service.impl;

import com.sunbox.sdpcompose.service.IKafkaListener;
import com.sunbox.sdpcompose.service.IPlanExecService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author : [niyang]
 * @className : ComposeListener
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/2 6:10 PM]
 */
@Qualifier("ComposeListener")
@Service
public class ComposeListener implements IKafkaListener, BaseCommonInterFace {


    @Autowired
    private IPlanExecService planExecService;
    /**
     * 处理消息
     *
     * @param consumerRecord
     */
    @Override
    public void listen(ConsumerRecord<String, String> consumerRecord) {
        String value = (String) consumerRecord.value();
        planExecService.composeExecute(value);
    }
}
