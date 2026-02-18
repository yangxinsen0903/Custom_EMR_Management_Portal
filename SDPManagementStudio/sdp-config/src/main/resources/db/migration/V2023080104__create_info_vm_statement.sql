CREATE TABLE if not exists info_vm_statement(
    `statement_id` VARCHAR(64)    COMMENT '订单编号' ,
    `status` VARCHAR(32)    COMMENT '状态; SYNCING同步数据中 COMPARING对比中 FINISHED对比完成 FAILURE对比失败' ,
    `created_time` DATETIME    COMMENT '创建时间' ,
    `modified_time` DATETIME    COMMENT '修改时间' ,
    PRIMARY KEY (statement_id)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '对账单主体';