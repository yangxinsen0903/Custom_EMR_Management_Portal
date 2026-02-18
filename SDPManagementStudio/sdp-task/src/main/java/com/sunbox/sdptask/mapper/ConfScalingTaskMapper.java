package com.sunbox.sdptask.mapper;

import com.sunbox.domain.ConfScalingTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfScalingTaskMapper {

    ConfScalingTask selectByPrimaryKey(String taskId);


    int getTaskCountByParam(@Param("clusterId") String clusterId, @Param("groupName") String groupName, @Param("scalingType") Integer scalingType);


    ConfScalingTask peekQueueHeadTask(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("groupName") String groupName, @Param("states") List<Integer> states);


    ConfScalingTask findLastByScalingTypeAndState(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("groupName") String groupName, @Param("scalingType") Integer scalingType, @Param("state") Integer state);


    ConfScalingTask findLastFinishedTask(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("groupName") String groupName, @Param("state") Integer state);


    ConfScalingTask findLastTaskByStates(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("groupName") String groupName, @Param("state1") Integer state1, @Param("state2") Integer state2);


    ConfScalingTask queryRunningTask(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("createTime") java.sql.Timestamp createTime, @Param("groupName") String groupName, @Param("states") List<Integer> states);


    ConfScalingTask queryRunningTaskByScalingType(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("createTime") java.sql.Timestamp createTime, @Param("scalingType") Integer scalingType, @Param("states") List<Integer> states);

    int countRunningTaskByScalingTypes(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("createTime") java.sql.Timestamp createTime, @Param("scalingTypes") List<Integer> scalingTypes, @Param("states") List<Integer> states);


    int countByClusterIdAndStates(@Param("clusterId") String clusterId, @Param("states") List<Integer> states);


    List<ConfScalingTask> queryTasksByStateAndVmRoleAndClusterId(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("state") Integer state, @Param("scalingType") Integer scalingType);


    Integer getAllScaleOutVMCountByClusterAndVmRole(@Param("clusterId") String clusterId, @Param("vmRole") String vmRole, @Param("taskId") String taskId, @Param("createTime") java.sql.Timestamp createTime);


    int deleteByPrimaryKey(String taskId);

    int insert(ConfScalingTask record);


    int insertSelective(ConfScalingTask record);


    int updateByPrimaryKeySelective(ConfScalingTask record);


    int updateByPrimaryKey(ConfScalingTask record);


    int countByScalingTypeAndState(@Param("clusterId") String clusterId, @Param("groupName") String groupName, @Param("state1") Integer state1, @Param("state2") Integer state2, @Param("scalingType1") Integer scalingType1, @Param("scalingType2") Integer scalingType2);


    int countByScaleOutTaskIdAndScalingTypeAndState(@Param("clusterId") String clusterId, @Param("groupName") String groupName, @Param("state1") Integer state1, @Param("state2") Integer state2, @Param("scalingType") Integer scalingType, @Param("scaleOutTaskId") String scaleOutTaskId);
}
