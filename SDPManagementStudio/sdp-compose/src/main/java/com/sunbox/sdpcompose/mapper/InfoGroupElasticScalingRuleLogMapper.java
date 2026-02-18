package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoGroupElasticScalingRuleLog;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoGroupElasticScalingRuleLogMapper {
    int deleteByPrimaryKey(Long esRuleLogId);

    int insert(InfoGroupElasticScalingRuleLog record);

    int insertSelective(InfoGroupElasticScalingRuleLog record);

    InfoGroupElasticScalingRuleLog selectByPrimaryKey(Long esRuleLogId);

    int updateByPrimaryKeySelective(InfoGroupElasticScalingRuleLog record);

    int updateByPrimaryKey(InfoGroupElasticScalingRuleLog record);
}