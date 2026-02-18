
/* Alter table in target */
ALTER TABLE sdpms.conf_cluster
    ADD COLUMN `log_mi` varchar(256)    NULL COMMENT '日志桶托管标识' after `log_path` ,
    CHANGE `keypair_id` `keypair_id` varchar(200)    NULL COMMENT '密钥对ID' after `log_mi` ,
    ADD COLUMN `vm_mi` varchar(256)    NULL COMMENT '虚拟机托管标识' after `keypair_id` ,
    ADD COLUMN `vm_mi_tenant_id` varchar(64)    NULL COMMENT '集群节点托管MI的TenantId' after `vm_mi` ,
    ADD COLUMN `vm_mi_client_id` varchar(64)    NULL COMMENT '集群节点托管MI的ClientId' after `vm_mi_tenant_id` ,
    CHANGE `delete_protected` `delete_protected` varchar(1)    NULL COMMENT '关闭保护' after `vm_mi_client_id` ,
    ADD COLUMN `ambari_db_autocreate` int   NULL COMMENT '是否自动创建ambari数据库' after `ambari_database` ,
    CHANGE `hive_metadata_dburl` `hive_metadata_dburl` varchar(300)    NULL COMMENT 'hive元数据数据库地址' after `ambari_db_autocreate` ,
    ADD COLUMN `src_cluster_id` varchar(40)    NULL COMMENT '复制源集群ID' after `ambari_acount` ,
    ADD `zone` varchar(32) NULL COMMENT '可用区代码'  AFTER scene,
    CHANGE `is_ha` `is_ha` int   NULL COMMENT '是否高可用' after `src_cluster_id` ;


/* Alter table in target */
ALTER TABLE sdpms.conf_cluster_script
    ADD COLUMN `node_list` text  NULL COMMENT '执行脚本的机器列表' after `created_time` ;

ALTER TABLE sdpms.conf_cluster_script MODIFY COLUMN script_param TEXT  NULL COMMENT '脚本参数';

/* Alter table in target */
ALTER TABLE sdpms.conf_cluster_vm
    ADD COLUMN `group_name` varchar(60)   NULL COMMENT '实例组名称' after `vm_role` ,
    ADD COLUMN `elastic_rule_id` varchar(40)   NULL COMMENT '弹性伸缩规则ID' after `group_name` ,
    CHANGE `sku` `sku` varchar(50)   NULL COMMENT '实例规格' after `elastic_rule_id` ;

/* Alter table in target */
ALTER TABLE sdpms.conf_cluster_vm_data_volume
    ADD COLUMN `local_volume_type` varchar(30)   NULL COMMENT '本地数据盘类型' after `data_volume_type` ,
    CHANGE `data_volume_size` `data_volume_size` int   NULL COMMENT '数据盘大小（GB）' after `local_volume_type` ;

/* Alter table in target */
ALTER TABLE sdpms.info_cluster_playbook_job
    ADD COLUMN `conf_script_id` varchar(40)   NULL COMMENT '脚本ID' after `end_time` ,
    ADD COLUMN `job_type` varchar(20)   NULL COMMENT '脚本类型：sys 系统类型 user 用户自定义脚本' after `conf_script_id` ;

/* Alter table in target */
ALTER TABLE sdpms.info_cluster_vm
    CHANGE `host_name` `host_name` varchar(200)    NULL COMMENT '机器名称' after `vm_conf_id` ,
    ADD COLUMN `group_name` varchar(60)    NULL COMMENT '实例组名称' after `vm_role` ,
    CHANGE `sku_name` `sku_name` varchar(255)    NULL COMMENT 'sku名称' after `group_name` ,
    ADD COLUMN `state` int   NULL COMMENT 'vm 运行状态， 0 停止 1 运行中 -1 已销毁' after `create_endtime` ;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

