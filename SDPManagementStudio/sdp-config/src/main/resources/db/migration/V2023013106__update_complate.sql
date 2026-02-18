update  conf_cluster_vm set group_name =vm_role
where group_name is null ;

update info_cluster_vm set default_username =(select avalue from config_detail where akey ='vm.username' and application='sdp-compose')
where default_username is null  or default_username ='';

update info_cluster_vm set purchase_type=1
where purchase_type is null  or purchase_type='';

update info_cluster_vm set group_name =vm_role
where group_name is null or group_name =''