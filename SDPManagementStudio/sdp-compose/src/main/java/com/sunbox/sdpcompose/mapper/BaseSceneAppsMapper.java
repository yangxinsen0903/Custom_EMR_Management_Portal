package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.BaseSceneApps;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.util.List;

/**
 * 场景应用组件基础信息;(base_scene_apps)表数据库访问层
 * @author : wangda
 * @date : 2022-12-21
 */
@Mapper
public interface BaseSceneAppsMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param sceneId 主键
     * @return 实例对象
     */
    List<BaseSceneApps> queryBySceneId(String sceneId);
    /**
     * 分页查询指定行数据
     *
     * @param baseSceneApps 查询条件
     * @param pageable 分页对象
     * @return 对象列表
     */
    List<BaseSceneApps> queryAllByLimit(BaseSceneApps baseSceneApps, @Param("pageable") SpringDataWebProperties.Pageable pageable);
    /**
     * 统计总行数
     *
     * @param baseSceneApps 查询条件
     * @return 总行数
     */
    long count(BaseSceneApps baseSceneApps);
    /**
     * 新增数据
     *
     * @param baseSceneApps 实例对象
     * @return 影响行数
     */
    int insert(BaseSceneApps baseSceneApps);

    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<BaseSceneApps> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<BaseSceneApps> entities);
    /**
     * 更新数据
     *
     * @param baseSceneApps 实例对象
     * @return 影响行数
     */
    int update(BaseSceneApps baseSceneApps);
    /**
     * 通过主键删除数据
     *
     * @param appName 主键
     * @return 影响行数
     */
    int deleteById(String appName);
}