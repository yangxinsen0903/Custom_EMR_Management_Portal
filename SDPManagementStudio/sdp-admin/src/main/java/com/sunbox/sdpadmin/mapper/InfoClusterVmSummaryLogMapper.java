package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoClusterVmSummaryLog;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoClusterVmSummaryLogMapper {
    int deleteByPrimaryKey(String cid);

    int insert(InfoClusterVmSummaryLog record);

    int insertSelective(InfoClusterVmSummaryLog record);

    InfoClusterVmSummaryLog selectByPrimaryKey(String cid);

    int updateByPrimaryKeySelective(InfoClusterVmSummaryLog record);

    int updateByPrimaryKey(InfoClusterVmSummaryLog record);
}