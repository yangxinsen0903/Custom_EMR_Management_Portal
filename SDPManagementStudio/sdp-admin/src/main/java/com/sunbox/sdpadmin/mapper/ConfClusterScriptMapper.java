package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterScript;
import com.sunbox.domain.ConfClusterScriptJob;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterScriptMapper {
    int insert(ConfClusterScript record);

    int insertSelective(ConfClusterScript record);

    List<ConfClusterScriptJob> queryScriptJob(ConfClusterScriptJob script, @Param("pageable") Pageable pageable);

    Long count(ConfClusterScriptJob script);

    List<ConfClusterScript> selectByClusterId(String clusterId);

    List<ConfClusterScript> selectByClusterIdForCp(String clusterId);
}