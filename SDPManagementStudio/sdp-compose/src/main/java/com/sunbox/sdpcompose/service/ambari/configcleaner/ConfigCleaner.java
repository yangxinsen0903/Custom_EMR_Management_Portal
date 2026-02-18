/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.service.ambari.configcleaner;

import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;

/**
 * 从Blueprint和CreateClusterTemplate中清里日志
 * @author wangda
 * @date 2025/2/24
 */
public interface ConfigCleaner {
    /**
     * 从Bluepint 和 ClusterTemplate中清除日志. <br/>
     * 根据不同的场景, 实现各自的清理方法
     * @param blueprint
     * @param clusterTemplate
     */
    void clean(Blueprint blueprint, ClusterTemplate clusterTemplate);
}
