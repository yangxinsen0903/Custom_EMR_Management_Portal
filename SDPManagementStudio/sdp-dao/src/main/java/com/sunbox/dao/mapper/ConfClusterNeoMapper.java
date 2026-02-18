package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfCluster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface ConfClusterNeoMapper {

    ConfCluster selectByPrimaryKey(String clusterId);

    /**
     * 根据集群名和状态查询集群
     * @param clusterName 集群名称
     * @param state 状态编码
     * @return 如果没有, 返回Null
     */
    ConfCluster selectByClusterNameAndState(@Param("clusterName") String clusterName,
                                            @Param("state") Integer state);

    /**
     * 根据名称查找最近的一个集群
     * @param clusterName 集群名
     * @return
     */
    ConfCluster selectLasestClusterByName(@Param("clusterName") String clusterName);


    /**
     * 模糊查询指定集群名称的集群id
     * @param clusterName
     * @return
     */
    List<ConfCluster> selectByName(String clusterName);

    List<ConfCluster> selectByClusterIds(@Param("clusterIds")List<String> clusterIds);


}
