-- 向Shein工单系统提交的审批请求
create table if not exists work_order_approval_request
(
    id     bigint NOT NULL AUTO_INCREMENT comment 'ID' primary key,
    cluster_id    varchar(40)  null comment '集群ID',
    ticket_id      varchar(60)  null comment 'Shein工单系统生成的工单ID',
    request_type   varchar(48)  null comment '请求类型。CREATE：创建集群；DESTORY：销毁集群',
    approval_state  varchar(32)  null comment '审核结果。INIT：初始；AGREE：通过，REFUSE：驳回，BACK，REVOKE',
    approval_result  text      null comment '审核返回结果',
    createdby     varchar(60)  null comment '创建人',
    created_time  datetime     null comment '创建时间',
    modifiedby    varchar(60)  null comment '修改人',
    modified_time datetime     null comment '修改时间'
    ) comment '向Shein工单系统提交的审批请求' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 根据集群ID,查询集群的审批请求
call create_index( 'work_order_approval_request', 'idx_cluster_id', ' (cluster_id)' );

-- 根据工单系统回调的ticket_id,查询审批请求
call create_index( 'work_order_approval_request', 'idx_ticket_id', ' (ticket_id)' );