delete from ambari_config_item where `key` = 'repo_ubuntu_template';

update ambari_component_layout set state = 'INVALID'
where host_group = 'TASK' and is_ha  = 1 and component_code  in ('ZOOKEEPER_CLIENT',
                                                                 'HDFS_CLIENT','MAPREDUCE2_CLIENT','HIVE_CLIENT','HBASE_CLIENT','SPARK3_CLIENT','TEZ_CLIENT','SQOOP');