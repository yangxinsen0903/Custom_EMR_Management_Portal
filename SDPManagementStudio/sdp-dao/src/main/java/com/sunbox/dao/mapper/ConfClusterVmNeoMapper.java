package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfClusterVm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ConfClusterVmNeoMapper {
    int deleteByPrimaryKey(String vmConfId);

    int insert(ConfClusterVm record);

    int insertSelective(ConfClusterVm record);

    ConfClusterVm selectByPrimaryKey(String vmConfId);

    int updateByPrimaryKeySelective(ConfClusterVm record);

    int updateByPrimaryKey(ConfClusterVm record);

    List<Map> selectByObject(Map params);

    int countByClusterId(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole);

    List<ConfClusterVm> selectByClusterIdAndVmRole(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole);

    ConfClusterVm selectByAny(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("groupName") String groupName);

    List<ConfClusterVm> selectByValidClusterId(@Param("clusterId") String clusterId);

    List<ConfClusterVm> selectByClusterId(@Param("clusterId") String clusterId);

    void updateByClusterIdAndGroupNameAndVmRole(ConfClusterVm confClusterVm);

    List<ConfClusterVm> getVmConfs(String clusterId);

    List<ConfClusterVm> getVmConfsByRole(@Param("vmRole") String vmRole,
                                         @Param("clusterId") String clusterId);

    List<ConfClusterVm> getVmConfsByGroupName(@Param("groupName") String vmRole,
                                              @Param("clusterId") String clusterId);

    int updateGroupId(ConfClusterVm record);

}