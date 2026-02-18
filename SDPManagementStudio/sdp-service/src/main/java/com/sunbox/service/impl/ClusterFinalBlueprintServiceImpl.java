package com.sunbox.service.impl;

import cn.hutool.core.util.StrUtil;
import com.sunbox.dao.mapper.InfoClusterFinalBlueprintMapper;
import com.sunbox.domain.InfoClusterFinalBlueprint;
import com.sunbox.service.IClusterFinalBlueprintService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 销毁集群配置表(InfoClusterFinalBlueprint)表服务实现类
 *
 * @since 2024-07-02 18:31:08
 */
@Service("clusterFinalBlueprintService")
public class ClusterFinalBlueprintServiceImpl implements IClusterFinalBlueprintService {
    @Resource
    private InfoClusterFinalBlueprintMapper infoClusterFinalBlueprintMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param clusterId 主键
     * @return 实例对象
     */
    @Override
    public InfoClusterFinalBlueprint queryById(String clusterId) {
        return this.infoClusterFinalBlueprintMapper.queryById(clusterId);
    }

    /**
     * 新增数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 实例对象
     */
    @Override
    public InfoClusterFinalBlueprint insert(InfoClusterFinalBlueprint infoClusterFinalBlueprint) {
        this.infoClusterFinalBlueprintMapper.insert(infoClusterFinalBlueprint);
        return infoClusterFinalBlueprint;
    }

    /**
     * 修改数据
     *
     * @param infoClusterFinalBlueprint 实例对象
     * @return 实例对象
     */
    @Override
    public InfoClusterFinalBlueprint update(InfoClusterFinalBlueprint infoClusterFinalBlueprint) {
        this.infoClusterFinalBlueprintMapper.update(infoClusterFinalBlueprint);
        return this.queryById(infoClusterFinalBlueprint.getClusterId());
    }

    /**
     * 新增或者更新数据
     * @param infoClusterFinalBlueprint
     * @return
     */
    public InfoClusterFinalBlueprint insertOrUpdate(InfoClusterFinalBlueprint infoClusterFinalBlueprint) {
        InfoClusterFinalBlueprint blueprint = this.infoClusterFinalBlueprintMapper.queryById(infoClusterFinalBlueprint.getClusterId());
        if (blueprint==null){
            this.infoClusterFinalBlueprintMapper.insert(infoClusterFinalBlueprint);
        }else {
            this.infoClusterFinalBlueprintMapper.update(infoClusterFinalBlueprint);
        }
        return infoClusterFinalBlueprint;
    }

    /**
     * 通过主键删除数据
     *
     * @param clusterId 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String clusterId) {
        return this.infoClusterFinalBlueprintMapper.deleteById(clusterId) > 0;
    }
}
