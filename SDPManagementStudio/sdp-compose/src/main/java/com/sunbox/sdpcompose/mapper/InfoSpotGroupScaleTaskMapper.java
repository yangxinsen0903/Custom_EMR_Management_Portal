package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoSpotGroupScaleTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoSpotGroupScaleTaskMapper {
    InfoSpotGroupScaleTask selectByPrimaryKey(String taskId);

    int updateByPrimaryKey(InfoSpotGroupScaleTask record);
}
