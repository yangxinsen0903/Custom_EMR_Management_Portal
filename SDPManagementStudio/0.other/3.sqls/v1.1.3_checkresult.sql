
select 'ambari_config_item' as table_name,if(count(*)=0,true,false) as check_result,'update' as action from ambari_config_item
where  value like '%/sdp/apps/${sdp.version}/%'
union all
select 'ambari_config_item' as table_name,if(count(*)=10,true,false) as check_result,'insert' as action from ambari_config_item
where `key` like '%fs.azure.account%'
union all
select 'ambari_config_item' as table_name,if(count(*)=2,true,false) as check_result,'insert_version' as action from ambari_config_item where config_type_code = 'mapred-site' and `key` = 'sdp.version';

