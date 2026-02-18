package com.sunbox.sdpcompose.model.azure.request;

public class DataDiskElement {
    /**
     * 磁盘数量
     */
    private long dataDiskCount;
    /**
     * 磁盘大小
     */
    private long dataDiskSizeGB;
    /**
     * 磁盘skuName，Standard_LRS： 标准HDD ；StandardSSD_LRS： 标准SSD； Premium_LRS：高级SSD
     */
    private String dataDiskSku;

    public long getDataDiskCount() { return dataDiskCount; }
    public void setDataDiskCount(long value) { this.dataDiskCount = value; }

    public long getDataDiskSizeGB() { return dataDiskSizeGB; }
    public void setDataDiskSizeGB(long value) { this.dataDiskSizeGB = value; }

    public String getDataDiskSku() { return dataDiskSku; }
    public void setDataDiskSku(String value) { this.dataDiskSku = value; }

    @Override
    public String toString() {
        return "DataDiskElement{" +
                "count=" + dataDiskCount +
                ", sizeGB=" + dataDiskSizeGB +
                ", skuName='" + dataDiskSku + '\'' +
                '}';
    }
}