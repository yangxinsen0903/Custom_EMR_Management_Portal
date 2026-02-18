
UPDATE ambari_config_item
	SET value='1.5f'
	WHERE config_type_code = 'yarn-site' and `key`  = 'yarn.timeline-service.versions';

UPDATE ambari_config_item
	SET value='org.apache.tez.dag.history.logging.ats.TimelineCachePluginImpl'
	WHERE config_type_code = 'yarn-site' and `key`  = 'yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes';

