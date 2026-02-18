CREATE TABLE if not exists conf_scaling_task(
                                  `task_id` VARCHAR(40) NOT NULL   COMMENT '伸缩任务ID' ,
                                  `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                  `group_name` VARCHAR(30)    COMMENT '伸缩实例组名称' ,
                                  `scaling_type` INT    COMMENT '伸缩类型;1 扩容 2 缩容' ,
                                  `vm_role` VARCHAR(30)    COMMENT '伸缩实例组角色' ,
                                  `es_rule_id` VARCHAR(40)    COMMENT '弹性伸缩规则ID' ,
                                  `es_rule_name` VARCHAR(200)    COMMENT '弹性伸缩规则名称' ,
                                  `before_scaling_count` INT    COMMENT '伸缩前数量' ,
                                  `after_scaling_count` INT    COMMENT '伸缩后数量' ,
                                  `scaling_count` INT    COMMENT '伸缩数量' ,
                                  `is_graceful_scalein` INT    COMMENT '是否优雅缩容;1 是 0 否' ,
                                  `scalein_waitingtime` INT    COMMENT '优雅缩容等待时间（S）;60 ～1800' ,
                                  `operatiion_type` INT    COMMENT '操作方式;1 用户主动  2 弹性伸缩  3 定时计划触发' ,
                                  `beg_time` DATETIME    COMMENT '开始时间' ,
                                  `end_time` DATETIME    COMMENT '完成时间' ,
                                  `state` INT    COMMENT '任务状态;1 任务' ,
                                  PRIMARY KEY (task_id)
)  COMMENT = '伸缩任务表;2.0版本新增';