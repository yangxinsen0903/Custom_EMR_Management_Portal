CREATE TABLE IF NOT EXISTS vm_events (
     id bigint NOT NULL COMMENT 'ID, 不自增',
     cluster_id varchar(48) NOT NULL COMMENT '集群ID',
     cluster_name varchar(128) NOT NULL COMMENT '集群名称',
     group_name varchar(48) NULL COMMENT '实例组名',
     vm_name varchar(128) NULL COMMENT 'vm名称',
     host_name varchar(128) NULL COMMENT 'vm的HostName',
     vm_id varchar(48) NULL COMMENT 'vmid',
     purchase_type varchar(32) NULL COMMENT '购买类型: Spot 或者 OnDemand',
     event_type varchar(32) NOT NULL COMMENT '事件类型:ONLINE(上线), OFFLINE(下线)',
     trigger_time datetime NOT NULL COMMENT '事件触发时间',
     remark varchar(4096) NULL COMMENT '备注说明',
     state varchar(32) NOT NULL COMMENT 'INIT(初始化), PROCESSING(处理中), SUCCESS(成功), FAIL(失败)',
     finish_time datetime NULL COMMENT '处理完成时间',
     CONSTRAINT vm_events_pk PRIMARY KEY (id)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_general_ci
    COMMENT='VM上下线事件表';

call create_index( 'vm_events', 'idx_trigger_time', ' (trigger_time)' );
call create_index( 'vm_events', 'idx_vm_name', ' (vm_name)' );
