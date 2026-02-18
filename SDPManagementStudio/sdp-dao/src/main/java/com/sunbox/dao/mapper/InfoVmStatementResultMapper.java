package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoVmStatementResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InfoVmStatementResultMapper {
    List<InfoVmStatementResult> selectByStatementId(@Param("statementId") String statementId);

    List<InfoVmStatementResult> queryByStatementId(@Param("statementId") String statementId,
                                                   @Param("clusterId") String clusterId,
                                                   @Param("clusterName") String clusterName,
                                                   @Param("hostName") String hostName,
                                                   @Param("vmRoles") String[] vmRoles,
                                                   @Param("diffType") String diffType,
                                                   @Param("purchaseType") Integer purchaseType,
                                                   @Param("minuteBefore") Integer minuteBefore,
                                                   @Param("pageStart") Integer pageStart,
                                                   @Param("pageLimit") Integer pageLimit);

    Integer countByStatementId(@Param("statementId") String statementId,
                               @Param("clusterId") String clusterId,
                               @Param("clusterName") String clusterName,
                               @Param("hostName") String hostName,
                               @Param("vmRoles") String[] vmRoles,
                               @Param("diffType") String diffType,
                               @Param("purchaseType") Integer purchaseType,
                               @Param("minuteBefore") Integer minuteBefore);

    InfoVmStatementResult selectByPrimaryKey(@Param("id") String id);

    InfoVmStatementResult selectByStatementIdAndVmNameAndHostName(@Param("statementId") String statementId,
                                                                  @Param("vmName") String vmName,
                                                                  @Param("hostName") String hostName);

    void deleteByPrimaryKey(@Param("id") String id);

    void insert(InfoVmStatementResult item);

    void updateByPrimaryKey(InfoVmStatementResult vmStatementResult);
}