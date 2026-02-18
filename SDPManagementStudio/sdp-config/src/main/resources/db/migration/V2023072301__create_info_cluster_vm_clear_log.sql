CREATE TABLE if not exists info_cluster_vm_clear_log(
    `ID` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键' ,
    `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
    `plan_id` VARCHAR(40)    COMMENT '任务ID' ,
    `vm_name` VARCHAR(100)    COMMENT 'vmName' ,
    `job_id` VARCHAR(200)    COMMENT '删除VM的任务ID' ,
    `status` INT    COMMENT '状态;0 未删除 1 删除请求发送 2 删除中 3 删除完成' ,
    `created_time` DATETIME    COMMENT '创建时间' ,
    `modified_time` DATETIME    COMMENT '修改时间' ,
    PRIMARY KEY (ID)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = 'VM清理日志';