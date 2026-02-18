package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ambari.AmbariComponentLayout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 组件布局的Mapper
 *
 * @author: wangda
 * @date: 2022/12/8
 */
@Mapper
@Repository
public interface AmbariComponentLayoutMapper {
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AmbariComponentLayout queryById(Long id);

    /**
     * 分页查询指定行数据
     *
     * @param layout 查询条件
     * @return 对象列表
     */
    List<AmbariComponentLayout> queryAll(AmbariComponentLayout layout);


    /**
     * 查询布局项
     *
     * @param hostGroup   主机分组名称
     * @param serviceCode 需要布署的大数据组件
     * @return 组件列表
     */
    List<AmbariComponentLayout> queryByHostGroupAndServiceCode(String stackCode, String hostGroup, List<String> serviceCode, Integer isHa);

    /**
     * 统计总行数
     *
     * @param ambariComponentLayout 查询条件
     * @return 总行数
     */
    long count(AmbariComponentLayout ambariComponentLayout);

    /**
     * 新增数据
     *
     * @param ambariComponentLayout 实例对象
     * @return 影响行数
     */
    int insert(AmbariComponentLayout ambariComponentLayout);

    /**
     * 批量新增数据
     *
     * @param entities List<AmbariComponentLayout> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AmbariComponentLayout> entities);

    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<AmbariComponentLayout> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<AmbariComponentLayout> entities);

    /**
     * 更新数据
     *
     * @param ambariComponentLayout 实例对象
     * @return 影响行数
     */
    int update(AmbariComponentLayout ambariComponentLayout);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
}
