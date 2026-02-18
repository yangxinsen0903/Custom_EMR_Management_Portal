package com.azure.csu.tiger.rm.api.service.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.enums.JobStatus;
import com.azure.csu.tiger.rm.api.enums.JobType;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureResourceGraphHelper;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.request.CreateVmsRequest;
import com.azure.csu.tiger.rm.api.request.UpdateVmsDiskIopsAndMbpsRequest;
import com.azure.csu.tiger.rm.api.request.UpdateVmsDiskSizeRequest;
import com.azure.csu.tiger.rm.api.response.*;
import com.azure.csu.tiger.rm.api.service.JobService;
import com.azure.csu.tiger.rm.api.utils.ArmUtil;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.HttpUtil;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.ProvisioningState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private JobDao jobDao;
    @Autowired
    private AzureResourceGraphHelper azureResourceGraphHelper;
    @Autowired
    private ResourceGraphManager resourceGraphManager;
    @Autowired
    private AzureResourceHelper azureResourceHelper;

    @Override
    public GetJobStatusResponse getJobStatus(String jobId) {
        SdpRmJobsRecord job = jobDao.findJob(jobId);
        if (job == null) {
            logger.warn("Job {} not found", jobId);
            return null;
        }

        if (job.getType().equals(JobType.CreateVirtualMachines.name())) {
            return getCreateVirtualMachinesJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.DeleteResourceGroup.name())) {
            return getDeleteResourceGroupJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.UpdateVirtualMachinesDiskSize.name())) {
            return getUpdateVirtualMachinesDiskSizeJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.UpdateVirtualMachinesDiskIopsAndMbps.name())) {
            return getUpdateVirtualMachinesDiskIopsMbpsJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.DeleteVirtualMachine.name())) {
            return getDeleteVirtualMachineJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.DeleteVirtualMachines.name())) {
            return getDeleteVirtualMachinesJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.AppendVirtualMachines.name())) {
            return getAppendVirtualMachinesJobStatus(jobId, job);
        } else if (job.getType().equals(JobType.DeleteComputeFleet.name())) {
            return getDeleteClusterGroupJobStatus(jobId, job);
        }
        return GetJobStatusResponse.from(new JobResponse(), "job type not supported");
    }

    @Override
    public ProvisionJobDetailResponse getJobProvisionDetail(String jobId) {
        SdpRmJobsRecord job = jobDao.findJob(jobId);
        if (job == null) {
            logger.warn("Job {} not found", jobId);
            return null;
        }
        if (!JobType.CreateVirtualMachines.name().equals(job.getType()) || !JobType.AppendVirtualMachines.name().equals(job.getType())) {
            return null;
        }
        String deploymentName = job.getName();
        JsonObject rawRequest = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("RawRequest").getAsJsonObject();
        String sysCreateBatch = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("SysCreateBatch").getAsString();
        CreateVmsRequest request = JsonUtil.string2Obj(rawRequest.toString(), CreateVmsRequest.class);
        String dnsName = request.getVirtualMachineGroups().get(0).getVirtualMachineSpec().getBaseProfile().getHostNameSuffix();
        String clusterName = request.getClusterName();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        Deployment deployment = ArmUtil.getArmData().deployments().getByResourceGroup(resourceGroup, deploymentName);
        if (deployment == null) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Deployment not found: " + deploymentName);
        }
        logger.info("Deployment {} found, provisioningState is {}", deploymentName, deployment.provisioningState());
        if (deployment.provisioningState() == ProvisioningState.SUCCEEDED.toString()) {
            List<GetGroupInfoVo> groupInfo = getGroupInfo(request.getVirtualMachineGroups().stream()
                    .map(i -> ConstantUtil.buildFleetName(clusterName, i.getGroupName()))
                    .collect(Collectors.toList()), sysCreateBatch);
            return ProvisionJobDetailResponse.success(jobId, clusterName, groupInfo);
        }

