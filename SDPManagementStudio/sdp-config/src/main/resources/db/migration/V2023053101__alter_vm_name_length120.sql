ALTER TABLE conf_scaling_vm MODIFY COLUMN vm_name varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称';
ALTER TABLE info_cluster_vm_reject MODIFY COLUMN vm_name varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '实例ID';
ALTER TABLE info_cluster_vm MODIFY COLUMN vm_name varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称';
