package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.ConfScalingTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfScalingTaskMapper {
    ConfScalingTask findByClusterIdAndGroupNameAndOperationType(@Param("clusterId") String clusterId,
                                                                @Param("groupName") String groupName,
                                                                @Param("scalingType") Integer scalingType,
                                                                @Param("operationType") Integer operationType);

    Integer countByClusterIdAndClusterNameAndStateAndScaleInNotSpot(@Param("clusterId") String clusterId,
                                                                    @Param("groupName") String groupName,
                                                                    @Param("state0") Integer state0,
                                                                    @Param("state1") Integer state1);

    Integer countByClusterIdAndClusterNameAndState(@Param("clusterId") String clusterId,
                                                   @Param("groupName") String groupName,
                                                   @Param("state0") Integer state0,
                                                   @Param("state1") Integer state1);

    ConfScalingTask selectByPrimaryKey(@Param("clusterId") String clusterId, @Param("taskId") String taskId);

    ConfScalingTask findLastTaskOrderByDesc(@Param("clusterId") String clusterId,
                                            @Param("vmRole") String vmRole,
                                            @Param("groupName") String groupName,
                                            @Param("states") List<Integer> state);

    List<ConfScalingTask> findLast3TaskOrderByDesc(@Param("clusterId") String clusterId,
                                                   @Param("vmRole") String vmRole,
                                                   @Param("groupName") String groupName,
                                                   @Param("scalingType") Integer scalingType);
}
