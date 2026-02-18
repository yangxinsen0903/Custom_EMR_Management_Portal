package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.BaseScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * 用户上传的需要执行的脚本库;(base_script)表数据库访问层
 * @author: wangda
 * @date: 2022/12/23
 */
@Mapper
public interface BaseScriptMapper{
    /**
     * 通过ID查询单条数据
     *
     * @param scriptId 主键
     * @return 实例对象
     */
    BaseScript queryById(String scriptId);

    /**
     * 根据脚本名查询脚本
     * @param scriptName 脚本名
     * @return
     */
    List<BaseScript> queryByScriptName(String scriptName);

    /**
     * 分页查询指定行数据
     *
     * @param baseScript 查询条件
     * @param pageable 分页对象
     * @return 对象列表
     */
    List<BaseScript> queryAllByLimit(BaseScript baseScript, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param baseScript 查询条件
     * @return 总行数
     */
    long count(BaseScript baseScript);

    /**
     * 新增数据
     *
     * @param baseScript 实例对象
     * @return 影响行数
     */
    int insert(BaseScript baseScript);

    /**
     * 批量新增数据
     *
     * @param entities List<BaseScript> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<BaseScript> entities);

    /**
     * 批量新增或按主键更新数据
     *
     * @param entities List<BaseScript> 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<BaseScript> entities);

    /**
     * 更新数据
     *
     * @param baseScript 实例对象
     * @return 影响行数
     */
    int update(BaseScript baseScript);

    /**
     * 通过主键删除数据
     *
     * @param scriptId 主键
     * @return 影响行数
     */
    int deleteById(String scriptId);
}