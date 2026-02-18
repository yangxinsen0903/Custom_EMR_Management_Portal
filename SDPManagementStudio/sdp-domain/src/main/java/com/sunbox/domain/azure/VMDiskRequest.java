package com.sunbox.domain.azure;

import lombok.Data;

import java.util.List;

/**
 * 更新PV2数据盘IOPS和MBPS,请求参数
 */
@Data
public class VMDiskRequest {

    private String apiVersion;
    private String clusterName;
    private String groupName;
    private Integer newDataDiskIOPSReadWrite;
    private Integer newDataDiskMBpsReadWrite;
    private String region;
    private String transactionId;
    private String subscriptionId;
    private List<String> vmNames;
}
