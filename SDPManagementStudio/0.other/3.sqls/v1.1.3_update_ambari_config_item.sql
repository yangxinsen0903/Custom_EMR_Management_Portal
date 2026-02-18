-- 替换掉/sdp/apps/目录中包含 sdp.version变量的配置值
update ambari_config_item set value = replace(value, '/sdp/apps/${sdp.version}/', '/sdp/apps/')
where value like '%/sdp/apps/${sdp.version}/%';

-- 恢复之前认为无用但其实很有用的MI的配置
delete from ambari_config_item where `key` like '%fs.azure.account%' ;
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.auth.type','OAuth',0,0,NULL,'HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth.provider.type','org.apache.hadoop.fs.azurebfs.oauth2.MsiTokenProvider',0,0,NULL,'HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.msi.tenant','',0,1,'MI_ABFS','HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.msi.endpoint','http://169.254.169.254/metadata/identity/oauth2/token',0,0,NULL,'HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.client.id','',0,1,'MI_ABFS','HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.auth.type','OAuth',0,0,NULL,'NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth.provider.type','org.apache.hadoop.fs.azurebfs.oauth2.MsiTokenProvider',0,0,NULL,'NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.msi.tenant','',0,1,'MI_ABFS','NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.msi.endpoint','http://169.254.169.254/metadata/identity/oauth2/token',0,0,NULL,'NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00'),
                                                                                                                                                                                                                 ('SDP-1.0','HDFS','NULL','core-site','fs.azure.account.oauth2.client.id','',0,1,'MI_ABFS','NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00');
-- 设置sdp.version版本号
delete from ambari_config_item where config_type_code = 'mapred-site' and `key` = 'sdp.version';

INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
    ('SDP-1.0','MAPREDUCE2','NULL','mapred-site','sdp.version','1.0',0,0,NULL,'HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00');
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
    ('SDP-1.0','MAPREDUCE2','NULL','mapred-site','sdp.version','1.0',0,0,NULL,'NON_HA','VALID','system','2023-01-10 17:54:00','system','2023-01-10 17:54:00');



