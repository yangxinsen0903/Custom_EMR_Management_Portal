package com.sunbox.sdpadmin.model.shein.response;

import com.sunbox.sdpadmin.model.shein.request.SheinElasticScalingRuleData;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstanceGroupResponse {

    private String clusterId;

    // 实例组id
    private String insGpId;

    // 实例组名称
    private String insGpName;

    // 实例组类型
    private String insGpRole;

    // 实例组的实例数量
    private Integer insGpCnt;

    private String insType;

    private String insMktType;

    private Integer rootVolSize;

    private Integer state;

    private List<Map<String, Object>> disks;

    // 期望数量
    private Integer expectCount;

    private List<SheinElasticScalingRuleData> scalingRules;

    private Integer maxCnt;

    private Integer minCnt;

    private Integer isGracefulScalein;
    private Integer scaleinWaitingTime;
    private Integer enableBeforestartScript;
    private Integer enableAfterstartScript;
    private Integer isFullCustody;
}
