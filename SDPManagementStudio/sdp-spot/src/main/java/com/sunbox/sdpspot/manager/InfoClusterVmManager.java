package com.sunbox.sdpspot.manager;

import cn.hutool.json.JSONUtil;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.sdpspot.constant.RedisConst;
import com.sunbox.sdpspot.mapper.InfoClusterVmMapper;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

public class InfoClusterVmManager {
    public static InfoClusterVm getByVmName(Logger logger,
                                            DistributedRedisLock distributedRedisLock,
                                            InfoClusterVmMapper infoClusterVmMapper,
                                            String vmName) {
        String redisKey = RedisConst.keyInfoClusterVms(vmName);
        String valueFromRedis = distributedRedisLock.getValue(redisKey);
        InfoClusterVm infoClusterVm = null;
        if (StringUtils.isNotEmpty(valueFromRedis)) {
            infoClusterVm = JSONUtil.toBean(valueFromRedis, InfoClusterVm.class);
            distributedRedisLock.expire(redisKey, RedisConst.EXPIRES_INFO_CLUSTER_VMS, TimeUnit.SECONDS);
            return infoClusterVm;
        }

        infoClusterVm = infoClusterVmMapper.findByVmName(vmName, ConfClusterVm.PURCHASETYPE_SPOT, InfoClusterVm.VM_RUNNING);
        if (infoClusterVm == null) {
            logger.warn("not found infoClusterVm vmName:{}", vmName);
            throw new RuntimeException("没有找到主机名对应的主机");
        }

        valueFromRedis = JSONUtil.toJsonStr(infoClusterVm);
        distributedRedisLock.save(redisKey, valueFromRedis, RedisConst.EXPIRES_INFO_CLUSTER_VMS, TimeUnit.SECONDS);
        return infoClusterVm;
    }
}
