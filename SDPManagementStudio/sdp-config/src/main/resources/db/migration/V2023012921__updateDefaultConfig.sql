-- 更新 yarn-site的 hadoop.http.cross-origin.allowed-origins.将原来的{{cross_origins}} 改为 *
update ambari_config_item set value = '*' where config_type_code  = 'yarn-site' and `key` = 'hadoop.http.cross-origin.allowed-origins';

-- 在 sqoop-env 增加配置 export HADOOP_CLASSPATH=$HIVE_HOME/lib/*:$HADOOP_CLASSPATH

