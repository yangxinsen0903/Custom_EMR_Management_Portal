package com.sunbox.domain.vmEvent;

import lombok.Builder;
import lombok.Data;

@Data
public class VmEventRequest {

    private String region;
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 实例组名
     */
    private String groupName;

    /**
     * vm名称
     */
    private String vmName;

    /**
     * vm的HostName
     */
    private String hostName;
    /**
     * INIT（初始化）， PROCESSING（处理中）， SUCCESS（成功）， FAIL（失败）
     */
    private String state;

    private String beginTime;
    private String endTime;

    private Integer pageIndex;
    private Integer pageSize;
}
