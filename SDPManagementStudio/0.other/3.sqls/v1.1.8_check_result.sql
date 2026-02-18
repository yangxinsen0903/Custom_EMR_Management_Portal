-- check
select 'base_release_vm_img' as table_name,if(count(*)=4,true,false) as check_result,'update' as action
from base_release_vm_img brvi where release_version = 'SDP-1.0' and vm_role in ('ambari', 'core', 'master', 'task')
and os_imageid = '/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourceGroups/rg-sdp-dev-test/providers/Microsoft.Compute/galleries/sig_sdp_images/images/sunbox-ubuntu/versions/1.17.2';


