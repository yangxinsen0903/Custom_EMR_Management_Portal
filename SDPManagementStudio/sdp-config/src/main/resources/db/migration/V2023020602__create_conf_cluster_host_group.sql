
CREATE TABLE if not exists conf_cluster_host_group(
                                        `group_id` VARCHAR(40) NOT NULL   COMMENT '实例组ID' ,
                                        `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                        `group_name` VARCHAR(50)    COMMENT '实例组名称' ,
                                        `vm_role` VARCHAR(50)    COMMENT '实例组角色' ,
                                        `ins_count` INT    COMMENT '实例数量' ,
                                        `state` INT    COMMENT '状态;0 停止 1运行中 2 扩容中 3 缩容中' ,
                                        `yarn_queue` VARCHAR(255)    COMMENT 'yarn队列名称' ,
                                        `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                        `created_time` DATETIME    COMMENT '创建时间' ,
                                        `modifiedby` VARCHAR(60)    COMMENT '修改人' ,
                                        `modified_time` DATETIME    COMMENT '修改时间' ,
                                        PRIMARY KEY (group_id)
)  COMMENT = '集群实例组';
