package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfClusterHostGroupAppsConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ConfClusterHostGroupAppsConfigNeoMapper {
    int deleteByPrimaryKey(String appConfigItemId);

    int insert(ConfClusterHostGroupAppsConfig record);

    int insertSelective(ConfClusterHostGroupAppsConfig record);

    ConfClusterHostGroupAppsConfig selectByPrimaryKey(String appConfigItemId);

    List<ConfClusterHostGroupAppsConfig> selectByGroupId(String groupId);

    int updateByPrimaryKeySelective(ConfClusterHostGroupAppsConfig record);

    int updateByPrimaryKeyWithBLOBs(ConfClusterHostGroupAppsConfig record);

    int updateByPrimaryKey(ConfClusterHostGroupAppsConfig record);

    int deleteByGroupId(@Param("groupId") String groupId);
}