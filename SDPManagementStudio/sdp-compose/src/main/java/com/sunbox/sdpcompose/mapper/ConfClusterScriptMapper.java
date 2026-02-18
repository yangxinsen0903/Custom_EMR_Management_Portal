package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterScript;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterScriptMapper {
    int insert(ConfClusterScript record);

    int insertSelective(ConfClusterScript record);

    List<ConfClusterScript> selectConfClusterScript(@Param("clusterId") String clusterId, @Param("runTiming") String runTiming);
}