package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class UpdateVmsDiskSizeRequest {

    private String apiVersion;

    private String transactionId;

    private String region;

    private String clusterName;

    private String groupName;

    private List<String> vmNames;

    private Integer newDataDiskSizeGB;
}
