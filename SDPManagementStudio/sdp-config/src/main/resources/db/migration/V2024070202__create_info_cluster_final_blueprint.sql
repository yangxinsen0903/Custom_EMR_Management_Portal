create table if not exists info_cluster_final_blueprint
(
    cluster_id        varchar(40)  not null comment '集群ID' primary key,
    cluster_name      varchar(200) null comment '集群名称',
    ambari_host       varchar(100) null comment 'ambari地址',
    blueprint_content longtext     null comment 'Blueprint内容',
    create_time       datetime     null comment '创建时间'
) ENGINE=InnoDB comment '销毁集群配置表' DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ;