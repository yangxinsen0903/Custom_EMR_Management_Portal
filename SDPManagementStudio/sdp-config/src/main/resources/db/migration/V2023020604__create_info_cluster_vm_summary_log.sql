
CREATE TABLE if not exists info_cluster_vm_summary_log(
                                            `cid` VARCHAR(40) NOT NULL   COMMENT '变更ID' ,
                                            `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
                                            `group_id` VARCHAR(40)    COMMENT '实例组ID' ,
                                            `change_reason` VARCHAR(50)    COMMENT '变更原因' ,
                                            `change_count` INT    COMMENT '变更数量' ,
                                            `before_change_count` INT    COMMENT '变更前数量' ,
                                            `after_change_count` INT    COMMENT '变更后数量' ,
                                            `create_time` DATETIME    COMMENT '变更时间' ,
                                            PRIMARY KEY (cid)
)  COMMENT = '集群VM摘要变更日志';

