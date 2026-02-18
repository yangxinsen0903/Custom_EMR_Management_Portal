package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfScalingVm;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface ConfScalingVmMapper {
    int insertSelective(ConfScalingVm record);

    List<ConfScalingVm> selectByTaskId(@Param("taskId") String taskId);

    void updateStateByTaskId(@Param("taskId")String taskId, @Param("state")Integer state);
}