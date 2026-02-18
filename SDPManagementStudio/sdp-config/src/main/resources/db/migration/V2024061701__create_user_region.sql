
 CREATE TABLE IF NOT EXISTS `base_user_region`  (
  `id`   bigint auto_increment NOT NULL COMMENT '自增主键',
  `user_id` varchar(64)    NOT NULL COMMENT '用户ID',
  `region` bigint(24)   NOT NULL COMMENT '数据中心ID, 如:westus3',
  PRIMARY KEY (`id` ) USING BTREE
) ENGINE = InnoDB   CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和数据中心关联表'  ;


CREATE TABLE IF NOT EXISTS `base_user_role`  (
  `id`   bigint auto_increment NOT NULL COMMENT '自增主键',
  `user_id` varchar(64)   NOT NULL COMMENT '用户ID',
  `role_name` varchar(48)    NOT NULL COMMENT '用户角色',
  `role_code` varchar(48)   NOT NULL COMMENT '角色编码, 普通人员:STAFF,运维人员:MAINTAINER,管理员:ADMINISTRATOR',
  PRIMARY KEY (`id` ) USING BTREE
) ENGINE = InnoDB   CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与角色表';
