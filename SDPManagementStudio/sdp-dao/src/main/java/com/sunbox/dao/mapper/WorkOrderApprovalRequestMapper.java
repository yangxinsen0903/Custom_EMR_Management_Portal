package com.sunbox.dao.mapper;


import com.sunbox.domain.OrderApprovalRequest;
import com.sunbox.domain.cluster.WorkOrderApprovalRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository
/**
 * 向Shein工单系统提交的审批请求
 */
public interface WorkOrderApprovalRequestMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WorkOrderApprovalRequest record);

    int insertSelective(WorkOrderApprovalRequest record);

    WorkOrderApprovalRequest selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WorkOrderApprovalRequest record);

    int updateByPrimaryKey(WorkOrderApprovalRequest record);

    List<WorkOrderApprovalRequest> selectByClusterIds(@Param("clusterIds")List<String> clusterIds);

    List<WorkOrderApprovalRequest> selectByTicketIds(@Param("ticketIds")List<String> ticketIds);

    int selectTotal(OrderApprovalRequest request);

    List<WorkOrderApprovalRequest> selectByPage(OrderApprovalRequest request);

    int selectTotalByList(@Param("request")OrderApprovalRequest request, @Param("clusterIdList") Set<String> clusterIdList);

    List<WorkOrderApprovalRequest> selectByPageList(@Param("request")OrderApprovalRequest request, @Param("clusterIdList") Set<String> clusterIdList);

}