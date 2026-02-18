package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateClusterConfigData {

    private String clusterId;

    private String insGpId;

    // 内部字段统一，给compose使用
    private String groupId;

    private String groupName;

    private List<ConfigProperties> clusterConfigs;
}
