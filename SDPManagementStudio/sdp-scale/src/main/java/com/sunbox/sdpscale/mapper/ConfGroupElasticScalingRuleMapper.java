package com.sunbox.sdpscale.mapper;

import com.sunbox.domain.ConfGroupElasticScalingRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfGroupElasticScalingRuleMapper {
    List<ConfGroupElasticScalingRule> selectValidRuleList();

    List<ConfGroupElasticScalingRule> selectDistinctValidRuleList(@Param("serverIndex") int serverIndex, @Param("serverCount") int serverCount);

    void updateByGroupEsId(ConfGroupElasticScalingRule scalingRule);
}