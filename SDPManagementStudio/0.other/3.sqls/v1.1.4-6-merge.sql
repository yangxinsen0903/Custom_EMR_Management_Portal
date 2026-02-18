-- 1.1.4 更新HBase版本号
update base_scene_apps set app_version = '2.4.13' where scene_id = '692fc42a-5251-1eb8-f1de-6dbbd3e47529' and app_name = 'HBASE';
update base_release_apps set app_verison = '2.4.13' where release_version = 'SDP-1.0' and app_name = 'HBase';

-- 1.1.5 更新MI配置信息
UPDATE ambari_config_item
        SET value='Custom'
        WHERE `key` = 'fs.azure.account.auth.type';
UPDATE ambari_config_item
        SET value='com.github.azure.hadoop.custom.auth.MSIHDFSCachedAccessTokenProvider'
        WHERE `key` = 'fs.azure.account.oauth.provider.type';

-- 1.1.6 解决HBase测试过程中的Bug
delete from ambari_config_item where `key` = 'dfs.storage.policy.enabled' and config_type_code = 'hdfs-site';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.storage.policy.enabled','false',0,0,'NON_HA','VALID','system','2023-01-16 22:51:47','system','2023-01-16 22:51:47');
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.storage.policy.enabled','false',0,0,'HA','VALID','system','2023-01-16 22:51:47','system','2023-01-16 22:51:47');
