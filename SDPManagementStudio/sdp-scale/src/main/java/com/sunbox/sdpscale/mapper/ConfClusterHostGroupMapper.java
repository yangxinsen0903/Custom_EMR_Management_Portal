package com.sunbox.sdpscale.mapper;

import com.sunbox.domain.ConfClusterHostGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterHostGroupMapper {
    int deleteByPrimaryKey(String groupId);

    int insert(ConfClusterHostGroup record);

    int insertSelective(ConfClusterHostGroup record);

    ConfClusterHostGroup selectByPrimaryKey(String groupId);

    List<ConfClusterHostGroup> selectByClusterId(String clusterId);

    ConfClusterHostGroup selectOneByGroupNameAndClusterId(@Param("clusterId") String clusterId, @Param("groupName") String groupName);

    ConfClusterHostGroup selectByClusterIdAndGroupName(@Param("clusterId") String clusterId, @Param("groupName") String groupName);

    int updateByPrimaryKeySelective(ConfClusterHostGroup record);

    int updateByPrimaryKey(ConfClusterHostGroup record);

    int updateByClusterId(@Param("clusterId") String clusterId, @Param("state") Integer state);

    List<ConfClusterHostGroup> selectByVmRoleAndClusterId(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole);
}