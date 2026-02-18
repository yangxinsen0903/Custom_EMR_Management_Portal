package com.sunbox.sdpcompose.model.azure.request;

/**
 * MASTER，CORE，TASK各有一个对象
 */
public class VmGroups {
    /**
     * 创建主机数量，下面规格的主机的数量
     */
    private long count;
    /**
     * 虚拟机组名称，AMBARI | MASTER | CORE | TASK
     */
    private String groupName;
    /**
     * 创建后初始化地址，脚本在Blob上的地址
     */
    private String initScript;
    /**
     * 是否跨物理机申请主机
     * VM_Standalone: 不强制跨物理机申请虚拟机
     * VMSS_Flexible: 跨物理机申请虚拟机
     */
    private String provisionType;
    /**
     * 申请开通主机规格
     */
    private SpecClass virtualMachineSpec;

    public long getCount() {
        return count;
    }

    public void setCount(long value) {
        this.count = value;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String value) {
        this.groupName = value;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String value) {
        this.initScript = value;
    }

    public String getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(String provisionType) {
        this.provisionType = provisionType;
    }

    public SpecClass getVirtualMachineSpec() {
        return virtualMachineSpec;
    }

    public void setVirtualMachineSpec(SpecClass value) {
        this.virtualMachineSpec = value;
    }

    @Override
    public String toString() {
        return "VmGroups{" +
                "count=" + count +
                ", groupName='" + groupName + '\'' +
                ", initScript='" + initScript + '\'' +
                ", spec=" + virtualMachineSpec +
                '}';
    }
}