package com.sunbox.domain.cluster;

/**
 * 创建工单响应
 */
@lombok.Data
public class TicketCreateResponse {
    private String code;
    private Data data;
    private String message;

}
