package com.sunbox.sdptask.mapper;

import com.sunbox.domain.ConfCluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterMapper {
    int deleteByPrimaryKey(String clusterId);

    int insert(ConfCluster record);

    int insertSelective(ConfCluster record);

    ConfCluster selectByPrimaryKey(String clusterId);

    ConfCluster selectByClusterName(String clusterName);

    String selectClusterNameByPrimaryKey(String clusterId);

    int updateByPrimaryKeySelective(ConfCluster record);

    int updateCreationSubStateByClusterId(@Param("clusterId") String clusterId,
                                          @Param("creationSubState") String creationSubState);

    int updateCreationSubStateByClusterIdAndCreationSubStateIsNotNull(@Param("clusterId") String clusterId,
                                                                      @Param("creationSubState") String creationSubState);

    /**
     * 更新集群状态
     *
     * @param clusterId 集群ID
     * @param state     状态： 0 待创建； 1 创建中； 2 已创建；  -1释放中； -2 已释放
     * @return 更新记录数
     */
    int updateClusterState(String clusterId, Integer state);

    int updateBatchClusterState(@Param("clusterIds")List<String> clusterIds,@Param("state") Integer state);


    int updateByPrimaryKey(ConfCluster record);


    List<ConfCluster> selectByClusterIdAndStateAndCreationSubState(@Param("clusterId") String clusterId,
                                                                   @Param("state") Integer state,
                                                                   @Param("creationSubState") String creationSubState);

    List<ConfCluster> selectByState(@Param("state") Integer state, @Param("region") String region);
}