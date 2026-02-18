package com.sunbox.sdpscale.listener;

import com.sunbox.sdpscale.task.ScaleContext;
import com.sunbox.sdpscale.constant.ScaleConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CloseListener implements ApplicationListener<ContextClosedEvent>, BaseCommonInterFace, ScaleConstant {
    @Autowired
    private DistributedRedisLock redisLock;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        List<String> list = redisLock.getList(metric_machine_ips);
        for (String ips : list) {
            if (!ips.startsWith(ScaleContext.ip)) continue;

            getLogger().info("销毁注册的信息-开始,key={},ip={}", metric_machine_ips, ips);
            redisLock.removeValueFromList(metric_machine_ips, ips);
            getLogger().info("销毁注册的信息-完成,key={},ip={}", metric_machine_ips, ips);
        }
    }
}
