package com.sunbox.sdpcompose.service.impl;

import com.sunbox.domain.ConfCluster;
import com.sunbox.sdpcompose.mapper.ConfClusterVmMapper;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/3/22
 */
public class ClusterServiceImplTest {

    @Test
    public void generateGroupCustomConfig() {
        ClusterServiceImpl svc = new ClusterServiceImpl();
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId("1");
        svc.confClusterVmMapper = Mockito.mock(ConfClusterVmMapper.class);
        List<BlueprintConfiguration> configurations = svc.generateGroupCustomConfig("task-3", confCluster, "core");
    }
}