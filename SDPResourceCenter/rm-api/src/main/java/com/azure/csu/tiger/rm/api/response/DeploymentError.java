package com.azure.csu.tiger.rm.api.response;

import com.azure.core.management.exception.ManagementError;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class DeploymentError {

    private String code;

    private String message;

    private String target;

    private List<DeploymentError> details;

    public static DeploymentError from(ManagementError managementError) {
        DeploymentError response = new DeploymentError();
        response.setCode(managementError.getCode());
        response.setMessage(managementError.getMessage());
        response.setTarget(managementError.getTarget());
        if (!CollectionUtils.isEmpty(managementError.getDetails())) {
            response.setDetails(Lists.newArrayList());
            for (ManagementError error : managementError.getDetails()) {
                response.getDetails().add(from(error));
            }
        }
        return response;
    }
}
