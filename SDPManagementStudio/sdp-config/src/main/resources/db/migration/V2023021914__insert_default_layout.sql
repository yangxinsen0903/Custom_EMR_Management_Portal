-- TASK组增加HDFS_CLIENT
delete from ambari_component_layout where service_code = 'HDFS' and host_group = 'TASK' and component_code = 'HDFS_CLIENT';

INSERT INTO ambari_component_layout (service_code,host_group,component_code,is_ha,state,created_by,created_time,updated_by,updated_time) VALUES
	 ('HDFS','TASK','HDFS_CLIENT',1,'VALID','system',now(),'system',now()),
	 ('HDFS','TASK','HDFS_CLIENT',0,'VALID','system',now(),'system',now());
