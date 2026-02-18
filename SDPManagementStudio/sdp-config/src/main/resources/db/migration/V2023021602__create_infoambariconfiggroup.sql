CREATE TABLE if not exists info_ambari_config_group(
                                         `conf_id` VARCHAR(40) NOT NULL   COMMENT '配置ID' ,
                                         `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                         `group_id` VARCHAR(40)    COMMENT '实例组ID' ,
                                         `ambari_id` BIGINT    COMMENT 'ambari_id' ,
                                         `ambari_service_name` VARCHAR(40)    COMMENT 'ambari服务名称' ,
                                         `ambari_group_name` VARCHAR(40)    COMMENT 'ambari配置组名称' ,
                                         `ambari_tag` VARCHAR(60)    COMMENT 'ambaritag' ,
                                         `ambari_cluster_name` VARCHAR(60)    COMMENT 'ambari集群名称' ,
                                         `ambari_description` VARCHAR(200)    COMMENT '描述' ,
                                         `state` INT    COMMENT '状态' ,
                                         `created_time` DATETIME    COMMENT '创建时间' ,
                                         PRIMARY KEY (conf_id)
)  COMMENT = '集群实例组与Ambari服务配置组关系表';