package com.sunbox.sdpadmin.service;

import com.sunbox.domain.DestroyTaskRequest;
import com.sunbox.domain.ResultMsg;

public interface IDestroyCluTaskService {

    /**
     * 根据cluster_name 模糊查询
     * @param request
     * @return
     */
    ResultMsg queryDestroyTask(DestroyTaskRequest request);

    ResultMsg retryActivity(String clusterId);

    ResultMsg cancelTask(String clusterId);
}
