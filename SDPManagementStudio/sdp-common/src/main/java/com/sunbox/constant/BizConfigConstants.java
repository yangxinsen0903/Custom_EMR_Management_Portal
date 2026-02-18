/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.constant;

/**
 * 业务配置的配置常量
 * @author wangda
 * @date 2024/7/16
 */
public class BizConfigConstants {
    /** 托管扩缩容分类 */
    public static final String CATEGORY_SCALE_HOSTING = "托管扩缩容配置";
    /** VM上下线事件通知分类 */
    public static final String CATEGORY_VM_EVENT = "VM上下线事件通知";
    /** sdp路由配置 **/
    public static final String SDP_ROUTER = "sdp路由配置";
    /** Shein CMDB配置信息 **/
    public static final String CATEGORY_CMDB = "shein请求接口配置";

    /** VM上下线事件通知:是否开启通知 */
    public static final String KEY_VM_EVENT_ENABLED = "vm.event.enabled";
    /** VM上下线事件通知: kafka地址 */
    public static final String KEY_VM_EVENT_KAFKA_SERVER = "vm.event.kafka.server";
    /** VM上下线事件通知: Topic */
    public static final String KEY_VM_EVENT_KAFKA_TOPIC = "vm.event.kafka.topic";
    /** 是否开启路由 */
    public static final String SDP_ROUTER_IS_ENABLE_ROUTER = "sdpRouter.isEnableRouter";
    /** SDP一期URL */
    public static final String SDP_ROUTER_SDP1URL = "sdpRouter.sdp1Url";
    /** SDP二期集群前缀 */
    public static final String SDP_ROUTER_CLUSTER_NAME_PREFIX = "sdpRouter.clusterNamePrefix";

    // 销毁任务中, 表里的 时间
    public static final String LIMITTIME = "cluster.destroy.limit.time";
    // 数量
    public static final String LIMITCOUNT = "cluster.destroy.limit.count";

    // 销毁任务中, 接口字段名称
    public static final String DESTORYINTERVALSECOND = "destoryIntervalSecond";
    public static final String DESTORYLIMITCOUNT = "destoryLimitCount";
    //缓存key名称
    public static final String DESTORYTASKKEY = "destorytaskkey";

    //shein接口服务相关配置
    public static final String SHEINCMDBURL = "shein.cmdb.request.url";
    public static final String SHEINCMDBXTOKEN = "shein.cmdb.request.xtoken";



}
