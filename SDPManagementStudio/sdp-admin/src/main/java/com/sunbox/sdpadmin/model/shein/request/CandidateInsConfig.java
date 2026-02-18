package com.sunbox.sdpadmin.model.shein.request;

import java.util.List;

public class CandidateInsConfig {
    private String insBidPrice;
    private String insBidPriceAsPercentageOfOnDemandPrice;
    private List<InsCfg> insCfgs;
    private long insEquivalentUnit;
    private String insType;
    private Long volumeIOps;
    private long volumeSizeInGB;
    private String volumeType;

    public String getInsBidPrice() { return insBidPrice; }
    public void setInsBidPrice(String value) { this.insBidPrice = value; }

    public String getInsBidPriceAsPercentageOfOnDemandPrice() { return insBidPriceAsPercentageOfOnDemandPrice; }
    public void setInsBidPriceAsPercentageOfOnDemandPrice(String value) { this.insBidPriceAsPercentageOfOnDemandPrice = value; }

    public List<InsCfg> getInsCfgs() { return insCfgs; }
    public void setInsCfgs(List<InsCfg> value) { this.insCfgs = value; }

    public long getInsEquivalentUnit() { return insEquivalentUnit; }
    public void setInsEquivalentUnit(long value) { this.insEquivalentUnit = value; }

    public String getInsType() { return insType; }
    public void setInsType(String value) { this.insType = value; }

    public Long getVolumeIOps() { return volumeIOps; }
    public void setVolumeIOps(Long value) { this.volumeIOps = value; }

    public long getVolumeSizeInGB() { return volumeSizeInGB; }
    public void setVolumeSizeInGB(long value) { this.volumeSizeInGB = value; }

    public String getVolumeType() { return volumeType; }
    public void setVolumeType(String value) { this.volumeType = value; }
}