package com.sunbox.sdpvmsr.mapper;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterVmMapper {

    int updateVMStateByClusterIdAndVMName(@Param("clusterId") String clusterId,
                                          @Param("vmName") String vmName,
                                          @Param("state") int state);

}