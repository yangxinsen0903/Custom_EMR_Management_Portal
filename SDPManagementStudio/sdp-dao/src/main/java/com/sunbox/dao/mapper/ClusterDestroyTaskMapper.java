package com.sunbox.dao.mapper;

import com.sunbox.domain.ClusterDestroyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface ClusterDestroyTaskMapper {
    int deleteByPrimaryKey(String clusterId);

    int insert(ClusterDestroyTask record);

    int insertSelective(ClusterDestroyTask record);

    /**
     * 废弃
     * @param clusterId
     * @return
     */
    @Deprecated
    ClusterDestroyTask selectByPrimaryKey(String clusterId);

    int updateByPrimaryKeySelective(ClusterDestroyTask record);

    int updateBatchTaskById(@Param("clusterIds") List<String> clusterIds, @Param("destroy_status") String destroy_status,
                            @Param("start_destroy_time") Date startDestroyTime, @Param("end_destroy_time") Date endDestroyTime);

    int updateBatchTaskByIds(@Param("ids") List<Long> ids, @Param("destroy_status") String destroy_status,
                            @Param("start_destroy_time") Date startDestroyTime, @Param("end_destroy_time") Date endDestroyTime);

    int updateByPrimaryKey(ClusterDestroyTask record);

   List<ClusterDestroyTask> selectByStatus(@Param("count")Integer count, @Param("destroy_status")String destroy_status);

   List<ClusterDestroyTask> selectByStatusList( @Param("destroy_status")List<String> destroy_status );

    List<ClusterDestroyTask> selectByNameAndStatus(@Param("clusterName") String clusterName,
                                                   @Param("destroyStatus")String destroyStatus,
                                                   @Param("pageStart") Integer pageStart,
                                                   @Param("pageLimit") Integer pageLimit);

    Integer countByNameAndStatus(@Param("clusterName") String clusterName,
                                                   @Param("destroyStatus")String destroyStatus);

    List<ClusterDestroyTask> selectByClusterId(String clusterId);

}