package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfScalingTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ConfScalingTaskMapper {
    int deleteByPrimaryKey(String taskId);

    int insert(ConfScalingTask record);

    int insertSelective(ConfScalingTask record);

    ConfScalingTask selectByPrimaryKey(String taskId);

    ConfScalingTask queryRunningTask(@Param("clusterId") String clusterId,
                                     @Param("vmRole") String vmRole,
                                     @Param("createTime") Date createTime,
                                     @Param("groupName") String groupName,
                                     @Param("states") List<Integer> state);

    ConfScalingTask peekQueueHeadTask(@Param("clusterId") String clusterId,
                                      @Param("vmRole") String vmRole,
                                      @Param("groupName") String groupName,
                                      @Param("states") List<Integer> state);

    ConfScalingTask findLastByScalingTypeAndState(@Param("clusterId") String clusterId,
                                                  @Param("vmRole") String vmRole,
                                                  @Param("groupName") String groupName,
                                                  @Param("scalingType") Integer scalingType,
                                                  @Param("state") Integer state);

    ConfScalingTask findLastFinishedTask(@Param("clusterId") String clusterId,
                                      @Param("vmRole") String vmRole,
                                      @Param("groupName") String groupName,
                                      @Param("state") Integer state);

    ConfScalingTask findLastTaskByStates(@Param("clusterId") String clusterId,
                                         @Param("vmRole") String vmRole,
                                         @Param("groupName") String groupName,
                                         @Param("state1") Integer state1,
                                         @Param("state2") Integer state2);

    ConfScalingTask queryRunningTaskByScalingType(@Param("clusterId") String clusterId,
                                     @Param("vmRole") String vmRole,
                                     @Param("createTime") Date createTime,
                                     @Param("scalingType") Integer scalingType,
                                     @Param("states") List<Integer> state);

    int countRunningTaskByScalingTypes(@Param("clusterId") String clusterId,
                                                  @Param("vmRole") String vmRole,
                                                  @Param("createTime") Date createTime,
                                                  @Param("scalingTypes") List<Integer> scalingType,
                                                  @Param("states") List<Integer> state);

    int countByClusterIdAndStates(@Param("clusterId") String clusterId, @Param("states") List<Integer> state);

    int updateByPrimaryKeySelective(ConfScalingTask record);

    int updateByPrimaryKey(ConfScalingTask record);

    /**
     * 根据参数查询伸缩task任务数
     * @param param
     * <if test="clusterId != null">
     *         and cluster_id = #{clusterId,jdbcType=VARCHAR}
     *       </if>
     *       <if test="groupName != null">
     *          and group_name = #{groupName,jdbcType=VARCHAR}
     *       </if>
     *       <if test="scalingType != null">
     *         and scaling_type = #{scalingType,jdbcType=INTEGER}
     *       </if>
     * @return
     */
    int getTaskCountByParam(Map param);

    /**
     *  查询扩缩容任务列表
     *
     * @param clusterId
     * @param vmRole
     * @param state
     * @param scalingType
     * @return
     */
    List<ConfScalingTask> queryTasksByStateAndVmRoleAndClusterId(@Param("clusterId") String clusterId,
                                                                 @Param("vmRole") String vmRole,
                                                                 @Param("state") Integer state,
                                                                 @Param("scalingType") Integer scalingType);

    Integer getAllScaleOutVMCountByClusterAndVmRole(@Param("clusterId") String clusterId,
                                                @Param("vmRole") String vmRole,
                                                @Param("taskId") String taskId,
                                                @Param("createTime") Date createTime);

    int countByScalingTypeAndState(@Param("clusterId") String clusterId,
                                   @Param("groupName") String groupName,
                                   @Param("scalingType1") Integer scalingType1,
                                   @Param("scalingType2") Integer scalingType2,
                                   @Param("state1") Integer state1,
                                   @Param("state2") Integer state2);

    int countByScaleOutTaskIdAndScalingTypeAndState(@Param("clusterId") String clusterId,
                                   @Param("groupName") String groupName,
                                   @Param("scaleOutTaskId") String scaleOutTaskId,
                                   @Param("scalingType") Integer scalingType,
                                   @Param("state1") Integer state1,
                                   @Param("state2") Integer state2);
}