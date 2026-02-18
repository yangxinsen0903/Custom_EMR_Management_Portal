package com.sunbox.sdpadmin.model.shein.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpadmin.model.admin.request.ConfGroupElasticScalingData;
import lombok.Data;

import java.util.List;

@Data
public class SheinInstanceGroupData {

    @JsonProperty("clusterId")
    private String srcClusterId;

    @JsonProperty("insGpRole")
    private String vmRole;

    @JsonProperty("insGpName")
    private String groupName;

    private SheinInstanceGroupSkuCfg instanceGroupSkuCfg;

    @JsonProperty("groupCfgs")
    private List<SheinClusterCfg> clusterCfgs;

    private ConfGroupElasticScalingData confGroupElasticScalingData;
}
