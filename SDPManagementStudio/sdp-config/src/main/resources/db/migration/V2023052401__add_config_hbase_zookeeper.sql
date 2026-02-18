delete from ambari_config_item where config_type_code = 'hbase-site' and `key` = 'hbase.zookeeper.quorum';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
('SDP-1.0','HBASE','NULL','hbase-site','hbase.zookeeper.quorum','%HOSTGROUP::MASTER1%:2181',0,0,'','NON_HA','VALID','sysstem',now(),'system',now()),
('SDP-1.0','HBASE','NULL','hbase-site','hbase.zookeeper.quorum','%HOSTGROUP::MASTER1%:2181,%HOSTGROUP::MASTER2%:2181,%HOSTGROUP::AMBARI%:2181',0,0,'','HA','VALID','sysstem',now(),'system',now());
