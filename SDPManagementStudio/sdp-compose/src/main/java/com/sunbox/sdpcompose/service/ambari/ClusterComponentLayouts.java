package com.sunbox.sdpcompose.service.ambari;

import com.sunbox.domain.InfoClusterComponentLayout;
import com.sunbox.sdpcompose.mapper.InfoClusterComponentLayoutMapper;
import com.sunbox.sdpcompose.util.SpringContextUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集群布局
 * @author: wangda
 * @date: 2023/2/14
 */
public class ClusterComponentLayouts {
    /** 集群ID */
    private String clusterId;

    /** 该集群全部的布局信息 */
    List<InfoClusterComponentLayout> infoClusterComponentLayouts = new ArrayList<>();

    /** 用于快速查询使用的Map */
    Map<String, InfoClusterComponentLayout> layoutMap = new HashMap<>();


    /**
     * 从数据库加载集群的布局
     * @param clusterId
     * @return
     */
    public static ClusterComponentLayouts loadLayouts(String clusterId) {
        ClusterComponentLayouts layouts = new ClusterComponentLayouts();
        layouts.loadFromDb(clusterId);
        layouts.generateLayoutMap();
        return layouts;
    }

    /**
     * 将布局保存到数据库。此方法的保存仅使用最简单的保存方式：先全量删除，再全量新增
     */
    public void saveAllLayout() {
        // 删除
        getLayoutMapper().deleteByClusterId(clusterId);

        // 新增
        for (InfoClusterComponentLayout infoClusterComponentLayout : infoClusterComponentLayouts) {
            getLayoutMapper().insert(infoClusterComponentLayout);
        }
    }

    /**
     * 保存一个实例组的布局
     * @param hostGroupName 实例组的名筄
     */
    public void saveHostGroupLayout(String hostGroupName) {
        List<InfoClusterComponentLayout> hostGroup = new ArrayList<>();
        for (InfoClusterComponentLayout infoClusterComponentLayout : infoClusterComponentLayouts) {
            if (Objects.equals(hostGroupName, infoClusterComponentLayout.getHostGroup())) {
                hostGroup.add(infoClusterComponentLayout);
            }
        }
        // 删除
        getLayoutMapper().deleteByClusterIdAndHostGroup(clusterId, hostGroupName);

        // 保存
        for (InfoClusterComponentLayout infoClusterComponentLayout : hostGroup) {
            getLayoutMapper().insert(infoClusterComponentLayout);
        }
    }

    /**
     * 增加一个布局组件
     * @param componentLayout
     */
    public void addComponentLayout(InfoClusterComponentLayout componentLayout) {
        if (Objects.isNull(infoClusterComponentLayouts)) {
            infoClusterComponentLayouts = new ArrayList<>();
        }

        // 检查是否已经存在， 如果已经存在，不增加
        InfoClusterComponentLayout layout = layoutMap.get(genLayoutKey(componentLayout));

        if (Objects.isNull(layout)) {
            infoClusterComponentLayouts.add(componentLayout);
            layoutMap.put(genLayoutKey(componentLayout), componentLayout);
        }
    }

    /** 集群组件布局存储器 */
    private InfoClusterComponentLayoutMapper layoutMapper;

    private void loadFromDb(String clusterId) {
        this.clusterId = clusterId;
        this.infoClusterComponentLayouts = getLayoutMapper().selectByClusterId(clusterId);
    }

    private InfoClusterComponentLayoutMapper getLayoutMapper() {
        if (Objects.isNull(layoutMapper)) {
            layoutMapper = SpringContextUtil.getBean(InfoClusterComponentLayoutMapper.class);
        }
        return layoutMapper;
    }

    private String genLayoutKey(InfoClusterComponentLayout layout) {
        return layout.getServiceCode() + layout.getComponentCode() + layout.getHostGroup();
    }

    private void generateLayoutMap() {
        layoutMap = infoClusterComponentLayouts.stream().collect(Collectors.toMap(layout -> {
            return genLayoutKey(layout);
        }, Function.identity()));
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
