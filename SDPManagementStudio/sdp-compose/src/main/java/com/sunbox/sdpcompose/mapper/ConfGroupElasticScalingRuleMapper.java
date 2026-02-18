package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfGroupElasticScalingRule;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfGroupElasticScalingRuleMapper {
    int deleteByPrimaryKey(String esRuleId);

    int insert(ConfGroupElasticScalingRule record);

    int insertSelective(ConfGroupElasticScalingRule record);

    ConfGroupElasticScalingRule selectByPrimaryKey(String esRuleId);

    List<ConfGroupElasticScalingRule> selectByClusterId(String clusterId);

    int updateByPrimaryKeySelective(ConfGroupElasticScalingRule record);

    int updateByPrimaryKey(ConfGroupElasticScalingRule record);

    int updateValid(ConfGroupElasticScalingRule scalingRule);
}