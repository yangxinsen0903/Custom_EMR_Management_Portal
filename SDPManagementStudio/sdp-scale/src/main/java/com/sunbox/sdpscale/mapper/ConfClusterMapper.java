package com.sunbox.sdpscale.mapper;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterHostGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterMapper {
    ConfCluster selectByPrimaryKey(String clusterId);
}