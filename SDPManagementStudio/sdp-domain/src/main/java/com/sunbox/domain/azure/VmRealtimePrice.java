package com.sunbox.domain.azure;

/**
 * @author : [niyang]
 * @className : VmRealtimePrice
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/28 3:09 PM]
 */
public class VmRealtimePrice {

    private String vmName;
    private double rtPrice;
    private double stdPrice;

    public VmRealtimePrice(){

    }

    public VmRealtimePrice(String vmName, double rtPrice, double stdPrice) {
        this.vmName = vmName;
        this.rtPrice = rtPrice;
        this.stdPrice = stdPrice;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public double getRtPrice() {
        return rtPrice;
    }

    public void setRtPrice(double rtPrice) {
        this.rtPrice = rtPrice;
    }

    public double getStdPrice() {
        return stdPrice;
    }

    public void setStdPrice(double stdPrice) {
        this.stdPrice = stdPrice;
    }

    @Override
    public String toString() {
        return "VmRealtimePrice{" +
                "vmName='" + vmName + '\'' +
                ", rtPrice=" + rtPrice +
                ", stdPrice=" + stdPrice +
                '}';
    }
}
