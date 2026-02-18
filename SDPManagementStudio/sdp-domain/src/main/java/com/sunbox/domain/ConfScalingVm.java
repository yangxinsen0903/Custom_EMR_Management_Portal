package com.sunbox.domain;

import java.util.Date;

/**
 * 扩缩容任务对应VM
 */
public class ConfScalingVm {
    //`record_id` varchar(40)  NOT NULL COMMENT '主键',
    private String recordId;
    //`task_id` varchar(40)  NOT NULL COMMENT '任务id',
    private String taskId;
    //`cluster_id` varchar(40)  NOT NULL COMMENT '集群ID',
    private String clusterId;
    //`vm_name` varchar(60)  NOT NULL COMMENT '实例名称',
    private String vmName;
    //`vm_conf_id` varchar(40)  DEFAULT NULL COMMENT '实例配置ID',
    private String vmConfId;
    //`host_name` varchar(200)  DEFAULT NULL COMMENT '机器名称',
    private String hostName;
    //`internalIp` varchar(50)  DEFAULT NULL COMMENT '内网IP',
    private String internalIp;
    //`default_username` varchar(20)  DEFAULT NULL COMMENT '默认用户名',
    private String defaultUsername;
    //`vm_role` varchar(45)  DEFAULT NULL COMMENT '实例角色',
    private String vmRole;
    //`es_rule_id` varchar(40) DEFAULT NULL COMMENT '弹性伸缩规则ID',
    private String esRuleId;
    //`es_rule_name` varchar(200) DEFAULT NULL COMMENT '弹性伸缩规则名称',
    private String esRuleName;
    //`before_scaling_count` int DEFAULT NULL COMMENT '伸缩前数量',
    private Integer beforeScalingCount;
    //`after_scaling_count` int DEFAULT NULL COMMENT '伸缩后数量',
    private Integer afterScalingCount;
    //`scaling_count` int DEFAULT NULL COMMENT '伸缩数量',
    private Integer scalingCount;
    //`group_name` varchar(60)  DEFAULT NULL COMMENT '实例组名称',
    private String groupName;
    //`group_id` varchar(40)  NOT NULL COMMENT '实例组id',
    private String groupId;
    //`sku_name` varchar(255)  DEFAULT NULL COMMENT 'sku名称',
    private String skuName;
    //`purchase_type` varchar(255)  DEFAULT NULL COMMENT '购买类型',
    private String purchaseType;
    //`scaling_type` int DEFAULT NULL COMMENT '扩缩容类型',
    private Integer scalingType;
    //`operation_type` int DEFAULT NULL COMMENT '扩缩容类型',
    private Integer operationType;
    //`beg_time` datetime DEFAULT NULL COMMENT '开始时间',
    private Date begTime;
    //`end_time` datetime DEFAULT NULL COMMENT '完成时间',
    private Date endTime;
    //`expect_count` int DEFAULT NULL COMMENT '期望值',
    private Integer expectCount;
    //`create_time` datetime(3) DEFAULT NULL COMMENT '任务创建时间',
    private Date createTime;
    //`created_by` varchar(60) DEFAULT NULL COMMENT '创建人',
    private String createdBy;
    //`modified_time` datetime(3) DEFAULT NULL COMMENT '修改时间',
    private Date modifiedTime;
    //`modified_by` varchar(60) DEFAULT NULL COMMENT '修改人',
    private String modifiedBy;
    //`state` int DEFAULT NULL COMMENT '状态'
    private Integer state;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmConfId() {
        return vmConfId;
    }

    public void setVmConfId(String vmConfId) {
        this.vmConfId = vmConfId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getInternalIp() {
        return internalIp;
    }

    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getEsRuleId() {
        return esRuleId;
    }

    public void setEsRuleId(String esRuleId) {
        this.esRuleId = esRuleId;
    }

    public String getEsRuleName() {
        return esRuleName;
    }

    public void setEsRuleName(String esRuleName) {
        this.esRuleName = esRuleName;
    }

    public Integer getBeforeScalingCount() {
        return beforeScalingCount;
    }

    public void setBeforeScalingCount(Integer beforeScalingCount) {
        this.beforeScalingCount = beforeScalingCount;
    }

    public Integer getAfterScalingCount() {
        return afterScalingCount;
    }

    public void setAfterScalingCount(Integer afterScalingCount) {
        this.afterScalingCount = afterScalingCount;
    }

    public Integer getScalingCount() {
        return scalingCount;
    }

    public void setScalingCount(Integer scalingCount) {
        this.scalingCount = scalingCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Integer getScalingType() {
        return scalingType;
    }

    public void setScalingType(Integer scalingType) {
        this.scalingType = scalingType;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public Date getBegTime() {
        return begTime;
    }

    public void setBegTime(Date begTime) {
        this.begTime = begTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ConfScalingVm{");
        sb.append("recordId='").append(recordId).append('\'');
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", vmName='").append(vmName).append('\'');
        sb.append(", vmConfId='").append(vmConfId).append('\'');
        sb.append(", hostName='").append(hostName).append('\'');
        sb.append(", internalIp='").append(internalIp).append('\'');
        sb.append(", defaultUsername='").append(defaultUsername).append('\'');
        sb.append(", vmRole='").append(vmRole).append('\'');
        sb.append(", es_rule_id='").append(esRuleId).append('\'');
        sb.append(", elRuleName='").append(esRuleName).append('\'');
        sb.append(", beforeScalingCount=").append(beforeScalingCount);
        sb.append(", afterScalingCount=").append(afterScalingCount);
        sb.append(", scalingCount=").append(scalingCount);
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", groupId='").append(groupId).append('\'');
        sb.append(", skuName='").append(skuName).append('\'');
        sb.append(", purchaseType=").append(purchaseType);
        sb.append(", scalingType=").append(scalingType);
        sb.append(", operationType=").append(operationType);
        sb.append(", begTime=").append(begTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", expectCount=").append(expectCount);
        sb.append(", createdTime=").append(createTime);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", modifiedBy='").append(modifiedBy).append('\'');
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}