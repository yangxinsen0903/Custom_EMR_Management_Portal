package com.sunbox.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 指标
 */
public class Metric {
    /**
     * 指标名
     */
    private String metricName;
    /**
     * 指标值
     */
    private BigDecimal metricValue;
    /**
     * 采集类型 max,min,avg
     */
    private String aggregateType;

    /**
     * 指标开始时间
     */
    private Date startTime;

    /**
     * 指标结束时间
     */
    private Date endTime;

    /**
     * 弹性规则Id
     */
    private String clusterId;

    /**
     * 实例名
     */
    private String groupEsId;

    private ConfGroupElasticScalingRule scalingRule;
    private Integer windowSize;

    public ConfGroupElasticScalingRule getScalingRule() {
        return scalingRule;
    }

    public void setScalingRule(ConfGroupElasticScalingRule scalingRule) {
        this.scalingRule = scalingRule;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupEsId() {
        return groupEsId;
    }

    public void setGroupEsId(String groupEsId) {
        this.groupEsId = groupEsId;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Metric{");
        sb.append("metricName='").append(metricName).append('\'');
        sb.append(", metricValue=").append(metricValue);
        sb.append(", aggregateType='").append(aggregateType).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", groupEsId='").append(groupEsId).append('\'');
        sb.append(", scalingRule=").append(scalingRule);
        sb.append(", windowSize=").append(windowSize);
        sb.append('}');
        return sb.toString();
    }

}
