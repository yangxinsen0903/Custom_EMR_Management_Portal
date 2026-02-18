
CREATE TABLE if not exists conf_cluster_host_group_apps_config(
                                                    `app_config_item_id` VARCHAR(255) NOT NULL   COMMENT '配置项ID' ,
                                                    `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                                    `group_id` VARCHAR(255)    COMMENT '实例组ID' ,
                                                    `app_config_classification` VARCHAR(20)    COMMENT '配置分类' ,
                                                    `app_name` VARCHAR(40)    COMMENT '组件名称' ,
                                                    `config_item` VARCHAR(300)    COMMENT '配置项' ,
                                                    `config_val` TEXT    COMMENT '配置项value' ,
                                                    `is_delete` INT    COMMENT '是否删除;0 无效 1 有效' ,
                                                    `created_time` DATETIME    COMMENT '创建时间' ,
                                                    `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                                    PRIMARY KEY (app_config_item_id)
)  COMMENT = '集群实例组配置项明细表;覆盖 conf_cluster_apps_config的配置。';
