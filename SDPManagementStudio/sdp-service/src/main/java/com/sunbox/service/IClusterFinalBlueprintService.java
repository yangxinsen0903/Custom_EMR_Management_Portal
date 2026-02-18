package com.sunbox.service;

import com.sunbox.domain.InfoClusterFinalBlueprint;

/**
 * 销毁集群配置表(InfoClusterFinalBlueprint)表服务接口
 *
 * @since 2024-07-02 18:31:04
 */
public interface IClusterFinalBlueprintService {

    /**
     * 通过ID查询单条数据
     *
     * @param clusterId 主键
     * @return 实例对象
     */
    InfoClusterFinalBlueprint queryById(String clusterId);

    /**
     * 新增数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 实例对象
     */
    InfoClusterFinalBlueprint insert(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 修改数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 实例对象
     */
    InfoClusterFinalBlueprint update(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 新增或者更新数据
     * @param infoClusterFinalBlueprint
     * @return
     */
    InfoClusterFinalBlueprint insertOrUpdate(InfoClusterFinalBlueprint infoClusterFinalBlueprint);

    /**
     * 通过主键删除数据
     *
     * @param clusterId 主键
     * @return 是否成功
     */
    boolean deleteById(String clusterId);

}
