package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ambari.StackServiceComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Stack布署的大数据组件;(stack_service_component)表数据库访问层
 * author: wangda
 * date: 2022/12/3
 */
@Repository
@Mapper
public interface StackServiceComponentMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    StackServiceComponent queryById(Long id);
    /**
     * 分页查询指定行数据
     *
     * @param stackServiceComponent 查询条件
     * @param pageable 分页对象
     * @return 对象列表
     */
    List<StackServiceComponent> queryAllByLimit(StackServiceComponent stackServiceComponent, @Param("pageable") Pageable pageable);
    /**
     * 统计总行数
     *
     * @param stackServiceComponent 查询条件
     * @return 总行数
     */
    long count(StackServiceComponent stackServiceComponent);
    /**
     * 新增数据
     *
     * @param stackServiceComponent 实例对象
     * @return 影响行数
     */
    int insert(StackServiceComponent stackServiceComponent);
    /**
     * 批量新增数据
     *
     * @param entities List<StackServiceComponent> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<StackServiceComponent> entities);
    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<StackServiceComponent> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<StackServiceComponent> entities);
    /**
     * 更新数据
     *
     * @param stackServiceComponent 实例对象
     * @return 影响行数
     */
    int update(StackServiceComponent stackServiceComponent);
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
}
