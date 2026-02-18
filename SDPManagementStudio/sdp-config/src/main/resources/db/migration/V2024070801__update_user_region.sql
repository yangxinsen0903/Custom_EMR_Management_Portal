
ALTER TABLE base_user_region MODIFY COLUMN region varchar(64)  NOT NULL COMMENT '数据中心ID, 如:westus3';
-- 表连接时的字符集问题
alter table base_user_info convert to character set utf8mb4 collate utf8mb4_general_ci ;
alter table base_user_role convert to character set utf8mb4 collate utf8mb4_general_ci ;
alter table base_user_region convert to character set utf8mb4 collate utf8mb4_general_ci ;
