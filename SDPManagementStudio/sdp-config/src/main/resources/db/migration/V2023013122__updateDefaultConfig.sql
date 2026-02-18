-- tez-site 更新配置值
update ambari_config_item set value = 'http://%HOSTGROUP::AMBARI%:8085'
where config_type_code='tez-site' and `key` = 'tez.tez-ui.history-url.base' and item_type = 'HA';

update ambari_config_item set value = 'http://%HOSTGROUP::MASTER1%:8085'
where config_type_code='tez-site' and `key` = 'tez.tez-ui.history-url.base' and item_type = 'NON_HA';

update ambari_config_item set value = '__HISTORY_URL_BASE__/#/tez-app/__APPLICATION_ID__'
where config_type_code='tez-site' and `key` = 'tez.am.tez-ui.history-url.template';
