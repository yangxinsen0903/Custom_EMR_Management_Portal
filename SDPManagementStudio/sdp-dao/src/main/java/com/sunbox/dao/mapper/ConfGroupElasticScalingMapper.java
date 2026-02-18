package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfGroupElasticScaling;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfGroupElasticScalingMapper {
    int deleteByPrimaryKey(String groupEsId);

    int insert(ConfGroupElasticScaling record);

    int insertSelective(ConfGroupElasticScaling record);

    ConfGroupElasticScaling selectByPrimaryKey(String groupEsId);

    int updateByPrimaryKeySelective(ConfGroupElasticScaling record);

    int updateByPrimaryKey(ConfGroupElasticScaling record);

    int updateValid(ConfGroupElasticScaling scaling);

    List<ConfGroupElasticScaling> listByValid();


    ConfGroupElasticScaling selectByClusterIdAndGroupName(@Param("clusterId") String clusterId, @Param("groupName") String groupName);
    ConfGroupElasticScaling selectByClusterIdAndFullCustodyAndValid(@Param("clusterId") String clusterId);

    ConfGroupElasticScaling selectByClusterIdAndGroupNameAndValid(@Param("clusterId") String clusterId,
                                                                  @Param("groupName") String groupName);

    List<ConfGroupElasticScaling> listByClusterIdAndGroupNameAndValid(@Param("clusterId") String clusterId,
                                                                      @Param("groupName") String groupName);

    List<ConfGroupElasticScaling> listByClusterIdAndGroupName(@Param("clusterId") String clusterId,
                                                              @Param("groupName") String groupName);


}