//        JobResponse jobResponse = null;
//        if (deployment.provisioningState() == ProvisioningState.FAILED.toString()) {
//            jobResponse = JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name());
//        } else if (deployment.provisioningState() == ProvisioningState.SUCCEEDED.toString()) {
//            jobResponse = JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name());
//        } else {
//            throw new RmException(HttpStatus.BAD_REQUEST, "Unknown provisioning state: " + deployment.provisioningState());
//        }
//        List<GetGroupInfoVo> groupInfo = getGroupInfo(request.getVirtualMachineGroups().stream()
//                .map(i -> ConstantUtil.buildFleetName(clusterName, i.getGroupName()))
//                .collect(Collectors.toList()), sysCreateBatch);
//        if (!CollectionUtils.isEmpty(groupInfo)) {
//            azureResourceHelper.registerVirtualMachinesToPrivateDns(groupInfo.stream().flatMap(i -> i.getVirtualMachines().stream()).collect(Collectors.toList()), dnsName);
//        }
//        if (deployment.provisioningState() == ProvisioningState.FAILED.toString()) {
//            DeploymentError deploymentError = DeploymentError.from(deployment.error());
//            List<DeploymentOperation> deploymentOperations = deployment.deploymentOperations().list().stream().collect(Collectors.toList());
//            return GetJobStatusResponse.from(jobResponse, groupInfo, DeploymentFailed.from(deploymentError, deploymentOperations));
//        }
//        return GetJobStatusResponse.from(jobResponse, groupInfo);

        return null;
    }

    private GetJobStatusResponse getDeleteClusterGroupJobStatus(String jobId, SdpRmJobsRecord job) {
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
        }
        JsonObject jobArgsObject = JsonParser.parseString(job.getJobargs()).getAsJsonObject();
        String clusterName = jobArgsObject.get("clusterName").getAsString();
        String group = jobArgsObject.get("groupName").getAsString();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        String fleetName = ConstantUtil.buildFleetName(clusterName, group);
        Pair<Integer, String> response = httpUtil.doGetFleet(resourceGroup, fleetName);
        if (response.getLeft() == HttpStatus.NOT_FOUND.value()) {
            jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
        }
        return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));

    }

    private GetJobStatusResponse getDeleteVirtualMachinesJobStatus(String jobId, SdpRmJobsRecord job) {
        JsonObject jobArgsObject = JsonParser.parseString(job.getJobargs()).getAsJsonObject();
        String resourceGroup = jobArgsObject.get("resourceGroup").getAsString();
        List<String> vmNames = JsonUtil.string2Obj(jobArgsObject.get("vmNames").toString(), List.class, String.class);
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()), vmNames);
        }
        if (!azureResourceHelper.existResourceGroup(resourceGroup)) {
            jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()),
                    String.format("Resource group %s not found", resourceGroup));
        }
        AtomicReference<VirtualMachine> virtualMachine = new AtomicReference<>();
        boolean anyMatch = ArmUtil.getArmData().virtualMachines().listByResourceGroup(resourceGroup).stream().parallel().anyMatch(vm -> {
            if (vmNames.contains(vm.name())) {
                logger.info("Virtual machine {} found, provisioningState is {}", vm.name(), vm.provisioningState());
                virtualMachine.set(vm);
                return true;
            }
            return false;
        });
        if (anyMatch) {
            if (virtualMachine.get().provisioningState().equals(ProvisioningState.FAILED.toString())) {
                jobDao.updateJobStatus(JobStatus.Failed.name(), jobId);
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name()), vmNames);
            }
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));
        }
        jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
        return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()), vmNames);
    }

    private GetJobStatusResponse getDeleteVirtualMachineJobStatus(String jobId, SdpRmJobsRecord job) {
        JsonObject jobArgs = JsonParser.parseString(job.getJobargs()).getAsJsonObject();
        String resourceGroup = jobArgs.get("resourceGroup").getAsString();
        String vmName = jobArgs.get("vmName").getAsString();
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
        }
        try {
            VirtualMachine virtualMachine = ArmUtil.getArmData().virtualMachines().getByResourceGroup(resourceGroup, vmName);
            String provisioningState = virtualMachine.provisioningState();
            logger.info("Virtual machine {} provisioning state: {}", vmName, provisioningState);
            if (provisioningState.equals(ProvisioningState.DELETING.toString())
                    || provisioningState.equals(ProvisioningState.ACCEPTED.toString())
            ) {
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));
            } else if (provisioningState.equals(ProvisioningState.FAILED.toString())) {
                jobDao.updateJobStatus(JobStatus.Failed.name(), jobId);
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name()));
            } else if (provisioningState.equals(ProvisioningState.DELETED.toString())) {
                jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
            } else {
                logger.info("Unknown provisioning state: {}", provisioningState);
                jobDao.updateJobStatus(JobStatus.Unknown.name(), jobId);
                return GetJobStatusResponse.from(
                        JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Unknown.name()),
                        "Unknown provisioning state: " + provisioningState);
            }
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
            } else {
                logger.error(e.getMessage());
                jobDao.updateJobStatus(JobStatus.Unknown.name(), jobId);
                return GetJobStatusResponse.from(
                        JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Unknown.name()),
                        "Unknown provisioning state: " + e.getMessage());
            }
        }
    }

    private GetJobStatusResponse getUpdateVirtualMachinesDiskIopsMbpsJobStatus(String jobId, SdpRmJobsRecord job) {
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
        }
        JsonObject rawRequest = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("RawRequest").getAsJsonObject();
        UpdateVmsDiskIopsAndMbpsRequest updateVmsDiskIopsAndMbpsRequest = JsonUtil.string2Obj(rawRequest.toString(), UpdateVmsDiskIopsAndMbpsRequest.class);
        String clusterName = updateVmsDiskIopsAndMbpsRequest.getClusterName();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        String deploymentName = job.getName();

        Deployment deployment = ArmUtil.getArmData().deployments().getByResourceGroup(resourceGroup, deploymentName);
        if (deployment != null) {
            String provisioningState = deployment.provisioningState();
            logger.info("Deployment {} found, provisioningState is {}", deploymentName, provisioningState);
            if (provisioningState.equals(ProvisioningState.RUNNING.toString())
                    || provisioningState.equals(ProvisioningState.ACCEPTED.toString())
                    || provisioningState.equals(ProvisioningState.CREATING.toString())
                    || provisioningState.equals(ProvisioningState.UPDATING.toString())
            ) {
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));
            } else if (provisioningState.equals(ProvisioningState.FAILED.toString())) {
                jobDao.updateJobStatus(JobStatus.Failed.name(), jobId);
                DeploymentError deploymentError = DeploymentError.from(deployment.error());
                List<DeploymentOperation> deploymentOperations = deployment.deploymentOperations().list().stream().collect(Collectors.toList());
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name()),
                        null, DeploymentFailed.from(deploymentError, deploymentOperations));
            }
        }
        jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
        return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));

    }

    private GetJobStatusResponse getUpdateVirtualMachinesDiskSizeJobStatus(String jobId, SdpRmJobsRecord job) {
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));
        }
        JsonObject rawRequest = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("RawRequest").getAsJsonObject();
        UpdateVmsDiskSizeRequest updateVmsDiskSizeRequest = JsonUtil.string2Obj(rawRequest.toString(), UpdateVmsDiskSizeRequest.class);
        String clusterName = updateVmsDiskSizeRequest.getClusterName();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        String deploymentName = job.getName();

        Deployment deployment = ArmUtil.getArmData().deployments().getByResourceGroup(resourceGroup, deploymentName);
        if (deployment != null) {
            String provisioningState = deployment.provisioningState();
            logger.info("Deployment {} found, provisioningState is {}", deploymentName, provisioningState);
            if (provisioningState.equals(ProvisioningState.RUNNING.toString())
                    || provisioningState.equals(ProvisioningState.ACCEPTED.toString())
                    || provisioningState.equals(ProvisioningState.CREATING.toString())
                    || provisioningState.equals(ProvisioningState.UPDATING.toString())
            ) {
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));
            } else if (provisioningState.equals(ProvisioningState.FAILED.toString())) {
                jobDao.updateJobStatus(JobStatus.Failed.name(), jobId);
                DeploymentError deploymentError = DeploymentError.from(deployment.error());
                List<DeploymentOperation> deploymentOperations = deployment.deploymentOperations().list().stream().collect(Collectors.toList());
                return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name()),
                        null, DeploymentFailed.from(deploymentError, deploymentOperations));
            }
        }
        jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
        return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()));

    }

    private GetJobStatusResponse getDeleteResourceGroupJobStatus(String jobId, SdpRmJobsRecord job) {
        String resourceGroupName = job.getName().split("del-rg-")[1];
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()), resourceGroupName);
        }
        boolean existResourceGroup = azureResourceHelper.existResourceGroup(resourceGroupName);
        if (existResourceGroup) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()), resourceGroupName);
        }
        jobDao.updateJobStatus(JobStatus.Completed.name(), jobId);
        return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()), resourceGroupName);
    }

    private GetJobStatusResponse getAppendVirtualMachinesJobStatus(String jobId, SdpRmJobsRecord job) {
        return getCreateVirtualMachinesJobStatus(jobId, job);
    }

    private GetJobStatusResponse getCreateVirtualMachinesJobStatus(String jobId, SdpRmJobsRecord job) {
        String deploymentName = job.getName();
        JsonObject rawRequest = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("RawRequest").getAsJsonObject();
        String sysCreateBatch = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("SysCreateBatch").getAsString();
        CreateVmsRequest request = JsonUtil.string2Obj(rawRequest.toString(), CreateVmsRequest.class);
        String dnsName = request.getVirtualMachineGroups().get(0).getVirtualMachineSpec().getBaseProfile().getHostNameSuffix();
        String clusterName = request.getClusterName();
        String resourceGroup = ConstantUtil.getResourceGroupName(clusterName);
        if (job.getStatus().equals(JobStatus.Completed.name())) {
            List<GetGroupInfoVo> groupInfo = getGroupInfo(request.getVirtualMachineGroups().stream()
                    .map(i -> ConstantUtil.buildFleetName(clusterName, i.getGroupName()))
                    .collect(Collectors.toList()), sysCreateBatch);
            if (!CollectionUtils.isEmpty(groupInfo)) {
                azureResourceHelper.registerVirtualMachinesToPrivateDnsWithRetry(groupInfo.stream().flatMap(i -> i.getVirtualMachines().stream()).collect(Collectors.toList()), dnsName);
            }
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name()), groupInfo);
        }

        Deployment deployment = ArmUtil.getArmData().deployments().getByResourceGroup(resourceGroup, deploymentName);

        if (deployment == null) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Deployment not found: " + deploymentName);
        }
        String provisioningState = deployment.provisioningState();
        logger.info("Deployment {} found, provisioningState is {}", deploymentName, provisioningState);
        JobResponse jobResponse = null;
        if (provisioningState.equals(ProvisioningState.RUNNING.toString())
                || provisioningState.equals(ProvisioningState.ACCEPTED.toString())
                || provisioningState.equals(ProvisioningState.CREATING.toString())
                || provisioningState.equals(ProvisioningState.UPDATING.toString())) {
            return GetJobStatusResponse.from(JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Started.name()));
        } else if (provisioningState.equals(ProvisioningState.FAILED.toString())) {
            jobResponse = JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Failed.name());
        } else if (provisioningState.equals(ProvisioningState.SUCCEEDED.toString())) {
            jobResponse = JobResponse.from(job.getJobid(), job.getName(), job.getType(), JobStatus.Completed.name());
        } else {
            throw new RmException(HttpStatus.BAD_REQUEST, "Unknown provisioning state: " + deployment.provisioningState());
        }
        jobDao.updateJobStatus(jobResponse.getStatus(), jobId);
        List<GetGroupInfoVo> groupInfo = getGroupInfo(request.getVirtualMachineGroups().stream()
                .map(i -> ConstantUtil.buildFleetName(clusterName, i.getGroupName()))
                .collect(Collectors.toList()), sysCreateBatch);
        if (!CollectionUtils.isEmpty(groupInfo)) {
            azureResourceHelper.registerVirtualMachinesToPrivateDnsWithRetry(groupInfo.stream().flatMap(i -> i.getVirtualMachines().stream()).collect(Collectors.toList()), dnsName);
        }
        if (provisioningState.equals(ProvisioningState.FAILED.toString())) {
            DeploymentError deploymentError = DeploymentError.from(deployment.error());
            List<DeploymentOperation> deploymentOperations = deployment.deploymentOperations().list().stream().collect(Collectors.toList());
            return GetJobStatusResponse.from(jobResponse, groupInfo, DeploymentFailed.from(deploymentError, deploymentOperations));
        }
        return GetJobStatusResponse.from(jobResponse, groupInfo);
    }

    /**
     * 查询集群VM信息
     * @param fleetNames
     * @return
     */
    private List<GetGroupInfoVo> getGroupInfo(List<String> fleetNames, String sysCreateBatch) {
        String query = azureResourceGraphHelper.getCompleteQueryByFleet(fleetNames, sysCreateBatch);
        JsonArray queryResult = azureResourceGraphHelper.executeQuery(query, null);
        List<GetVmInfoVo> vmInfos = GetVmInfoVo.from(queryResult);
        return GetGroupInfoVo.from(vmInfos);
    }

}
