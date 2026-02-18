package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterFullLog;
import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InfoClusterFullLogMapper {
    int deleteByPrimaryKey(Long logId);

    int insert(InfoClusterFullLogWithBLOBs record);

    int insertSelective(InfoClusterFullLogWithBLOBs record);

    InfoClusterFullLogWithBLOBs selectByPrimaryKey(Long logId);

    int updateByPrimaryKeySelective(InfoClusterFullLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(InfoClusterFullLogWithBLOBs record);

    int updateByPrimaryKey(InfoClusterFullLog record);

    int deleteByResponseTime(Date responseTime);
}