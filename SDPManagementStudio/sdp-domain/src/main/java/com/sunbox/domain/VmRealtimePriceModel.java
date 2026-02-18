package com.sunbox.domain;

public class VmRealtimePriceModel {
    private String vmName;
    private double rtPrice;
    private double stdPrice;

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
        return "{" +
                "\"vmName\":\"" + vmName + '"' +
                ", \"rtPrice\":" + rtPrice +
                ", \"stdPrice\":" + stdPrice +
                "}";
    }
}
