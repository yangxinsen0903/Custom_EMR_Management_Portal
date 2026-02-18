package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterVmReject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterVmRejectMapper {
    int deleteByPrimaryKey(String rejectId);

    int insert(InfoClusterVmReject record);

    int insertSelective(InfoClusterVmReject record);

    InfoClusterVmReject selectByPrimaryKey(String rejectId);

    int updateByPrimaryKeySelective(InfoClusterVmReject record);

    int updateByPrimaryKeyWithBLOBs(InfoClusterVmReject record);

    int updateByPrimaryKey(InfoClusterVmReject record);

    int bastchInsert(@Param("vms") List<InfoClusterVmReject> vms);

    List<InfoClusterVmReject> getVmRejectsByPlanId(@Param("planId") String planId);

    /**
     *  更新销毁时间
     * @param planId
     * @return
     */
    int updateVmRejectsByPlanId(@Param("planId") String planId);
}