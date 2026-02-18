CREATE TABLE if not exists info_cluster_vm_activity_log(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键' ,
    `cluster_id` VARCHAR(36)    COMMENT '集群ID' ,
    `vm_name` VARCHAR(200)    COMMENT 'vmName' ,
    `plan_id` VARCHAR(36)    COMMENT '任务ID' ,
    `activity_log_id` VARCHAR(36)    COMMENT '步骤ID' ,
    `activity_type` INT    COMMENT '活动类型;11 发起VM申请 12 完成VM申请 21 VM被降级 41 发起销毁VM申请 42 完成VM销毁' ,
    `created_time` DATETIME    COMMENT '创建时间' ,
    `remark` TEXT    COMMENT '备注字段' ,
    PRIMARY KEY (id)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = 'VM活动审计表';