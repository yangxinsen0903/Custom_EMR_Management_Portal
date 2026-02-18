package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterVmMapper {
    int deleteByPrimaryKey(String vmConfId);

    int insert(ConfClusterVm record);

    int insertSelective(ConfClusterVm record);

    ConfClusterVm selectByPrimaryKey(String vmConfId);

    ConfClusterVm selectByClusterIdAndVmConfId(@Param("clusterId") String clusterId, @Param("vmConfId") String vmConfId);

    int updateByPrimaryKeySelective(ConfClusterVm record);

    int updateByPrimaryKey(ConfClusterVm record);

    List<ConfClusterVm> getVmConfs(String clusterId);

    List<ConfClusterVm> getVmConfsByRole(@Param("vmRole") String vmRole,
                                         @Param("clusterId") String clusterId);

    List<ConfClusterVm> getVmConfsByGroupName(@Param("groupName") String groupName,
                                              @Param("clusterId") String clusterId);

    int updateGroupId(ConfClusterVm record);
}