CREATE TABLE if not exists info_delay_msg(
                               `msg_id` bigint NOT NULL AUTO_INCREMENT  COMMENT '消息ID' ,
                               `delay_second` INT    COMMENT '延时时间（s)' ,
                               `plan_send_time` DATETIME    COMMENT '计划发送时间' ,
                               `send_time` DATETIME    COMMENT '发送时间' ,
                               `msg_content` TEXT    COMMENT '消息体内容' ,
                               `msg_state` INT    COMMENT '消息状态;0 未发送 1 已发送' ,
                               `created_time` DATETIME    COMMENT '创建时间' ,
                               PRIMARY KEY (msg_id)
)  COMMENT = '延时消息记录表';