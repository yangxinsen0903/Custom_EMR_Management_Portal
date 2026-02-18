package com.sunbox.service;

import com.sunbox.domain.AmbariConfigAddRequest;
import com.sunbox.domain.AmbariConfigItemRequest;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.ambari.AmbariConfigItem;

import java.util.List;
import java.util.Map;

public interface IConfigInfoService {

    /**
     * 查询组件列表
     * @return
     */
    ResultMsg queryComponentList();

    /**
     * 查询配置文件列表
     * @return
     */
    ResultMsg queryProfilesList(List<String> releaseVersion);

    /**
     * 查询默认配置列表,所有
     * @return
     */
    ResultMsg queryConfigList(AmbariConfigItemRequest request);


    ResultMsg queryConfigById(String id);

    ResultMsg addConfig(AmbariConfigAddRequest request);

    ResultMsg updateConfig(AmbariConfigAddRequest request);

    ResultMsg deleteConfig(AmbariConfigItem request);
}
