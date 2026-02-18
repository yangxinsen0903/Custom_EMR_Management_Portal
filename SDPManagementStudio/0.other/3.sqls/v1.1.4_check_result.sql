
select 'base_scene_apps' as table_name,if(count(*)=1,true,false) as check_result,'update' as action from base_scene_apps
where  scene_id = '692fc42a-5251-1eb8-f1de-6dbbd3e47529' and app_name = 'HBASE' and app_version ='2.4.13'
union all
select 'base_release_apps' as table_name,if(count(*)=1,true,false) as check_result,'update' as action from base_release_apps
where release_version = 'SDP-1.0' and app_name = 'HBase' and app_verison = '2.4.13'

