package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfCluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ConfClusterMapper {
    int deleteByPrimaryKey(String clusterId);

    int insert(ConfCluster record);

    int insertSelective(ConfCluster record);

    ConfCluster selectByPrimaryKey(String clusterId);

    List<ConfCluster> selectByClusterIds(@Param("clusterIds")List<String> clusterIds);

    String selectClusterNameByPrimaryKey(String clusterId);

    int updateByPrimaryKeySelective(ConfCluster record);

    int updateByPrimaryKey(ConfCluster record);

    /**
     * 查询指定状态的符合条件的集群.<br/>
     * 可以使用的参数为:region, clusterName, clusterId, emrStatus, atfer, before <br/>
     * emrStatus是List< Integer>类型, 会使用in语法查询
     *
     * @param params
     * @return
     */
    List<Map> selectByObject(Map params);

    ConfCluster findTop1ByObject(Map params);

    List<Map<String, Object>> selectByObjectInnerICV(Map<String, Object> params);

    List<Map> selectStateGroupByState();
}