--
delete from ambari_config_item where config_type_code = 'spark3-defaults' and `key` = 'spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version','2',0,0,NULL,'HA','VALID','system',now(),'system',now()),
	 ('SDP-1.0','SPARK3','NULL','spark3-defaults','spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version','2',0,0,NULL,'NON_HA','VALID','system',now(),'system',now());
