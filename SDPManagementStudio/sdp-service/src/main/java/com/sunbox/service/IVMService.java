package com.sunbox.service;

import com.alibaba.fastjson.JSONObject;

public interface IVMService {

    /**
     * 查询jobDetail
     *
     * @param jobId
     * @param clusterName
     * @return
     */
    JSONObject getProvisionDetail(String jobId, String clusterName,String region);
}
