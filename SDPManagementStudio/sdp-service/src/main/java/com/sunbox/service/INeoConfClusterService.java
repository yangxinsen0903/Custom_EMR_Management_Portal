package com.sunbox.service;

import com.sunbox.domain.ConfCluster;

public interface INeoConfClusterService {

    /**
     * 根据集群Id查询集群信息
     * @param clusterId  集群Id
     * @return
     */
    ConfCluster getConfClusterByClusterId (String clusterId);
}
