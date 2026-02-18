package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterOpTask;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfClusterOpTaskMapper {
    int deleteByPrimaryKey(String taskId);

    int insert(ConfClusterOpTask record);

    int insertSelective(ConfClusterOpTask record);

    ConfClusterOpTask selectByPrimaryKey(String taskId);

    ConfClusterOpTask selectByTaskIdAndClusterId(String taskId, String clusterId);

    int updateByPrimaryKeySelective(ConfClusterOpTask record);

    int updateByPrimaryKey(ConfClusterOpTask record);
}