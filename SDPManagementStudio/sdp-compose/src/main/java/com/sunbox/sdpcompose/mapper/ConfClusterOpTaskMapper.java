package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterOpTask;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfClusterOpTaskMapper {
    int deleteByPrimaryKey(String taskId);

    int insert(ConfClusterOpTask record);

    int insertSelective(ConfClusterOpTask record);

    ConfClusterOpTask selectByPrimaryKey(String taskId);

    int updateByPrimaryKeySelective(ConfClusterOpTask record);

    int updateByPrimaryKey(ConfClusterOpTask record);
}