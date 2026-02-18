package com.sunbox.sdpcompose.configuration;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoDelayMsg;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.mapper.InfoDelayMsgMapper;
import com.sunbox.sdpcompose.mode.queue.DelayedTask;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IMQProducerService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * @author : [niyang]
 * @className : DelayQueueConfiguration
 * @description : [本地延时消息队列处理器，原理：使用java delayQueue实现延时生产kafka消息，为保证
 * 消息的可靠性，延时消息会落地到数据表中。]
 * @createTime : [2023/1/9 6:30 PM]
 */
public class DelayQueueConfiguration implements BaseCommonInterFace {

    @Value("${message.compose:servicebus}")
    private String messagecompose;

    @Autowired
    private IMQProducerService imqProducerService;

    @Autowired
    private InfoDelayMsgMapper delayMsgMapper;

    @Bean
    public boolean processQueue(){
        if (!messagecompose.equalsIgnoreCase("kafka")){
            return false;
        }

        //region load lost message and insert delayedQueue
        List<InfoDelayMsg> delayMsgList=delayMsgMapper.selectLost();
        if (ProducerCache.delayQueue==null){
            ProducerCache.delayQueue=new DelayQueue<>();
        }
        delayMsgList.stream().forEach(x->{
            DelayedTask task=new DelayedTask(x.getDelaySecond(),
                    x.getMsgContent(),
                    x.getMsgId().toString(),
                    x.getClientName());

            ProducerCache.delayQueue.add(task);
        });
        //endregion load last message and insert delayedQueue

        //region 轮询队列取出到期的消息
        while (true){
            if (null!=ProducerCache.delayQueue
                    && !ProducerCache.delayQueue.isEmpty()){
                try {
                  DelayedTask delayedTask = ProducerCache.delayQueue.take();
                  sendDelayMessage(delayedTask);
                }catch (Exception e){
                    getLogger().error("发送延时生产消息异常，",e);
                }
            }
            try {
                Thread.sleep(10l);
            }catch (Exception e){
                getLogger().error("发送延时生产消息异常，",e);
            }
        }
        // endregion
    }


    /**
     * 发送延时生产的消息
     * @param delayedTask
     * @return
     */
    private ResultMsg sendDelayMessage(DelayedTask delayedTask){
        ResultMsg msg=new ResultMsg();
        try {
            ResultMsg resultMsg = imqProducerService.sendMessage(delayedTask.getClientName(), delayedTask.getMsgBody());

            if (resultMsg.getResult()) {
                InfoDelayMsg delayMsg = delayMsgMapper.selectByPrimaryKey(Long.parseLong(delayedTask.getMsgId()));
                if (delayMsg != null) {
                    delayMsg.setMsgState(InfoDelayMsg.MSGSTATE_Send);
                    delayMsg.setSendTime(new Date());
                    delayMsgMapper.updateByPrimaryKey(delayMsg);
                }
            }
            msg.setResult(true);
        }catch (Exception e){
            getLogger().error("发送延时生产消息异常，",e);
            msg.setResult(false);
        }
        return msg;
    }

}
