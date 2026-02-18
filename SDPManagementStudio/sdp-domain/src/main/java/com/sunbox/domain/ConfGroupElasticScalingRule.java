package com.sunbox.domain;

import java.util.Date;

public class ConfGroupElasticScalingRule {

    public static final Integer ISVALID_YES = 1;
    public static final Integer ISVALID_NO = 0;

    private String esRuleId;

    private String groupEsId;

    private String esRuleName;

    // 1扩容，0缩容
    private Integer scalingType;

    private Integer perSalingCout;

    private String loadMetric;

    private Integer windowSize;

    private String aggregateType;

    private String operator;

    private Double threshold;

    private Integer repeatCount;

    private Integer freezingTime;

    private String createdby;

    private Date createdTime;

    private String modifiedby;

    private Date modifiedTime;

    private Integer ruleSorted;

    private String clusterId;

    // 是否执行集群启动前脚本（1：执行，0：不执行）
    private Integer enableBeforestartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterstartScript;
    private String groupName;
    private Integer isValid;
    /**
     * 是否优雅缩容
     */
    private Integer isGracefulScalein;
    /**
     * 优雅缩容等待时间
     */
    private Integer scaleinWaitingtime;
    private Integer maxCount;
    private Integer minCount;

    public String getEsRuleId() {
        return esRuleId;
    }

    public void setEsRuleId(String esRuleId) {
        this.esRuleId = esRuleId;
    }

    public String getGroupEsId() {
        return groupEsId;
    }

    public void setGroupEsId(String groupEsId) {
        this.groupEsId = groupEsId;
    }

    public String getEsRuleName() {
        return esRuleName;
    }

    public void setEsRuleName(String esRuleName) {
        this.esRuleName = esRuleName;
    }

    public Integer getScalingType() {
        return scalingType;
    }

    public void setScalingType(Integer scalingType) {
        this.scalingType = scalingType;
    }

    public Integer getPerSalingCout() {
        return perSalingCout;
    }

    public void setPerSalingCout(Integer perSalingCout) {
        this.perSalingCout = perSalingCout;
    }

    public String getLoadMetric() {
        return loadMetric;
    }

    public void setLoadMetric(String loadMetric) {
        this.loadMetric = loadMetric;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Integer getFreezingTime() {
        return freezingTime;
    }

    public void setFreezingTime(Integer freezingTime) {
        this.freezingTime = freezingTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getRuleSorted() {
        return ruleSorted;
    }

    public void setRuleSorted(Integer ruleSorted) {
        this.ruleSorted = ruleSorted;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getEnableBeforestartScript() {
        return enableBeforestartScript;
    }

    public void setEnableBeforestartScript(Integer enableBeforestartScript) {
        this.enableBeforestartScript = enableBeforestartScript;
    }

    public Integer getEnableAfterstartScript() {
        return enableAfterstartScript;
    }

    public void setEnableAfterstartScript(Integer enableAfterstartScript) {
        this.enableAfterstartScript = enableAfterstartScript;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMetriWindowKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(clusterId).append("_").
                append(loadMetric).append("_").
                append(windowSize);
        return sb.toString();
    }

    public Integer getIsGracefulScalein() {
        return isGracefulScalein;
    }

    public void setIsGracefulScalein(Integer isGracefulScalein) {
        this.isGracefulScalein = isGracefulScalein;
    }

    public Integer getScaleinWaitingtime() {
        return scaleinWaitingtime;
    }

    public void setScaleinWaitingtime(Integer scaleinWaitingtime) {
        this.scaleinWaitingtime = scaleinWaitingtime;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfGroupElasticScalingRule{");
        sb.append("esRuleId='").append(esRuleId).append('\'');
        sb.append(", groupEsId='").append(groupEsId).append('\'');
        sb.append(", esRuleName='").append(esRuleName).append('\'');
        sb.append(", scalingType=").append(scalingType);
        sb.append(", perSalingCout=").append(perSalingCout);
        sb.append(", loadMetric='").append(loadMetric).append('\'');
        sb.append(", windowSize=").append(windowSize);
        sb.append(", aggregateType='").append(aggregateType).append('\'');
        sb.append(", operator='").append(operator).append('\'');
        sb.append(", threshold=").append(threshold);
        sb.append(", repeatCount=").append(repeatCount);
        sb.append(", freezingTime=").append(freezingTime);
        sb.append(", createdby='").append(createdby).append('\'');
        sb.append(", createdTime=").append(createdTime);
        sb.append(", modifiedby='").append(modifiedby).append('\'');
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", ruleSorted=").append(ruleSorted);
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", enableBeforestartScript=").append(enableBeforestartScript);
        sb.append(", enableAfterstartScript=").append(enableAfterstartScript);
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", isValid=").append(isValid);
        sb.append(", isGracefulScalein=").append(isGracefulScalein);
        sb.append(", scaleinWaitingtime=").append(scaleinWaitingtime);
        sb.append(", maxCount=").append(maxCount);
        sb.append(", minCount=").append(minCount);
        sb.append('}');
        return sb.toString();
    }
}