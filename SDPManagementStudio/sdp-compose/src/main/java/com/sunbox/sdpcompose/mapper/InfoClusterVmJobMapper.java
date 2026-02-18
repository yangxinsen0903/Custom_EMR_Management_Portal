package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterVmJob;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface InfoClusterVmJobMapper {
    int deleteByPrimaryKey(String transactionId);

    int insert(InfoClusterVmJob record);

    int insertSelective(InfoClusterVmJob record);

    InfoClusterVmJob selectByPrimaryKey(String transactionId);

    int updateByPrimaryKeySelective(InfoClusterVmJob record);

    int updateByPrimaryKey(InfoClusterVmJob record);

    InfoClusterVmJob getVmJobByClusterIdAndOperation(Map<String,Object> paramap);

    InfoClusterVmJob getVmJobByJobId(String jobId);

    InfoClusterVmJob getVmJobByActivityLogID(String activityLogId);

    InfoClusterVmJob getVmJobByPlanId(String planId);

}