package com.sunbox.domain.cluster;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class UpdateDestroyStatusRequest {
    @NotEmpty(message ="集群id不能为空!")
    private String clusterId;

    @NotNull(message ="是否加入直接销毁白名单,不能为空!")
    private Integer isWhiteAddr;

}
