package com.azure.csu.tiger.rm.api.response;

import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.StatusMessage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@ApiModel
@Data
@NoArgsConstructor
public class DeploymentFailed {

    private DeploymentError deploymentError;

    private List<Map<String, Object>> deploymentOperations;

    public static DeploymentFailed from(DeploymentError error, List<DeploymentOperation> operations) {
        DeploymentFailed response = new DeploymentFailed();
        response.setDeploymentError(error);
        List<Map<String, Object>> operationsList = Lists.newArrayList();
        for (DeploymentOperation operation : operations) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("operationId", operation.innerModel().properties().targetResource().id());
            map.put("provisioningState", operation.innerModel().properties().provisioningState());
            StatusMessage statusMessage = operation.innerModel().properties().statusMessage();
            if (statusMessage != null) {
                map.put("errorStatus", statusMessage.error().getCode());
                map.put("errorMessage", statusMessage.error().getMessage());
            }
            operationsList.add(map);
        }
        response.setDeploymentOperations(operationsList);
        return response;
    }
}
