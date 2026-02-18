package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoVmStatement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface InfoVmStatementMapper {
    InfoVmStatement selectByPrimaryKey(@Param("statementId") String statementId);

    InfoVmStatement selectByPrimaryKeyAndRegion(@Param("statementId") String statementId, @Param("region") String region);

    void deleteByPrimaryKey(@Param("statementId") String statementId);

    void insert(InfoVmStatement item);

    void updateStatus(@Param("statementId") String statementId,
                      @Param("status") String status,
                      @Param("modifiedTime") Date modifiedTime);

    List<InfoVmStatement> selectByCreatedTime(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);


    List<InfoVmStatement> queryByCreatedTime(@Param("region") String region,
                                             @Param("beginTime") Date beginTime,
                                             @Param("endTime") Date endTime,
                                             @Param("pageStart") Integer pageStart,
                                             @Param("pageLimit") Integer pageLimit);

    Integer countByCreatedTime(@Param("region") String region,
                               @Param("beginTime") Date beginTime,
                                             @Param("endTime") Date endTime);

    InfoVmStatement selectLatest();
}