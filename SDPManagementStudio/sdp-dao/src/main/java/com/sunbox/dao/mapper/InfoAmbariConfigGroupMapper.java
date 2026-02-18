package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoAmbariConfigGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoAmbariConfigGroupMapper {
    int deleteByPrimaryKey(String confId);

    int insert(InfoAmbariConfigGroup record);

    int insertSelective(InfoAmbariConfigGroup record);

    InfoAmbariConfigGroup selectByPrimaryKey(String confId);

    InfoAmbariConfigGroup selectByClusterIdAndAmbariGroupNameAndVmSkuId(@Param("clusterId") String clusterId,
                                                              @Param("ambariGroupName") String ambariGroupName,
                                                              @Param("vmSkuId") String vmSkuId);
    InfoAmbariConfigGroup selectByClusterIdAndAmbariGroupNameAndVmSkuIdAndServiceName(@Param("clusterId") String clusterId,
                                                              @Param("ambariGroupName") String ambariGroupName,
                                                              @Param("vmSkuId") String vmSkuId,
                                                              @Param("ambariServiceName") String serviceName);
    InfoAmbariConfigGroup selectByClusterIdAndAmbariId(@Param("clusterId") String clusterId, @Param("ambariId") Long ambariId);

    int updateByPrimaryKeySelective(InfoAmbariConfigGroup record);

    int updateByPrimaryKey(InfoAmbariConfigGroup record);

    int updateStateByGroupId(@Param("groupId") String groupId,@Param("state") Integer state);

    List<InfoAmbariConfigGroup> selectByGroupIdAndStates(@Param("groupId") String groupId, @Param("states") List<Integer> states);

    int updateByGroupIdSelective(InfoAmbariConfigGroup record);

    int deleteByClusterIdAndAmbariId(@Param("ambariId") Long ambariId, @Param("clusterId") String clusterId);
}