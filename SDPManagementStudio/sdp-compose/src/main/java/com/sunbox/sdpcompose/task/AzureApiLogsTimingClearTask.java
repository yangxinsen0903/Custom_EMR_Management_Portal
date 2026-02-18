package com.sunbox.sdpcompose.task;

import com.sunbox.sdpcompose.service.IFullLogService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Description Azure-api日志定时清理
 * @Author shishicheng
 * @Date 2023/3/24 10:59
 */
@Component
public class AzureApiLogsTimingClearTask {

    @Value("${azure.api.logs.retention.days:10}")
    private int retentionDays;

    @Autowired
    private IFullLogService fullLogService;

    @Scheduled(cron = "0 0 2 */3 * ?")
    public void azureApiLogsTimingClear() {
        Date currentTime = new Date();
        Date targetDate = DateUtils.addDays(currentTime, -retentionDays);
        fullLogService.deleteLogsByResponseTime(targetDate);
    }
}
