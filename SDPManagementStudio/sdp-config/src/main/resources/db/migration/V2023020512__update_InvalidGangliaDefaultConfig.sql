update ambari_config_item
set state = 'INVALID'
where service_code = 'HDFS' and config_type_code = 'hadoop-metrics2.properties';