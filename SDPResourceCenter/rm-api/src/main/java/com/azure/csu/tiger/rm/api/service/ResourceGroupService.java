package com.azure.csu.tiger.rm.api.service;

import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ResourceGroupResponse;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.List;
import java.util.Map;

public interface ResourceGroupService {

    void createResourceGroup(String region, String rgName);

    boolean existResourceGroup(String rgName);

    ResourceGroupResponse getResourceGroup(String rgName);

    JobResponse deleteResourceGroup(String rgName);

    void createOrUpdateResourceGroupTags(String rgName, Map<String, String> newTags);

    void resetResourceGroupTags(String rgName, Map<String, String> newTags);

    void deleteResourceGroupTags(String rgName, List<String> deleteTags);
}
