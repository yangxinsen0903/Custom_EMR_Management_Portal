CREATE TABLE if not exists info_group_elastic_scaling_rule_log(
                                                    `es_rule_log_id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '规则日志ID' ,
                                                    `es_rule_id` VARCHAR(40)    COMMENT '规则ID' ,
                                                    `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                                    `load_metric` VARCHAR(50)    COMMENT '负载指标' ,
                                                    `aggregate_type` VARCHAR(20)    COMMENT '聚合类型' ,
                                                    `operator` VARCHAR(20)    COMMENT '运算符' ,
                                                    `threshold` double   COMMENT '阈值' ,
                                                    `metric_val` double   COMMENT '指标值' ,
                                                    `is_start_scaling` INT    COMMENT '是否启动弹性伸缩' ,
                                                    `created_time` DATETIME    COMMENT '创建时间' ,
                                                    PRIMARY KEY (es_rule_log_id)
)  COMMENT = '弹性伸缩规则条件触发日志表;2.0版本新增';
