CREATE TABLE if not exists conf_group_elastic_scaling(
                                           `group_es_id` VARCHAR(40) NOT NULL   COMMENT '实例组弹性配置ID' ,
                                           `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                           `group_name` VARCHAR(30)    COMMENT '实例组名称' ,
                                           `vm_role` VARCHAR(30)    COMMENT '实例角色' ,
                                           `max_count` INT    COMMENT '实例组最大实例数' ,
                                           `min_count` INT    COMMENT '实例组最小实例数' ,
                                           `scaling_limit_time` DATETIME    COMMENT '弹性伸缩时间限制' ,
                                           `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                           `created_time` DATETIME    COMMENT '创建时间' ,
                                           `modifiedby` VARCHAR(60)    COMMENT '修改人' ,
                                           `modified_time` DATETIME    COMMENT '修改时间' ,
                                           PRIMARY KEY (group_es_id)
)  COMMENT = '实例组弹性伸缩配置;2.0版本新增';
