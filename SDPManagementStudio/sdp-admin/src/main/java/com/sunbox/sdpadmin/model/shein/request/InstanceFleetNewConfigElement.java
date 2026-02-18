package com.sunbox.sdpadmin.model.shein.request;

import java.util.List;

public class InstanceFleetNewConfigElement {
    private List<CandidateInsConfig> candidateInsConfigs;
    private String insFtRole;
    private long targetOnDemandCapacity;
    private long targetSpotCapacity;

    public List<CandidateInsConfig> getCandidateInsConfigs() { return candidateInsConfigs; }
    public void setCandidateInsConfigs(List<CandidateInsConfig> value) { this.candidateInsConfigs = value; }

    public String getInsFtRole() { return insFtRole; }
    public void setInsFtRole(String value) { this.insFtRole = value; }

    public long getTargetOnDemandCapacity() { return targetOnDemandCapacity; }
    public void setTargetOnDemandCapacity(long value) { this.targetOnDemandCapacity = value; }

    public long getTargetSpotCapacity() { return targetSpotCapacity; }
    public void setTargetSpotCapacity(long value) { this.targetSpotCapacity = value; }
}
