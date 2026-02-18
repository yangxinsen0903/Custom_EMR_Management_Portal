package com.sunbox.sdpspot.service.impl;

import com.sunbox.sdpspot.mapper.InfoClusterVmMapper;
import com.sunbox.sdpspot.service.SportService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpotServiceImpl implements SportService, BaseCommonInterFace {

    @Autowired
    DistributedRedisLock distributedRedisLock;

    @Autowired
    InfoClusterVmMapper infoClusterVmMapper;
}
