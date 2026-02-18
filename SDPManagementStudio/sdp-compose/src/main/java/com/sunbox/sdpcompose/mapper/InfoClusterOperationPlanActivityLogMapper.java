package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterOperationPlanActivityLog;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface InfoClusterOperationPlanActivityLogMapper {
    int deleteByPrimaryKey(String activityLogId);

    int insert(InfoClusterOperationPlanActivityLogWithBLOBs record);

    int insertSelective(InfoClusterOperationPlanActivityLogWithBLOBs record);

    InfoClusterOperationPlanActivityLogWithBLOBs selectByPrimaryKey(String activityLogId);

    int updateByPrimaryKeySelective(InfoClusterOperationPlanActivityLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(InfoClusterOperationPlanActivityLogWithBLOBs record);

    int updateByPrimaryKey(InfoClusterOperationPlanActivityLog record);

    List<InfoClusterOperationPlanActivityLogWithBLOBs> getAllActivity(String planid);

    InfoClusterOperationPlanActivityLogWithBLOBs getNextActivity(Map param);

    InfoClusterOperationPlanActivityLogWithBLOBs getPrevActivity(Map param);

    InfoClusterOperationPlanActivityLogWithBLOBs getFirstActivity(String planId);

    int insertBatch(@Param("logs") List<InfoClusterOperationPlanActivityLogWithBLOBs> activityLogs);

    List<HashMap<String,Object>>  getRunningStateAndTimeoutActivity(@Param("timeout") Integer timeout);

}