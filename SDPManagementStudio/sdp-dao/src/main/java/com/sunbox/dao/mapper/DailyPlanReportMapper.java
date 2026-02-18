package com.sunbox.dao.mapper;

import com.sunbox.domain.DailyPlanReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface DailyPlanReportMapper {
    void insert(DailyPlanReport item);

    List<DailyPlanReport> selectByReportId(@Param("region") String region, @Param("reportId") String reportId);

    int countByReportId(String reportId, String region);

    List<DailyPlanReport> selectByTime(@Param("region") String region, @Param("beginTime") Date beginDate, @Param("endTime") Date endDate);
}