
/* Create table in target */
CREATE TABLE IF NOT EXISTS sdpms.base_user_info(
                                               `user_id` varchar(60)   NOT NULL  COMMENT '用户ID' ,
                                               `user_name` varchar(60)   NOT NULL  COMMENT '账号' ,
                                               `real_name` varchar(60)   NULL  COMMENT '用户姓名' ,
                                               `dept_id` varchar(200)   NULL  COMMENT '部门ID' ,
                                               `dept_name` varchar(200)   NULL  COMMENT '部门名称' ,
                                               `password` varchar(60)   NOT NULL  COMMENT '密码' ,
                                               `created_time` datetime NULL  COMMENT '创建时间' ,
                                               `createdby` varchar(60)   NULL  COMMENT '创建人' ,
                                               `modified_time` datetime NULL  COMMENT '修改时间' ,
                                               `modifiedby` varchar(60)   NULL  COMMENT '修改人' ,
                                               PRIMARY KEY (`user_id`) ,
                                               UNIQUE KEY `user_name`(`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_general_ci' COMMENT='集群用户信息表';


replace INTO sdpms.base_user_info (user_id,user_name,real_name,dept_id,dept_name,password,created_time,createdby,modified_time,modifiedby) VALUES
    ('5772a355-ce6d-4d91-9331-e3bce19c52d6','sdpadmin','sdpadmin',NULL,NULL,'9b6daa66b84dc456f31011a6e44b5b97','2022-12-26 08:19:15',NULL,NULL,NULL);
