
call add_index( 'conf_cluster', 'idx_clsuter_name', ' (cluster_name)' );
call add_index( 'conf_cluster', 'idx_created_time', ' (created_time)' );
call add_index( 'conf_cluster_vm', 'idx_cluster_id', ' (cluster_id)' );
call add_index( 'conf_cluster_vm_data_volume', 'idx_vm_conf_id', '(vm_conf_id)' );
call add_index( 'conf_group_elastic_scaling', 'idx_cluster_id', '(cluster_id )' );
call add_index( 'conf_cluster_host_group_apps_config', 'idx_cluster_id ', '(cluster_id )' );
call add_index( 'conf_cluster_apps_config', 'idx_cluster_id', '(cluster_id)' );
call add_index( 'conf_cluster_script', 'idx_conf_script_id', '(conf_script_id)' );
call add_index( 'conf_scaling_vm', 'idx_cluster_id_task_id', '(cluster_id,task_id)' );
call add_index( 'info_cluster_vm', 'idx_cluster_id', '(cluster_id)' );
call add_index( 'info_cluster_vm', 'idx_scaleout_task_id', '(scaleout_task_id)' );
call add_index( 'info_cluster_vm', 'idx_scalein_task_id', '(scalein_task_id)' );
call add_index( 'conf_cluster_host_group', 'idx_cluster_id', '(cluster_id)' );
call add_index( 'conf_group_elastic_scaling_rule', 'idx_cluster_id', '(cluster_id)' );
call add_index( 'conf_group_elastic_scaling_rule', 'idx_group_es_id', '(group_es_id)' );
call add_index( 'conf_scaling_task', 'idx_cluster_id', '(cluster_id)' );
call add_index( 'conf_scaling_task_vm', 'idx_task_id', '(task_id)' );
