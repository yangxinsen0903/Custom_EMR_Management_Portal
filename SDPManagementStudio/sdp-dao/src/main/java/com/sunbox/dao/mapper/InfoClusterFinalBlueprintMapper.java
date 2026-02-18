package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterFinalBlueprint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销毁集群配置表(InfoClusterFinalBlueprint)表数据库访问层
 *
 * @since 2024-07-02 18:30:56
 */
@Mapper
@Repository
public interface InfoClusterFinalBlueprintMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param clusterId 主键
     * @return 实例对象
     */
    InfoClusterFinalBlueprint queryById(String clusterId);

    /**
     * 统计总行数
     *
     * @param infoClusterFinalBlueprint 查询条件
     * @return 总行数
     */
    long count(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 新增数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 影响行数
     */
    int insert(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<InfoClusterFinalBlueprint> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<InfoClusterFinalBlueprint> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<InfoClusterFinalBlueprint> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<InfoClusterFinalBlueprint> entities);

    /**
     * 修改数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 影响行数
     */
    int update(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 通过主键删除数据
     *
     * @param clusterId 主键
     * @return 影响行数
     */
    int deleteById(String clusterId);

}

