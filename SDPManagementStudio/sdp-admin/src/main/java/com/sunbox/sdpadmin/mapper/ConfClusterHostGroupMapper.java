package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterHostGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ConfClusterHostGroupMapper {
    int deleteByPrimaryKey(String groupId);

    int insert(ConfClusterHostGroup record);

    int insertSelective(ConfClusterHostGroup record);

    ConfClusterHostGroup selectByPrimaryKey(String groupId);

    ConfClusterHostGroup selectByClusterIdAndGroupName(String clusterId, String groupName);

    List<ConfClusterHostGroup> selectAllByObject(Map<String, Object> params);

    List<ConfClusterHostGroup> selectByValidClusterId(@Param("clusterId") String clusterId);

    int selectInsCountByVmRole(String clusterId, String vmRole);

    int updateByPrimaryKeySelective(ConfClusterHostGroup record);

    int updateByPrimaryKey(ConfClusterHostGroup record);

    int updateByClusterIdAndGroupNameAndVmRole(ConfClusterHostGroup hostGroup);

    List<ConfClusterHostGroup> selectByClusterId(@Param("clusterId") String clusterId);

    int updateByClusterId(@Param("clusterId") String clusterId, @Param("state") Integer stateReleasing);

    int updateHostGroupSpotStateByClusterIdAndGroupName(@Param("clusterId") String clusterId,
                                                        @Param("groupName") String groupName,
                                                        @Param("spotState") Integer spotState);
}