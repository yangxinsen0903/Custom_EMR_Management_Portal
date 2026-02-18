package com.sunbox.sdpcompose.model.azure.fleet.request;

import java.util.List;
import java.util.Map;

public class VirtualMachineSpec {

    private BaseProfile baseProfile;

    private RegularProfile regularProfile;

    private SpotProfile spotProfile;

    private Map<String,String> virtualMachineTags;

    private List<VMSizesProfile> vmSizesProfile;

    public BaseProfile getBaseProfile() {
        return baseProfile;
    }

    public void setBaseProfile(BaseProfile baseProfile) {
        this.baseProfile = baseProfile;
    }

    public RegularProfile getRegularProfile() {
        return regularProfile;
    }

    public void setRegularProfile(RegularProfile regularProfile) {
        this.regularProfile = regularProfile;
    }

    public SpotProfile getSpotProfile() {
        return spotProfile;
    }

    public void setSpotProfile(SpotProfile spotProfile) {
        this.spotProfile = spotProfile;
    }

    public Map<String, String> getVirtualMachineTags() {
        return virtualMachineTags;
    }

    public void setVirtualMachineTags(Map<String, String> virtualMachineTags) {
        this.virtualMachineTags = virtualMachineTags;
    }

    public List<VMSizesProfile> getVmSizesProfile() {
        return vmSizesProfile;
    }

    public void setVmSizesProfile(List<VMSizesProfile> vmSizesProfile) {
        this.vmSizesProfile = vmSizesProfile;
    }

    @Override
    public String toString() {
        return "VirtualMachineSpec{" +
                "baseProfile=" + baseProfile +
                ", regularProfile=" + regularProfile +
                ", spotProfile=" + spotProfile +
                ", virtualMachineTags=" + virtualMachineTags +
                ", vmSizesProfile=" + vmSizesProfile +
                '}';
    }
}
