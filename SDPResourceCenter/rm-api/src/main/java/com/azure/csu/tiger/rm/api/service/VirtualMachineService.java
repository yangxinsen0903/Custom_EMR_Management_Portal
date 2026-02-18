package com.azure.csu.tiger.rm.api.service;

import com.azure.csu.tiger.rm.api.request.*;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ListVmResponse;

import java.io.IOException;

public interface VirtualMachineService {

    JobResponse createClusterGroups(CreateVmsRequest request) throws IOException;

    JobResponse appendClusterGroups(AppendVmsRequest request) throws IOException;

    JobResponse updateVmsDataDisk(UpdateVmsDiskSizeRequest request) throws IOException;

    JobResponse deleteVirtualMachine(String clusterName, String vmName, String dnsName);

    JobResponse deleteVirtualMachines(DeleteVirtualMachinesRequest request);

    ListVmResponse listVirtualMachines(ListVmsRequest request);

    JobResponse deleteClusterGroup(String cluster, String group);

    boolean updateVmsDataDiskIopsAndMbps(UpdateVmsDiskIopsAndMbpsRequest request);

    JobResponse updateVmsDataDiskIopsAndMbpsWithArm(UpdateVmsDiskIopsAndMbpsRequest request) throws IOException;

}
