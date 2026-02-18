package com.sunbox.domain;

import java.util.Date;
import java.util.List;

public class ConfScalingTaskVm {

    private String vmDetailId;

    private String taskId;

    private String sku;

    private String osImageid;

    private String osImageType;

    private String osVersion;

    private Integer osVolumeSize;

    private String osVolumeType;

    private String vcpus;

    private String memory;

    private Integer count;

    private Integer purchaseType;

    private Integer state;

    private String vmConfId;

    private Date createdTime;

    private String createdby;

    private List<ConfScalingVmDataVol> vmDataVolumes;

    public List<ConfScalingVmDataVol> getVmDataVolumes() {
        return vmDataVolumes;
    }

    public void setVmDataVolumes(List<ConfScalingVmDataVol> vmDataVolumes) {
        this.vmDataVolumes = vmDataVolumes;
    }

    public String getVmConfId() {
        return vmConfId;
    }

    public void setVmConfId(String vmConfId) {
        this.vmConfId = vmConfId;
    }

    public String getVmDetailId() {
        return vmDetailId;
    }

    public void setVmDetailId(String vmDetailId) {
        this.vmDetailId = vmDetailId == null ? null : vmDetailId.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku == null ? null : sku.trim();
    }

    public String getOsImageid() {
        return osImageid;
    }

    public void setOsImageid(String osImageid) {
        this.osImageid = osImageid == null ? null : osImageid.trim();
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType == null ? null : osImageType.trim();
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion == null ? null : osVersion.trim();
    }

    public Integer getOsVolumeSize() {
        return osVolumeSize;
    }

    public void setOsVolumeSize(Integer osVolumeSize) {
        this.osVolumeSize = osVolumeSize;
    }

    public String getOsVolumeType() {
        return osVolumeType;
    }

    public void setOsVolumeType(String osVolumeType) {
        this.osVolumeType = osVolumeType == null ? null : osVolumeType.trim();
    }

    public String getVcpus() {
        return vcpus;
    }

    public void setVcpus(String vcpus) {
        this.vcpus = vcpus == null ? null : vcpus.trim();
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory == null ? null : memory.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    @Override
    public String toString() {
        return "ConfScalingTaskVm{" +
                "vmDetailId='" + vmDetailId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", sku='" + sku + '\'' +
                ", osImageid='" + osImageid + '\'' +
                ", osImageType='" + osImageType + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", osVolumeSize=" + osVolumeSize +
                ", osVolumeType='" + osVolumeType + '\'' +
                ", vcpus='" + vcpus + '\'' +
                ", memory='" + memory + '\'' +
                ", count=" + count +
                ", purchaseType=" + purchaseType +
                ", state=" + state +
                ", createdTime=" + createdTime +
                ", createdby='" + createdby + '\'' +
                '}';
    }
}