/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdptask.task;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.sunbox.constant.BizConfigConstants;
import com.sunbox.constant.RedisLockKeys;
import com.sunbox.dao.mapper.VmEventMapper;
import com.sunbox.dao.query.VmEventQueryParam;
import com.sunbox.domain.VmEvent;
import com.sunbox.sdptask.dto.VmEventModel;
import com.sunbox.service.BizConfigService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * VM上下线事件发布任务,
 * @author wangda
 * @date 2024/7/15
 */
@Component
public class VmEventPublishTask implements BaseCommonInterFace {

    private Boolean enablePush = false;
    private String kafkaServers;

    private String kafkaTopic;

    @Autowired
    private VmEventMapper vmEventMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private BizConfigService bizConfigService;

    Cache<String, Producer<String, String>> producerCache = CacheUtil.newLFUCache(2);

    @Scheduled(cron = "${vmevent.task.cron: 28 0/1 * * * ?}")
    public void startPublishVmEvent() {
        getLogger().info("===开始通知VM上下线事件...");
        // 从数据库加载参数
        getParameterFromDb();

        if (!enablePush) {
            getLogger().info("未开启推送VM上下线事件，退出任务。");
            return;
        }
        boolean locked = redisLock.tryLock(RedisLockKeys.LOCK_VM_EVENT);
        if (!locked) {
            getLogger().info("===通知VM上下线事件进未获取到锁,退出处理");
            return;
        }

        try {
            VmEventQueryParam param = new VmEventQueryParam();
            param.setBeginTime(DateUtil.offsetDay(new Date(), -1));
            param.setEndTime(new Date());
            param.setStates(VmEvent.STATE_INIT, VmEvent.STATE_FAIL);
            param.pager(1, 200);
            param.setSortType("asc");
            List<VmEvent> vmEvents = vmEventMapper.selectPageList(param);
            getLogger().info("获取待推送VM上下线事件数量: " + vmEvents.size() + " 条");

            for (VmEvent vmEvent : vmEvents) {
                try {
                    VmEventModel eventModel = BeanUtil.copyProperties(vmEvent, VmEventModel.class);
                    sendMessage(vmEvent.getVmName() + "-" + vmEvent.getEventType(), JSON.toJSONString(eventModel));
                    vmEvent.setState(VmEvent.STATE_SUCCESS);
                    vmEvent.setFinishTime(new Date());
                    vmEventMapper.updateById(vmEvent);
                } catch (Exception ex) {
                    vmEvent.setState(VmEvent.STATE_FAIL);
                    vmEvent.setFinishTime(new Date());
                    vmEvent.setRemark(ex.getMessage());
                    vmEventMapper.updateById(vmEvent);
                    getLogger().error("向Kafka推送VM上下线事件失败,为了保证顺序,本次暂停推送.", ex);
                    break;
                }
            }
            getLogger().info("===通知VM上下线事件完成");
        } finally {
            redisLock.unlock(RedisLockKeys.LOCK_VM_EVENT);
        }
    }

    private void getParameterFromDb() {
        enablePush = bizConfigService.getConfigValue(
                BizConfigConstants.CATEGORY_VM_EVENT,
                BizConfigConstants.KEY_VM_EVENT_ENABLED,
                Boolean.class);
        kafkaServers = bizConfigService.getConfigValue(
                BizConfigConstants.CATEGORY_VM_EVENT,
                BizConfigConstants.KEY_VM_EVENT_KAFKA_SERVER,
                String.class);
        kafkaTopic = bizConfigService.getConfigValue(
                BizConfigConstants.CATEGORY_VM_EVENT,
                BizConfigConstants.KEY_VM_EVENT_KAFKA_TOPIC,
                String.class);
    }

    private void sendMessage(String key, String message) {
        try {
            ProducerRecord<String, String> msg = new ProducerRecord<>(kafkaTopic, key, message);
            getKafkaProducer().send(msg).get();
        } catch (Exception ex) {
            throw new RuntimeException("向Kafka发送VM上下线事件失败:" + ex.getMessage(), ex);
        }
    }

    /**
     * 获取KafkaProducer, 先从缓存取, 缓存没有, 再新建
     * @return
     */
    private Producer<String, String> getKafkaProducer() {
        String key = kafkaServers + kafkaTopic;
        Producer<String, String> producer = producerCache.get(key);
        if (Objects.nonNull(producer)) {
            return producer;
        }

        Properties prop = new Properties();
        // 设置连接Kafka的初始连接用到的服务器地址
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        // 设置key的序列化类
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        // 设置value的序列化类
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.ACKS_CONFIG,"all");
        prop.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        producer = new KafkaProducer<>(prop);
        getLogger().info("连接Kafka,并创建Producer: kafkaServer={}, topic={}", kafkaServers, kafkaTopic);
        producerCache.put(key, producer);
        return producer;
    }
}
