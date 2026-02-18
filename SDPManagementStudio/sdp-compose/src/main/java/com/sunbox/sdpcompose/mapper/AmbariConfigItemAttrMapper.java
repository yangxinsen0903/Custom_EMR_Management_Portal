package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ambari.AmbariConfigItemAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ambari配置项属性，默认在Stack中的配置;(ambari_config_item_attr)表数据库访问层
 * @author :wangda
 * @date : 2022-12-9
 */
@Mapper
@Repository
public interface AmbariConfigItemAttrMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AmbariConfigItemAttr queryById(Long id);
    /**
     * 分页查询指定行数据
     *
     * @param stackCode Stack代码，如： SDP-1.0.0
     * @param serviceCode 大数据服务代码列表，如 ['HDFS','YARN']
     * @return 对象列表
     */
    List<AmbariConfigItemAttr> queryByStackCodeAndServiceCode(String stackCode, List<String> serviceCode);
    /**
     * 统计总行数
     *
     * @param ambariConfigItemAttr 查询条件
     * @return 总行数
     */
    long count(AmbariConfigItemAttr ambariConfigItemAttr);
    /**
     * 新增数据
     *
     * @param ambariConfigItemAttr 实例对象
     * @return 影响行数
     */
    int insert(AmbariConfigItemAttr ambariConfigItemAttr);
    /**
     * 批量新增数据
     *
     * @param entities List<AmbariConfigItemAttr> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AmbariConfigItemAttr> entities);
    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<AmbariConfigItemAttr> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<AmbariConfigItemAttr> entities);
    /**
     * 更新数据
     *
     * @param ambariConfigItemAttr 实例对象
     * @return 影响行数
     */
    int update(AmbariConfigItemAttr ambariConfigItemAttr);
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
}