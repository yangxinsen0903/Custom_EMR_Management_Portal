package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoCluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface InfoClusterMapper {
    int deleteByPrimaryKey(String clusterId);

    int insert(InfoCluster record);

    int insertSelective(InfoCluster record);

    InfoCluster selectByPrimaryKey(String clusterId);

    int updateByPrimaryKeySelective(InfoCluster record);

    int updateByPrimaryKey(InfoCluster record);

    List<Map> selectByObject(Map params);

    List<Map<String, Object>> selectByClusterIds(@Param("clusterIds") List<String> clusterIds);

    int countByObject(Map params);

    /**
     *  根据查询条件，查询竞价实例逐出数量
     * @param params
     * clusterId  集群ID
     * skuName 实例ID
     * dc 数据中心
     * endTime 截止时间
     * begTime 开始时间
     * @return
     * skuName
     * count
     */
    List<Map> getSpotEvictionCountByParam(Map params);

    /**
     *  根据查询条件，查询竞价实例买入的数量
     * @param params
     * clusterId  集群ID
     * skuName 实例ID
     * dc 数据中心
     * endTime 截止时间
     * begTime 开始时间
     * @return
     * skuName
     * count
     */
    List<Map> getSpotSaleCountByParam(Map params);

}