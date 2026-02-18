CREATE TABLE if not exists `info_cluster_vm_index_history` (
    `cluster_id` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '集群ID',
    `vm_role` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例ID',
    `task_id` VARCHAR(40)    COMMENT '任务id' ,
    `before_index` INT DEFAULT NULL COMMENT '最后一次变化之前的数量',
    `after_index` INT DEFAULT NULL COMMENT '最后一次变化之后的数量',
    `delta_index` INT DEFAULT NULL COMMENT '最后一次变化的增量',
    `create_time` DATETIME COMMENT '创建时间' ,
    `modified_time` DATETIME COMMENT '修改时间' ,
    PRIMARY KEY (`cluster_id`, `vm_role`, `task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群的vm序号申请历史';