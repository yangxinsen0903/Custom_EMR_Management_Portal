package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

@Data
public class SheinElasticScalingRuleData {

    // 弹性伸缩规则id
    private String esRuleId;

    // 规则名称
    private String esRuleName;

    // 伸缩类型（1扩容，0缩容）
    private Integer scalingType;

    // 单次伸缩数量
    private Integer perScalingCnt;

    // 集群负载指标
    private Integer loadMetric;

    // 统计周期
    private Integer windowSize;

    // 聚合类型
    private Integer aggregateType;

    // 运算符
    private Integer operator;

    // 阈值
    private Double threshold;

    // 统计周期个数
    private Integer repeatCnt;

    // 集群冷却时间
    private Integer freezingTime;

    private Integer isValid;

    // 是否执行集群启动前脚本（1：执行，0：不执行）
    private Integer enableBeforeStartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterStartScript;

    // 是否优雅缩容
    private Integer isGracefulScaleIn;

    // 优雅缩容等待时间
    private Integer scaleInWaitingTime;
}
