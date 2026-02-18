package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.InfoSpotGroupScaleTaskItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoSpotGroupScaleTaskItemMapper {
    int countByVmNameAndState(@Param("clusterId") String clusterId,
                              @Param("groupId") String groupId,
                              @Param("vmName") String vmName,
                              @Param("state") Integer state);

    InfoSpotGroupScaleTaskItem selectTop1ByClusterIdAndGroupIdAndVmNameAndTaskId(@Param("clusterId") String clusterId,
                                                                                 @Param("groupId") String groupId,
                                                                                 @Param("vmName") String vmName,
                                                                                 @Param("taskId") String taskId);

    int deleteByPrimaryKey(String itemId);

    int insert(InfoSpotGroupScaleTaskItem record);

    int insertSelective(InfoSpotGroupScaleTaskItem record);

    InfoSpotGroupScaleTaskItem selectByPrimaryKey(String itemId);

    int updateByPrimaryKeySelective(InfoSpotGroupScaleTaskItem record);

    int updateByPrimaryKey(InfoSpotGroupScaleTaskItem record);

    List<InfoSpotGroupScaleTaskItem> listByClusterIdAndGroupIdAndTaskId(@Param("clusterId") String clusterId,
                                                                        @Param("groupId") String groupId,
                                                                        @Param("taskId") String taskId);
}
