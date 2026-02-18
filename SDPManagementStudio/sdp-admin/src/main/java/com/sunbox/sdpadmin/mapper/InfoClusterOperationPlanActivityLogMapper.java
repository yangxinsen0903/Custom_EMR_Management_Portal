package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoClusterOperationPlanActivityLog;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterOperationPlanActivityLogMapper {
    int deleteByPrimaryKey(String activityLogId);

    int insert(InfoClusterOperationPlanActivityLogWithBLOBs record);

    int insertSelective(InfoClusterOperationPlanActivityLogWithBLOBs record);

    InfoClusterOperationPlanActivityLogWithBLOBs selectByPrimaryKey(String activityLogId);

    int updateByPrimaryKeySelective(InfoClusterOperationPlanActivityLogWithBLOBs record);

    int updateByPrimaryKey(InfoClusterOperationPlanActivityLogWithBLOBs record);

    List<InfoClusterOperationPlanActivityLogWithBLOBs> getAllActivity(String planId);

    List<Integer> getAllActivityState(String planId);

    List<InfoClusterOperationPlanActivityLog> getActByIdName(@Param("planId")String planId, @Param("activityName")String activityName,@Param("states") List<Integer> states );

}