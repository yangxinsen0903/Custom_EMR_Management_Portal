package com.sunbox.domain;

import java.util.Date;

public class InfoDelayMsg {
    private Long msgId;

    private Integer delaySecond;

    private String clientName;

    private Date planSendTime;

    private Date sendTime;

    private Integer msgState;

    private Date createdTime;

    private String msgContent;


    public static final Integer MSGSTATE_NoSend=0;

    public static final Integer MSGSTATE_Send=1;

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Integer getDelaySecond() {
        return delaySecond;
    }

    public void setDelaySecond(Integer delaySecond) {
        this.delaySecond = delaySecond;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName == null ? null : clientName.trim();
    }

    public Date getPlanSendTime() {
        return planSendTime;
    }

    public void setPlanSendTime(Date planSendTime) {
        this.planSendTime = planSendTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getMsgState() {
        return msgState;
    }

    public void setMsgState(Integer msgState) {
        this.msgState = msgState;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent == null ? null : msgContent.trim();
    }
}