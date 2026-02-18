package com.azure.csu.tiger.rm.api.enums;

public enum JobType {
    Unknown,
    DeleteResourceGroup,
    CreateVirtualMachines,
    CreateVirtualMachine,
    DeleteVirtualMachine,
    AppendVirtualMachines,
    DeleteVirtualMachines,
    UpdateVirtualMachinesDiskSize,
    UpdateVirtualMachinesDiskIopsAndMbps,
    DeleteComputeFleet
}
