
select '2.1.6' as version, 'ambari_config_item' as table_name, if(count(*)=2, true, false) as check_result,'update' as action
from ambari_config_item
where config_type_code = 'yarn-site' and `key`  = 'yarn.resourcemanager.zk-timeout-ms' and `value` = '30000'
union all
select '2.1.6' as version, 'ambari_config_item' as table_name, if(count(*)=2, true, false) as check_result,'update' as action
from ambari_config_item
where config_type_code = 'zoo.cfg' and `key`  = 'dataDir' and `value` = '/data/disk0/hadoop/zookeeper'
union all
select '2.1.6' as version, 'ambari_config_item' as table_name, if(count(*)=4, true, false) as check_result,'update' as action
from ambari_config_item
where config_type_code = 'spark3-defaults'
	and ((`key`  = 'spark.history.store.path' and `value` = '/data/disk0/hadoop/spark3/shs_db')
		or (`key`  = 'spark.history.store.maxDiskUsage' and `value` = '10737418240'));