package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

@Data
public class SheinDiskPerformance {
    /**
     * 集群ID不能为空
     */
    private String clusterId;

    /**
     * 实例组id不能为空
     */
    private String insGpId;

    /**
     * IPOS不能为空
     */
    private Integer iops;

    /**
     * 吞吐量不能为空
     */
    private Integer throughput;
}
