package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfScalingVmDataVol;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfScalingVmDataVolMapper {
    int deleteByPrimaryKey(String vmDataVolId);

    int insert(ConfScalingVmDataVol record);

    int insertSelective(ConfScalingVmDataVol record);

    ConfScalingVmDataVol selectByPrimaryKey(String vmDataVolId);

    int updateByPrimaryKeySelective(ConfScalingVmDataVol record);

    int updateByPrimaryKey(ConfScalingVmDataVol record);
}