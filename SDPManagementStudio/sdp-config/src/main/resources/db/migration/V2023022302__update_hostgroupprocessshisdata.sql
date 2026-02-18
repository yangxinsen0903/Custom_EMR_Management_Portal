replace into conf_cluster_host_group (group_id,cluster_id,group_name,vm_role,state,ins_count)
select md5(concat(ccv.cluster_id,LOWER(ccv.group_name)))as group_id,ccv.cluster_id,group_name,ccv.vm_role,2 as state,ccv.count as ins_count
from conf_cluster_vm  ccv
where ccv.cluster_id not in (select cluster_id from conf_cluster_host_group)
group by ccv.cluster_id,ccv.group_name,ccv.vm_role,count;

update conf_cluster_vm set group_id=md5(concat(cluster_id,LOWER(group_name))) where group_id is null;
update info_cluster_vm set group_id=md5(concat(cluster_id,LOWER(group_name))) where group_id is null ;

