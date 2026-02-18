package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterAppsConfig;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterAppsConfigMapper {
    int deleteByPrimaryKey(String appConfigItemId);

    int insert(ConfClusterAppsConfig record);

    int insertSelective(ConfClusterAppsConfig record);

    ConfClusterAppsConfig selectByPrimaryKey(String appConfigItemId);

    List<ConfClusterAppsConfig> selectByClusterId(String clusterId);

    int updateByPrimaryKeySelective(ConfClusterAppsConfig record);

    int updateByPrimaryKey(ConfClusterAppsConfig record);

    int batchInsert(@Param("records")List<ConfClusterAppsConfig> records);
}