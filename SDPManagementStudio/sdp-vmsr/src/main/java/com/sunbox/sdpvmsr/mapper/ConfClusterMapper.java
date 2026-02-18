package com.sunbox.sdpvmsr.mapper;

import com.sunbox.domain.ConfCluster;
import org.springframework.stereotype.Repository;


@Repository
public interface ConfClusterMapper {

    ConfCluster selectByPrimaryKey(String clusterId);

}