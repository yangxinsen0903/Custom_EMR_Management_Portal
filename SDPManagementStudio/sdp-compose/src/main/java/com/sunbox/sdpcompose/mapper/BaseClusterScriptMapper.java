package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.BaseClusterScript;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseClusterScriptMapper {
    int deleteByPrimaryKey(String confScriptId);

    int insert(BaseClusterScript record);

    int insertSelective(BaseClusterScript record);

    List<BaseClusterScript> selectBaseClusterScriptList(@Param("releaseVersion") String releaseVersion, @Param("runTiming") String runTiming);

    BaseClusterScript selectByPrimaryKey(String confScriptId);

    int updateByPrimaryKeySelective(BaseClusterScript record);

    int updateByPrimaryKey(BaseClusterScript record);
}