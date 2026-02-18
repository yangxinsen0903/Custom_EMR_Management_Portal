package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfScalingVmDataVol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ConfScalingVmDataVolNeoMapper {
    int deleteByPrimaryKey(String vmDataVolId);

    int insert(ConfScalingVmDataVol record);

    int insertSelective(ConfScalingVmDataVol record);

    ConfScalingVmDataVol selectByPrimaryKey(String vmDataVolId);

    int updateByPrimaryKeySelective(ConfScalingVmDataVol record);

    int updateByPrimaryKey(ConfScalingVmDataVol record);

    List<ConfScalingVmDataVol> getDataVolByVmConfId(@Param("vmDetailId") String vmDetailId);

    int insertBatch(@Param("vmdatas")List<ConfScalingVmDataVol> vmdatas);

}