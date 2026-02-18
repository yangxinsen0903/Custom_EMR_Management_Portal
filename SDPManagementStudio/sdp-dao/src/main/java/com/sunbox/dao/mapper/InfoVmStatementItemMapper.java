package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoVmStatementItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InfoVmStatementItemMapper {
    List<InfoVmStatementItem> selectByStatementId(@Param("statementId") String statementId);

    InfoVmStatementItem selectByPrimaryKey(@Param("id") String id);

    void deleteByPrimaryKey(@Param("id") String id);

    void insert(InfoVmStatementItem item);

    Integer countByStatementIdAndVmSource(@Param("statementId") String statementId,
                                          @Param("vmSource") String vmSource,@Param("region") String region);
}