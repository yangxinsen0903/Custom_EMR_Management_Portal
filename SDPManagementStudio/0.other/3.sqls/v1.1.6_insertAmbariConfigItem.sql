-- 解决HBase测试过程中的Bug
delete from ambari_config_item where `key` = 'dfs.storage.policy.enabled' and config_type_code = 'hdfs-site';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.storage.policy.enabled','false',0,0,'NON_HA','VALID','system','2023-01-16 22:51:47','system','2023-01-16 22:51:47');
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.storage.policy.enabled','false',0,0,'HA','VALID','system','2023-01-16 22:51:47','system','2023-01-16 22:51:47');
