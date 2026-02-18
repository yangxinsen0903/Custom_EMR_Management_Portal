select config_type_code , item_type , `key`, value from ambari_config_item where config_type_code = 'yarn-site' and `key`  = 'yarn.resourcemanager.zk-timeout-ms'
union all
select config_type_code , item_type , `key`, value from ambari_config_item where config_type_code = 'zoo.cfg' and `key`  = 'dataDir'
union all
select config_type_code , item_type , `key`, value from ambari_config_item where config_type_code = 'spark3-defaults' and `key` in ('spark.history.store.path', 'spark.history.store.maxDiskUsage');
