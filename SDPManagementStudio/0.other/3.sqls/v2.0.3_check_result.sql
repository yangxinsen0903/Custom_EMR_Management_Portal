--
select '2.0.3' as version, 'ambari_config_item' as table_name, if(count(*)=2, true, false) as check_result, 'insert' as action
from ambari_config_item
where config_type_code = 'hdfs-site' and `key` = 'dfs.namenode.datanode.registration.ip-hostname-check';

