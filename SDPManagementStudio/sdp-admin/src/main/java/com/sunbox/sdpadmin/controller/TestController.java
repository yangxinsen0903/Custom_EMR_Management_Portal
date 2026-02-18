package com.sunbox.sdpadmin.controller;

import cn.hutool.core.util.IdUtil;
import com.sunbox.domain.SystemEvent;
import com.sunbox.domain.TaskEvent;
import com.sunbox.domain.cluster.Data;
import com.sunbox.domain.cluster.TicketCreateRequest;
import com.sunbox.domain.cluster.TicketCreateResponse;
import com.sunbox.domain.enums.SystemEventType;
import com.sunbox.domain.enums.TaskEventType;
import com.sunbox.sdpadmin.service.AutoStartClusterComponentService;
import com.sunbox.service.ISystemEventService;
import com.sunbox.service.ITaskEventService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.KeyVaultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : [niyang]
 * @className : TestController
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/8 10:52 PM]
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private AutoStartClusterComponentService autoStartClusterComponentService;

    @Autowired
    private ISystemEventService systemEventService;

    @Autowired
    private ITaskEventService taskEventService;

    @PostMapping("/kv")
    public String createCluster(String endpoint) {
        keyVaultUtil.setSecret("ambari-db-user-cl-9wNwZ1FSmqHH","ambari-db-user_cl-9wNwZ1FSmqHH",endpoint);
        return keyVaultUtil.getSecretVal("ambari-db-user-cl-9wNwZ1FSmqHH",endpoint);
    }

    @GetMapping("/systemEvent")
    public String systemEvent() {
        SystemEvent event = SystemEvent.build()
                        .setEventType(SystemEventType.REBOOT.name())
                        .setEventDesc("系统重启")
                                .setEventTriggerTime(new Date());
        systemEventService.saveSystemEvent(event);
        return "success";
    }

    @GetMapping("/taskEvent")
    public String taskEvent() {

        TaskEvent taskEvent = TaskEvent.build()
                .setEventType(TaskEventType.ELASTIC_SCALEIN_TIMEOUT.name())
                .setEventTriggerTime(new Date())
                .setClusterId("clusterId")
                .setClusterName("clusterName")
                .setVmRole("vmRole")
                .setGroupName("groupName")
                .setPlanId("planId")
                .setPlanName("planName")
                .setPlanActivityLogId("planActivityLogId")
                .setPlanActivityLogName("planActivityLogName")
                .setEventDesc("eventDesc");

        taskEventService.saveTaskEvent(taskEvent);
        return "success";
    }

    @GetMapping("/autostart")
    public String autoStartVmComponent() {
        autoStartClusterComponentService.autoStartClusterComponents();
        return "success";
    }

    @RequestMapping("/lock")
    public Boolean lock() {
        Boolean lock=redisLock.tryLock("test11111");
        if (lock) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return lock;
    }


    @RequestMapping("/locktime")
    public Boolean lock(long leaseTime) {
        Boolean lock=redisLock.tryLock("test11112", TimeUnit.SECONDS,0,leaseTime);
        if (lock) {
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e){
                System.out.println(e);
            }
        }
        return lock;
    }

    @PostMapping("/api/v1/taskpower/ticket/create/")
    public TicketCreateResponse lock222(@RequestBody TicketCreateRequest request) {
        String ticketId=IdUtil.getSnowflakeNextIdStr();
        TicketCreateResponse ticketCreateResponse = new TicketCreateResponse();
        ticketCreateResponse.setCode("200");
        Data data = new Data();
        data.setTicketid(ticketId);
        ticketCreateResponse.setData(data);
        return  ticketCreateResponse;
        //return ticketCreateResponse;
    }

}
