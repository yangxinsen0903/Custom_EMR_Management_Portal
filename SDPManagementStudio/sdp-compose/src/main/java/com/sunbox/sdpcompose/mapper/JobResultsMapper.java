package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoCluster;
import com.sunbox.domain.JobResults;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobResultsMapper {
    JobResults selectByJobId(@Param("jobid") String jobId);
}