-- hadoop-env   hdfs_user_nofile_limit=655360   hdfs_user_nproc_limit=655360
delete from ambari_config_item  where config_type_code = 'hadoop-env' and `key` in ('yarn_user_nofile_limit', 'hdfs_user_nproc_limit', 'hdfs_user_nofile_limit');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nofile_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nofile_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nproc_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nproc_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());
