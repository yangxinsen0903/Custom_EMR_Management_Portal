package com.sunbox.dao.mapper;

import com.sunbox.domain.TaskEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TaskEventMapper {
    /**
     * 根据主键查询一条记录
     * @param taskEventId
     * @return
     */
    TaskEvent selectByPrimaryKey(String taskEventId);

    /**
     * 根据主键删除一条记录
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 新增一条记录
     * @param record
     * @return
     */
    int insert(TaskEvent record);

    /**
     * 根据集群ID和任务ID查询列表
     */
    List<TaskEvent> selectByClusterIdAndPlanId(@Param("clusterId") String clusterId, @Param("planId") String planId);

    /**
     * 根据任务ID和活动日志ID查询列表
     */
    List<TaskEvent> selectByPlanIdAndActivityLogId(@Param("planId") String planId, @Param("planActivityLogId") String planActivityLogId);

    /**
     * 统计所有缩容销毁类任务失败的事件次数<br/>
     * 返回统计数据格式如下：<br/>
     * <ol>
     *     <li>cluster_id: 集群ID</li>
     *     <li>cluster_name: 集群名</li>
     *     <li>group_name: 实例组名</li>
     *     <li>event_type: 事件类型</li>
     *     <li>purchase_type: 实例类型</li>
     *     <li>fail_count: 失败数量</li>
     * </ol>
     */
    List<Map> statDestroyTaskFailEventCount();
}