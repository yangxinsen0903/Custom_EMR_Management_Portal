-- 保证不会产生重复数据， 先删除
delete from ambari_config_item where config_type_code = 'core-site' and `key` = 'fs.azure.account.hns.enabled';

-- fs.azure.account.hns.enabled 配置项
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,'','NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
	 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,'','HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00');

