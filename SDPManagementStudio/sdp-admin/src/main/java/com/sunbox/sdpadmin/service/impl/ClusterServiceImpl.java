package com.sunbox.sdpadmin.service.impl;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupAppsConfigMapper;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpadmin.service.IClusterService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : [niyang]
 * @className : ClusterServiceImpl
 * @description : [集群操作接口]
 * @createTime : [2022/11/29 2:06 PM]
 */
@Service
public class ClusterServiceImpl  implements IClusterService, BaseCommonInterFace {

    @Autowired
    private ConfClusterHostGroupMapper hostGroupMapper;

    @Autowired
    private ConfClusterHostGroupAppsConfigMapper hostGroupAppsConfigMapper;

    /**
     * 创建hostgroup
     *
     * @param hostGroups
     * @return
     */
    @Override
    public ResultMsg createHostGroups(List<ConfClusterHostGroup> hostGroups) {
        return null;
    }

    /**
     * 保存hostgroupAppConfigs
     *
     * @param groupAppsConfigs
     * @return
     */
    @Override
    public ResultMsg CreateHostGroupAppConfig(List<ConfClusterHostGroupAppsConfig> groupAppsConfigs) {
        return null;
    }
}
