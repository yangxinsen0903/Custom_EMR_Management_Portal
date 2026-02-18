package com.sunbox.sdpcompose.service.ambari;

/**
 * 查询执行进展的命令
 * @author: wangda
 * @date: 2022/12/10
 */
public class QueryProgressCmd {
    /** Ambari信息 */
    private AmbariInfo ambariInfo;

    /** 集群名称 */
    String clusterName;

    /** 请求Id */
    private Long requestId;


    public void setAmbariInfo(AmbariInfo ambariInfo) {
        this.ambariInfo = ambariInfo;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public AmbariInfo getAmbariInfo() {
        return ambariInfo;
    }

    public String getClusterName() {
        return clusterName;
    }

    public Long getRequestId() {
        return requestId;
    }
}
