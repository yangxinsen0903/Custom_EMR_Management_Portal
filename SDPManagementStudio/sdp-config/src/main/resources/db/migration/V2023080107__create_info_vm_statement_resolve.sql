CREATE TABLE if not exists info_vm_statement_resolve(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键' ,
    `statement_id` VARCHAR(64)    COMMENT '对账单编号' ,
    `statement_result_id` VARCHAR(64)    COMMENT '对账单结果id' ,
    `vm_source` VARCHAR(64)    COMMENT '来源AZURE SDP YARN' ,
    `resolved_result` VARCHAR(32)    COMMENT '处理结果 DELETED FAILURE' ,
    `resolved_remark` VARCHAR(500)    COMMENT '处理结果备注' ,
    `resolved_begin_time` DATETIME    COMMENT '处理开始时间' ,
    `resolved_end_time` DATETIME    COMMENT '处理结束时间' ,
    PRIMARY KEY (id)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '对账处理结果';