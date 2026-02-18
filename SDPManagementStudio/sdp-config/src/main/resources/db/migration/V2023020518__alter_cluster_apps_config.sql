ALTER TABLE conf_cluster_apps_config MODIFY COLUMN app_config_classification varchar(128)  NULL COMMENT '配置分类';
ALTER TABLE conf_cluster_apps_config MODIFY COLUMN config_item varchar(1024) NULL COMMENT '配置项';
ALTER TABLE conf_cluster_apps_config MODIFY COLUMN config_val MEDIUMTEXT NULL COMMENT '配置项value';
