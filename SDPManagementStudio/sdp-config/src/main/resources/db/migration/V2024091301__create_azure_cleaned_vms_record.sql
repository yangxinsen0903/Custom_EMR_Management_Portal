-- azure清理vm记录表

create table if not exists azure_cleaned_vms_record
(
    id     bigint NOT NULL AUTO_INCREMENT comment 'ID' primary key,
    vm_name    varchar(100)  null comment '实例名称',
    host_name      varchar(200)  null comment '机器名称',
    unique_id   varchar(100)  null comment '唯一id',
    private_ip  varchar(100)  null comment 'private ip',
    zone  varchar(100)      null comment '可用区',
    priority     varchar(40) null comment '优先级',
    vm_size     varchar(60)  null comment 'vmsize',
    cluster_id     varchar(40)  null comment '集群id',
    cluster_name     varchar(100)  null comment '集群名称',
    vm_role     varchar(40)  null comment '伸缩实例组角色',
    group_id     varchar(40)  null comment '实例组ID',
    group_name     varchar(60)  null comment '实例组名称',
    vm_created_time    datetime  null comment 'vm创建时间',
    createdby     varchar(60)  null comment '创建人',
    created_time  datetime     null comment '创建时间',
    modifiedby    varchar(60)  null comment '修改人',
    modified_time datetime     null comment '修改时间'
    ) comment 'azure清理vm记录表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

call create_index('azure_cleaned_vms_record', 'idx_cluster_id', ' (cluster_id)' );
