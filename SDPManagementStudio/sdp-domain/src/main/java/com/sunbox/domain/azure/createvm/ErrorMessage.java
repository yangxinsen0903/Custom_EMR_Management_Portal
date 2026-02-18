/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.azure.createvm;

import java.util.List;

/**
 * @author wangda
 * @date 2024/7/17
 */
public class ErrorMessage {

    private DeploymentError deploymentError;

    private List<DeploymentOperation> deploymentOperations;

    public DeploymentError getDeploymentError() {
        return deploymentError;
    }

    public void setDeploymentError(DeploymentError deploymentError) {
        this.deploymentError = deploymentError;
    }

    public List<DeploymentOperation> getDeploymentOperations() {
        return deploymentOperations;
    }

    public void setDeploymentOperations(List<DeploymentOperation> deploymentOperations) {
        this.deploymentOperations = deploymentOperations;
    }
}
