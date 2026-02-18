package com.sunbox.service.impl;

import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.domain.ConfCluster;
import com.sunbox.service.INeoConfClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NeoConfClusterServiceImpl implements INeoConfClusterService {

    @Autowired
    private ConfClusterNeoMapper confClusterNeoMapper;

    /**
     * 根据集群Id查询集群信息
     *
     * @param clusterId 集群Id
     * @return
     */
    @Override
    public ConfCluster getConfClusterByClusterId(String clusterId) {
        return confClusterNeoMapper.selectByPrimaryKey(clusterId);
    }

}
