package com.sunbox.sdpscale.mapper;

import com.sunbox.domain.InfoGroupElasticScalingRuleLog;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InfoGroupElasticScalingRuleLogMapper {
    int insertSelective(InfoGroupElasticScalingRuleLog record);

    List<InfoGroupElasticScalingRuleLog> selectComputePassInWindowSize(String esRuleId, String loadMetric, String aggregateType, Integer repeatCount, Date createdTime);

    void update(InfoGroupElasticScalingRuleLog updateComputeResult);
}