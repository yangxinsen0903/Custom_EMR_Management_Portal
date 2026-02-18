CREATE TABLE if not exists info_cluster_ambari_host_delete(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键' ,
    `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
    `ambari_cluster_name` VARCHAR(100)    COMMENT 'ambari集群名称' ,
    `host_name` VARCHAR(20)    COMMENT 'hostName带域名;' ,
    `ambari_server_ip` VARCHAR(200)    COMMENT 'ambariServer地址' ,
    `plan_id` VARCHAR(40)    COMMENT '任务ID' ,
    `status` INT    COMMENT '状态;0 未删除  3 删除完成' ,
    `retry_count` INT    COMMENT '重试次数' ,
    `created_time` DATETIME    COMMENT '创建时间' ,
    `modified_time` DATETIME    COMMENT '修改时间' ,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = 'ambariHost删除补偿表';