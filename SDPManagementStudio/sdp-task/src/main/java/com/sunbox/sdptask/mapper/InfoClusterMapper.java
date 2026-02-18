package com.sunbox.sdptask.mapper;

import com.sunbox.domain.InfoCluster;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterMapper {
    int deleteByPrimaryKey(String clusterId);

    int insert(InfoCluster record);

    int insertSelective(InfoCluster record);

    InfoCluster selectByPrimaryKey(String clusterId);

    int updateByPrimaryKeySelective(InfoCluster record);

    int updateByPrimaryKey(InfoCluster record);
}