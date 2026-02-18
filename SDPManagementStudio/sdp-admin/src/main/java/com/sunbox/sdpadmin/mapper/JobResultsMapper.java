package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.JobResults;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobResultsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(JobResults record);

    int insertSelective(JobResults record);

    JobResults selectByPrimaryKey(Integer id);

    JobResults selectByJobId(@Param("jobId") String jobId);

    int updateByPrimaryKeySelective(JobResults record);

    int updateByPrimaryKeyWithBLOBs(JobResults record);

    int updateByPrimaryKey(JobResults record);
}