package com.azure.csu.tiger.rm.api.service.impl;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.enums.JobStatus;
import com.azure.csu.tiger.rm.api.enums.JobType;
import com.azure.csu.tiger.rm.api.enums.ProvisionType;
import com.azure.csu.tiger.rm.api.enums.SSHPublicKeyType;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureArmHelper;
import com.azure.csu.tiger.rm.api.helper.AzureResourceGraphHelper;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.helper.JobHelper;
import com.azure.csu.tiger.rm.api.request.*;
import com.azure.csu.tiger.rm.api.response.GetVmInfoVo;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ListVmResponse;
import com.azure.csu.tiger.rm.api.service.VirtualMachineService;
import com.azure.csu.tiger.rm.api.utils.*;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VirtualMachineServiceImpl implements VirtualMachineService {

    private static final Logger logger = LoggerFactory.getLogger(VirtualMachineServiceImpl.class);
    @Autowired
    private AzureArmHelper azureArmHelper;
    @Autowired
    private JobHelper jobHelper;
    @Autowired
    private AzureResourceHelper azureResourceHelper;
    @Autowired
    private JobDao jobDao;
    @Autowired
    private AzureResourceGraphHelper azureResourceGraphHelper;
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private DnsUtil dnsUtil;

    @Transactional
    @Override
    public JobResponse createClusterGroups(CreateVmsRequest request) throws IOException {
        String region = request.getRegion();
        String clusterName = request.getClusterName();
        String jobArgs = JsonUtil.obj2String(request);

        JobResponse response = JobResponse.from(ConstantUtil.buildCreateVirtualMachinesJobId(clusterName),
                ConstantUtil.getClusterDeploymentName(clusterName),
                JobType.CreateVirtualMachines.name(),
                JobStatus.Started.name());
        String sysCreateBatch = ConstantUtil.md5Hex(response.getId());

        List<VirtualMachineGroup> virtualMachineGroups = request.getVirtualMachineGroups();

        JsonObject armObject = azureArmHelper.getArmObject(AzureArmHelper.CREATE_GROUP);
        JsonArray resourcesArray = azureArmHelper.getResourcesArray(armObject);

        for (VirtualMachineGroup group : virtualMachineGroups) {
            if (!CollectionUtils.isEmpty(request.getClusterTags())) {
                group.getVirtualMachineSpec().getVirtualMachineTags().putAll(request.getClusterTags());
            }
            if (ProvisionType.AZURE_FLEET.getName().equals(group.getProvisionType())) {
                JsonObject fleetObject = azureArmHelper.getOneCopiedObject(resourcesArray);
                generateAzureFleet(region, clusterName, group, fleetObject, sysCreateBatch);
                resourcesArray.add(fleetObject);
            }
        }
        resourcesArray.remove(0);
        logger.info("Arm create group object: {}", armObject);
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), convertRequest(jobArgs, sysCreateBatch));
        azureResourceHelper.createArmTemplate(armObject,
                response.getName(),
                ConstantUtil.getResourceGroupName(clusterName));

        return response;

    }

    @Override
    public JobResponse appendClusterGroups(AppendVmsRequest request) throws IOException {
        String region = request.getRegion();
        String clusterName = request.getClusterName();
        String jobArgs = JsonUtil.obj2String(request);

        JobResponse response = JobResponse.from(ConstantUtil.buildAppendVirtualMachinesJobId(clusterName, jobArgs),
                ConstantUtil.getAppendVirtualMachinesDeployName(clusterName, jobArgs),
                JobType.AppendVirtualMachines.name(),
                JobStatus.Started.name());
        String sysCreateBatch = ConstantUtil.md5Hex(response.getId());

        List<VirtualMachineGroup> virtualMachineGroups = request.getVirtualMachineGroups();

        JsonObject armObject = azureArmHelper.getArmObject(AzureArmHelper.CREATE_GROUP);
        JsonArray resourcesArray = azureArmHelper.getResourcesArray(armObject);

        for (VirtualMachineGroup group : virtualMachineGroups) {
            if (ProvisionType.AZURE_FLEET.getName().equals(group.getProvisionType())) {
                JsonObject fleetObject = azureArmHelper.getOneCopiedObject(resourcesArray);
                generateAzureFleet(region, clusterName, group, fleetObject, sysCreateBatch);
                resourcesArray.add(fleetObject);
            }
        }
        resourcesArray.remove(0);
        logger.info("Arm append group object: {}", armObject);
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), convertRequest(jobArgs, sysCreateBatch));
        azureResourceHelper.createArmTemplate(armObject,
                response.getName(),
                ConstantUtil.getResourceGroupName(clusterName));

        return response;
    }

    @Override
    public JobResponse updateVmsDataDisk(UpdateVmsDiskSizeRequest request) throws IOException {

        String resourceGroup = ConstantUtil.getResourceGroupName(request.getClusterName());

        String jobArgs = JsonUtil.obj2String(request);

        JobResponse response = JobResponse.from(ConstantUtil.buildUpdateDataDiskSizeJobId(request.getClusterName(), jobArgs),
                ConstantUtil.getUpdateDataDiskSizeDeployName(request.getClusterName(), jobArgs),
                JobType.UpdateVirtualMachinesDiskSize.name(),
                JobStatus.Started.name());

        HashSet<String> vmNamesSet = Sets.newHashSet(request.getVmNames());

        HashSet<String> vmIdSet = Sets.newHashSet(ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(vm -> vmNamesSet.contains(vm.name()))
                .map(vm -> vm.id().toLowerCase())
                .collect(Collectors.toList()));

        List<Disk> filteredDiskList = ArmUtil.getArmData().disks().listByResourceGroup(resourceGroup).stream().filter(disk -> {
            if (!disk.isAttachedToVirtualMachine()) {
                return false;
            } else if (disk.osType() != null) {
                return false;
            } else if (!vmIdSet.contains(disk.virtualMachineId().toLowerCase())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());

        JsonObject armObject = azureArmHelper.getArmObject(AzureArmHelper.UPDATE_DISK);
        JsonArray resourcesArray = azureArmHelper.getResourcesArray(armObject);

        for(Disk disk : filteredDiskList) {
            JsonObject diskObject = azureArmHelper.getOneCopiedObject(resourcesArray);
            diskObject.addProperty("name", disk.name());
            diskObject.addProperty("location", disk.regionName());
            diskObject.getAsJsonObject("properties").addProperty("diskSizeGB", request.getNewDataDiskSizeGB());
            resourcesArray.add(diskObject);
        }
        resourcesArray.remove(0);
        logger.info("Arm update disk object: {}", armObject.toString());
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), convertRequest(jobArgs, null));
        azureResourceHelper.createArmTemplate(armObject,
                response.getName(),
                ConstantUtil.getResourceGroupName(request.getClusterName()));

        return response;
    }

    @Override
    public JobResponse deleteVirtualMachine(String clusterName, String vmName, String dnsName) {
        JobResponse response = JobResponse.from(ConstantUtil.buildDeleteVirtualMachineJobId(vmName),
                ConstantUtil.buildDeleteVirtualMachineJobName(vmName),
                JobType.DeleteVirtualMachine.name(),
                JobStatus.Started.name());
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        if (!azureResourceHelper.existResourceGroup(resourceGroup)) {
            logger.warn("Resource group {} not found", resourceGroup);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("Resource group %s not found", resourceGroup));
        }
        JsonObject o = new JsonObject();
        o.addProperty("vmName", vmName);
        o.addProperty("resourceGroup", resourceGroup);
        o.addProperty("subscriptionId", ArmUtil.getSubData());
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), o.toString());
        String dns = dnsUtil.getPrivateDnsZone(clusterName);
        try {
            VirtualMachine virtualMachine = ArmUtil.getArmData().virtualMachines().getByResourceGroup(resourceGroup, vmName);
            azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(Lists.newArrayList(virtualMachine.computerName().toLowerCase()), dns);
            ArmUtil.getArmData().virtualMachines().beginDeleteByResourceGroup(resourceGroup, vmName, true);
        } catch (ApiErrorException e) {
            if (e.getResponse().getStatusCode() == 404) {
                logger.warn("Virtual machine {} not found under Cluster {}", vmName, clusterName);
                if (StringUtils.hasText(dnsName)) {
                    azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(Lists.newArrayList(dnsName.toLowerCase()), dns);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
        return response;
    }

    @Override
    public JobResponse deleteVirtualMachines(DeleteVirtualMachinesRequest request) {
        String clusterName = request.getClusterName();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        String vmNames = JsonUtil.obj2String(request.getVmNames());
        JsonObject o = new JsonObject();
        o.add("vmNames", JsonParser.parseString(vmNames).getAsJsonArray());
        o.addProperty("resourceGroup", resourceGroup);
        o.addProperty("subscriptionId", ArmUtil.getSubData());
        String jobArgs = o.toString();
        JobResponse response = JobResponse.from(ConstantUtil.buildDeleteVirtualMachinesJobId(clusterName, jobArgs),
                ConstantUtil.buildDeleteVirtualMachinesJobName(clusterName, jobArgs),
                JobType.DeleteVirtualMachines.name(),
                JobStatus.Started.name());
        if (!azureResourceHelper.existResourceGroup(resourceGroup)) {
            logger.warn("Resource group {} not found", resourceGroup);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("Resource group %s not found", resourceGroup));
        }
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), jobArgs);
        String dns = dnsUtil.getPrivateDnsZone(clusterName);
        List<String> hostNames = ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(i -> request.getVmNames().contains(i.name()))
                .map(i -> i.computerName().toLowerCase()).collect(Collectors.toList());
        logger.info("Debug deleteVirtualMachines vmNames {}, hostNames: {}, dnsName: {}", request.getVmNames(), hostNames,request.getDnsNames());
        if (!CollectionUtils.isEmpty(request.getDnsNames())) {
            hostNames = Stream.concat(hostNames.stream(), request.getDnsNames().stream())
                            .map(String::toLowerCase)
                            .distinct()
                            .collect(Collectors.toList());
        }
        azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(hostNames, dns);
        AzureResourceManager azureResourceManager = ArmUtil.getArmData();
        request.getVmNames().parallelStream().forEach(vmName -> {
            int retryTimes = 0;
            while (retryTimes++ < ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
                try {
                    azureResourceManager.virtualMachines().beginDeleteByResourceGroup(resourceGroup, vmName, true);
                    break;
                } catch (ApiErrorException e) {
                    if (e.getResponse().getStatusCode() == 404) {
                        logger.warn("Virtual machine {} not found under Cluster {}", vmName, clusterName);
                    } else {
                        throw new RuntimeException(e);
                    }
                    break;
                } catch (ManagementException ex) {
                    if (ex.getResponse().getStatusCode() == 429) {
                        HttpResponse httpResponse = ex.getResponse();
                        String retryAfterHeader = httpResponse.getHeaderValue(HttpHeaderName.RETRY_AFTER);
                        int retryAfterSeconds;
                        if (retryAfterHeader != null) {
                            retryAfterSeconds = Integer.parseInt(retryAfterHeader);
                            logger.info("Received 429, retrying after " + retryAfterSeconds + " seconds.");
                        } else {
                            retryAfterSeconds = ConstantUtil.AZURE_THROTTLE_WAIT_TIME_SECONDS_DEFAULT;
                            logger.info("Received 429, but no Retry-After header. Retrying after " + retryAfterSeconds + " seconds.");
                        }
                        try {
                            Thread.sleep((retryAfterSeconds + 1) * 1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        });
        return response;
    }

    @Transactional
    @Override
    public JobResponse deleteClusterGroup(String cluster, String group) {
        String resourceGroup = ConstantUtil.getResourceGroupName(cluster);
        JobResponse response = JobResponse.from(ConstantUtil.buildDeleteClusterGroupJobId(cluster, group),
                ConstantUtil.buildDeleteClusterGroupJobName(cluster, group),
                JobType.DeleteComputeFleet.name(),
                JobStatus.Started.name());
        if (!azureResourceHelper.existResourceGroup(resourceGroup)) {
            logger.warn("Resource group {} not found", resourceGroup);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("Resource group %s not found", resourceGroup));
        }
        JsonObject o = new JsonObject();
        o.addProperty("clusterName", cluster);
        o.addProperty("groupName", group);
        o.addProperty("subscriptionId", ArmUtil.getSubData());
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), o.toString());

        String dns = dnsUtil.getPrivateDnsZone(cluster);
        List<String> hostNames = ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(i -> {
                    if (i.tags().containsKey(ConstantUtil.SYS_SDP_CLUSTER) && i.tags().containsKey(ConstantUtil.SYS_SDP_GROUP)) {
                        String c = i.tags().get(ConstantUtil.SYS_SDP_CLUSTER);
                        String g = i.tags().get(ConstantUtil.SYS_SDP_GROUP);
                        if (cluster.equals(c) && group.equals(g)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(i -> i.computerName().toLowerCase()).collect(Collectors.toList());
        azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(hostNames, dns);
        String fleetName = ConstantUtil.buildFleetName(cluster, group);
        int code = httpUtil.doDeleteFleet(resourceGroup, fleetName);
        if (code == HttpStatus.NO_CONTENT.value()) {
            throw new RmException(HttpStatus.NO_CONTENT, "Fleet not found, cluster: " + cluster + ", group: " + group);
        }
        return response;
    }

    @Override
    public boolean updateVmsDataDiskIopsAndMbps(UpdateVmsDiskIopsAndMbpsRequest request) {
        String resourceGroup = ConstantUtil.getResourceGroupName(request.getClusterName());
        HashSet<String> vmNamesSet = Sets.newHashSet(request.getVmNames());
        List<VirtualMachineDataDisk> dataDisks = ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(vm -> vmNamesSet.contains(vm.name()))
                .map(vm -> vm.dataDisks().values())
                .flatMap(Collection::stream)
                .filter(disk -> disk.storageAccountType().equals(StorageAccountTypes.PREMIUM_V2_LRS))
                .collect(Collectors.toList());
        AzureResourceManager azureResourceManager = ArmUtil.getArmData();
        dataDisks.parallelStream().forEach(disk -> {
            ArmUtil.setArmData(azureResourceManager);
            int retryTimes = 0;
            while (retryTimes++ < ConstantUtil.AZURE_THROTTLE_RETRY_TIMES_DEFAULT) {
                try {
                    azureResourceHelper.updateDiskIopsAndMbps(resourceGroup, disk.name(), request.getNewDataDiskIOPSReadWrite(), request.getNewDataDiskMBpsReadWrite());
                    break;
                } catch (ManagementException ex) {
                    if (ex.getResponse().getStatusCode() == 429) {
                        HttpResponse response = ex.getResponse();
                        String retryAfterHeader = response.getHeaderValue(HttpHeaderName.RETRY_AFTER);
                        int retryAfterSeconds;
                        if (retryAfterHeader != null) {
                            retryAfterSeconds = Integer.parseInt(retryAfterHeader);
                            logger.info("Received 429, retrying after " + retryAfterSeconds + " seconds.");
                        } else {
                            retryAfterSeconds = ConstantUtil.AZURE_THROTTLE_WAIT_TIME_SECONDS_DEFAULT;
                            logger.info("Received 429, but no Retry-After header. Retrying after " + retryAfterSeconds + " seconds.");
                        }
                        try {
                            Thread.sleep((retryAfterSeconds + 1) * 1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        });
        return true;
    }

    @Override
    public JobResponse updateVmsDataDiskIopsAndMbpsWithArm(UpdateVmsDiskIopsAndMbpsRequest request) throws IOException {
        String resourceGroup = ConstantUtil.getResourceGroupName(request.getClusterName());

        String jobArgs = JsonUtil.obj2String(request);

        JobResponse response = JobResponse.from(ConstantUtil.buildUpdateDataIopsMbpsJobId(request.getClusterName(), jobArgs),
                ConstantUtil.getUpdateDataDiskIopsMbpsDeployName(request.getClusterName(), jobArgs),
                JobType.UpdateVirtualMachinesDiskIopsAndMbps.name(),
                JobStatus.Started.name());

        HashSet<String> vmNamesSet = Sets.newHashSet(request.getVmNames());
        List<VirtualMachineDataDisk> dataDisks = ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream()
                .filter(vm -> vmNamesSet.contains(vm.name()))
                .map(vm -> vm.dataDisks().values())
                .flatMap(Collection::stream)
                .filter(disk -> disk.storageAccountType().equals(StorageAccountTypes.PREMIUM_V2_LRS))
                .collect(Collectors.toList());

        JsonObject armObject = azureArmHelper.getArmObject(AzureArmHelper.UPDATE_DISK_IOPS_MPBS);
        JsonArray resourcesArray = azureArmHelper.getResourcesArray(armObject);

        for(VirtualMachineDataDisk disk : dataDisks) {
            JsonObject diskObject = azureArmHelper.getOneCopiedObject(resourcesArray);
            diskObject.addProperty("name", disk.name());
            diskObject.addProperty("location", request.getRegion());
            diskObject.getAsJsonObject("properties").addProperty("diskSizeGB", disk.size());
            diskObject.getAsJsonObject("properties").addProperty("diskIOPSReadWrite", request.getNewDataDiskIOPSReadWrite());
            diskObject.getAsJsonObject("properties").addProperty("diskMBpsReadWrite", request.getNewDataDiskMBpsReadWrite());
            resourcesArray.add(diskObject);
        }
        resourcesArray.remove(0);
        logger.info("Arm update disk iops and mbps object: {}", armObject.toString());
        jobHelper.saveJob(response.getId(), response.getName(), response.getType() , response.getStatus(), convertRequest(jobArgs, null));
        azureResourceHelper.createArmTemplate(armObject,
                response.getName(),
                ConstantUtil.getResourceGroupName(request.getClusterName()));

        return response;
    }

    @Override
    public ListVmResponse listVirtualMachines(ListVmsRequest request) {
        List<String> subNetIds = request.getSubNetIds();
        String region = request.getRegion();
        int pageNo = request.getPageNo() == null ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() == null ? Integer.MAX_VALUE : request.getPageSize();
        String queryFormat = azureResourceGraphHelper.getVmQueryBySubNet();
        String regionQuery = StringUtils.hasText(region) ? String.format("location =~ \"%s\"", region) : "1 == 1";
        String subNetIdsQuery = CollectionUtils.isEmpty(subNetIds) ? "1 == 1":
                String.format("ipConfigs.properties.subnet.id in~ (%s)",subNetIds.stream().map(i -> String.format("\"%s\"", i)).collect(Collectors.joining(",")));
        String query = String.format(queryFormat, regionQuery, subNetIdsQuery, (pageNo - 1) * pageSize, pageSize);
        JsonArray queryResult = azureResourceGraphHelper.executeQuery(query, null);
        List<GetVmInfoVo> vmInfos = GetVmInfoVo.from(queryResult);
        return ListVmResponse.from(vmInfos, request.getPageNo(), request.getPageSize());
    }

    private void generateAzureFleet(String region, String clusterName, VirtualMachineGroup group, JsonObject fleetObject, String sysCreateBatch) {
        if (SSHPublicKeyType.KeyVaultSecret.name().equals(group.getVirtualMachineSpec().getBaseProfile().getSshPublicKeyType())) {
            String kvId = group.getVirtualMachineSpec().getBaseProfile().getSshKeyVaultId();
            String secretName = group.getVirtualMachineSpec().getBaseProfile().getSshPublicKeySecretName();
            String sshPublicKey = azureResourceHelper.getSshPublicKeyVer2(kvId, secretName);
            group.getVirtualMachineSpec().getBaseProfile().setSshPublicKey(sshPublicKey);
        }
        azureArmHelper.setResourceBase(region, clusterName, group, fleetObject, sysCreateBatch);
        azureArmHelper.setVmSizeProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonArray("vmSizesProfile"));
        if (group.getVirtualMachineSpec().getSpotProfile() == null) {
            fleetObject.getAsJsonObject("properties").remove("spotPriorityProfile");
        } else {
            azureArmHelper.setSpotProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonObject("spotPriorityProfile"));
        }
        if (group.getVirtualMachineSpec().getRegularProfile() == null) {
            fleetObject.getAsJsonObject("properties").remove("regularPriorityProfile");
        } else {
            azureArmHelper.setRegularProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonObject("regularPriorityProfile"));
        }
        azureArmHelper.setStorageProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonObject("computeProfile").getAsJsonObject("baseVirtualMachineProfile").getAsJsonObject("storageProfile"));
        azureArmHelper.setOsProfile(clusterName, group, fleetObject.getAsJsonObject("properties").getAsJsonObject("computeProfile").getAsJsonObject("baseVirtualMachineProfile").getAsJsonObject("osProfile"));
        azureArmHelper.setNetworkProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonObject("computeProfile").getAsJsonObject("baseVirtualMachineProfile").getAsJsonObject("networkProfile"));
        if (group.getVirtualMachineSpec().getBaseProfile().getStartupScriptBlobUrl() == null) {
            fleetObject.getAsJsonObject("properties").getAsJsonObject("computeProfile").getAsJsonObject("baseVirtualMachineProfile").remove("extensionProfile");
        } else {
            azureArmHelper.setExtensionProfile(group, fleetObject.getAsJsonObject("properties").getAsJsonObject("computeProfile").getAsJsonObject("baseVirtualMachineProfile").getAsJsonObject("extensionProfile").getAsJsonArray("extensions").get(0).getAsJsonObject().getAsJsonObject("properties"));
        }
    }

    private String convertRequest(String request, String sysCreateBatch) {
        JsonObject requestObject = JsonParser.parseString(request).getAsJsonObject();
        JsonObject rawRequest = new JsonObject();
        rawRequest.add("RawRequest", requestObject);
        if (sysCreateBatch != null) {
            rawRequest.addProperty("SysCreateBatch", sysCreateBatch);
        }
        rawRequest.addProperty("subscriptionId", ArmUtil.getSubData());
        return rawRequest.toString();
    }

}
