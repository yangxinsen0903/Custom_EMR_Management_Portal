CREATE TABLE if not exists info_cluster_vm_delete(
        `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键' ,
        `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
        `vm_name` VARCHAR(100)    COMMENT 'vmName' ,
        `vm_role` VARCHAR(20)    COMMENT '虚拟机实例组类型;task/core' ,
        `priority` INT    COMMENT '优先级' ,
        `purchase_type` VARCHAR(20)    COMMENT '购买类型;od / spot' ,
        `plan_id` VARCHAR(40)    COMMENT '任务ID' ,
        `status` INT    COMMENT '状态;-1 冻结  0 未删除 1 删除请求发送中 2 删除中 3 删除完成' ,
        `job_id` VARCHAR(200)    COMMENT '删除VM的任务ID' ,
        `beg_send_request_time` DATETIME    COMMENT '发送删除请求的开始时间' ,
        `get_delete_jobId_time` DATETIME    COMMENT '完成删除请求得到删除jobId的时间' ,
        `retry_count` INT    COMMENT '重试次数' ,
        `release_freeze_time` DATETIME    COMMENT '解除冻结时间' ,
        `freeze_count` INT    COMMENT '被冻结次数' ,
        `created_time` DATETIME    COMMENT '创建时间' ,
        `modified_time` DATETIME    COMMENT '修改时间' ,
        PRIMARY KEY (id)
)  COMMENT = 'vm删除表日志表';