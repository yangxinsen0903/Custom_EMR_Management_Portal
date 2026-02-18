package com.sunbox.sdpcompose.listener;

import com.sunbox.domain.InfoDelayMsg;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.mapper.InfoDelayMsgMapper;
import com.sunbox.sdpcompose.mode.queue.DelayedTask;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.service.IMQProducerService;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.web.BaseCommonInterFace;

import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * @author : [niyang]
 * @className : DelayedMsgProcess
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/10 1:57 PM]
 */
public class DelayedMsgProcess implements Runnable, BaseCommonInterFace {

    private InfoDelayMsgMapper delayMsgMapper= SpringContextUtil.getBean(InfoDelayMsgMapper.class);

    private IMQProducerService imqProducerService=SpringContextUtil.getBean(IMQProducerService.class);


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

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
