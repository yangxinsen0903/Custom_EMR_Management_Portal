-- 更新TezUI的默认配置
-- 1. yarn-site.xml
delete from ambari_config_item
where config_type_code = 'yarn-site' and `key`  in ('yarn.timeline-service.enabled', 'yarn.timeline-service.address', 'yarn.timeline-service.version');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','YARN','RESOURCEMANAGER','yarn-site','yarn.timeline-service.address','%HOSTGROUP::MASTER1%:10200',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.timeline-service.enabled','true',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.timeline-service.version','2.0f',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','RESOURCEMANAGER','yarn-site','yarn.timeline-service.address','%HOSTGROUP::MASTER1%:10200',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.timeline-service.enabled','true',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.timeline-service.version','2.0f',0,0,NULL,'HA','VALID','system',now(),'system',now());


-- 2. tez-site.xml
delete from ambari_config_item where config_type_code = 'tez-site' and `key`  in ('tez.tez-ui.history-url.base', 'tez.history.logging.service.class', 'tez.am.tez-ui.history-url.template');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.history.logging.service.class','org.apache.tez.dag.history.logging.ats.ATSHistoryLoggingService',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.am.tez-ui.history-url.template','__HISTORY_URL_BASE__/#/tez-app/__APPLICATION_ID__',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.tez-ui.history-url.base','http://%HOSTGROUP::MASTER1%:8085',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.history.logging.service.class','org.apache.tez.dag.history.logging.ats.ATSHistoryLoggingService',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.am.tez-ui.history-url.template','__HISTORY_URL_BASE__/#/tez-app/__APPLICATION_ID__',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','TEZ','NULL','tez-site','tez.tez-ui.history-url.base','http://%HOSTGROUP::AMBARI%:8085',0,0,NULL,'HA','VALID','system',now(),'system',now());


-- 3. hive-site.xml
delete from ambari_config_item
where config_type_code = 'hive-site' and `key`  in ('hive.exec.failure.hooks', 'hive.exec.post.hooks', 'hive.exec.pre.hooks');

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.failure.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.post.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.pre.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.failure.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.post.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HIVE','NULL','hive-site','hive.exec.pre.hooks','org.apache.hadoop.hive.ql.hooks.HiveProtoLoggingHook,org.apache.hadoop.hive.ql.hooks.ATSHook',0,0,NULL,'HA','VALID','system',now(),'system',now());

