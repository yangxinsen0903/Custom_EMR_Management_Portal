package com.sunbox.sdptask.mapper;

import com.sunbox.domain.ConfClusterSplitTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfClusterSplitTaskMapper {
    int countByClusterIdAndState(@Param("clusterId") String clusterId,
                                 @Param("state1") Integer state1,
                                 @Param("state2") Integer state2);

    ConfClusterSplitTask peekQueueHeadByClusterIdAndStateAndGroupNameAndVmRole(@Param("clusterId") String clusterId,
                                                                               @Param("groupName") String groupName,
                                                                               @Param("vmRole") String vmRole,
                                                                               @Param("state1") Integer state1,
                                                                               @Param("state2") Integer state2);

    int updateByPrimaryKeySelective(ConfClusterSplitTask record);
}
