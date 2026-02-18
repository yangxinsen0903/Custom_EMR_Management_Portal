package com.sunbox.sdpcompose.model.azure.request;

public class AzureVMGroupsRequest {

    private String groupName;

    private Integer count;

    private Integer beginIndex;

    /**
     * 是否跨物理机申请主机
     * VM_Standalone: 不强制跨物理机申请虚拟机
     * VMSS_Flexible: 跨物理机申请虚拟机
     */
    private String provisionType;

    private AzureVMSpecRequest virtualMachineSpec;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(Integer beginIndex) {
        this.beginIndex = beginIndex;
    }

    public String getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(String provisionType) {
        this.provisionType = provisionType;
    }

    public AzureVMSpecRequest getVirtualMachineSpec() {
        return virtualMachineSpec;
    }

    public void setVirtualMachineSpec(AzureVMSpecRequest virtualMachineSpec) {
        this.virtualMachineSpec = virtualMachineSpec;
    }
}
