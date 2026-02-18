package com.azure.csu.tiger.rm.api.service;

import com.azure.csu.tiger.rm.api.response.GetJobStatusResponse;
import com.azure.csu.tiger.rm.api.response.ProvisionJobDetailResponse;

public interface JobService {

    GetJobStatusResponse getJobStatus(String jobId);

    ProvisionJobDetailResponse getJobProvisionDetail(String jobId);
}
