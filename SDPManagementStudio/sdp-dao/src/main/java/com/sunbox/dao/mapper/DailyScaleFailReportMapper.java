package com.sunbox.dao.mapper;

import com.sunbox.domain.DailyScaleFailReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface DailyScaleFailReportMapper {
    void insert(DailyScaleFailReport item);

    List<DailyScaleFailReport> selectByReportId(@Param("reportId") String reportId, @Param("region") String region);

    List<Map> getScaleInFailureResultReport(@Param("region") String region, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    int countByReportId(String reportId, @Param("region") String region);

    List<DailyScaleFailReport> selectByTime(@Param("region") String region, @Param("beginTime") Date beginDate, @Param("endTime") Date endDate);
}