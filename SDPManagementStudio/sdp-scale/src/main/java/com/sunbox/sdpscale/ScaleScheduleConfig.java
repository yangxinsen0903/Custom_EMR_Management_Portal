package com.sunbox.sdpscale;

import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class ScaleScheduleConfig implements SchedulingConfigurer, BaseCommonInterFace {
    @Value("${scale.task.thread.pool.size:50}")
    private Integer scaleTaskThreadPoolSize;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        getLogger().error("ScaleScheduleConfig 添加线程池");
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(scaleTaskThreadPoolSize));
    }
}
