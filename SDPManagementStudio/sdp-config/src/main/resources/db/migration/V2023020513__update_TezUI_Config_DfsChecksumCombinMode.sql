
-- 更新tez-site中的tez.history.logging.service.class配置
update ambari_config_item 
set value = 'org.apache.tez.dag.history.logging.ats.ATSV15HistoryLoggingService',
    updated_time = now()
where config_type_code = 'tez-site' and `key` = 'tez.history.logging.service.class';


-- 更新yarn-site中的yarn.timeline-service.version配置
update ambari_config_item 
set value = '1.5f',
    updated_time = now()
where config_type_code = 'yarn-site' and `key` = 'yarn.timeline-service.version';


-- 新增配置 hdfs-site 中 dfs.checksum.combine.mode
delete from ambari_config_item where config_type_code = 'hdfs-site' and `key` = 'dfs.checksum.combine.mode';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.checksum.combine.mode','COMPOSITE_CRC',0,0,'HA','VALID','system',now(),'system',now());
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,item_type,state,created_by,created_time,updated_by,updated_time)
	VALUES ('SDP-1.0','HDFS','NULL','hdfs-site','dfs.checksum.combine.mode','COMPOSITE_CRC',0,0,'NON_HA','VALID','system',now(),'system',now());

