/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.enums;

/**
 * 补全的被驱逐的VM的状态
 * @author wangda
 * @date 2024/7/4
 */
public enum EvictVmStateType {
    /** 初始化，待处理 */
    INIT,
    /** 集群未运行 */
    CLUSTER_NOT_RUNNING,
    /** 集群不存在 */
    CLUSTER_NOT_EXIST,
    /** VM已经加入集群 */
    VM_IN_CLUSTER,
    /** 现存的Hostgroup里为空 */
    HOSTGROUP_EMPTY,
    /** 处理成功 */
    SUCCESS;
}
