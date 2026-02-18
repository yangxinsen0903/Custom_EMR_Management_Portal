package com.sunbox.domain;

import java.util.Date;

public class ConfGroupElasticScaling {
    public static final Integer ISVALID_YES = 1;
    public static final Integer ISVALID_NO = 0;

    private String groupEsId;

    private String clusterId;

    private String groupName;

    private Integer isGracefulScalein;
    private Integer scaleinWaitingTime;
    private Integer enableBeforestartScript;
    private Integer enableAfterstartScript;
    private Integer isFullCustody;
    /** 全托管自定义参数 */
    private String fullCustodyParam;

    private String vmRole;

    private Integer maxCount;

    private Integer minCount;

    private Date scalingLimitTime;

    private String createdby;

    private Date createdTime;

    private String modifiedby;

    private Date modifiedTime;

    private Integer isValid;


    public String getFullCustodyParam() {
        return fullCustodyParam;
    }

    public void setFullCustodyParam(String fullCustodyParam) {
        this.fullCustodyParam = fullCustodyParam;
    }

    public FullCustodyParam getFullCustodyParamObject() {
        return FullCustodyParam.parse(fullCustodyParam);
    }

    public String getGroupEsId() {
        return groupEsId;
    }

    public void setGroupEsId(String groupEsId) {
        this.groupEsId = groupEsId == null ? null : groupEsId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole == null ? null : vmRole.trim();
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

    public Date getScalingLimitTime() {
        return scalingLimitTime;
    }

    public void setScalingLimitTime(Date scalingLimitTime) {
        this.scalingLimitTime = scalingLimitTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
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
        this.modifiedby = modifiedby == null ? null : modifiedby.trim();
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Integer getIsFullCustody() {
        return isFullCustody;
    }

    public void setIsFullCustody(Integer isFullCustody) {
        this.isFullCustody = isFullCustody;
    }

    public Integer getIsGracefulScalein() {
        return isGracefulScalein;
    }

    public void setIsGracefulScalein(Integer isGracefulScalein) {
        this.isGracefulScalein = isGracefulScalein;
    }

    public Integer getScaleinWaitingTime() {
        return scaleinWaitingTime;
    }

    public void setScaleinWaitingTime(Integer scaleinWaitingTime) {
        this.scaleinWaitingTime = scaleinWaitingTime;
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

    @Override
    public String toString() {
        return "ConfGroupElasticScaling{" +
                "groupEsId='" + groupEsId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", maxCount=" + maxCount +
                ", minCount=" + minCount +
                ", scalingLimitTime=" + scalingLimitTime +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedby='" + modifiedby + '\'' +
                ", modifiedTime=" + modifiedTime +'\'' +
                ",isFullCustody='"+isFullCustody+
                '}';
    }
}