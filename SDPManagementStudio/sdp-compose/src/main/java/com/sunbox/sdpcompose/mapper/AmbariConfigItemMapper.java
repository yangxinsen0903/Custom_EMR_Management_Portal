package com.sunbox.sdpcompose.mapper;

import java.util.List;

import com.sunbox.domain.ambari.AmbariConfigItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Ambari默认配置项;(ambari_config_item)表数据库访问层
 * author: wangda
 * date: 2022/12/3
 */
@Repository
@Mapper
public interface AmbariConfigItemMapper {
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AmbariConfigItem queryById(Long id);

    /**
     * 分页查询指定行数据
     *
     * @param ambariConfigItem 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<AmbariConfigItem> queryAllByLimit(AmbariConfigItem ambariConfigItem, @Param("pageable") Pageable pageable);

    List<AmbariConfigItem> queryByServiceId(List<Long> serviceIds);

    /**
     * 查询包括了 <code>HOSTGROUP:: </code> 关键字段的所有配置项
     * @param itemType 配置项状态， HA 或  NON_HA
     * @return
     */
    List<AmbariConfigItem> queryHostGroupConfigByItemType(String itemType);

    /**
     * 查询某个Stack下， 指定服务的所有配置项
     * @param stackCode
     * @param services
     * @return
     */
    List<AmbariConfigItem> queryByStackCodeAndServices(String stackCode, List<String> services, String itemType);


    /**
     * 根据动态配置类型和是否集群的类型 查询
     * @param dynamicType 动态配置的类型
     * @param itemType 集群配置
     */
    List<AmbariConfigItem> queryByDynamicTypeAndItemType(@Param("stackVersion") String stackVersion,
                                                         @Param("dynamicType")  String dynamicType,
                                                         @Param("itemType")  String itemType);

    /**
     * 统计总行数
     *
     * @param ambariConfigItem 查询条件
     * @return 总行数
     */
    long count(AmbariConfigItem ambariConfigItem);

    /**
     * 新增数据
     *
     * @param ambariConfigItem 实例对象
     * @return 影响行数
     */
    int insert(AmbariConfigItem ambariConfigItem);

    /**
     * 批量新增数据
     *
     * @param entities List<AmbariConfigItem> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AmbariConfigItem> entities);

    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<AmbariConfigItem> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<AmbariConfigItem> entities);

    /**
     * 更新数据
     *
     * @param ambariConfigItem 实例对象
     * @return 影响行数
     */
    int update(AmbariConfigItem ambariConfigItem);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
