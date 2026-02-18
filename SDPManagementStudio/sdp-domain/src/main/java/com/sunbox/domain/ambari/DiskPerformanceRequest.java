package com.sunbox.domain.ambari;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DiskPerformanceRequest {
    @NotEmpty(message = "集群ID不能为空")
    private String clusterId;

    @NotEmpty(message = "实例配置ID不能为空")
    private String vmConfId;

    @NotNull(message = "IPOS不能为空")
    private Integer newDataDiskIOPSReadWrite;

    @NotNull(message = "吞吐量不能为空")
    private Integer newDataDiskMBpsReadWrite;
}
