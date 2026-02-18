CREATE TABLE if not exists conf_scaling_vm_data_vol(
                                         `vm_data_vol_id` VARCHAR(32) NOT NULL   COMMENT '虚拟机数据盘配置ID' ,
                                         `vm_detail_id` VARCHAR(32)    COMMENT '虚拟机配置ID' ,
                                         `data_volume_type` VARCHAR(30)    COMMENT '云盘数据盘类型' ,
                                         `local_volume_type` VARCHAR(30)    COMMENT '本地数据盘类型' ,
                                         `data_volume_size` INT    COMMENT '数据盘大小（GB）' ,
                                         `count` INT    COMMENT '购买磁盘数量' ,
                                         PRIMARY KEY (vm_data_vol_id)
)  COMMENT = '伸缩VM数据盘配置;2.0版本新增';

