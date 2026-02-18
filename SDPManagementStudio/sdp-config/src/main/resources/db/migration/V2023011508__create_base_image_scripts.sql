CREATE TABLE if not exists base_image_scripts(
                                   `img_script_id` VARCHAR(40) NOT NULL   COMMENT '脚本ID' ,
                                   `img_id` VARCHAR(40)    COMMENT '镜像ID' ,
                                   `script_name` VARCHAR(60)    COMMENT '脚本名称' ,
                                   `run_timing` VARCHAR(30)    COMMENT '执行时机;aftervminit,beforestart,afterstart' ,
                                   `playbook_uri` VARCHAR(255)    COMMENT 'playbook脚本' ,
                                   `script_file_uri` VARCHAR(300)    COMMENT '脚本路径' ,
                                   `extra_vars` VARCHAR(300)    COMMENT '扩展' ,
                                   `sort_no` INT    COMMENT '脚本执行顺序;升序' ,
                                   `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                   `created_time` DATETIME    COMMENT '创建时间' ,
                                   PRIMARY KEY (img_script_id)
)  COMMENT = '镜像与脚本关联表';