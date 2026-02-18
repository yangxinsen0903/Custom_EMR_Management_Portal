package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfScalingVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfScalingVmMapper {
    List<ConfScalingVm> selectByTaskId(@Param("clusterId") String clusterId, @Param("taskId") String taskId);
}