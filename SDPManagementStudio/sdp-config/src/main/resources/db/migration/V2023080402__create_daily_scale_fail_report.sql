CREATE TABLE if not exists daily_scale_fail_report(
    `id` VARCHAR(64) NOT NULL COMMENT '主键' ,
    `report_id` VARCHAR(64) NOT NULL  COMMENT 'report_id' ,
    `task_name` VARCHAR(128)    COMMENT 'taskName' ,
    `purchase_type` VARCHAR(64)    COMMENT 'purchaseType' ,
    `task_count` INT    COMMENT 'taskCount' ,
    `vm_count` INT    COMMENT 'vmCount' ,
    `cpu_count` INT    COMMENT 'cpuCount' ,
    `begin_time` DATETIME    COMMENT '创建时间' ,
    `end_time` DATETIME    COMMENT '创建时间' ,
    `report_date` DATETIME    COMMENT '创建时间' ,
    PRIMARY KEY (id)
)  COMMENT = 'SDP巡检任务失败报告查询';
