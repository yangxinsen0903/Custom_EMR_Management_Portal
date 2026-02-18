package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoGroupElasticScalingRuleLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoGroupElasticScalingRuleLogMapper {
    int deleteByPrimaryKey(Long esRuleLogId);

    int insert(InfoGroupElasticScalingRuleLog record);

    int insertSelective(InfoGroupElasticScalingRuleLog record);

    InfoGroupElasticScalingRuleLog selectByPrimaryKey(Long esRuleLogId);

    int updateByPrimaryKeySelective(InfoGroupElasticScalingRuleLog record);

    int updateByPrimaryKey(InfoGroupElasticScalingRuleLog record);


    InfoGroupElasticScalingRuleLog findTop1OrderByDesc(@Param("clusterId") String clusterId,
                                                       @Param("esRuleId") String esRuleId);
}