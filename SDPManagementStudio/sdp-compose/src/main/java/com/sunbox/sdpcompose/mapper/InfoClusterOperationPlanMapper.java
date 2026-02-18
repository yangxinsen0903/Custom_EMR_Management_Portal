package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterOperationPlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterOperationPlanMapper {
    int deleteByPrimaryKey(String planId);

    int insert(InfoClusterOperationPlan record);

    int insertSelective(InfoClusterOperationPlan record);

    InfoClusterOperationPlan selectByPrimaryKey(String planId);

    int updateByPrimaryKeySelective(InfoClusterOperationPlan record);

    int updateByPrimaryKey(InfoClusterOperationPlan record);

    int updatePlanName(InfoClusterOperationPlan plan);

    InfoClusterOperationPlan queryExistPlan(InfoClusterOperationPlan plan);

    int updatePlanStateAndPercent(InfoClusterOperationPlan plan);

    InfoClusterOperationPlan getPlanByScaleOutTaskId(@Param("clusterId") String clusterId, @Param("scalingTaskId") String scalingTaskId);

    InfoClusterOperationPlan getPlanByScalingTaskId(@Param("clusterId") String clusterId, @Param("scalingTaskId") String scalingTaskId);
}