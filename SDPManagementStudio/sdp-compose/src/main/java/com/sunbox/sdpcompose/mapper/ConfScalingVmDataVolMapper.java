package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfScalingVmDataVol;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfScalingVmDataVolMapper {
    int deleteByPrimaryKey(String vmDataVolId);

    int insert(ConfScalingVmDataVol record);

    int insertSelective(ConfScalingVmDataVol record);

    ConfScalingVmDataVol selectByPrimaryKey(String vmDataVolId);

    int updateByPrimaryKeySelective(ConfScalingVmDataVol record);

    int updateByPrimaryKey(ConfScalingVmDataVol record);

    List<ConfScalingVmDataVol> getDataVolByVmConfId(@Param("vmDetailId") String vmDetailId);

    int insertBatch(@Param("vmdatas")List<ConfScalingVmDataVol> vmdatas);

}