package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfGroupElasticScalingRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfGroupElasticScalingRuleMapper {
    int deleteByPrimaryKey(String esRuleId);

    int deleteByGroupEsId(String groupEsId);

    int insert(ConfGroupElasticScalingRule record);

    int insertSelective(ConfGroupElasticScalingRule record);

    ConfGroupElasticScalingRule selectByPrimaryKey(String esRuleId);

    List<ConfGroupElasticScalingRule> selectAllByGroupEsId(String groupEsId);

    List<ConfGroupElasticScalingRule> selectAllByGroupEsIdAndValid(String groupEsId);

    int updateByPrimaryKeySelective(ConfGroupElasticScalingRule record);

    int updateByPrimaryKey(ConfGroupElasticScalingRule record);

    List<ConfGroupElasticScalingRule> selectByClusterIdAndGroupName(String clusterId, String groupName);

    List<ConfGroupElasticScalingRule> selectByClusterIdAndGroupNameAndValid(String clusterId, String groupName);

    int updateValid(ConfGroupElasticScalingRule scalingRule);

    List<ConfGroupElasticScalingRule> selectValidByClusterId(@Param("clusterId") String clusterId);
}