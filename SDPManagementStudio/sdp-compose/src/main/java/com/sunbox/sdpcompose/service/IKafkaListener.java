package com.sunbox.sdpcompose.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IKafkaListener {

    /**
     * 处理消息
     * @param consumerRecord
     */
    void listen(ConsumerRecord<String, String> consumerRecord);
}
