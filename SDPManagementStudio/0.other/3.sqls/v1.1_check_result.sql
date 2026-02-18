
select 'base_scene' as table_name,if(count(*)=2,true,false) as check_result,'init' as action from sdpms.base_scene
union all
select 'base_scene_apps' as table_name,if(count(*)=13,true,false) as check_result,'init'as action from sdpms.base_scene_apps
union all
select 'base_script' as table_name, if(count(*)>=0,true,false) as check_result,'init' as action from sdpms.base_script
union all
select 'base_user_info' as table_name,if(count(*)>=1,true,false) as check_result,'init' as action from sdpms.base_user_info
union all
select 'conf_cluster' as table_name, if(count(*)=7,true,false) as check_result,'addcolumn' as action from information_schema.columns
where table_name = 'conf_cluster'
  and column_name in('log_mi','vm_mi','vm_mi_tenant_id','vm_mi_client_id','ambari_db_autocreate','src_cluster_id','zone')
  and TABLE_SCHEMA ='sdpms'
union all
select 'conf_cluster_script' as table_name,if(count(*)=1,true,false) as check_result,'addcolumn' as action from information_schema.columns
where table_name = 'conf_cluster_script'
  and column_name in('node_list')
  and TABLE_SCHEMA ='sdpms'
union all
select 'conf_cluster_script' as table_name, if(DATA_TYPE='text',true,false) as check_result,'column_change' as action from information_schema.columns
where table_name = 'conf_cluster_script'
  and column_name in('script_param')
  and TABLE_SCHEMA ='sdpms'
union all
select 'conf_cluster_vm' as table_name, if(count(*)=2,true,false) as check_result,'addcolumn' as action from information_schema.columns
where table_name = 'conf_cluster_vm'
  and column_name in('group_name','elastic_rule_id')
  and TABLE_SCHEMA ='sdpms'
union all
select 'conf_cluster_vm_data_volume' as table_name,if(count(*)=1,true,false) as check_result,'addcolumn' as action from information_schema.columns
where table_name = 'conf_cluster_vm_data_volume'
  and column_name in('local_volume_type')
  and TABLE_SCHEMA ='sdpms'
union all
select 'info_cluster_playbook_job' as table_name, if(count(*)=2,true,false) as check_result,'addcolumn' as action from information_schema.columns
where table_name = 'info_cluster_playbook_job'
  and column_name in('conf_script_id','job_type')
  and TABLE_SCHEMA ='sdpms'
union all
select 'info_cluster_vm' as table_name,if(count(*)=2,true,false) as  check_result,'addcolumn' as action from information_schema.columns
where table_name = 'info_cluster_vm'
  and column_name in('group_name','state')
  and TABLE_SCHEMA ='sdpms'
union all
select 'ambari_component_layout' as table_name, if(count(*)=117,true,false) as check_result,'reinit' as action from sdpms.ambari_component_layout
union all
select 'ambari_config_item' as table_name, if(count(*)=2353,true,false) as check_result,'reinit' as action from sdpms.ambari_config_item
union all
select 'base_cluster_operation_template' as table_name,if(count(*)=1,true,false) as check_result,'insert' as action from sdpms.base_cluster_operation_template
where template_id ='90f52443-838b-11ed-8607-6045bdc792d8'
union all
select 'base_cluster_operation_template_activity' as table_name,if(count(*)=2,true,false) as check_result,'insert' as action from sdpms.base_cluster_operation_template_activity where activity_id in ('dc050236-838b-11ed-8607-6045bdc792d8','dc0506c0-838b-11ed-8607-6045bdc792d8')
union all
select 'base_cluster_operation_template_activity' as table_name, if(count(*)=2,true,false) as check_result,'update' as action from sdpms.base_cluster_operation_template_activity
where activity_id  in ('5c4dc157-78d0-11ed-85b7-6045bdc7fdca','876f5319-79f0-11ed-85b7-6045bdc7fdca') and  timeout =3600
union all
select 'base_cluster_script' as table_name,if(count(*)=2,true,false) as check_result,'update' as action from sdpms.base_cluster_script where extra_vars like '%username={username}%' and script_file_uri like '%initialize%'
union all
select 'config_detail' as table_name,if(avalue='hadoop',true,false) as check_result,'update_vmusername' as action from sdpms.config_detail where akey='vm.username'
union all
select 'config_detail' as table_name,if(count(*)=1,true,false) as check_result,'insert_ambari.settings.autostart' as action from sdpms.config_detail where akey='ambari.settings.autostart'

