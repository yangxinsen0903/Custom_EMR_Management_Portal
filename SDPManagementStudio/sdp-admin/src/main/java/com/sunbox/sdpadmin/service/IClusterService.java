package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import com.sunbox.domain.ResultMsg;

import java.util.List;

public interface IClusterService {

    /**
     * 创建hostgroup
     * @return
     */
    ResultMsg createHostGroups(List<ConfClusterHostGroup> hostGroups);

    /**
     *  保存hostgroupAppConfigs
      * @param groupAppsConfigs
     * @return
     */
    ResultMsg CreateHostGroupAppConfig(List<ConfClusterHostGroupAppsConfig>groupAppsConfigs);

}
