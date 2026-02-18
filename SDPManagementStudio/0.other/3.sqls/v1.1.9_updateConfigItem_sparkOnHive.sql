-- 更新Spark On Hive的默认参数，由 spark 改为 hive
UPDATE ambari_config_item SET value='hive'
where config_type_code = 'spark3-hive-site-override' and `key` = 'metastore.catalog.default' and value = 'spark';
