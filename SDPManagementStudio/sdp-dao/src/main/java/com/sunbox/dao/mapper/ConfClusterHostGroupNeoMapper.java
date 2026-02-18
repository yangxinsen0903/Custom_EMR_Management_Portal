package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfClusterHostGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ConfClusterHostGroupNeoMapper {
    int deleteByPrimaryKey(String groupId);

    int insert(ConfClusterHostGroup record);

    int insertSelective(ConfClusterHostGroup record);

    ConfClusterHostGroup selectByPrimaryKey(String groupId);

    List<ConfClusterHostGroup> selectByClusterId(String clusterId);

    ConfClusterHostGroup selectOneByGroupNameAndClusterId(@Param("clusterId") String clusterId, @Param("groupName") String groupName);

    int updateByPrimaryKeySelective(ConfClusterHostGroup record);

    int updateByPrimaryKey(ConfClusterHostGroup record);

    int updateByClusterId(@Param("clusterId") String clusterId, @Param("state") Integer state);

    List<ConfClusterHostGroup> selectByVmRoleAndClusterId(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole);

    /**
     * 获取创建完成的集群的所有HostGroup
     * @return
     */
    List<ConfClusterHostGroup> listRunningClusterHostGroup();

    /**
     * 获取正在增量创建中的集群的所有HostGroup
     * @return
     */
    List<ConfClusterHostGroup> listRunningSplitClusterHostGroup();

    int countRunningClusterHostGroup();

    int countRunningSplitClusterHostGroup();

    int updateExpectCount(@Param("clusterId") String clusterId,
                          @Param("groupId") String groupId,
                          @Param("expectCount") Integer expectCount);

    int updateCreationSubStateByClusterId(@Param("clusterId") String clusterId,
                                          @Param("creationSubState") String creationSubState);

    int updateCreationSubStateByClusterIdAndGroupName(@Param("clusterId") String clusterId,
                                                      @Param("groupName") String groupName,
                                                      @Param("creationSubState") String creationSubState);

    /**
     *  更新hostgroup ins_count
     *  逻辑：ins_count -1
     * @param clusterId
     * @param groupName
     * @return
     */
    int updateHostGroupInsCountForEvication(@Param("clusterId") String clusterId,
                                            @Param("groupName") String groupName);
}