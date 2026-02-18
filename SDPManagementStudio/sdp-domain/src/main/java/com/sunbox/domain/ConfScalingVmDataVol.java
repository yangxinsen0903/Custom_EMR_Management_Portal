package com.sunbox.domain;

public class ConfScalingVmDataVol {
    private String vmDataVolId;

    private String vmDetailId;

    private String dataVolumeType;

    private String localVolumeType;

    private Integer dataVolumeSize;

    private Integer count;

    public String getVmDataVolId() {
        return vmDataVolId;
    }

    public void setVmDataVolId(String vmDataVolId) {
        this.vmDataVolId = vmDataVolId == null ? null : vmDataVolId.trim();
    }

    public String getVmDetailId() {
        return vmDetailId;
    }

    public void setVmDetailId(String vmDetailId) {
        this.vmDetailId = vmDetailId == null ? null : vmDetailId.trim();
    }

    public String getDataVolumeType() {
        return dataVolumeType;
    }

    public void setDataVolumeType(String dataVolumeType) {
        this.dataVolumeType = dataVolumeType == null ? null : dataVolumeType.trim();
    }

    public String getLocalVolumeType() {
        return localVolumeType;
    }

    public void setLocalVolumeType(String localVolumeType) {
        this.localVolumeType = localVolumeType == null ? null : localVolumeType.trim();
    }

    public Integer getDataVolumeSize() {
        return dataVolumeSize;
    }

    public void setDataVolumeSize(Integer dataVolumeSize) {
        this.dataVolumeSize = dataVolumeSize;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ConfScalingVmDataVol{" +
                "vmDataVolId='" + vmDataVolId + '\'' +
                ", vmDetailId='" + vmDetailId + '\'' +
                ", dataVolumeType='" + dataVolumeType + '\'' +
                ", localVolumeType='" + localVolumeType + '\'' +
                ", dataVolumeSize=" + dataVolumeSize +
                ", count=" + count +
                '}';
    }
}