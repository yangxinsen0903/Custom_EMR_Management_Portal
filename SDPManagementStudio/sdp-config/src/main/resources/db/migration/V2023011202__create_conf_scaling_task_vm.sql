CREATE TABLE if not exists conf_scaling_task_vm(
                                     `vm_detail_id` VARCHAR(40) NOT NULL   COMMENT '任务明细ID' ,
                                     `task_id` VARCHAR(40)    COMMENT '任务ID' ,
                                     `sku` VARCHAR(50)    COMMENT '实例规格' ,
                                     `os_imageid` VARCHAR(50)    COMMENT '镜像ID' ,
                                     `os_image_type` VARCHAR(20)    COMMENT '镜像类型;标准/自定义' ,
                                     `os_version` VARCHAR(60)    COMMENT '镜像内系统版本号' ,
                                     `os_volume_size` INT    COMMENT 'OS磁盘大小' ,
                                     `os_volume_type` VARCHAR(30)    COMMENT 'OS磁盘类型' ,
                                     `vcpus` VARCHAR(10)    COMMENT 'CPU核数' ,
                                     `memory` VARCHAR(10)    COMMENT '内存大小（GB）' ,
                                     `count` INT    COMMENT '实例购买数量' ,
                                     `purchase_type` INT    COMMENT '购买类型;1 按需  2 竞价' ,
                                     `state` INT    COMMENT '状态;0 待创建 1 创建中 2 已创建  -1释放中 -2 已释放   7 扩容中  8 缩容中' ,
                                     `createdby` VARCHAR(60)    COMMENT '创建人' ,
                                     `created_time` DATETIME    COMMENT '创建时间' ,
                                     PRIMARY KEY (vm_detail_id)
)  COMMENT = '伸缩任务VM明细表;2.0版本新增';