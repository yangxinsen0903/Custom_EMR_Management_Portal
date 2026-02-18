
UPDATE ambari_config_item
	SET state='VALID', updated_time = now()
	WHERE config_type_code  = 'hadoop-metrics2.properties' and `key` = 'content';

