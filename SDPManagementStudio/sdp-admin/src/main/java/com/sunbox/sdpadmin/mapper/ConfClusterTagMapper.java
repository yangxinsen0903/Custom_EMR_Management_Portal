package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterTag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ConfClusterTagMapper {
    int deleteByPrimaryKey(@Param("clusterId") String clusterId, @Param("tagGroup") String tagGroup);

    int insert(ConfClusterTag record);

    int insertSelective(ConfClusterTag record);

    ConfClusterTag selectByPrimaryKey(@Param("clusterId") String clusterId, @Param("tagGroup") String tagGroup);

    int updateByPrimaryKeySelective(ConfClusterTag record);

    int updateByPrimaryKey(ConfClusterTag record);

    List<ConfClusterTag> selectByObject(ConfClusterTag confClusterTag);

    List<ConfClusterTag> selectDistinctValueByTagGroup(ConfClusterTag confClusterTag);
}