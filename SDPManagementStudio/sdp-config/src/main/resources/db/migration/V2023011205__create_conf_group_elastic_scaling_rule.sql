CREATE TABLE if not exists conf_group_elastic_scaling_rule(
                                                `es_rule_id` VARCHAR(40) NOT NULL   COMMENT '弹性伸缩规则ID' ,
                                                `group_es_id` VARCHAR(40)    COMMENT '实例组弹性配置ID' ,
                                                `es_rule_name` VARCHAR(200)    COMMENT '规则名称' ,
                                                `scaling_type` INT    COMMENT '伸缩类型' ,
                                                `per_saling_cout` INT    COMMENT '单次伸缩数量' ,
                                                `load_metric` VARCHAR(50)    COMMENT '负载指标' ,
                                                `window_size` INT    COMMENT '统计时间窗口大小（分钟）' ,
                                                `aggregate_type` VARCHAR(20)    COMMENT '聚合类型;max min avg' ,
                                                `operator` VARCHAR(10)    COMMENT '运算符;>=,  > ,<=, <' ,
                                                `threshold` double    COMMENT '阈值' ,
                                                `repeat_count` INT    COMMENT '重复次数触发弹性伸缩任务' ,
                                                `freezing_time` INT    COMMENT '冷却时间（s)' ,
                                                `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                                `created_time` DATETIME    COMMENT '创建时间' ,
                                                `modifiedby` VARCHAR(60)    COMMENT '修改人' ,
                                                `modified_time` DATETIME    COMMENT '修改时间' ,
                                                PRIMARY KEY (es_rule_id)
)  COMMENT = '实例组弹性伸缩规则配置;2.0版本新增';
