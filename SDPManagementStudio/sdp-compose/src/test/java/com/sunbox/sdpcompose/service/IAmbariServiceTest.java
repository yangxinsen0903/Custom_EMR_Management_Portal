package com.sunbox.sdpcompose.service;

import cn.hutool.core.lang.Console;
import com.sunbox.domain.ConfScalingTask;
import com.sunbox.sdpcompose.SdpComposeApplication;
import com.sunbox.sdpcompose.mapper.ConfScalingTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;


/**
 * @author: wangda
 * @date: 2022/12/10
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SdpComposeApplication.class},properties = {"log.zip.upload.url=http://test"})

public class IAmbariServiceTest {

    @Resource
    public IAmbariService service;

    @Autowired
    public ConfScalingTaskMapper taskMapper;

    @Test
    void createCluster() {
        service.createCluster(null);
    }

    @org.junit.Test
    public void queryCreaateClusterProgress() {
        ConfScalingTask currentTask = taskMapper.selectByPrimaryKey("62f2e66c-b7e1-11ed-bcf2-6045bdc792d8");
        ConfScalingTask scalingTask = taskMapper.queryRunningTask("007c56c4-3ac8-46c4-9c77-b8059fa07af3", "task", currentTask.getCreateTime(), "task-apifox7367",
                Arrays.asList(ConfScalingTask.SCALINGTASK_Complete));

        Console.log("currentTask-nextTask={}", currentTask.getCreateTime().getTime() - scalingTask.getCreateTime().getTime());
        Console.log("任务Id:{}", scalingTask.getTaskId());
    }
}