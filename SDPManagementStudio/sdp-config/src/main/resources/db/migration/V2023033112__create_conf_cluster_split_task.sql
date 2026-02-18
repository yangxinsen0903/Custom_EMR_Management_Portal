CREATE TABLE if not exists `conf_cluster_split_task` (
    `id` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `subject` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题',
    `cluster_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '集群ID',
    `vm_role` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '实例ID',
    `group_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '集群名称',
    `scaling_out_count` INT DEFAULT NULL COMMENT '扩容数量',
    `expect_count` INT DEFAULT NULL COMMENT '期望数量',
    `task_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务id',
    `task_type` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务类型',
    `create_time` DATETIME COMMENT '创建时间' ,
    `modified_time` DATETIME COMMENT '修改时间' ,
    `state` INT DEFAULT NULL COMMENT '状态',
    `remark` VARCHAR(255) DEFAULT NULL   COMMENT '备注' ,
    `sort_index` INT DEFAULT NULL COMMENT '排序号',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='创建集群的拆分任务';