package com.sunbox.domain;

import java.util.Date;

public class InfoGroupElasticScalingRuleLog {
    private Long esRuleLogId;

    private String esRuleId;

    private String clusterId;

    private String loadMetric;

    private String aggregateType;

    private String operator;

    private Double threshold;

    private Double metricVal;

    private Integer isStartScaling;

    private Date createdTime;
    private Integer computeResult;

    private Date metricStartTime;
    private Date metricEndTime;
    private String scaleServerIp;
    private String taskResult;
    private String taskResultMessage;
    private String taskId;

    public Long getEsRuleLogId() {
        return esRuleLogId;
    }

    public void setEsRuleLogId(Long esRuleLogId) {
        this.esRuleLogId = esRuleLogId;
    }

    public String getEsRuleId() {
        return esRuleId;
    }

    public void setEsRuleId(String esRuleId) {
        this.esRuleId = esRuleId == null ? null : esRuleId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getLoadMetric() {
        return loadMetric;
    }

    public void setLoadMetric(String loadMetric) {
        this.loadMetric = loadMetric == null ? null : loadMetric.trim();
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType == null ? null : aggregateType.trim();
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getMetricVal() {
        return metricVal;
    }

    public void setMetricVal(Double metricVal) {
        this.metricVal = metricVal;
    }

    public Integer getIsStartScaling() {
        return isStartScaling;
    }

    public void setIsStartScaling(Integer isStartScaling) {
        this.isStartScaling = isStartScaling;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setComputeResult(Integer computeResult) {
        this.computeResult = computeResult;
    }

    public Integer getComputeResult() {
        return computeResult;
    }

    public void setMetricStartTime(Date metricStartTime) {
        this.metricStartTime = metricStartTime;
    }

    public Date getMetricStartTime() {
        return metricStartTime;
    }

    public void setMetricEndTime(Date metricEndTime) {
        this.metricEndTime = metricEndTime;
    }

    public Date getMetricEndTime() {
        return metricEndTime;
    }

    public void setScaleServerIp(String scaleServerIp) {
        this.scaleServerIp = scaleServerIp;
    }

    public String getScaleServerIp() {
        return scaleServerIp;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResultMessage(String taskResultMessage) {
        this.taskResultMessage = taskResultMessage;
    }

    public String getTaskResultMessage() {
        return taskResultMessage;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InfoGroupElasticScalingRuleLog{");
        sb.append("esRuleLogId=").append(esRuleLogId);
        sb.append(", esRuleId='").append(esRuleId).append('\'');
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", loadMetric='").append(loadMetric).append('\'');
        sb.append(", aggregateType='").append(aggregateType).append('\'');
        sb.append(", operator='").append(operator).append('\'');
        sb.append(", threshold=").append(threshold);
        sb.append(", metricVal=").append(metricVal);
        sb.append(", isStartScaling=").append(isStartScaling);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", computeResult=").append(computeResult);
        sb.append(", metricStartTime=").append(metricStartTime);
        sb.append(", metricEndTime=").append(metricEndTime);
        sb.append(", scaleServerIp='").append(scaleServerIp).append('\'');
        sb.append(", taskResult='").append(taskResult).append('\'');
        sb.append(", taskResultMessage='").append(taskResultMessage).append('\'');
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}