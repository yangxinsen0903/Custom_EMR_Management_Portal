
CREATE TABLE if not exists `info_cluster_vm_reject` (
                                          `reject_id` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '剔除ID',
                                          `cluster_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '集群ID',
                                          `vm_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '实例ID',
                                          `host_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'hostName',
                                          `internalIp` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内网Ip',
                                          `activity_log_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '执行步骤ID',
                                          `activity_cn_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '步骤名称',
                                          `reject_reason` text COLLATE utf8mb4_general_ci COMMENT '剔除原因',
                                          `created_time` datetime DEFAULT NULL COMMENT '创建时间',
                                          `destroy_time` datetime DEFAULT NULL COMMENT '销毁时间',
                                          PRIMARY KEY (`reject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='安装或扩容时被剔除集群的VM';
