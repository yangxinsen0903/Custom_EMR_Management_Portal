
CREATE TABLE IF NOT EXISTS `api_auth_key`  (
`id` bigint AUTO_INCREMENT  NOT NULL COMMENT '自增主键',
`user_id` varchar(64)   NULL COMMENT '用户ID',
`name` VARCHAR(64) NOT NULL COMMENT '自定义名称',
`access_key` VARCHAR(255) NOT NULL UNIQUE COMMENT 'Access Key',
`secret_key` VARCHAR(255) NOT NULL COMMENT 'Secret Key',
`permission` VARCHAR(255) NOT NULL COMMENT '权限',
`expiration_date`  DATETIME   NOT NULL COMMENT '有效期',
`status` varchar(60)   NOT NULL COMMENT '状态',
`created_time` DATETIME DEFAULT NULL COMMENT '创建时间',
`createdby` varchar(60) DEFAULT NULL  COMMENT '创建人' ,
`modified_time` DATETIME DEFAULT NULL COMMENT '修改时间',
`modifiedby` varchar(60) DEFAULT NULL  COMMENT '修改人',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = 'AKSK表';
