package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

/**
 * @author : [niyang]
 * @className : SheinElasticScalingAddRuleData
 * @description : [描述说明该类的功能]
 * @createTime : [2023/6/25 10:16 AM]
 */
@Data
public class SheinElasticScalingAddRuleData {
    private String clusterId;

    private String vmRole;

    private String groupName;

    private SheinElasticScalingRuleData rule;
}
