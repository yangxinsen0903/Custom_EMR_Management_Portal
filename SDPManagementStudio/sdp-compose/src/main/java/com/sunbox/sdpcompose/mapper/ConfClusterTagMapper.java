package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfClusterTag;
import com.sunbox.domain.ConfClusterTagKey;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterTagMapper {
    int deleteByPrimaryKey(ConfClusterTagKey key);

    int deleteByClusterId(String clusterId);

    int insert(ConfClusterTag record);

    int insertSelective(ConfClusterTag record);

    ConfClusterTag selectByPrimaryKey(ConfClusterTagKey key);

    int updateByPrimaryKeySelective(ConfClusterTag record);

    int updateByPrimaryKey(ConfClusterTag record);

    List<ConfClusterTag> getTagsbyClusterId(String clusterId);
}