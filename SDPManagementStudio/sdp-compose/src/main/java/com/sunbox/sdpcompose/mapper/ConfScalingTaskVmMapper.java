package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfScalingTaskVm;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfScalingTaskVmMapper {
    int deleteByPrimaryKey(String vmDetailId);

    int insert(ConfScalingTaskVm record);

    int insertSelective(ConfScalingTaskVm record);

    ConfScalingTaskVm selectByPrimaryKey(String vmDetailId);

    int updateCount(ConfScalingTaskVm record);

    int updateByPrimaryKeySelective(ConfScalingTaskVm record);

    int updateByPrimaryKey(ConfScalingTaskVm record);

    List<ConfScalingTaskVm> getScalingVmConfig(String taskId);
}