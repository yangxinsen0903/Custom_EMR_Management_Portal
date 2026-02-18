package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterAppsConfig;
import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterHostGroupAppsConfigMapper {
    int deleteByPrimaryKey(String appConfigItemId);

    int insert(ConfClusterHostGroupAppsConfig record);

    int insertSelective(ConfClusterHostGroupAppsConfig record);

    ConfClusterHostGroupAppsConfig selectByPrimaryKey(String appConfigItemId);

    int updateByPrimaryKeySelective(ConfClusterHostGroupAppsConfig record);

    int updateByPrimaryKeyWithBLOBs(ConfClusterHostGroupAppsConfig record);

    int updateByPrimaryKey(ConfClusterHostGroupAppsConfig record);

    int batchInsert(@Param("list") List<ConfClusterHostGroupAppsConfig> records);

    List<ConfClusterHostGroupAppsConfig> listByClusterIdAndGroupId(@Param("clusterId") String clusterId, @Param("groupId") String groupId);
}