-- 字段扩容
ALTER TABLE base_release_apps_config MODIFY COLUMN app_config_classification varchar(48) NOT NULL COMMENT '配置分类';

-- 更新所有配置分类与大数据应用的关系
REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'core-site', 'core-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'hadoop-env', 'hadoop-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'hadoop-metrics2.properties', 'hadoop-metrics2.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'hadoop-policy', 'hadoop-policy.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'hdfs-log4j', 'hdfs-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'hdfs-site', 'hdfs-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'llap-cli-log4j2', 'llap-cli-log4j2.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'llap-daemon-log4j', 'llap-daemon-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'ranger-hdfs-audit', 'ranger-hdfs-audit.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'ranger-hdfs-plugin-properties', 'ranger-hdfs-plugin-properties.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'ranger-hdfs-policymgr-ssl', 'ranger-hdfs-policymgr-ssl.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'ranger-hdfs-security', 'ranger-hdfs-security.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'resource-types', 'resource-types.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'ssl-server', 'ssl-server.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'HDFS', 'viewfs-mount-table', 'viewfs-mount-table.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'beeline-log4j2', 'beeline-log4j2.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-atlas-application.properties', 'hive-atlas-application.properties', NULL, 0, 'sysadmin',now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-env', 'hive-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-exec-log4j2', 'hive-exec-log4j2.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-interactive-env', 'hive-interactive-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-interactive-site', 'hive-interactive-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-log4j2', 'hive-log4j2.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hive-site', 'hive-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hivemetastore-site', 'hivemetastore-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hiveserver2-interactive-site', 'hiveserver2-interactive-site.xml', NULL, 0, 'sysadmin',now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'hiveserver2-site', 'hiveserver2-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'parquet-logging', 'parquet-logging.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'ranger-hive-audit', 'ranger-hive-audit.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'ranger-hive-plugin-properties', 'ranger-hive-plugin-properties.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'ranger-hive-policymgr-ssl', 'ranger-hive-policymgr-ssl.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Hive', 'ranger-hive-security', 'ranger-hive-security.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'MapReduce2', 'mapred-env', 'mapred-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'MapReduce2', 'mapred-site', 'mapred-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy3-client-conf', 'livy3-client-conf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy3-conf', 'livy3-conf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy3-env', 'livy3-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy3-log4j-properties', 'livy3-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy3-spark-blacklist', 'livy3-spark-blacklist.conf', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-atlas-application-properties-override','spark3-atlas-application-properties-override', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-defaults', 'spark3-defaults.conf', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-env', 'spark3-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-hive-site-override', 'spark3-hive-site-override.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-log4j-properties', 'spark3-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-metrics-properties', 'spark3-metrics.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-thrift-fairscheduler', 'spark3-thrift-fairscheduler.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark3-thrift-sparkconf', 'spark3-thrift-sparkconf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy-client-conf', 'livy3-client-conf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy-conf', 'livy3-conf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy-env', 'livy3-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy-log4j-properties', 'livy3-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'livy-spark-blacklist', 'livy3-spark-blacklist.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-atlas-application-properties-override','spark3-atlas-application-properties-override', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-defaults', 'spark3-defaults.conf', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-env', 'spark3-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-hive-site-override', 'spark3-hive-site-override.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-log4j-properties', 'spark3-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-metrics-properties', 'spark3-metrics.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-thrift-fairscheduler', 'spark3-thrift-fairscheduler.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Spark3', 'spark-thrift-sparkconf', 'spark3-thrift-sparkconf.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'SQOOP', 'sqoop-atlas-application.properties', 'sqoop-atlas-application.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'SQOOP', 'sqoop-env', 'sqoop-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Tez', 'tez-env', 'tez-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Tez', 'tez-interactive-site', 'tez-interactive-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Tez', 'tez-site', 'tez-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'capacity-scheduler', 'capacity-scheduler.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'container-executor', 'container-executor.cfg', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'ranger-yarn-audit', 'ranger-yarn-audit.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'ranger-yarn-plugin-properties', 'ranger-yarn-plugin.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'ranger-yarn-policymgr-ssl', 'ranger-yarn-policymgr-ssl.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'ranger-yarn-security', 'ranger-yarn-security.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'yarn-env', 'yarn-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'yarn-log4j', 'yarn-log4j.properties', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'Yarn', 'yarn-site', 'yarn-site.xml', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'ZooKeeper', 'zoo.cfg', 'zoo.cfg', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'ZooKeeper', 'zookeeper-env', 'zookeeper-env.sh', NULL, 0, 'sysadmin', now());

REPLACE INTO base_release_apps_config (release_version, app_name, app_config_classification, app_config_file,sort_no, is_delete, createdby, created_time) VALUES ('SDP-1.0', 'ZooKeeper', 'zookeeper-log4j', 'zookeeper-log4j.properties', NULL, 0, 'sysadmin', now());
