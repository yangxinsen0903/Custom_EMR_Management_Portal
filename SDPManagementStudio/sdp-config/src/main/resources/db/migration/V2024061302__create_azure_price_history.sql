CREATE TABLE IF NOT EXISTS `azure_price_history` (
                                     id bigint auto_increment NOT NULL COMMENT '自增ID',
                                     execute_time datetime NOT NULL COMMENT '从Azure获取数据的执行时间',
                                     vm_sku_name varchar(128) NOT NULL COMMENT 'VM SKU Name',
                                     region varchar(32) NOT NULL COMMENT 'Region',
                                     spot_unit_price DECIMAL NOT NULL COMMENT '竞价实例价格',
                                     ondemand_unit_price DECIMAL NOT NULL COMMENT '按需实例价格',
                                     eviction_rate_lower DECIMAL NOT NULL COMMENT '最低驱逐率',
                                     eviction_rate_upper DECIMAL NOT NULL COMMENT '最高驱逐率',
                                     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Azure历史价格表,包括:价格,逐出率';