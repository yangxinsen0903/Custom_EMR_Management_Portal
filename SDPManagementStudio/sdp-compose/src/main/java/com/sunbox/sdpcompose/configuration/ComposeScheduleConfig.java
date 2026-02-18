package com.sunbox.sdpcompose.configuration;

import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class ComposeScheduleConfig implements SchedulingConfigurer, BaseCommonInterFace {
    @Value("${compose.task.thread.pool.size:50}")
    private Integer scaleTaskThreadPoolSize;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        getLogger().info("ComposeScheduleConfig 添加线程池");
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(scaleTaskThreadPoolSize));
    }
}
