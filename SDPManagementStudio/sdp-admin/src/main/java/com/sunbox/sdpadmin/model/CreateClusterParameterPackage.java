package com.sunbox.sdpadmin.model;

import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;

import java.util.List;

public class CreateClusterParameterPackage {
    private AdminSaveClusterRequest adminSaveClusterRequest;
    private List<CreateGroupParameter> groupParameters;

    public AdminSaveClusterRequest getAdminSaveClusterRequest() {
        return adminSaveClusterRequest;
    }

    public void setAdminSaveClusterRequest(AdminSaveClusterRequest adminSaveClusterRequest) {
        this.adminSaveClusterRequest = adminSaveClusterRequest;
    }

    public List<CreateGroupParameter> getGroupParameters() {
        return groupParameters;
    }

    public void setGroupParameters(List<CreateGroupParameter> groupParameters) {
        this.groupParameters = groupParameters;
    }
}
