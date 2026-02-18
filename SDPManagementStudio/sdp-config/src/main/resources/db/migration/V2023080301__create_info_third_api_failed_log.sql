CREATE TABLE if not exists info_third_api_failed_log(
 `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT 'id' ,
 `api_name` VARCHAR(200)    COMMENT '接口名称' ,
 `api_url` VARCHAR(200)    COMMENT '接口url' ,
 `api_key_param` TEXT   COMMENT '关键参数' ,
 `exception_info` mediumtext    COMMENT '异常信息' ,
 `failed_type` INT    COMMENT '失败类型;1 超时 2 异常' ,
 `time_out` INT    COMMENT '超时设置时间（秒）' ,
 `created_time` DATETIME    COMMENT '创建时间' ,
 PRIMARY KEY (id)
)  COMMENT = '调用外部接口失败日志表';
