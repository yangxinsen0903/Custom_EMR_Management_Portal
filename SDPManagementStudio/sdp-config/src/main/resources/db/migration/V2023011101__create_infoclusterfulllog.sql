CREATE TABLE if not exists info_cluster_full_log(
                                       `log_id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '日志ID' ,
                                       `cluster_id` VARCHAR(32)    COMMENT '集群ID' ,
                                       `cluster_name` VARCHAR(255)    COMMENT '集群名称' ,
                                       `activity_log_id` VARCHAR(255)    COMMENT 'actionID' ,
                                       `action_name` VARCHAR(255)    COMMENT '操作名称' ,
                                       `plan_id` VARCHAR(255)    COMMENT '执行计划ID' ,
                                       `request_time` DATETIME    COMMENT '请求时间' ,
                                       `response_time` DATETIME    COMMENT '响应时间' ,
                                       `request_param` TEXT(255)    COMMENT '请求参数' ,
                                       `response_body` TEXT(255)    COMMENT '响应内容' ,
                                       PRIMARY KEY (log_id)
)  COMMENT = 'SDP内外接口交互日志表';