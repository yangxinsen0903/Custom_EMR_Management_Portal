
select 'ambari_config_item' as table_name,if(count(*)=2,true,false) as check_result,'update' as action from ambari_config_item
where  `key` = 'fs.azure.account.auth.type' and value = 'Custom'
union all
select 'ambari_config_item' as table_name,if(count(*)=2,true,false) as check_result,'update' as action from ambari_config_item
where `key` = 'fs.azure.account.oauth.provider.type' and value = 'com.github.azure.hadoop.custom.auth.MSIHDFSCachedAccessTokenProvider';