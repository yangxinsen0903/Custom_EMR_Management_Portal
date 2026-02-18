package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfScalingTaskVm;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ConfScalingTaskVmNeoMapper {
    int deleteByPrimaryKey(String vmDetailId);

    int insert(ConfScalingTaskVm record);

    int insertSelective(ConfScalingTaskVm record);

    ConfScalingTaskVm selectByPrimaryKey(String vmDetailId);

    int updateCount(ConfScalingTaskVm record);

    int updateByPrimaryKeySelective(ConfScalingTaskVm record);

    int updateByPrimaryKey(ConfScalingTaskVm record);

    List<ConfScalingTaskVm> getScalingVmConfig(String taskId);
}