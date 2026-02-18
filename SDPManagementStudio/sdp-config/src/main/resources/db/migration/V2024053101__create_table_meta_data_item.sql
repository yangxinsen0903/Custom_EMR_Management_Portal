CREATE TABLE IF NOT EXISTS `meta_data_item` (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    region VARCHAR(64) NOT NULL DEFAULT '-' COMMENT '地域，如果type类型是SupportedRegionList则为默认值',
    type VARCHAR(255) NOT NULL COMMENT '元数据类型 - SupportedRegionList, SupportedVMSkuList, SupportedDiskSkuList, SupportedSubnetList, SupportedNSGSkuList, SupportedSSHKeyPairList, SupportedManagedIdentityList, SupportedLogsBlobContainerList, SupportedAvailabilityZoneList',
    version VARCHAR(64) COMMENT '版本号',
    data TEXT NOT NULL COMMENT '元数据',
    remark TEXT COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id VARCHAR(64) NOT NULL,
    last_modified_id  VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci  COMMENT='元数据条目表';