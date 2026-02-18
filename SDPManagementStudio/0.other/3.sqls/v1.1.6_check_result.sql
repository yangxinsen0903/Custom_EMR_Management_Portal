select 'ambari_config_item' as table_name,if(count(*)=2,true,false) as check_result,'insert' as action from ambari_config_item
where `key` = 'dfs.storage.policy.enabled' and config_type_code = 'hdfs-site';
