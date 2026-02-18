package com.sunbox.sdpadmin.model.admin.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class ConfGroupElasticScalingRuleData {

    // 弹性伸缩规则ID
    private String esRuleId;

    // 规则名称
    @NotEmpty
    private String esRuleName;

    // 伸缩类型（1扩容，0缩容）
    @NotNull
    private Integer scalingType;

    // 单次伸缩数量
    @NotNull
    private Integer perSalingCout;

    // 集群负载指标
    @NotEmpty
    private String loadMetric;

    // 统计周期
    @NotNull
    private Integer windowSize;


    // 聚合类型
    @NotEmpty
    private String aggregateType;

    // 运算法
    @NotEmpty
    private String operator;

    // 阈值
    @NotNull
    private Double threshold;


    // 统计周期个数
    @NotNull
    private Integer repeatCount;

    // 集群冷却时间
    @NotNull
    private Integer freezingTime;

    private Integer isValid;

    // 最后一次执行时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecuteTime;

    // 最后一次计算时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastComputedTime;

    // 是否执行集群启动前脚本（1：执行，0：不执行）
    private Integer enableBeforestartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterstartScript;

    // 是否优雅缩容
    private Integer isGracefulScalein;

    // 优雅缩容等待时间
    private Integer scaleinWaitingtime;

    @Override
    public String toString() {
        return "{" +
                "\"esRuleId\":\"" + esRuleId + '"' +
                ", \"esRuleName\":\"" + esRuleName + '"' +
                ", \"scalingType\":" + scalingType +
                ", \"perSalingCout\":" + perSalingCout +
                ", \"loadMetric\":\"" + loadMetric + '"' +
                ", \"windowSize\":" + windowSize +
                ", \"aggregateType\":\"" + aggregateType + '"' +
                ", \"operator\":\"" + operator + '"' +
                ", \"threshold\":" + threshold +
                ", \"repeatCount\":" + repeatCount +
                ", \"freezingTime\":" + freezingTime +
                ", \"isValid\":" + isValid +
                ", \"lastExecuteTime\":" + lastExecuteTime +
                ", \"lastComputedTime\":" + lastComputedTime +
                ", \"enableBeforestartScript\":" + enableBeforestartScript +
                ", \"enableAfterstartScript\":" + enableAfterstartScript +
                ", \"isGracefulScalein\":" + isGracefulScalein +
                ", \"scaleinWaitingtime\":" + scaleinWaitingtime +
                "}";
    }
}
