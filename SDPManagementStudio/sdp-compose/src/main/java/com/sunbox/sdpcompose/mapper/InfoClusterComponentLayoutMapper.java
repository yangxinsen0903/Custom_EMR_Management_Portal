package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterComponentLayout;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterComponentLayoutMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByClusterId(String clusterId);

    int deleteByClusterIdAndHostGroup(@Param("clusterId") String clusterId, @Param("hostGroup") String hostGroup);

    int insert(InfoClusterComponentLayout record);

    int insertSelective(InfoClusterComponentLayout record);

    InfoClusterComponentLayout selectByPrimaryKey(Long id);

    List<InfoClusterComponentLayout> selectByClusterId(String clusterId);

    int updateByPrimaryKeySelective(InfoClusterComponentLayout record);

    int updateByPrimaryKey(InfoClusterComponentLayout record);

    List<InfoClusterComponentLayout> getComponentsByClusterIdAndHostGroup(String clusterId, String hostGroup);
}