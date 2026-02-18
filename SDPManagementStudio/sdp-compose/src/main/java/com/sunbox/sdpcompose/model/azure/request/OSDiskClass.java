package com.sunbox.sdpcompose.model.azure.request;

/**
 * 系盘信息
 */
public class OSDiskClass {
    /**
     * 大小
     */
    private long osDiskSizeGB;
    /**
     * 磁盘skuName，Standard_LRS： 标准HDD ；StandardSSD_LRS： 标准SSD； Premium_LRS：高级SSD
     */
    private String osDiskSku;

    public long getOsDiskSizeGB() { return osDiskSizeGB; }
    public void setOsDiskSizeGB(long value) { this.osDiskSizeGB = value; }

    public String getOsDiskSku() { return osDiskSku; }
    public void setOsDiskSku(String value) { this.osDiskSku = value; }

    @Override
    public String toString() {
        return "OSDiskClass{" +
                "sizeGB=" + osDiskSizeGB +
                ", skuName='" + osDiskSku + '\'' +
                '}';
    }
}