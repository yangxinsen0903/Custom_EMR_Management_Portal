package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cluster信息
 * @author: wangda
 * @date: 2022/12/6
 */
public class ClusterInfo {

    @JsonProperty("cluster_name")
    String clusterName;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
