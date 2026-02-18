package com.sunbox.sdpscale.service;

import com.sunbox.sdpscale.model.ClusterMetrics;

import java.util.List;

public interface MetricService {
    /**
     * 可用vCore百分比
     * @param clusterMetrics
     * @return
     */
    int metricVCoreAvailablePrecentage(ClusterMetrics clusterMetrics);

    /**
     * 可用内存百分比
     * @param clusterMetrics
     * @return
     */
    int metricMemoryAvailablePrecentage(ClusterMetrics clusterMetrics);

    /**
     * 容器分配比率
     * @param clusterMetrics
     * @return
     */
    int metricContainerPendingRatio(ClusterMetrics clusterMetrics);

    /**
     * 应用程序挂起数
     * @param clusterMetrics
     * @return
     */
    int metricAppsPending(ClusterMetrics clusterMetrics);

    /**
     * 获取ResourceMangerHostName
     * HA 获取vmRole = master
     * 非HA获取 vmRole = ambari
     *
     * @param clusterId
     * @return
     */
    List<String> getResourceManagerHostNames(String clusterId);
}
