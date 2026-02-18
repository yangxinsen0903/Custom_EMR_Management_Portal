package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class AppendVmsRequest {

    private String apiVersion;

    private String transactionId;

    private String requestTimestamp;

    private String region;

    private String clusterName;

    private List<VirtualMachineGroup> virtualMachineGroups;

}
