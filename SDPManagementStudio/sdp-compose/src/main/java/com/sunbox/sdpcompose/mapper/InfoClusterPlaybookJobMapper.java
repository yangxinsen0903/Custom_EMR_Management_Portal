package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterPlaybookJob;
import com.sunbox.domain.InfoClusterPlaybookJobWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterPlaybookJobMapper {
    int deleteByPrimaryKey(String transactionId);

    int insert(InfoClusterPlaybookJobWithBLOBs record);

    int insertBatch(@Param("jobs")List<InfoClusterPlaybookJobWithBLOBs> jobs);

    int insertSelective(InfoClusterPlaybookJobWithBLOBs record);

    InfoClusterPlaybookJobWithBLOBs selectByPrimaryKey(String transactionId);

    List<InfoClusterPlaybookJobWithBLOBs> selectByActivityLogId(String activityLogId);

    int updateByPrimaryKeySelective(InfoClusterPlaybookJobWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(InfoClusterPlaybookJobWithBLOBs record);

    int updateByPrimaryKey(InfoClusterPlaybookJob record);

    InfoClusterPlaybookJob getPlaybookJobByActivityLogId(String activityLogId);
}