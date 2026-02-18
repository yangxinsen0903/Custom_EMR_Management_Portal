package com.azure.csu.tiger.rm.api.response;

import com.azure.resourcemanager.resources.models.ResourceGroup;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@ApiModel
@Data
@NoArgsConstructor
public class ResourceGroupResponse {

    private String location;

    private String name;

    private Map<String, String> tags;

    public static ResourceGroupResponse from(ResourceGroup resourceGroup) {
        ResourceGroupResponse response = new ResourceGroupResponse();
        response.setLocation(resourceGroup.regionName());
        response.setName(resourceGroup.name());
        response.setTags(resourceGroup.tags());
        return response;
    }
}
