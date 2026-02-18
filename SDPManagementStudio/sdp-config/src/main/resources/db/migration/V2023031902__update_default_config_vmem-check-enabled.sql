
delete from ambari_config_item where config_type_code='yarn-site' and `key` = 'yarn.nodemanager.vmem-check-enabled';
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.nodemanager.vmem-check-enabled','true',0,0,NULL,'NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','YARN','NULL','yarn-site','yarn.nodemanager.vmem-check-enabled','true',0,0,NULL,'HA','VALID','system',now(),'system',now());
