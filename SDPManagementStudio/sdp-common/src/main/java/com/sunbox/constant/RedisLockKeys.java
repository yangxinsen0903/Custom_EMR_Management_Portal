/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.constant;

/**
 * Redis的锁Key
 * @author wangda
 * @date 2024/7/15
 */
public class RedisLockKeys {

    /**
     * 将Vm上下线事件推送至Kafka的锁
     */
    public static final String LOCK_VM_EVENT = "lock:vmEvent:pushKafka";

    // 集合状态: 1,已加入集合(等待销毁)   2,销毁中
    public static final String CLUSTERDAEMONTASK_CLUSTER_STATUS = "ClusterDaemonTask_cluster_status";
    public static final String CLUSTERDAEMONTASK_CLUSTER_TIME = "ClusterDaemonTask_cluster_time";
    public static final Long TIME_24H = 60 * 60 * 24L;
}
