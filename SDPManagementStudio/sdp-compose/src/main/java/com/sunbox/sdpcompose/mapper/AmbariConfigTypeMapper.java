package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ambari.AmbariConfigType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ambari配置类型;(ambari_config_type)表数据库访问层
 * author: wangda
 * date: 2022/12/3
 */
@Repository
@Mapper
public interface AmbariConfigTypeMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AmbariConfigType queryById(Long id);
    /**
     * 分页查询指定行数据
     *
     * @param ambariConfigType 查询条件
     * @param pageable 分页对象
     * @return 对象列表
     */
    List<AmbariConfigType> queryAllByLimit(AmbariConfigType ambariConfigType, @Param("pageable") Pageable pageable);
    /**
     * 统计总行数
     *
     * @param ambariConfigType 查询条件
     * @return 总行数
     */
    long count(AmbariConfigType ambariConfigType);
    /**
     * 新增数据
     *
     * @param ambariConfigType 实例对象
     * @return 影响行数
     */
    int insert(AmbariConfigType ambariConfigType);
    /**
     * 批量新增数据
     *
     * @param entities List<AmbariConfigType> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AmbariConfigType> entities);
    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<AmbariConfigType> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<AmbariConfigType> entities);
    /**
     * 更新数据
     *
     * @param ambariConfigType 实例对象
     * @return 影响行数
     */
    int update(AmbariConfigType ambariConfigType);
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
}
