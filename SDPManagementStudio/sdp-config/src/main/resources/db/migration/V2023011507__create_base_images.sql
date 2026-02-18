CREATE TABLE if not exists base_images(
                            `img_id` VARCHAR(32) NOT NULL   COMMENT '镜像ID' ,
                            `os_image_id` VARCHAR(200)    COMMENT '镜像resource ID' ,
                            `os_image_type` VARCHAR(20)    COMMENT '镜像类型;标准/自定义' ,
                            `os_version` VARCHAR(200)    COMMENT '镜像内系统版本号' ,
                            `created_time` DATETIME    COMMENT '创建时间' ,
                            `createdby` VARCHAR(60)    COMMENT '创建人' ,
                            PRIMARY KEY (img_id)
)  COMMENT = '镜像表';
