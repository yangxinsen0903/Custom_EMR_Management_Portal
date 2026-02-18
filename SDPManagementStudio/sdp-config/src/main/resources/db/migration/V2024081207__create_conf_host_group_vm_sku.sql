create table if not exists  conf_host_group_vm_sku
(
    vm_sku_id     varchar(40)  not null comment '实例组SKU ID'
    primary key,
    cluster_id    varchar(40)  null comment '集群ID',
    group_id      varchar(40)  null comment '实例组ID,conf_cluster_host_group表主键',
    vm_conf_id    varchar(40)  null comment '实例组配置ID,conf_cluster_vm表主键',
    group_name    varchar(60)  null comment '实例组名称',
    vm_role       varchar(30)  null comment '实例角色;Master Core Task',
    sku           varchar(50)  null comment '实例规格',
    cpu_type      varchar(32)  null comment 'CPU类型:AMD64或Intel',
    vcpus         varchar(255) null comment 'CPU核数',
    memory        varchar(255) null comment '内存大小（GB）',
    purchase_type int          null comment '购买类型;1 按需  2 竞价',
    createdby     varchar(60)  null comment '创建人',
    created_time  datetime     null comment '创建时间',
    modifiedby    varchar(60)  null comment '修改人',
    modified_time datetime     null comment '修改时间'
    )
    comment '实例组SKU表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;