package com.sunbox.sdpcompose.producer;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.sunbox.sdpcompose.mode.queue.DelayedTask;
import com.sunbox.sdpcompose.util.BeanMethod;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

/**
 * @author : [niyang]
 * @className : ProducerCache
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 11:12 AM]
 */
public class ProducerCache {

    /**
     * serviceBus 生产者
     */
    public static Map<String, ServiceBusSenderClient> producers;

    /**
     * serviceBus 消费者
     */
    public static Map<String, ServiceBusProcessorClient> consumers;

    /**
     * BeanMethod缓存
     */
    public static HashMap<String, BeanMethod> methods;

    /**
     * Kafka 生产者
     */
    public static HashMap<String, KafkaProducer> kafkaProducers;


    /**
     * Kafka生产者客户端对应的toppic
     * <clientname,topic>
     */
    public static HashMap<String,String> kafkaClientTopic;


    public static DelayQueue<DelayedTask> delayQueue;

}
