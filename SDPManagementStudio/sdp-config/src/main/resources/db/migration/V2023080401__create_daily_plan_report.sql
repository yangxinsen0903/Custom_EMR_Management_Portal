CREATE TABLE if not exists daily_plan_report(
    `id` VARCHAR(64) NOT NULL COMMENT '主键' ,
    `report_id` VARCHAR(64) NOT NULL  COMMENT 'report_id' ,
    `operation_name` VARCHAR(128)    COMMENT 'operation name' ,
    `success_count` INT    COMMENT 'success_count' ,
    `failure_count` INT    COMMENT 'failure_count' ,
    `timeout_count` INT    COMMENT 'timeout_count' ,
    `total_count` INT    COMMENT 'total_count' ,
    `success_rate` double    COMMENT 'success_rate' ,
    `begin_time` DATETIME    COMMENT '创建时间' ,
    `end_time` DATETIME    COMMENT '创建时间' ,
    `report_date` DATETIME    COMMENT '创建时间' ,
    PRIMARY KEY (id)
)  COMMENT = 'SDP巡检计划执行情况报告查询';
