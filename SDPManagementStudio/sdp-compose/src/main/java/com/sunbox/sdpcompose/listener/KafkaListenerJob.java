package com.sunbox.sdpcompose.listener;

import com.alibaba.fastjson.JSON;
import com.sunbox.sdpcompose.configuration.KafkaConfig;
import com.sunbox.sdpcompose.service.IKafkaListener;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;

public class KafkaListenerJob implements Runnable, BaseCommonInterFace {

    private KafkaConfig kafkaConfig;

    private IKafkaListener kafkaListener;

    private KafkaConsumer<String, String> kafkaConsumer;

    public KafkaListenerJob(KafkaConfig kafkaConfig1, IKafkaListener kafkaListener){
        this.kafkaConfig=kafkaConfig1;
        this.kafkaListener=kafkaListener;
        kafkaConsumer = new KafkaConsumer<String, String>(kafkaConfig.getProperties());
        // 订阅消息
        kafkaConsumer.subscribe(Collections.singletonList(kafkaConfig.getTopic()));
        getLogger().info("消息订阅成功！kafka配置：" + kafkaConfig.getProperties().toString());
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        getLogger().info("kafka 监听启动，"+kafkaConfig.getTopic());
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(kafkaConfig.getDuration()));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    kafkaListener.listen(record);
                    getLogger().info(record.topic());
                    getLogger().info(record.value().toString());

                } catch (Exception e) {
                    getLogger().error("消息消费异常！", e);
                }
            }
            try {
                Thread.sleep(1l);
            } catch (InterruptedException e) {
                getLogger().error("listener 异常，",e);
            }
        }
    }
}
