package com.sunbox.sdpadmin.model.shein.request;

/**
 * EMR 集群托管伸缩策略
 */
public class Emr {
    private long maxCoreNodeUnit;
    private long maxUnit;
    private long minUnit;
    private long onDemandUnitLimit;

    public long getMaxCoreNodeUnit() { return maxCoreNodeUnit; }
    public void setMaxCoreNodeUnit(long value) { this.maxCoreNodeUnit = value; }

    public long getMaxUnit() { return maxUnit; }
    public void setMaxUnit(long value) { this.maxUnit = value; }

    public long getMinUnit() { return minUnit; }
    public void setMinUnit(long value) { this.minUnit = value; }

    public long getOnDemandUnitLimit() { return onDemandUnitLimit; }
    public void setOnDemandUnitLimit(long value) { this.onDemandUnitLimit = value; }
}