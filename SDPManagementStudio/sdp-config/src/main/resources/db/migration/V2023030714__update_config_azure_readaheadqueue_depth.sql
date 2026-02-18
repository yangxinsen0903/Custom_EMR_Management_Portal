-- ABFS driver 有prefetch buffer size的功能, 但是有bug, 网络限流的时候会读错, 目前把此功能关闭
delete from ambari_config_item where config_type_code = 'core-site' and `key` = 'fs.azure.readaheadqueue.depth';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.readaheadqueue.depth','0',0,0,'','NON_HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.readaheadqueue.depth','0',0,0,'','HA','VALID','system',now(),'system',now());

