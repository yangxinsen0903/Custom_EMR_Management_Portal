package com.sunbox.sdpadmin.task;

import com.sunbox.sdpadmin.service.AutoStartClusterComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @date 2023/6/23
 */
@Component
@EnableAsync
public class AutoStartClsuterComponentTask {

    /**
     * 检查并重启集群中异常关闭组件的定时任务执行时间间隔, 单位:秒，默认1小时检查一遍
     */
    @Value("${auto.start.cluster.component.interval:3600000}")
    public Integer scheduleTime;

    @Autowired
    private AutoStartClusterComponentService autoStartClusterComponentService;

    @Scheduled(fixedDelayString = "${auto.start.cluster.component.interval:3600000}")
    @Async
    public void start() {
        autoStartClusterComponentService.autoStartClusterComponents();
    }
}
