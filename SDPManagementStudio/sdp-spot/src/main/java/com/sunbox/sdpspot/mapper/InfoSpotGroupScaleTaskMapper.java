package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.InfoSpotGroupScaleTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoSpotGroupScaleTaskMapper {
    int deleteByPrimaryKey(String taskId);

    int insert(InfoSpotGroupScaleTask record);

    int insertSelective(InfoSpotGroupScaleTask record);

    InfoSpotGroupScaleTask selectByPrimaryKey(String taskId);

    int updateByPrimaryKeySelective(InfoSpotGroupScaleTask record);

    int updateByPrimaryKey(InfoSpotGroupScaleTask record);

    int countByClusterIdAndNotState(@Param("clusterId") String clusterId,
                                    @Param("groupId") String groupId,
                                    @Param("vmRole") String vmRole,
                                    @Param("state1") int state1,
                                    @Param("state2") int state2);

    InfoSpotGroupScaleTask findLatestByClusterIdAndNotState(@Param("clusterId") String clusterId,
                                                            @Param("groupId") String groupId,
                                                            @Param("vmRole") String vmRole,
                                                            @Param("state1") int state1,
                                                            @Param("state2") int state2);

    int updateState(InfoSpotGroupScaleTask record);
}
