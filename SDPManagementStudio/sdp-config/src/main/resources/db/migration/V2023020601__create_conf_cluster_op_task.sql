
CREATE TABLE if not exists conf_cluster_op_task(
                                     `task_id` VARCHAR(40) NOT NULL   COMMENT '操作任务ID' ,
                                     `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                     `service_name` VARCHAR(50)    COMMENT '大数据服务' ,
                                     `opreation_type` VARCHAR(40)    COMMENT '操作类型' ,
                                     `state` INT    COMMENT '状态' ,
                                     `param_info` VARCHAR(1000)    COMMENT '参数' ,
                                     `begin_time` DATETIME    COMMENT '任务开始时间' ,
                                     `end_time` DATETIME    COMMENT '任务结束时间' ,
                                     `create_time` DATETIME    COMMENT '任务创建时间' ,
                                     PRIMARY KEY (task_id)
)  COMMENT = '集群操作任务;集群操作任务：包含大数据服务重启、停止、启动。';
