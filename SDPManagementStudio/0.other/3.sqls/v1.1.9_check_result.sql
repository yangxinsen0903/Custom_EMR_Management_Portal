-- check 1.1.9 的执行情况
select '1.1.9' as version, 'ambari_config_item' as table_name, if(count(*)=2,true,false) as check_result,'update' as action
from ambari_config_item aci
where config_type_code = 'spark3-hive-site-override' and `key` = 'metastore.catalog.default' and value = 'hive';

