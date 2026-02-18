package com.sunbox.domain.ambari;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CleanAmbariHistoryRequest {
    @NotEmpty(message = "集群id不能为空")
    private String clusterId;

    @NotEmpty(message = "开始日期不能为空")
    private String startDate;
}
