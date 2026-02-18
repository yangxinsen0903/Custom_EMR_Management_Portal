package com.azure.csu.tiger.rm.api.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class ProvisionJobDetailResponse {

    private String jobId;

    private String clusterName;

    private List<GetGroupInfoVo> provisionedVmGroups;

    private List<String> failedVMs;

    private String provisionStatus;

    private String message;

    public static ProvisionJobDetailResponse success(String jobId, String clusterName, List<GetGroupInfoVo> provisionedVmGroups) {
        ProvisionJobDetailResponse response = new ProvisionJobDetailResponse();
        response.setJobId(jobId);
        response.setClusterName(clusterName);
        response.setProvisionedVmGroups(provisionedVmGroups);
        response.setProvisionStatus("Succeed");
        response.setFailedVMs(Lists.newArrayList());
        response.setMessage(String.format("Provision Job %s succeed", jobId));
        return response;
    }
}
