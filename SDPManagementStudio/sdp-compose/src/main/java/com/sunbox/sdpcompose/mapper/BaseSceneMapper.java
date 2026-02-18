package com.sunbox.sdpcompose.mapper;


import com.sunbox.domain.BaseScene;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.util.List;

/**
 * 场景基础信息;(base_scene)表数据库访问层
 * @author : wangda
 * @date : 2022-12-21
 */
@Mapper
public interface BaseSceneMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param sceneId 主键
     * @return 实例对象
     */
    BaseScene queryById(String sceneId);

    /**
     * 根据版本和场景查询场景对象
     * @param releaseVer Stack版本
     * @param scene 场景字符串
     * @return
     */
    BaseScene queryByReleaseVerAndSceneName(String releaseVer, String scene);
    /**
     * 分页查询指定行数据
     *
     * @param baseScene 查询条件
     * @param pageable 分页对象
     * @return 对象列表
     */
    List<BaseScene> queryAllByLimit(BaseScene baseScene, @Param("pageable") SpringDataWebProperties.Pageable pageable);
    /**
     * 统计总行数
     *
     * @param baseScene 查询条件
     * @return 总行数
     */
    long count(BaseScene baseScene);
    /**
     * 新增数据
     *
     * @param baseScene 实例对象
     * @return 影响行数
     */
    int insert(BaseScene baseScene);
    /**
     * 批量新增数据
     *
     * @param entities List<BaseScene> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<BaseScene> entities);

    /**
     * 更新数据
     *
     * @param baseScene 实例对象
     * @return 影响行数
     */
    int update(BaseScene baseScene);

    /**
     * 通过主键删除数据
     *
     * @param sceneId 主键
     * @return 影响行数
     */
    int deleteById(String sceneId);
}
