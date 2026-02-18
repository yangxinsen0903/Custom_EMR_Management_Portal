package com.sunbox.sdpcompose.model.azure.fleet.request;

public class AzureVMGroupRequest {
    private Integer beginIndex;

    private Integer count;

    private String groupName;

    private String vmRole;

    private String provisionType;

    private VirtualMachineSpec virtualMachineSpec;

    public Integer getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(Integer beginIndex) {
        this.beginIndex = beginIndex;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(String provisionType) {
        this.provisionType = provisionType;
    }

    public VirtualMachineSpec getVirtualMachineSpec() {
        return virtualMachineSpec;
    }

    public void setVirtualMachineSpec(VirtualMachineSpec virtualMachineSpec) {
        this.virtualMachineSpec = virtualMachineSpec;
    }

    @Override
    public String toString() {
        return "AzureVMGroupRequest{" +
                "beginIndex=" + beginIndex +
                ", count=" + count +
                ", groupName='" + groupName + '\'' +
                ", provisionType='" + provisionType + '\'' +
                ", virtualMachineSpec=" + virtualMachineSpec +
                '}';
    }
}
