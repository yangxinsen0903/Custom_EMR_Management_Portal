-- hive-log4j2  hive2_log_maxfilesize=102400
delete from ambari_config_item  where config_type_code = 'hive-log4j2' and `key` = 'hive2_log_maxfilesize';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HIVE','NULL','hive-log4j2','hive2_log_maxfilesize','102400',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HIVE','NULL','hive-log4j2','hive2_log_maxfilesize','102400',0,0,NULL,'HA','VALID','system', now(),'system', now());

-- hadoop-env   yarn_user_nofile_limit=655360   hdfs_user_nproc_limit=655360
delete from ambari_config_item  where config_type_code = 'hadoop-env' and `key` in ('yarn_user_nofile_limit', 'hdfs_user_nproc_limit');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','hadoop-env','yarn_user_nofile_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HDFS','NULL','hadoop-env','yarn_user_nofile_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nproc_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HDFS','NULL','hadoop-env','hdfs_user_nproc_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());

-- yarn-env    yarn_user_nofile_limit=655360   yarn_user_nproc_limit=655360
delete from ambari_config_item  where config_type_code = 'yarn-env' and `key` in ('yarn_user_nofile_limit', 'yarn_user_nproc_limit');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','YARN','NULL','yarn-env','yarn_user_nofile_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','YARN','NULL','yarn-env','yarn_user_nproc_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','YARN','NULL','yarn-env','yarn_user_nofile_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','YARN','NULL','yarn-env','yarn_user_nproc_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());

-- mapred-env   mapred_user_nofile_limit=655350   mapred_user_nproc_limit=655350
delete from ambari_config_item  where config_type_code = 'mapred-env' and `key` in ('mapred_user_nofile_limit', 'mapred_user_nproc_limit');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','MAPREDUCE2','NULL','mapred-env','mapred_user_nofile_limit','655350',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','MAPREDUCE2','NULL','mapred-env','mapred_user_nproc_limit','655350',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','MAPREDUCE2','NULL','mapred-env','mapred_user_nofile_limit','655350',0,0,NULL,'HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','MAPREDUCE2','NULL','mapred-env','mapred_user_nproc_limit','655350',0,0,NULL,'HA','VALID','system', now(),'system', now());

-- hive-env  hive_user_nofile_limit=655360
delete from ambari_config_item  where config_type_code = 'hive-env' and `key` in ('hive_user_nofile_limit');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HIVE','NULL','hive-env','hive_user_nofile_limit','655360',0,0,NULL,'NON_HA','VALID','system', now(),'system', now()),
	 ('SDP-1.0','HIVE','NULL','hive-env','hive_user_nofile_limit','655360',0,0,NULL,'HA','VALID','system', now(),'system', now());
