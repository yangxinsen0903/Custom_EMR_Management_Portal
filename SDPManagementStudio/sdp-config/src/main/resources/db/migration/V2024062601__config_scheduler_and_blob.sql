-- SDP 1.0的 scheduler文件路径
update ambari_config_item set
  value = '{{spark_conf}}/spark-thrift-fairscheduler.xml',
  updated_by = 'system',
  updated_time=now()
where `key` = 'spark.scheduler.allocation.file'
  and config_type_code = 'spark3-thrift-sparkconf'
  and stack_code = 'SDP-1.0';

-- Stack blob的开关
-- SDP-1.0 fs.azure.enable.blob.endpoint  true
INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-1.0','HDFS','NULL','core-site','fs.azure.enable.blob.endpoint','true',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.enable.blob.endpoint'
                    and item_type='NON_HA'
                    and stack_code = 'SDP-1.0');

INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-1.0','HDFS','NULL','core-site','fs.azure.enable.blob.endpoint','true',0,0,NULL,'HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.enable.blob.endpoint'
                    and item_type='HA'
                    and stack_code = 'SDP-1.0');

-- SDP-1.0 fs.azure.account.hns.enabled   false
INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-1.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.account.hns.enabled'
                    and item_type='NON_HA'
                    and stack_code = 'SDP-1.0');

INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-1.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,NULL,'HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.account.hns.enabled'
                    and item_type='HA'
                    and stack_code = 'SDP-1.0');


-- SDP-2.0 fs.azure.enable.blob.endpoint  true
INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-2.0','HDFS','NULL','core-site','fs.azure.enable.blob.endpoint','true',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.enable.blob.endpoint'
                    and item_type='NON_HA'
                    and stack_code = 'SDP-2.0');

INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-2.0','HDFS','NULL','core-site','fs.azure.enable.blob.endpoint','true',0,0,NULL,'HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.enable.blob.endpoint'
                    and item_type='HA'
                    and stack_code = 'SDP-2.0');

-- SDP-2.0 fs.azure.account.hns.enabled   false
INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-2.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,NULL,'NON_HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.account.hns.enabled'
                    and item_type='NON_HA'
                    and stack_code = 'SDP-2.0');

INSERT INTO ambari_config_item(stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time)
select 'SDP-2.0','HDFS','NULL','core-site','fs.azure.account.hns.enabled','false',0,0,NULL,'HA','VALID','system', now(),'system',now()
where not exists (select 1 from ambari_config_item
                  where config_type_code = 'core-site'
                    and `key` = 'fs.azure.account.hns.enabled'
                    and item_type='HA'
                    and stack_code = 'SDP-2.0');


-- 更新
update ambari_config_item
set value = 'true'
where config_type_code = 'core-site'
  and `key` = 'fs.azure.enable.blob.endpoint';

update ambari_config_item
set value = 'true'
where config_type_code = 'core-site'
  and `key` = 'fs.azure.account.hns.enabled';