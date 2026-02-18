CREATE TABLE if not exists info_cluster_vm_req_job_failed(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键' ,
  `cluster_id` VARCHAR(40)    COMMENT '集群ID' ,
  `plan_id` VARCHAR(40)    COMMENT '任务id' ,
  `job_id` VARCHAR(200)    COMMENT '申请任务ID' ,
  `status` INT    COMMENT '状态;0 未处理  1  已处理' ,
  `created_time` DATETIME    COMMENT '创建时间' ,
  `modified_time` DATETIME    COMMENT '修改时间' ,
  PRIMARY KEY (id)
)  COMMENT = '失败扩容任务购买VM监控表';
