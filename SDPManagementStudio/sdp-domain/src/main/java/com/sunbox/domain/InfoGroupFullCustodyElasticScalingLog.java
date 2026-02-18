package com.sunbox.domain;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.Date;

public class InfoGroupFullCustodyElasticScalingLog {
    private Long esFullLogId;
    private String esRuleId;

    private String clusterId;
    private String groupName;
    private Integer isStartScaling;
    private String scalingType;
    private Date createdTime;
    private Double computeValue;
    private String taskResult;
    private String taskResultMessage;
    private String taskId;
    private String metricValues;
    private Integer scalingCount;



    public String getEsRuleId() {
        return esRuleId;
    }

    public void setEsRuleId(String esRuleId) {
        this.esRuleId = StrUtil.trim(esRuleId);
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = StrUtil.trim(clusterId);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getIsStartScaling() {
        return isStartScaling;
    }

    public void setIsStartScaling(Integer isStartScaling) {
        this.isStartScaling = isStartScaling;
    }

    public String getScalingType() {
        return scalingType;
    }

    public void setScalingType(String scalingType) {
        this.scalingType = scalingType;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public String getTaskResultMessage() {
        return taskResultMessage;
    }

    public void setTaskResultMessage(String taskResultMessage) {
        this.taskResultMessage = taskResultMessage;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMetricValues() {
        return metricValues;
    }

    public void setMetricValues(String metricValues) {
        this.metricValues = metricValues;
    }

    public Double getComputeValue() {
        return computeValue;
    }

    public void setComputeValue(Double computeValue) {
        this.computeValue = computeValue;
    }

    public Long getEsFullLogId() {
        return esFullLogId;
    }

    public void setEsFullLogId(Long esFullLogId) {
        this.esFullLogId = esFullLogId;
    }

    public Integer getScalingCount() {
        return scalingCount;
    }

    public void setScalingCount(Integer scalingCount) {
        this.scalingCount = scalingCount;
    }
}
