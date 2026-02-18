update base_cluster_operation_template_activity
set sort_no=sort_no+1
where template_id='3fe546f4-9544-11ed-922d-6045bdc792d8'
and sort_no>(select sort_no from (select sort_no from base_cluster_operation_template_activity where template_id='3fe546f4-9544-11ed-922d-6045bdc792d8' and activity_name='ambariAddHosts') b)
and not exists(select 1 from(select 1 from base_cluster_operation_template_activity where template_id='3fe546f4-9544-11ed-922d-6045bdc792d8' and activity_name='ambariAddHostsToConfigGroup') a);

INSERT INTO base_cluster_operation_template_activity (activity_id,template_id,activity_type,activity_cnname,activity_name,sort_no,timeout,createdby,created_time)
select 'e3096985-954e-11ed-922d-6045bdc792d8','3fe546f4-9544-11ed-922d-6045bdc792d8','clusterservice','将实例添加到ambari配置组中','ambariAddHostsToConfigGroup',sort_no+1,0,'system',null
from base_cluster_operation_template_activity
where activity_name='ambariAddHosts' and not exists(select 1 from base_cluster_operation_template_activity where template_id='3fe546f4-9544-11ed-922d-6045bdc792d8' and activity_name='ambariAddHostsToConfigGroup');
