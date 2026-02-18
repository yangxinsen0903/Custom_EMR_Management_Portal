package com.azure.csu.tiger.rm.api.service.impl;

import com.azure.csu.tiger.rm.api.enums.JobStatus;
import com.azure.csu.tiger.rm.api.enums.JobType;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.helper.JobHelper;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ResourceGroupResponse;
import com.azure.csu.tiger.rm.api.service.ResourceGroupService;
import com.azure.csu.tiger.rm.api.utils.ArmUtil;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.DnsUtil;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceGroupServiceImpl implements ResourceGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceGroupServiceImpl.class);

    @Autowired
    private AzureResourceHelper azureResourceHelper;
    @Autowired
    private JobHelper jobHelper;
    @Autowired
    private DnsUtil dnsUtil;

    @Override
    public void createResourceGroup(String region, String rgName) {
        if (azureResourceHelper.existResourceGroup(rgName)) {
            logger.info("Resource group {} already exists", rgName);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("Resource group %s already exists. Please use another new cluster name.", rgName));
        }
        azureResourceHelper.createResourceGroup(region, rgName);
        logger.info("Resource group {} created", rgName);
    }

    @Override
    public boolean existResourceGroup(String rgName) {
        return azureResourceHelper.existResourceGroup(rgName);
    }

    @Override
    public ResourceGroupResponse getResourceGroup(String rgName) {
        if (!existResourceGroup(rgName)) {
            logger.warn("Resource group {} not found", rgName);
            throw new RuntimeException("Resource group not found");
        }
        ResourceGroup resourceGroup = azureResourceHelper.getResourceGroup(rgName);
        return ResourceGroupResponse.from(resourceGroup);
    }

    @Transactional
    @Override
    public JobResponse deleteResourceGroup(String rgName) {
        if (!existResourceGroup(rgName)) {
            logger.warn("Resource group {} not found", rgName);
            throw new RuntimeException("Resource group not found");
        }
        JobResponse response = JobResponse.from(ConstantUtil.buildDeleteResourceGroupJobId(rgName),
                ConstantUtil.buildDeleteResourceGroupJobName(rgName),
                JobType.DeleteResourceGroup.name(),
                JobStatus.Started.name());
        JsonObject o = new JsonObject();
        o.addProperty("resourceGroup", rgName);
        o.addProperty("subscriptionId", ArmUtil.getSubData());
        jobHelper.saveJob(response.getId(), response.getName(), response.getType(), response.getStatus(), o.toString());

        String cluster = ConstantUtil.getClusterName(rgName);
        String dns = dnsUtil.getPrivateDnsZone(cluster);
        List<String> hostNames = ArmUtil.getArmData().virtualMachines().listByResourceGroup(rgName).stream()
                .map(i -> i.computerName().toLowerCase()).collect(Collectors.toList());
        logger.info("Begin deleting VM DNS records from resource group {} in DNS {}", rgName, dns);
        azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(hostNames, dns);
        logger.info("Begin deleting resource group {}", rgName);
        azureResourceHelper.deleteResourceGroup(rgName);
        logger.info("Delete resource group {} successfully", rgName);
        return response;
    }

    @Override
    public void createOrUpdateResourceGroupTags(String rgName, Map<String, String> newTags) {
        if (!existResourceGroup(rgName)) {
            logger.warn("Resource group {} not found", rgName);
            throw new RuntimeException("Resource group not found");
        }
        ResourceGroup resourceGroup = azureResourceHelper.getResourceGroup(rgName);
        Map<String, String> oldTags = resourceGroup.tags();
        Map<String, String> updateTags = Maps.newHashMap(oldTags);
        updateTags.putAll(newTags);
        resourceGroup.update().withTags(updateTags).apply();
    }

    @Override
    public void resetResourceGroupTags(String rgName, Map<String, String> newTags) {
        if (!existResourceGroup(rgName)) {
            logger.warn("Resource group {} not found", rgName);
            throw new RuntimeException("Resource group not found");
        }
        ResourceGroup resourceGroup = azureResourceHelper.getResourceGroup(rgName);
        resourceGroup.update().withTags(newTags).apply();
    }

    @Override
    public void deleteResourceGroupTags(String rgName, List<String> deleteTags) {
        if (!existResourceGroup(rgName)) {
            logger.warn("Resource group {} not found", rgName);
            throw new RuntimeException("Resource group not found");
        }
        ResourceGroup resourceGroup = azureResourceHelper.getResourceGroup(rgName);
        Map<String, String> updateTags = Maps.newHashMap(resourceGroup.tags());
        deleteTags.forEach(updateTags::remove);
        resourceGroup.update().withTags(updateTags).apply();
    }

}
