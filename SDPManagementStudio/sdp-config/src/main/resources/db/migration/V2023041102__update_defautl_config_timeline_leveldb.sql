-- 删除原有的配置项
DELETE FROM ambari_config_item WHERE stack_code = 'SDP-1.0' AND service_code = 'YARN'
    AND component_code = 'RESOURCEMANAGER' AND config_type_code = 'yarn-site' AND item_type= 'NON_HA'
    AND `key` IN ('yarn.timeline-service.leveldb-timeline-store.path','yarn.timeline-service.leveldb-state-store.path');

-- 新增配置项
INSERT INTO ambari_config_item (stack_code,service_code,component_code,config_type_code,`key`,value,is_content_prop,is_dynamic,dynamic_type,item_type,state,created_by,created_time,updated_by,updated_time) VALUES
    ('SDP-1.0','YARN','RESOURCEMANAGER','yarn-site','yarn.timeline-service.leveldb-timeline-store.path','/data/disk0/hadoop/yarn/timeline',0,0,NULL,'NON_HA','VALID','sysstem',now(),'system',now()),
    ('SDP-1.0','YARN','RESOURCEMANAGER','yarn-site','yarn.timeline-service.leveldb-state-store.path','/data/disk0/hadoop/yarn/timeline',0,0,NULL,'NON_HA','VALID','sysstem',now(),'system',now());

-- 更新配置项为多磁盘
update ambari_config_item set is_dynamic = 1, dynamic_type = 'MULTI_DISK_TASK', updated_by = 'system', updated_time = now()
where stack_code = 'SDP-1.0' AND service_code = 'YARN'
  AND component_code = 'RESOURCEMANAGER' AND config_type_code = 'yarn-site'
  AND `key` IN ('yarn.nodemanager.local-dirs', 'yarn.nodemanager.log-dirs',
                'yarn.timeline-service.leveldb-timeline-store.path','yarn.timeline-service.leveldb-state-store.path');



