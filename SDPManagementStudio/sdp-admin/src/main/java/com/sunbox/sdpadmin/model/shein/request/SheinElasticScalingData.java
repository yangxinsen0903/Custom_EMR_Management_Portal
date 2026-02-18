package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

import java.util.List;

@Data
public class SheinElasticScalingData {

    // 集群id
    private String clusterId;

    // 实例组id
    private String insGpId;

    // 实例组最大实例数
    private Integer maxCnt;

    // 实例组最小实例数
    private Integer minCnt;

    // 实例组弹性伸缩规则配置
    private List<SheinElasticScalingRuleData> scalingRules;

    private List<String> esRuleIds;

    /**
     * 是否优雅缩容
     */
    private Integer isGracefulScalein;
    /**
     * 优雅缩容等待时间单位：分钟
     */
    private Integer scaleinWaitingTime;
    /**
     * 扩容是否执行启动前脚本
     */
    private Integer enableBeforestartScript;
    /**
     * 扩容是否执行启动后脚本
     */
    private Integer enableAfterstartScript;
    /**
     * 是否开启全托管弹性伸缩
     */
    private Integer isFullCustody;
}
