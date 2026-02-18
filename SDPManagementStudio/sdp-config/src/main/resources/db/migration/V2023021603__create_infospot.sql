
CREATE TABLE if not exists info_spot_group_scale_task(
                                           `task_id` VARCHAR(40) NOT NULL   COMMENT '任务id' ,
                                           `cluster_id` VARCHAR(40)    COMMENT '集群id' ,
                                           `group_id` VARCHAR(40)    COMMENT '实例组id' ,
                                           `scale_method` INT    COMMENT '缩放方式;1-扩容，2-缩容' ,
                                           `scale_count` INT    COMMENT '预期缩放数量' ,
                                           `actual_count` INT    COMMENT '实际缩放数量' ,
                                           `state` INT    COMMENT '状态;0未开始 1执行中 2 执行成功 3执行失败' ,
                                           `createdby` VARCHAR(255)    COMMENT '创建人' ,
                                           `created_time` DATETIME    COMMENT '创建时间' ,
                                           `modifiedby` VARCHAR(255)    COMMENT '修改人' ,
                                           `modified_time` DATETIME    COMMENT '修改时间' ,
                                           `beg_time` DATETIME    COMMENT '开始执行时间' ,
                                           `end_time` DATETIME    COMMENT '结束执行时间' ,
                                           PRIMARY KEY (task_id)
)  COMMENT = '竞价实例组的扩缩任务';

CREATE TABLE if not exists info_spot_group_scale_task_item(
                                                `item_id` VARCHAR(40) NOT NULL   COMMENT '项目id' ,
                                                `task_id` VARCHAR(40)    COMMENT '任务id' ,
                                                `cluster_id` VARCHAR(40)    COMMENT '集群id' ,
                                                `group_id` VARCHAR(40)    COMMENT '实例组id' ,
                                                `vm_name` VARCHAR(255)    COMMENT '实例名称' ,
                                                `scale_method` INT    COMMENT '缩放方式;1-扩容，2-缩容' ,
                                                `state` INT    COMMENT '状态;0未开始 1执行中 2 执行成功 3执行失败' ,
                                                `expected_time` DATETIME    COMMENT '计划执行时间;逐出时记录最晚要完成缩容的时间' ,
                                                `createdby` VARCHAR(255)    COMMENT '创建人' ,
                                                `created_time` DATETIME    COMMENT '创建时间' ,
                                                `modifiedby` VARCHAR(255)    COMMENT '修改人' ,
                                                `modified_time` DATETIME    COMMENT '修改时间' ,
                                                `beg_time` DATETIME    COMMENT '开始执行时间' ,
                                                `end_time` DATETIME    COMMENT '结束执行时间' ,
                                                PRIMARY KEY (item_id)
)  COMMENT = '竞价实例组扩缩容任务明细';