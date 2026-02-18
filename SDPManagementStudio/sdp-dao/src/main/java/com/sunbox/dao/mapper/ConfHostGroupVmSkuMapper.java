package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfHostGroupVmSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 实例组SKU表(ConfHostGroupVmSku)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-05 10:44:37
 */
@Mapper
@Repository
public interface ConfHostGroupVmSkuMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param vmSkuId 主键
     * @return 实例对象
     */
    ConfHostGroupVmSku queryById(String vmSkuId);

    /**
     * 查询指定行数据
     *
     * @param confHostGroupVmSku 查询条件
     * @return 对象列表
     */
    List<ConfHostGroupVmSku> queryAllByLimit(@Param("entity") ConfHostGroupVmSku confHostGroupVmSku,
                                             @Param("pageIndex") int pageIndex,
                                             @Param("pageSize") int pageSize);

    /**
     * 统计总行数
     *
     * @param confHostGroupVmSku 查询条件
     * @return 总行数
     */
    long count(@Param("entity") ConfHostGroupVmSku confHostGroupVmSku);

    /**
     * 新增数据
     *
     * @param confHostGroupVmSku 实例对象
     * @return 影响行数
     */
    int insert(ConfHostGroupVmSku confHostGroupVmSku);

    /**
     * 修改数据
     *
     * @param confHostGroupVmSku 实例对象
     * @return 影响行数
     */
    int update(ConfHostGroupVmSku confHostGroupVmSku);

    /**
     * 通过主键删除数据
     *
     * @param vmSkuId 主键
     * @return 影响行数
     */
    int deleteById(String vmSkuId);

    /**
     * 根据集群id和分组id查询信息
     * @return 对象列表
     */
    List<ConfHostGroupVmSku> selectByClusterIdAndGroupId(@Param("clusterId") String clusterId, @Param("groupId") String groupId);

    /**
     * 根据集群id和分组id查询信息
     * @return 对象列表
     */
    List<ConfHostGroupVmSku> selectByClusterIdAndGroupName(@Param("clusterId") String clusterId, @Param("groupName") String groupName);

    /**
     * 根据集群id查询信息
     * @return 对象列表
     */
    List<ConfHostGroupVmSku> selectByClusterId(@Param("clusterId") String clusterId);

    List<ConfHostGroupVmSku> selectByVmConfIdAndSku(@Param("vmConfId") String vmConfId, @Param("sku") String sku);

    List<ConfHostGroupVmSku> selectByVmConfId(@Param("vmConfId") String vmConfId);

    ConfHostGroupVmSku selectOldOneByClusterIdAndvmRole(@Param("clusterId") String clusterId, @Param("vmRole") String vm_role);

    ConfHostGroupVmSku selectOneByClusterIdAndSku(@Param("clusterId") String clusterId, @Param("sku") String sku);
}

