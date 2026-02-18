package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterAppsConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterAppsConfigMapper {
    int deleteByPrimaryKey(String appConfigItemId);

    int insert(ConfClusterAppsConfig record);

    int insertSelective(ConfClusterAppsConfig record);

    ConfClusterAppsConfig selectByPrimaryKey(String appConfigItemId);

    int updateByPrimaryKeySelective(ConfClusterAppsConfig record);

    int updateByPrimaryKey(ConfClusterAppsConfig record);

    List<ConfClusterAppsConfig> getAppConfigsByConfigId(String clusterId);
}