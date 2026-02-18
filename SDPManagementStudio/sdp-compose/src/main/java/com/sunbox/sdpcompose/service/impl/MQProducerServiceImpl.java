package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.sunbox.domain.InfoDelayMsg;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.mapper.InfoDelayMsgMapper;
import com.sunbox.sdpcompose.mode.queue.DelayedTask;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IMQProducerService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.concurrent.DelayQueue;

/**
 * @author : [niyang]
 * @className : MQProducerServiceImpl
 * @description : [发送消息]
 * @createTime : [2022/11/30 3:30 PM]
 */
@Service
public class MQProducerServiceImpl implements IMQProducerService,
        BaseCommonInterFace {

    @Value("${message.compose:servicebus}")
    private String messagecompose;

    @Autowired
    private InfoDelayMsgMapper delayMsgMapper;

    @Override
    public ResultMsg sendMessage(String clientname, String messagebody) {
        ResultMsg msg=new ResultMsg();
        try {
            if (messagecompose.equalsIgnoreCase("kafka")){
                try {
                    KafkaProducer client=ProducerCache.kafkaProducers.get(clientname);
                    String topicname=ProducerCache.kafkaClientTopic.get(clientname);
                    if(StringUtils.isEmpty(topicname)){
                        getLogger().error("topicName is null,");
                    }

                    String topic=ProducerCache.kafkaClientTopic.get(clientname);
                    ProducerRecord<String, String> record =
                            new ProducerRecord<String, String>(topic,messagebody);
                    client.send(record);
                    msg.setResult(true);
                } catch (Exception e) {
                    getLogger().error("kafka send message,",e);
                }
            }

            if (messagecompose.equalsIgnoreCase("servicebus")){
                ServiceBusSenderClient client = ProducerCache.producers.get(clientname);
                //region 带有退火重试机制消息发送,重试5次
                for (int i=0;i<6;i++){
                    boolean rsg=sendServiceBusMsg(client,messagebody);
                    if (rsg){
                        msg.setResult(true);
                        return msg;
                    }else{
                        int delay = (int) Math.pow(2, i);
                        getLogger().error("send scheduleMessage 异常，retry After(s)："+delay);
                        ThreadUtil.sleep(delay*1000L);
                        msg.setResult(false);
                    }
                }
                //endregion

            }
        }catch (Exception e){
            msg.setResult(false);
            getLogger().error("send message Exception:",e);
        }
        return msg;
    }

    @Override
    public ResultMsg sendScheduleMessage(String clientName, String messagebody, Long offsetSeconds) {
        ResultMsg msg=new ResultMsg();

        try {
            //region 使用servicebus作为消息队列
            if (messagecompose.equalsIgnoreCase("servicebus")) {
                ServiceBusSenderClient client = ProducerCache.producers.get(clientName);
                getLogger().info("send scheduleMessage：" + messagebody);
                //region 带有退火重试机制消息发送,重试5次
                for (int i=0;i<6;i++){
                    boolean rsg=sendServiceBusScheduleMsg(client,messagebody,offsetSeconds);
                    if (rsg){
                        msg.setResult(true);
                        return msg;
                    }else{
                        int delay = (int) Math.pow(2, i);
                        getLogger().error("send scheduleMessage 异常，retry After(s)："+delay);
                        ThreadUtil.sleep(delay*1000L);
                        msg.setResult(false);
                    }
                }
                //endregion
                msg.setResult(true);
            }
            //endregion 使用servicebus作为消息队列

            //region 使用kafka作为消息队列
            if (messagecompose.equalsIgnoreCase("kafka")){

                //region 消息数据落地

                InfoDelayMsg delayMsg=new InfoDelayMsg();
                delayMsg.setDelaySecond(offsetSeconds.intValue());
                delayMsg.setMsgContent(messagebody);
                delayMsg.setClientName(clientName);
                delayMsg.setCreatedTime(new Date());
                Date plandate=new Date(delayMsg.getCreatedTime().getTime()+offsetSeconds*1000);
                delayMsg.setPlanSendTime(plandate);
                delayMsg.setMsgState(InfoDelayMsg.MSGSTATE_NoSend);
                delayMsgMapper.insert(delayMsg);

                //endregion 消息数据落地

                //region 消息数据发送至内部Delayqueue

                DelayedTask task=new DelayedTask(offsetSeconds,
                        delayMsg.getMsgContent(),
                        delayMsg.getMsgId().toString(),
                        delayMsg.getClientName());

                if (ProducerCache.delayQueue==null){
                    ProducerCache.delayQueue=new DelayQueue<>();
                }
                ProducerCache.delayQueue.add(task);

                //endregion 消息数据发送至内部Delayqueue

            }
            //endregion 使用kafka作为消息队列

        }catch (Exception e){
            msg.setResult(false);
            getLogger().error("send scheduleMessage exception:",e);
        }
        return msg;
    }

    /**
     * 发送延时消息
     *
     * @param client
     * @param messagebody
     * @param offsetSeconds
     * @return
     */
    private boolean sendServiceBusScheduleMsg(ServiceBusSenderClient client,
                                      String messagebody,
                                      Long offsetSeconds){
        try {
            client.scheduleMessage(new ServiceBusMessage(messagebody),
                    OffsetDateTime.now().plusSeconds(offsetSeconds));
            return true;
        }catch (Exception e){
            getLogger().error("send scheduleMessage exception:",e);
            return false;
        }
    }

    /**
     * 发送普通消息
     *
     * @param client
     * @param messagebody
     * @return
     */
    private boolean sendServiceBusMsg(ServiceBusSenderClient client,
                                      String messagebody){
        try {
            client.sendMessage(new ServiceBusMessage(messagebody));
            getLogger().info("send message：" + messagebody);
            return true;
        }catch (Exception e){
            getLogger().error("send message Exception:",e);
            return false;
        }

    }

}
