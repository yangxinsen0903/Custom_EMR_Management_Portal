package com.sunbox.sdpcompose.listener;

import cn.hutool.core.thread.ThreadUtil;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.sunbox.dao.mapper.SystemEventMapper;
import com.sunbox.domain.SystemEvent;
import com.sunbox.domain.enums.SystemEventType;
import com.sunbox.runtime.RuntimeGlobal;
import com.sunbox.sdpcompose.configuration.AsyncConfig;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.task.UpLoadLogTask;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;


@Component
public class CloseListener implements ApplicationListener<ContextClosedEvent>, BaseCommonInterFace {

    @Autowired
    private AsyncConfig asyncConfig;

    @Autowired
    private UpLoadLogTask upLoadLogTask;

    @Autowired
    private SystemEventMapper systemEventMapper;

    @Autowired
    private ScheduledAnnotationBeanPostProcessor postProcessor;


    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        /*getLogger().info("RuntimeManager，应用关闭逻辑处理开始");
        getLogger().info("RuntimeManager，关闭消费者开始。");
        SystemEvent systemEvent = SystemEvent.build()
                .setEventTriggerTime(new Date())
                .setEventType(SystemEventType.CLOSE.name())
                .setEventDesc("服务[sdp-compose]收到关闭事件");
        systemEventMapper.insert(systemEvent);

        Iterator<Map.Entry<String, ServiceBusProcessorClient>> it =
                ProducerCache.consumers.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, ServiceBusProcessorClient> entry = it.next();
            ServiceBusProcessorClient client = entry.getValue();
            client.stop();
            client.close();
        }
        getLogger().info("RuntimeManager，关闭消息消费者结束。");

        //region 等待所有消息处理线程退出
        ThreadPoolTaskExecutor executor =(ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();
        Integer cnt = 0;

        //region 上传当前日志到blob
        upLoadLogTask.upLoadCurrentLogToBlob();
        //endregion 上传当前日志到blob

        while(executor.getActiveCount()>0){
           getLogger().info("RuntimeManager，存在未完成的消息处理线程，等待退出,线程数：{}",executor.getActiveCount());
            ThreadUtil.sleep(1000*10L);
            cnt++;
            if (cnt >20){
                getLogger().warn("等待时间超时。");
                break;
            }
        }
        getLogger().info("RuntimeManager，消息处理当前线程数：{}",executor.getActiveCount());
        //endregion 等待所有消息处理线程退出
        getLogger().info("RuntimeManager，应用关闭逻辑处理结束");
        RuntimeGlobal.ThirdBlocked=false;*/
    }


}
