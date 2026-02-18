package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfScalingTaskVm;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfScalingTaskVmMapper {
    int deleteByPrimaryKey(String vmDetailId);

    int insert(ConfScalingTaskVm record);

    int insertSelective(ConfScalingTaskVm record);

    ConfScalingTaskVm selectByPrimaryKey(String vmDetailId);

    int updateByPrimaryKeySelective(ConfScalingTaskVm record);

    int updateByPrimaryKey(ConfScalingTaskVm record);
}