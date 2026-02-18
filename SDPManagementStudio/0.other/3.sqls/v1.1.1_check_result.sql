
select 'ambari_config_item' as table_name,if(count(*)=0,true,false) as check_result,'delete' as action
from sdpms.ambari_config_item where `key` = 'repo_ubuntu_template'
union all
select 'ambari_component_layout' as table_name,if(count(*)=8,true,false) as check_result,'update' as action
from sdpms.ambari_component_layout where state = 'INVALID' and host_group = 'TASK' and is_ha  = 1 and component_code  in ('ZOOKEEPER_CLIENT',
                                                                 'HDFS_CLIENT','MAPREDUCE2_CLIENT','HIVE_CLIENT','HBASE_CLIENT','SPARK3_CLIENT','TEZ_CLIENT','SQOOP');
