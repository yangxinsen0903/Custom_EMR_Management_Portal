package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoClusterOperationPlan;
import com.sunbox.sdpadmin.model.admin.response.SelectJoinCCAndActivityLogResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterOperationPlanMapper {
    int deleteByPrimaryKey(String planId);

    int insert(InfoClusterOperationPlan record);

    int insertSelective(InfoClusterOperationPlan record);

    InfoClusterOperationPlan selectByPrimaryKey(String planId);

    int updateByPrimaryKeySelective(InfoClusterOperationPlan record);

    int updateByPrimaryKey(InfoClusterOperationPlan record);

    List<InfoClusterOperationPlan> selectByObject(InfoClusterOperationPlan record);

    int countByObject(InfoClusterOperationPlan record);

    /**
     * 查询计划列表，兼容selectByObject方法
     * @param record 原selectByObject参数
     * @param jobNames 增加的多个JobName参数
     * @return
     */
    List<InfoClusterOperationPlan> selectJobList(InfoClusterOperationPlan record, List<String> jobNames);

    /**
     * 查询计划列表数量，兼容selectByObject方法
     * @param record 原selectByObject参数
     * @param jobNames 增加的多个JobName参数
     * @return
     */
    int countJobList(InfoClusterOperationPlan record, List<String> jobNames);

    List<SelectJoinCCAndActivityLogResult> selectJoinCCAndActivityLog(InfoClusterOperationPlan record);

    // 查询所有planName为的计划
    List<InfoClusterOperationPlan> selectAllWithOutPlanName();

    List<InfoClusterOperationPlan> selectType(@Param("clusterIdList")List<String> clusterIdList, @Param("operationType") String operationType);
}