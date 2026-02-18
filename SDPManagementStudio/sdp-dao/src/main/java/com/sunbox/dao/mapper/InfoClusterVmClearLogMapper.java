package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmClearLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InfoClusterVmClearLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InfoClusterVmClearLog record);

    int insertBatchVMClearLog(@Param("logs") List<InfoClusterVmClearLog> logs);

    int insertSelective(InfoClusterVmClearLog record);

    InfoClusterVmClearLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InfoClusterVmClearLog record);

    int updateByPrimaryKey(InfoClusterVmClearLog record);

    List<InfoClusterVmClearLog> selectByPlanId(String planId);

    List<InfoClusterVm> selectInfoClusterVmsByPlanId(String planId);

    List<InfoClusterVmClearLog> queryNeedClearVmLog();

    List<InfoClusterVmClearLog> queryDeletingVMTask();

    List<InfoClusterVmClearLog> queryExistClearLogs(@Param("clusterId") String clusterId,@Param("vmNames") List<String> vmNames);

}