-- 美国站追平 config_item
delete from ambari_config_item where config_type_code = 'hdfs-site' and `key` = 'dfs.namenode.datanode.registration.ip-hostname-check';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.namenode.datanode.registration.ip-hostname-check','false',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.namenode.datanode.registration.ip-hostname-check','false',0,0,NULL,'HA','VALID','system',now(),'system',now());

