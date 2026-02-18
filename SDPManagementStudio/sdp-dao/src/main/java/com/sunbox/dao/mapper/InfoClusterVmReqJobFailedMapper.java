package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterVmDelete;
import com.sunbox.domain.InfoClusterVmReqJobFailed;
import com.sunbox.domain.InfoClusterVmReqJobFailedDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface InfoClusterVmReqJobFailedMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InfoClusterVmReqJobFailed record);

    int insertSelective(InfoClusterVmReqJobFailed record);

    InfoClusterVmReqJobFailed selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InfoClusterVmReqJobFailed record);

    int updateByPrimaryKey(InfoClusterVmReqJobFailed record);

    List<InfoClusterVmReqJobFailed> getAllByStatus(@Param("status") Integer status);

    List<InfoClusterVmReqJobFailed> getByClusterIdAndPlanIdAndJobId(@Param("clusterId")String clusterId,
                                                                    @Param("planId") String planId,
                                                                    @Param("jobId") String jobId);

    List<InfoClusterVmReqJobFailedDetail> queryByTime(@Param("region") String region,
                                                      @Param("planName") String planName,
                                                      @Param("planId") String planId,
                                                      @Param("clusterId") String clusterId,
                                                      @Param("clusterName") String clusterName,
                                                      @Param("jobId") String jobId,
                                                      @Param("status") Integer status,
                                                      @Param("beginDate") Date beginDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("pageStart") Integer pageStart,
                                                      @Param("pageLimit") Integer pageLimit);

    Long countByTime(@Param("region") String region,
                     @Param("planName") String planName,
                     @Param("planId") String planId,
                     @Param("clusterId") String clusterId,
                     @Param("clusterName") String clusterName,
                     @Param("jobId") String jobId,
                     @Param("status") Integer status,
                     @Param("beginDate") Date beginDate,
                     @Param("endDate") Date endDate);

    List<Map> vmReqJobFailedSummary(@Param("region") String region);
}