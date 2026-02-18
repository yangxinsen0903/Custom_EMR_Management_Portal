package com.sunbox.sdpadmin.task;

import com.sunbox.dao.mapper.AuthKeyMapper;
import com.sunbox.domain.ApiAuthKey;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AuthKeyDaemonTask implements BaseCommonInterFace {

    @Resource
    private AuthKeyMapper authKeyMapper;

    @Scheduled(cron = "${authkey.task.time:1/5 * * * * ?}")
    public void start() {
        getLogger().info("AuthKeyDaemonTask start");
        //查询所有的ApiAuthKey中expirationDate小于当前时间的记录，并更新状态为过期
        List<ApiAuthKey> apiAuthKeys = authKeyMapper.selectAllExpire();
        if (CollectionUtils.isEmpty(apiAuthKeys)) {
            return;
        }
        apiAuthKeys.forEach(apiAuthKey -> {
            apiAuthKey.setStatus(SheinParamConstant.EXPIRED);
            authKeyMapper.updateByPrimaryKey(apiAuthKey);
        });
    }
}
