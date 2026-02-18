package com.sunbox.sdpadmin.service;

/**
 * 自动启动已关闭的集群组件的服务接口
 * @date 2023/6/20
 */
public interface AutoStartClusterComponentService {

    /**
     * 自动启动已关闭的集群组件
     * @return
     */
    void autoStartClusterComponents();
}
