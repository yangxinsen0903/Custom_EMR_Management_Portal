package com.sunbox.domain.cluster;

import lombok.Data;

import java.util.List;

/**
 * 创建工单请求
 */
@Data
public class TicketCreateRequest {
    /**
     * 创建用户UID
     */
    private String cuser;
    private String description;
    /**
     * 是否快速执行
     */
    private Integer is_fast_mode;
    private Boolean is_fill_empty;
    private List<NodeOperator> node_operators;
    private List<Record>  records;
    private String title;
    /**
     * ulp, cmdb，staff
     */
    private String user_type;
    /**
     * 与workflow_name, 只需要填一个
     */
    private String workflow_identifier;
    private String workflow_name;


}
