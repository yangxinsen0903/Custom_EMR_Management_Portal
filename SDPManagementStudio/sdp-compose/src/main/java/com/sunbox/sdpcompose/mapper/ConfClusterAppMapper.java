package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterApp;
import com.sunbox.domain.ConfClusterAppKey;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterAppMapper {
    int deleteByPrimaryKey(ConfClusterAppKey key);

    int insert(ConfClusterApp record);

    int insertSelective(ConfClusterApp record);

    ConfClusterApp selectByPrimaryKey(ConfClusterAppKey key);

    int updateByPrimaryKeySelective(ConfClusterApp record);

    int updateByPrimaryKey(ConfClusterApp record);

    List<ConfClusterApp> getClusterAppsByClusterId(String clusterId);
}