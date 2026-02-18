
-- yarn-site
delete from ambari_config_item where config_type_code = 'yarn-site' and `key`  = 'yarn.resourcemanager.zk-timeout-ms';
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.resourcemanager.zk-timeout-ms','30000',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.resourcemanager.zk-timeout-ms','30000',0,0,NULL,'HA','VALID','system', now(),'system',now());

-- spark3-defaults
delete from ambari_config_item where config_type_code = 'spark3-defaults' and `key` in ('spark.history.store.path', 'spark.history.store.maxDiskUsage');
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.history.store.path','/data/disk0/hadoop/spark3/shs_db',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.history.store.path','/data/disk0/hadoop/spark3/shs_db',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.history.store.maxDiskUsage','10737418240',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.history.store.maxDiskUsage','10737418240',0,0,NULL,'HA','VALID','system',now(),'system',now());

-- zoo.cfg
delete from ambari_config_item where config_type_code = 'zoo.cfg' and `key`  = 'dataDir';
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','ZOOKEEPER','NULL','zoo.cfg','dataDir','/data/disk0/hadoop/zookeeper',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()),
	 ('SDP-1.0','ZOOKEEPER','NULL','zoo.cfg','dataDir','/data/disk0/hadoop/zookeeper',0,0,NULL,'HA','VALID','system', now(),'system',now());
