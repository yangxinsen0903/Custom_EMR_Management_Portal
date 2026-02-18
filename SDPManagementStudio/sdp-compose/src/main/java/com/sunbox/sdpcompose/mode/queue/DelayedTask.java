package com.sunbox.sdpcompose.mode.queue;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author : [niyang]
 * @className : DelayedTask
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/9 5:51 PM]
 */
public class DelayedTask implements Delayed {

    /**
     * 延时时长(s)
     */
    private long delay;

    /**
     * 过期时间
     */
    private long expire;

    /**
     * 消息体
      */
    private String msgBody;

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 生产者客户端名称
     */
    private String clientName;

    /**
     *
     * @param delay 延迟时长 单位 s
     * @param msgBody 消息体内容
     * @param msgId 消息ID
     * @param clientName 发送消息的ClientName
     */
    public DelayedTask(long delay, String msgBody,String msgId,String clientName) {
        this.delay = delay;
        this.msgBody = msgBody;
        this.msgId=msgId;
        this.clientName=clientName;
        this.expire = System.currentTimeMillis() + delay*1000;
    }

    /**
     * 指定到期时间计算规则
     */
    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     *用于延迟队列内部进行排序，将最先到期的放在队首，保证take出来的是到期的那个
     * */
    @Override
    public int compareTo(@NotNull Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "DelayedTask{" +
                "delay=" + delay +
                ", expire=" + expire +
                ", msgBody='" + msgBody + '\'' +
                ", msgId='" + msgId + '\'' +
                '}';
    }
}
